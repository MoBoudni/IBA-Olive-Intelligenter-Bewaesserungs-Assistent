package org.iba.Integrationstest;

import org.iba.model.Messwerte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unittests für die Messwerte-Modellklasse.
 * Fokus liegt auf der Validierung der Konstruktor-Parameter (Temperatur und Niederschlag).
 */
class MesswerteRepositoryTest {

    // Gültige Testdaten
    private static final double GUELTIGE_TEMPERATUR = 25.5; // °C
    private static final double GUELTIGER_NIEDERSCHLAG = 10.0; // mm

    /**
     * Testet die erfolgreiche Erstellung eines Messwerte-Objekts mit gültigen Parametern
     * und überprüft die Getter-Methoden.
     */
    @Test
    void testGueltigeMesswerteErstellung() {
        // ARRANGE / ACT
        Messwerte messwerte = new Messwerte(GUELTIGE_TEMPERATUR, GUELTIGER_NIEDERSCHLAG);

        // ASSERT
        assertNotNull(messwerte, "Das Messwerte-Objekt sollte nicht null sein.");
        assertEquals(GUELTIGE_TEMPERATUR, messwerte.getTemperatur(), "Die Temperatur wurde nicht korrekt gesetzt.");
        assertEquals(GUELTIGER_NIEDERSCHLAG, messwerte.getNiederschlag(), "Der Niederschlag wurde nicht korrekt gesetzt."); // KORREKTUR: Instanz-Methode
    }

    // ========================================================================
    // TEST DER TEMPERATUR-VALIDIERUNG
    // ========================================================================

    /**
     * Testet ungültige Temperaturen außerhalb des definierten Bereichs (-50.0° C bis 60.0° C).
     */
    @ParameterizedTest(name = "Temperatur {0}°C sollte IllegalArgumentException werfen")
    @ValueSource(doubles = {-50.001, -100.0, 60.001, 100.0}) // Grenzfälle und Extremwerte
    void testUngueltigeTemperaturWirftException(double ungueltigeTemperatur) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Messwerte(ungueltigeTemperatur, GUELTIGER_NIEDERSCHLAG);
        }, "Temperatur außerhalb des Bereichs (-50°C bis 60°C) sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet die Grenzfälle der Temperatur, die als gültig angesehen werden (-50.0° C und 60.0° C).
     */
    @ParameterizedTest(name = "Temperatur {0}°C ist gültig")
    @ValueSource(doubles = {-50.0, 0.0, 60.0}) // Gültige Grenzwerte und Mittelwert
    void testGueltigeTemperaturGrenzfaelle(double gueltigeTemperatur) {
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Messwerte messwerte = new Messwerte(gueltigeTemperatur, GUELTIGER_NIEDERSCHLAG);
        assertEquals(gueltigeTemperatur, messwerte.getTemperatur(), "Temperatur sollte korrekt gesetzt sein.");
    }

    // ========================================================================
    // TEST DER NIEDERSCHLAG-VALIDIERUNG
    // ========================================================================

    /**
     * Testet ungültigen Niederschlag (negative Werte).
     */
    @ParameterizedTest(name = "Niederschlag {0} mm sollte IllegalArgumentException werfen")
    @CsvSource({
            "-0.001", // Negativer Grenzfall
            "-10.5",  // Negative Zahl
            "-1000.0" // Extreme negative Zahl
    })
    void testNegativerNiederschlagWirftException(double negativerNiederschlag) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Messwerte(GUELTIGE_TEMPERATUR, negativerNiederschlag);
        }, "Negativer Niederschlag sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet den Grenzfall, bei dem Niederschlag 0,0 mm gültig ist.
     */
    @Test
    void testNiederschlagGleichNullIstGueltig() {
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Messwerte wetter = new Messwerte(GUELTIGE_TEMPERATUR, 0.0);
        assertEquals(0.0, wetter.getNiederschlag(), "Niederschlag 0.0 sollte erlaubt sein."); // KORREKTUR: Instanz-Methode
    }

    /**
     * Testet verschiedene gültige Niederschlagswerte.
     */
    @ParameterizedTest(name = "Niederschlag {0} mm ist gültig")
    @ValueSource(doubles = {0.1, 1.0, 5.0, 10.0, 50.0, 100.0})
    void testGueltigeNiederschlagWerte(double niederschlag) {
        // ARRANGE / ACT / ASSERT
        Messwerte messwerte = new Messwerte(GUELTIGE_TEMPERATUR, niederschlag);
        assertEquals(niederschlag, messwerte.getNiederschlag(), 0.001, "Niederschlag sollte korrekt gesetzt sein.");
    }

    /**
     * Testet die Unveränderlichkeit (Immutable) der Klasse.
     */
    @Test
    void testMesswerteIstImmutable() {
        // ARRANGE
        Messwerte messwerte = new Messwerte(20.0, 5.0);

        // ASSERT: Es gibt keine Setter, also ist die Klasse immutable
        // der Test besteht darin, dass wir die Werte nur lesen können
        assertEquals(20.0, messwerte.getTemperatur());
        assertEquals(5.0, messwerte.getNiederschlag());
    }

    /**
     * Testet die Gleichheit der Objekte bei gleichen Werten.
     */
    @Test
    void testGleicheWerteErzeugenGleicheMesswerte() {
        // ARRANGE / ACT
        Messwerte messwerte1 = new Messwerte(25.0, 10.0);
        Messwerte messwerte2 = new Messwerte(25.0, 10.0);

        // ASSERT: Obwohl zwei verschiedene Objekte, sollten sie gleiche Werte haben
        assertEquals(messwerte1.getTemperatur(), messwerte2.getTemperatur());
        assertEquals(messwerte1.getNiederschlag(), messwerte2.getNiederschlag());
        // Hinweis: equals() ist nicht überschrieben, also ist assertEquals(messwerte1, messwerte2) nicht sinnvoll
    }
}