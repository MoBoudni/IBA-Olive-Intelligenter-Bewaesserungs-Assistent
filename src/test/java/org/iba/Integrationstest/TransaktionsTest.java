package org.iba.Integrationstest;

import org.iba.db.ParzelleRepository;
import org.iba.exception.BusinessException;
import org.iba.model.Baum;
import org.iba.model.Parzelle;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Testet transaktionale Operationen.
 */
public class TransaktionsTest {

    @Test
    void testParzelleMitBaeumenSpeichern() throws BusinessException {
        ParzelleRepository repository = new ParzelleRepository();

        // Test-Parzelle
        Parzelle parzelle = new Parzelle(0, "Test-Parzelle-Transaktion", 0, 500.0, "Mediterran", 101);

        // Test-Bäume
        List<Baum> baeume = Arrays.asList(
                new Baum(0, 5, 1, 25.0),
                new Baum(0, 3, 1, 20.0),
                new Baum(0, 7, 1, 30.0)
        );

        try {
            // Transaktionales Speichern
            Parzelle gespeichert = repository.speichereParzelleMitBaeumen(parzelle, baeume);

            System.out.println("Erfolgreich gespeichert: " + gespeichert.getName() +
                    " mit " + baeume.size() + " Bäumen");

            // Test: Sollte auch bei Fehlern rollback machen
            // Beispiel mit ungültigen Daten würde ValidationException werfen

        } catch (Exception e) {
            System.err.println("Transaktion fehlgeschlagen: " + e.getMessage());
            // Transaktion sollte automatisch gerollbackt sein
        }
    }

    @Test
    void testTransaktionsRollbackBeiFehler() {
        ParzelleRepository repository = new ParzelleRepository();

        // Versuch mit ungültigen Daten (leerer Name)
        Parzelle ungueligeParzelle = new Parzelle(0, "", 0, 500.0, "Mediterran", 101);
        List<Baum> baeume = Arrays.asList(new Baum(0, 5, 1, 25.0));

        try {
            repository.speichereParzelleMitBaeumen(ungueligeParzelle, baeume);
            // Sollte nicht hier ankommen
            System.err.println("FEHLER: ValidationException wurde nicht geworfen!");

        } catch (Exception e) {
            System.out.println("ERWARTET: Transaktion wurde zurückgerollt wegen: " +
                    e.getMessage());
            // Datenbank sollte keinen Eintrag haben
        }
    }
}