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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            borrarAntiguos();
        } catch (IOException e) {
            LOG.warn("No se pudo crear el backup de la base: {}", e.getMessage());
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
