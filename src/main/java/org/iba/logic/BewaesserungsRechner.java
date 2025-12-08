package org.iba.logic;

import org.iba.model.Baum;
import org.iba.model.Messwerte;

/**
 * Die Kernlogik zur Berechnung des täglichen Wasserbedarfs eines Olivenbaums
 * unter Berücksichtigung verschiedener Korrekturfaktoren (Alter, Temperatur,
 * Niederschlag und Bodenfeuchte).
 */
public class BewaesserungsRechner {

    // Reduktionsfaktor für Niederschlag (1 mm Niederschlag = 0.5 Liter Reduktion)
    private static final double NIEDERSCHLAG_REDUKTIONSFAKTOR = 0.5;

    /**
     * Berechnet den täglichen Wasserbedarf basierend auf Baum- und Wetterdaten
     * ohne Berücksichtigung der Bodenfeuchte (Fallback-Logik).
     *
     * @param baum Die Olivenbaum-Daten.
     * @param messwerte Die Wetterdaten (Temperatur, Niederschlag).
     * @return Der berechnete Wasserbedarf in Litern (mindestens 0.0).
     */
    public double berechneWasserbedarf(Baum baum, Messwerte messwerte) {
        double basisbedarf = baum.getBasisBedarf();

        // 1. Anwendung der Faktoren
        double faktorAlter = getAltersFaktor(baum.getAlterJahre());
        double faktorTemperatur = getTemperaturFaktor(messwerte.getTemperatur());

        double korrigierterBedarf = basisbedarf * faktorAlter * faktorTemperatur;

        // 2. Abzug des Niederschlags
        double abzugNiederschlag = messwerte.getNiederschlag() * NIEDERSCHLAG_REDUKTIONSFAKTOR;
        double endbedarf = korrigierterBedarf - abzugNiederschlag;

        // Der Bedarf darf nie negativ sein
        return Math.max(0.0, endbedarf);
    }

    /**
     * Berechnet den täglichen Wasserbedarf MIT Berücksichtigung der Bodenfeuchte.
     *
     * @param baum Die Olivenbaum-Daten.
     * @param messwerte Die Wetterdaten (Temperatur, Niederschlag).
     * @param bodenfeuchte Die gemessene Bodenfeuchte in Prozent (z.B. 45.5).
     * @return Der berechnete Wasserbedarf in Litern (mindestens 0.0).
     */
    public double berechneWasserbedarf(Baum baum, Messwerte messwerte, double bodenfeuchte) {
        // Zuerst den Bedarf ohne Feuchtekontrolle berechnen
        double bedarfOhneFeuchte = berechneWasserbedarf(baum, messwerte);

        // Wenn der Bedarf durch Regen bereits 0.0 ist, bleibt er 0.0
        if (bedarfOhneFeuchte <= 0.0) {
            return 0.0;
        }

        // Korrektur durch Bodenfeuchte
        double faktorFeuchte = getBodenfeuchteFaktor(bodenfeuchte);

        // Anwendung des Feuchtefaktors auf den bereits durch Alter/Temperatur/Regen
        // korrigierten Bedarf.
        return bedarfOhneFeuchte * faktorFeuchte;
    }


    // ========================================================================
    //                              KORREKTURFAKTOREN
    // ========================================================================

    /**
     * Ermittelt den Korrekturfaktor basierend auf dem Alter des Olivenbaums.
     */
    private double getAltersFaktor(int alter) {
        if (alter < 3) {
            return 0.8;
        } else if (alter <= 10) {
            return 1.0;
        } else { // alter > 10
            return 1.2;
        }
    }

    /**
     * Ermittelt den Korrekturfaktor basierend auf der aktuellen Temperatur.
     */
    private double getTemperaturFaktor(double temperatur) {
        if (temperatur < 15.0) {
            return 0.85;
        } else if (temperatur <= 25.0) {
            return 1.0;
        } else { // temperatur > 25.0
            return 1.15;
        }
    }

    /**
     * Ermittelt den **KORRIGIERTEN** Korrekturfaktor basierend auf der Bodenfeuchte.
     *
     * @param feuchte Bodenfeuchte in Prozent.
     * @return Korrekturfaktor.
     */
    private double getBodenfeuchteFaktor(double feuchte) {
        if (feuchte <= 20.0) {
            return 1.2; // Extrem trocken
        } else if (feuchte <= 35.0) {
            return 1.1; // Eher trocken
        } else if (feuchte <= 50.0) {
            return 1.0; // Neutral
        } else if (feuchte <= 70.0) {
            return 0.8; // Nass
        } else {
            return 0.6; // Sehr nass (> 70.0)
        }
    }
}