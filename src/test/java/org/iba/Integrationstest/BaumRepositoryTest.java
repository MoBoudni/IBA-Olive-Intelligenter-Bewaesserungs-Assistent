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
 * Integrationstest für das BaumRepository (abwärtskompatible Version).
 * Funktioniert sowohl mit alten als auch neuen Repository-Implementierungen.
 */
class BaumRepositoryTest {

    private BaumRepository baumRepository;
    private ParzelleRepository parzelleRepository;

    // Test-Konstanten
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

            // Test-Parzelle mit eindeutigem Namen
            String testParzelleName = "TestParzelle_" + System.currentTimeMillis();

            // Alte Test-Parzellen löschen
            stmt.execute("DELETE FROM parzelle WHERE name LIKE 'TestParzelle_%'");

            // Neue Test-Parzelle in DB direkt erstellen (um Repository-Probleme zu umgehen)
            String insertSql = String.format(
                    "INSERT INTO parzelle (name, anzahl_baeume, flaeche_qm, klima_zone, besitzer_id) " +
                            "VALUES ('%s', 0, 100.0, 'TestZone', 101)",
                    testParzelleName
            );
            stmt.executeUpdate(insertSql, Statement.RETURN_GENERATED_KEYS);

            // Generierte ID holen
            try (var generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int parzelleId = generatedKeys.getInt(1);

                    // Bäume für diese Parzelle löschen
                    stmt.execute("DELETE FROM baum WHERE parzelle_id = " + parzelleId);
                }
            }

        } catch (SQLException e) {
            fail("Fehler bei der Test-Einrichtung: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Alle Test-Parzellen und ihre Bäume löschen
            stmt.execute("DELETE FROM baum WHERE parzelle_id IN " +
                    "(SELECT parzelle_id FROM parzelle WHERE name LIKE 'TestParzelle_%')");
            stmt.execute("DELETE FROM parzelle WHERE name LIKE 'TestParzelle_%'");

        } catch (SQLException e) {
            System.err.println("WARNUNG: Konnte Testdaten nicht aufräumen: " + e.getMessage());
        }
    }

    @Test
    void speichereUndFindeAlleVonParzelleSollteErfolgreichSein() {
        try {
            // Finde eine Test-Parzelle
            List<Parzelle> parzellen = parzelleRepository.findAlle();
            Parzelle testParzelle = parzellen.stream()
                    .filter(p -> p.getName().startsWith("TestParzelle_"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(testParzelle, "Test-Parzelle sollte existieren");
            int parzelleId = testParzelle.getParzelleId();

            final double erwarteterBedarf = 12.55;

            // Erstellen und speichern des Baum-Objekts
            Baum neuerBaum = new Baum(parzelleId, 5, 1, erwarteterBedarf);

            // Versuche zu speichern (kann Exception werfen oder nicht)
            Baum gespeicherterBaum = null;
            try {
                gespeicherterBaum = baumRepository.speichere(neuerBaum);
            } catch (Exception e) {
                // Wenn Repository Exceptions wirft, fangen wir sie ab
                fail("Speichern fehlgeschlagen: " + e.getMessage());
            }

            assertNotNull(gespeicherterBaum, "Der gespeicherte Baum sollte nicht null sein.");
            int id = gespeicherterBaum.getBaumId();
            assertTrue(id > 0, "Die generierte ID sollte größer als 0 sein.");

            // Versuche Bäume zu finden
            List<Baum> baeume = null;
            try {
                baeume = baumRepository.findByParzelleId(parzelleId);
            } catch (Exception e) {
                fail("findByParzelleId fehlgeschlagen: " + e.getMessage());
            }

            assertNotNull(baeume, "Bäume-Liste sollte nicht null sein");
            assertFalse(baeume.isEmpty(), "Mindestens ein Baum sollte gefunden werden");

            Baum gefundenerBaum = baeume.get(0);
            assertEquals(id, gefundenerBaum.getBaumId());
            assertEquals(5, gefundenerBaum.getAlterJahre());
            assertEquals(1, gefundenerBaum.getPflanzenartId());
            assertEquals(erwarteterBedarf, gefundenerBaum.getBasisBedarf(), 0.001);

        } catch (Exception e) {
            fail("Unerwarteter Fehler im Test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testeAktualisierenUndLoeschen() {
        try {
            // Finde Test-Parzelle
            List<Parzelle> parzellen = parzelleRepository.findAlle();
            Parzelle testParzelle = parzellen.stream()
                    .filter(p -> p.getName().startsWith("TestParzelle_"))
                    .findFirst()
                    .orElse(null);

            assertNotNull(testParzelle, "Test-Parzelle sollte existieren");
            int parzelleId = testParzelle.getParzelleId();

            // Baum speichern
            Baum baum = new Baum(parzelleId, 10, 1, 5.0);
            Baum gespeicherterBaum = null;

            try {
                gespeicherterBaum = baumRepository.speichere(baum);
            } catch (Exception e) {
                fail("Speichern fehlgeschlagen: " + e.getMessage());
            }

            assertNotNull(gespeicherterBaum);
            int baumId = gespeicherterBaum.getBaumId();

            // Aktualisieren
            gespeicherterBaum.setAlterJahre(15);
            gespeicherterBaum.setBasisBedarf(7.77);
            gespeicherterBaum.setPflanzenartId(2);

            boolean updateErfolg = false;
            try {
                updateErfolg = baumRepository.aktualisiere(gespeicherterBaum);
            } catch (Exception e) {
                fail("Aktualisieren fehlgeschlagen: " + e.getMessage());
            }

            assertTrue(updateErfolg, "Aktualisierung sollte erfolgreich sein");

            // Löschen
            boolean deleteErfolg = false;
            try {
                deleteErfolg = baumRepository.loesche(baumId);
            } catch (Exception e) {
                fail("Löschen fehlgeschlagen: " + e.getMessage());
            }

            assertTrue(deleteErfolg, "Löschen sollte erfolgreich sein");

        } catch (Exception e) {
            fail("Unerwarteter Fehler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}