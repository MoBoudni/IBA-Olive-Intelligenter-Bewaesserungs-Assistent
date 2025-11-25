package org.iba.model;

/**
 * Kapselung meteorologischer Parameter für einen bestimmten Tag. [cite: 14]
 */
public class Wetterdaten {

    private double temperatur; // Temperatur in Celsius
    private double niederschlag; // Niederschlag in mm

    /**
     * Konstruktor für die Wetterdaten-Entität.
     * @param temperatur Aktuelle Tagestemperatur (°C)
     * @param niederschlag Niederschlagsmenge (mm)
     */
    public Wetterdaten(double temperatur, double niederschlag) {
        this.temperatur = temperatur;
        this.niederschlag = niederschlag;
    }

    // --- Getters ---

    public double getTemperatur() {
        return temperatur;
    }

    public double getNiederschlag() {
        return niederschlag;
    }

    @Override
    public String toString() {
        return "Wetterdaten [Temperatur=" + temperatur + "°C, Niederschlag=" + niederschlag + " mm]";
    }
}