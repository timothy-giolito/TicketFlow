package it.ticketflow.service;

import it.ticketflow.model.Biglietto;

/**
 * Servizio responsabile della gestione delle notifiche inviate agli utenti.
 * <p>
 * Questa classe simula l'invio di email di conferma per le operazioni effettuate,
 * come l'acquisto di biglietti.
 * </p>
 */
public class NotificationService {

    /**
     * Simula l'invio di un'email di conferma acquisto con i dettagli del biglietto.
     *
     * @param email L'indirizzo email del destinatario.
     * @param b     L'oggetto Biglietto contenente i dati della prenotazione.
     */
    public void inviaEmail(String email, Biglietto b) {
        // Intestazione della simulazione
        System.out.println("\n------------------------------------------------------------");
        System.out.println(" [SIMULAZIONE INVIO EMAIL] ");
        System.out.println("------------------------------------------------------------");

        // Header dell'email
        System.out.println(" A: " + email);
        System.out.println(" Oggetto: Conferma Acquisto Biglietto TicketFlow");
        System.out.println("------------------------------------------------------------");

        // Corpo del messaggio
        System.out.println(" Gentile Cliente,");
        System.out.println("\n Grazie per aver scelto TicketFlow. Ecco il riepilogo del tuo acquisto:");
        System.out.println("");

        // Dettagli dello spettacolo e del posto
        // Nota: Assumiamo che l'oggetto Spettacolo abbia un metodo toString() leggibile.
        System.out.println(" Spettacolo: " + (b.getSpettacolo() != null ? b.getSpettacolo().toString() : "N/D"));
        System.out.println(" Fila: " + b.getFila() + " | Posto: " + b.getColonna());
        System.out.println(" Prezzo Pagato: € " + String.format("%.2f", b.getPrezzo()));
        System.out.println("");

        // Codice QR e data emissione
        System.out.println(" CODICE QR: " + b.getCodiceQr());
        System.out.println(" Data Emissione: " + b.getDataEmissione());

        // Chiusura
        System.out.println("\n Mostra questo codice all'ingresso. Buona visione!");
        System.out.println("------------------------------------------------------------\n");
    }
}