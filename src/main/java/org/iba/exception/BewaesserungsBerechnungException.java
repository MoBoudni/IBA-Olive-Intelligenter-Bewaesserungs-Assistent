package org.iba.exception;

/**
 * Spezifische Exception für Fehler bei der Bewässerungsberechnung.
 * Enthält zusätzliche Informationen über die betroffene Parzelle.
 */
public class BewaesserungsBerechnungException extends IbaException {

    private final int parzelleId;

    public BewaesserungsBerechnungException(String message, int parzelleId) {
        super(ErrorCode.BUSINESS_ERROR, message);
        this.parzelleId = parzelleId;
    }

    public BewaesserungsBerechnungException(String message, int parzelleId, ErrorCode errorCode) {
        super(errorCode, message);
        this.parzelleId = parzelleId;
    }

    public BewaesserungsBerechnungException(String message, int parzelleId, Throwable cause) {
        super(ErrorCode.BUSINESS_ERROR, message, cause);
        this.parzelleId = parzelleId;
    }

    public BewaesserungsBerechnungException(String message, int parzelleId, ErrorCode errorCode, Throwable cause) {
        super(errorCode, message, cause);
        this.parzelleId = parzelleId;
    }

    public int getParzelleId() {
        return parzelleId;
    }

    @Override
    public String getMessage() {
        return String.format("[Parzelle ID: %d] %s", parzelleId, super.getMessage());
    }
}