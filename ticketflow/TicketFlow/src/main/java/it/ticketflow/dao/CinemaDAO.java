package it.ticketflow.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import it.ticketflow.db.DbConnection;
import it.ticketflow.model.Cinema;
import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * DAO per l'entità Cinema.
 * Gestisce l'inserimento, la ricerca e la cancellazione delle strutture.
 * @author Timothy Giolito 20054431
 */
public class CinemaDAO {

    private static final String INSERT_QUERY = "INSERT INTO cinema (nome, indirizzo, citta) VALUES (?, ?, ?)";
    private static final String SELECT_ALL_QUERY = "SELECT * FROM cinema";
    private static final String SELECT_BY_ID_QUERY = "SELECT * FROM cinema WHERE id = ?";
    private static final String DELETE_QUERY = "DELETE FROM cinema WHERE id = ?";
    private static final String UPDATE_QUERY = "UPDATE cinema SET nome = ?, indirizzo = ?, citta = ? WHERE id = ?";

    /**
     * Inserisce un nuovo cinema.
     * @param cinema L'oggetto da salvare.
     */
    public void insert(Cinema cinema) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("DB non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cinema.getNome());
            stmt.setString(2, cinema.getIndirizzo());
            stmt.setString(3, cinema.getCitta());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    cinema.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Trova un cinema per ID.
     */
    public Cinema trovaPerId(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("DB non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_QUERY)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    try {
                        return new Cinema(
                                rs.getInt("id"),
                                rs.getString("nome"),
                                rs.getString("indirizzo"),
                                rs.getString("citta")
                        );
                    } catch (DatiNonValidiException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Recupera tutti i cinema.
     */
    public List<Cinema> findAll() throws SQLException {
        List<Cinema> cinemas = new ArrayList<>();
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("DB non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    cinemas.add(new Cinema(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("indirizzo"),
                            rs.getString("citta")
                    ));
                } catch (DatiNonValidiException e) {
                    System.err.println("Errore cinema ID " + rs.getInt("id"));
                }
            }
        }
        return cinemas;
    }

    /**
     * Elimina un cinema per ID.
     */
    public void delete(int id) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("DB non disponibile.");

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_QUERY)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Aggiorna i dati di un cinema esistente nel database.
     *
     * @param cinema L'oggetto Cinema con i dati aggiornati.
     * @throws SQLException In caso di errore SQL o se la connessione fallisce.
     */
    public void update(Cinema cinema) throws SQLException {
        Connection conn = DbConnection.getInstance().getConnection();
        if (conn == null) throw new SQLException("Connection to database failed.");

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_QUERY)) {
            stmt.setString(1, cinema.getNome());
            stmt.setString(2, cinema.getIndirizzo());
            stmt.setString(3, cinema.getCitta());
            stmt.setInt(4, cinema.getId());

            stmt.executeUpdate();
        }
    }


    // --- HELPER METHODS ---

    /**
     * Metodo di utilità per mappare una riga del ResultSet in un oggetto Cinema.
     * Gestisce le eccezioni di validazione dei dati.
     *
     * @param rs Il ResultSet posizionato sulla riga da leggere.
     * @return L'oggetto Cinema mappato o null in caso di dati non validi.
     * @throws SQLException Se si verifica un errore di lettura dal ResultSet.
     */
    private Cinema mapResultSetToCinema(ResultSet rs) throws SQLException {
        try {
            return new Cinema(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("indirizzo"),
                    rs.getString("citta")
            );
        } catch (DatiNonValidiException e) {
            // Log dell'errore (opzionale) e ritorno null per indicare dato corrotto
            System.err.println("Skipping invalid Cinema data (ID: " + rs.getInt("id") + "): " + e.getMessage());
            return null;
        }
    }
}