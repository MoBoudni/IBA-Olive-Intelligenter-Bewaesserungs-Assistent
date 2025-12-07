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

    // --- DATENBANK KONFIGURATION (Muss in ein zentrales Utility ausgelagert werden) ---
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
     * Basierend auf dem normalisierten Schema.
     */
    private void erstelleTabelleWennNichtVorhanden() {
        // Hinweis: Wir gehen davon aus, dass 'parzelle' und 'Pflanzenart' bereits existieren.
        String createTableSQL = "CREATE TABLE IF NOT EXISTS baum ("
                + "baum_id INT AUTO_INCREMENT PRIMARY KEY,"
                + "parzelle_id INT NOT NULL,"
                + "alter_jahre INT,"
                + "pflanzenart_id INT,"
                + "FOREIGN KEY (parzelle_id) REFERENCES parzelle(parzelle_id),"
                + "FOREIGN KEY (pflanzenart_id) REFERENCES Pflanzenart(pflanzenart_id)"
                + ")";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            // System.out.println("Datenbanktabelle 'baum' ist bereit.");
        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Tabelle 'baum' nicht prüfen/erstellen: " + e.getMessage());
        }
    }

    // --- C (CREATE) ---

    /**
     * Speichert einen neuen Baum in der Datenbank.
     */
    public Baum speichere(Baum baum) {
        String sql = "INSERT INTO baum (parzelle_id, alter_jahre, pflanzenart_id) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, baum.getParzelleId());
            pstmt.setInt(2, baum.getAlterJahre());
            pstmt.setInt(3, baum.getPflanzenartId());

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
        String sql = "SELECT baum_id, parzelle_id, alter_jahre, pflanzenart_id FROM baum";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int baumId = rs.getInt("baum_id");
                int parzelleId = rs.getInt("parzelle_id");
                int alterJahre = rs.getInt("alter_jahre");
                int pflanzenartId = rs.getInt("pflanzenart_id");

                Baum baum = new Baum(baumId, parzelleId, alterJahre, pflanzenartId);
                baeume.add(baum);
            }
        } catch (SQLException e) {
            System.err.println("FEHLER beim Laden aller Bäume: " + e.getMessage());
        }
        return baeume;
    }

    // --- U (UPDATE) ---

    /**
     * Aktualisiert die Daten eines vorhandenen Baumes.
     */
    public boolean aktualisiere(Baum baum) {
        String sql = "UPDATE baum SET parzelle_id = ?, alter_jahre = ?, pflanzenart_id = ? WHERE baum_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, baum.getParzelleId());
            pstmt.setInt(2, baum.getAlterJahre());
            pstmt.setInt(3, baum.getPflanzenartId());
            pstmt.setInt(4, baum.getBaumId());

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
}