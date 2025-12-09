package org.iba.exception;

/**
 * Basis-Exception für alle fachlichen Fehler im IBA-Projekt.
 * Ermöglicht einheitliche Fehlerbehandlung über alle Schichten hinweg.
 */
public class IbaException extends Exception {

    private final ErrorCode errorCode;

    public IbaException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public IbaException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public IbaException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public IbaException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}