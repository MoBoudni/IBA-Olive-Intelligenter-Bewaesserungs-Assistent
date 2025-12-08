package org.iba;

import org.iba.db.BaumRepository;
import org.iba.db.MesswerteRepository;
import org.iba.db.ParzelleRepository;
import org.iba.model.Baum;
import org.iba.model.Parzelle;
import org.iba.model.Messwerte;
import org.iba.service.BerechnungService;

import java.util.Map;

/**
 * Hauptanwendungsklasse zum Initialisieren aller Komponenten und zum Testen
 * der Geschäftslogik (Berechnung des Wasserbedarfs).
 * Stellt sicher, dass die Datenbankverbindungskonstanten in den Repositories
 * korrekt sind, bevor diese Klasse ausgeführt wird.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("--- IBA Bewaesserungs-Assistent: Systemstart ---");

        // 1. Initialisierung der Persistenzschicht
        ParzelleRepository parzelleRepo = new ParzelleRepository();
        BaumRepository baumRepo = new BaumRepository();
        MesswerteRepository messwerteRepo = new MesswerteRepository();

        // Stellt sicher, dass alle Tabellen existieren und löscht alte Daten für den Test
        setupTestData(parzelleRepo, baumRepo, messwerteRepo);

        // 2. Initialisierung der Serviceschicht
        BerechnungService berechnungService = new BerechnungService(parzelleRepo, baumRepo, messwerteRepo);

        // 3. Ausführung der Kernlogik
        System.out.println("\n--- Starte Wasserbedarfsberechnung ---");
        Map<String, Double> ergebnisse = berechnungService.berechneGesamtWasserbedarfProParzelle();

        // 4. Ausgabe der Ergebnisse
        System.out.println("\n--- Ergebnisse der Bewässerungsempfehlung (Liter pro Tag) ---");
        if (ergebnisse.isEmpty()) {
            System.out.println("Es wurden keine Parzellen gefunden oder berechnet.");
        } else {
            ergebnisse.forEach((name, bedarf) ->
                    System.out.printf("Parzelle '%s': Benötigter Wasserbedarf: %.2f Liter%n", name, bedarf)
            );
        }

        System.out.println("\n--- Systemende ---");
    }

    /**
     * Setzt die Datenbank mit Testdaten auf.
     * Löscht vorhandene Daten in den Baum- und Parzellen-Tabellen.
     */
    private static void setupTestData(ParzelleRepository parzelleRepo, BaumRepository baumRepo, MesswerteRepository messwerteRepo) {
        try {
            System.out.println("Lösche vorhandene Testdaten und erstelle neue...");

            // 1. Bereinigung (in umgekehrter Abhängigkeitsreihenfolge)
            // HINWEIS: Ein echtes Repository benötigt eine Methode zum Löschen aller Daten, hier wird es simuliert

            // 2. Erstellung der Parzellen
            // KORREKTUR: Verwendung des 6-Argumente-Konstruktors (ID, Name, Anzahl Bäume, Fläche, KlimaZone, BesitzerID)
            // die ID (0) wird als Platzhalter verwendet. Die Anzahl der Bäume wird auf 2 gesetzt.
            Parzelle parzelleA = parzelleRepo.speichere(new Parzelle(0, "Feld Südhang", 2, 1000.0, "Mediterran", 101));
            Parzelle parzelleB = parzelleRepo.speichere(new Parzelle(0, "Feld Nordseite", 2, 800.0, "Kontinental", 101));

            System.out.println("Parzellen erstellt: " + parzelleA.getName() + " (ID: " + parzelleA.getParzelleId() + "), " + parzelleB.getName() + " (ID: " + parzelleB.getParzelleId() + ")");

            // 3. Erstellung der Bäume (alle mit PflanzenartId 1)
            int pflanzenartId = 1; // Olivenbaum

            // Parzelle A (Südhang) - Trockenhoheitsszenario
            baumRepo.speichere(new Baum(parzelleA.getParzelleId(), 5, pflanzenartId, 25.0)); // 5 Jahre, 25l Basis
            baumRepo.speichere(new Baum(parzelleA.getParzelleId(), 12, pflanzenartId, 30.0)); // 12 Jahre, 30l Basis

            // Parzelle B (Nordseite) - Feuchtigkeitsszenario
            baumRepo.speichere(new Baum(parzelleB.getParzelleId(), 2, pflanzenartId, 20.0)); // 2 Jahre, 20l Basis
            baumRepo.speichere(new Baum(parzelleB.getParzelleId(), 7, pflanzenartId, 28.0)); // 7 Jahre, 28l Basis

            // 4. Erstellung der Messwerte
            // Parzelle A: Hohe Temperatur, wenig Niederschlag (HOCH-BEDARF)
            Messwerte datenA = new Messwerte(30.0, 0.5); // 30°C, 0.5mm Regen
            messwerteRepo.speichere(datenA, parzelleA.getParzelleId());

            // Parzelle B: Mittlere Temperatur, viel Niederschlag (NIEDRIG-BEDARF)
            Messwerte datenB = new Messwerte(20.0, 10.0); // 20°C, 10mm Regen
            messwerteRepo.speichere(datenB, parzelleB.getParzelleId());

            System.out.println("Testdaten erfolgreich in die Datenbank geschrieben.");

        } catch (IllegalArgumentException e) {
            System.err.println("FEHLER beim Erstellen der Testdaten (ungültige Modelldaten): " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ein unerwarteter Fehler beim Setup ist aufgetreten: " + e.getMessage());
        }
    }
}