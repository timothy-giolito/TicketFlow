package it.ticketflow.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import it.ticketflow.db.DbConnection;
import it.ticketflow.model.Sala;
import it.ticketflow.model.Cinema;
import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * Gestisce l'accesso ai dati (DAO) per l'entità Sala.
 * <p>
 * Questa classe si occupa di tutte le operazioni CRUD sulla tabella 'sala',
 * gestendo la relazione con l'entità Cinema e il calcolo automatico della capienza.
 * @author Stefano Bellan 20054330
 */
public class SalaDAO {

    // --- QUERY SQL ---

    // Nota: La colonna 'capienza' è obbligatoria nel DB e viene calcolata prima dell'inserimento.
    private static final String INSERT_QUERY = "INSERT INTO sala (id_cinema, nome, righe, colonne, capienza, tipo_sala) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM sala WHERE id = ?";

    private static final String SELECT_ALL = "SELECT * FROM sala";

    // Query fondamentale per il Manager: recupera tutte le sale di un cinema specifico
    private static final String SELECT_BY_CINEMA = "SELECT * FROM sala WHERE id_cinema = ?";

    private static final String DELETE_QUERY = "DELETE FROM sala WHERE id = ?";

    /**
     * Inserisce una nuova sala nel database.
     * <p>
     * Il metodo calcola automaticamente la capienza totale basandosi su righe e colonne
     * prima di eseguire il salvataggio, garantendo la coerenza dei dati.
     *
     * @param sala L'oggetto Sala da salvare. Deve contenere un riferimento valido a un Cinema.
     * @throws SQLException In caso di errore durante l'interazione con il database.
     */
    public void inserisci(Sala sala) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        if (conn == null) {
            throw new SQLException("Connessione al database non disponibile.");
        }

        // RETURN_GENERATED_KEYS permette di recuperare l'ID assegnato dal DB
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {

            // Calcolo della logica di business: Capienza = Righe * Colonne
            int capienzaCalcolata = sala.getRighe() * sala.getColonne();

            // Mapping dei parametri
            stmt.setInt(1, sala.getCinema().getId());
            stmt.setString(2, sala.getNome());
            stmt.setInt(3, sala.getRighe());
            stmt.setInt(4, sala.getColonne());
            stmt.setInt(5, capienzaCalcolata); // Inseriamo il valore calcolato
            stmt.setString(6, sala.getTipoSala());

            stmt.executeUpdate();

            // Recupero dell'ID generato e aggiornamento dell'oggetto Java
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    sala.setId(generatedKeys.getInt(1));
                    sala.setCapienza(capienzaCalcolata); // Aggiorniamo anche il model locale
                }
            } catch (DatiNonValidiException e) {
                // Log dell'errore se l'assegnazione dell'ID fallisce lato Java
                System.err.println("Errore aggiornamento ID Sala: " + e.getMessage());
            }
        }
    }

    /**
     * Recupera una sala specifica tramite il suo ID univoco.
     *
     * @param id L'identificativo della sala.
     * @return L'oggetto Sala trovato, oppure null se non esiste.
     * @throws SQLException In caso di errore SQL.
     */
    public Sala trovaPerId(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        Sala sala = null;

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sala = mappaResultSet(rs);
                }
            }
        }
        return sala;
    }

    /**
     * Recupera l'elenco completo di tutte le sale presenti nel database.
     * <p>
     * Questo metodo interroga l'intera tabella 'sala' e costruisce una lista
     * di oggetti Sala popolati con i rispettivi dati e riferimenti ai Cinema.
     *
     * @return Una lista contenente tutte le sale registrate nel sistema.
     * @throws SQLException In caso di errore durante l'interazione con il database.
     */
    public List<Sala> trovaTutti() throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        List<Sala> sale = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Sala s = mappaResultSet(rs);
                if (s != null) {
                    sale.add(s);
                }
            }
        }
        return sale;
    }

    /**
     * Recupera tutte le sale appartenenti a uno specifico cinema.
     * Utile per popolare la dashboard del Manager.
     *
     * @param idCinema L'ID del cinema di cui cercare le sale.
     * @return Una lista di oggetti Sala appartenenti al cinema specificato.
     * @throws SQLException In caso di errore SQL.
     */
    public List<Sala> trovaPerCinema(int idCinema) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        List<Sala> sale = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CINEMA)) {
            stmt.setInt(1, idCinema);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sala s = mappaResultSet(rs);
                    if (s != null) {
                        sale.add(s);
                    }
                }
            }
        }
        return sale;
    }

    /**
     * Elimina una sala dal sistema.
     *
     * @param id L'ID della sala da eliminare.
     * @throws SQLException In caso di errore SQL (es. vincoli di chiave esterna se ci sono spettacoli).
     */
    public void elimina(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_QUERY)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // --- METODI DI SUPPORTO ---

    /**
     * Converte una riga del ResultSet SQL in un oggetto Java Sala.
     * Ricostruisce anche l'oggetto Cinema associato.
     *
     * @param rs Il ResultSet posizionato sulla riga da convertire.
     * @return Un'istanza di Sala popolata, oppure null in caso di errore di validazione dati.
     * @throws SQLException In caso di errore di lettura dal ResultSet.
     */
    private Sala mappaResultSet(ResultSet rs) throws SQLException {
        try {
            // Estrazione dati primitivi
            int id = rs.getInt("id");
            String nome = rs.getString("nome");
            int righe = rs.getInt("righe");
            int colonne = rs.getInt("colonne");
            // La capienza viene letta dal DB (dove è stata calcolata all'inserimento)
            int capienza = rs.getInt("capienza");
            String tipoSala = rs.getString("tipo_sala");
            int idCinema = rs.getInt("id_cinema");

            // Ricostruzione della dipendenza Cinema
            CinemaDAO cinemaDAO = new CinemaDAO();
            Cinema cinema = cinemaDAO.trovaPerId(idCinema);

            // Creazione oggetto Sala
            Sala sala = new Sala(cinema, nome, righe, colonne, tipoSala);
            sala.setId(id);
            sala.setCapienza(capienza); // Assicura che il model rifletta il DB

            return sala;

        } catch (DatiNonValidiException e) {
            System.err.println("Errore nel mapping della Sala (ID " + rs.getInt("id") + "): " + e.getMessage());
            return null;
        }
    }
}