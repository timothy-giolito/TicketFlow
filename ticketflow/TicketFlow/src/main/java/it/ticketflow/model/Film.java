package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * Rappresenta un contenuto cinematografico presente nel catalogo.
 * Include metadati descrittivi (titolo, genere, trama) e risorse multimediali (locandina).
 * @author Stefano Bellan 200543330
 */
public class Film {
    private int id;
    private String titolo;
    private String genere;
    private int durataMinuti;
    private String descrizione;
    private String urlLocandina;

    /**
     * Costruttore completo per oggetti recuperati dal database.
     *
     * @param id Identificativo univoco.
     * @param durataMinuti Durata in minuti.
     * @param titolo Titolo del film.
     * @param descrizione Trama o descrizione breve.
     * @param genere Categoria del film.
     * @throws DatiNonValidiException Se titolo o durata non sono validi.
     */
    public Film(int id, int durataMinuti, String titolo, String descrizione, String genere) throws DatiNonValidiException {
        if (titolo == null || titolo.isBlank()) throw new DatiNonValidiException("Il titolo non può essere vuoto");
        if (durataMinuti <= 0) throw new DatiNonValidiException("La durata deve essere positiva");

        this.id = id;
        this.durataMinuti = durataMinuti;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.genere = genere;
    }

    /**
     * Costruttore per la creazione di nuovi film (senza ID preesistente).
     */
    public Film(int durataMinuti, String titolo, String descrizione, String genere) throws DatiNonValidiException {
        this(0, durataMinuti, titolo, descrizione, genere);
    }

    // --- GETTERS & SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitolo() { return titolo; }

    public String getGenere() { return genere; }

    public int getDurataMinuti() { return durataMinuti; }

    public String getDescrizione() { return descrizione; }

    /**
     * Restituisce l'URL o il percorso relativo dell'immagine di copertina.
     */
    public String getUrlLocandina() { return urlLocandina; }

    /**
     * Imposta il percorso dell'immagine di copertina.
     */
    public void setUrlLocandina(String urlLocandina) { this.urlLocandina = urlLocandina; }
}