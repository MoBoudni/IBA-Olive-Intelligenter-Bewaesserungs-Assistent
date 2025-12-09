package org.iba.db;

import org.iba.db.transaction.TransactionManager;
import org.iba.exception.*;
import org.iba.model.Baum;
import org.iba.model.Parzelle;
import org.iba.util.ExceptionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vollständiges ParzelleRepository mit transaktionalen Operationen.
 */
public class ParzelleRepository extends BaseRepository {

    // ========================================================================
    // TRANSAKTIONELLE METHODEN
    // ========================================================================

    /**
     * Speichert eine Parzelle mit allen ihren Bäumen in einer Transaktion.
     */
    public Parzelle speichereParzelleMitBaeumen(Parzelle parzelle, List<Baum> baeume)
            throws DatabaseException, ValidationException, BusinessException {

        // Validierung
        validateParzelle(parzelle);
        validateBaeume(baeume);

        // Transaktionale Operation
        return TransactionManager.executeInTransaction(connection -> {
            try {
                // 1. Parzelle speichern
                int parzelleId = speichereParzelleInConnection(connection, parzelle);

                // 2. Bäume speichern
                for (Baum baum : baeume) {
                    baum.setParzelleId(parzelleId);
                    speichereBaumInConnection(connection, baum);
                }

                // 3. Parzellen-Zähler aktualisieren
                aktualisiereAnzahlBaeumeInConnection(connection, parzelleId, baeume.size());

                // 4. Gespeichertes Objekt zurückgeben
                return ladeParzelleAusConnection(connection, parzelleId);

            } catch (SQLException e) {
                throw ExceptionUtils.wrapSQLException(e, "Speichern von Parzelle mit Bäumen");
            }
        });
    }

    /**
     * Löscht eine Parzelle mit allen zugehörigen Bäumen und Messwerten in einer Transaktion.
     */
    public boolean loescheParzelleKomplett(int parzelleId)
            throws DatabaseException, BusinessException {

        return TransactionManager.executeInTransaction(connection -> {
            try {
                // 1. Existenz prüfen
                if (!parzelleExistiertInConnection(connection, parzelleId)) {
                    throw new BusinessException("Parzelle mit ID " + parzelleId + " existiert nicht");
                }

                // 2. Abhängigkeiten löschen
                loescheMesswerteInConnection(connection, parzelleId);
                loescheBaeumeInConnection(connection, parzelleId);

                // 3. Parzelle löschen
                int affectedRows = loescheParzelleInConnection(connection, parzelleId);

                return affectedRows > 0;

            } catch (SQLException e) {
                throw ExceptionUtils.wrapSQLException(e, "Löschen von Parzelle komplett");
            }
        });
    }

    /**
     * Transferiert Bäume von einer Parzelle zur anderen in einer Transaktion.
     */
    public void transferiereBaeume(int vonParzelleId, int zuParzelleId, List<Integer> baumIds)
            throws DatabaseException, BusinessException {

        TransactionManager.executeInTransaction(connection -> {
            try {
                // 1. Validierung
                if (!parzelleExistiertInConnection(connection, vonParzelleId)) {
                    throw new BusinessException("Quell-Parzelle existiert nicht: " + vonParzelleId);
                }
                if (!parzelleExistiertInConnection(connection, zuParzelleId)) {
                    throw new BusinessException("Ziel-Parzelle existiert nicht: " + zuParzelleId);
                }
                if (vonParzelleId == zuParzelleId) {
                    throw new BusinessException("Quell- und Ziel-Parzelle müssen unterschiedlich sein");
                }

                // 2. Bäume transferieren
                for (Integer baumId : baumIds) {
                    transferiereBaumInConnection(connection, baumId, vonParzelleId, zuParzelleId);
                }

                // 3. Zähler aktualisieren
                int anzahlTransferiert = baumIds.size();
                aktualisiereAnzahlBaeumeInConnection(connection, vonParzelleId, -anzahlTransferiert);
                aktualisiereAnzahlBaeumeInConnection(connection, zuParzelleId, anzahlTransferiert);

                return null; // Void-Operation

            } catch (SQLException e) {
                throw ExceptionUtils.wrapSQLException(e, "Transfer von Bäumen");
            }
        });
    }

    // ========================================================================
    // INTERNE METHODEN FÜR TRANSAKTIONEN (Connection-basiert)
    // ========================================================================

    private int speichereParzelleInConnection(Connection connection, Parzelle parzelle)
            throws SQLException {

        String sql = "INSERT INTO parzelle (name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, parzelle.getName());
            stmt.setInt(2, parzelle.getAnzahlBaeume());
            stmt.setDouble(3, parzelle.getFlaecheQm());
            stmt.setString(4, parzelle.getKlimaZone());
            stmt.setInt(5, parzelle.getBesitzerId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Parzelle konnte nicht gespeichert werden");
            }

            // Generierte ID holen
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Konnte keine ID für Parzelle generieren");
                }
            }
        }
    }

    private void speichereBaumInConnection(Connection connection, Baum baum) throws SQLException {
        String sql = "INSERT INTO baum (parzelle_id, alter_jahre, pflanzenart_id, basis_bedarf) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, baum.getParzelleId());
            stmt.setInt(2, baum.getAlterJahre());
            stmt.setInt(3, baum.getPflanzenartId());
            stmt.setDouble(4, baum.getBasisBedarf());

            stmt.executeUpdate();
        }
    }

    private void aktualisiereAnzahlBaeumeInConnection(Connection connection, int parzelleId, int delta)
            throws SQLException {

        String sql = "UPDATE parzelle SET anzahl_baeume = anzahl_baeume + ? WHERE parzelle_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setInt(2, parzelleId);
            stmt.executeUpdate();
        }
    }

    private Parzelle ladeParzelleAusConnection(Connection connection, int parzelleId) throws SQLException {
        String sql = "SELECT * FROM parzelle WHERE parzelle_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parzelleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapToParzelle(rs);
                }
            }
        }
        return null;
    }

    private boolean parzelleExistiertInConnection(Connection connection, int parzelleId) throws SQLException {
        String sql = "SELECT 1 FROM parzelle WHERE parzelle_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parzelleId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void loescheMesswerteInConnection(Connection connection, int parzelleId) throws SQLException {
        String sql = "DELETE FROM messwerte WHERE parzelle_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parzelleId);
            stmt.executeUpdate();
        }
    }

    private void loescheBaeumeInConnection(Connection connection, int parzelleId) throws SQLException {
        String sql = "DELETE FROM baum WHERE parzelle_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parzelleId);
            stmt.executeUpdate();
        }
    }

    private int loescheParzelleInConnection(Connection connection, int parzelleId) throws SQLException {
        String sql = "DELETE FROM parzelle WHERE parzelle_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parzelleId);
            return stmt.executeUpdate();
        }
    }

    private void transferiereBaumInConnection(Connection connection, int baumId, int vonParzelleId, int zuParzelleId)
            throws SQLException, BusinessException {

        // Prüfe ob Baum existiert und zur Quell-Parzelle gehört
        String checkSql = "SELECT 1 FROM baum WHERE baum_id = ? AND parzelle_id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, baumId);
            checkStmt.setInt(2, vonParzelleId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    throw new BusinessException(
                            String.format("Baum %d gehört nicht zur Parzelle %d oder existiert nicht",
                                    baumId, vonParzelleId));
                }
            }
        }

        // Transfer durchführen
        String updateSql = "UPDATE baum SET parzelle_id = ? WHERE baum_id = ?";

        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            updateStmt.setInt(1, zuParzelleId);
            updateStmt.setInt(2, baumId);
            updateStmt.executeUpdate();
        }
    }

    // ========================================================================
    // VALIDIERUNG
    // ========================================================================

    private void validateParzelle(Parzelle parzelle) throws ValidationException {
        if (parzelle.getName() == null || parzelle.getName().trim().isEmpty()) {
            throw new ValidationException("name", parzelle.getName(),
                    "Parzellenname darf nicht leer sein");
        }

        if (parzelle.getFlaecheQm() <= 0) {
            throw new ValidationException("flaecheQm", parzelle.getFlaecheQm(),
                    "Fläche muss größer als 0 sein");
        }

        if (parzelle.getBesitzerId() <= 0) {
            throw new ValidationException("besitzerId", parzelle.getBesitzerId(),
                    "Ungültige Besitzer-ID");
        }
    }

    private void validateBaeume(List<Baum> baeume) throws ValidationException {
        if (baeume == null) {
            throw new ValidationException("baeume", null, "Bäume-Liste darf nicht null sein");
        }

        for (int i = 0; i < baeume.size(); i++) {
            Baum baum = baeume.get(i);

            if (baum.getAlterJahre() < 0) {
                throw new ValidationException("baeume[" + i + "].alterJahre", baum.getAlterJahre(),
                        "Alter darf nicht negativ sein");
            }

            if (baum.getBasisBedarf() <= 0) {
                throw new ValidationException("baeume[" + i + "].basisBedarf", baum.getBasisBedarf(),
                        "Basisbedarf muss größer als 0 sein");
            }
        }
    }

    // ========================================================================
    // EINFACHE CRUD-OPERATIONEN (abwärtskompatibel)
    // ========================================================================

    public Parzelle speichere(Parzelle parzelle) throws DatabaseException, ValidationException {
        validateParzelle(parzelle);

        String sql = "INSERT INTO parzelle (name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            int affectedRows = executeUpdate(sql,
                    parzelle.getName(),
                    parzelle.getAnzahlBaeume(),
                    parzelle.getFlaecheQm(),
                    parzelle.getKlimaZone(),
                    parzelle.getBesitzerId());

            if (affectedRows == 0) {
                throw new DatabaseException("Parzelle konnte nicht gespeichert werden",
                        null, -1);
            }

            return findeLetzteParzelle();

        } catch (DatabaseException e) {
            if (e.getCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) e.getCause();
                if (ExceptionUtils.isConstraintViolation(sqlEx) &&
                        sqlEx.getErrorCode() == 1062) {
                    throw new ValidationException("name", parzelle.getName(),
                            "Eine Parzelle mit diesem Namen existiert bereits");
                }
            }
            throw e;
        }
    }

    private Parzelle findeLetzteParzelle() throws DatabaseException {
        String sql = "SELECT * FROM parzelle ORDER BY parzelle_id DESC LIMIT 1";

        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return mapToParzelle(rs);
            }
            return null;
        });
    }

    public List<Parzelle> findAlle() throws DatabaseException {
        String sql = "SELECT * FROM parzelle ORDER BY name";

        return executeQuery(sql, rs -> {
            List<Parzelle> parzellen = new ArrayList<>();
            while (rs.next()) {
                parzellen.add(mapToParzelle(rs));
            }
            return parzellen;
        });
    }

    private Parzelle mapToParzelle(ResultSet rs) throws SQLException {
        return new Parzelle(
                rs.getInt("parzelle_id"),
                rs.getString("name"),
                rs.getInt("anzahl_baeume"),
                rs.getDouble("flaeche_qm"),
                rs.getString("klima_zone"),
                rs.getInt("besitzer_id")
        );
    }
}