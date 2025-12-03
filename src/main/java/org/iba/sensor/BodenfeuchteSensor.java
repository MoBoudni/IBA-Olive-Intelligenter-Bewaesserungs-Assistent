package org.iba.sensor;

import org.iba.exception.SensorFehlerException;

import java.util.Random;

/**
 * Implementierung eines realistischeren Bodenfeuchtesensors.
 * Simuliert das Verhalten einer externen Schnittstelle (z.B. IoT-API),
 * indem eine Netzwerk-Verzögerung hinzugefügt wird und spezifischere
 * Fehlerfälle behandelt werden.
 */
public class BodenfeuchteSensor implements Sensor {

    private static final Random RANDOM = new Random();
    private static final int SIMULIERTE_LATENZ_MS = 500; // Simuliert halbe Sekunde Netzwerkverzögerung
    private static final double REALISTISCHE_FEHLERQUOTE = 0.15; // 15% Wahrscheinlichkeit für einen Fehler

    /**
     * Simuliert das Lesen eines Bodenfeuchtemesswerts (10% bis 90%)
     * durch eine externe API-Abfrage.
     *
     * @return Simulierte Bodenfeuchte als double.
     * @throws SensorFehlerException Wird geworfen, wenn eine Zeitüberschreitung,
     * ein Verbindungsfehler oder ein ungültiger Wert auftritt.
     */
    @Override
    public double messWertLesen() throws SensorFehlerException {
        // [1] Netzwerk-Latenz simulieren
        try {
            System.out.println("[Sensor] Simuliere externe Datenabfrage...");
            Thread.sleep(SIMULIERTE_LATENZ_MS);
        } catch (InterruptedException e) {
            // Wird selten geworfen, behandelt aber den Fall, wenn der Thread unterbrochen wird.
            Thread.currentThread().interrupt();
            throw new SensorFehlerException("Simulierter API-Aufruf unterbrochen.", e);
        }

        // [2] Realistische Fehlerbedingungen simulieren (15% der Fälle)
        if (RANDOM.nextDouble() < REALISTISCHE_FEHLERQUOTE) {

            // Simuliere verschiedene Arten von Fehlern, um die Robustheit zu testen
            int fehlerTyp = RANDOM.nextInt(3);

            if (fehlerTyp == 0) {
                // Typ 1: Zeitüberschreitung (Timeout)
                throw new SensorFehlerException("API-Verbindung: Zeitüberschreitung (Timeout).");
            } else if (fehlerTyp == 1) {
                // Typ 2: Sensor liefert ungültigen Wert
                throw new SensorFehlerException("Sensor-Messwert ungültig (Wert außerhalb des Bereichs 0-100).");
            } else {
                // Typ 3: Generischer Verbindungsfehler
                throw new SensorFehlerException("Generischer Verbindungsfehler zur Sensor-API.");
            }
        }

        // [3] Erfolgreicher Messwert (zwischen 10% und 90%)
        // Realistische Feuchtigkeitswerte für Olivenbäume
        double feuchte = 10.0 + (80.0 - 10.0) * RANDOM.nextDouble();

        System.out.printf("[Sensor] Daten erfolgreich abgerufen (%.1f%%).\n", feuchte);
        return feuchte;
    }
}