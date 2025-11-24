package org.iba.sensor;

import java.util.Random;

/**
 * Simuliert einen Bodenfeuchtesensor.
 * Der Messwert liegt zwischen ca. 10.0 (trocken) und 95.0 (gesättigt).
 */
public class BodenfeuchteSensor implements Sensor {

    private final Random random = new Random();

    /**
     * Simuliert das Lesen eines Bodenfeuchtemesswerts
     * @return Ein zufälliger Wert zwischen 10.0 und 95.0, um reale Schwankungen abzubilden.
     */
    @Override
    public double messWertLesen() {
        // Simuliert einen realistischen Bereich von 10% bis 95%
        return 10.0 + (95.0 - 10.0);
    }
}
