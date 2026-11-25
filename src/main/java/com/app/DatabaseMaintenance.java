package com.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Aplica operaciones pendientes de restauración o borrado antes de abrir H2.
 * Se ejecuta al iniciar, cuando todavía no hay conexiones a la base.
 */
public final class DatabaseMaintenance {

    private DatabaseMaintenance() {
    }

    public static void procesar() {
        try {
            Path data = AppPaths.dataDir();
            Files.createDirectories(data);

            Path borrar = data.resolve(".delete-request");
            if (Files.exists(borrar)) {
                Files.deleteIfExists(AppPaths.databaseFile());
                Files.deleteIfExists(data.resolve("database.trace.db"));
                Files.deleteIfExists(data.resolve("database.lock.db"));
                Files.deleteIfExists(borrar);
                System.out.println("Base de datos eliminada. Se creará una base vacía al iniciar.");
            }

            Path restaurar = data.resolve(".restore-request");
            if (Files.exists(restaurar)) {
                String archivo = Files.readString(restaurar, StandardCharsets.UTF_8).trim();
                Path origen = AppPaths.backupsDir().resolve(archivo).normalize();
                Path backups = AppPaths.backupsDir().toAbsolutePath().normalize();
                if (archivo.startsWith("database-")
                        && archivo.endsWith(".mv.db")
                        && origen.startsWith(backups)
                        && Files.isRegularFile(origen)) {
                    Files.deleteIfExists(data.resolve("database.lock.db"));
                    Files.deleteIfExists(data.resolve("database.trace.db"));
                    Files.copy(origen, AppPaths.databaseFile(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Base restaurada desde " + archivo);
                } else {
                    System.err.println("No se pudo restaurar el backup solicitado: " + archivo);
                }
                Files.deleteIfExists(restaurar);
            }
        } catch (IOException e) {
            System.err.println("No se pudieron aplicar las tareas de mantenimiento: " + e.getMessage());
        }
    }
}
