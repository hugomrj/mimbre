package com.app;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Rutas estables de la aplicacion. No dependen del directorio desde el que se
 * ejecute: la base y los backups viven en la carpeta del usuario.
 */
public final class AppPaths {

    private static final String APP_DIR = ".mimbre";

    private AppPaths() {
    }

    public static Path baseDir() {
        return Paths.get(System.getProperty("user.home"), APP_DIR);
    }

    public static Path dataDir() {
        return baseDir().resolve("data");
    }

    public static Path backupsDir() {
        return baseDir().resolve("backups");
    }

    public static Path databaseFile() {
        return dataDir().resolve("database.mv.db");
    }

    public static Path emptyDatabaseFile() {
        return dataDir().resolve("database_empty.mv.db");
    }

    public static Path initMarker() {
        return dataDir().resolve(".initialized");
    }
}
