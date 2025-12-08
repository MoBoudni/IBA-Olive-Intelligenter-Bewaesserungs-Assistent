package org.iba.model;

/**
 * Speichert die aktuellen meteorologischen Daten, die für die Bedarfsberechnung relevant sind.
 */
public class Messwerte {
    private final double temperatur; // Temperatur in Celsius
    private final double niederschlag; // Niederschlag in mm - NICHT STATIC!

    /**
     * Konstruktor für Wetterdaten.
     * Führt eine grundlegende Validierung der Eingabeparameter durch.
     *
     * @param temperatur Aktuelle Temperatur in °C.
     * @param niederschlag Niederschlag in mm (letzte 24h).
     * @throws IllegalArgumentException wenn die Eingabewerte ungültig sind.
     */
    public Messwerte(double temperatur, double niederschlag) {
        if (temperatur < -50.0 || temperatur > 60.0) { // Realistische Spannen
            throw new IllegalArgumentException("Temperatur liegt außerhalb des realistischen Bereichs (-50°C bis 60°C).");
        }
        if (niederschlag < 0.0) {
            throw new IllegalArgumentException("Niederschlag kann nicht negativ sein.");
        }

        this.temperatur = temperatur;
        this.niederschlag = niederschlag; // NICHT Messwerte.niederschlag = niederschlag
    }

    // Getter-Methoden - NICHT STATIC!

    public double getTemperatur() {
        return temperatur;
    }

    public double getNiederschlag() {
        return niederschlag;
    }
}