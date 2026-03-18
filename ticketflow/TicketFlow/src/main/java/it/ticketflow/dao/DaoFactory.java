package it.ticketflow.dao;
/**
 * Factory per la gestione dei file DAO basato sul pattern Singleton
 *
 * @author Timothy Giolito 20054431
 * */
// Factory per la gestione dei file DAO implementando il pattern Singleton
public class DaoFactory {

    // istanze statiche DAO
    private static UtenteDAO utenteDAO;
    private static FilmDAO filmDAO;
    private static CinemaDAO cinemaDAO;
    private static SpettacoloDAO spettacoloDAO;
    private static BigliettoDAO bigliettoDAO;
    private static SalaDAO salaDAO;

    // costruttore privato
    private DaoFactory() {}

    // ----metodi statici

    public static UtenteDAO getUtenteDAO() {

        if (utenteDAO == null) {

            utenteDAO = new UtenteDAO();
        }
        return utenteDAO;
    }

    public static FilmDAO getFilmDAO() {

        if (filmDAO == null) {

            filmDAO = new FilmDAO();
        }
        return filmDAO;
    }

    public static CinemaDAO getCinemaDAO() {

        if (cinemaDAO == null) {

            cinemaDAO = new CinemaDAO();
        }
        return cinemaDAO;
    }

    public static SpettacoloDAO getSpettacoloDAO() {

        if (spettacoloDAO == null) {

            spettacoloDAO = new SpettacoloDAO();
        }
        return spettacoloDAO;
    }

    public static BigliettoDAO getBigliettoDAO() {

        if (bigliettoDAO == null) {

            bigliettoDAO = new BigliettoDAO();
        }
        return bigliettoDAO;
    }

    public static SalaDAO getSalaDAO() {

        if (salaDAO == null) {

            salaDAO = new SalaDAO();
        }
        return salaDAO;
    }
}