package org.iba.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.iba.exception.SensorFehlerException;
import org.iba.logic.BewaesserungsRechner;
import org.iba.model.Baum;
import org.iba.model.Wetterdaten;
import org.iba.sensor.BodenfeuchteSensor;

import java.text.DecimalFormat;

/**
 * Hauptanwendung des intelligenten Bewässerungs-Assistenten (IBA-Olive).
 * Diese Klasse initialisiert die JavaFX-Benutzeroberfläche und bindet die
 * Geschäftslogik (Rechner und Sensor) ein.
 */
public class MainApplication extends Application {

    // Geschäftslogik und Modelle
    private final BewaesserungsRechner rechner = new BewaesserungsRechner();
    private final BodenfeuchteSensor sensor = new BodenfeuchteSensor();
    private final DecimalFormat df = new DecimalFormat("#0.00");

    // UI-Elemente
    private TextField txtName, txtAlter, txtBasisBedarf;
    private TextField txtTemperatur, txtNiederschlag;
    private Text txtErgebnis;
    private Text txtSensorStatus;

    /**
     * Startet die JavaFX-Anwendung.
     */
    @Override
    public void start(Stage primaryStage) {
        // Konfiguriere das Hauptfenster
        primaryStage.setTitle("IBA-Olive: Intelligenter Bewässerungs-Assistent");

        // Erstelle das Layout
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f7f9f7;");

        // Titel
        Label title = new Label("Intelligenter Bewaesserungs-Assistent (IBA)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#38761d")); // Dunkles Olivgrün

        // Eingabegrid für Olivenbaum- und Wetterdaten
        GridPane inputGrid = createInputGrid();

        // Ergebnis- und Statusanzeige
        txtErgebnis = new Text("Ergebnis: Werte eingeben und berechnen.");
        txtErgebnis.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        txtErgebnis.setFill(Color.web("#8fbc8f")); // Helles Olivgrün

        txtSensorStatus = new Text("Sensor-Status: Nicht getestet");
        txtSensorStatus.setFont(Font.font("Arial", 14));
        txtSensorStatus.setFill(Color.DARKGRAY);

        // Berechnungs-Button
        Button btnBerechnen = new Button("Wasserbedarf berechnen");
        btnBerechnen.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        btnBerechnen.setStyle("-fx-background-color: #6aa84f; -fx-text-fill: white; -fx-border-radius: 5; -fx-background-radius: 5;");
        btnBerechnen.setPrefWidth(250);
        btnBerechnen.setOnAction(e -> berechneBedarf());

        // Füge alle Komponenten zum Haupt-Layout hinzu
        root.getChildren().addAll(title, inputGrid, btnBerechnen, txtErgebnis, txtSensorStatus);

        // Zeige die Szene an
        Scene scene = new Scene(root, 500, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Erstellt das GridPane für die Eingabefelder.
     */
    private GridPane createInputGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-border-color: #d9ead3; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-color: #eaf1e7;");

        int row = 0;

        // Abschnitt Olivenbaum
        Label lblBaum = new Label("OLIVENBAUM-DATEN:");
        lblBaum.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        grid.add(lblBaum, 0, row++, 2, 1);

        txtName = addRow(grid, row++, "Parzelle Name:", "Parzelle A");
        txtAlter = addRow(grid, row++, "Alter (Jahre):", "5");
        txtBasisBedarf = addRow(grid, row++, "Basisbedarf (L/Tag):", "75.0");

        row++; // Abstand

        // Abschnitt Wetterdaten
        Label lblWetter = new Label("WETTERDATEN (Aktuell):");
        lblWetter.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        grid.add(lblWetter, 0, row++, 2, 1);

        txtTemperatur = addRow(grid, row++, "Temperatur (°C):", "28.0");
        txtNiederschlag = addRow(grid, row++, "Niederschlag (mm, 24h):", "5.0");

        return grid;
    }

    /**
     * Hilfsmethode zum Hinzufügen einer Label-TextField-Kombination.
     */
    private TextField addRow(GridPane grid, int row, String labelText, String placeholder) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setMaxWidth(150);

        grid.add(label, 0, row);
        grid.add(textField, 1, row);
        return textField;
    }

    /**
     * Verarbeitet die Benutzereingaben, führt die Berechnung durch
     * und behandelt mögliche Fehler (Validierung und Sensor).
     */
    private void berechneBedarf() {
        try {
            // 1. Daten aus der UI lesen und validieren
            // KORREKTUR: Der Baum-Konstruktor erfordert (parzelleId, alterJahre, pflanzenartId, basisBedarf).
            // Der Parzellenname (txtName.getText()) wurde entfernt und durch Platzhalter-IDs ersetzt.
            Baum baum = new Baum(
                    1, // Platzhalter: parzelleId (Muss in realer App dynamisch ermittelt werden)
                    Integer.parseInt(txtAlter.getText()),
                    1, // Platzhalter: pflanzenartId (z.B. Olivenbaum)
                    Double.parseDouble(txtBasisBedarf.getText())
            );

            Wetterdaten wetter = new Wetterdaten(
                    Double.parseDouble(txtTemperatur.getText()),
                    Double.parseDouble(txtNiederschlag.getText())
            );

            double endbedarf;
            double gemesseneFeuchte = 0.0;
            boolean sensorFehler = false;

            // 2. Sensor-Messwert abrufen (mit Fehler-Handling)
            try {
                gemesseneFeuchte = sensor.messWertLesen();
                // Wenn erfolgreich, mit Bodenfeuchte rechnen
                endbedarf = rechner.berechneWasserbedarf(baum, wetter, gemesseneFeuchte);
                txtSensorStatus.setText(String.format("Sensor-Status: OK. Feuchte: %.1f%%", gemesseneFeuchte));
                txtSensorStatus.setFill(Color.web("#38761d")); // Grün

            } catch (SensorFehlerException e) {
                // Wenn Sensorfehler auftritt, auf Fallback-Logik umschalten
                sensorFehler = true;
                endbedarf = rechner.berechneWasserbedarf(baum, wetter); // Ohne Bodenfeuchte
                txtSensorStatus.setText("Sensor-Status: FEHLER! Fallback-Modus aktiv.");
                txtSensorStatus.setFill(Color.web("#cc0000")); // Rot
                zeigeFehler(e.getMessage());
            }

            // 3. Ergebnis anzeigen
            String ergebnisText = String.format("Ergebnis: %s Liter/Tag empfohlen.", df.format(endbedarf));
            if (sensorFehler) {
                ergebnisText += " (Basis-Berechnung ohne Feuchte)";
            }
            txtErgebnis.setText(ergebnisText);
            txtErgebnis.setFill(Color.web("#38761d")); // Grün

        } catch (IllegalArgumentException e) {
            // Fängt alle ungültigen Argumente und Formatfehler ab
            txtErgebnis.setText("Ergebnis: Fehler bei der Eingabe.");
            txtErgebnis.setFill(Color.RED);
            txtSensorStatus.setText("Sensor-Status: Fehler in Eingabedaten.");
            txtSensorStatus.setFill(Color.RED);

            // Differenzierte Fehlermeldung
            String errorMessage;
            if (e instanceof NumberFormatException) {
                errorMessage = "Ungültiges Zahlenformat: Bitte stellen Sie sicher, dass alle numerischen Felder Zahlen enthalten (z.B. 75.0 oder 5).";
            } else {
                // Fängt die "echte" IllegalArgumentException ab (z.B. wenn Alter < 0)
                errorMessage = "Eingabefehler: " + e.getMessage();
            }
            zeigeFehler(errorMessage);
        }
    }

    /**
     * Zeigt eine JavaFX-Alert-Box für Fehlermeldungen an.
     * @param message Die anzuzeigende Fehlermeldung.
     */
    private void zeigeFehler(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler im Assistenten");
        alert.setHeaderText("Achtung: Problem aufgetreten");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Die Main-Methode, die die Anwendung startet.
     */
    public static void main(String[] args) {
        launch(args);
    }
}