package org.iba.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unittests für die Olivenbaum-Modellklasse.
 * Fokus liegt auf der Validierung der Konstruktor_Parameter.
 */
public class OlivenbaumTest {

    // Testdaten für die Validierung
    private static final String GÜLTIGER_NAME = "Parzelle A";
    private static final int GÜLTIGES_ALTER = 5;
    private static final double GÜLTIGER_BEDARF = 80.5;

    /**
     * Testet die erfolgreiche Erstellung eines Olivenbaum-Objekts
     * mit gültigen Parametern und überprüft die Getter-Methoden.
     */
    @Test
    void testGültigeOlivenbaumErstellung(){
        // ARRANGE / ACT
        Olivenbaum baum = new Olivenbaum(GÜLTIGER_NAME, GÜLTIGES_ALTER, GÜLTIGER_BEDARF);

        // ASSERT
        assertNotNull(baum, "Das Olivenbaum-Objekt sollte nicht null sein.");
        assertEquals(GÜLTIGER_NAME, baum.getName(), "Der Name wurde nicht korrekt gesetzt.");
        assertEquals(GÜLTIGES_ALTER, baum.getAlter(), "Das Alter wurde nicht korrekt gesetzt.");
        assertEquals(GÜLTIGER_BEDARF, baum.getBasisBedarf(), " Der Basisbedarf wurde nicht korrekt gesetzt");
    }

    // =======================================================================
    //                        TEST DER NAME-VALIDIERUNG
    // =======================================================================

    /**
     * Testet die Validierung des Namens auf ungültige Werte (null, leer, nur Leerzeichen).
     */
    @ParameterizedTest(name = "Name \"{0}\" sollte IllegalArgumentException werfen")
    @NullAndEmptySource // Testet null und ""
    @ValueSource(strings = {" ", "\t", "\n", " "}) // Testet Leerzeichen, Tabs, Zeilenumbrüche
    void testUngültigerNameWirftException(String ungültigerName) {

        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
        new Olivenbaum(ungültigerName, GÜLTIGES_ALTER, GÜLTIGER_BEDARF);
        }, "Ungültige Namen (null, leer, blank) sollten eine IllegalArgumentException auslösen.");
    }

    // ========================================================================
    //                        TEST DER ALTER-VALIDIERUNG
    // ========================================================================

    /**
     * Testet die Validierung des Alters auf negative Werte.
     * Alter muss 0 oder größer sein
     */
    @ParameterizedTest(name = "Alter {0} sollte IllegalArgumentException werfen")
    @ValueSource(ints = {-1, -100, -2147483648}) // Negative Grenzfälle
    void testNegativesAlterWirftException(int negativesAlter) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Olivenbaum(GÜLTIGER_NAME, negativesAlter, GÜLTIGER_BEDARF);
        }, "Negatives Alter sollte eine IllegalArgumentException auslösen.");
    }
    /**
     * Testet den Grenzfall, bei dem das Alter 0 (neu gepflanzt) gültig ist.
     */
    @Test
    void testAlterGleichNullIstGültig() {
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        new Olivenbaum(GÜLTIGER_NAME, 0, GÜLTIGER_BEDARF);
        //Expliziter Test, dass Alter 0 korrekt ausgelesen wird
        assertEquals(0, new Olivenbaum(GÜLTIGER_NAME, 0 , GÜLTIGER_BEDARF).getAlter());
    }

    // ========================================================================
    //                   TEST DER BASISBEDARF-VALIDIERUNG
    // ========================================================================

    /**
     * Testet die Validierung des Basisbedarfs auf 0.0 oder negative Werte.
     * Der Bedarf muss positiv (> 0.0) sein.
     */
    @ParameterizedTest(name = "Basisbedarf {0} sollte IllegalArgumentException werfen")
    @CsvSource({
            "0.0",   // Grenzfall 0
            "-0.001",// Negativer Wert (Grenzfall)
            "-50.5"  // Negativer Wert
    })
    void testNichtPositiverBedarfWirftException(double ungültigerBedarf) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Olivenbaum(GÜLTIGER_NAME, GÜLTIGES_ALTER, ungültigerBedarf);
        }, "Basisbedarf <= 0.0 sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet den Grenzfall, bei dem ein minimal positiver Basisbedarf gültig ist.
     */
    @Test
    void testMinimalPositiverBedarfIstGültig() {
        double minimalBedarf = 0.0001;
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Olivenbaum baum = new Olivenbaum(GÜLTIGER_NAME, GÜLTIGES_ALTER, minimalBedarf);
        assertEquals(minimalBedarf, baum.getBasisBedarf(), "Minimal positiver Bedarf ist gültig.");
    }
}
