package org.iba.sensor;

import org.iba.exception.SensorFehlerException;

/**
 * Interface für alle Sensor-Klassen im Bewässerungs-Assistenten.
 * Definiert den Vertrag für das Auslesen eines Messwertes.
 */
public interface Sensor {
    /**
     * Liest den aktuellen Messwert des Sensors aus.
     * Muss eine SensorFehlerException deklarieren, da Sensoren fehlschlagen können.
     *
     * @return Der aktuelle Messwert (z.B. Bodenfeuchte in Prozent).
     * @throws SensorFehlerException Wenn ein technischer Fehler beim Auslesen des Sensors auftritt.
     */
    double messWertLesen() throws SensorFehlerException;
}