package org.iba;

import org.iba.logic.BewaesserungsRechner;
import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;

import java.util.Scanner;

/**
 * Einstiegspunkt der Anwendung und Steuerung des Konsolen-Workflows.
 * Liest Benutzereingaben und gibt die Bewässerungsempfehlung aus.
 */
public class Main {

    public static void main(String[] args) {
        // [1] Initialisierung
        Scanner scanner = new Scanner(System.in);
        BewaesserungsRechner rechner = new BewaesserungsRechner();

        System.out.println("--- Intelligenter Bewässerungs-Assistent (IBA-Olive) ---");

        // [2] Olivenbaum-Daten festlegen (Beispiel-Parzelle)
        System.out.println("\n[1] Daten der Oliven-Parzelle eingeben:");

        System.out.print("Name der Parzelle: ");
        String name = scanner.nextLine();

        System.out.print("Alter (Jahre, z.B. 8): ");
        int alter = leseZahl(scanner); // Alter wird für f_Alter benötigt [cite: 24]

        System.out.print("Basis-Bedarf (L/Tag, z.B. 40): ");
        double basisBedarf = leseDezimalZahl(scanner); // Basisbedarf B_basis [cite: 24]

        Olivenbaum baum = new Olivenbaum(name, alter, basisBedarf);
        System.out.println("-> Erfasste Parzelle: " + baum);


        // [3] Wetterdaten erfassen
        System.out.println("\n[2] Aktuelle Wetterdaten eingeben:");

        System.out.print("Temperatur (°C, z.B. 28.5): ");
        double temperatur = leseDezimalZahl(scanner); // Temperatur für f_Temp [cite: 24]

        System.out.print("Niederschlag (mm, z.B. 5.0): ");
        double niederschlag = leseDezimalZahl(scanner); // Niederschlag für E_Niederschlag [cite: 24]

        Wetterdaten wetter = new Wetterdaten(temperatur, niederschlag);
        System.out.println("-> Erfasste Daten: " + wetter);


        // [4] Berechnung durchführen
        double bedarf = rechner.berechneWasserbedarf(baum, wetter);

        // [5] Ausgabe der Empfehlung
        System.out.println("\n==============================================");
        System.out.printf("Bewässerungsempfehlung für Parzelle '%s':%n", baum.getName());

        if (bedarf > 0) {
            System.out.printf("Empfohlene Bewässerungsmenge heute: %.2f Liter.%n", bedarf);
            System.out.println("Grund: Hohe Temperatur und/oder geringer Niederschlag.");
        } else {
            System.out.println("KEINE Bewässerung notwendig.");
            System.out.println("Grund: Ausreichender Niederschlag oder niedrige Verdunstung/Temperatur.");
        }
        System.out.println("==============================================");

        scanner.close();
    }

    // Hilfsmethoden zur robusten Eingabe (einfache Fehlerbehandlung)
    private static int leseZahl(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Ungültige Eingabe. Bitte ganze Zahl eingeben: ");
            scanner.next(); // Ungültigen Token verwerfen
        }
        int zahl = scanner.nextInt();
        scanner.nextLine(); // Newline verwerfen
        return zahl;
    }

    private static double leseDezimalZahl(Scanner scanner) {
        while (!scanner.hasNextDouble()) {
            System.out.print("Ungültige Eingabe. Bitte Dezimalzahl (z.B. 25.5) eingeben: ");
            scanner.next(); // Ungültigen Token verwerfen
        }
        double zahl = scanner.nextDouble();
        scanner.nextLine(); // Newline verwerfen
        return zahl;
    }
}
