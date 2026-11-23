package com.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyDataMigratorTest {

    @TempDir
    Path temp;

    @Test
    void migraBasePlantillaYBackupsSinSobrescribir() throws Exception {
        Path cwd = temp.resolve("proyecto");
        Path legacyData = cwd.resolve("target").resolve("data");
        Path legacyBackups = cwd.resolve("target").resolve("backups");
        Files.createDirectories(legacyData);
        Files.createDirectories(legacyBackups);
        Files.writeString(legacyData.resolve("database.mv.db"), "base-vieja");
        Files.writeString(legacyData.resolve("database_empty.mv.db"), "plantilla");
        Files.writeString(legacyBackups.resolve("database-20260101-000000.mv.db"), "backup");

        Path dataDir = temp.resolve("home").resolve(".mimbre").resolve("data");
        Path backupsDir = temp.resolve("home").resolve(".mimbre").resolve("backups");

        LegacyDataMigrator.migrar(cwd, dataDir, backupsDir);

        assertEquals("base-vieja", Files.readString(dataDir.resolve("database.mv.db")));
        assertEquals("plantilla", Files.readString(dataDir.resolve("database_empty.mv.db")));
        assertEquals("backup", Files.readString(backupsDir.resolve("database-20260101-000000.mv.db")));

        // Una segunda corrida no pisa lo que ya esta en la ubicacion nueva.
        Files.writeString(legacyData.resolve("database.mv.db"), "base-nueva");
        LegacyDataMigrator.migrar(cwd, dataDir, backupsDir);

        assertEquals("base-vieja", Files.readString(dataDir.resolve("database.mv.db")));
        assertTrue(Files.exists(dataDir.resolve("database.mv.db")));
    }
}
