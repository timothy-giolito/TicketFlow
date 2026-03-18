package it.ticketflow.ui;

import it.ticketflow.model.Utente;

/**
 * <p>
 * Classe Singleton per gestire la sessione dell'utente corrente nell'applicazione.
 * Permette di condividere i dati dell'utente loggato tra i vari controller.
 * </p>
 * @author Stefano Bellan 20054330
 */
public class UserSession {

    private static UserSession instance;
    private final Utente utente;

    // Costruttore privato per il pattern Singleton
    private UserSession(Utente utente) {
        this.utente = utente;
    }

    /**
     * Inizializza (o sovrascrive) la sessione con un nuovo utente.
     * Da chiamare al momento del login.
     *
     * @param utente L'utente che ha effettuato l'accesso.
     * @return L'istanza della sessione.
     */
    public static UserSession getInstance(Utente utente) {
        if (instance == null) {
            instance = new UserSession(utente);
        }
        return instance;
    }

    /**
     * Restituisce l'istanza corrente della sessione.
     * Da chiamare nei controller delle dashboard per recuperare i dati.
     *
     * @return L'istanza corrente o null se nessun utente è loggato.
     */
    public static UserSession getInstance() {
        return instance;
    }

    /**
     * Resetta la sessione (Logout).
     */
    public static void cleanUserSession() {
        instance = null;
    }

    /**
     * @return L'oggetto Utente memorizzato nella sessione.
     */
    public Utente getUtente() {
        return utente;
    }
}