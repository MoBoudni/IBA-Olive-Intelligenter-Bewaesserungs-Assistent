package org.iba.model;

/**
 * Repräsentiert eine Parzelle oder Baumgruppe mit altersabhängigem Wasserbedarf. [cite: 12]
 */
public class Olivenbaum {

    private String name;
    private int alter; // Alter in Jahren
    private double basisBedarf; // Basiswasserbedarf in Litern

    /**
     * Konstruktor für die Olivenbaum-Entität.
     * @param name Name der Parzelle oder Baumgruppe
     * @param alter Alter des Baumes/der Gruppe in Jahren
     * @param basisBedarf Standard-Wasserbedarf (z.B. 30-50 L/Tag)
     */
    public Olivenbaum(String name, int alter, double basisBedarf) {
        this.name = name;
        this.alter = alter;
        this.basisBedarf = basisBedarf;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public int getAlter() {
        return alter;
    }

    public double getBasisBedarf() {
        return basisBedarf;
    }

    // --- Setters ---

    public void setName(String name) {
        this.name = name;
    }

    public void setAlter(int alter) {
        this.alter = alter;
    }

    public void setBasisBedarf(double basisBedarf) {
        this.basisBedarf = basisBedarf;
    }

    @Override
    public String toString() {
        return "Olivenbaum [Name=" + name + ", Alter=" + alter + " Jahre, Basisbedarf=" + basisBedarf + " L]";
    }
}
