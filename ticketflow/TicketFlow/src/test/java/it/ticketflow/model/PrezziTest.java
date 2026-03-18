package it.ticketflow.model;

import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.model.pricing.CalcoloPrezzo;
import it.ticketflow.model.pricing.ScontoStudenti;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite di test completa per la gestione dei prezzi.
 * Copre le strategie di sconto, la validazione dei dati nel modello e l'estendibilità del sistema.
 * <p>
 * @author Timothy Giolito 20054431
 * </p>
 */
public class PrezziTest {

    private Spettacolo spettacoloBase;
    private Film filmDummy;
    private Sala salaDummy;
    private static final double PREZZO_BASE = 10.0;

    @BeforeEach
    void setUp() throws DatiNonValidiException {
        // Setup comune: Creiamo uno spettacolo valido
        Cinema cinema = new Cinema(1, "Cinema Test", "Via Roma 1", "Milano");
        salaDummy = new Sala(cinema, "Sala 1", 10, 10, "Standard");
        filmDummy = new Film(120, "Film Test", "Descrizione", "Genere");

        // Creiamo uno spettacolo con prezzo base 10.0
        spettacoloBase = new Spettacolo(filmDummy, salaDummy, LocalDateTime.now().plusDays(1), PREZZO_BASE);
    }

    // --- SEZIONE 1: Test della Strategia "Sconto Studenti" ---
    @Nested
    @DisplayName("Test Strategia Sconto Studenti")
    class ScontoStudentiTest {

        private final CalcoloPrezzo strategia = new ScontoStudenti();

        @Test
        @DisplayName("Utente under 25: Applica sconto 20%")
        void testUtenteGiovane() {
            Utente studente = new Utente("Mario", "Rossi", "m@test.it", "pwd", Ruolo.CLIENTE, 20);
            assertEquals(8.0, strategia.calcola(spettacoloBase, studente), 0.001);
        }

        @Test
        @DisplayName("Utente over 25: Prezzo pieno")
        void testUtenteAdulto() {
            Utente adulto = new Utente("Luigi", "Verdi", "l@test.it", "pwd", Ruolo.CLIENTE, 30);
            assertEquals(PREZZO_BASE, strategia.calcola(spettacoloBase, adulto), 0.001);
        }
    }

    // --- SEZIONE 2: Test Validazione Prezzi in Spettacolo ---
    @Nested
    @DisplayName("Test Validazione Modello Spettacolo")
    class ValidazioneSpettacoloTest {

        @Test
        @DisplayName("Creazione Spettacolo: Il prezzo non può essere negativo")
        void testCostruttorePrezzoNegativo() {
            assertThrows(DatiNonValidiException.class, () -> {
                new Spettacolo(filmDummy, salaDummy, LocalDateTime.now(), -5.0);
            }, "Il costruttore dovrebbe lanciare eccezione per prezzi negativi");
        }

        @Test
        @DisplayName("Creazione Spettacolo: Il prezzo non può essere zero")
        void testCostruttorePrezzoZero() {
            assertThrows(DatiNonValidiException.class, () -> {
                new Spettacolo(filmDummy, salaDummy, LocalDateTime.now(), 0.0);
            }, "Il costruttore dovrebbe lanciare eccezione per prezzo zero");
        }

        @Test
        @DisplayName("Setter: Non si può impostare un prezzo negativo su uno spettacolo esistente")
        void testSetterPrezzoNegativo() {
            assertThrows(DatiNonValidiException.class, () -> {
                spettacoloBase.setPrezzoBase(-10.0);
            }, "Il setter setPrezzoBase dovrebbe impedire valori negativi");
        }
    }

    // --- SEZIONE 3: Test Flessibilità (Nuove Strategie) ---
    @Nested
    @DisplayName("Test Estendibilità Strategie")
    class NuoveStrategieTest {

        @Test
        @DisplayName("Simulazione Sconto Anziani (Over 65 paga metà prezzo)")
        void testNuovaStrategiaAnziani() {
            // Definiamo una nuova strategia "al volo" (Classe Anonima)
            // per dimostrare che il sistema accetta qualsiasi logica futura.
            CalcoloPrezzo strategiaAnziani = new CalcoloPrezzo() {
                @Override
                public double calcola(Spettacolo s, Utente u) {
                    if (u != null && u.getEta() >= 65) {
                        return s.getPrezzoBase() * 0.50; // 50% di sconto
                    }
                    return s.getPrezzoBase();
                }
            };

            Utente anziano = new Utente("Nonno", "Rossi", "n@test.it", "pwd", Ruolo.CLIENTE, 70);
            Utente giovane = new Utente("Nipote", "Rossi", "j@test.it", "pwd", Ruolo.CLIENTE, 20);

            // Verifica: L'anziano paga 5.0, il giovane paga 10.0
            assertEquals(5.0, strategiaAnziani.calcola(spettacoloBase, anziano), 0.001);
            assertEquals(10.0, strategiaAnziani.calcola(spettacoloBase, giovane), 0.001);
        }

        @Test
        @DisplayName("Strategia 'Giornata Gratis' (Prezzo sempre 0)")
        void testStrategiaGratis() {
            // Lambda expression: implementazione super concisa dell'interfaccia
            CalcoloPrezzo tuttoGratis = (s, u) -> 0.0;

            Utente utente = new Utente("Test", "User", "t@test.it", "pwd", Ruolo.CLIENTE, 30);
            assertEquals(0.0, tuttoGratis.calcola(spettacoloBase, utente), 0.001);
        }
    }
}