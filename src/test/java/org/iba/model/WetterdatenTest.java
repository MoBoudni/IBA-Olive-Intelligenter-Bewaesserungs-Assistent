package org.iba.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit Tests für die Wetterdaten-Modellklasse.
 * Fokus liegt auf der Validierung der Konstruktor-Parameter (Temperatur und Niederschlag).
 */
class WetterdatenTest {

    // Gültige Testdaten
    private static final double GÜLTIGE_TEMPERATUR = 25.5; // °C
    private static final double GÜLTIGER_NIEDERSCHLAG = 10.0; // mm

    /**
     * Testet die erfolgreiche Erstellung eines Wetterdaten-Objekts mit gültigen Parametern
     * und überprüft die Getter-Methoden.
     */
    @Test
    void testGültigeWetterdatenErstellung() {
        // ARRANGE / ACT
        Wetterdaten wetter = new Wetterdaten(GÜLTIGE_TEMPERATUR, GÜLTIGER_NIEDERSCHLAG);

        // ASSERT
        assertNotNull(wetter, "Das Wetterdaten-Objekt sollte nicht null sein.");
        assertEquals(GÜLTIGE_TEMPERATUR, wetter.getTemperatur(), "Die Temperatur wurde nicht korrekt gesetzt.");
        assertEquals(GÜLTIGER_NIEDERSCHLAG, wetter.getNiederschlag(), "Der Niederschlag wurde nicht korrekt gesetzt.");
    }

    // ========================================================================
    // TEST DER TEMPERATUR-VALIDIERUNG
    // ========================================================================

    /**
     * Testet ungültige Temperaturen außerhalb des definierten Bereichs (-50.0°C bis 60.0°C).
     */
    @ParameterizedTest(name = "Temperatur {0}°C sollte IllegalArgumentException werfen")
    @ValueSource(doubles = {-50.001, -100.0, 60.001, 100.0}) // Grenzfälle und Extremwerte
    void testUngültigeTemperaturWirftException(double ungültigeTemperatur) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Wetterdaten(ungültigeTemperatur, GÜLTIGER_NIEDERSCHLAG);
        }, "Temperatur außerhalb des Bereichs (-50°C bis 60°C) sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet die Grenzfälle der Temperatur, die als gültig angesehen werden (-50.0°C und 60.0°C).
     */
    @ParameterizedTest(name = "Temperatur {0}°C ist gültig")
    @ValueSource(doubles = {-50.0, 0.0, 60.0}) // Gültige Grenzwerte und Mittelwert
    void testGültigeTemperaturGrenzfälle(double gültigeTemperatur) {
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Wetterdaten wetter = new Wetterdaten(gültigeTemperatur, GÜLTIGER_NIEDERSCHLAG);
        assertEquals(gültigeTemperatur, wetter.getTemperatur());
    }

    // ========================================================================
    // TEST DER NIEDERSCHLAG-VALIDIERUNG
    // ========================================================================

    /**
     * Testet ungültigen Niederschlag (negative Werte).
     */
    @ParameterizedTest(name = "Niederschlag {0}mm sollte IllegalArgumentException werfen")
    @CsvSource({
            "-0.001", // Negativer Grenzfall
            "-10.5",  // Negative Zahl
            "-1000.0" // Extreme negative Zahl
    })
    void testNegativerNiederschlagWirftException(double negativerNiederschlag) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Wetterdaten(GÜLTIGE_TEMPERATUR, negativerNiederschlag);
        }, "Negativer Niederschlag sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet den Grenzfall, bei dem Niederschlag 0.0mm gültig ist.
     */
    @Test
    void testNiederschlagGleichNullIstGültig() {
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Wetterdaten wetter = new Wetterdaten(GÜLTIGE_TEMPERATUR, 0.0);
        assertEquals(0.0, wetter.getNiederschlag());
    }
}