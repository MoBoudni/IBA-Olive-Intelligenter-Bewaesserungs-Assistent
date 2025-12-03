package org.iba.exception;

/**
 * Benutzerdefinierte Exception für Fehler, die beim Lesen des Bodenfeuchte-Sensors auftreten.
 * Dies simuliert Hardware- oder Kommunikationsprobleme.
 */
public class SensorFehlerException extends Exception {

    /**
     * Erstellt eine neue SensorFehlerException mit der angegebenen Detailnachricht.
     *
     * @param message Die Detailnachricht, die den Fehler beschreibt.
     */
    public SensorFehlerException(String message) {
        super(message);
    }

    /**
     * Erstellt eine neue SensorFehlerException mit der angegebenen Detailnachricht
     * und der zugrunde liegenden Ursache (Throwable).
     *
     * @param message Die Detailnachricht, die den Fehler beschreibt.
     * @param cause Die Ursache dieser Exception.
     */
    public SensorFehlerException(String message, Throwable cause) {
        super(message, cause);
    }
}