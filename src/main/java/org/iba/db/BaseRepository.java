package org.iba.db;

import org.iba.db.transaction.TransactionManager;
import org.iba.exception.DatabaseException;
import org.iba.util.ExceptionUtils;

import java.sql.*;

/**
 * Basis-Repository-Klasse die DBConnector verwendet.
 */
public abstract class BaseRepository {

    /**
     * Öffnet eine Datenbankverbindung mit Exception-Handling.
     * Nutzt DBConnector für zentrale Konfiguration.
     */
    protected Connection getConnection() throws DatabaseException {
        // Wenn eine Transaktion aktiv ist, deren Connection verwenden
        if (TransactionManager.isTransactionActive()) {
            return TransactionManager.getCurrentConnection();
        }

        // Sonst neue Connection über DBConnector
        return DBConnector.getConnection();
    }

    /**
     * Führt ein SQL-Statement aus mit automatischem Resource-Management.
     */
    protected <T> T executeQuery(String sql, ResultSetHandler<T> handler, Object... params)
            throws DatabaseException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);

            // Setze Parameter
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            rs = stmt.executeQuery();
            return handler.handle(rs);

        } catch (SQLException e) {
            throw ExceptionUtils.wrapSQLException(e, "Abfrage ausführen");
        } finally {
            // WICHTIG: Nur schließen wenn keine Transaktion aktiv
            if (!TransactionManager.isTransactionActive()) {
                closeResources(rs, stmt, conn);
            } else {
                // In Transaktion: Nur ResultSet und Statement schließen
                closeStatementAndResultSet(rs, stmt);
            }
        }
    }

    /**
     * Führt ein Update-Statement aus.
     */
    protected int executeUpdate(String sql, Object... params) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw ExceptionUtils.wrapSQLException(e, "Update ausführen");
        } finally {
            if (!TransactionManager.isTransactionActive()) {
                closeResources(null, stmt, conn);
            } else {
                closeStatementAndResultSet(null, stmt);
            }
        }
    }

    /**
     * Schließt alle JDBC-Ressourcen sicher.
     */
    protected void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        closeStatementAndResultSet(rs, stmt);
        closeConnection(conn);
    }

    /**
     * Schließt nur Statement und ResultSet.
     */
    private void closeStatementAndResultSet(ResultSet rs, Statement stmt) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen des ResultSet: " + e.getMessage());
        }

        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen des Statement: " + e.getMessage());
        }
    }

    /**
     * Schließt eine Connection.
     */
    private void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            System.err.println("Fehler beim Schließen der Connection: " + e.getMessage());
        }
    }

    /**
     * Functional Interface für ResultSet-Verarbeitung.
     */
    @FunctionalInterface
    protected interface ResultSetHandler<T> {
        T handle(ResultSet rs) throws SQLException;
    }
}