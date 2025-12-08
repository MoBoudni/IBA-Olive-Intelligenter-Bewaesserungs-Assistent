package org.iba.Unittest.model;

import org.iba.model.Baum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unittests für die Baum-Modellklasse.
 * Fokus liegt auf der Validierung der Konstruktor-Parameter.
 */
public class BaumTest {

    // Testdaten für die Validierung
    private static final int GUELTIGE_PARZELLE_ID = 1;
    private static final int GUELTIGES_ALTER = 5;
    private static final int GUELTIGE_PFLANZENART_ID = 1;
    private static final double GUELTIGER_BEDARF = 80.5;

    /**
     * Testet die erfolgreiche Erstellung eines Baum-Objekts
     * mit gültigen Parametern und überprüft die Getter-Methoden.
     */
    @Test
    void testGueltigeBaumErstellung() {
        // ARRANGE / ACT
        Baum baum = new Baum(GUELTIGE_PARZELLE_ID, GUELTIGES_ALTER, GUELTIGE_PFLANZENART_ID, GUELTIGER_BEDARF);

        // ASSERT
        assertNotNull(baum, "Das Baum-Objekt sollte nicht null sein.");
        assertEquals(GUELTIGE_PARZELLE_ID, baum.getParzelleId(), "Die Parzellen-ID wurde nicht korrekt gesetzt.");
        assertEquals(GUELTIGES_ALTER, baum.getAlterJahre(), "Das Alter wurde nicht korrekt gesetzt.");
        assertEquals(GUELTIGE_PFLANZENART_ID, baum.getPflanzenartId(), "Die Pflanzenart-ID wurde nicht korrekt gesetzt.");
        assertEquals(GUELTIGER_BEDARF, baum.getBasisBedarf(), "Der Basisbedarf wurde nicht korrekt gesetzt");
    }

    /**
     * Testet den Konstruktor mit allen Feldern (für DB-Loading).
     */
    @Test
    void testVollstaendigerKonstruktor() {
        // ARRANGE / ACT
        Baum baum = new Baum(1, GUELTIGE_PARZELLE_ID, GUELTIGES_ALTER, GUELTIGE_PFLANZENART_ID, GUELTIGER_BEDARF);

        // ASSERT
        assertNotNull(baum);
        assertEquals(1, baum.getBaumId(), "Die Baum-ID wurde nicht korrekt gesetzt.");
        assertEquals(GUELTIGE_PARZELLE_ID, baum.getParzelleId());
        assertEquals(GUELTIGES_ALTER, baum.getAlterJahre());
        assertEquals(GUELTIGE_PFLANZENART_ID, baum.getPflanzenartId());
        assertEquals(GUELTIGER_BEDARF, baum.getBasisBedarf());
    }

    /**
     * Testet den Standard-Konstruktor und Setter-Methoden.
     */
    @Test
    void testStandardKonstruktorUndSetter() {
        // ARRANGE
        Baum baum = new Baum();

        // ACT
        baum.setBaumId(1);
        baum.setParzelleId(GUELTIGE_PARZELLE_ID);
        baum.setAlterJahre(GUELTIGES_ALTER);
        baum.setPflanzenartId(GUELTIGE_PFLANZENART_ID);
        baum.setBasisBedarf(GUELTIGER_BEDARF);

        // ASSERT
        assertEquals(1, baum.getBaumId());
        assertEquals(GUELTIGE_PARZELLE_ID, baum.getParzelleId());
        assertEquals(GUELTIGES_ALTER, baum.getAlterJahre());
        assertEquals(GUELTIGE_PFLANZENART_ID, baum.getPflanzenartId());
        assertEquals(GUELTIGER_BEDARF, baum.getBasisBedarf());
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
            new Baum(GUELTIGE_PARZELLE_ID, negativesAlter, GUELTIGE_PFLANZENART_ID, GUELTIGER_BEDARF);
        }, "Negatives Alter sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet den Grenzfall, bei dem das Alter 0 (neu gepflanzt) gültig ist.
     */
    @Test
    void testAlterGleichNullIstGültig() {
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Baum baum = new Baum(GUELTIGE_PARZELLE_ID, 0, GUELTIGE_PFLANZENART_ID, GUELTIGER_BEDARF);
        assertEquals(0, baum.getAlterJahre(), "Alter 0 sollte erlaubt sein.");
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
    void testNichtPositiverBedarfWirftException(double ungueltigerBedarf) {
        // ASSERT: Erwartet IllegalArgumentException beim Erstellen
        assertThrows(IllegalArgumentException.class, () -> {
            new Baum(GUELTIGE_PARZELLE_ID, GUELTIGES_ALTER, GUELTIGE_PFLANZENART_ID, ungueltigerBedarf);
        }, "Basisbedarf <= 0.0 sollte eine IllegalArgumentException auslösen.");
    }

    /**
     * Testet den Grenzfall, bei dem ein minimal positiver Basisbedarf gültig ist.
     */
    @Test
    void testMinimalPositiverBedarfIstGültig() {
        double minimalBedarf = 0.0001;
        // ARRANGE / ACT / ASSERT: Sollte keine Exception werfen
        Baum baum = new Baum(GUELTIGE_PARZELLE_ID, GUELTIGES_ALTER, GUELTIGE_PFLANZENART_ID, minimalBedarf);
        assertEquals(minimalBedarf, baum.getBasisBedarf(), "Minimal positiver Bedarf ist gültig.");
    }

    // ========================================================================
    //                   TEST DER ANDEREN VALIDIERUNGEN
    // ========================================================================

    /**
     * Testet, dass negative Parzellen-ID keine Exception wirft 
     * (möglicherweise nicht validiert, da es ein Fremdschlüssel ist)
     */
    @Test
    void testNegativeParzellenIdIstErlaubt() {
        // Negative Parzellen-ID sollte erlaubt sein (wird durch FK-Constraint in DB geprüft)
        Baum baum = new Baum(-1, GUELTIGES_ALTER, GUELTIGE_PFLANZENART_ID, GUELTIGER_BEDARF);
        assertEquals(-1, baum.getParzelleId());
    }

    /**
     * Testet, dass negative Pflanzenart-ID keine Exception wirft
     * (möglicherweise nicht validiert, da es ein Fremdschlüssel ist)
     */
    @Test
    void testNegativePflanzenartIdIstErlaubt() {
        // Negative Pflanzenart-ID sollte erlaubt sein (wird durch FK-Constraint in DB geprüft)
        Baum baum = new Baum(GUELTIGE_PARZELLE_ID, GUELTIGES_ALTER, -1, GUELTIGER_BEDARF);
        assertEquals(-1, baum.getPflanzenartId());
    }

    /**
     * Testet die toString-Methode.
     */
    @Test
    void testToString() {
        // ARRANGE
        Baum baum = new Baum(1, 2, 5, 1, 100.5);

        // ACT
        String toStringResult = baum.toString();

        // ASSERT
        assertTrue(toStringResult.contains("Baum{"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("parzelleId=2"));
        assertTrue(toStringResult.contains("alterJahre=5"));
        assertTrue(toStringResult.contains("pflanzenartId=1"));
        assertTrue(toStringResult.contains("basisBedarf=100.5"));
        assertTrue(toStringResult.contains("l/Tag"));
    }
}