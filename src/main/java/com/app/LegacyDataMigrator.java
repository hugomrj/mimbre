package com.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * Migra una sola vez la base y los backups que quedaron en la ubicacion vieja
 * (./data o ./target/data) a la carpeta estable del usuario. Nunca borra ni
 * sobrescribe el origen.
 */
public final class LegacyDataMigrator {

    private LegacyDataMigrator() {
    }

    public static void migrar() {
        migrar(Paths.get("").toAbsolutePath(), AppPaths.dataDir(), AppPaths.backupsDir());
    }

    static void migrar(Path cwd, Path dataDir, Path backupsDir) {
        try {
            Files.createDirectories(dataDir);
            Files.createDirectories(backupsDir);

            Path destino = dataDir.resolve("database.mv.db");
            if (!Files.exists(destino)) {
                for (Path origen : candidatos(cwd, "database.mv.db")) {
                    if (Files.isRegularFile(origen)) {
                        Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Datos migrados de " + origen + " a " + destino);
                        break;
                    }
                }
            }

            Path plantillaDestino = dataDir.resolve("database_empty.mv.db");
            if (!Files.exists(plantillaDestino)) {
                for (Path origen : candidatos(cwd, "database_empty.mv.db")) {
                    if (Files.isRegularFile(origen)) {
                        Files.copy(origen, plantillaDestino, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                }
            }

            copiarBackups(cwd, backupsDir);
        } catch (IOException e) {
            System.err.println("No se pudieron migrar los datos anteriores: " + e.getMessage());
        }
    }

    private static List<Path> candidatos(Path cwd, String archivo) {
        return List.of(
                cwd.resolve("data").resolve(archivo),
                cwd.resolve("target").resolve("data").resolve(archivo)
        );
    }

    private static void copiarBackups(Path cwd, Path backupsDir) throws IOException {
        for (Path origen : List.of(cwd.resolve("backups"), cwd.resolve("target").resolve("backups"))) {
            if (!Files.isDirectory(origen)) {
                continue;
            }
            try (Stream<Path> archivos = Files.list(origen)) {
                for (Path archivo : archivos.toList()) {
                    String nombre = archivo.getFileName().toString();
                    if (!nombre.startsWith("database-") || !nombre.endsWith(".mv.db")) {
                        continue;
                    }
                    Path destino = backupsDir.resolve(nombre);
                    if (!Files.exists(destino)) {
                        Files.copy(archivo, destino);
                    }
                }
            }
        }
    }
}
