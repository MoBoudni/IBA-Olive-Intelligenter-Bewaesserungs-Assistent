package org.iba.exception;

/**
 * Zentrale Enumeration aller Fehlercodes im System.
 * Ermöglicht strukturierte Fehlerbehandlung und Internationalisierung.
 */
public enum ErrorCode {

    // Allgemeine Fehler
    GENERAL_ERROR("ERR-0001", "Allgemeiner Fehler"),

    // Datenbank-Fehler
    DATABASE_ERROR("ERR-1001", "Datenbankfehler"),
    DATABASE_CONNECTION_ERROR("ERR-1002", "Datenbankverbindungsfehler"),
    DATABASE_QUERY_ERROR("ERR-1003", "Datenbankabfragefehler"),
    DATABASE_CONSTRAINT_VIOLATION("ERR-1004", "Datenbank-Constraint-Verletzung"),

    // Validierungsfehler
    VALIDATION_ERROR("ERR-2001", "Validierungsfehler"),
    INVALID_INPUT("ERR-2002", "Ungültige Eingabe"),
    MISSING_REQUIRED_FIELD("ERR-2003", "Pflichtfeld fehlt"),
    VALUE_OUT_OF_RANGE("ERR-2004", "Wert außerhalb des gültigen Bereichs"),

    // Sensor-Fehler
    SENSOR_ERROR("ERR-3001", "Sensorfehler"),
    SENSOR_TIMEOUT("ERR-3002", "Sensor-Antwortzeit überschritten"),
    SENSOR_CONNECTION_ERROR("ERR-3003", "Sensor-Verbindungsfehler"),
    SENSOR_INVALID_VALUE("ERR-3004", "Sensor liefert ungültigen Wert"),

    // Geschäftslogik-Fehler
    BUSINESS_ERROR("ERR-4001", "Geschäftsregel-Verletzung"),
    INSUFFICIENT_WATER("ERR-4002", "Unzureichende Wassermenge verfügbar"),
    PARZELLE_NOT_FOUND("ERR-4003", "Parzelle nicht gefunden"),
    BAUM_NOT_FOUND("ERR-4004", "Baum nicht gefunden"),

    // Externe Services
    EXTERNAL_SERVICE_ERROR("ERR-5001", "Externer Service-Fehler"),
    WEATHER_API_ERROR("ERR-5002", "Wetter-API Fehler"),

    // Authentifizierung/Autorisierung
    AUTHENTICATION_ERROR("ERR-6001", "Authentifizierungsfehler"),
    AUTHORIZATION_ERROR("ERR-6002", "Berechtigungsfehler"),

    // Systemfehler
    SYSTEM_ERROR("ERR-9001", "Systeminterner Fehler"),
    CONFIGURATION_ERROR("ERR-9002", "Konfigurationsfehler");

    private final String code;
    private final String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ErrorCode fromString(String code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return GENERAL_ERROR;
    }
}