package org.iba.Integrationstest;

import org.iba.db.ParzelleRepository;
import org.iba.model.Parzelle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integrationstest für das ParzelleRepository.
 */
class ParzelleRepositoryTest {

    private ParzelleRepository repository;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

//    @BeforeEach
//    void setUp() {
//        repository = new ParzelleRepository();
//
//        try (Connection conn = getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            // Bestehende Test-Parzellen löschen
//            stmt.execute("DELETE FROM parzelle WHERE name LIKE 'TestParzelle%'");
//
//        } catch (SQLException e) {
//            fail("Fehler bei der Einrichtung: " + e.getMessage());
//        }
//    }
//
//    @AfterEach
//    void tearDown() {
//        try (Connection conn = getConnection();
//             Statement stmt = conn.createStatement()) {
//
//            // Test-Parzellen aufräumen
//            stmt.execute("DELETE FROM parzelle WHERE name LIKE 'TestParzelle%'");
//
//        } catch (SQLException e) {
//            System.err.println("WARNUNG: Konnte Testdaten nicht aufräumen: " + e.getMessage());
//        }
//    }
@BeforeEach
void setUp() {
    repository = new ParzelleRepository();

    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {

        // ALLE Test-Parzellen löschen (auch von vorherigen Testläufen)
        stmt.execute("DELETE FROM parzelle WHERE name LIKE 'TestParzelle%' " +
                "OR name LIKE 'TestDuplicate_%' " +
                "OR name LIKE 'DuplicateTest_%' " +
                "OR name LIKE 'EindeutigerName%'");

    } catch (SQLException e) {
        fail("Fehler bei der Einrichtung: " + e.getMessage());
    }
}

    @AfterEach
    void tearDown() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Test-Parzellen aufräumen
            stmt.execute("DELETE FROM parzelle WHERE name LIKE 'TestParzelle%' " +
                    "OR name LIKE 'TestDuplicate_%' " +
                    "OR name LIKE 'DuplicateTest_%' " +
                    "OR name LIKE 'EindeutigerName%'");

        } catch (SQLException e) {
            // Nur warnen, nicht failen im tearDown
            System.err.println("WARNUNG: Konnte Testdaten nicht vollständig aufräumen: " + e.getMessage());
        }
    }

    @Test
    void speichereSollteErfolgreichSein() {
        // ARRANGE
        Parzelle neueParzelle = new Parzelle(
                0,  // ID wird von DB generiert
                "TestParzelle1",
                5,  // anzahl_baeume
                1000.0,  // flaeche_qm
                "Mediterran",  // klima_zone
                101  // besitzer_id
        );

        // ACT
        Parzelle gespeicherteParzelle = repository.speichere(neueParzelle);

        // ASSERT
        assertNotNull(gespeicherteParzelle, "Die gespeicherte Parzelle sollte nicht null sein.");
        assertTrue(gespeicherteParzelle.getParzelleId() > 0, "Es sollte eine ID generiert worden sein.");
        assertEquals("TestParzelle1", gespeicherteParzelle.getName());
        assertEquals(5, gespeicherteParzelle.getAnzahlBaeume());
        assertEquals(1000.0, gespeicherteParzelle.getFlaecheQm(), 0.001);
        assertEquals("Mediterran", gespeicherteParzelle.getKlimaZone());
        assertEquals(101, gespeicherteParzelle.getBesitzerId());
    }

    @Test
    void findAlleSollteAlleParzellenZurueckgeben() {
        // ARRANGE: Zwei Test-Parzellen erstellen
        Parzelle parzelle1 = repository.speichere(new Parzelle(0, "TestParzelleA", 3, 500.0, "Kontinental", 101));
        Parzelle parzelle2 = repository.speichere(new Parzelle(0, "TestParzelleB", 7, 800.0, "Mediterran", 102));

        assertNotNull(parzelle1);
        assertNotNull(parzelle2);

        // ACT
        List<Parzelle> alleParzellen = repository.findAlle();

        // ASSERT
        assertNotNull(alleParzellen);
        assertTrue(alleParzellen.size() >= 2, "Es sollten mindestens 2 Parzellen gefunden werden.");

        // Prüfen ob unsere Test-Parzellen enthalten sind
        boolean parzelle1Gefunden = alleParzellen.stream()
                .anyMatch(p -> p.getName().equals("TestParzelleA"));
        boolean parzelle2Gefunden = alleParzellen.stream()
                .anyMatch(p -> p.getName().equals("TestParzelleB"));

        assertTrue(parzelle1Gefunden, "TestParzelleA sollte gefunden werden.");
        assertTrue(parzelle2Gefunden, "TestParzelleB sollte gefunden werden.");
    }

    @Test
    void aktualisiereSollteErfolgreichSein() {
        // ARRANGE: Parzelle erstellen
        Parzelle parzelle = repository.speichere(new Parzelle(0, "TestParzelleUpdate", 5, 600.0, "Kontinental", 101));
        assertNotNull(parzelle);
        int parzelleId = parzelle.getParzelleId();

        // ACT: Parzelle aktualisieren
        parzelle.setName("TestParzelleUpdated");
        parzelle.setAnzahlBaeume(10);
        parzelle.setFlaecheQm(750.0);
        parzelle.setKlimaZone("Mediterran");
        parzelle.setBesitzerId(201);

        boolean updateErfolgreich = repository.aktualisiere(parzelle);

        // ASSERT
        assertTrue(updateErfolgreich, "Update sollte erfolgreich sein.");

        // Überprüfen durch erneutes Laden aller Parzellen
        List<Parzelle> alleParzellen = repository.findAlle();
        Parzelle aktualisierteParzelle = alleParzellen.stream()
                .filter(p -> p.getParzelleId() == parzelleId)
                .findFirst()
                .orElse(null);

        assertNotNull(aktualisierteParzelle, "Aktualisierte Parzelle sollte gefunden werden.");
        assertEquals("TestParzelleUpdated", aktualisierteParzelle.getName());
        assertEquals(10, aktualisierteParzelle.getAnzahlBaeume());
        assertEquals(750.0, aktualisierteParzelle.getFlaecheQm(), 0.001);
        assertEquals("Mediterran", aktualisierteParzelle.getKlimaZone());
        assertEquals(201, aktualisierteParzelle.getBesitzerId());
    }

    @Test
    void loescheSollteErfolgreichSein() {
        // ARRANGE: Parzelle erstellen
        Parzelle parzelle = repository.speichere(new Parzelle(0, "TestParzelleDelete", 3, 400.0, "Kontinental", 101));
        assertNotNull(parzelle);
        int parzelleId = parzelle.getParzelleId();

        // ACT: Parzelle löschen
        boolean deleteErfolgreich = repository.loesche(parzelleId);

        // ASSERT
        assertTrue(deleteErfolgreich, "Löschen sollte erfolgreich sein.");

        // Überprüfen dass Parzelle nicht mehr existiert
        List<Parzelle> alleParzellen = repository.findAlle();
        boolean parzelleNochVorhanden = alleParzellen.stream()
                .anyMatch(p -> p.getParzelleId() == parzelleId);

        assertFalse(parzelleNochVorhanden, "Gelöschte Parzelle sollte nicht mehr gefunden werden.");
    }

//    @Test
//    void speichereMitDoppeltemNamenSollteFehlschlagen() {
//        // ARRANGE: Erste Parzelle mit eindeutigem Namen
//        Parzelle ersteParzelle = repository.speichere(new Parzelle(0, "EindeutigerName", 5, 1000.0, "Zone1", 101));
//        assertNotNull(ersteParzelle);
//
//        // ACT: Zweite Parzelle mit gleichem Namen versuchen zu speichern
//        Parzelle zweiteParzelle = repository.speichere(new Parzelle(0, "EindeutigerName", 3, 500.0, "Zone2", 102));
//
//        // ASSERT: Zweite Speicherung sollte wegen UNIQUE Constraint fehlschlagen
//        assertNull(zweiteParzelle, "Parzelle mit doppeltem Namen sollte nicht gespeichert werden können.");
//    }

//    *******************************************************************

//    @Test
//    void speichereMitDoppeltemNamenSollteFehlschlagen() {
//        // ARRANGE: Eindeutigen Namen für diesen Test
//        String testName = "EindeutigerName_DuplicateTest";
//
//        // Zuerst sicherstellen, dass der Name nicht bereits existiert
//        try (Connection conn = getConnection();
//             Statement stmt = conn.createStatement()) {
//            stmt.execute("DELETE FROM parzelle WHERE name = '" + testName + "'");
//        } catch (SQLException e) {
//            fail("Konnte alte Testdaten nicht löschen: " + e.getMessage());
//        }
//
//        // Erste Parzelle mit eindeutigem Namen
//        Parzelle ersteParzelle = repository.speichere(new Parzelle(0, testName, 5, 1000.0, "Zone1", 101));
//        assertNotNull(ersteParzelle, "Erste Parzelle sollte erfolgreich gespeichert werden.");
//
//        // ACT: Zweite Parzelle mit GLEICHEM Namen versuchen zu speichern
//        Parzelle zweiteParzelle = repository.speichere(new Parzelle(0, testName, 3, 500.0, "Zone2", 102));
//
//        // ASSERT: Zweite Speicherung sollte wegen UNIQUE Constraint fehlschlagen
//        assertNull(zweiteParzelle,
//                "Parzelle mit doppeltem Namen sollte nicht gespeichert werden können. SQL sollte UNIQUE Constraint werfen.");
//    }
//
//    @Test
//    void speichereMitDoppeltemNamenSollteNullZurueckgeben() {
//        // Dieser Test prüft explizit das Verhalten der Repository-Methode
//        String name = "DuplicateNameTest";
//
//        // Cleanup
//        try (Connection conn = getConnection();
//             Statement stmt = conn.createStatement()) {
//            stmt.execute("DELETE FROM parzelle WHERE name = '" + name + "'");
//        } catch (SQLException ignored) {}
//
//        // Erste Parzelle sollte erfolgreich sein
//        Parzelle erste = repository.speichere(new Parzelle(0, name, 1, 100.0, "Zone", 100));
//        assertNotNull(erste, "Erste Speicherung sollte erfolgreich sein");
//
//        // Zweite mit gleichem Namen sollte null zurückgeben
//        Parzelle zweite = repository.speichere(new Parzelle(0, name, 2, 200.0, "Zone2", 200));
//        assertNull(zweite, "Zweite Speicherung mit gleichem Namen sollte null zurückgeben");
//
//        // Verifizieren dass nur eine Parzelle mit diesem Namen existiert
//        List<Parzelle> alle = repository.findAlle();
//        long anzahlMitDiesemNamen = alle.stream()
//                .filter(p -> p.getName().equals(name))
//                .count();
//        assertEquals(1, anzahlMitDiesemNamen, "Es sollte genau eine Parzelle mit diesem Namen geben");
//    }

//    @Test
//    void speichereMitDoppeltemNamenSollteFehlschlagen() {
//        // ARRANGE: Eindeutigen Namen mit Zeitstempel für absolute Eindeutigkeit
//        String testName = "EindeutigerName_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
//
//        // Zuerst sicherstellen, dass der Name nicht bereits existiert
//        try (Connection conn = getConnection();
//             Statement stmt = conn.createStatement()) {
//            stmt.execute("DELETE FROM parzelle WHERE name = '" + testName + "'");
//        } catch (SQLException e) {
//            fail("Konnte alte Testdaten nicht löschen: " + e.getMessage());
//        }
//
//        // Erste Parzelle mit eindeutigem Namen
//        Parzelle ersteParzelle = repository.speichere(new Parzelle(0, testName, 5, 1000.0, "Zone1", 101));
//        assertNotNull(ersteParzelle, "Erste Parzelle sollte erfolgreich gespeichert werden.");
//
//        // ACT: Zweite Parzelle mit GLEICHEM Namen versuchen zu speichern
//        Parzelle zweiteParzelle = repository.speichere(new Parzelle(0, testName, 3, 500.0, "Zone2", 102));
//
//        // ASSERT: Zweite Speicherung sollte wegen UNIQUE Constraint fehlschlagen
//        assertNull(zweiteParzelle,
//                "Parzelle mit doppeltem Namen sollte nicht gespeichert werden können. SQL sollte UNIQUE Constraint werfen.");
//    }

    @Test
    void speichereMitDoppeltemNamenSollteFehlschlagen() {
        // ARRANGE: Eindeutigen Namen mit Zeitstempel + Thread-ID für absolute Eindeutigkeit
        String testName = "TestDuplicate_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();

        // Cleanup: Sicherstellen, dass Name nicht existiert
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM parzelle WHERE name = '" + testName + "'");
        } catch (SQLException e) {
            fail("Konnte Cleanup nicht durchführen: " + e.getMessage());
        }

        // Erste Parzelle mit eindeutigem Namen (sollte erfolgreich sein)
        Parzelle ersteParzelle = repository.speichere(new Parzelle(0, testName, 5, 1000.0, "Zone1", 101));
        assertNotNull(ersteParzelle,
                "Erste Parzelle mit Namen '" + testName + "' sollte erfolgreich gespeichert werden.");
        assertTrue(ersteParzelle.getParzelleId() > 0,
                "Erste Parzelle sollte eine generierte ID haben.");

        // ACT: Zweite Parzelle mit GLEICHEM Namen versuchen zu speichern
        Parzelle zweiteParzelle = repository.speichere(new Parzelle(0, testName, 3, 500.0, "Zone2", 102));

        // ASSERT: Zweite Speicherung sollte wegen UNIQUE Constraint fehlschlagen
        assertNull(zweiteParzelle,
                "Zweite Parzelle mit gleichem Namen '" + testName + "' sollte null zurückgeben. " +
                        "Erwartet: UNIQUE Constraint Fehler.");

        // Zusätzliche Verifikation: Es sollte genau eine Parzelle mit diesem Namen geben
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM parzelle WHERE name = '" + testName + "'")) {

            if (rs.next()) {
                int count = rs.getInt("count");
                assertEquals(1, count,
                        "Es sollte genau 1 Parzelle mit Namen '" + testName + "' in der DB existieren.");
            }
        } catch (SQLException e) {
            fail("Konnte Verifikation nicht durchführen: " + e.getMessage());
        }
    }
}