package it.ticketflow.model.pricing;

import it.ticketflow.model.Utente;
import it.ticketflow.model.Spettacolo;

/**
 * Implementazione della strategia di prezzo riservata agli studenti e ai giovani.
 * <p>
 * Questa classe applica uno sconto percentuale sul prezzo base dello spettacolo
 * se l'utente che effettua l'acquisto soddisfa i requisiti di età (es. under 25).
 */
public class ScontoStudenti implements CalcoloPrezzo {

    /** * Il moltiplicatore da applicare al prezzo base per ottenere il prezzo scontato.
     * 0.80 corrisponde a uno sconto del 20%.
     */
    private static final double MOLTIPLICATORE_SCONTO = 0.80;

    /** * La soglia di età massima (esclusa) per usufruire dello sconto studenti.
     */
    private static final int SOGLIA_ETA = 25;

    /**
     * Calcola il prezzo finale del biglietto applicando la logica dello sconto studenti.
     * <p>
     * Il metodo verifica l'età dell'utente: se è inferiore alla soglia stabilita (25 anni),
     * viene applicato lo sconto. In caso contrario, o se l'utente non è definito,
     * viene restituito il prezzo pieno.
     *
     * @param s Lo spettacolo per cui si sta calcolando il prezzo.
     * @param u L'utente che sta effettuando l'acquisto.
     * @return Il prezzo calcolato (scontato o intero).
     */
    @Override
    public double calcola(Spettacolo s, Utente u) {
        // Controllo difensivo: se lo spettacolo è nullo, il prezzo è 0.
        if (s == null) {
            return 0.0;
        }

        double prezzoBase = s.getPrezzoBase();

        // Se l'utente è nullo, non possiamo verificare l'età, quindi niente sconto.
        if (u == null) {
            return prezzoBase;
        }

        // Verifica il requisito di età (minore di 25 anni)
        if (u.getEta() < SOGLIA_ETA) {
            // Applica lo sconto del 20%
            return prezzoBase * MOLTIPLICATORE_SCONTO;
        } else {
            // L'utente non ha i requisiti, paga il prezzo pieno
            return prezzoBase;
        }
    }
}