package it.ticketflow.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe responsabile dell'inizializzazione dello schema del database.
 * Crea le tabelle necessarie se non esistono.
 * @author Timothy Giolito 20054431
 */
public class SchemaInit {

    /**
     * Esegue le query DDL per creare le tabelle.
     */
    public static void initializeDatabase() {

        String createCinemaTable = """
            CREATE TABLE IF NOT EXISTS cinema (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                indirizzo TEXT NOT NULL,
                citta TEXT NOT NULL
            );
        """;

        String createSalaTable = """
            CREATE TABLE IF NOT EXISTS sala (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                righe INTEGER NOT NULL,
                colonne INTEGER NOT NULL,
                capienza INTEGER,
                tipo_sala TEXT,
                id_cinema INTEGER,
                FOREIGN KEY (id_cinema) REFERENCES cinema(id) ON DELETE CASCADE
            );
        """;

        String createFilmTable = """
            CREATE TABLE IF NOT EXISTS film (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titolo TEXT NOT NULL,
                descrizione TEXT,
                genere TEXT,
                durata INTEGER NOT NULL,
                url_locandina TEXT
            );
        """;

        // Tabella Spettacolo con supporto locandina specifica
        String createSpettacoloTable = """
            CREATE TABLE IF NOT EXISTS spettacolo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data_ora TIMESTAMP NOT NULL,
                prezzo_base REAL NOT NULL,
                id_film INTEGER,
                id_sala INTEGER,
                url_locandina TEXT,
                FOREIGN KEY (id_film) REFERENCES film(id) ON DELETE CASCADE,
                FOREIGN KEY (id_sala) REFERENCES sala(id) ON DELETE CASCADE
            );
        """;

        String createUtenteTable = """
            CREATE TABLE IF NOT EXISTS utente (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                cognome TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                ruolo TEXT NOT NULL,
                eta INTEGER,
                indirizzo TEXT,
                numero_carta TEXT,
                scadenza_carta TEXT,
                cvv TEXT,
                id_cinema INTEGER
            );
        """;

        String createBigliettoTable = """
            CREATE TABLE IF NOT EXISTS biglietto (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fila INTEGER NOT NULL,
                colonna INTEGER NOT NULL,
                prezzo_finale REAL NOT NULL,
                tipo_biglietto TEXT,
                codice_qr TEXT,
                data_emissione TIMESTAMP,
                id_spettacolo INTEGER,
                id_utente INTEGER,
                FOREIGN KEY (id_spettacolo) REFERENCES spettacolo(id),
                FOREIGN KEY (id_utente) REFERENCES utente(id)
            );
        """;

        try (Connection conn = DbConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createCinemaTable);
            stmt.execute(createSalaTable);
            stmt.execute(createFilmTable);
            stmt.execute(createSpettacoloTable);
            stmt.execute(createUtenteTable);
            stmt.execute(createBigliettoTable);

            System.out.println("Tabelle del database inizializzate correttamente.");

        } catch (SQLException e) {
            System.err.println("Errore durante l'inizializzazione del database: " + e.getMessage());
        }
    }
}