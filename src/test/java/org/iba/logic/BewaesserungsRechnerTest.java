package org.iba.logic;

import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit Tests für die Kernlogik-Klasse BewaesserungsRechner.
 * Testet die Faktoren, die Hauptformel und die Bodenfeuchte-Korrektur.
 * * HINWEIS: Es wird der JUnit 5 Standard für parametrisierte Tests verwendet.
 */
class BewaesserungsRechnerTest {

    // Genauigkeit für Fließkommazahlen-Vergleiche (z.B. 0.001 Liter)
    private static final double DELTA = 0.001;
    private final BewaesserungsRechner rechner = new BewaesserungsRechner();

    // ========================================================================
    // A. TEST DER HAUPTFORMEL (Fallback-Logik ohne Sensor)
    // Formel: max(0, basis * f_Alter * f_Temp - Niederschlag * 0.5)
    // ========================================================================

    /**
     * Testet die Hauptberechnung unter "normalen" Bedingungen (Faktoren = 1.0).
     * Alter: 5 (f_Alter=1.0), Temp: 20°C (f_Temp=1.0), Basis: 100L, Regen: 10mm (Abzug: 5L)
     * Erwartet: 100 * 1.0 * 1.0 - 5.0 = 95.0 L
     */
    @Test
    void testNormalConditions() {
        Olivenbaum baum = new Olivenbaum("Normal", 5, 100.0);
        Wetterdaten wetter = new Wetterdaten(20.0, 10.0);

        double expected = 95.0; // 100 * 1.0 * 1.0 - 10 * 0.5
        double actual = rechner.berechneWasserbedarf(baum, wetter);

        assertEquals(expected, actual, DELTA, "Der Bedarf bei Standardfaktoren sollte korrekt berechnet werden.");
    }

    /**
     * Testet den Grenzfall, bei dem der Bedarf negativ werden würde, aber 0.0 zurückgegeben werden muss.
     * Alter: 5 (f_Alter=1.0), Temp: 20°C (f_Temp=1.0), Basis: 50L, Regen: 100mm (Abzug: 50L)
     * Berechnung: 50 * 1.0 * 1.0 - 50.0 = 0.0 L (keine Bewässerung nötig)
     */
    @Test
    void testZeroOutputIfNegative() {
        Olivenbaum baum = new Olivenbaum("Viel Regen", 5, 50.0);
        Wetterdaten wetter = new Wetterdaten(20.0, 100.0);

        double expected = 0.0; // 50 * 1.0 * 1.0 - 100 * 0.5 = 0.0
        double actual = rechner.berechneWasserbedarf(baum, wetter);

        assertEquals(expected, actual, DELTA, "Der Bedarf darf nicht negativ sein und muss 0.0 zurückgeben.");
    }

    /**
     * Testet die Hauptberechnung mit maximalen Faktoren (max. Bedarf).
     * Alter: 11 (f_Alter=1.2), Temp: 30°C (f_Temp=1.15), Basis: 100L, Regen: 0mm
     * Erwartet: 100 * 1.2 * 1.15 = 138.0 L
     */
    @Test
    void testMaximumDemand() {
        Olivenbaum baum = new Olivenbaum("Maximal", 11, 100.0);
        Wetterdaten wetter = new Wetterdaten(30.0, 0.0);

        double expected = 138.0; // 100 * 1.2 * 1.15
        double actual = rechner.berechneWasserbedarf(baum, wetter);

        assertEquals(expected, actual, DELTA, "Der Bedarf bei maximalen Faktoren sollte korrekt berechnet werden.");
    }

    // ========================================================================
    // B. TEST DER ALTERSKORREKTUR (f_Alter)
    // Grenzfälle: 2, 3, 10, 11
    // ========================================================================

    /**
     * Testet die f_Alter Faktoren an allen Grenzwerten und in den Äquivalenzklassen.
     * Format: alter, erwarteter_faktor
     */
    @ParameterizedTest(name = "Alter {0} Jahre sollte Faktor {1} ergeben")
    @CsvSource({
            "2, 0.8",  // Klasse 1: < 3 (Grenzfall)
            "0, 0.8",  // Klasse 1: < 3 (min)
            "3, 1.0",  // Klasse 2: 3-10 (Grenzfall)
            "7, 1.0",  // Klasse 2: 3-10 (Äquivalenzklasse)
            "10, 1.0", // Klasse 2: 3-10 (Grenzfall)
            "11, 1.2", // Klasse 3: > 10 (Grenzfall)
            "20, 1.2"  // Klasse 3: > 10 (max)
    })
    void testFaktorAlter(int alter, double expectedFactor) {
        // ARRANGE: Wir verwenden die Basis-Berechnung, um nur den Faktor zu isolieren.
        // Basis: 100L, Temp: 20°C (f_Temp=1.0), Regen: 0mm (Abzug: 0L)
        Olivenbaum baum = new Olivenbaum("TestAlter", alter, 100.0);
        Wetterdaten wetter = new Wetterdaten(20.0, 0.0);

        // ACT
        double actual = rechner.berechneWasserbedarf(baum, wetter);

        // ASSERT: 100 * f_Alter * 1.0 - 0 = 100 * f_Alter
        double expectedDemand = 100.0 * expectedFactor;

        assertEquals(expectedDemand, actual, DELTA,
                String.format("Alter %d sollte mit Faktor %.1f multipliziert werden.", alter, expectedFactor));
    }

    // ========================================================================
    // C. TEST DER TEMPERATURKORREKTUR (f_Temp)
    // Grenzfälle: 14.9, 15.0, 25.0, 25.1
    // ========================================================================

    /**
     * Testet die f_Temp Faktoren an allen Grenzwerten und in den Äquivalenzklassen.
     * Format: temperatur, erwarteter_faktor
     */
    @ParameterizedTest(name = "Temp {0}°C sollte Faktor {1} ergeben")
    @CsvSource({
            "14.9, 0.85", // Klasse 1: < 15°C (Grenzfall)
            "10.0, 0.85", // Klasse 1: < 15°C (Äquivalenzklasse)
            "15.0, 1.0",  // Klasse 2: 15-25°C (Grenzfall)
            "20.0, 1.0",  // Klasse 2: 15-25°C (Äquivalenzklasse)
            "25.0, 1.0",  // Klasse 2: 15-25°C (Grenzfall)
            "25.1, 1.15", // Klasse 3: > 25°C (Grenzfall)
            "30.0, 1.15"  // Klasse 3: > 25°C (Äquivalenzklasse)
    })
    void testFaktorTemperatur(double temp, double expectedFactor) {
        // ARRANGE: Basis: 100L, Alter: 5 (f_Alter=1.0), Regen: 0mm (Abzug: 0L)
        Olivenbaum baum = new Olivenbaum("TestTemp", 5, 100.0);
        Wetterdaten wetter = new Wetterdaten(temp, 0.0);

        // ACT
        double actual = rechner.berechneWasserbedarf(baum, wetter);

        // ASSERT: 100 * 1.0 * f_Temp - 0 = 100 * f_Temp
        double expectedDemand = 100.0 * expectedFactor;

        assertEquals(expectedDemand, actual, DELTA,
                String.format("Temperatur %.1f sollte mit Faktor %.2f multipliziert werden.", temp, expectedFactor));
    }

    // ========================================================================
    // D. TEST DER BODENFEUCHTE-KORREKTUR (erweiterte Methode)
    // ========================================================================

    /**
     * Testet die Bodenfeuchte-Korrektur an allen Grenzwerten.
     * Basis-Bedarf ohne Feuchtekontrolle = 100.0 L (100 * 1.0 * 1.0 - 0)
     * Format: feuchte_prozent, erwarteter_faktor, erwarteter_bedarf
     */
    @ParameterizedTest(name = "Feuchte {0}% sollte Faktor {1} und Bedarf {2} ergeben")
    @CsvSource({
            "19.9, 1.2, 120.0", // Extrem trocken (Grenzfall)
            "20.0, 1.2, 120.0", // Extrem trocken (Grenzfall)
            "34.9, 1.1, 110.0", // Eher trocken (Grenzfall)
            "35.0, 1.1, 110.0", // Eher trocken (Grenzfall)
            "40.0, 1.0, 100.0", // Neutral (Äquivalenzklasse)
            "50.0, 1.0, 100.0", // Neutral (Grenzfall)
            "50.1, 0.8, 80.0",  // Nass (Grenzfall)
            "70.0, 0.8, 80.0",  // Nass (Grenzfall)
            "70.1, 0.6, 60.0",  // Sehr nass (Grenzfall)
            "80.0, 0.6, 60.0",  // Sehr nass (max)
    })
    void testFeuchteKorrektur(double bodenfeuchte, double expectedFactor, double expectedDemand) {
        // ARRANGE: Basis-Bedarf von 100 L vor Feuchtekontrolle
        Olivenbaum baum = new Olivenbaum("TestFeuchte", 5, 100.0);
        Wetterdaten wetter = new Wetterdaten(20.0, 0.0);

        // ACT
        double actual = rechner.berechneWasserbedarf(baum, wetter, bodenfeuchte);

        // ASSERT
        assertEquals(expectedDemand, actual, DELTA,
                String.format("Feuchte %.1f sollte Bedarf auf %.2f korrigieren.", bodenfeuchte, expectedDemand));
    }

    /**
     * Testet, dass die Feuchtekontrolle nicht greift, wenn der Basis-Bedarf bereits 0 ist.
     * Simuliert: Sehr viel Regen, sodass der Bedarf 0 ist, aber der Sensor meldet extrem trocken (10%).
     * Das System soll nicht bewässern, da der Regen noch berücksichtigt wird.
     */
    @Test
    void testFeuchteDoesNotOverruleZeroDemand() {
        // ARRANGE: Basis: 50L, Regen: 100mm (Abzug: 50L) -> Basis-Bedarf = 0.0
        Olivenbaum baum = new Olivenbaum("ZeroDemand", 5, 50.0);
        Wetterdaten wetter = new Wetterdaten(20.0, 100.0);

        // ACT: Sensor meldet extrem trocken (10.0%), was den Bedarf um 1.2 erhöhen würde,
        // wenn der Basis-Bedarf > 0 wäre.
        double bodenfeuchte = 10.0;
        double actual = rechner.berechneWasserbedarf(baum, wetter, bodenfeuchte);

        // ASSERT: Erwartet 0.0, da die Reduktion durch Regen Priorität hat.
        assertEquals(0.0, actual, DELTA,
                "Die Bodenfeuchtekontrolle darf keinen Bedarf erzeugen, wenn die Basisberechnung 0 ergibt.");
    }
}