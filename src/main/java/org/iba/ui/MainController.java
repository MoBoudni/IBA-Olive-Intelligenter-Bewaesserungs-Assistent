package org.iba.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.iba.exception.SensorFehlerException;
import org.iba.logic.BewaesserungsRechner;
import org.iba.model.Olivenbaum;
import org.iba.model.Wetterdaten;
import org.iba.sensor.BodenfeuchteSensor;
import org.iba.sensor.Sensor;

import java.text.DecimalFormat;

/**
 * Controller-Klasse für die Hauptansicht (MainView.fxml) des IBA-Olive Assistenten.
 * Diese Klasse enthält die gesamte Geschäftslogik zur Steuerung der Benutzeroberfläche
 * und zur Berechnung des Bewässerungsbedarfs.
 */
public class MainController {

    // FXML-Elemente, die über fx:id in MainView.fxml injiziert werden
    @FXML private TextField txtName;
    @FXML private TextField txtAlter;
    @FXML private TextField txtBasisBedarf;
    @FXML private TextField txtTemperatur;
    @FXML private TextField txtNiederschlag;
    @FXML private Label lblErgebnis;
    @FXML private Label lblSensorStatus;

    // Geschäftslogik und Modelle
    private final BewaesserungsRechner rechner = new BewaesserungsRechner();
    private final Sensor bodenfeuchteSensor = new BodenfeuchteSensor();
    private final DecimalFormat df = new DecimalFormat("#0.00");

    /**
     * Initialisierungsmethode (wird nach dem Laden des FXML-Dokuments aufgerufen).
     * Hier können Initialwerte oder Listener gesetzt werden.
     */
    @FXML
    public void initialize() {
        // Optionale Initialisierung: Kann hier weggelassen werden, da alle Elemente im FXML gesetzt sind.
    }

    /**
     * Die Aktionsmethode, die beim Klick auf den Button "Wasserbedarf berechnen" aufgerufen wird.
     * (Definiert im FXML mit onAction="#berechneBedarf")
     */
    @FXML
    private void berechneBedarf() {
        try {
            // 1. Daten aus der UI lesen und validieren
            Olivenbaum baum = new Olivenbaum(
                    txtName.getText(),
                    Integer.parseInt(txtAlter.getText()),
                    Double.parseDouble(txtBasisBedarf.getText())
            );

            Wetterdaten wetter = new Wetterdaten(
                    Double.parseDouble(txtTemperatur.getText()),
                    Double.parseDouble(txtNiederschlag.getText())
            );

            double endbedarf;
            double gemesseneFeuchte = -1.0;
            boolean sensorFehler = false;

            // 2. Sensor-Messwert abrufen (mit Fehler-Handling)
            try {
                // Lesen des Sensorwerts
                gemesseneFeuchte = bodenfeuchteSensor.messWertLesen();

                // Logik: Wenn erfolgreich, mit Bodenfeuchte rechnen
                endbedarf = rechner.berechneWasserbedarf(baum, wetter, gemesseneFeuchte);

                lblSensorStatus.setText(String.format("Sensor-Status: OK. Feuchte: %.1f%%", gemesseneFeuchte));
                lblSensorStatus.setTextFill(Color.web("#38761d")); // Grün

            } catch (SensorFehlerException e) {
                // Fallback: Wenn Sensorfehler auftritt, ohne Sensor rechnen
                sensorFehler = true;
                endbedarf = rechner.berechneWasserbedarf(baum, wetter); // Ohne Bodenfeuchte
                lblSensorStatus.setText("Sensor-Status: FEHLER! Fallback-Modus aktiv.");
                lblSensorStatus.setTextFill(Color.RED);
                zeigeFehler("Sensorfehler", "Die Verbindung zum Bodenfeuchtesensor ist fehlgeschlagen. Die Berechnung basiert nur auf Wetterdaten.");
            }

            // 3. Ergebnis anzeigen
            String ergebnisText = String.format("Empfehlung: %.2f Liter/Tag.", df.format(endbedarf));
            if (sensorFehler) {
                ergebnisText += " (Basis-Berechnung ohne Feuchte)";
            }
            lblErgebnis.setText(ergebnisText);
            lblErgebnis.setTextFill(Color.web("#38761d")); // Grün

        } catch (IllegalArgumentException e) {
            // Fängt Fehler bei ungültiger Zahleneingabe oder Domänenfehlern (z.B. negatives Alter)
            lblErgebnis.setText("Fehler: Ungültige Eingabedaten.");
            lblErgebnis.setTextFill(Color.RED);
            lblSensorStatus.setText("Eingabefehler.");
            lblSensorStatus.setTextFill(Color.RED);

            String errorMessage;
            if (e instanceof NumberFormatException) {
                errorMessage = "Bitte stellen Sie sicher, dass 'Alter', 'Basis-Bedarf', 'Temperatur' und 'Niederschlag' gültige Zahlen (z.B. 75.0) enthalten.";
            } else {
                errorMessage = e.getMessage(); // Zeigt die Validierungsmeldung des Olivenbaum-Modells
            }
            zeigeFehler("Eingabefehler", errorMessage);
        }
    }

    /**
     * Zeigt eine JavaFX-Alert-Box für Fehlermeldungen an.
     */
    private void zeigeFehler(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("IBA-Olive Fehler");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}