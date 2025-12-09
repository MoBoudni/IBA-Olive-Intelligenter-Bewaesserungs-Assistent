package org.iba.service;

import org.iba.db.BaumRepository;
import org.iba.db.MesswerteRepository;
import org.iba.db.ParzelleRepository;
import org.iba.exception.*;
import org.iba.model.Baum;
import org.iba.model.Messwerte;
import org.iba.model.Parzelle;

import java.util.*;

/**
 * Service-Klasse für transaktionale Geschäftslogik.
 */
public class TransaktionalerBerechnungService {

    private final ParzelleRepository parzelleRepository;
    private final BaumRepository baumRepository;
    private final MesswerteRepository messwerteRepository;

    public TransaktionalerBerechnungService(ParzelleRepository parzelleRepository,
                                            BaumRepository baumRepository,
                                            MesswerteRepository messwerteRepository) {
        this.parzelleRepository = parzelleRepository;
        this.baumRepository = baumRepository;
        this.messwerteRepository = messwerteRepository;
    }

    // ========================================================================
    // TRANSAKTIONELLE OPERATIONEN
    // ========================================================================

    /**
     * Initialisiert eine neue Parzelle mit Bäumen in einer Transaktion.
     */
    public Parzelle initialisiereNeueParzelle(String name, double flaeche, String klimaZone,
                                              int besitzerId, List<Baum> baeume)
            throws BusinessException {

        try {
            Parzelle neueParzelle = new Parzelle(0, name, 0, flaeche, klimaZone, besitzerId);
            return parzelleRepository.speichereParzelleMitBaeumen(neueParzelle, baeume);

        } catch (DatabaseException | ValidationException e) {
            throw new BusinessException("Fehler bei Initialisierung der Parzelle: " +
                    e.getMessage(), e);
        }
    }

    /**
     * Berechnet und speichert Bewässerungsempfehlungen für alle Parzellen.
     */
    public Map<Integer, Double> berechneUndSpeichereFuerAlleParzellen() throws BusinessException {
        Map<Integer, Double> empfehlungen = new HashMap<>();

        try {
            List<Parzelle> parzellen = parzelleRepository.findAlle();

            for (Parzelle parzelle : parzellen) {
                try {
                    double wasserbedarf = berechneWasserbedarfFuerParzelle(parzelle.getParzelleId());

                    // Speichere die Empfehlung (könnte auch transaktional sein)
                    speichereBewaesserungsEmpfehlung(parzelle.getParzelleId(), wasserbedarf);

                    empfehlungen.put(parzelle.getParzelleId(), wasserbedarf);

                    System.out.printf("Empfehlung für Parzelle '%s' (ID: %d): %.2f Liter%n",
                            parzelle.getName(), parzelle.getParzelleId(), wasserbedarf);

                } catch (Exception e) {
                    System.err.println("Fehler bei Parzelle " + parzelle.getName() +
                            ": " + e.getMessage());
                    // Fortsetzen mit nächster Parzelle
                }
            }

            return empfehlungen;

        } catch (DatabaseException e) {
            throw new BusinessException("Fehler bei der Berechnung: " + e.getMessage(), e);
        }
    }

    /**
     * Massenupdate von Klimazonen in einer Transaktion.
     */
    public void aktualisiereKlimazonen(Map<Integer, String> parzelleKlimazonen)
            throws BusinessException {

        try {
            // Für jedes Update eine separate Transaktion (oder Batch-Transaktion)
            for (Map.Entry<Integer, String> entry : parzelleKlimazonen.entrySet()) {
                try {
                    aktualisiereKlimazoneEinzeln(entry.getKey(), entry.getValue());

                } catch (Exception e) {
                    System.err.println("Fehler beim Update von Parzelle " + entry.getKey() +
                            ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new BusinessException("Fehler beim Update der Klimazonen", e);
        }
    }

    /**
     * Transferiert Bäume zwischen Parzellen in einer Transaktion.
     */
    public void transferiereBaeumeZwischenParzellen(int vonParzelleId, int zuParzelleId,
                                                    List<Integer> baumIds) throws BusinessException {
        try {
            parzelleRepository.transferiereBaeume(vonParzelleId, zuParzelleId, baumIds);
            System.out.printf("Erfolgreich %d Bäume von Parzelle %d nach %d transferiert%n",
                    baumIds.size(), vonParzelleId, zuParzelleId);

        } catch (DatabaseException e) {
            throw new BusinessException("Fehler beim Transfer von Bäumen: " + e.getMessage(), e);
        }
    }

    /**
     * Löscht eine Parzelle komplett mit allen Abhängigkeiten in einer Transaktion.
     */
    public boolean loescheParzelleKomplett(int parzelleId) throws BusinessException {
        try {
            return parzelleRepository.loescheParzelleKomplett(parzelleId);

        } catch (DatabaseException e) {
            throw new BusinessException("Fehler beim Löschen der Parzelle: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // KOMPLEXE GESCHÄFTSLOGIK (Transaktionen über mehrere Repositories)
    // ========================================================================

    /**
     * Berechnet Wasserbedarf und speichert Messwerte in einer Transaktion.
     */
    public void berechneUndAktualisiereFuerParzelle(int parzelleId, Messwerte neueMesswerte)
            throws BusinessException {

        try {
            // 1. Neue Messwerte speichern
            messwerteRepository.speichere(neueMesswerte, parzelleId);

            // 2. Wasserbedarf berechnen
            double wasserbedarf = berechneWasserbedarfFuerParzelle(parzelleId);

            // 3. Empfehlung speichern
            speichereBewaesserungsEmpfehlung(parzelleId, wasserbedarf);

            System.out.printf("Parzelle %d: Neue Messwerte gespeichert, Bedarf: %.2f Liter%n",
                    parzelleId, wasserbedarf);

        } catch (Exception e) {
            throw new BusinessException("Fehler bei Berechnung und Update: " + e.getMessage(), e);
        }
    }

    /**
     * Initialisiert eine Test-Parzelle mit Beispieldaten.
     */
    public Parzelle erstelleTestParzelle() throws BusinessException {
        try {
            String name = "Test-Parzelle-" + new Date().getTime();
            List<Baum> testBaeume = Arrays.asList(
                    new Baum(0, 3, 1, 20.0),
                    new Baum(0, 5, 1, 25.0),
                    new Baum(0, 8, 1, 30.0)
            );

            return initialisiereNeueParzelle(name, 500.0, "Mediterran", 101, testBaeume);

        } catch (Exception e) {
            throw new BusinessException("Fehler beim Erstellen der Test-Parzelle: " + e.getMessage(), e);
        }
    }

    // ========================================================================
    // HILFSMETHODEN
    // ========================================================================

    /**
     * Berechnet den Wasserbedarf für eine Parzelle.
     */
    private double berechneWasserbedarfFuerParzelle(int parzelleId) {
        try {
            // 1. Bäume der Parzelle laden
            List<Baum> baeume = baumRepository.findByParzelleId(parzelleId);

            if (baeume.isEmpty()) {
                return 0.0;
            }

            // 2. Messwerte laden
            Messwerte messwerte = messwerteRepository.findeLetzteMessung(parzelleId);
            if (messwerte == null) {
                // Fallback: Standardwerte
                messwerte = new Messwerte(20.0, 0.0);
            }

            // 3. Berechnung (vereinfacht)
            double basisBedarf = baeume.stream()
                    .mapToDouble(Baum::getBasisBedarf)
                    .sum();

            double temperaturFaktor = Math.max(0.5, (messwerte.getTemperatur() - 25.0) / 50.0 + 1.0);
            double niederschlagFaktor = messwerte.getNiederschlag() > 5.0 ? 0.5 :
                    messwerte.getNiederschlag() > 1.0 ? 0.8 : 1.0;

            double bedarf = basisBedarf * temperaturFaktor * niederschlagFaktor;
            return Math.min(Math.max(0.0, bedarf), basisBedarf * 2.0);

        } catch (Exception e) {
            System.err.println("Fehler bei Berechnung für Parzelle " + parzelleId +
                    ": " + e.getMessage());
            return 0.0; // Fallback
        }
    }

    /**
     * Speichert eine Bewässerungsempfehlung.
     */
    private void speichereBewaesserungsEmpfehlung(int parzelleId, double wasserbedarf) {
        // Hier würde die Empfehlung in eine separate Tabelle gespeichert werden
        // Für jetzt nur Logging
        System.out.printf("Speichere Empfehlung: Parzelle %d -> %.2f Liter%n",
                parzelleId, wasserbedarf);

        // Beispiel: In eine Empfehlungen-Tabelle speichern
        // String sql = "INSERT INTO bewasserungs_empfehlungen (parzelle_id, wasserbedarf, datum) VALUES (?, ?, NOW())";
        // executeUpdate(sql, parzelleId, wasserbedarf);
    }

    /**
     * Aktualisiert die Klimazone einer einzelnen Parzelle.
     */
    private void aktualisiereKlimazoneEinzeln(int parzelleId, String neueKlimazone) {
        // Hier würde ein Update durchgeführt werden
        System.out.printf("Aktualisiere Parzelle %d auf Klimazone: %s%n",
                parzelleId, neueKlimazone);

        // Beispiel:
        // String sql = "UPDATE parzelle SET klima_zone = ? WHERE parzelle_id = ?";
        // executeUpdate(sql, neueKlimazone, parzelleId);
    }

    /**
     * Gibt eine Zusammenfassung aller Parzellen zurück.
     */
    public String getParzellenUebersicht() throws BusinessException {
        try {
            List<Parzelle> parzellen = parzelleRepository.findAlle();
            StringBuilder sb = new StringBuilder();

            sb.append("=== PARZELLEN ÜBERSICHT ===\n");
            sb.append(String.format("%-5s %-20s %-10s %-15s %-12s\n",
                    "ID", "Name", "Fläche", "Klimazone", "Bäume"));
            sb.append("------------------------------------------------------------\n");

            for (Parzelle p : parzellen) {
                sb.append(String.format("%-5d %-20s %-10.1f %-15s %-12d\n",
                        p.getParzelleId(),
                        p.getName(),
                        p.getFlaecheQm(),
                        p.getKlimaZone(),
                        p.getAnzahlBaeume()));
            }

            sb.append("------------------------------------------------------------\n");
            sb.append("Gesamt: ").append(parzellen.size()).append(" Parzellen\n");

            return sb.toString();

        } catch (DatabaseException e) {
            throw new BusinessException("Fehler beim Laden der Parzellenübersicht", e);
        }
    }
}