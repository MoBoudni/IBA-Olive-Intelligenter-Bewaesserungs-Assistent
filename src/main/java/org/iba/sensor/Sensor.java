package org.iba.sensor;

import org.iba.exception.SensorFehlerException;

/**
 * Definiert das allgemeine Interface für alle Sensoren im IBA-Olive System (MVP+).
 * Dieses Interface abstrahiert den Zugriff auf Hardware und ermöglicht eine
 * spätere nahtlose Integration von echten IoT-Sensoren oder Simulationen,
 * ohne die Geschäftslogik ändern zu müssen.
 */
public interface Sensor {

    /**
     * Liest den aktuellen Messwert des Sensors aus.
     *
     * @return Der gemessene Wert als Gleitkommazahl (z.B. Bodenfeuchte in Prozent).
     * @throws SensorFehlerException Wenn der Sensor nicht erreichbar ist, ein Timeout auftritt
     * oder ungültige Daten liefert.
     */
    double messWertLesen() throws SensorFehlerException;
}