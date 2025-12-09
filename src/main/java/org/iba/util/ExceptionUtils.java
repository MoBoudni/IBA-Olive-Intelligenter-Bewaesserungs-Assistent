package org.iba.util;

import org.iba.exception.DatabaseException;
import org.iba.exception.IbaException;

import java.sql.SQLException;

/**
 * Utility-Klasse für Exception-Transformation und -Handling.
 */
public class ExceptionUtils {

    private ExceptionUtils() {
        // Utility-Klasse, kein Instanzierung
    }

    /**
     * Wandelt eine SQLException in eine DatabaseException um.
     */
    public static DatabaseException wrapSQLException(SQLException sqlEx, String operation) {
        return new DatabaseException(
                String.format("Fehler bei Datenbankoperation '%s': %s",
                        operation, sqlEx.getMessage()),
                sqlEx.getSQLState(),
                sqlEx.getErrorCode(),
                sqlEx
        );
    }

    /**
     * Überprüft, ob es sich um einen Constraint-Violation-Fehler handelt.
     */
    public static boolean isConstraintViolation(SQLException sqlEx) {
        return sqlEx.getErrorCode() == 1062 || // Duplicate entry
                sqlEx.getErrorCode() == 1452 || // Foreign key constraint
                sqlEx.getErrorCode() == 1451 || // Cannot delete/update parent row
                "23000".equals(sqlEx.getSQLState()); // Integrity constraint violation
    }

    /**
     * Erstellt eine benutzerfreundliche Fehlermeldung aus einer Exception.
     */
    public static String getUserFriendlyMessage(Throwable throwable) {
        if (throwable instanceof IbaException) {
            IbaException ibaEx = (IbaException) throwable;
            return String.format("%s (Code: %s)",
                    ibaEx.getMessage(),
                    ibaEx.getErrorCode().getCode());
        } else if (throwable instanceof SQLException) {
            SQLException sqlEx = (SQLException) throwable;
            if (sqlEx.getErrorCode() == 1062) {
                return "Der Datensatz existiert bereits (Duplikat).";
            } else if (sqlEx.getErrorCode() == 1452) {
                return "Ungültige Referenz (Fremdschlüssel-Fehler).";
            } else {
                return "Datenbankfehler: " + sqlEx.getMessage();
            }
        } else {
            return "Ein unerwarteter Fehler ist aufgetreten: " + throwable.getMessage();
        }
    }

    /**
     * Loggt eine Exception mit vollem Stacktrace.
     */
    public static void logException(String context, Throwable throwable) {
        System.err.println("=== EXCEPTION in " + context + " ===");
        System.err.println("Message: " + throwable.getMessage());
        System.err.println("Type: " + throwable.getClass().getName());

        if (throwable.getCause() != null) {
            System.err.println("Cause: " + throwable.getCause().getMessage());
        }

        System.err.println("Stacktrace:");
        throwable.printStackTrace(System.err);
        System.err.println("=== END EXCEPTION ===");
    }
}