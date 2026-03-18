package it.ticketflow.service;

import it.ticketflow.dao.DaoFactory;
import it.ticketflow.dao.FilmDAO;
import it.ticketflow.dao.SalaDAO;
import it.ticketflow.dao.SpettacoloDAO;
import it.ticketflow.dao.UtenteDAO;
import it.ticketflow.eccezioni.DatabaseException;
import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.eccezioni.PostoOccupatoException;
import it.ticketflow.eccezioni.RisorsaNonTrovataException;
import it.ticketflow.eccezioni.SalaNonDisponibileException;
import it.ticketflow.eccezioni.UtenteNonAutorizzatoException;
import it.ticketflow.model.Biglietto;
import it.ticketflow.model.Film;
import it.ticketflow.model.Sala;
import it.ticketflow.model.Spettacolo;
import it.ticketflow.model.Utente;
import it.ticketflow.model.pricing.CalcoloPrezzo;
import it.ticketflow.model.pricing.ScontoStudenti;
import it.ticketflow.service.payment.PagamentoMock;
import it.ticketflow.service.payment.ServizioPagamento;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe Facade per la logica di business.
 * Contiene l'implementazione della Logica Manager (Stefano), la gestione acquisti (Luca)
 * e i metodi base di ricerca (Timothy).
 *
 * @author Timohthy Giolito 20054431
 */
public class TicketFlowService {

    // =================================================================================
    // METODI BASE (Infrastruttura comune)
    // =================================================================================

    /**
     * Esegue il login dell'utente verificando le credenziali nel database.
     *
     * @param email L'email dell'utente.
     * @param password La password in criptata.
     * @return L'oggetto Utente loggato.
     * @throws UtenteNonAutorizzatoException Se le credenziali sono errate.
     * @throws DatabaseException Se c'è un errore di connessione.
     */
    public Utente login(String email, String password) throws UtenteNonAutorizzatoException, DatabaseException {
        try {
            UtenteDAO utenteDAO = DaoFactory.getUtenteDAO();
            Utente utente = utenteDAO.findByEmail(email);

            if (utente == null || !utente.checkPassword(password)) {
                throw new UtenteNonAutorizzatoException("Email o password non validi.");
            }
            return utente;

        } catch (SQLException e) {
            // Catch specifico per errori SQL
            throw new DatabaseException("Errore database durante il login", e);

        } catch (UtenteNonAutorizzatoException e) {
            // FIX: Questa eccezione deve essere rilanciata così com'è, senza avvolgerla in DatabaseException
            throw e;

        } catch (Exception e) {
            // Catch generico per qualsiasi altro errore imprevisto (es. NullPointerException)
            throw new DatabaseException("Errore imprevisto durante il login", e);
        }
    }

    // =================================================================================
    // 🔍 SEZIONE TIMOTHY: RICERCA SPETTACOLI
    // =================================================================================


    // Alias per compatibilità con eventuali controller che chiamano 'getSpettacoli'
    public List<Spettacolo> getSpettacoli() throws DatabaseException {
        return getTuttiSpettacoli();
    }

    /**
     * Cerca spettacoli filtrando per Titolo del film o Genere.
     * Implementa la logica di ricerca richiesta per la UserDashboard.
     *
     * @param query La stringa di ricerca (es. "Avatar" o "Action"). Se null o vuota, restituisce tutto.
     * @return Lista di spettacoli filtrata.
     */
    public List<Spettacolo> cercaSpettacoli(String query) throws DatabaseException {
        // 1. Recuperiamo tutti gli spettacoli dal DB
        SpettacoloDAO spettacoloDAO = DaoFactory.getSpettacoloDAO();
        List<Spettacolo> tuttiSpettacoli = spettacoloDAO.findAll();

        // 2. Se la query è vuota, restituiamo tutto
        if (query == null || query.isBlank()) {
            return tuttiSpettacoli;
        }

        // 3. Altrimenti, filtriamo la lista in memoria (Java Stream API)
        // Convertiamo tutto in minuscolo per una ricerca case-insensitive
        String filtro = query.toLowerCase();

        return tuttiSpettacoli.stream()
                .filter(s -> s.getFilm().getTitolo().toLowerCase().contains(filtro) ||
                        s.getFilm().getGenere().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }


    // =================================================================================
    // LOGICA MANAGER (A cura di Stefano)
    // =================================================================================

    // --- RICERCA SPETTACOLI ---

    public List<Spettacolo> getTuttiSpettacoli() throws DatabaseException {
        return DaoFactory.getSpettacoloDAO().findAll();
    }

    /**
     * Crea un nuovo spettacolo verificando la disponibilità della sala.
     * Aggiornato per gestire la locandina specifica dello spettacolo.
     */
    public void creaSpettacolo(int idFilm, int idSala, LocalDateTime dataOraInizio, double prezzoBase, String urlLocandina)
            throws DatabaseException, DatiNonValidiException, RisorsaNonTrovataException, SalaNonDisponibileException {

        try {
            FilmDAO filmDAO = DaoFactory.getFilmDAO();
            SalaDAO salaDAO = DaoFactory.getSalaDAO();
            SpettacoloDAO spettacoloDAO = DaoFactory.getSpettacoloDAO();

            Film film = filmDAO.trovaPerId(idFilm);
            if (film == null) {
                throw new RisorsaNonTrovataException("Film non trovato con ID: " + idFilm);
            }

            Sala sala = salaDAO.trovaPerId(idSala);
            if (sala == null) {
                throw new RisorsaNonTrovataException("Sala non trovata con ID: " + idSala);
            }

            int durataTotaleMinuti = film.getDurataMinuti() + 20;
            LocalDateTime dataOraFine = dataOraInizio.plusMinutes(durataTotaleMinuti);

            // Lettura: recupera lista (connessione apre e chiude)
            List<Spettacolo> tuttiGliSpettacoli = spettacoloDAO.findAll();

            for (Spettacolo s : tuttiGliSpettacoli) {
                if (s.getSala().getId() == idSala) {
                    LocalDateTime inizioEsistente = s.getDataOra();
                    LocalDateTime fineEsistente = inizioEsistente.plusMinutes(s.getFilm().getDurataMinuti() + 20);

                    boolean sovrapposizione = dataOraInizio.isBefore(fineEsistente) && dataOraFine.isAfter(inizioEsistente);

                    if (sovrapposizione) {
                        throw new SalaNonDisponibileException(
                                "La sala è occupata dalle " + inizioEsistente.toLocalTime() +
                                        " alle " + fineEsistente.toLocalTime()
                        );
                    }
                }
            }

            // Scrittura: apre nuova connessione per inserimento
            Spettacolo nuovoSpettacolo = new Spettacolo(film, sala, dataOraInizio, prezzoBase, urlLocandina);
            spettacoloDAO.create(nuovoSpettacolo);

        } catch (SQLException e) {
            throw new DatabaseException("Errore nel database durante la creazione dello spettacolo", e);
        }
    }

    /**
     * Overload per retrocompatibilità.
     */
    public void creaSpettacolo(int idFilm, int idSala, LocalDateTime dataOraInizio, double prezzoBase)
            throws DatabaseException, DatiNonValidiException, RisorsaNonTrovataException, SalaNonDisponibileException {
        creaSpettacolo(idFilm, idSala, dataOraInizio, prezzoBase, null);
    }

    public void creaSala(Sala sala) throws DatabaseException, DatiNonValidiException {
        try {
            SalaDAO salaDAO = DaoFactory.getSalaDAO();
            if (sala == null) throw new DatiNonValidiException("Sala nulla.");
            if (sala.getCinema() == null) throw new DatiNonValidiException("Cinema mancante.");
            salaDAO.inserisci(sala);
        } catch (SQLException e) {
            throw new DatabaseException("Errore creazione sala.", e);
        }
    }

    /**
     * Modifica uno spettacolo esistente controllando sovrapposizioni.
     */
    public void modificaSpettacolo(Spettacolo s) throws DatabaseException, SalaNonDisponibileException {
        // Controlla sovrapposizioni escludendo l'ID dello spettacolo stesso
        controllaSovrapposizione(s.getSala().getId(), s.getDataOra(), s.getFilm().getDurataMinuti(), s.getId());
        DaoFactory.getSpettacoloDAO().update(s);
    }

    private void controllaSovrapposizione(int idSala, LocalDateTime start, int durata, int excludeId) throws SalaNonDisponibileException {
        LocalDateTime end = start.plusMinutes(durata + 20); // 20 min pulizia
        List<Spettacolo> existing = DaoFactory.getSpettacoloDAO().findAll();

        for(Spettacolo es : existing) {
            if(es.getId() == excludeId) continue; // Salta se stesso durante la modifica

            if(es.getSala().getId() == idSala) {
                LocalDateTime esStart = es.getDataOra();
                LocalDateTime esEnd = esStart.plusMinutes(es.getFilm().getDurataMinuti() + 20);

                // Logica di sovrapposizione temporale
                if(start.isBefore(esEnd) && end.isAfter(esStart)) {
                    throw new SalaNonDisponibileException("Sala occupata dalle " + esStart.toLocalTime() + " alle " + esEnd.toLocalTime());
                }
            }
        }
    }

    // =================================================================================
    // 👨‍💻 SPAZIO LUCA: Booking, Payments e Registrazione
    // =================================================================================

    /**
     * Gestisce l'acquisto di un biglietto.
     * Include il calcolo del prezzo dinamico e il processo di pagamento.
     *
     * @param utente L'utente che acquista.
     * @param spettacolo Lo spettacolo scelto.
     * @param riga La riga del posto.
     * @param colonna La colonna del posto.
     * @throws DatabaseException Errore generico di DB.
     * @throws DatiNonValidiException Errore validazione o pagamento fallito.
     * @throws PostoOccupatoException Se il posto non è più disponibile.
     */
    public void compraBiglietto(Utente utente, Spettacolo spettacolo, int riga, int colonna)
            throws DatabaseException, DatiNonValidiException, PostoOccupatoException {

        try {
            // 1. CALCOLO PREZZO
            // Utilizziamo la strategia ScontoStudenti implementando l'interfaccia CalcoloPrezzo
            CalcoloPrezzo strategiaPrezzo = new ScontoStudenti();
            double prezzoFinale = strategiaPrezzo.calcola(spettacolo, utente);

            // 2. PAGAMENTO
            // Istanziamo il servizio di pagamento (Mock)
            ServizioPagamento servizioPagamento = new PagamentoMock();

            // Simuliamo il numero di carta (in un'app reale verrebbe dall'input utente)
            String numeroCartaSimulato = "1234-5678-9012-3456";

            // Chiamiamo il metodo dell'istanza per elaborare il pagamento
            boolean pagamentoRiuscito = servizioPagamento.elaboraPagamento(numeroCartaSimulato, prezzoFinale);

            if (!pagamentoRiuscito) {
                throw new DatiNonValidiException("Pagamento rifiutato: fondi insufficienti o carta non valida.");
            }

            // 3. CREAZIONE BIGLIETTO
            // Creiamo l'oggetto biglietto utilizzando il costruttore completo (obbligatorio nel Model)
            Biglietto biglietto = new Biglietto(utente, spettacolo, riga, colonna, prezzoFinale);

            // 4. SALVATAGGIO
            // Il BigliettoDAO si occuperà della transazione SQL e verificherà se il posto è libero
            // Se il posto è occupato, il DAO lancerà PostoOccupatoException che propaghiamo
            DaoFactory.getBigliettoDAO().inserisci(biglietto);

        } catch (SQLException e) {
            // Se l'eccezione SQL non è stata convertita in PostoOccupatoException dal DAO,
            // la trattiamo come errore generico del database.
            throw new DatabaseException("Errore tecnico durante il salvataggio del biglietto.", e);
        }
    }

    /**
     * Registra un nuovo utente nel database.
     * Gestisce l'eccezione di duplicazione email controllando il messaggio o il codice errore.
     */
    public void registraUtente(Utente nuovoUtente) throws DatabaseException, DatiNonValidiException {
        try {
            DaoFactory.getUtenteDAO().inserisci(nuovoUtente);
        } catch (SQLException e) {
            // Controlla se l'errore è dovuto a un vincolo UNIQUE (email duplicata)
            // Supporta sia l'errore nativo SQLite (code 19) sia il messaggio personalizzato del DAO ("già registrato")
            boolean isUniqueConstraint = e.getMessage().contains("UNIQUE")
                    || e.getMessage().contains("già registrato")
                    || e.getErrorCode() == 19;

            if (isUniqueConstraint) {
                throw new DatiNonValidiException("Email già registrata.");
            }
            throw new DatabaseException("Errore registrazione", e);
        }
    }

    /**
     * Aggiorna i dati di pagamento di un utente.
     * Da chiamare PRIMA di processare un pagamento se i dati non sono presenti.
     */
    public void aggiornaDatiPagamento(Utente utente, String carta, String scadenza, String cvv)
            throws DatiNonValidiException, DatabaseException {

        // Validazione Formale
        if (carta == null || !carta.matches("\\d{16}")) {
            throw new DatiNonValidiException("Numero carta non valido (richieste 16 cifre).");
        }
        if (scadenza == null || scadenza.isBlank()) {
            throw new DatiNonValidiException("Scadenza carta obbligatoria.");
        }
        if (cvv == null || !cvv.matches("\\d{3}")) {
            throw new DatiNonValidiException("CVV non valido (richieste 3 cifre).");
        }

        // Aggiornamento Oggetto Utente
        utente.setNumeroCarta(carta);
        utente.setScadenzaCarta(scadenza);
        utente.setCvv(cvv);

        // Aggiornamento Database
        try {
            DaoFactory.getUtenteDAO().aggiornaDatiPagamento(utente);
        } catch (SQLException e) {
            throw new DatabaseException("Errore durante il salvataggio dei dati di pagamento.", e);
        }
    }

}