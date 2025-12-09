package org.iba.exception;

/**
 * Wird geworfen, wenn ein technischer Fehler beim Auslesen eines Sensors auftritt.
 * Kann verschiedene Ursachen haben: Verbindungsprobleme, Hardwarefehler, etc.
 */
public class SensorFehlerException extends IbaException {

    public SensorFehlerException(String message) {
        super(ErrorCode.SENSOR_ERROR, message);
    }

    public SensorFehlerException(String message, Throwable cause) {
        super(ErrorCode.SENSOR_ERROR, message, cause);
    }
}