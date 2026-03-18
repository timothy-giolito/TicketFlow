package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Rappresenta uno <strong>Spettacolo</strong> programmato all'interno del sistema.
 * <p>
 * Questa classe è il cuore della programmazione: unisce un {@link Film},
 * una {@link Sala} e un orario specifico ({@link LocalDateTime}).
 * Include il prezzo base del biglietto per questa specifica proiezione e una locandina opzionale.
 * Mappa la tabella 'Spettacolo' del database.
 * @author Stefano Bellan 200543330
 */
public class Spettacolo {

    private int id;
    private Film film;
    private Sala sala;
    private LocalDateTime dataOra;
    private double prezzoBase;
    private String urlLocandina; // Percorso della locandina specifica per lo spettacolo

    /**
     * Costruisce una nuova istanza di Spettacolo con validazione immediata dei dati.
     * Costruttore completo con locandina personalizzata.
     *
     * @param film       L'oggetto Film da proiettare (non può essere null).
     * @param sala       La Sala dove avverrà la proiezione (non può essere null).
     * @param dataOra    La data e l'ora dell'inizio dello spettacolo.
     * @param prezzoBase Il prezzo base del biglietto (deve essere positivo).
     * @param urlLocandina URL o percorso della locandina specifica (opzionale).
     * @throws DatiNonValidiException Se uno dei parametri è nullo o il prezzo è invalido.
     */
    public Spettacolo(Film film, Sala sala, LocalDateTime dataOra, double prezzoBase, String urlLocandina) throws DatiNonValidiException {
        if (film == null) {
            throw new DatiNonValidiException("Lo spettacolo deve essere associato a un film valido.");
        }
        if (sala == null) {
            throw new DatiNonValidiException("Lo spettacolo deve essere associato a una sala valida.");
        }
        if (dataOra == null) {
            throw new DatiNonValidiException("La data e l'ora dello spettacolo sono obbligatorie.");
        }
        if (prezzoBase <= 0) {
            throw new DatiNonValidiException("Il prezzo base dello spettacolo deve essere positivo.");
        }

        this.film = film;
        this.sala = sala;
        this.dataOra = dataOra;
        this.prezzoBase = prezzoBase;
        this.urlLocandina = urlLocandina;
    }

    /**
     * Costruttore semplificato (senza locandina specifica).
     * Utile per mantenere compatibilità con codice esistente.
     */
    public Spettacolo(Film film, Sala sala, LocalDateTime dataOra, double prezzoBase) throws DatiNonValidiException {
        this(film, sala, dataOra, prezzoBase, null);
    }

    // ----------------- GETTER E SETTER -----------------

    /**
     * Restituisce l'identificativo univoco dello spettacolo.
     * @return l'ID numerico.
     */
    public int getId() {
        return id;
    }

    /**
     * Imposta l'ID dello spettacolo.
     * Usato dopo il caricamento dal database o l'inserimento.
     * @param id il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce il film programmato.
     * @return l'oggetto Film completo.
     */
    public Film getFilm() {
        return film;
    }

    /**
     * Cambia il film dello spettacolo.
     * @param film il nuovo film.
     * @throws DatiNonValidiException se il film è null.
     */
    public void setFilm(Film film) throws DatiNonValidiException {
        if (film == null) {
            throw new DatiNonValidiException("Il film non può essere nullo.");
        }
        this.film = film;
    }

    /**
     * Restituisce la sala dove avviene la proiezione.
     * @return l'oggetto Sala completo.
     */
    public Sala getSala() {
        return sala;
    }

    /**
     * Cambia la sala dello spettacolo.
     * @param sala la nuova sala.
     * @throws DatiNonValidiException se la sala è null.
     */
    public void setSala(Sala sala) throws DatiNonValidiException {
        if (sala == null) {
            throw new DatiNonValidiException("La sala non può essere nulla.");
        }
        this.sala = sala;
    }

    /**
     * Restituisce la data e l'ora di inizio.
     * @return oggetto LocalDateTime.
     */
    public LocalDateTime getDataOra() {
        return dataOra;
    }

    /**
     * Imposta una nuova data e ora per lo spettacolo.
     * @param dataOra la nuova data/ora.
     * @throws DatiNonValidiException se il parametro è null.
     */
    public void setDataOra(LocalDateTime dataOra) throws DatiNonValidiException {
        if (dataOra == null) {
            throw new DatiNonValidiException("La data e l'ora non possono essere nulle.");
        }
        this.dataOra = dataOra;
    }

    /**
     * Restituisce il prezzo base del biglietto.
     * @return il prezzo.
     */
    public double getPrezzoBase() {
        return prezzoBase;
    }

    /**
     * Imposta il prezzo base del biglietto.
     * @param prezzoBase il nuovo prezzo.
     * @throws DatiNonValidiException se il prezzo è <= 0.
     */
    public void setPrezzoBase(double prezzoBase) throws DatiNonValidiException {
        if (prezzoBase <= 0) {
            throw new DatiNonValidiException("Il prezzo deve essere positivo.");
        }
        this.prezzoBase = prezzoBase;
    }

    /**
     * Restituisce l'URL della locandina specifica per questo spettacolo.
     * @return URL come stringa o null se non presente.
     */
    public String getUrlLocandina() {
        return urlLocandina;
    }

    /**
     * Imposta l'URL della locandina specifica.
     * @param urlLocandina percorso o URL dell'immagine.
     */
    public void setUrlLocandina(String urlLocandina) {
        this.urlLocandina = urlLocandina;
    }

    /**
     * Fornisce una descrizione testuale leggibile dello spettacolo.
     * Utile per le liste nell'interfaccia utente.
     * @return Stringa formattata con Titolo, Sala, Data e Prezzo.
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return film.getTitolo() +
                " | Sala: " + sala.getNome() +
                " | Data: " + dataOra.format(formatter) +
                " | Prezzo: " + String.format("%.2f", prezzoBase) + "€";
    }
}