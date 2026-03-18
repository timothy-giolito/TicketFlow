package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe di test per verificare le funzionalità della classe Utente.
 * <p>
 * Questa classe utilizza JUnit 5 per assicurare che il modello Utente
 * gestisca correttamente i dati, le validazioni nel costruttore,
 * i metodi di business e l'incapsulamento dei dati.
 * </p>
 * @author Luca Franzon 20054744
 */
public class UtenteTest {

    private Utente utenteBase;

    /**
     * Configurazione iniziale eseguita prima di ogni singolo test.
     * <p>
     * Inizializza un oggetto Utente con dati minimi validi per garantire
     * un punto di partenza pulito per ogni test.
     * </p>
     */
    @BeforeEach
    public void setUp() {
        utenteBase = new Utente("Mario", "Rossi", "mario.rossi@example.com", "PasswordHash123!", Ruolo.CLIENTE, 25);
    }

    // -------------------------------------------------------------------------
    // TEST COSTRUTTORI E CREAZIONE
    // -------------------------------------------------------------------------

    /**
     * Verifica la corretta creazione dell'oggetto Utente tramite costruttore base.
     */
    @Test
    @DisplayName("Test Creazione Utente Base Valido")
    public void testCostruttoreBase() {
        assertNotNull(utenteBase);
        assertEquals("Mario", utenteBase.getNome());
        assertEquals("Rossi", utenteBase.getCognome());
        assertEquals("mario.rossi@example.com", utenteBase.getEmail());
        assertEquals("PasswordHash123!", utenteBase.getPassword());
        assertEquals(Ruolo.CLIENTE, utenteBase.getRuolo());
        assertEquals(25, utenteBase.getEta());
    }

    /**
     * Verifica la corretta creazione dell'oggetto Utente tramite costruttore completo.
     * Controlla che anche i dati di fatturazione vengano assegnati.
     */
    @Test
    @DisplayName("Test Creazione Utente Completo Valido")
    public void testCostruttoreCompleto() {
        Utente utenteCompleto = new Utente(
                "Luigi", "Verdi", "luigi.verdi@example.com", "PassHash456!",
                Ruolo.CLIENTE, 30,
                "Via Roma 10", "1234567890123456", "12/26", "123"
        );

        assertEquals("Via Roma 10", utenteCompleto.getIndirizzo());
        assertEquals("1234567890123456", utenteCompleto.getNumeroCarta());
        assertEquals("12/26", utenteCompleto.getScadenzaCarta());
        assertEquals("123", utenteCompleto.getCvv());
    }

    // -------------------------------------------------------------------------
    // TEST VALIDAZIONI (ECCEZIONI)
    // -------------------------------------------------------------------------

    /**
     * Verifica che venga lanciata un'eccezione se il nome è nullo o vuoto.
     */
    @Test
    @DisplayName("Test Eccezione Nome Non Valido")
    public void testNomeNonValido() {
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("", "Rossi", "email@test.com", "Pass", Ruolo.CLIENTE, 20)
        );
        assertThrows(DatiNonValidiException.class, () ->
                new Utente(null, "Rossi", "email@test.com", "Pass", Ruolo.CLIENTE, 20)
        );
    }

    /**
     * Verifica che venga lanciata un'eccezione se il cognome è nullo o vuoto.
     */
    @Test
    @DisplayName("Test Eccezione Cognome Non Valido")
    public void testCognomeNonValido() {
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("Mario", "", "email@test.com", "Pass", Ruolo.CLIENTE, 20)
        );
    }

    /**
     * Verifica che venga lanciata un'eccezione se l'email non rispetta il formato regex.
     */
    @Test
    @DisplayName("Test Eccezione Email Formato Errato")
    public void testEmailNonValida() {
        // Email senza @
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("Mario", "Rossi", "mariorossi.it", "Pass", Ruolo.CLIENTE, 20)
        );
        // Email vuota
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("Mario", "Rossi", "", "Pass", Ruolo.CLIENTE, 20)
        );
    }

    /**
     * Verifica che venga lanciata un'eccezione se l'età è fuori dal range consentito (0-120).
     */
    @Test
    @DisplayName("Test Eccezione Età Non Valida")
    public void testEtaNonValida() {
        // Età negativa
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("Mario", "Rossi", "a@b.c", "Pass", Ruolo.CLIENTE, -5)
        );
        // Età eccessiva
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("Mario", "Rossi", "a@b.c", "Pass", Ruolo.CLIENTE, 150)
        );
    }

    /**
     * Verifica che venga lanciata un'eccezione se la password (hash) è mancante.
     */
    @Test
    @DisplayName("Test Eccezione Password Mancante")
    public void testPasswordMancante() {
        assertThrows(DatiNonValidiException.class, () ->
                new Utente("Mario", "Rossi", "a@b.c", null, Ruolo.CLIENTE, 20)
        );
    }

    // -------------------------------------------------------------------------
    // TEST METODI DI BUSINESS
    // -------------------------------------------------------------------------

    /**
     * Verifica il funzionamento del metodo haDatiPagamento.
     * Deve restituire true solo se numero carta e CVV sono presenti.
     */
    @Test
    @DisplayName("Test Verifica Dati Pagamento")
    public void testHaDatiPagamento() {
        // Caso 1: Utente base senza dati di pagamento
        assertFalse(utenteBase.haDatiPagamento(), "Dovrebbe essere false se mancano i dati carta");

        // Caso 2: Aggiungo solo il numero carta (manca CVV)
        utenteBase.setNumeroCarta("123456789");
        assertFalse(utenteBase.haDatiPagamento(), "Dovrebbe essere false se manca il CVV");

        // Caso 3: Dati completi
        utenteBase.setCvv("123");
        assertTrue(utenteBase.haDatiPagamento(), "Dovrebbe essere true con carta e CVV inseriti");
    }

    // -------------------------------------------------------------------------
    // TEST SETTER E AGGIORNAMENTI
    // -------------------------------------------------------------------------

    /**
     * Verifica l'aggiornamento dei dati anagrafici tramite setter.
     */
    @Test
    @DisplayName("Test Aggiornamento Anagrafica")
    public void testSetterAnagrafica() {
        utenteBase.setNome("Giulia");
        utenteBase.setCognome("Bianchi");
        utenteBase.setEta(30);

        assertEquals("Giulia", utenteBase.getNome());
        assertEquals("Bianchi", utenteBase.getCognome());
        assertEquals(30, utenteBase.getEta());
    }

    /**
     * Verifica l'aggiornamento dei dati di fatturazione opzionali.
     */
    @Test
    @DisplayName("Test Setter Dati Fatturazione")
    public void testSetterFatturazione() {
        utenteBase.setIndirizzo("Via Napoli 5");
        utenteBase.setNumeroCarta("987654321");
        utenteBase.setScadenzaCarta("01/30");
        utenteBase.setCvv("999");

        assertEquals("Via Napoli 5", utenteBase.getIndirizzo());
        assertEquals("987654321", utenteBase.getNumeroCarta());
        assertEquals("01/30", utenteBase.getScadenzaCarta());
        assertEquals("999", utenteBase.getCvv());
    }

    /**
     * Verifica l'aggiornamento del ruolo e dell'ID cinema (per Manager).
     */
    @Test
    @DisplayName("Test Setter Ruolo e Cinema")
    public void testSetRuoloECinema() {
        utenteBase.setRuolo(Ruolo.MANAGER);
        utenteBase.setIdCinema(5);

        assertEquals(Ruolo.MANAGER, utenteBase.getRuolo());
        assertEquals(5, utenteBase.getIdCinema());
    }

    // -------------------------------------------------------------------------
    // TEST UTILITY
    // -------------------------------------------------------------------------

    /**
     * Verifica che il metodo toString restituisca una stringa formattata correttamente.
     */
    @Test
    @DisplayName("Test Metodo toString")
    public void testToString() {
        String result = utenteBase.toString();
        assertNotNull(result);
        assertTrue(result.contains("Mario"));
        assertTrue(result.contains("mario.rossi@example.com"));
    }
}