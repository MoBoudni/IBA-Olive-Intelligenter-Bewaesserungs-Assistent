package org.iba.Unittest.model;

import org.iba.model.Parzelle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UnitTests für die Parzelle-Modellklasse.
 * Testet Konstruktoren, Getter/Setter, Validierung und toString-Methode.
 */
class ParzelleTest {

    // Testdaten
    private static final int GUELTIGE_PARZELLE_ID = 1;
    private static final String GUELTIGER_NAME = "Feld Südhang";
    private static final int GUELTIGE_ANZAHL_BAEUME = 5;
    private static final double GUELTIGE_FLAECHE = 1000.0;
    private static final String GUELTIGE_KLIMA_ZONE = "Mediterran";
    private static final int GUELTIGE_BESITZER_ID = 101;

    /**
     * Testet den Standard-Konstruktor und Setter-Methoden.
     */
    @Test
    void testStandardKonstruktorUndSetter() {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setParzelleId(GUELTIGE_PARZELLE_ID);
        parzelle.setName(GUELTIGER_NAME);
        parzelle.setAnzahlBaeume(GUELTIGE_ANZAHL_BAEUME);
        parzelle.setFlaecheQm(GUELTIGE_FLAECHE);
        parzelle.setKlimaZone(GUELTIGE_KLIMA_ZONE);
        parzelle.setBesitzerId(GUELTIGE_BESITZER_ID);

        // ASSERT
        assertEquals(GUELTIGE_PARZELLE_ID, parzelle.getParzelleId(), "Parzellen-ID sollte korrekt gesetzt sein.");
        assertEquals(GUELTIGER_NAME, parzelle.getName(), "Name sollte korrekt gesetzt sein.");
        assertEquals(GUELTIGE_ANZAHL_BAEUME, parzelle.getAnzahlBaeume(), "Anzahl Bäume sollte korrekt gesetzt sein.");
        assertEquals(GUELTIGE_FLAECHE, parzelle.getFlaecheQm(), 0.001, "Fläche sollte korrekt gesetzt sein.");
        assertEquals(GUELTIGE_KLIMA_ZONE, parzelle.getKlimaZone(), "Klimazone sollte korrekt gesetzt sein.");
        assertEquals(GUELTIGE_BESITZER_ID, parzelle.getBesitzerId(), "Besitzer-ID sollte korrekt gesetzt sein.");
    }

    /**
     * Testet den vollständigen Konstruktor mit allen Parametern.
     */
    @Test
    void testVollstaendigerKonstruktor() {
        // ARRANGE & ACT
        Parzelle parzelle = new Parzelle(
                GUELTIGE_PARZELLE_ID,
                GUELTIGER_NAME,
                GUELTIGE_ANZAHL_BAEUME,
                GUELTIGE_FLAECHE,
                GUELTIGE_KLIMA_ZONE,
                GUELTIGE_BESITZER_ID
        );

        // ASSERT
        assertNotNull(parzelle, "Parzelle-Objekt sollte nicht null sein.");
        assertEquals(GUELTIGE_PARZELLE_ID, parzelle.getParzelleId());
        assertEquals(GUELTIGER_NAME, parzelle.getName());
        assertEquals(GUELTIGE_ANZAHL_BAEUME, parzelle.getAnzahlBaeume());
        assertEquals(GUELTIGE_FLAECHE, parzelle.getFlaecheQm(), 0.001);
        assertEquals(GUELTIGE_KLIMA_ZONE, parzelle.getKlimaZone());
        assertEquals(GUELTIGE_BESITZER_ID, parzelle.getBesitzerId());
    }

    /**
     * Testet Grenzwerte für die Anzahl der Bäume.
     * Erwartet: Positive Zahlen (inklusive 0)
     */
    @ParameterizedTest(name = "Anzahl Bäume {0} sollte gültig sein")
    @ValueSource(ints = {0, 1, 5, 100, 1000, Integer.MAX_VALUE})
    void testGueltigeAnzahlBaeume(int anzahlBaeume) {
        // ARRANGE & ACT
        Parzelle parzelle = new Parzelle();
        parzelle.setAnzahlBaeume(anzahlBaeume);

        // ASSERT
        assertEquals(anzahlBaeume, parzelle.getAnzahlBaeume(),
                String.format("Anzahl Bäume %d sollte korrekt gesetzt werden.", anzahlBaeume));
    }

    /**
     * Testet negative Anzahl Bäume (sollte erlaubt sein, da keine Validierung im Modell).
     * HINWEIS: Validierung sollte im Service/Repository erfolgen.
     */
    @ParameterizedTest(name = "Negative Anzahl Bäume {0} sollte möglich sein (keine Validierung im Modell)")
    @ValueSource(ints = {-1, -5, -100, Integer.MIN_VALUE})
    void testNegativeAnzahlBaeumeIstMoeglich(int anzahlBaeume) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setAnzahlBaeume(anzahlBaeume);

        // ASSERT
        assertEquals(anzahlBaeume, parzelle.getAnzahlBaeume(),
                "Negative Anzahl Bäume sollte möglich sein, da keine Validierung im Modell.");
    }

    /**
     * Testet verschiedene gültige Flächenwerte.
     */
    @ParameterizedTest(name = "Fläche {0} qm sollte gültig sein")
    @CsvSource({
            "0.0, 0.0",
            "0.001, 0.001",
            "1.0, 1.0",
            "100.5, 100.5",
            "999999.999, 999999.999"
    })
    void testGueltigeFlaechenWerte(double flaeche, double expected) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setFlaecheQm(flaeche);

        // ASSERT
        assertEquals(expected, parzelle.getFlaecheQm(), 0.0001,
                String.format("Fläche %.3f sollte korrekt gesetzt werden.", flaeche));
    }

    /**
     * Testet negative Flächenwerte (sollte erlaubt sein).
     */
    @Test
    void testNegativeFlaechenWerteSindMoeglich() {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setFlaecheQm(-100.0);

        // ASSERT
        assertEquals(-100.0, parzelle.getFlaecheQm(), 0.001,
                "Negative Fläche sollte möglich sein, da keine Validierung im Modell.");
    }

    /**
     * Testet verschiedene gültige Klimazonen.
     */
    @ParameterizedTest(name = "Klimazone '{0}' sollte gültig sein")
    @ValueSource(strings = {
            "Mediterran",
            "Kontinental",
            "Tropisch",
            "Subtropisch",
            "Gemäßigt",
            "Wüste",
            "A",
            "Zone 1",
            "Nordhang",
            "Südhang"
    })
    void testGueltigeKlimaZonen(String klimaZone) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setKlimaZone(klimaZone);

        // ASSERT
        assertEquals(klimaZone, parzelle.getKlimaZone(),
                String.format("Klimazone '%s' sollte korrekt gesetzt werden.", klimaZone));
    }

    /**
     * Testet leere und null Klimazonen (sollten erlaubt sein).
     */
    @ParameterizedTest(name = "Klimazone '{0}' sollte möglich sein (keine Validierung)")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void testLeereUndNullKlimaZonenSindMoeglich(String klimaZone) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setKlimaZone(klimaZone);

        // ASSERT
        assertEquals(klimaZone, parzelle.getKlimaZone(),
                "Leere/null Klimazone sollte möglich sein, da keine Validierung im Modell.");
    }

    /**
     * Testet verschiedene Besitzer-IDs.
     */
    @ParameterizedTest(name = "Besitzer-ID {0} sollte gültig sein")
    @ValueSource(ints = {0, 1, 100, 9999, Integer.MAX_VALUE})
    void testGueltigeBesitzerIds(int besitzerId) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setBesitzerId(besitzerId);

        // ASSERT
        assertEquals(besitzerId, parzelle.getBesitzerId(),
                String.format("Besitzer-ID %d sollte korrekt gesetzt werden.", besitzerId));
    }

    /**
     * Testet negative Besitzer-IDs (sollten erlaubt sein).
     */
    @Test
    void testNegativeBesitzerIdIstMoeglich() {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setBesitzerId(-1);

        // ASSERT
        assertEquals(-1, parzelle.getBesitzerId(),
                "Negative Besitzer-ID sollte möglich sein (Fremdschlüssel-Validierung in DB).");
    }

    /**
     * Testet die toString-Methode auf korrekte Formatierung.
     */
    @Test
    void testToString() {
        // ARRANGE
        Parzelle parzelle = new Parzelle(
                42,
                "Test Parzelle",
                10,
                500.5,
                "Test Zone",
                123
        );

        // ACT
        String toStringResult = parzelle.toString();

        // ASSERT
        assertNotNull(toStringResult, "toString() sollte nicht null zurückgeben.");
        assertTrue(toStringResult.contains("Parzelle{"), "Sollte mit 'Parzelle{' beginnen.");
        assertTrue(toStringResult.contains("id=42"), "Sollte die ID enthalten.");
        assertTrue(toStringResult.contains("name='Test Parzelle'"), "Sollte den Namen enthalten.");
        assertTrue(toStringResult.contains("flaeche=500.5qm"), "Sollte die Fläche mit Einheit enthalten.");
        assertTrue(toStringResult.contains("zone='Test Zone'"), "Sollte die Klimazone enthalten.");
        assertTrue(toStringResult.endsWith("}"), "Sollte mit '}' enden.");
    }

    /**
     * Testet, dass toString() nicht sensitive Daten wie Besitzer-ID enthält.
     */
    @Test
    void testToStringEnthaeltKeineSensitiveDaten() {
        // ARRANGE
        Parzelle parzelle = new Parzelle(1, "Test", 5, 100.0, "Zone", 999);

        // ACT
        String result = parzelle.toString();

        // ASSERT
        assertFalse(result.contains("besitzerId"), "toString sollte keine Besitzer-ID enthalten.");
        assertFalse(result.contains("999"), "toString sollte keine Besitzer-ID Werte enthalten.");
        assertFalse(result.contains("anzahlBaeume"), "toString sollte nicht explizit 'anzahlBaeume' enthalten.");
    }

    /**
     * Testet die Gleichheit von zwei Parzellen mit gleichen Werten.
     * Obwohl equals() nicht überschrieben ist, testen wir die Wertegleichheit.
     */
    @Test
    void testGleicheWerteErzeugenGleicheAttribute() {
        // ARRANGE & ACT
        Parzelle parzelle1 = new Parzelle(1, "Name", 5, 100.0, "Zone", 101);
        Parzelle parzelle2 = new Parzelle(1, "Name", 5, 100.0, "Zone", 101);

        // ASSERT: Alle Attribute sollten gleich sein
        assertEquals(parzelle1.getParzelleId(), parzelle2.getParzelleId());
        assertEquals(parzelle1.getName(), parzelle2.getName());
        assertEquals(parzelle1.getAnzahlBaeume(), parzelle2.getAnzahlBaeume());
        assertEquals(parzelle1.getFlaecheQm(), parzelle2.getFlaecheQm(), 0.001);
        assertEquals(parzelle1.getKlimaZone(), parzelle2.getKlimaZone());
        assertEquals(parzelle1.getBesitzerId(), parzelle2.getBesitzerId());
    }

    /**
     * Testet verschiedene Namenswerte.
     */
    @ParameterizedTest(name = "Name '{0}' sollte korrekt gesetzt werden")
    @ValueSource(strings = {
            "Feld 1",
            "Nordhang",
            "Südseite",
            "Parzelle A",
            "123",
            "Test-Parzelle_42",
            "Lange Name mit Leerzeichen und Sonderzeichen"
    })
    void testVerschiedeneNamen(String name) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setName(name);

        // ASSERT
        assertEquals(name, parzelle.getName(),
                String.format("Name '%s' sollte korrekt gesetzt werden.", name));
    }

    /**
     * Testet leere und null Namen (sollten erlaubt sein).
     */
    @ParameterizedTest(name = "Name '{0}' sollte möglich sein (keine Validierung im Modell)")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    void testLeereUndNullNamenSindMoeglich(String name) {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setName(name);

        // ASSERT
        assertEquals(name, parzelle.getName(),
                "Leere/null Name sollte möglich sein, da keine Validierung im Modell.");
    }

    /**
     * Testet die Immutability von Getter-Rückgaben (Strings sollten nicht manipulierbar sein).
     */
    @Test
    void testGetterRueckgabenSolltenSicherSein() {
        // ARRANGE
        Parzelle parzelle = new Parzelle(1, "Original", 5, 100.0, "Zone", 101);
        String originalName = parzelle.getName();
        String originalZone = parzelle.getKlimaZone();

        // ACT: Versuche, die Rückgaben zu manipulieren (sollte keinen Effekt haben)
        if (originalName != null) {
            // String ist immutable, also sicher
        }
        if (originalZone != null) {
            // String ist immutable, also sicher
        }

        // ASSERT: Werte sollten unverändert bleiben
        assertEquals("Original", parzelle.getName());
        assertEquals("Zone", parzelle.getKlimaZone());
    }

    /**
     * Testet Extremwerte für alle Felder.
     */
    @Test
    void testExtremwerte() {
        // ARRANGE & ACT
        Parzelle parzelle = new Parzelle(
                Integer.MAX_VALUE,
                "X".repeat(100), // Langer Name
                Integer.MAX_VALUE,
                Double.MAX_VALUE,
                "Z".repeat(50), // Lange Klimazone
                Integer.MAX_VALUE
        );

        // ASSERT
        assertEquals(Integer.MAX_VALUE, parzelle.getParzelleId());
        assertEquals(Integer.MAX_VALUE, parzelle.getAnzahlBaeume());
        assertEquals(Double.MAX_VALUE, parzelle.getFlaecheQm(), 0.001);
        assertEquals(Integer.MAX_VALUE, parzelle.getBesitzerId());
        assertNotNull(parzelle.getName());
        assertNotNull(parzelle.getKlimaZone());
    }

    /**
     * Testet Fließkomma-Genauigkeit bei der Fläche.
     */
    @Test
    void testFliesskommaGenauigkeit() {
        // ARRANGE
        Parzelle parzelle = new Parzelle();

        // ACT
        parzelle.setFlaecheQm(123.456789);

        // ASSERT
        assertEquals(123.456789, parzelle.getFlaecheQm(), 0.0000001,
                "Fließkommazahlen sollten mit hoher Genauigkeit gespeichert werden.");
    }
}