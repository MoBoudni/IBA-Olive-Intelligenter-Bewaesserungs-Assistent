package org.iba.db;

import org.iba.model.Parzelle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository-Klasse für den Datenbankzugriff auf Parzellen-Daten (DAO-Muster).
 * Aktualisiert für die Tabelle 'parzelle' mit den Spalten:
 * parzelle_id, name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id
 */
public class ParzelleRepository {

    // --- DATENBANK KONFIGURATION ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";
    // ---------------------------------------------------------

    public ParzelleRepository() {
        // Optionale Überprüfung beim Start (hier angepasst auf das aktuelle Schema)
        erstelleTabelleWennNichtVorhanden();
    }

    /**
     * Stellt die Verbindung zur MySQL-Datenbank her.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Erstellt die 'parzelle'-Tabelle, falls sie nicht existiert.
     * Aktualisiert auf das neue Schema.
     */
    private void erstelleTabelleWennNichtVorhanden() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS parzelle ("
                + "parzelle_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL UNIQUE,"
                + "anzahl_baeume INT,"
                + "flaeche_qm DECIMAL(10,2) NOT NULL,"
                + "klima_zone VARCHAR(50),"
                + "besitzer_id INT,"
                + "adresse_id INT" // Falls in DB vorhanden, sonst optional
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            // Nur zur Info, keine Ausgabe im Erfolgsfall, um die Konsole sauber zu halten
        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Tabelle 'parzelle' nicht prüfen/erstellen: " + e.getMessage());
        }
    }

    // --- C (CREATE) ---

    /**
     * Speichert eine neue Parzelle in der Datenbank.
     */
    public Parzelle speichere(Parzelle parzelle) {
        // WICHTIG: Hier werden die aktuellen Spaltennamen verwendet
        String sql = "INSERT INTO parzelle (name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, parzelle.getName());
            pstmt.setInt(2, parzelle.getAnzahlBaeume());
            pstmt.setDouble(3, parzelle.getFlaecheQm());       // Korrigierter Getter
            pstmt.setString(4, parzelle.getKlimaZone());       // Neues Feld
            pstmt.setInt(5, parzelle.getBesitzerId());         // Neues Feld

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Setzt die generierte ID zurück in das Objekt (parzelle_id)
                        parzelle.setParzelleId(rs.getInt(1));
                    }
                }
            }
            return parzelle;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Speichern der Parzelle: " + e.getMessage());
            return null;
        }
    }

    // --- R (READ) ---

    /**
     * Ruft alle Parzellen ab.
     */
    public List<Parzelle> findAlle() {
        List<Parzelle> parzellen = new ArrayList<>();
        // SQL Query mit den korrekten Spaltennamen
        String sql = "SELECT parzelle_id, name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id FROM parzelle";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Mapping der DB-Spalten auf die Java-Klasse
                int id = rs.getInt("parzelle_id");
                String name = rs.getString("name");
                int anzahl = rs.getInt("anzahl_baeume");
                double flaeche = rs.getDouble("flaeche_qm");   // Achten Sie auf den Unterstrich in SQL
                String klima = rs.getString("klima_zone");
                int besitzer = rs.getInt("besitzer_id");

                Parzelle parzelle = new Parzelle(id, name, anzahl, flaeche, klima, besitzer);
                parzellen.add(parzelle);
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Laden der Parzellen: " + e.getMessage());
        }
        return parzellen;
    }

    // --- U (UPDATE) ---

    /**
     * Aktualisiert eine vorhandene Parzelle.
     */
    public boolean aktualisiere(Parzelle parzelle) {
        // Update Query mit allen Feldern
        String sql = "UPDATE parzelle SET name = ?, anzahl_baeume = ?, flaeche_qm = ?, klima_zone = ?, besitzer_id = ? WHERE parzelle_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, parzelle.getName());
            pstmt.setInt(2, parzelle.getAnzahlBaeume());
            pstmt.setDouble(3, parzelle.getFlaecheQm());
            pstmt.setString(4, parzelle.getKlimaZone());
            pstmt.setInt(5, parzelle.getBesitzerId());
            pstmt.setInt(6, parzelle.getParzelleId()); // Wichtig: ID für WHERE-Klausel

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Aktualisieren der Parzelle (ID: " + parzelle.getParzelleId() + "): " + e.getMessage());
            return false;
        }
    }

    // --- D (DELETE) ---

    /**
     * Löscht eine Parzelle anhand ihrer ID.
     */
    public boolean loesche(int parzelleId) {
        // Wichtig: Spaltenname ist parzelle_id
        String sql = "DELETE FROM parzelle WHERE parzelle_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parzelleId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Löschen der Parzelle (ID: " + parzelleId + "): " + e.getMessage());
            return false;
        }
    }
}