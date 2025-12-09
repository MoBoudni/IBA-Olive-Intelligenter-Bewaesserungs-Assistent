package org.iba.exception;

/**
 * Wird geworfen, wenn Verstöße gegen Geschäftsregeln auftreten.
 * Beispiel: Bewässerung nicht möglich, weil Wasserreservoir leer ist.
 */
public class BusinessException extends IbaException {

    public BusinessException(String message) {
        super(ErrorCode.BUSINESS_ERROR, message);
    }

    public BusinessException(String message, Throwable cause) {
        super(ErrorCode.BUSINESS_ERROR, message, cause);
    }
}