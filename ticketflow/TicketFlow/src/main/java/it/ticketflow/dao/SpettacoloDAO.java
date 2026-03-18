package it.ticketflow.dao;

import it.ticketflow.db.DbConnection;
import it.ticketflow.eccezioni.DatabaseException;
import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.model.Cinema;
import it.ticketflow.model.Film;
import it.ticketflow.model.Sala;
import it.ticketflow.model.Spettacolo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO per la gestione degli Spettacoli.
 * Gestisce le operazioni CRUD sulla tabella Spettacolo.
 * @author Stefano Bellan 20054330
 */
public class SpettacoloDAO {

    private static final String SELECT_ALL = """
        SELECT 
            S.id AS s_id, S.data_ora, S.prezzo_base, S.url_locandina AS s_locandina,
            F.id AS f_id, F.titolo, F.descrizione, F.genere, F.durata, F.url_locandina AS f_locandina,
            SL.id AS sl_id, SL.nome AS sl_nome, SL.righe, SL.colonne, SL.tipo_sala,
            C.id AS c_id, C.nome AS c_nome, C.indirizzo AS c_indirizzo, C.citta
        FROM Spettacolo S
        JOIN Film F ON S.id_film = F.id
        JOIN Sala SL ON S.id_sala = SL.id
        JOIN Cinema C ON SL.id_cinema = C.id
        ORDER BY S.data_ora ASC
    """;

    private static final String SELECT_BY_ID = """
        SELECT 
            S.id AS s_id, S.data_ora, S.prezzo_base, S.url_locandina AS s_locandina,
            F.id AS f_id, F.titolo, F.descrizione, F.genere, F.durata, F.url_locandina AS f_locandina,
            SL.id AS sl_id, SL.nome AS sl_nome, SL.righe, SL.colonne, SL.tipo_sala,
            C.id AS c_id, C.nome AS c_nome, C.indirizzo AS c_indirizzo, C.citta
        FROM Spettacolo S
        JOIN Film F ON S.id_film = F.id
        JOIN Sala SL ON S.id_sala = SL.id
        JOIN Cinema C ON SL.id_cinema = C.id
        WHERE S.id = ?
    """;

    private static final String INSERT_QUERY =
            "INSERT INTO Spettacolo (data_ora, prezzo_base, id_film, id_sala, url_locandina) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE Spettacolo SET data_ora = ?, prezzo_base = ?, id_film = ?, id_sala = ?, url_locandina = ? WHERE id = ?";

    /**
     * Recupera tutti gli spettacoli dal database.
     */
    public List<Spettacolo> findAll() {
        List<Spettacolo> spettacoli = new ArrayList<>();
        try {
            Connection conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        spettacoli.add(mapRowToSpettacolo(rs));
                    } catch (DatiNonValidiException e) {
                        System.err.println("Skip spettacolo invalido ID " + rs.getInt("s_id") + ": " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Errore lettura spettacoli", e);
        }
        return spettacoli;
    }

    /**
     * Recupera uno spettacolo per ID.
     */
    public Spettacolo trovaPerId(int id) {
        try {
            Connection conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapRowToSpettacolo(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Errore ricerca spettacolo ID " + id, e);
        }
        return null;
    }

    /**
     * Inserisce un nuovo spettacolo.
     */
    public void create(Spettacolo spettacolo) {
        try {
            Connection conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setTimestamp(1, Timestamp.valueOf(spettacolo.getDataOra()));
                stmt.setDouble(2, spettacolo.getPrezzoBase());
                stmt.setInt(3, spettacolo.getFilm().getId());
                stmt.setInt(4, spettacolo.getSala().getId());
                stmt.setString(5, spettacolo.getUrlLocandina());
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        spettacolo.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Errore creazione spettacolo", e);
        }
    }

    /**
     * Aggiorna i dati di uno spettacolo esistente.
     */
    public void update(Spettacolo spettacolo) {
        try {
            Connection conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(UPDATE_QUERY)) {
                stmt.setTimestamp(1, Timestamp.valueOf(spettacolo.getDataOra()));
                stmt.setDouble(2, spettacolo.getPrezzoBase());
                stmt.setInt(3, spettacolo.getFilm().getId());
                stmt.setInt(4, spettacolo.getSala().getId());
                stmt.setString(5, spettacolo.getUrlLocandina());
                stmt.setInt(6, spettacolo.getId());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Errore aggiornamento spettacolo", e);
        }
    }

    private Spettacolo mapRowToSpettacolo(ResultSet rs) throws SQLException, DatiNonValidiException {
        // Mappatura Cinema
        Cinema cinema = new Cinema(
                rs.getInt("c_id"),
                rs.getString("c_nome"),
                rs.getString("c_indirizzo"),
                rs.getString("citta")
        );

        // Mappatura Sala
        Sala sala = new Sala(cinema, rs.getString("sl_nome"), rs.getInt("righe"), rs.getInt("colonne"), rs.getString("tipo_sala"));
        sala.setId(rs.getInt("sl_id"));

        // Mappatura Film
        Film film = new Film(rs.getInt("f_id"), rs.getInt("durata"), rs.getString("titolo"), rs.getString("descrizione"), rs.getString("genere"));
        String fLoc = rs.getString("f_locandina");
        if(fLoc != null) film.setUrlLocandina(fLoc);

        // Mappatura Spettacolo
        Spettacolo s = new Spettacolo(film, sala, rs.getTimestamp("data_ora").toLocalDateTime(), rs.getDouble("prezzo_base"), rs.getString("s_locandina"));
        s.setId(rs.getInt("s_id"));
        return s;
    }
}