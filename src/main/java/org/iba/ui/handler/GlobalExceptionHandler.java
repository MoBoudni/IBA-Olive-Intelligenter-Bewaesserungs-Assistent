package org.iba.ui.handler;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.iba.exception.ErrorCode;
import org.iba.exception.IbaException;

import java.util.Optional;

/**
 * Globaler Exception-Handler für JavaFX-Anwendungen.
 * Fängt unerwartete Exceptions ab und zeigt sie benutzerfreundlich an.
 */
public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static GlobalExceptionHandler instance;

    private GlobalExceptionHandler() {
        // Privater Konstruktor für Singleton
    }

    public static GlobalExceptionHandler getInstance() {
        if (instance == null) {
            instance = new GlobalExceptionHandler();
        }
        return instance;
    }

    /**
     * Installiert den globalen Exception-Handler für die Anwendung.
     */
    public void install() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // Logge den Fehler (hier einfache Ausgabe, später mit Logging-Framework)
        System.err.println("Uncaught exception in thread: " + thread.getName());
        throwable.printStackTrace();

        // Zeige Fehlerdialog im JavaFX Application Thread
        Platform.runLater(() -> showErrorDialog(throwable));
    }

    /**
     * Zeigt einen benutzerfreundlichen Fehlerdialog an.
     */
    private void showErrorDialog(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler im Bewässerungsassistenten");
        alert.setHeaderText("Ein unerwarteter Fehler ist aufgetreten");

        String contentText;
        if (throwable instanceof IbaException) {
            IbaException ibaException = (IbaException) throwable;
            contentText = String.format("Fehlercode: %s\n%s\n\nTechnische Details: %s",
                    ibaException.getErrorCode().getCode(),
                    ibaException.getMessage(),
                    ibaException.getCause() != null ?
                            ibaException.getCause().getMessage() : "Keine weiteren Details");
        } else {
            contentText = String.format("Fehler: %s\n\nTechnische Details: %s",
                    throwable.getMessage(),
                    throwable.getCause() != null ?
                            throwable.getCause().getMessage() : "Keine weiteren Details");
        }

        alert.setContentText(contentText);

        // Füge Buttons hinzu
        alert.getButtonTypes().setAll(
                ButtonType.OK,
                new ButtonType("Details anzeigen"),
                new ButtonType("Programm beenden")
        );

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get().getText().equals("Details anzeigen")) {
                showDetailedErrorDialog(throwable);
            } else if (result.get().getText().equals("Programm beenden")) {
                Platform.exit();
                System.exit(1);
            }
        }
    }

    /**
     * Zeigt detaillierten Fehlerdialog mit Stacktrace.
     */
    private void showDetailedErrorDialog(Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Technische Fehlerdetails");
        alert.setHeaderText("Stacktrace:");

        // Erzeuge formatierte Stacktrace-Ausgabe
        StringBuilder stackTrace = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.append(element.toString()).append("\n");
        }

        alert.setContentText(stackTrace.toString());
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
    }

    /**
     * Behandelt bekannte Exceptions mit spezifischen Dialogen.
     */
    public void handleKnownException(IbaException exception) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Anwendungsfehler");

            switch (exception.getErrorCode()) {
                case VALIDATION_ERROR:
                    alert.setHeaderText("Eingabefehler");
                    alert.setContentText("Bitte überprüfen Sie Ihre Eingaben:\n" + exception.getMessage());
                    break;

                case SENSOR_ERROR:
                    alert.setHeaderText("Sensorfehler");
                    alert.setContentText("Der Sensor konnte nicht ausgelesen werden.\n" +
                            "Bitte prüfen Sie die Verbindung.\n\n" +
                            "Das System verwendet Fallback-Werte.");
                    break;

                case DATABASE_ERROR:
                    alert.setHeaderText("Datenbankfehler");
                    alert.setContentText("Die Datenbank ist vorübergehend nicht erreichbar.\n" +
                            "Bitte versuchen Sie es später erneut.");
                    break;

                case BUSINESS_ERROR:
                    alert.setHeaderText("Geschäftsregel-Verletzung");
                    alert.setContentText(exception.getMessage());
                    break;

                default:
                    alert.setHeaderText("Fehler");
                    alert.setContentText(exception.getMessage());
            }

            alert.showAndWait();
        });
    }
}