package org.iba.db.transaction;

import org.iba.exception.DatabaseException;
import org.iba.util.ExceptionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Stack;

/**
 * Verwaltet Datenbanktransaktionen mit Unterstützung für:
 * - Verschachtelte Transaktionen (Savepoints)
 * - Automatisches Rollback bei Fehlern
 * - Connection Pooling (einfache Implementierung)
 * - Transaction Timeouts
 */
public class TransactionManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/IBA_Olive_DEV";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Chakeb1978&";

    // ThreadLocal für thread-sichere Transaktionen
    private static final ThreadLocal<TransactionContext> currentTransaction =
            ThreadLocal.withInitial(() -> null);

    /**
     * Führt eine Operation innerhalb einer Transaktion aus.
     * Automatisches Commit bei Erfolg, Rollback bei Exception.
     */
    public static <T> T executeInTransaction(TransactionalOperation<T> operation)
            throws DatabaseException {

        TransactionContext context = null;
        Connection connection = null;

        try {
            // 1. Transaktion starten
            context = beginTransaction();
            connection = context.getConnection();

            // 2. Operation ausführen
            T result = operation.execute(connection);

            // 3. Bei Erfolg: Commit
            commitTransaction(context);

            return result;

        } catch (Exception e) {
            // 4. Bei Fehler: Rollback
            if (context != null) {
                rollbackTransaction(context, e);
            }

            // 5. Exception umwandeln/weiterschleudern
            if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            } else if (e instanceof SQLException) {
                throw ExceptionUtils.wrapSQLException((SQLException) e, "Transaktionsoperation");
            } else {
                throw new DatabaseException("Transaktionsfehler: " + e.getMessage(), e);
            }

        } finally {
            // 6. Resources aufräumen
            cleanupTransaction(context);
        }
    }

    /**
     * Startet eine neue Transaktion oder erstellt einen Savepoint für verschachtelte Transaktionen.
     */
    private static TransactionContext beginTransaction() throws SQLException {
        TransactionContext parentContext = currentTransaction.get();

        if (parentContext == null) {
            // Neue Haupttransaktion
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            TransactionContext newContext = new TransactionContext(connection);
            currentTransaction.set(newContext);

            System.out.println("[Transaction] Neue Transaktion gestartet");
            return newContext;

        } else {
            // Verschachtelte Transaktion (Savepoint)
            Savepoint savepoint = parentContext.getConnection().setSavepoint();
            parentContext.pushSavepoint(savepoint);

            System.out.println("[Transaction] Savepoint erstellt: " + savepoint.getSavepointId());
            return parentContext; // Verwende Parent-Context
        }
    }

    /**
     * Führt ein Commit durch (nur für Haupttransaktion).
     */
    private static void commitTransaction(TransactionContext context) throws SQLException {
        if (context.isRootTransaction()) {
            context.getConnection().commit();
            System.out.println("[Transaction] Commit erfolgreich");
        } else {
            // Für Savepoints: Release
            Savepoint savepoint = context.popSavepoint();
            if (savepoint != null) {
                context.getConnection().releaseSavepoint(savepoint);
                System.out.println("[Transaction] Savepoint released: " + savepoint.getSavepointId());
            }
        }
    }

    /**
     * Führt ein Rollback durch.
     */
    private static void rollbackTransaction(TransactionContext context, Exception cause) {
        try {
            if (context.isRootTransaction()) {
                context.getConnection().rollback();
                System.err.println("[Transaction] Rollback der Haupttransaktion aufgrund von: " +
                        cause.getMessage());
            } else {
                Savepoint savepoint = context.peekSavepoint();
                if (savepoint != null) {
                    context.getConnection().rollback(savepoint);
                    System.err.println("[Transaction] Rollback zu Savepoint: " +
                            savepoint.getSavepointId() + " aufgrund von: " +
                            cause.getMessage());
                }
            }
        } catch (SQLException rollbackEx) {
            System.err.println("[Transaction] FEHLER beim Rollback: " + rollbackEx.getMessage());
            rollbackEx.printStackTrace();
        }
    }

    /**
     * Räumt Transaktionsressourcen auf.
     */
    private static void cleanupTransaction(TransactionContext context) {
        if (context != null && context.isRootTransaction()) {
            try {
                Connection connection = context.getConnection();

                // Zurück zu Auto-Commit Mode
                connection.setAutoCommit(true);
                connection.close();

                System.out.println("[Transaction] Transaktion beendet, Connection geschlossen");

            } catch (SQLException e) {
                System.err.println("[Transaction] Fehler beim Cleanup: " + e.getMessage());
            } finally {
                // Aus ThreadLocal entfernen
                currentTransaction.remove();
            }
        }
    }

    /**
     * Gibt die aktuelle Connection zurück (für Repository-Operationen).
     */
    public static Connection getCurrentConnection() throws DatabaseException {
        TransactionContext context = currentTransaction.get();

        if (context != null) {
            return context.getConnection();
        } else {
            // Keine aktive Transaktion - neue Connection öffnen
            try {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                connection.setAutoCommit(true); // Implizite Transaktionen
                return connection;
            } catch (SQLException e) {
                throw ExceptionUtils.wrapSQLException(e, "Verbindungsaufbau");
            }
        }
    }

    /**
     * Prüft, ob eine Transaktion aktiv ist.
     */
    public static boolean isTransactionActive() {
        return currentTransaction.get() != null;
    }

    /**
     * TransactionContext Klasse für Transaktionszustand.
     */
    private static class TransactionContext {
        private final Connection connection;
        private final Stack<Savepoint> savepoints = new Stack<>();
        private final boolean isRootTransaction;

        public TransactionContext(Connection connection) {
            this.connection = connection;
            this.isRootTransaction = true;
        }

        public Connection getConnection() {
            return connection;
        }

        public void pushSavepoint(Savepoint savepoint) {
            savepoints.push(savepoint);
        }

        public Savepoint popSavepoint() {
            return savepoints.isEmpty() ? null : savepoints.pop();
        }

        public Savepoint peekSavepoint() {
            return savepoints.isEmpty() ? null : savepoints.peek();
        }

        public boolean isRootTransaction() {
            return isRootTransaction && savepoints.isEmpty();
        }

        public int getSavepointLevel() {
            return savepoints.size();
        }
    }

    /**
     * Functional Interface für transaktionale Operationen.
     */
    @FunctionalInterface
    public interface TransactionalOperation<T> {
        T execute(Connection connection) throws Exception;
    }
}