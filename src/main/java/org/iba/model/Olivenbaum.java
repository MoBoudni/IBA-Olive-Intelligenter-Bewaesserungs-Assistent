package org.iba.model;

/**
 * Modelliert eine Olivenbaumparzelle oder eine Gruppe von Bäumen mit deren Eigenschaften.
 */
public class Olivenbaum {
    private final String name;
    private final int alter; // Alter in Jahren
    private final double basisBedarf; // Basis-Wasserbedarf in Litern pro Tag

    /**
     * Konstruktor für Olivenbaum.
     * Führt eine grundlegende Validierung der Eingabeparameter durch.
     *
     * @param name Name der Parzelle.
     * @param alter Alter der Bäume in Jahren.
     * @param basisBedarf Basis-Wasserbedarf in Litern pro Tag.
     * @throws IllegalArgumentException wenn die Eingabewerte ungültig sind.
     */
    public Olivenbaum(String name, int alter, double basisBedarf) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Name der Parzelle darf nicht leer sein.");
        }
        if (alter < 0) {
            throw new IllegalArgumentException("Das Alter muss 0 oder größer sein.");
        }
        if (basisBedarf <= 0.0) {
            throw new IllegalArgumentException("Der Basis-Wasserbedarf muss positiv sein.");
        }

        this.name = name;
        this.alter = alter;
        this.basisBedarf = basisBedarf;
    }

    // Getter-Methoden

    public String getName() {
        return name;
    }

    public int getAlter() {
        return alter;
    }

    public double getBasisBedarf() {
        return basisBedarf;
    }
}