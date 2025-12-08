package org.iba.db;

import org.iba.model.Parzelle;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParzelleRepository {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";

    public ParzelleRepository() {
        erstelleTabelleWennNichtVorhanden();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void erstelleTabelleWennNichtVorhanden() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS parzelle ("
                + "parzelle_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL UNIQUE,"
                + "anzahl_baeume INT NOT NULL,"
                + "flaeche_qm DECIMAL(10,2) NOT NULL,"
                + "klima_zone VARCHAR(50) NOT NULL,"
                + "besitzer_id INT NOT NULL"
                + ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabelle 'parzelle' ist bereit.");
        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Tabelle 'parzelle' nicht erstellen: " + e.getMessage());
        }
    }

    public Parzelle speichere(Parzelle parzelle) {
        String sql = "INSERT INTO parzelle (name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, parzelle.getName());
            pstmt.setInt(2, parzelle.getAnzahlBaeume());
            pstmt.setDouble(3, parzelle.getFlaecheQm());
            pstmt.setString(4, parzelle.getKlimaZone());
            pstmt.setInt(5, parzelle.getBesitzerId());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        parzelle.setParzelleId(rs.getInt(1));
                    }
                }
            }
            return parzelle;
        } catch (SQLException e) {
            // Spezifische Behandlung für Duplicate Entry (MySQL Error Code 1062)
            if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate")) {
                // Optional: Loggen auf DEBUG-Level statt ERROR für erwartete Fälle
                System.err.println("INFO: Parzelle mit Namen '" + parzelle.getName() + "' existiert bereits (erwartet bei Duplikattests).");
            } else {
                // Echte Fehler loggen
                System.err.println("FEHLER beim Speichern der Parzelle '" + parzelle.getName() + "': " + e.getMessage());
            }
            return null;
        }
    }

    public List<Parzelle> findAlle() {
        List<Parzelle> parzellen = new ArrayList<>();
        String sql = "SELECT parzelle_id, name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id FROM parzelle";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Parzelle parzelle = new Parzelle(
                        rs.getInt("parzelle_id"),
                        rs.getString("name"),
                        rs.getInt("anzahl_baeume"),
                        rs.getDouble("flaeche_qm"),
                        rs.getString("klima_zone"),
                        rs.getInt("besitzer_id")
                );
                parzellen.add(parzelle);
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Laden der Parzellen: " + e.getMessage());
        }
        return parzellen;
    }

    public boolean aktualisiere(Parzelle parzelle) {
        String sql = "UPDATE parzelle SET name = ?, anzahl_baeume = ?, flaeche_qm = ?, klima_zone = ?, besitzer_id = ? WHERE parzelle_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, parzelle.getName());
            pstmt.setInt(2, parzelle.getAnzahlBaeume());
            pstmt.setDouble(3, parzelle.getFlaecheQm());
            pstmt.setString(4, parzelle.getKlimaZone());
            pstmt.setInt(5, parzelle.getBesitzerId());
            pstmt.setInt(6, parzelle.getParzelleId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FEHLER beim Aktualisieren der Parzelle: " + e.getMessage());
            return false;
        }
    }

    public boolean loesche(int parzelleId) {
        String sql = "DELETE FROM parzelle WHERE parzelle_id = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parzelleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("FEHLER beim Löschen der Parzelle: " + e.getMessage());
            return false;
        }
    }
}