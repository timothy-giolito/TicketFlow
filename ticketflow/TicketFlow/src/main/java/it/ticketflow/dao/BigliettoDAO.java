package it.ticketflow.dao;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

import it.ticketflow.db.DbConnection;
import it.ticketflow.model.Biglietto;
import it.ticketflow.model.Utente;
import it.ticketflow.model.Spettacolo;
import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.eccezioni.PostoOccupatoException;
import java.time.LocalDateTime;

/**
 * Gestisce le operazioni CRUD (Create, Read, Update, Delete) per l'entità Biglietto.
 * Si occupa di interagire con la tabella 'Biglietto' del database, gestendo
 * il mapping tra le colonne SQL e l'oggetto Java, inclusa la gestione di
 * chiavi esterne (Utente, Spettacolo) e campi speciali come QR Code e Date.
 * @author Luca Franzon 20054744
 */
public class BigliettoDAO {

    // --- QUERY SQL ---

    /** Query per verificare se un posto specifico è già stato prenotato per uno spettacolo. */
    private static final String CHECK_POSTO = "SELECT 1 FROM Biglietto WHERE id_spettacolo = ? AND fila = ? AND colonna = ?";

    /** * Query di inserimento.
     * Nota: L'ID è autoincrementale e gestito dal DB.
     * Mappa i campi: utente, spettacolo, coordinate posto, prezzo finale, QR e data emissione.
     */
    private static final String INSERT_QUERY = "INSERT INTO Biglietto (id_utente, id_spettacolo, fila, colonna, prezzo_finale, codice_qr, data_emissione) VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** Seleziona tutti i biglietti venduti per un determinato spettacolo. */
    private static final String SELECT_BY_SPETTACOLO = "SELECT * FROM Biglietto WHERE id_spettacolo = ?";

    /** Seleziona lo storico dei biglietti acquistati da un utente specifico. */
    private static final String SELECT_BY_UTENTE = "SELECT * FROM Biglietto WHERE id_utente = ?";


    /**
     * Inserisce un nuovo biglietto nel database dopo aver verificato la disponibilità del posto.
     *
     * @param b L'oggetto Biglietto da salvare.
     * @throws SQLException In caso di problemi di connessione o errori SQL.
     * @throws PostoOccupatoException Se il posto selezionato (fila/colonna) non è più disponibile.
     */
    public void inserisci(Biglietto b) throws SQLException, PostoOccupatoException {
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Impossibile stabilire una connessione con il database.");
        }

        // 1. Verifica disponibilità del posto prima dell'inserimento
        try (PreparedStatement stmtCheck = conn.prepareStatement(CHECK_POSTO)) {
            stmtCheck.setInt(1, b.getSpettacolo().getId());
            stmtCheck.setInt(2, b.getFila());
            stmtCheck.setInt(3, b.getColonna());

            try (ResultSet rs = stmtCheck.executeQuery()) {
                if (rs.next()) {
                    // Se la query restituisce un risultato, il posto è già occupato
                    throw new PostoOccupatoException("Il posto (Fila: " + b.getFila() +
                            ", Colonna: " + b.getColonna() + ") è già occupato.");
                }
            }
        }

        // 2. Inserimento effettivo del biglietto
        try (PreparedStatement stmtInsert = conn.prepareStatement(INSERT_QUERY)) {
            stmtInsert.setInt(1, b.getUtente().getId());
            stmtInsert.setInt(2, b.getSpettacolo().getId());
            stmtInsert.setInt(3, b.getFila());
            stmtInsert.setInt(4, b.getColonna());
            stmtInsert.setDouble(5, b.getPrezzo()); // Mappa il prezzo calcolato nel campo 'prezzo_finale'
            stmtInsert.setString(6, b.getCodiceQr());
            stmtInsert.setTimestamp(7, Timestamp.valueOf(b.getDataEmissione())); // Conversione LocalDateTime -> Timestamp SQL

            stmtInsert.executeUpdate();
        }
    }

    /**
     * Recupera la lista di tutti i biglietti emessi per un determinato spettacolo.
     * Utile per visualizzare la mappa dei posti occupati in sala.
     *
     * @param spettacoloId L'ID dello spettacolo di riferimento.
     * @return Una lista di oggetti Biglietto (vuota se non ci sono biglietti).
     * @throws SQLException In caso di errore di accesso al database.
     */
    public List<Biglietto> trovaPerSpettacolo(int spettacoloId) throws SQLException {
        SpettacoloDAO spettacoloDAO = new SpettacoloDAO();
        UtenteDAO utenteDAO = new UtenteDAO();

        // Recuperiamo i dettagli dello spettacolo una sola volta per ottimizzare
        Spettacolo s = spettacoloDAO.trovaPerId(spettacoloId);
        if (s == null) return new ArrayList<>();

        List<Biglietto> biglietti = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) throw new SQLException("Connessione al database non valida.");

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_SPETTACOLO)) {
            stmt.setInt(1, spettacoloId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Ricostruiamo l'oggetto Biglietto associando lo Spettacolo già caricato
                    Biglietto b = mappingBiglietto(rs, utenteDAO, s);
                    if (b != null) biglietti.add(b);
                }
            }
        }
        return biglietti;
    }

    /**
     * Recupera lo storico dei biglietti acquistati da un utente.
     *
     * @param idUtente L'ID dell'utente di cui cercare i biglietti.
     * @return Una lista di biglietti acquistati dall'utente.
     * @throws SQLException In caso di errore SQL.
     */
    public List<Biglietto> trovaPerUtente(int idUtente) throws SQLException {
        UtenteDAO utenteDAO = new UtenteDAO();
        SpettacoloDAO spettacoloDAO = new SpettacoloDAO();

        // Recuperiamo i dati dell'utente
        Utente u = utenteDAO.findById(idUtente);
        if (u == null) return new ArrayList<>();

        List<Biglietto> storico = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) throw new SQLException("Connessione al database non valida.");

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_UTENTE)) {
            stmt.setInt(1, idUtente);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Per ogni biglietto, dobbiamo recuperare lo specifico spettacolo associato
                    int idSpettacolo = rs.getInt("id_spettacolo");
                    Spettacolo s = spettacoloDAO.trovaPerId(idSpettacolo);

                    if (s != null) {
                        Biglietto b = mappingBiglietto(rs, u, s);
                        if (b != null) storico.add(b);
                    }
                }
            }
        }
        return storico;
    }

    // --- METODI DI SUPPORTO (MAPPING) ---

    /**
     * Metodo helper per mappare una riga del ResultSet in un oggetto Biglietto.
     * Questo overload recupera l'utente dal DB tramite il suo ID.
     */
    private Biglietto mappingBiglietto(ResultSet rs, UtenteDAO uDao, Spettacolo s) throws SQLException {
        int idUtente = rs.getInt("id_utente");
        Utente u = uDao.findById(idUtente);
        return mappingBiglietto(rs, u, s);
    }

    /**
     * Metodo helper principale per la costruzione dell'oggetto Biglietto.
     * Converte i tipi SQL (Timestamp, Double, ecc.) nei tipi Java del Model.
     */
    private Biglietto mappingBiglietto(ResultSet rs, Utente u, Spettacolo s) throws SQLException {
        try {
            // Estrazione dati primitivi
            int id = rs.getInt("id");
            int fila = rs.getInt("fila");
            int colonna = rs.getInt("colonna");
            double prezzo = rs.getDouble("prezzo_finale"); // Corrisponde alla colonna del DB

            // Estrazione e conversione dati complessi
            String qr = rs.getString("codice_qr");
            Timestamp ts = rs.getTimestamp("data_emissione");
            LocalDateTime data = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

            // Creazione oggetto del dominio
            Biglietto b = new Biglietto(u, s, fila, colonna, prezzo);
            b.setId(id);
            b.setCodiceQr(qr);
            b.setDataEmissione(data);

            return b;
        } catch (DatiNonValidiException e) {
            // In caso di dati corrotti nel DB che violano le regole del dominio
            e.printStackTrace();
            return null;
        }
    }
}