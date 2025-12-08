package org.iba.db;

import org.iba.model.Baum;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository-Klasse für den Datenbankzugriff auf Baum-Daten.
 * Enthält Methoden für CRUD-Operationen auf der Tabelle 'baum'.
 */
public class BaumRepository {

    // --- DATENBANK KONFIGURATION ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";
    // ---------------------------------------------------------

    public BaumRepository() {
        erstelleTabelleWennNichtVorhanden();
    }

    /**
     * Stellt die Verbindung zur MySQL-Datenbank her.
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Erstellt die 'baum'-Tabelle in der Datenbank, falls sie noch nicht existiert.
     */
    private void erstelleTabelleWennNichtVorhanden() {
        // Vereinfachte Version ohne Pflanzenart-FK, da die Tabelle nicht existiert
        String createTableSQL = "CREATE TABLE IF NOT EXISTS baum ("
                + "baum_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "parzelle_id INT NOT NULL,"
                + "alter_jahre INT NOT NULL,"
                + "pflanzenart_id INT NOT NULL,"
                + "basis_bedarf DECIMAL(10, 2) DEFAULT 0.0 NOT NULL,"
                + "FOREIGN KEY (parzelle_id) REFERENCES parzelle(parzelle_id)"
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Tabelle 'baum' ist bereit.");

        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Tabelle 'baum' nicht prüfen/erstellen: " + e.getMessage());
        }
    }

    // --- C (CREATE) ---

    /**
     * Speichert einen neuen Baum in der Datenbank.
     */
    public Baum speichere(Baum baum) {
        String sql = "INSERT INTO baum (parzelle_id, alter_jahre, pflanzenart_id, basis_bedarf) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, baum.getParzelleId());
            pstmt.setInt(2, baum.getAlterJahre());
            pstmt.setInt(3, baum.getPflanzenartId());
            pstmt.setDouble(4, baum.getBasisBedarf());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        baum.setBaumId(rs.getInt(1));
                    }
                }
            }
            return baum;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Speichern des Baumes: " + e.getMessage());
            return null;
        }
    }

    // --- R (READ) ---

    /**
     * Ruft alle Bäume aus der Datenbank ab.
     */
    public List<Baum> findAlle() {
        List<Baum> baeume = new ArrayList<>();
        String sql = "SELECT baum_id, parzelle_id, alter_jahre, pflanzenart_id, basis_bedarf FROM baum";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Baum baum = new Baum(
                        rs.getInt("baum_id"),
                        rs.getInt("parzelle_id"),
                        rs.getInt("alter_jahre"),
                        rs.getInt("pflanzenart_id"),
                        rs.getDouble("basis_bedarf")
                );
                baeume.add(baum);
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Laden aller Bäume: " + e.getMessage());
        }
        return baeume;
    }

    /**
     * Ruft einen Baum anhand seiner ID ab.
     */
    public Baum findById(int baumId) {
        String sql = "SELECT baum_id, parzelle_id, alter_jahre, pflanzenart_id, basis_bedarf FROM baum WHERE baum_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, baumId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Baum(
                        rs.getInt("baum_id"),
                        rs.getInt("parzelle_id"),
                        rs.getInt("alter_jahre"),
                        rs.getInt("pflanzenart_id"),
                        rs.getDouble("basis_bedarf")
                );
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Laden des Baumes mit ID " + baumId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Ruft alle Bäume einer bestimmten Parzelle ab.
     */
    public List<Baum> findByParzelleId(int parzelleId) {
        List<Baum> baeume = new ArrayList<>();
        String sql = "SELECT baum_id, parzelle_id, alter_jahre, pflanzenart_id, basis_bedarf FROM baum WHERE parzelle_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parzelleId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Baum baum = new Baum(
                        rs.getInt("baum_id"),
                        rs.getInt("parzelle_id"),
                        rs.getInt("alter_jahre"),
                        rs.getInt("pflanzenart_id"),
                        rs.getDouble("basis_bedarf")
                );
                baeume.add(baum);
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Laden der Bäume für Parzelle " + parzelleId + ": " + e.getMessage());
        }
        return baeume;
    }

    // --- U (UPDATE) ---

    /**
     * Aktualisiert die Daten eines vorhandenen Baumes.
     */
    public boolean aktualisiere(Baum baum) {
        String sql = "UPDATE baum SET parzelle_id = ?, alter_jahre = ?, pflanzenart_id = ?, basis_bedarf = ? WHERE baum_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, baum.getParzelleId());
            pstmt.setInt(2, baum.getAlterJahre());
            pstmt.setInt(3, baum.getPflanzenartId());
            pstmt.setDouble(4, baum.getBasisBedarf());
            pstmt.setInt(5, baum.getBaumId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Aktualisieren des Baumes (ID: " + baum.getBaumId() + "): " + e.getMessage());
            return false;
        }
    }

    // --- D (DELETE) ---

    /**
     * Löscht einen Baum anhand seiner ID.
     */
    public boolean loesche(int baumId) {
        String sql = "DELETE FROM baum WHERE baum_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, baumId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Löschen des Baumes (ID: " + baumId + "): " + e.getMessage());
            return false;
        }
    }

    /**
     * Löscht alle Bäume einer Parzelle.
     */
    public boolean loescheAlleVonParzelle(int parzelleId) {
        String sql = "DELETE FROM baum WHERE parzelle_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parzelleId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("FEHLER beim Löschen der Bäume für Parzelle " + parzelleId + ": " + e.getMessage());
            return false;
        }
    }
}