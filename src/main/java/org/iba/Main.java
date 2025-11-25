package org.iba;

import org.iba.logic.BewaesserungsRechner;
import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;
import org.iba.sensor.BodenfeuchteSensor;
import org.iba.sensor.Sensor;
import org.iba.exception.SensorFehlerException;

import java.util.Scanner;

/**
 * Die Hauptklasse der Anwendung "Intelligenter Bewaesserungs-Assistent" (IBA-Olive).
 * Diese Klasse dient als Einstiegspunkt (Entry Point) und steuert den gesamten
 * Workflow der Konsolenanwendung. Sie koordiniert die Benutzereingaben,
 * die Erfassung von Sensordaten und die Ausgabe der Bewässerungsempfehlung.
 */
public class Main {

    /**
     * Die Main-Methode startet die Anwendung.
     * Der Ablauf ist wie folgt definiert:
     * Initialisierung der Komponenten (Rechner, Sensor, Scanner).
     * Erfassung der Parzellendaten (mit Validierung).
     * Erfassung der Wetterdaten.
     * Versuch, den Bodenfeuchtesensor auszulesen (inkl. Fehlertoleranz).
     * Berechnung des Wasserbedarfs basierend auf verfügbaren Daten.
     * Ausgabe der Empfehlung an den Nutzer.
     *
     * @param args Kommandozeilenargumente (werden in dieser Version nicht verwendet).
     */
    public static void main(String[] args) {
        // [1] Initialisierung der Systemkomponenten
        Scanner scanner = new Scanner(System.in);
        BewaesserungsRechner rechner = new BewaesserungsRechner();

        // Sensor wird instanziiert, aber der Zugriff erfolgt erst später im geschützten Block
        Sensor bodenfeuchteSensor = new BodenfeuchteSensor();

        System.out.println("--- Intelligenter Bewaesserungs-Assistent (IBA-Olive) ---");

        try {
            // [2] Olivenbaum-Daten erfassen
            // Wir befinden uns im try-Block, um Validierungsfehler (IllegalArgumentException)
            // aus dem Olivenbaum-Konstruktor abzufangen.
            System.out.println("\n[1] Daten der Oliven-Parzelle eingeben:");

            System.out.print("Name der Parzelle: ");
            String name = scanner.nextLine();

            System.out.print("Alter (Jahre, z.B. 8): ");
            int alter = leseZahl(scanner);

            System.out.print("Basis-Bedarf (L/Tag, z.B. 40): ");
            double basisBedarf = leseDezimalZahl(scanner);

            // Erstellung des Objekts - hier greift die Validierung der Klasse Olivenbaum
            Olivenbaum baum = new Olivenbaum(name, alter, basisBedarf);
            System.out.println("-> Parzelle erfolgreich angelegt: " + baum.getName());


            // [3] Wetterdaten erfassen
            System.out.println("\n[2] Aktuelle Wetterdaten eingeben:");

            System.out.print("Temperatur (°C, z.B. 28.5): ");
            double temperatur = leseDezimalZahl(scanner);

            System.out.print("Niederschlag (mm, z.B. 5.0): ");
            double niederschlag = leseDezimalZahl(scanner);

            Wetterdaten wetter = new Wetterdaten(temperatur, niederschlag);
            System.out.println("-> Wetterdaten erfasst.");


            // [4] Sensor lesen (Kritischer Pfad mit spezifischem Exception Handling)
            double bodenfeuchte = -1.0; // Initialwert (Signal für "keine Daten")
            boolean sensorErfolg = false; // Flag zur Steuerung der Berechnungslogik

            try {
                System.out.print("\n[SYSTEM] Verbinde mit Bodenfeuchtesensor... ");
                // Versuch, den Sensor zu lesen. Dies kann eine SensorFehlerException werfen.
                bodenfeuchte = bodenfeuchteSensor.messWertLesen();

                System.out.printf("OK (Messwert: %.1f%%)\n", bodenfeuchte);
                sensorErfolg = true;

            } catch (SensorFehlerException e) {
                // Graceful Degradation: Wir fangen den Fehler ab und arbeiten ohne Sensor weiter.
                System.err.println("\n[FEHLER] Sensor-Verbindung fehlgeschlagen: " + e.getMessage());
                System.out.println("[FALLBACK] System wechselt in den manuellen Modus (Wetterdaten-basiert).");
                sensorErfolg = false;
            }


            // [5] Berechnung durchführen (Logikweiche basierend auf Sensor-Verfügbarkeit)
            double bedarf;

            if (sensorErfolg) {
                // MVP+ Logik: Berechnung unter Einbeziehung der Bodenfeuchte
                bedarf = rechner.berechneWasserbedarf(baum, wetter, bodenfeuchte);
            } else {
                // Fallback / MVP Logik: Berechnung rein basierend auf Wetterdaten
                // Wir nutzen die überladene Methode, die keinen Sensorwert erwartet.
                bedarf = rechner.berechneWasserbedarf(baum, wetter);
            }


            // [6] Ausgabe der Empfehlung
            System.out.println("\n==============================================");
            System.out.printf("Bewässerungsempfehlung für Parzelle '%s':%n", baum.getName());

            if (bedarf > 0) {
                System.out.printf("Empfohlene Bewässerungsmenge heute: %.2f Liter.%n", bedarf);
                System.out.println("Grund: Hohe Temperatur, geringer Niederschlag und/oder trockener Boden.");
            } else {
                System.out.println("KEINE Bewässerung notwendig.");

                // Detaillierte Begründung abhängig vom Sensor-Status
                if (sensorErfolg && bodenfeuchte > 80.0) {
                    System.out.printf("Grund: Bodenfeuchte ist ausreichend hoch (%.1f%%).\n", bodenfeuchte);
                } else {
                    System.out.println("Grund: Ausreichender natürlicher Niederschlag oder niedrige Verdunstung.");
                }
            }
            System.out.println("==============================================");

        } catch (IllegalArgumentException e) {
            // Fängt fachliche Fehler ab (z.B. negatives Alter, leerer Name)
            System.err.println("\n[ABBRUCH] Ungültige Eingabedaten erkannt:");
            System.err.println(">> " + e.getMessage());
            System.out.println("Bitte starten Sie das Programm neu und prüfen Sie Ihre Eingaben.");

        } catch (Exception e) {
            // Catch-All für unerwartete technische Fehler (z.B. NullPointer)
            System.err.println("\n[FATAL] Ein unerwarteter Systemfehler ist aufgetreten: " + e.getMessage());
            e.printStackTrace(); // Für Debugging-Zwecke hilfreich

        } finally {
            // Ressource freigeben: Scanner schließen
            if (scanner != null) {
                scanner.close();
            }
            System.out.println("\n[SYSTEM] Programm beendet.");
        }
    }

    // --- Hilfsmethoden für robuste Eingabe ---

    /**
     * Liest eine ganze Zahl (Integer) von der Konsole ein und behandelt
     * Fehleingaben (z.B. Buchstaben), bis eine gültige Zahl eingegeben wird.
     *
     * @param scanner Der aktive Scanner für die Eingabe.
     * @return Eine gültige ganze Zahl.
     */
    private static int leseZahl(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Ungültige Eingabe. Bitte ganze Zahl eingeben: ");
            scanner.next(); // Ungültigen Token aus dem Puffer entfernen
        }
        int zahl = scanner.nextInt();
        scanner.nextLine(); // Zeilenumbruch konsumieren
        return zahl;
    }

    /**
     * Liest eine Dezimalzahl (Double) von der Konsole ein und behandelt
     * Fehleingaben, bis eine gültige Zahl eingegeben wird.
     *
     * @param scanner Der aktive Scanner für die Eingabe.
     * @return Eine gültige Dezimalzahl.
     */
    private static double leseDezimalZahl(Scanner scanner) {
        while (!scanner.hasNextDouble()) {
            System.out.print("Ungültige Eingabe. Bitte Dezimalzahl (z.B. 25.5) eingeben: ");
            scanner.next(); // Ungültigen Token aus dem Puffer entfernen
        }
        double zahl = scanner.nextDouble();
        scanner.nextLine(); // Zeilenumbruch konsumieren
        return zahl;
    }
}