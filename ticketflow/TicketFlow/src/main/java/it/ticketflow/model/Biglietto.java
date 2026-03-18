package it.ticketflow.model;

import java.time.LocalDateTime;
import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * Rappresenta un titolo di ingresso venduto a un utente per uno specifico spettacolo.
 * <p>
 * Questa classe contiene le informazioni essenziali per l'accesso alla sala (fila, colonna, QR Code)
 * e i dettagli della transazione (prezzo, data di emissione).
 * @author Stefano Bellan 200543330
 */
public class Biglietto {
    private int id;
    private Utente utente;
    private Spettacolo spettacolo;
    private int fila;
    private int colonna;
    private double prezzo;

    // Campi per la validazione e tracciamento
    private String codiceQr;
    private LocalDateTime dataEmissione;

    /**
     * Costruttore principale per la creazione di un nuovo biglietto.
     * Inizializza automaticamente la data di emissione al momento corrente e genera un QR temporaneo.
     *
     * @param utente L'utente che acquista il biglietto.
     * @param spettacolo Lo spettacolo prenotato.
     * @param fila Il numero della fila.
     * @param colonna Il numero del posto (colonna).
     * @param prezzo Il prezzo finale pagato.
     * @throws DatiNonValidiException Se i parametri obbligatori sono nulli o i valori numerici non validi.
     */
    public Biglietto(Utente utente, Spettacolo spettacolo, int fila, int colonna, double prezzo) throws DatiNonValidiException {
        validaDati(utente, spettacolo, fila, colonna, prezzo);

        this.utente = utente;
        this.spettacolo = spettacolo;
        this.fila = fila;
        this.colonna = colonna;
        this.prezzo = prezzo;

        // Impostazioni di default per nuovi biglietti
        this.dataEmissione = LocalDateTime.now();
        this.codiceQr = generaQrTemporaneo();
    }

    /**
     * Genera una stringa segnaposto per il codice QR.
     * In un sistema reale, questo verrebbe sostituito da un algoritmo di crittografia o hash.
     */
    private String generaQrTemporaneo() {
        return "TKT-" + System.currentTimeMillis() + "-" + fila + "-" + colonna;
    }

    /**
     * Esegue la validazione formale dei dati in ingresso.
     */
    private void validaDati(Utente u, Spettacolo s, int f, int c, double p) throws DatiNonValidiException {
        if (u == null || s == null) {
            throw new DatiNonValidiException("Utente e Spettacolo sono campi obbligatori.");
        }
        if (f < 1 || c < 1) {
            throw new DatiNonValidiException("Fila e Colonna devono essere maggiori di 0.");
        }
        if (p < 0) {
            throw new DatiNonValidiException("Il prezzo non può essere negativo.");
        }
    }

    // --- GETTERS & SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Utente getUtente() { return utente; }

    public Spettacolo getSpettacolo() { return spettacolo; }

    public int getFila() { return fila; }

    public int getColonna() { return colonna; }

    public double getPrezzo() { return prezzo; }
    public void setPrezzo(double prezzo) { this.prezzo = prezzo; }

    /**
     * Restituisce il codice univoco per la validazione del biglietto.
     */
    public String getCodiceQr() { return codiceQr; }

    /**
     * Imposta il codice QR (usato solitamente in fase di caricamento dal DB).
     */
    public void setCodiceQr(String codiceQr) { this.codiceQr = codiceQr; }

    public LocalDateTime getDataEmissione() { return dataEmissione; }
    public void setDataEmissione(LocalDateTime dataEmissione) { this.dataEmissione = dataEmissione; }
}