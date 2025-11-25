package org.iba.model;

/**
 * [cite_start]Repräsentiert eine Parzelle oder Baumgruppe mit altersabhängigem Wasserbedarf. [cite: 12]
 * <p>
 * Diese Klasse dient als zentrales Datenmodell (POJO) und stellt sicher,
 * dass nur valide Parameter (positives Alter, nicht-leerer Name) für eine
 * Parzelle erfasst werden.
 */
public class Olivenbaum {

    /** Der Name der Parzelle oder Baumgruppe. */
    private String name;

    /** Das Alter des Baumes oder der Gruppe in Jahren. */
    private int alter;

    /** Der grundlegende Wasserbedarf in Litern pro Tag (ohne Faktoren). */
    private double basisBedarf;

    /**
     * Initialisiert ein neues Olivenbaum-Objekt mit Validierung der Eingabewerte.
     *
     * @param name        Der Name der Parzelle oder Baumgruppe (darf nicht null oder leer sein).
     * @param alter       Das Alter des Baumes in Jahren (darf nicht negativ sein).
     * @param basisBedarf Der Standard-Wasserbedarf in Litern (muss größer als 0 sein, z.B. 30-50 L/Tag).
     * @throws IllegalArgumentException Wenn der Name leer ist, das Alter negativ ist
     * oder der Basisbedarf <= 0 ist.
     */
    public Olivenbaum(String name, int alter, double basisBedarf) {
        // Exception Handling: Validierung der Eingabedaten (Guard Clauses)
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Name der Parzelle darf nicht leer sein.");
        }
        if (alter < 0) {
            throw new IllegalArgumentException("Das Alter darf nicht negativ sein: " + alter);
        }
        if (basisBedarf <= 0) {
            throw new IllegalArgumentException("Der Basisbedarf muss größer als 0 sein: " + basisBedarf);
        }

        this.name = name;
        this.alter = alter;
        this.basisBedarf = basisBedarf;
    }

    // --- Getters ---

    /**
     * Gibt den Namen der Parzelle zurück.
     *
     * @return Der Name als String.
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt das Alter der Bäume zurück.
     *
     * @return Das Alter in Jahren.
     */
    public int getAlter() {
        return alter;
    }

    /**
     * Gibt den konfigurierten Basiswasserbedarf zurück.
     *
     * @return Der Basisbedarf in Litern.
     */
    public double getBasisBedarf() {
        return basisBedarf;
    }

    // --- Setters ---

    /**
     * Setzt einen neuen Namen für die Parzelle.
     *
     * @param name Der neue Name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Aktualisiert das Alter der Bäume.
     *
     * @param alter Das neue Alter in Jahren.
     */
    public void setAlter(int alter) {
        this.alter = alter;
    }

    /**
     * Setzt den Basiswasserbedarf neu.
     *
     * @param basisBedarf Der neue Bedarf in Litern.
     */
    public void setBasisBedarf(double basisBedarf) {
        this.basisBedarf = basisBedarf;
    }

    /**
     * Liefert eine textuelle Repräsentation des Olivenbaums.
     *
     * @return Ein String mit Name, Alter und Basisbedarf.
     */
    @Override
    public String toString() {
        return "Olivenbaum [Name=" + name + ", Alter=" + alter + " Jahre, Basisbedarf=" + basisBedarf + " L]";
    }
}