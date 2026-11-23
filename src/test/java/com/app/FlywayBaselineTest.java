package com.app;

import org.flywaydb.core.Flyway;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Simula una base creada con la version anterior de la app (esquema viejo, sin
 * historial de Flyway) y verifica que el baseline no borre datos y aplique V2.
 */
class FlywayBaselineTest {

    @TempDir
    Path tempDir;

    @Test
    void unaBaseExistenteSinHistorialSeBaselineaSinPerderDatos() throws Exception {
        String url = "jdbc:h2:file:" + tempDir.resolve("database") + ";DB_CLOSE_DELAY=0";

        // 1. Base "legada": esquema viejo ya creado y un producto cargado.
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            try (Reader script = new InputStreamReader(
                    getClass().getResourceAsStream("/db/migration/V1__baseline.sql"),
                    StandardCharsets.UTF_8)) {
                RunScript.execute(conn, script);
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("INSERT INTO producto (nombre, sku, precio, stock, categoria) " +
                        "VALUES ('Producto legado', 'LEG-001', 123456, 7, 'Prueba')");
            }
        }

        // 2. Flyway encuentra el esquema sin historial: debe baselinarse en 1 y aplicar V2.
        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
        flyway.migrate();

        assertEquals("6", flyway.info().current().getVersion().getVersion());

        // 3. Los datos siguen ahi y el historial registra baseline + migracion.
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM producto WHERE sku = 'LEG-001'")) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT \"version\", \"type\" FROM \"flyway_schema_history\" " +
                    "WHERE \"version\" IS NOT NULL ORDER BY \"installed_rank\"")) {
                assertTrue(rs.next());
                assertEquals("1", rs.getString(1));
                assertEquals("BASELINE", rs.getString(2));
                assertTrue(rs.next());
                assertEquals("2", rs.getString(1));
                assertEquals("SQL", rs.getString(2));
            }
            stmt.execute("SHUTDOWN");
        }
    }
}
