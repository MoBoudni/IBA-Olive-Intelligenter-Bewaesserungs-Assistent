package org.iba.db;

import org.iba.model.Wetterdaten;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Repository-Klasse für den Datenbankzugriff auf Messwerte (Wetterdaten).
 * Enthält Methoden für CRUD-Operationen auf der Tabelle 'messwerte'.
 */
public class MesswerteRepository {

    // --- DATENBANK KONFIGURATION (Sollte zentralisiert werden) ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";
    // ---------------------------------------------------------

    public MesswerteRepository() {
        erstelleTabelleWennNichtVorhanden();
    }

    /**
     * Stellt die Verbindung zur MySQL-Datenbank her.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Erstellt die 'messwerte'-Tabelle in der Datenbank, falls sie noch nicht existiert.
     * Nimmt an, dass die Tabelle 'parzelle' existiert.
     */
    private void erstelleTabelleWennNichtVorhanden() {
        // Die Tabelle speichert die Wetterdaten (Temperatur, Niederschlag) für eine bestimmte Parzelle zu einem bestimmten Zeitpunkt.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS messwerte ("
                + "messwerte_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "parzelle_id INT NOT NULL,"
                + "zeitstempel TIMESTAMP DEFAULT CURRENT_TIMESTAMP," // Wichtig für historische Daten
                + "temperatur DECIMAL(5, 2) NOT NULL,"
                + "niederschlag DECIMAL(5, 2) NOT NULL,"
                + "FOREIGN KEY (parzelle_id) REFERENCES parzelle(parzelle_id)"
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            // System.out.println("Datenbanktabelle 'messwerte' ist bereit.");
        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Tabelle 'messwerte' nicht prüfen/erstellen: " + e.getMessage());
        }
    }

    // --- C (CREATE) ---

    /**
     * Speichert neue Wetterdaten zusammen mit der Parzellen-ID in der Datenbank.
     * Der Zeitstempel wird automatisch von der Datenbank gesetzt.
     *
     * @param daten Die zu speichernden Wetterdaten.
     * @param parzelleId Die ID der Parzelle, für die die Messung gilt.
     * @return true, wenn die Speicherung erfolgreich war.
     */
    public boolean speichere(Wetterdaten daten, int parzelleId) {
        String sql = "INSERT INTO messwerte (parzelle_id, temperatur, niederschlag) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parzelleId);
            pstmt.setDouble(2, daten.getTemperatur());
            pstmt.setDouble(3, daten.getNiederschlag());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Speichern der Wetterdaten: " + e.getMessage());
            return false;
        }
    }

    // --- R (READ) ---

    /**
     * Ruft die letzten Wetterdaten für eine bestimmte Parzelle ab.
     *
     * @param parzelleId Die ID der Parzelle.
     * @return Das neueste Wetterdaten-Objekt oder null, falls keine Daten gefunden.
     */
    public Wetterdaten findeLetzteMessung(int parzelleId) {
        // Findet die Messung mit dem neuesten Zeitstempel
        String sql = "SELECT temperatur, niederschlag FROM messwerte WHERE parzelle_id = ? ORDER BY zeitstempel DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parzelleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double temperatur = rs.getDouble("temperatur");
                    double niederschlag = rs.getDouble("niederschlag");
                    // Erstellt ein neues Wetterdaten-Objekt (muss die Validierung im Konstruktor bestehen)
                    return new Wetterdaten(temperatur, niederschlag);
                }
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Abrufen der letzten Messung für Parzelle " + parzelleId + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Datenbankwert ist ungültig (Wetterdaten-Modell Validierung fehlgeschlagen): " + e.getMessage());
        }
        return null;
    }


    // Wichtiger Hinweis: UPDATE und DELETE sind für historische Messwerte nicht sinnvoll
    // und werden hier nicht implementiert, da Messungen unveränderlich sein sollten.
}