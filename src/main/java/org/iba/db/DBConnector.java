package org.iba.db;

import org.iba.exception.DatabaseException;
import org.iba.util.ExceptionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Zentrale Klasse zur Verwaltung von Datenbankverbindungen.
 * Unterstützt verschiedene Profile und Connection-Pooling (erweiterbar).
 */
public class DBConnector {

    // Konfigurationsprofile
    public enum Profile {
        DEVELOPMENT,
        TEST,
        PRODUCTION
    }

    // Aktives Profil (default: DEVELOPMENT)
    private static Profile activeProfile = Profile.DEVELOPMENT;

    // Konfiguration für verschiedene Profile
    private static final Properties DEV_CONFIG = new Properties();
    private static final Properties TEST_CONFIG = new Properties();
    private static final Properties PROD_CONFIG = new Properties();

    static {
        // Entwicklungsumgebung (deine lokale DB)
        DEV_CONFIG.setProperty("url", "jdbc:mysql://localhost:3306/IBA_Olive_DEV");
        DEV_CONFIG.setProperty("user", "root");
        DEV_CONFIG.setProperty("password", "Chakeb1978&");
        DEV_CONFIG.setProperty("charset", "UTF-8");
        DEV_CONFIG.setProperty("useSSL", "false");
        DEV_CONFIG.setProperty("serverTimezone", "UTC");

        // Testumgebung
        TEST_CONFIG.setProperty("url", "jdbc:mysql://localhost:3306/IBA_Olive_TEST");
        TEST_CONFIG.setProperty("user", "test_user");
        TEST_CONFIG.setProperty("password", "test123");
        TEST_CONFIG.setProperty("charset", "UTF-8");
        TEST_CONFIG.setProperty("useSSL", "false");
        TEST_CONFIG.setProperty("serverTimezone", "UTC");

        // Produktionsumgebung (Beispiel)
        PROD_CONFIG.setProperty("url", "jdbc:mysql://prod-db.example.com:3306/IBA_Olive_PROD");
        PROD_CONFIG.setProperty("user", "prod_user");
        PROD_CONFIG.setProperty("password", "secure_password");
        PROD_CONFIG.setProperty("charset", "UTF-8");
        PROD_CONFIG.setProperty("useSSL", "true");
        PROD_CONFIG.setProperty("serverTimezone", "UTC");
    }

    /**
     * Setzt das aktive Konfigurationsprofil.
     */
    public static void setActiveProfile(Profile profile) {
        activeProfile = profile;
        System.out.println("DB Profil gewechselt zu: " + profile);
    }

    /**
     * Gibt die aktuelle Konfiguration zurück.
     */
    private static Properties getCurrentConfig() {
        switch (activeProfile) {
            case TEST:
                return TEST_CONFIG;
            case PRODUCTION:
                return PROD_CONFIG;
            case DEVELOPMENT:
            default:
                return DEV_CONFIG;
        }
    }

    /**
     * Stellt eine Verbindung zur MySQL-Datenbank her.
     * @return Eine aktive JDBC Connection.
     * @throws DatabaseException falls die Verbindung fehlschlägt.
     */
    public static Connection getConnection() throws DatabaseException {
        Properties config = getCurrentConfig();

        try {
            // Treiber laden (für Kompatibilität)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Verbindung herstellen mit zusätzlichen Parametern
            String url = config.getProperty("url");
            String user = config.getProperty("user");
            String password = config.getProperty("password");

            // Zusätzliche Connection Properties
            Properties connectionProps = new Properties();
            connectionProps.setProperty("user", user);
            connectionProps.setProperty("password", password);
            connectionProps.setProperty("characterEncoding", config.getProperty("charset", "UTF-8"));
            connectionProps.setProperty("useSSL", config.getProperty("useSSL", "false"));
            connectionProps.setProperty("serverTimezone", config.getProperty("serverTimezone", "UTC"));

            // Debug-Info
            if (activeProfile == Profile.DEVELOPMENT) {
                System.out.println("Verbindung wird hergestellt zu: " + url);
            }

            Connection connection = DriverManager.getConnection(url, connectionProps);

            // Optimale Connection-Einstellungen
            connection.setAutoCommit(true); // Standard: Auto-Commit
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            return connection;

        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver nicht gefunden.", e);
        } catch (SQLException e) {
            throw ExceptionUtils.wrapSQLException(e, "Verbindungsaufbau zu " + activeProfile);
        }
    }

    /**
     * Testet die Datenbankverbindung.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2); // 2 Sekunden Timeout
        } catch (Exception e) {
            System.err.println("Verbindungstest fehlgeschlagen: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gibt Informationen über die aktuelle Konfiguration zurück.
     */
    public static String getConnectionInfo() {
        Properties config = getCurrentConfig();
        return String.format(
                "DB Profil: %s\nURL: %s\nUser: %s",
                activeProfile,
                config.getProperty("url"),
                config.getProperty("user")
        );
    }
}