package it.ticketflow.service;

import it.ticketflow.dao.DaoFactory;
import it.ticketflow.db.DbConnection;
import it.ticketflow.db.SchemaInit;
import it.ticketflow.eccezioni.*;
import it.ticketflow.model.*;
import it.ticketflow.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test completa per la classe TicketFlowService.
 * <p>
 * Verifica le funzionalità core: Login, Registrazione, Gestione Spettacoli, Acquisti e Pagamenti.
 * Risolve i problemi di schema DB e ID disallineati resettando l'ambiente prima di ogni test.
 * </p>
 *
 * @author Stefano Bellan 20054330
 */
class TicketFlowServiceTest {

    private TicketFlowService service;

    /**
     * Configurazione ambiente di test.
     * Pulisce completamente il database eliminando le tabelle per forzare una ricreazione pulita.
     */
    @BeforeEach
    void setUp() throws SQLException {
        // 1. Attiva modalità test (usa ticketflow_test.db)
        DbConnection.setTestMode(true);
        service = new TicketFlowService();

        // 2. DROP delle tabelle per garantire uno schema pulito e aggiornato
        try (Connection conn = DbConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // Disabilita FK temporaneamente per evitare errori durante il drop
            stmt.executeUpdate("PRAGMA foreign_keys = OFF;");

            stmt.executeUpdate("DROP TABLE IF EXISTS Biglietto");
            stmt.executeUpdate("DROP TABLE IF EXISTS Spettacolo");
            stmt.executeUpdate("DROP TABLE IF EXISTS Film");
            stmt.executeUpdate("DROP TABLE IF EXISTS Sala");
            stmt.executeUpdate("DROP TABLE IF EXISTS Cinema");
            stmt.executeUpdate("DROP TABLE IF EXISTS Utente");

            stmt.executeUpdate("PRAGMA foreign_keys = ON;");
        }

        // 3. Inizializza schema
        SchemaInit.initializeDatabase();
    }

    // =================================================================================
    // TEST UTENTI E AUTENTICAZIONE
    // =================================================================================

    @Test
    @DisplayName("Registrazione Utente: Successo")
    void testRegistrazioneUtenteSuccesso() {
        // IMPORTANTE: Hashiamo la password prima di creare l'oggetto Utente, perché il modello si aspetta l'hash.
        String passwordPlain = "Pass123!";
        String passwordHash = SecurityUtils.hashPassword(passwordPlain);

        Utente u = new Utente("Mario", "Rossi", "mario@email.com", passwordHash, Ruolo.CLIENTE, 30);

        assertDoesNotThrow(() -> service.registraUtente(u));

        // Il login usa la password in chiaro, che verrà hashata internamente e confrontata con quella nel DB
        assertDoesNotThrow(() -> {
            Utente loggato = service.login("mario@email.com", passwordPlain);
            assertNotNull(loggato);
            assertEquals("Mario", loggato.getNome());
        });
    }

    @Test
    @DisplayName("Registrazione Utente: Fallimento Email Duplicata")
    void testRegistrazioneUtenteDuplicato() throws Exception {
        String passwordHash = SecurityUtils.hashPassword("Pass123!");

        Utente u1 = new Utente("Mario", "Rossi", "duplicato@email.com", passwordHash, Ruolo.CLIENTE, 30);
        service.registraUtente(u1);

        Utente u2 = new Utente("Luigi", "Verdi", "duplicato@email.com", passwordHash, Ruolo.CLIENTE, 25);

        // Verifica che venga lanciata l'eccezione corretta (DatiNonValidiException invece di DatabaseException)
        assertThrows(DatiNonValidiException.class, () -> service.registraUtente(u2),
                "Deve impedire la registrazione di email già esistenti.");
    }

    @Test
    @DisplayName("Login: Successo")
    void testLoginSuccesso() throws Exception {
        String passwordPlain = "AdminPass1!";
        String passwordHash = SecurityUtils.hashPassword(passwordPlain);

        Utente u = new Utente("Admin", "User", "admin@test.it", passwordHash, Ruolo.SUPER_ADMIN, 40);
        service.registraUtente(u);

        Utente loggato = service.login("admin@test.it", passwordPlain);
        assertEquals(Ruolo.SUPER_ADMIN, loggato.getRuolo());
    }

    @Test
    @DisplayName("Login: Fallimento Password Errata")
    void testLoginPasswordErrata() throws Exception {
        String passwordHash = SecurityUtils.hashPassword("UserPass1!");
        Utente u = new Utente("User", "Test", "user@test.it", passwordHash, Ruolo.CLIENTE, 20);
        service.registraUtente(u);

        assertThrows(UtenteNonAutorizzatoException.class,
                () -> service.login("user@test.it", "WrongPass!"));
    }

    // =================================================================================
    // TEST RICERCA (TIMOTHY)
    // =================================================================================

    @Test
    @DisplayName("Ricerca Spettacoli: Filtro per Titolo")
    void testCercaSpettacoli() throws Exception {
        Sala sala = prepareInfrastructure();

        Film f1 = new Film(0, 120, "Avatar", "Blu", "Sci-Fi");
        DaoFactory.getFilmDAO().inserisci(f1);

        Film f2 = new Film(0, 90, "Titanic", "Nave", "Romantico");
        DaoFactory.getFilmDAO().inserisci(f2);

        service.creaSpettacolo(f1.getId(), sala.getId(), LocalDateTime.now().plusHours(1), 10.0);
        service.creaSpettacolo(f2.getId(), sala.getId(), LocalDateTime.now().plusHours(5), 10.0);

        List<Spettacolo> risultati = service.cercaSpettacoli("Avatar");
        assertEquals(1, risultati.size());
        assertEquals("Avatar", risultati.get(0).getFilm().getTitolo());

        List<Spettacolo> tutti = service.cercaSpettacoli("");
        assertEquals(2, tutti.size());
    }

    // =================================================================================
    // TEST MANAGER (STEFANO)
    // =================================================================================

    @Test
    @DisplayName("Creazione Spettacolo: Successo")
    void testCreaSpettacoloSuccesso() throws Exception {
        Sala sala = prepareInfrastructure();

        Film f = new Film(0, 100, "Film Test", "Desc", "Genere");
        DaoFactory.getFilmDAO().inserisci(f);

        LocalDateTime data = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0);

        assertDoesNotThrow(() ->
                service.creaSpettacolo(f.getId(), sala.getId(), data, 12.50, "http://locandina.url")
        );

        List<Spettacolo> lista = service.getTuttiSpettacoli();
        assertFalse(lista.isEmpty());
        assertEquals(12.50, lista.get(0).getPrezzoBase());
    }

    @Test
    @DisplayName("Creazione Spettacolo: Fallimento Sovrapposizione")
    void testCreaSpettacoloSovrapposizione() throws Exception {
        Sala sala = prepareInfrastructure();

        Film f = new Film(0, 60, "Short Film", "Desc", "Genere");
        DaoFactory.getFilmDAO().inserisci(f);

        // Spettacolo 1: 20:00 -> 21:20 (libera)
        LocalDateTime data1 = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0);
        service.creaSpettacolo(f.getId(), sala.getId(), data1, 10.0);

        // Spettacolo 2: 21:00 (Sovrapposto)
        LocalDateTime data2 = LocalDateTime.now().plusDays(1).withHour(21).withMinute(0);

        assertThrows(SalaNonDisponibileException.class, () ->
                        service.creaSpettacolo(f.getId(), sala.getId(), data2, 10.0),
                "Deve lanciare eccezione se l'orario si sovrappone a uno spettacolo esistente"
        );
    }

    @Test
    @DisplayName("Creazione Sala: Successo")
    void testCreaSala() throws Exception {
        Cinema c = new Cinema(0, "Nuovo Cinema", "Indirizzo", "Città");
        DaoFactory.getCinemaDAO().insert(c);

        Sala s = new Sala(c, "Sala VIP", 5, 5, "VIP");

        assertDoesNotThrow(() -> service.creaSala(s));

        Sala recuperata = DaoFactory.getSalaDAO().trovaTutti().stream()
                .filter(sala -> sala.getNome().equals("Sala VIP"))
                .findFirst()
                .orElse(null);
        assertNotNull(recuperata);
    }

    // =================================================================================
    // TEST DATI UTENTE E PAGAMENTI
    // =================================================================================

    @Test
    @DisplayName("Aggiornamento Dati Pagamento: Successo")
    void testAggiornaDatiPagamento() throws Exception {
        String passwordPlain = "Pass1!";
        String passwordHash = SecurityUtils.hashPassword(passwordPlain);

        Utente u = new Utente("User", "Pay", "pay@test.it", passwordHash, Ruolo.CLIENTE, 25);
        service.registraUtente(u);

        u = service.login("pay@test.it", passwordPlain);

        service.aggiornaDatiPagamento(u, "1234567812345678", "12/30", "123");

        Utente updated = service.login("pay@test.it", passwordPlain);
        assertEquals("1234567812345678", updated.getNumeroCarta());
    }

    @Test
    @DisplayName("Aggiornamento Dati Pagamento: Dati Invalidi")
    void testAggiornaDatiPagamentoInvalidi() {
        Utente u = new Utente("User", "Invalid", "inv@test.it", "hash", Ruolo.CLIENTE, 25);

        assertThrows(DatiNonValidiException.class, () ->
                        service.aggiornaDatiPagamento(u, "123", "12/30", "123"),
                "Deve fallire se numero carta troppo corto"
        );

        assertThrows(DatiNonValidiException.class, () ->
                        service.aggiornaDatiPagamento(u, "1234567812345678", "12/30", "1"),
                "Deve fallire se CVV non valido"
        );
    }

    // =================================================================================
    // TEST ACQUISTO (LUCA)
    // =================================================================================

    @Test
    @DisplayName("Acquisto Biglietto: Flusso Completo e Controllo Posto")
    void testAcquistoBigliettoSuccesso() throws Exception {
        Sala sala = prepareInfrastructure();
        Film film = new Film(0, 120, "Matrix", "Sci-Fi", "Action");
        DaoFactory.getFilmDAO().inserisci(film);

        Spettacolo spettacolo = new Spettacolo(
                film,
                sala,
                LocalDateTime.now().plusHours(2),
                10.0
        );
        DaoFactory.getSpettacoloDAO().create(spettacolo);

        String passwordPlain = "Pass1!";
        String passwordHash = SecurityUtils.hashPassword(passwordPlain);
        Utente utente = new Utente("Buyer", "One", "buyer@test.it", passwordHash, Ruolo.CLIENTE, 25);
        service.registraUtente(utente);

        utente = service.login("buyer@test.it", passwordPlain);

        service.compraBiglietto(utente, spettacolo, 5, 5);

        List<Biglietto> biglietti = DaoFactory.getBigliettoDAO().trovaPerSpettacolo(spettacolo.getId());
        assertEquals(1, biglietti.size());
        assertEquals(5, biglietti.get(0).getFila());

        Utente finalUtente = utente;
        assertThrows(PostoOccupatoException.class, () ->
                        service.compraBiglietto(finalUtente, spettacolo, 5, 5),
                "Non deve essere possibile acquistare un posto già occupato"
        );
    }

    // =================================================================================
    // HELPER METHODS
    // =================================================================================

    private Sala prepareInfrastructure() throws SQLException, DatiNonValidiException {
        Cinema cinema = new Cinema(0, "Cinema Test", "Via Test", "Città Test");
        DaoFactory.getCinemaDAO().insert(cinema);

        Sala sala = new Sala(cinema, "Sala 1", 10, 10, "Standard");
        DaoFactory.getSalaDAO().inserisci(sala);

        return sala;
    }
}