package org.iba.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.iba.logic.BewaesserungsRechner;
import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;
import org.iba.sensor.BodenfeuchteSensor;
import org.iba.exception.SensorFehlerException;

/**
 * Die Hauptklasse für die JavaFX-basierte grafische Benutzeroberfläche (GUI) des IBA-Olive Assistenten.
 * Sie ersetzt die reine Konsolenanwendung und dient zur visuellen Eingabe und Ausgabe.
 */
public class MainApplication extends Application {

    // Geschäftslogik und Sensoren werden instanziiert
    private final BewaesserungsRechner rechner = new BewaesserungsRechner();
    private final BodenfeuchteSensor sensor = new BodenfeuchteSensor();

    // GUI Komponenten für Eingabe und Ausgabe
    private TextField nameField = new TextField("Parzelle A");
    private TextField alterField = new TextField("8");
    private TextField basisBedarfField = new TextField("40.0");
    private TextField tempField = new TextField("28.5");
    private TextField niederschlagField = new TextField("5.0");
    private Label ergebnisLabel = new Label("Bitte Daten eingeben und berechnen.");
    private Label sensorStatusLabel = new Label("Sensor: Wird bei Berechnung gelesen.");

    /**
     * Startet die JavaFX-Anwendung und erstellt die primäre Benutzeroberfläche.
     * * @param stage Die primäre Bühne (Fenster) für diese Anwendung.
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("IBA-Olive - Bewässerungs-Assistent");

        // Haupt-Layout Container
        VBox root = new VBox(20); // Abstand 20px
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-font-family: 'Inter', sans-serif;");

        // Titel
        Label title = new Label("Intelligenter Bewässerungs-Assistent");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #38a169;");

        // Eingabe-Grid
        GridPane inputGrid = createInputGrid();

        // Button
        Button calculateButton = new Button("Bewässerungsbedarf berechnen");
        calculateButton.setStyle("-fx-background-color: #38a169; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10 20; -fx-cursor: hand;");
        calculateButton.setOnAction(e -> calculateWaterNeed());

        // Ergebnis- und Status-Container
        VBox resultBox = new VBox(5);
        resultBox.setPadding(new Insets(10));
        resultBox.setStyle("-fx-border-color: #d1d5db; -fx-border-radius: 8px; -fx-background-color: #f9fafb;");

        ergebnisLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        sensorStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        resultBox.getChildren().addAll(ergebnisLabel, sensorStatusLabel);

        root.getChildren().addAll(title, inputGrid, calculateButton, resultBox);

        Scene scene = new Scene(root, 600, 650);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Erstellt das Gitter-Layout für alle Eingabefelder.
     * @return Ein konfiguriertes GridPane.
     */
    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-border-color: #10b981; -fx-border-radius: 8px; -fx-padding: 15;");

        int row = 0;
        grid.add(new Label("--- Parzellen-Daten ---"), 0, row++);
        grid.add(new Label("Name der Parzelle:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Alter (Jahre):"), 0, row);
        grid.add(alterField, 1, row++);
        grid.add(new Label("Basis-Bedarf (L/Tag):"), 0, row);
        grid.add(basisBedarfField, 1, row++);

        grid.add(new Label("--- Wetterdaten ---"), 0, row++);
        grid.add(new Label("Temperatur (°C):"), 0, row);
        grid.add(tempField, 1, row++);
        grid.add(new Label("Niederschlag (mm):"), 0, row);
        grid.add(niederschlagField, 1, row++);

        return grid;
    }

    /**
     * Event Handler, der bei Klick auf den Button aufgerufen wird.
     * Führt die Berechnung durch und aktualisiert die Labels.
     */
    private void calculateWaterNeed() {
        try {
            // 1. Daten aus GUI lesen und validieren
            String name = nameField.getText();
            int alter = Integer.parseInt(alterField.getText());
            double basisBedarf = Double.parseDouble(basisBedarfField.getText());
            double temperatur = Double.parseDouble(tempField.getText());
            double niederschlag = Double.parseDouble(niederschlagField.getText());

            // 2. Erstellung der Modell-Objekte (wirft IllegalArgumentException bei Fehlern)
            Olivenbaum baum = new Olivenbaum(name, alter, basisBedarf);
            Wetterdaten wetter = new Wetterdaten(temperatur, niederschlag);

            // 3. Sensor lesen (mit Exception Handling)
            double bodenfeuchte = -1.0;
            boolean sensorErfolg = false;

            try {
                bodenfeuchte = sensor.messWertLesen();
                sensorStatusLabel.setText(String.format("Sensor-Status: OK (Bodenfeuchte: %.1f%%)", bodenfeuchte));
                sensorStatusLabel.setStyle("-fx-text-fill: #10b981;");
                sensorErfolg = true;
            } catch (SensorFehlerException e) {
                sensorStatusLabel.setText("Sensor-FEHLER: " + e.getMessage());
                sensorStatusLabel.setStyle("-fx-text-fill: #ef4444;");
                sensorErfolg = false;
            }

            // 4. Berechnung
            double bedarf;
            if (sensorErfolg) {
                bedarf = rechner.berechneWasserbedarf(baum, wetter, bodenfeuchte);
            } else {
                // Fallback-Berechnung
                bedarf = rechner.berechneWasserbedarf(baum, wetter);
            }

            // 5. Ergebnis ausgeben
            if (bedarf > 0) {
                ergebnisLabel.setText(String.format("Empfehlung: %.2f Liter", bedarf));
                ergebnisLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981;");
            } else {
                ergebnisLabel.setText("Empfehlung: KEINE Bewässerung notwendig.");
                ergebnisLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
            }

        } catch (NumberFormatException e) {
            // Fängt Fehler ab, wenn Text in Zahl konvertiert werden soll
            ergebnisLabel.setText("FEHLER: Bitte nur gültige Zahlen eingeben.");
            ergebnisLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            sensorStatusLabel.setText("Überprüfen Sie alle Eingabefelder.");

        } catch (IllegalArgumentException e) {
            // Fängt Validierungsfehler aus den Modellklassen ab (z.B. negatives Alter)
            ergebnisLabel.setText("FEHLER: Ungültige Daten: " + e.getMessage());
            ergebnisLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            sensorStatusLabel.setText("Korrigieren Sie die Parzellen- oder Wetterdaten.");

        } catch (Exception e) {
            // Catch-All für unerwartete Fehler
            ergebnisLabel.setText("Unerwarteter Fehler: " + e.getClass().getSimpleName());
            ergebnisLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
        }
    }

    /**
     * Startmethode, die von der JVM aufgerufen wird.
     * Ersetzt die alte Main-Methode in der Main-Klasse.
     * * @param args Kommandozeilenargumente.
     */
    public static void main(String[] args) {
        launch(args);
    }
}