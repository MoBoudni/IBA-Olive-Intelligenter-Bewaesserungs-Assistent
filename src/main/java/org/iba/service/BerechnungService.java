package org.iba.service;

import org.iba.db.BaumRepository;
import org.iba.db.MesswerteRepository;
import org.iba.db.ParzelleRepository;
import org.iba.model.Baum;
import org.iba.model.Parzelle;
import org.iba.model.Messwerte;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service-Klasse zur Durchführung der Kerngeschäftslogik:
 * Berechnung des individuellen Wasserbedarfs pro Parzelle basierend auf Baumdaten und aktuellen Wetterbedingungen.
 */
public class BerechnungService {

    // Abstraktion über Repositories (Dependency Injection in realen Anwendungen)
    private final ParzelleRepository parzelleRepository;
    private final BaumRepository baumRepository;
    private final MesswerteRepository messwerteRepository;

    /**
     * Konstruktor zur Initialisierung der benötigten Repositories.
     */
    public BerechnungService(ParzelleRepository parzelleRepository, BaumRepository baumRepository, MesswerteRepository messwerteRepository) {
        this.parzelleRepository = parzelleRepository;
        this.baumRepository = baumRepository;
        this.messwerteRepository = messwerteRepository;
    }

    /**
     * Berechnet den empfohlenen Gesamt-Wasserbedarf (in Litern) für jede Parzelle.
     *
     * @return Eine Map, die Parzellen-Namen auf ihren berechneten Wasserbedarf abbildet.
     */
    public Map<String, Double> berechneGesamtWasserbedarfProParzelle() {
        // 1. Alle Parzellen laden
        List<Parzelle> alleParzellen = parzelleRepository.findAlle();
        // 2. Alle Bäume laden (könnte optimiert werden)
        List<Baum> alleBaeume = baumRepository.findAlle();

        Map<String, Double> ergebnisse = new HashMap<>();

        for (Parzelle parzelle : alleParzellen) {
            // 3. Messwerte für die Parzelle abrufen (Wir nehmen an, Messungen sind Parzellen-spezifisch)
            Messwerte aktuelleMesswerte = messwerteRepository.findeLetzteMessung(parzelle.getParzelleId());

            if (aktuelleMesswerte == null) {
                System.err.println("WARNUNG: Keine Messwerte für Parzelle " + parzelle.getName() + " gefunden. Überspringe Berechnung.");
                ergebnisse.put(parzelle.getName(), 0.0);
                continue;
            }

            // 4. Nur Bäume filtern, die zu dieser Parzelle gehören
            List<Baum> baeumeAufParzelle = alleBaeume.stream()
                    .filter(baum -> baum.getParzelleId() == parzelle.getParzelleId())
                    .collect(Collectors.toList());

            // 5. Gesamten Wasserbedarf für diese Parzelle berechnen
            double gesamtbedarf = berechneWasserbedarf(baeumeAufParzelle, aktuelleMesswerte);
            ergebnisse.put(parzelle.getName(), gesamtbedarf);
        }

        return ergebnisse;
    }

    /**
     * Berechnet den kombinierten Wasserbedarf für eine Liste von Bäumen unter den gegebenen Wetterbedingungen.
     *
     * @param baeume Die Bäume auf der Parzelle.
     * @param messwerte Die aktuellen Messwerte.
     * @return Der Gesamtbedarf in Litern.
     */
    private double berechneWasserbedarf(List<Baum> baeume, Messwerte messwerte) {
        if (baeume.isEmpty()) {
            return 0.0;
        }

        double temperatur = messwerte.getTemperatur();
        double niederschlag = messwerte.getNiederschlag(); // KORREKTUR: Instanz-Methode, nicht static
        double gesamterBasisbedarf = baeume.stream().mapToDouble(Baum::getBasisBedarf).sum();

        // --- Vereinfachte Berechnungslogik (Kern der Domäne) ---

        // 1. Temperaturfaktor: Bei 25° C ist der Faktor 1.0. Steigt um 0.1 pro 5° C darüber, sinkt um 0.1 pro 5° C darunter.
        // Vereinfachte Formel: (Temperatur - 25) / 5 * 0.1 + 1.0
        double temperaturFaktor = Math.max(0.5, (temperatur - 25.0) / 50.0 + 1.0);  // Faktor kann nicht unter 0.5 fallen

        // 2. Niederschlagsfaktor: Wenn Niederschlag > 5 mm, Bewässerung um 50 % reduzieren.
        double niederschlagFaktor = 1.0;
        if (niederschlag > 5.0) {
            niederschlagFaktor = 0.5; // Reduktion um 50 %
        } else if (niederschlag > 1.0) {
            niederschlagFaktor = 0.8; // Reduktion um 20 %
        }

        // 3. Endgültiger Bedarf: Basisbedarf * Temperaturfaktor * Niederschlagsfaktor
        double berechneterBedarf = gesamterBasisbedarf * temperaturFaktor * niederschlagFaktor;

        // Sicherstellen, dass der Bedarf nicht negativ wird, aber auch nicht zu hoch ist (z.B. max 2x Basisbedarf)
        return Math.min(Math.max(0.0, berechneterBedarf), gesamterBasisbedarf * 2.0);
    }
}