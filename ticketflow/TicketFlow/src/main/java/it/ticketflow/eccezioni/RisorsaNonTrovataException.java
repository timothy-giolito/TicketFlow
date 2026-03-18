package it.ticketflow.eccezioni;

/**
 * Eccezione lanciata quando una risorsa richiesta (es. Film, Sala, Utente)
 * non viene trovata nel database.
 */
public class RisorsaNonTrovataException extends Exception {

    /**
     * Costruttore senza argomenti.
     * Imposta un messaggio di errore generico.
     */
    public RisorsaNonTrovataException() {
        super("La risorsa richiesta non è stata trovata nel sistema.");
    }

    /**
     * Costruttore con messaggio personalizzato.
     * @param message Il messaggio che descrive l'errore specifico (es. "Film con ID 5 non trovato").
     */
    public RisorsaNonTrovataException(String message) {
        super(message);
    }
}