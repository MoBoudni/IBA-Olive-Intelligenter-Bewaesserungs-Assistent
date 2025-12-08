package org.iba.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hilfsklasse zur zentralen Verwaltung der Datenbankverbindung.
 */
public class DBConnector {

    // BITTE AN IHRE LOKALEN MYSQL-ZUGANGSDATEN ANPASSEN!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/iba_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";

    /**
     * Stellt eine Verbindung zur MySQL-Datenbank her.
     * @return Eine aktive JDBC Connection.
     * @throws SQLException falls die Verbindung fehlschlägt.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Optionale Registrierung des Treibers (seit JDBC 4.0 meist nicht mehr nötig)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver nicht gefunden.");
            throw new SQLException("JDBC Driver fehlt.", e);
        }

        System.out.println("Verbindung wird hergestellt zu: " + DB_URL);
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}