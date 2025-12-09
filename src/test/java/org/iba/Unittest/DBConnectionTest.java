package org.iba.Unittest;

import org.iba.db.DBConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Testet die Datenbankverbindung.
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        System.out.println("=== Datenbank Verbindungstest ===");
        System.out.println(DBConnector.getConnectionInfo());

        // Verbindung testen
        if (DBConnector.testConnection()) {
            System.out.println("✓ Verbindung erfolgreich!");
            testDatabaseOperations();
        } else {
            System.err.println("✗ Verbindung fehlgeschlagen!");
        }
    }

    private static void testDatabaseOperations() {
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement()) {

            // Einfache Abfrage
            ResultSet rs = stmt.executeQuery("SELECT VERSION() as version");
            if (rs.next()) {
                System.out.println("MySQL Version: " + rs.getString("version"));
            }

            // Parzellen zählen
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM parzelle");
            if (rs.next()) {
                System.out.println("Anzahl Parzellen: " + rs.getInt("count"));
            }

            // Bäume zählen
            rs = stmt.executeQuery("SELECT COUNT(*) as count FROM baum");
            if (rs.next()) {
                System.out.println("Anzahl Bäume: " + rs.getInt("count"));
            }

        } catch (Exception e) {
            System.err.println("Fehler bei Datenbankoperationen: " + e.getMessage());
        }
    }
}