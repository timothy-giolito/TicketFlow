package it.ticketflow.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import it.ticketflow.db.DbConnection;
import it.ticketflow.model.Utente;
import it.ticketflow.model.Ruolo;

/**
 * Classe DAO per la gestione della persistenza degli Utenti.
 * Gestisce operazioni CRUD e aggiornamenti specifici per i dati di pagamento.
 * @author Luca Franzon 20054744
 */
public class UtenteDAO {

    // --- QUERY SQL ---

    private static final String INSERT_QUERY =
            "INSERT INTO Utente (nome, cognome, email, password, ruolo, eta, indirizzo, numero_carta, scadenza_carta, cvv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_EMAIL =
            "SELECT * FROM Utente WHERE email = ?";

    private static final String SELECT_BY_ID =
            "SELECT * FROM Utente WHERE id = ?";

    private static final String SELECT_ALL =
            "SELECT * FROM Utente";

    private static final String UPDATE_CINEMA_ID =
            "UPDATE Utente SET id_cinema = ? WHERE id = ?";

    // Nuova query per aggiornare solo i dati di pagamento
    private static final String UPDATE_PAGAMENTO =
            "UPDATE Utente SET numero_carta = ?, scadenza_carta = ?, cvv = ? WHERE id = ?";

    // Query per la promozione a Manager (SPOSTATA QUI DENTRO, POSIZIONE CORRETTA)
    private static final String PROMOTE_TO_MANAGER =
            "UPDATE Utente SET ruolo = 'MANAGER', id_cinema = ? WHERE id = ?";

    /**
     * Inserisce un nuovo utente nel database.
     * Gestisce correttamente i campi nullabili come quelli di pagamento.
     *
     * @param u L'oggetto Utente da persistere.
     * @throws SQLException In caso di errore di connessione o violazione vincoli (es. email duplicata).
     */
    public void inserisci(Utente u) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) throw new SQLException("Connessione al database non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY)) {
            stmt.setString(1, u.getNome());
            stmt.setString(2, u.getCognome());
            stmt.setString(3, u.getEmail());
            stmt.setString(4, u.getPassword());
            stmt.setString(5, u.getRuolo().name());
            stmt.setInt(6, u.getEta());

            // Gestione campi opzionali (possono essere null se non ancora inseriti)
            stmt.setString(7, u.getIndirizzo());
            stmt.setString(8, u.getNumeroCarta());
            stmt.setString(9, u.getScadenzaCarta());
            stmt.setString(10, u.getCvv());

            stmt.executeUpdate();
        } catch (SQLException e) {
            // Gestione specifica per email duplicate
            if (e.getErrorCode() == 19 || e.getMessage().contains("UNIQUE")) {
                throw new SQLException("Errore: Un utente con email " + u.getEmail() + " è già registrato.", e);
            }
            throw e;
        }
    }

    /**
     * Aggiorna esclusivamente i dati di pagamento di un utente esistente.
     *
     * @param u L'utente con i nuovi dati di carta impostati.
     * @throws SQLException Se l'aggiornamento fallisce.
     */
    public void aggiornaDatiPagamento(Utente u) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connessione DB mancante");

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_PAGAMENTO)) {
            stmt.setString(1, u.getNumeroCarta());
            stmt.setString(2, u.getScadenzaCarta());
            stmt.setString(3, u.getCvv());
            stmt.setInt(4, u.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Recupera la lista completa di tutti gli utenti registrati nel sistema.
     *
     * @return Una lista di oggetti Utente (può essere vuota se non ci sono utenti).
     * @throws SQLException Se si verifica un errore durante la lettura dal database.
     */
    public List<Utente> findAll() throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connessione DB mancante");

        List<Utente> utenti = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {

            // Itera su tutti i risultati del database
            while (rs.next()) {
                Utente u = mapResultSetToUtente(rs);
                if (u != null) {
                    utenti.add(u);
                }
            }
        }
        return utenti;
    }

    /**
     * Cerca un utente specifico tramite indirizzo email.
     *
     * @param email L'email da cercare.
     * @return L'oggetto Utente se trovato, altrimenti null.
     * @throws SQLException Errore di accesso ai dati.
     */
    public Utente findByEmail(String email) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connessione DB mancante");

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUtente(rs);
            }
        }
        return null;
    }

    /**
     * Cerca un utente specifico tramite il suo ID univoco.
     *
     * @param id L'ID dell'utente.
     * @return L'oggetto Utente se trovato, altrimenti null.
     * @throws SQLException Errore di accesso ai dati.
     */
    public Utente findById(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connessione DB mancante");

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToUtente(rs);
            }
        }
        return null;
    }

    /**
     * Aggiorna l'associazione tra un Manager e il suo Cinema.
     *
     * @param idUtente ID del manager.
     * @param idCinema ID del cinema da assegnare.
     * @throws SQLException Errore durante l'aggiornamento.
     */
    public void aggiornaIdCinema(int idUtente, int idCinema) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connessione DB mancante");

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_CINEMA_ID)) {
            stmt.setInt(1, idCinema);
            stmt.setInt(2, idUtente);
            stmt.executeUpdate();
        }
    }

    /**
     * Promuove un utente al ruolo di MANAGER e gli assegna un cinema.
     * (METODO SPOSTATO QUI DENTRO, POSIZIONE CORRETTA)
     *
     * @param idUtente ID dell'utente da promuovere.
     * @param idCinema ID del cinema assegnato.
     * @throws SQLException Errore durante l'aggiornamento.
     */
    public void promuoviAManager(int idUtente, int idCinema) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connessione DB mancante");

        try (PreparedStatement stmt = conn.prepareStatement(PROMOTE_TO_MANAGER)) {
            stmt.setInt(1, idCinema);
            stmt.setInt(2, idUtente);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Promozione fallita: Utente non trovato.");
            }
        }
    }

    /**
     * Metodo di utilità interno per convertire una riga del ResultSet in un oggetto Utente.
     * Gestisce il parsing dei ruoli e i campi opzionali.
     *
     * @param rs Il ResultSet posizionato sulla riga corrente.
     * @return Un'istanza di Utente popolata.
     * @throws SQLException Se si verifica un errore di lettura colonne.
     */
    private Utente mapResultSetToUtente(ResultSet rs) throws SQLException {
        int idUtente = rs.getInt("id");
        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String email = rs.getString("email");
        String passwordDb = rs.getString("password");
        String ruoloString = rs.getString("ruolo");
        int eta = rs.getInt("eta");

        // Recupero campi opzionali (gestione valori NULL dal DB)
        String indirizzo = rs.getString("indirizzo");
        String numeroCarta = rs.getString("numero_carta");
        String scadenzaCarta = rs.getString("scadenza_carta");
        String cvv = rs.getString("cvv");

        int idCinema = rs.getInt("id_cinema"); // Restituisce 0 se il valore è NULL

        try {
            Ruolo ruolo = Ruolo.valueOf(ruoloString);

            // Costruzione dell'oggetto Utente completo
            Utente u = new Utente(nome, cognome, email, passwordDb, ruolo, eta, indirizzo, numeroCarta, scadenzaCarta, cvv);
            u.setId(idUtente);
            u.setIdCinema(idCinema);
            return u;

        } catch (IllegalArgumentException e) {
            // Gestione robusta nel caso in cui il ruolo nel DB non corrisponda all'Enum
            System.err.println("Dati utente non validi nel DB (ID " + idUtente + "): " + e.getMessage());
            return null;
        }
    }
}