package org.iba.exception;

/**
 * Wird geworfen, wenn Datenbankoperationen fehlschlagen.
 * Enthält SQL-Statuscodes und SQLState für genauere Fehleranalyse.
 */
public class DatabaseException extends IbaException {

    private final String sqlState;
    private final int errorCode;

    public DatabaseException(String message, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, message, cause);
        this.sqlState = null;
        this.errorCode = -1;
    }

    public DatabaseException(String message, String sqlState, int errorCode) {
        super(ErrorCode.DATABASE_ERROR, message);
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }

    public DatabaseException(String message, String sqlState, int errorCode, Throwable cause) {
        super(ErrorCode.DATABASE_ERROR, message, cause);
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getDatabaseErrorCode() {
        return errorCode;
    }
}