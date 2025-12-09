package org.iba.db;

import org.iba.exception.DatabaseException;
import org.iba.exception.ValidationException;
import org.iba.model.Baum;
import org.iba.util.ExceptionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Vollständige BaumRepository-Implementierung mit allen benötigten Methoden.
 */
public class BaumRepository extends BaseRepository {

    /**
     * Speichert einen neuen Baum in der Datenbank.
     */
    public Baum speichere(Baum baum) throws DatabaseException, ValidationException {
        // Validierung
        if (baum.getAlterJahre() < 0) {
            throw new ValidationException("alterJahre", baum.getAlterJahre(),
                    "Alter darf nicht negativ sein");
        }

        if (baum.getBasisBedarf() <= 0) {
            throw new ValidationException("basisBedarf", baum.getBasisBedarf(),
                    "Basisbedarf muss größer als 0 sein");
        }

        String sql = "INSERT INTO baum (parzelle_id, alter_jahre, pflanzenart_id, basis_bedarf) " +
                "VALUES (?, ?, ?, ?)";

        try {
            // Mit RETURN_GENERATED_KEYS für die ID
            Connection conn = getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, baum.getParzelleId());
                stmt.setInt(2, baum.getAlterJahre());
                stmt.setInt(3, baum.getPflanzenartId());
                stmt.setDouble(4, baum.getBasisBedarf());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows == 0) {
                    throw new DatabaseException("Baum konnte nicht gespeichert werden",
                            null, -1);
                }

                // Generierte ID holen
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        baum.setBaumId(generatedKeys.getInt(1));
                    } else {
                        throw new DatabaseException("Konnte keine ID für Baum generieren",
                                null, -1);
                    }
                }

                return baum;
            }

        } catch (SQLException e) {
            // Spezifische Fehlerbehandlung
            if (ExceptionUtils.isConstraintViolation(e) && e.getErrorCode() == 1452) {
                throw new ValidationException("parzelleId", baum.getParzelleId(),
                        "Die angegebene Parzelle existiert nicht");
            }
            throw ExceptionUtils.wrapSQLException(e, "Speichern von Baum");
        }
    }

    /**
     * Findet einen Baum anhand seiner ID.
     */
    public Baum findById(int baumId) throws DatabaseException {
        String sql = "SELECT * FROM baum WHERE baum_id = ?";

        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return mapToBaum(rs);
            }
            return null;
        }, baumId);
    }

    /**
     * Findet alle Bäume einer Parzelle.
     */
    public List<Baum> findByParzelleId(int parzelleId) throws DatabaseException {
        String sql = "SELECT * FROM baum WHERE parzelle_id = ? ORDER BY baum_id";

        return executeQuery(sql, rs -> {
            List<Baum> baeume = new ArrayList<>();
            while (rs.next()) {
                baeume.add(mapToBaum(rs));
            }
            return baeume;
        }, parzelleId);
    }

    /**
     * Aktualisiert einen bestehenden Baum.
     */
    public boolean aktualisiere(Baum baum) throws DatabaseException, ValidationException {
        // Validierung
        if (baum.getAlterJahre() < 0) {
            throw new ValidationException("alterJahre", baum.getAlterJahre(),
                    "Alter darf nicht negativ sein");
        }

        if (baum.getBasisBedarf() <= 0) {
            throw new ValidationException("basisBedarf", baum.getBasisBedarf(),
                    "Basisbedarf muss größer als 0 sein");
        }

        String sql = "UPDATE baum SET parzelle_id = ?, alter_jahre = ?, " +
                "pflanzenart_id = ?, basis_bedarf = ? WHERE baum_id = ?";

        try {
            int affectedRows = executeUpdate(sql,
                    baum.getParzelleId(),
                    baum.getAlterJahre(),
                    baum.getPflanzenartId(),
                    baum.getBasisBedarf(),
                    baum.getBaumId());

            return affectedRows > 0;

        } catch (DatabaseException e) {
            if (e.getCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) e.getCause();
                if (ExceptionUtils.isConstraintViolation(sqlEx) && sqlEx.getErrorCode() == 1452) {
                    throw new ValidationException("parzelleId", baum.getParzelleId(),
                            "Die angegebene Parzelle existiert nicht");
                }
            }
            throw e;
        }
    }

    /**
     * Löscht einen Baum anhand seiner ID.
     */
    public boolean loesche(int baumId) throws DatabaseException {
        String sql = "DELETE FROM baum WHERE baum_id = ?";

        int affectedRows = executeUpdate(sql, baumId);
        return affectedRows > 0;
    }

    /**
     * Löscht alle Bäume einer Parzelle.
     */
    public boolean loescheAlleVonParzelle(int parzelleId) throws DatabaseException {
        String sql = "DELETE FROM baum WHERE parzelle_id = ?";

        int affectedRows = executeUpdate(sql, parzelleId);
        return affectedRows > 0;
    }

    /**
     * Findet alle Bäume (für Berechnungen).
     */
    public List<Baum> findAlle() throws DatabaseException {
        String sql = "SELECT * FROM baum ORDER BY baum_id";

        return executeQuery(sql, rs -> {
            List<Baum> baeume = new ArrayList<>();
            while (rs.next()) {
                baeume.add(mapToBaum(rs));
            }
            return baeume;
        });
    }

    /**
     * Mappt ein ResultSet zu einem Baum-Objekt.
     */
    private Baum mapToBaum(ResultSet rs) throws SQLException {
        return new Baum(
                rs.getInt("baum_id"),
                rs.getInt("parzelle_id"),
                rs.getInt("alter_jahre"),
                rs.getInt("pflanzenart_id"),
                rs.getDouble("basis_bedarf")
        );
    }
}