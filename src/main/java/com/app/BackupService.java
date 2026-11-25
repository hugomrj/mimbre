package com.app;

import io.micronaut.context.annotation.Value;
import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Copia la base H2 a ./backups al arrancar y borra los respaldos vencidos.
 * Se invoca desde DatabaseInitializer para asegurar que la base ya exista.
 */
@Singleton
public class BackupService {

    private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final DataSource dataSource;
    private final boolean enabled;
    private final Path source;
    private final Path backupDir;
    private final int retentionDays;

    public BackupService(DataSource dataSource,
                         @Value("${app.backup.enabled:true}") boolean enabled,
                         @Value("${app.backup.source:./data/database.mv.db}") String source,
                         @Value("${app.backup.dir:./backups}") String backupDir,
                         @Value("${app.backup.retention-days:30}") int retentionDays) {
        this.dataSource = dataSource;
        this.enabled = enabled;
        this.source = Paths.get(source);
        this.backupDir = Paths.get(backupDir);
        this.retentionDays = retentionDays;
    }

    @Connectable
    public void respaldarDiario() {
        if (existeBackupDeHoy()) {
            depurarDuplicadosDeHoy();
            return;
        }
        respaldar();
    }

    @Connectable
    public void respaldar() {
        if (!enabled) {
            return;
        }
        if (!Files.isRegularFile(source)) {
            LOG.debug("Todavia no hay base para respaldar: {}", source.toAbsolutePath());
            return;
        }
        try {
            // Fuerza a H2 a bajar los cambios a disco antes de copiar el archivo.
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CHECKPOINT");
            } catch (SQLException e) {
                LOG.warn("No se pudo hacer CHECKPOINT antes del backup: {}", e.getMessage());
            }

            Files.createDirectories(backupDir);
            Path destino = backupDir.resolve("database-" + LocalDateTime.now().format(STAMP) + ".mv.db");
            Files.copy(source, destino, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Backup de la base creado en {}", destino.toAbsolutePath());
            depurarDuplicadosDeHoy();
            borrarAntiguos();
        } catch (IOException e) {
            LOG.warn("No se pudo crear el backup de la base: {}", e.getMessage());
        }
    }

    public List<BackupDto> listar() {
        if (!Files.isDirectory(backupDir)) {
            return List.of();
        }
        try (Stream<Path> archivos = Files.list(backupDir)) {
            return archivos
                    .filter(p -> p.getFileName().toString().startsWith("database-"))
                    .filter(p -> p.getFileName().toString().endsWith(".mv.db"))
                    .sorted(Comparator.comparing((Path p) -> p.getFileName().toString()).reversed())
                    .map(this::toDto)
                    .toList();
        } catch (IOException e) {
            LOG.warn("No se pudieron listar los backups: {}", e.getMessage());
            return List.of();
        }
    }

    public boolean prepararRestauracion(String archivo) {
        if (archivo == null || !archivo.startsWith("database-") || !archivo.endsWith(".mv.db")) {
            return false;
        }
        Path base = backupDir.toAbsolutePath().normalize();
        Path origen = backupDir.resolve(archivo).normalize();
        if (!origen.startsWith(base) || !Files.isRegularFile(origen)) {
            return false;
        }
        try {
            Files.createDirectories(AppPaths.dataDir());
            Files.writeString(AppPaths.dataDir().resolve(".restore-request"), archivo, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            LOG.warn("No se pudo preparar la restauración: {}", e.getMessage());
            return false;
        }
    }

    public void prepararBorrado() throws IOException {
        Files.createDirectories(AppPaths.dataDir());
        Files.writeString(AppPaths.dataDir().resolve(".delete-request"),
                LocalDateTime.now().toString(), StandardCharsets.UTF_8);
    }

    private BackupDto toDto(Path archivo) {
        String nombre = archivo.getFileName().toString();
        LocalDateTime fecha = LocalDateTime.now();
        try {
            String sello = nombre.replace("database-", "").replace(".mv.db", "");
            fecha = LocalDateTime.parse(sello, STAMP);
        } catch (Exception ignored) {
            try {
                fecha = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(Files.getLastModifiedTime(archivo).toMillis()),
                        ZoneId.systemDefault());
            } catch (IOException ignored2) {
                // deja la fecha actual
            }
        }
        String tamano;
        try {
            long bytes = Files.size(archivo);
            tamano = String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } catch (IOException e) {
            tamano = "-";
        }
        return new BackupDto(nombre, fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), tamano);
    }

    private boolean existeBackupDeHoy() {
        return !backupsDeHoy().isEmpty();
    }

    private void depurarDuplicadosDeHoy() {
        List<Path> backups = backupsDeHoy();
        if (backups.size() <= 1) {
            return;
        }
        // El nombre incluye la hora: el último es el más reciente.
        backups.sort((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()));
        for (int i = 1; i < backups.size(); i++) {
            try {
                Files.deleteIfExists(backups.get(i));
                LOG.info("Backup duplicado del día eliminado: {}", backups.get(i).getFileName());
            } catch (IOException e) {
                LOG.warn("No se pudo eliminar el backup duplicado {}: {}",
                        backups.get(i).getFileName(), e.getMessage());
            }
        }
    }

    private List<Path> backupsDeHoy() {
        if (!Files.isDirectory(backupDir)) {
            return List.of();
        }
        String prefijo = "database-" + LocalDate.now().format(DAY) + "-";
        try (Stream<Path> archivos = Files.list(backupDir)) {
            return archivos
                    .filter(p -> p.getFileName().toString().startsWith(prefijo))
                    .filter(p -> p.getFileName().toString().endsWith(".mv.db"))
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private void borrarAntiguos() throws IOException {
        if (retentionDays <= 0) {
            return;
        }
        long limite = System.currentTimeMillis() - retentionDays * 24L * 60L * 60L * 1000L;
        List<Path> vencidos;
        try (Stream<Path> archivos = Files.list(backupDir)) {
            vencidos = archivos
                    .filter(p -> p.getFileName().toString().startsWith("database-"))
                    .filter(p -> p.getFileName().toString().endsWith(".mv.db"))
                    .filter(p -> {
                        try {
                            return Files.getLastModifiedTime(p).toMillis() < limite;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .toList();
        }
        for (Path p : vencidos) {
            Files.deleteIfExists(p);
            LOG.info("Backup vencido eliminado: {}", p.getFileName());
        }
    }
}
