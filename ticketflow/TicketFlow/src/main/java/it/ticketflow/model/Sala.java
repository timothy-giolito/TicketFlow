package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * Rappresenta una sala fisica all'interno di un Cinema.
 * Gestisce la configurazione dei posti (righe x colonne) e la capienza totale.
 * @author Stefano Bellan 200543330
 */
public class Sala {
    private int id;
    private Cinema cinema;
    private String nome;
    private int righe;
    private int colonne;
    private int capienza;
    private String tipoSala; // Es. "IMAX", "Standard", "VIP"

    /**
     * Costruttore per definire una nuova sala.
     * Calcola automaticamente la capienza iniziale basandosi sulle dimensioni fornite.
     *
     * @param cinema Il cinema di appartenenza.
     * @param nome Identificativo della sala (es. "Sala 1").
     * @param righe Numero di file di poltrone.
     * @param colonne Numero di poltrone per fila.
     * @param tipoSala Tipologia tecnologica o di comfort della sala.
     * @throws DatiNonValidiException Se le dimensioni sono nulle o negative.
     */
    public Sala(Cinema cinema, String nome, int righe, int colonne, String tipoSala) throws DatiNonValidiException {
        if (cinema == null) throw new DatiNonValidiException("Il cinema di appartenenza è obbligatorio");
        if (nome == null || nome.isBlank()) throw new DatiNonValidiException("Il nome della sala è obbligatorio");
        if (righe <= 0 || colonne <= 0) throw new DatiNonValidiException("Le dimensioni della sala (righe/colonne) devono essere positive");

        this.cinema = cinema;
        this.nome = nome;
        this.righe = righe;
        this.colonne = colonne;
        this.tipoSala = tipoSala;

        // Calcolo della capienza logica basata sulla griglia
        this.capienza = righe * colonne;
    }

    // --- GETTERS & SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Cinema getCinema() { return cinema; }

    public String getNome() { return nome; }

    public int getRighe() { return righe; }

    public int getColonne() { return colonne; }

    public String getTipoSala() { return tipoSala; }

    /**
     * Restituisce la capienza totale della sala.
     * Questo valore è generalmente persistito nel DB per query rapide.
     */
    public int getCapienza() { return capienza; }

    /**
     * Imposta la capienza totale.
     * Usato principalmente dal DAO in fase di lettura dati.
     */
    public void setCapienza(int capienza) { this.capienza = capienza; }
}