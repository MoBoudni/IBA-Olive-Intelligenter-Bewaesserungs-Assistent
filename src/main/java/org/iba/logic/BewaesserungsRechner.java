package org.iba.logic;

import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;

/**
 * Implementierung des Berechnungsmodells zur Ermittlung des täglichen Wasserbedarfs.
 * <p>
 * Diese Klasse enthält die Geschäftslogik (Business Logic). Sie ist zustandslos (stateless)
 * und rein funktional implementiert.
 * </p>
 */
public class BewaesserungsRechner {

    /**
     * Berechnet den angepassten täglichen Wasserbedarf (MVP-Logik).
     * <p>
     * Formel: {@code Bedarf = max(0, B_basis * f_Alter * f_Temp - E_Niederschlag)}
     * </p>
     *
     * @param baum   Das Olivenbaum-Objekt mit Basisbedarf und Alter.
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
        return Math.max(0, berechneterBedarf);
    }

    /**
     * Berechnet den Wasserbedarf unter Berücksichtigung von Sensordaten (MVP+ Logik).
     * <p>
     * Dies ist eine überladene Methode (Method Overloading).
     * </p>
     *
     * @param baum         Das Olivenbaum-Objekt.
     * @param wetter       Die Wetterdaten.
     * @param bodenfeuchte Der gemessene Wert der Bodenfeuchte in Prozent (0.0 bis 100.0).
     * @return Der errechnete Wasserbedarf in Litern (0.0, wenn Boden feucht genug).
     */
    public double berechneWasserbedarf(Olivenbaum baum, Wetterdaten wetter, double bodenfeuchte) {
        // Logik-Erweiterung: Wenn der Boden sehr feucht ist (> 80%),
        // ist keine Bewässerung nötig, egal wie heiß es ist.
        if (bodenfeuchte > 80.0) {
            return 0.0;
        }

        // Ansonsten: Rückfall auf die Standard-Formel
        return berechneWasserbedarf(baum, wetter);
    }

    // --- Private Hilfsmethoden (Kapselung der Faktoren) ---

    /**
     * Ermittelt den Altersfaktor f_Alter.
     * Jung (<3): 0.8 | Reif (3-10): 1.0 | Alt (>10): 1.2
     */
    private double getAltersFaktor(int alter) {
        if (alter < 3) {
            return 0.8;
        } else if (alter <= 10) {
            return 1.0;
        } else {
            return 1.2;
        }
    }

    /**
     * Ermittelt den Temperaturfaktor f_Temp.
     * Kühl (<15°C): 0.85 | Normal (15-25°C): 1.0 | Heiß (>25°C): 1.15
     */
    private double getTemperaturFaktor(double temperatur) {
        if (temperatur < 15.0) {
            return 0.85;
        } else if (temperatur <= 25.0) {
            return 1.0;
        } else {
            return 1.15;
        }
    }

    /**
     * Berechnet den effektiven Niederschlag.
     * Nur 50% des Regens werden als effektiv für die Wurzeln angesehen.
     */
    private double getEffektiverNiederschlag(double niederschlag) {
        return niederschlag * 0.5;
    }
}