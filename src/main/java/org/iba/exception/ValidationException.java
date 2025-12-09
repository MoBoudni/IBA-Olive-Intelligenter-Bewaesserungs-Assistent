package org.iba.exception;

/**
 * Wird geworfen, wenn Eingabedaten oder Geschäftsobjekte
 * gegen Validierungsregeln verstoßen.
 */
public class ValidationException extends IbaException {

    private final String fieldName;
    private final Object invalidValue;

    public ValidationException(String fieldName, Object invalidValue, String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public ValidationException(String fieldName, Object invalidValue, String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}