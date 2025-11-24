package org.iba.sensor;

/**
 * Definiert das allgemeine Interface für Sensoren im IBA-Olive System (MVP+)
 * dies ermöglicht eine spätere einfache Erweiterung um reale Sensordaten.
 */
public interface Sensor {

    /**
     * Liest den aktuellen Messwert des Sensors.
     * @return Der gemessene Wert (z.B. Bodenfeuchte in Prozent).
     */
    double messWertLesen();
}
