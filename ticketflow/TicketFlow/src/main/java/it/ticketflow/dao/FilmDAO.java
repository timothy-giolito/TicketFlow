package it.ticketflow.dao;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import it.ticketflow.db.DbConnection;
import it.ticketflow.model.Film;
import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * Gestisce l'accesso ai dati (DAO) per l'entità Film.
 * <p>
 * Questa classe fornisce i metodi necessari per eseguire le operazioni CRUD sulla tabella 'Film'.
 * Aggiornato per gestire il campo 'url_locandina' e la ricerca per titolo.
 *
 * @author Timothy Giolito 20054431
 */
public class FilmDAO {

    // Query SQL predefinite
    private static final String INSERT_QUERY = "INSERT INTO Film (titolo, genere, durata, descrizione, url_locandina) VALUES (?,?,?,?,?)";
    private static final String SELECT_ALL = "SELECT * FROM Film";
    private static final String SELECT_BY_ID = "SELECT * FROM Film WHERE id = ?";
    private static final String SELECT_BY_TITLE = "SELECT * FROM Film WHERE titolo LIKE ?"; // Query per la ricerca parziale
    private static final String DELETE_QUERY = "DELETE FROM Film WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE Film SET titolo = ?, genere = ?, durata = ?, descrizione = ?, url_locandina = ? WHERE id = ?";

    /**
     * Inserisce un nuovo film nel database.
     *
     * @param film L'oggetto Film da salvare.
     * @throws SQLException Se si verifica un errore SQL.
     */
    public void inserisci(Film film) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Impossibile stabilire una connessione con il database");
        }

        // RETURN_GENERATED_KEYS serve per ottenere l'ID autoincrementale appena creato
        PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        // Imposta i parametri della query
        stmt.setString(1, film.getTitolo());
        stmt.setString(2, film.getGenere());
        stmt.setInt(3, film.getDurataMinuti());
        stmt.setString(4, film.getDescrizione());
        stmt.setString(5, film.getUrlLocandina());

        stmt.executeUpdate();

        // Recupera l'ID generato dal DB e aggiorna l'oggetto Java
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            try {
                int nuovoId = rs.getInt(1);
                film.setId(nuovoId);
            } catch (DatiNonValidiException e) {
                System.err.println("Errore nell'assegnazione ID generato: " + e.getMessage());
            }
        }
    }

    /**
     * Recupera l'elenco completo di tutti i film presenti nel catalogo.
     */
    public List<Film> trovaTutti() throws SQLException {
        List<Film> films = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Impossibile stabilire una connessione al database");
        }

        PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            try {
                Film f = mappaResultSetSuOggetto(rs);
                films.add(f);
            } catch (DatiNonValidiException e) {
                System.err.println("Film corrotto nel DB (ID ignoto): " + e.getMessage());
            }
        }
        return films;
    }

    /**
     * Cerca un singolo film tramite il suo identificativo univoco.
     */
    public Film trovaPerId(int filmID) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Impossibile stabilire una connessione al database");
        }

        PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID);
        stmt.setInt(1, filmID);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            try {
                return mappaResultSetSuOggetto(rs);
            } catch (DatiNonValidiException e) {
                System.err.println("Errore ricostruzione film ID " + filmID + ": " + e.getMessage());
            }
        }

        return null; // Nessun film trovato
    }

    /**
     * [NUOVO] Cerca film il cui titolo contiene la stringa specificata.
     * Richiesto dalla UserDashboard per la barra di ricerca.
     *
     * @param titoloParte Parte del titolo da cercare (es. "Avenger").
     * @return Lista di film che corrispondono alla ricerca.
     */
    public List<Film> cercaPerTitolo(String titoloParte) throws SQLException {
        List<Film> films = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Connessione al database fallita");
        }

        PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TITLE);
        // I simboli % servono per la ricerca SQL "LIKE" (contiene la stringa)
        stmt.setString(1, "%" + titoloParte + "%");

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            try {
                films.add(mappaResultSetSuOggetto(rs));
            } catch (DatiNonValidiException e) {
                System.err.println("Film saltato nei risultati di ricerca: " + e.getMessage());
            }
        }
        return films;
    }

    /**
     * Elimina un film dal database.
     */
    public void elimina(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Connessione persa");
        }

        PreparedStatement stmt = conn.prepareStatement(DELETE_QUERY);
        stmt.setInt(1, id);

        stmt.executeUpdate();
    }

    /**
     * Metodo di utilità per trasformare una riga del ResultSet in un oggetto Film.
     */
    private Film mappaResultSetSuOggetto(ResultSet rs) throws SQLException, DatiNonValidiException {
        int id = rs.getInt("id");
        String titolo = rs.getString("titolo");
        String genere = rs.getString("genere");
        int durata = rs.getInt("durata");
        String descrizione = rs.getString("descrizione");
        String urlLocandina = rs.getString("url_locandina");

        // CORREZIONE: Usiamo il costruttore a 5 parametri (che matcha Film.java)
        // L'ordine in Film.java è: (id, durata, titolo, descrizione, genere)
        Film film = new Film(id, durata, titolo, descrizione, genere);

        // CORREZIONE: Impostiamo la locandina separatamente col setter
        if (urlLocandina != null && !urlLocandina.isBlank()) {
            film.setUrlLocandina(urlLocandina);
        }

        return film;
    }

    /**
     * Aggiorna le informazioni di un film esistente.
     *
     * @param film L'oggetto Film con i dati modificati.
     * @throws SQLException In caso di errore durante l'aggiornamento.
     */
    public void update(Film film) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Unable to connect to database");

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_QUERY)) {
            stmt.setString(1, film.getTitolo());
            stmt.setString(2, film.getGenere());
            stmt.setInt(3, film.getDurataMinuti());
            stmt.setString(4, film.getDescrizione());
            stmt.setString(5, film.getUrlLocandina());
            stmt.setInt(6, film.getId());

            stmt.executeUpdate();
        }
    }
}