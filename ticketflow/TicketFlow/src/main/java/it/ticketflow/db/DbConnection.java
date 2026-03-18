package it.ticketflow.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestisce la connessione al database SQLite utilizzando il pattern Singleton.
 * @author Timothy Giolito 20054431
 *
 */
public class DbConnection {

    private static DbConnection instance;
    private static String url = "jdbc:sqlite:ticketflow.db";

    private DbConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    /**
     * Apre e restituisce una connessione configurata per evitare lock.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);

        // Configurazione fondamentale per evitare SQLITE_BUSY
        try (Statement stmt = conn.createStatement()) {
            // Imposta un timeout di 5000ms (5 secondi)
            // Se il DB è occupato, aspetta invece di fallire subito.
            stmt.execute("PRAGMA busy_timeout = 5000;");

            // Abilita i vincoli di chiave esterna (integrità dati)
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Modalità WAL (Write-Ahead Logging): permette letture e scritture concorrenti.
            // Riduce drasticamente i conflitti di lock.
            stmt.execute("PRAGMA journal_mode = WAL;");
        }

        return conn;
    }

    public static void setTestMode(boolean isTestMode) {
        if (isTestMode) {
            url = "jdbc:sqlite:ticketflow_test.db";
        } else {
            url = "jdbc:sqlite:ticketflow.db";
        }
    }
}