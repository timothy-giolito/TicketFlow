package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.utils.SecurityUtils;

import java.util.regex.Pattern;

/**
 * Rappresenta un utente del sistema TicketFlow.
 * <p>
 * Questa classe modella le informazioni personali, le credenziali di accesso
 * e i dati di fatturazione (opzionali fino al primo acquisto).
 * </p>
 * @author Stefano Bellan 20054330
 */
public class Utente {

    private int id;
    private String nome;
    private String cognome;
    private String email;
    private String password; // Contiene l'HASH della password
    private Ruolo ruolo;
    private int eta;

    // Dati aggiuntivi per fatturazione (Opzionali in fase di registrazione)
    private String indirizzo;
    private String numeroCarta;
    private String scadenzaCarta;
    private String cvv;

    private int idCinema; // Usato solo se ruolo == MANAGER

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    /**
     * Costruttore BASE per la Registrazione.
     * Inizializza l'utente solo con i dati anagrafici essenziali.
     *
     * @param nome     Nome dell'utente.
     * @param cognome  Cognome dell'utente.
     * @param email    Email univoca.
     * @param password Password (già hashata).
     * @param ruolo    Ruolo nel sistema.
     * @param eta      Età anagrafica.
     */
    public Utente(String nome, String cognome, String email, String password, Ruolo ruolo, int eta) {
        if (nome == null || nome.isBlank()) throw new DatiNonValidiException("Il nome non può essere vuoto.");
        if (cognome == null || cognome.isBlank()) throw new DatiNonValidiException("Il cognome non può essere vuoto.");
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) throw new DatiNonValidiException("Formato email non valido.");
        if (eta < 0 || eta > 120) throw new DatiNonValidiException("Età non valida.");
        if (password == null || password.isBlank()) throw new DatiNonValidiException("La password (hash) è mancante.");

        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.password = password;
        this.ruolo = ruolo;
        this.eta = eta;
    }

    /**
     * Costruttore COMPLETO per il caricamento dal Database o aggiornamenti.
     * Supporta tutti i campi, inclusi quelli di fatturazione.
     */
    public Utente(String nome, String cognome, String email, String password, Ruolo ruolo, int eta,
                  String indirizzo, String numeroCarta, String scadenzaCarta, String cvv) {
        this(nome, cognome, email, password, ruolo, eta); // Richiama il costruttore base per le validazioni comuni
        this.indirizzo = indirizzo;
        this.numeroCarta = numeroCarta;
        this.scadenzaCarta = scadenzaCarta;
        this.cvv = cvv;
    }

    // --- Metodi di Business ---

    /**
     * Verifica se la password fornita in input corrisponde a quella salvata.
     */
    public boolean checkPassword(String inputPassword) {
        if (inputPassword == null) return false;
        return SecurityUtils.verificaPassword(inputPassword, this.password);
    }

    /**
     * Verifica se l'utente ha già salvato un metodo di pagamento valido.
     */
    public boolean haDatiPagamento() {
        return numeroCarta != null && !numeroCarta.isBlank() &&
                cvv != null && !cvv.isBlank();
    }

    // --- Getter e Setter ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String passwordHash) { this.password = passwordHash; }

    public Ruolo getRuolo() { return ruolo; }
    public void setRuolo(Ruolo ruolo) { this.ruolo = ruolo; }

    public int getEta() { return eta; }
    public void setEta(int eta) { this.eta = eta; }

    public String getIndirizzo() { return indirizzo; }
    public void setIndirizzo(String indirizzo) { this.indirizzo = indirizzo; }

    public String getNumeroCarta() { return numeroCarta; }
    public void setNumeroCarta(String numeroCarta) { this.numeroCarta = numeroCarta; }

    public String getScadenzaCarta() { return scadenzaCarta; }
    public void setScadenzaCarta(String scadenzaCarta) { this.scadenzaCarta = scadenzaCarta; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public int getIdCinema() { return idCinema; }
    public void setIdCinema(int idCinema) { this.idCinema = idCinema; }

    @Override
    public String toString() {
        return "Utente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}