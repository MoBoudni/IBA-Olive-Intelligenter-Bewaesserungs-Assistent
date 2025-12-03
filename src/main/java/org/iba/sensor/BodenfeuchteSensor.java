package org.iba.sensor;

import org.iba.exception.SensorFehlerException;
import java.util.Random;

/**
 * Simulierter Bodenfeuchte-Sensor.
 * Liefert einen zufälligen Messwert im Bereich von 10.0% bis 70.0%.
 * Kann in 5% der Fälle einen simulierten Fehler werfen.
 */
public class BodenfeuchteSensor {

    private final Random random = new Random();

    /**
     * Liest einen simulierten Messwert der Bodenfeuchte aus.
     * @return Der gemessene Bodenfeuchtewert in Prozent (10.0 bis 70.0).
     * @throws SensorFehlerException falls die Sensor-Simulation einen Fehler wirft.
     */
    public double messWertLesen() throws SensorFehlerException {
        // Simuliere einen Hardware-Fehler in ca. 5% der Fälle
        if (random.nextDouble() < 0.05) {
            throw new SensorFehlerException("Sensor-Hardwarefehler oder Kommunikationsabbruch simuliert.");
        }

        // Generiere einen realistischen Bodenfeuchtewert
        // Werte zwischen 10.0 und 70.0
        return 10.0 + (70.0 - 10.0) * random.nextDouble();
    }
}