package org.iba.exception;

/**
 * Eine geprüfte Exception (Checked Exception), die geworfen wird,
 * wenn ein Sensor nicht erreichbar ist oder fehlerhafte Werte liefert.
 */
public class SensorFehlerException extends Exception {

    public SensorFehlerException(String nachricht){
        super(nachricht);
    }

    public SensorFehlerException(String nachricht, Throwable ursache){
        super(nachricht, ursache);
    }
}
