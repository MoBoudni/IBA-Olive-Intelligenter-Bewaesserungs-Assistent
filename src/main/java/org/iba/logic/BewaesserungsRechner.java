package org.iba.logic;

import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;

/**
 * Implementierung des Berechnungsmodells zur Ermittlung des täglichen Wasserbedarfs.
 */
public class BewaesserungsRechner {

    /**
     * Berechnet den angepassten täglichen Wasserbedarf für den Baum.
     *
     * Formel: Bedarf = max(0, B_basis * f_Alter * f_Temp - E_Niederschlag)
     * @param baum Das Olivenbaum-Objekt mit Basisbedarf und Alter.
     * @param wetter Die Wetterdaten (Temperatur, Niederschlag).
     * @return Der errechnete Wasserbedarf in Litern.
     */
    public double berechneWasserbedarf(Olivenbaum baum, Wetterdaten wetter) {

        // 1. Faktoren ermitteln
        double fAlter = getAltersFaktor(baum.getAlter());
        double fTemp = getTemperaturFaktor(wetter.getTemperatur());
        double eNiederschlag = getEffektiverNiederschlag(wetter.getNiederschlag());

        // 2. Berechnung nach Formel
        // B_basis * f_Alter * f_Temp - E_Niederschlag
        double berechneterBedarf = baum.getBasisBedarf() * fAlter * fTemp - eNiederschlag;

        // 3. Sicherstellen, dass der Bedarf nicht negativ ist (max(0, ...))
        // Da die Bewässerungsmenge 0 oder positiv sein muss.
        return Math.max(0, berechneterBedarf);
    }

    /**
     * Ermittelt den Altersfaktor f_Alter.
     * Jung: 0,8 | Reif: 1,0 | Alt: 1,2
     */
    private double getAltersFaktor(int alter) {
        if (alter < 3) {
            return 0.8; // Jung
        } else if (alter <= 10) {
            return 1.0; // Reif
        } else {
            return 1.2; // Alt
        }
    }

    /**
     * Ermittelt den Temperaturfaktor f_Temp.
     * Kühl <15°C: 0,85 | Normal: 1,0 | Heiß >25°C: 1,15
     */
    private double getTemperaturFaktor(double temperatur) {
        if (temperatur < 15.0) {
            return 0.85; // Kühl
        } else if (temperatur <= 25.0) {
            return 1.0; // Normal
        } else {
            return 1.15; // Heiß
        }
    }

    /**
     * Berechnet den effektiven Niederschlag (50% der gemessenen Menge).
     * E_Niederschlag = Niederschlag [mm] * 0,5
     */
    private double getEffektiverNiederschlag(double niederschlag) {
        return niederschlag * 0.5;
    }
}
