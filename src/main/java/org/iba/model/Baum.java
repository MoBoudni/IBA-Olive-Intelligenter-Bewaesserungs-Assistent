package org.iba.model;

/**
 * Repräsentiert einen einzelnen Baum auf einer Parzelle.
 * Diese Klasse bildet die SQL-Tabelle 'baum' ab und integriert
 * die domänenspezifischen Felder (wie basisBedarf) und Validierung.
 */
public class Baum {

    private int baumId;
    private int parzelleId;      // Verweis auf die Parzelle, auf der der Baum steht
    private int alterJahre;      // Alter in Jahren (entspricht 'alter' aus Olivenbaum)
    private int pflanzenartId;   // Verweis auf die Pflanzenart (z.B. Olivenbaum)
    private double basisBedarf;  // Basis-Wasserbedarf in Litern pro Tag (aus Olivenbaum)

    /**
     * Standard-Konstruktor (für ORM-Frameworks und das Laden aus der DB)
     */
    public Baum() {
    }

    /**
     * Konstruktor mit allen Feldern (für das Laden aus der DB)
     * Führt KEINE Validierung durch, da die Daten aus der Datenbank als valide gelten.
     */
    public Baum(int baumId, int parzelleId, int alterJahre, int pflanzenartId, double basisBedarf) {
        this.baumId = baumId;
        this.parzelleId = parzelleId;
        this.alterJahre = alterJahre;
        this.pflanzenartId = pflanzenartId;
        this.basisBedarf = basisBedarf;
    }

    /**
     * Konstruktor zur Erstellung eines neuen Baum-Objekts mit Validierung.
     * Führt die Validierung aus der ursprünglichen Olivenbaum-Klasse durch.
     */
    public Baum(int parzelleId, int alterJahre, int pflanzenartId, double basisBedarf) {
        if (alterJahre < 0) {
            throw new IllegalArgumentException("Das Alter muss 0 oder größer sein.");
        }
        if (basisBedarf <= 0.0) {
            throw new IllegalArgumentException("Der Basis-Wasserbedarf muss positiv sein.");
        }

        this.parzelleId = parzelleId;
        this.alterJahre = alterJahre;
        this.pflanzenartId = pflanzenartId;
        this.basisBedarf = basisBedarf;
    }


    // --- Getter und Setter ---

    public int getBaumId() {
        return baumId;
    }

    public void setBaumId(int baumId) {
        this.baumId = baumId;
    }

    public int getParzelleId() {
        return parzelleId;
    }

    public void setParzelleId(int parzelleId) {
        this.parzelleId = parzelleId;
    }

    public int getAlterJahre() {
        return alterJahre;
    }

    public void setAlterJahre(int alterJahre) {
        this.alterJahre = alterJahre;
    }

    public int getPflanzenartId() {
        return pflanzenartId;
    }

    public void setPflanzenartId(int pflanzenartId) {
        this.pflanzenartId = pflanzenartId;
    }

    public double getBasisBedarf() {
        return basisBedarf;
    }

    public void setBasisBedarf(double basisBedarf) {
        this.basisBedarf = basisBedarf;
    }

    @Override
    public String toString() {
        return "Baum{" +
                "id=" + baumId +
                ", parzelleId=" + parzelleId +
                ", alterJahre=" + alterJahre +
                ", pflanzenartId=" + pflanzenartId +
                ", basisBedarf=" + basisBedarf + "l/Tag" +
                '}';
    }
}