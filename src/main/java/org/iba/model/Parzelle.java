package org.iba.model;

/**
 * Repräsentiert eine Parzelle (Anbaufläche) im "Intelligenten Bewässerungs-Assistenten".
 * Diese Klasse ist das Gegenstück zur SQL-Tabelle 'parzelle' und bildet das Datenmodell ab.
 */
public class Parzelle {

    // Primary key from the database (parzelle_id)
    private int parzelleId;

    // Unique name of the plot
    private String name;

    // Number of trees planted
    private int anzahlBaeume;

    // Area in square meters (flaeche_qm)
    private double flaecheQm;

    // Climate zone as a string (not yet normalized)
    private String klimaZone;

    // Foreign key to the owner
    private int besitzerId;

    /**
     * Standard constructor
     */
    public Parzelle() {
    }

    /**
     * Constructor with all fields
     */
    public Parzelle(int parzelleId, String name, int anzahlBaeume, double flaecheQm, String klimaZone, int besitzerId) {
        this.parzelleId = parzelleId;
        this.name = name;
        this.anzahlBaeume = anzahlBaeume;
        this.flaecheQm = flaecheQm;
        this.klimaZone = klimaZone;
        this.besitzerId = besitzerId;
    }

    // --- Getter and Setter ---

    public int getParzelleId() {
        return parzelleId;
    }

    public void setParzelleId(int parzelleId) {
        this.parzelleId = parzelleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAnzahlBaeume() {
        return anzahlBaeume;
    }

    public void setAnzahlBaeume(int anzahlBaeume) {
        this.anzahlBaeume = anzahlBaeume;
    }

    public double getFlaecheQm() {
        return flaecheQm;
    }

    public void setFlaecheQm(double flaecheQm) {
        this.flaecheQm = flaecheQm;
    }

    public String getKlimaZone() {
        return klimaZone;
    }

    public void setKlimaZone(String klimaZone) {
        this.klimaZone = klimaZone;
    }

    public int getBesitzerId() {
        return besitzerId;
    }

    public void setBesitzerId(int besitzerId) {
        this.besitzerId = besitzerId;
    }

    @Override
    public String toString() {
        return "Parzelle{" +
                "id=" + parzelleId +
                ", name='" + name + '\'' +
                ", flaeche=" + flaecheQm + "qm" +
                ", zone='" + klimaZone + '\'' +
                '}';
    }
}