package org.iba.db;

import org.iba.exception.DatabaseException;
import org.iba.exception.ValidationException;
import org.iba.model.Messwerte;
import org.iba.util.ExceptionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MesswerteRepository extends BaseRepository {

    public void speichere(Messwerte messwerte, int parzelleId)
            throws DatabaseException, ValidationException {

        // Validierung könnte hier sein, aber Messwerte-Klasse validiert bereits
        // Wir validieren nur die Parzellen-ID

        String sql = "INSERT INTO messwerte (parzelle_id, temperatur, niederschlag) " +
                "VALUES (?, ?, ?)";

        try {
            int affectedRows = executeUpdate(sql,
                    parzelleId,
                    messwerte.getTemperatur(),
                    messwerte.getNiederschlag());

            if (affectedRows == 0) {
                throw new DatabaseException("Messwerte konnten nicht gespeichert werden",
                        null, -1);
            }

        } catch (DatabaseException e) {
            if (e.getCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) e.getCause();
                if (ExceptionUtils.isConstraintViolation(sqlEx) &&
                        sqlEx.getErrorCode() == 1452) {
                    throw new ValidationException("parzelleId", parzelleId,
                            "Die angegebene Parzelle existiert nicht");
                }
            }
            throw e;
        }
    }

    public Messwerte findeLetzteMessung(int parzelleId) throws DatabaseException {
        String sql = "SELECT temperatur, niederschlag FROM messwerte " +
                "WHERE parzelle_id = ? " +
                "ORDER BY zeitstempel DESC LIMIT 1";

        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return new Messwerte(
                        rs.getDouble("temperatur"),
                        rs.getDouble("niederschlag")
                );
            }
            return null;
        }, parzelleId);
    }
}