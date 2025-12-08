package org.iba.Integrationstest;

import org.iba.db.BaumRepository;
import org.iba.db.ParzelleRepository;
import org.iba.model.Baum;
import org.iba.model.Parzelle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest für das BaumRepository mit vereinfachter Testumgebung.
 */
class BaumRepositoryTest {

    private BaumRepository baumRepository;
    private ParzelleRepository parzelleRepository;

    // Test-Konstanten
    private static final int TEST_PARZELLE_ID = 9999;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    @BeforeEach
    void setUp() {
        baumRepository = new BaumRepository();
        parzelleRepository = new ParzelleRepository();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Zuerst sicherstellen, dass keine Test-Parzelle existiert
            stmt.execute("DELETE FROM baum WHERE parzelle_id = " + TEST_PARZELLE_ID);
            stmt.execute("DELETE FROM parzelle WHERE parzelle_id = " + TEST_PARZELLE_ID + " OR name = 'TestParzelle'");

            // Neue Test-Parzelle erstellen (mit korrekten Feldern!)
            Parzelle testParzelle = new Parzelle(
                    0,  // ID wird von der DB generiert
                    "TestParzelle",
                    0,  // anzahl_baeume
                    100.0,  // flaeche_qm
                    "TestZone",  // klima_zone
                    101  // besitzer_id
            );

            // Parzelle speichern (Repository generiert die ID)
            Parzelle gespeicherteParzelle = parzelleRepository.speichere(testParzelle);
            assertNotNull(gespeicherteParzelle, "Test-Parzelle sollte erfolgreich gespeichert werden");

            // Bestehende Bäume entfernen
            baumRepository.loescheAlleVonParzelle(TEST_PARZELLE_ID);

        } catch (SQLException e) {
            fail("Fehler bei der Einrichtung: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Bäume löschen
            stmt.execute("DELETE FROM baum WHERE parzelle_id = " + TEST_PARZELLE_ID);

            // Parzelle löschen
            stmt.execute("DELETE FROM parzelle WHERE parzelle_id = " + TEST_PARZELLE_ID);

        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Testdaten nicht aufräumen: " + e.getMessage());
        }
    }

    @Test
    void speichereUndFindeAlleVonParzelleSollteErfolgreichSein() {
        // Erst: Prüfen ob Parzelle existiert
        List<Parzelle> parzellen = parzelleRepository.findAlle();
        Parzelle testParzelle = parzellen.stream()
                .filter(p -> p.getName().equals("TestParzelle"))
                .findFirst()
                .orElse(null);

        assertNotNull(testParzelle, "Test-Parzelle sollte existieren");
        int parzelleId = testParzelle.getParzelleId();

        final double erwarteterBedarf = 12.55;

        // Erstellen des Baum-Objekts
        Baum neuerBaum = new Baum(parzelleId, 5, 1, erwarteterBedarf);

        // Speichern
        Baum gespeicherterBaum = baumRepository.speichere(neuerBaum);
        assertNotNull(gespeicherterBaum, "Der gespeicherte Baum sollte nicht null sein.");
        int id = gespeicherterBaum.getBaumId();
        assertTrue(id > 0, "Die generierte ID sollte größer als 0 sein.");

        // Abrufen mit der neuen Methode
        List<Baum> baeume = baumRepository.findByParzelleId(parzelleId);

        assertEquals(1, baeume.size(), "Genau ein Baum sollte gefunden werden.");

        Baum gefundenerBaum = baeume.get(0);
        assertEquals(id, gefundenerBaum.getBaumId());
        assertEquals(5, gefundenerBaum.getAlterJahre());
        assertEquals(1, gefundenerBaum.getPflanzenartId());
        assertEquals(erwarteterBedarf, gefundenerBaum.getBasisBedarf(), 0.001);
    }

    @Test
    void aktualisiereSollteErfolgreichSein() {
        // Erst: Parzelle finden
        List<Parzelle> parzellen = parzelleRepository.findAlle();
        Parzelle testParzelle = parzellen.stream()
                .filter(p -> p.getName().equals("TestParzelle"))
                .findFirst()
                .orElse(null);

        assertNotNull(testParzelle, "Test-Parzelle sollte existieren");
        int parzelleId = testParzelle.getParzelleId();

        final double initialerBedarf = 5.0;
        final double neuerBedarf = 7.77;

        // 1. Initial speichern
        Baum baum = new Baum(parzelleId, 10, 1, initialerBedarf);
        Baum initialerBaum = baumRepository.speichere(baum);
        assertNotNull(initialerBaum, "Baum sollte erfolgreich gespeichert werden");
        int id = initialerBaum.getBaumId();

        // 2. Objekt ändern
        initialerBaum.setAlterJahre(15);
        initialerBaum.setBasisBedarf(neuerBedarf);
        initialerBaum.setPflanzenartId(2);

        // 3. Aktualisieren und prüfen
        assertTrue(baumRepository.aktualisiere(initialerBaum), "Die Aktualisierung sollte erfolgreich sein.");

        // 4. Abrufen und Validieren
        Baum aktualisierterBaum = baumRepository.findById(id);

        assertNotNull(aktualisierterBaum, "Aktualisierter Baum sollte existieren.");
        assertEquals(15, aktualisierterBaum.getAlterJahre());
        assertEquals(2, aktualisierterBaum.getPflanzenartId());
        assertEquals(neuerBedarf, aktualisierterBaum.getBasisBedarf(), 0.001);
    }

    @Test
    void loescheSollteErfolgreichSein() {
        // Erst: Parzelle finden
        List<Parzelle> parzellen = parzelleRepository.findAlle();
        Parzelle testParzelle = parzellen.stream()
                .filter(p -> p.getName().equals("TestParzelle"))
                .findFirst()
                .orElse(null);

        assertNotNull(testParzelle, "Test-Parzelle sollte existieren");
        int parzelleId = testParzelle.getParzelleId();

        // 1. Initial speichern
        Baum baum = new Baum(parzelleId, 1, 1, 1.0);
        Baum initialerBaum = baumRepository.speichere(baum);
        assertNotNull(initialerBaum, "Baum sollte erfolgreich gespeichert werden");
        int id = initialerBaum.getBaumId();

        // 2. Löschen und prüfen
        assertTrue(baumRepository.loesche(id), "Das Löschen sollte erfolgreich sein.");

        // 3. Abrufen und Validieren
        Baum geloeschterBaum = baumRepository.findById(id);
        assertNull(geloeschterBaum, "Der Baum sollte nach dem Löschen nicht mehr gefunden werden.");
    }
}