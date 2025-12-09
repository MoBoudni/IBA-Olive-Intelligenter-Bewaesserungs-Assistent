package org.iba.Integrationstest;

import org.iba.db.BaumRepository;
import org.iba.db.MesswerteRepository;
import org.iba.db.ParzelleRepository;
import org.iba.model.Baum;
import org.iba.service.TransaktionalerBerechnungService;

import java.util.Arrays;
import java.util.List;

/**
 * Testet den TransaktionalenBerechnungService.
 */
public class TransaktionalerServiceTest {

    public static void main(String[] args) {
        try {
            System.out.println("=== Test TransaktionalerBerechnungService ===\n");

            // Repositories initialisieren
            ParzelleRepository parzelleRepo = new ParzelleRepository();
            BaumRepository baumRepo = new BaumRepository();
            MesswerteRepository messwerteRepo = new MesswerteRepository();

            // Service initialisieren
            TransaktionalerBerechnungService service =
                    new TransaktionalerBerechnungService(parzelleRepo, baumRepo, messwerteRepo);

            // 1. Test-Parzelle erstellen
            System.out.println("1. Test-Parzelle erstellen...");
            List<Baum> testBaeume = Arrays.asList(
                    new Baum(0, 3, 1, 15.0),
                    new Baum(0, 7, 1, 22.0)
            );

            var testParzelle = service.initialisiereNeueParzelle(
                    "Test-" + System.currentTimeMillis(),
                    300.0,
                    "Mediterran",
                    101,
                    testBaeume
            );

            System.out.println("✓ Test-Parzelle erstellt: " + testParzelle.getName());
            System.out.println("  ID: " + testParzelle.getParzelleId());
            System.out.println("  Bäume: " + testParzelle.getAnzahlBaeume());

            // 2. Übersicht anzeigen
            System.out.println("\n2. Parzellenübersicht:");
            System.out.println(service.getParzellenUebersicht());

            // 3. Berechnung durchführen
            System.out.println("3. Berechnung für alle Parzellen...");
            var empfehlungen = service.berechneUndSpeichereFuerAlleParzellen();
            System.out.println("✓ " + empfehlungen.size() + " Empfehlungen berechnet");

            // 4. Test-Löschung (kommentiert, da destruktiv)
            /*
            System.out.println("\n4. Test-Parzelle löschen...");
            boolean geloescht = service.loescheParzelleKomplett(testParzelle.getParzelleId());
            System.out.println(geloescht ? "✓ Gelöscht" : "✗ Löschen fehlgeschlagen");
            */

            System.out.println("\n=== Test abgeschlossen ===");

        } catch (Exception e) {
            System.err.println("Test fehlgeschlagen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}