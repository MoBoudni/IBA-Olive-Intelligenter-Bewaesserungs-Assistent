package org.iba.service;

import org.iba.db.BaumRepository;
import org.iba.db.MesswerteRepository;
import org.iba.db.ParzelleRepository;
import org.iba.model.Baum;
import org.iba.model.Messwerte;
import org.iba.model.Parzelle;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Vereinfachte Version ohne checked Exceptions für schnelle Integration.
 */
public class BerechnungService {

    private final ParzelleRepository parzelleRepository;
    private final BaumRepository baumRepository;
    private final MesswerteRepository messwerteRepository;

    public BerechnungService(ParzelleRepository parzelleRepository,
                             BaumRepository baumRepository,
                             MesswerteRepository messwerteRepository) {
        this.parzelleRepository = parzelleRepository;
        this.baumRepository = baumRepository;
        this.messwerteRepository = messwerteRepository;
    }

    /**
     * Einfache Version ohne Exception-Deklarationen.
     */
    public Map<String, Double> berechneGesamtWasserbedarfProParzelle() {
        Map<String, Double> ergebnisse = new HashMap<>();

        try {
            // 1. Parzellen laden
            List<Parzelle> alleParzellen = parzelleRepository.findAlle();

            if (alleParzellen == null || alleParzellen.isEmpty()) {
                System.out.println("Keine Parzellen gefunden.");
                return ergebnisse;
            }

            // 2. Für jede Parzelle berechnen
            for (Parzelle parzelle : alleParzellen) {
                try {
                    double bedarf = berechneFuerParzelle(parzelle);
                    ergebnisse.put(parzelle.getName(), bedarf);
                } catch (Exception e) {
                    System.err.println("Fehler bei Parzelle " + parzelle.getName() + ": " + e.getMessage());
                    ergebnisse.put(parzelle.getName(), 0.0);
                }
            }

        } catch (Exception e) {
            System.err.println("Schwerwiegender Fehler: " + e.getMessage());
            e.printStackTrace();
        }

        return ergebnisse;
    }

    /**
     * Berechnung für einzelne Parzelle.
     */
    private double berechneFuerParzelle(Parzelle parzelle) {
        try {
            // 1. Messwerte laden
            Messwerte messwerte = messwerteRepository.findeLetzteMessung(parzelle.getParzelleId());
            if (messwerte == null) {
                System.out.println("Keine Messwerte für " + parzelle.getName());
                return 0.0;
            }

            // 2. Bäume laden (muss angepasst werden)
            List<Baum> baeume = Collections.emptyList(); // Platzhalter

            // 3. Berechnen
            return berechneWasserbedarf(baeume, messwerte);

        } catch (Exception e) {
            throw new RuntimeException("Berechnung fehlgeschlagen für " + parzelle.getName(), e);
        }
    }

    /**
     * Kern-Berechnungslogik (public für Tests).
     */
    public double berechneWasserbedarf(List<Baum> baeume, Messwerte messwerte) {
        if (baeume == null || baeume.isEmpty() || messwerte == null) {
            return 0.0;
        }

        double basisBedarf = baeume.stream()
                .mapToDouble(Baum::getBasisBedarf)
                .sum();

        double tempFaktor = Math.max(0.5, (messwerte.getTemperatur() - 25.0) / 50.0 + 1.0);
        double niederschlag = messwerte.getNiederschlag();

        double niederschlagFaktor = 1.0;
        if (niederschlag > 5.0) niederschlagFaktor = 0.5;
        else if (niederschlag > 1.0) niederschlagFaktor = 0.8;

        double bedarf = basisBedarf * tempFaktor * niederschlagFaktor;
        return Math.min(Math.max(0.0, bedarf), basisBedarf * 2.0);
    }
}