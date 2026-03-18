package it.ticketflow.service;

import it.ticketflow.db.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servizio per la generazione di report e statistiche.
 * Gestisce le query aggregate per calcolare incassi e biglietti venduti.
 */
public class ReportingService {

    /**
     * Calcola l'incasso totale globale di tutti i cinema.
     * @return Somma totale dei prezzi dei biglietti venduti.
     */
    public double getIncassiGlobali() throws SQLException {
        // CORREZIONE: Il nome della colonna nel DB è 'prezzo_finale', non 'prezzo'
        String sql = "SELECT SUM(prezzo_finale) FROM Biglietto";

        Connection conn = DbConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    /**
     * Calcola il numero totale di biglietti venduti globalmente.
     * @return Numero totale di righe nella tabella Biglietto.
     */
    public int getBigliettiVendutiTotali() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Biglietto";
        Connection conn = DbConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Calcola l'incasso totale per uno specifico cinema.
     * Esegue una JOIN tra Biglietto, Spettacolo e Sala filtrando per id_cinema.
     * @param idCinema ID del cinema di riferimento.
     * @return Somma degli incassi per quel cinema.
     */
    public double getIncassiCinema(int idCinema) throws SQLException {
        // CORREZIONE: Il nome della colonna nel DB è 'prezzo_finale'
        String sql = "SELECT SUM(b.prezzo_finale) " +
                "FROM Biglietto b " +
                "JOIN Spettacolo s ON b.id_spettacolo = s.id " +
                "JOIN Sala r ON s.id_sala = r.id " +
                "WHERE r.id_cinema = ?";

        Connection conn = DbConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCinema);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            return 0.0;
        }
    }

    /**
     * Calcola il numero di biglietti venduti per uno specifico cinema.
     * @param idCinema ID del cinema di riferimento.
     * @return Numero di biglietti venduti.
     */
    public int getBigliettiVendutiCinema(int idCinema) throws SQLException {
        String sql = "SELECT COUNT(b.id) " +
                "FROM Biglietto b " +
                "JOIN Spettacolo s ON b.id_spettacolo = s.id " +
                "JOIN Sala r ON s.id_sala = r.id " +
                "WHERE r.id_cinema = ?";

        Connection conn = DbConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCinema);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        }
    }
}