package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;

/**
 * Rappresenta un <strong>Cinema</strong> fisico all'interno della catena TicketFlow.
 * <p>
 * Questa classe funge da anagrafica principale. Identifica la struttura
 * tramite nome e collocazione geografica.
 * @author Stefano Bellan 200543330
 * </p>
 */
public class Cinema {

    // Identificativo univoco del cinema nel database (Primary Key).
    private int id;

    // Il nome commerciale del cinema (es. "Multisala Vercelli").
    private String nome;

    // L'indirizzo fisico (Via/Piazza).
    private String indirizzo;

    // La città di appartenenza. Utile per future funzionalità di filtro per località.
    private String citta;


    /**
     * Costruisce una nuova istanza di Cinema con validazione dei dati.
     *
     * @param id        L'ID del cinema (se nuovo inserimento, verrà sovrascritto dal DB).
     * @param nome      Il nome della struttura (obbligatorio).
     * @param indirizzo L'indirizzo fisico (obbligatorio).
     * @param citta     La città (obbligatoria).
     * @throws DatiNonValidiException se uno dei campi testuali è vuoto o nullo.
     */
    public Cinema(int id, String nome, String indirizzo, String citta) throws DatiNonValidiException {
        // Verifico che le stringhe non siano null o composte solo da spazi bianchi.
        if (nome == null || nome.isBlank()) {
            throw new DatiNonValidiException("Il nome del cinema è obbligatorio.");
        }
        if (indirizzo == null || indirizzo.isBlank()) {
            throw new DatiNonValidiException("L'indirizzo del cinema è obbligatorio.");
        }
        if (citta == null || citta.isBlank()) {
            throw new DatiNonValidiException("La città del cinema è obbligatoria.");
        }

        this.id = id;
        this.nome = nome;
        this.indirizzo = indirizzo;
        this.citta = citta;
    }

    // ----------------- GETTER -----------------

    /**
     * Restituisce l'ID del cinema.
     * @return l'identificativo intero.
     */
    public int getId() {
        return id;
    }

    /**
     * Restituisce il nome del cinema.
     * @return il nome.
     */
    public String getNome() {
        return nome;
    }

    /**
     * Restituisce l'indirizzo.
     * @return la via o piazza.
     */
    public String getIndirizzo() {
        return indirizzo;
    }

    /**
     * Restituisce la città.
     * @return la città.
     */
    public String getCitta() {
        return citta;
    }

    // ----------------- SETTER -----------------

    /**
     * Imposta l'ID del cinema.
     * @param id il nuovo ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Modifica il nome del cinema.
     * @param nome il nuovo nome.
     * @throws DatiNonValidiException se il nuovo nome è vuoto.
     */
    public void setNome(String nome) throws DatiNonValidiException {
        // Controllo di integrità: non permetto di "cancellare" il nome per sbaglio.
        if (nome == null || nome.isBlank()) {
            throw new DatiNonValidiException("Il nome del cinema non può essere vuoto.");
        }
        this.nome = nome;
    }

    /**
     * Modifica l'indirizzo.
     * @param indirizzo il nuovo indirizzo.
     * @throws DatiNonValidiException se il nuovo indirizzo è vuoto.
     */
    public void setIndirizzo(String indirizzo) throws DatiNonValidiException {
        if (indirizzo == null || indirizzo.isBlank()) {
            throw new DatiNonValidiException("L'indirizzo non può essere vuoto.");
        }
        this.indirizzo = indirizzo;
    }

    /**
     * Modifica la città.
     * @param citta la nuova città.
     * @throws DatiNonValidiException se la nuova città è vuota.
     */
    public void setCitta(String citta) throws DatiNonValidiException {
        if (citta == null || citta.isBlank()) {
            throw new DatiNonValidiException("La città non può essere vuota.");
        }
        this.citta = citta;
    }


    /**
     * Restituisce una rappresentazione testuale elegante del Cinema.
     * Fondamentale per le ComboBox (menu a tendina) nell'interfaccia JavaFX.
     * Invece di mostrare l'indirizzo di memoria, mostrerà "Nome - Città".
     *
     * @return Stringa formattata (es. "Cinema Odeon - Milano").
     */
    @Override
    public String toString() {
        return nome + " - " + citta;
    }
}