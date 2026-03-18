package it.ticketflow.ui.controller;

import it.ticketflow.dao.CinemaDAO;
import it.ticketflow.dao.FilmDAO;
import it.ticketflow.dao.DaoFactory;
import it.ticketflow.dao.UtenteDAO;
import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.model.Cinema;
import it.ticketflow.model.Film;
import it.ticketflow.model.Ruolo;
import it.ticketflow.model.Utente;
import it.ticketflow.service.ReportingService;
import it.ticketflow.ui.SceneManager;
import it.ticketflow.ui.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;

/**
 * Controller per la Dashboard Amministratore.
 * Gestisce Cinema, Film, Utenti e Statistiche globali.
 * @author Timothy Giolito 20054431
 */
public class AdminDashboardController {

    // --- TAB CINEMA ---
    @FXML private TableView<Cinema> cinemaTable;
    @FXML private TableColumn<Cinema, Integer> colIdCinema;
    @FXML private TableColumn<Cinema, String> colNomeCinema;
    @FXML private TableColumn<Cinema, String> colIndirizzoCinema;
    @FXML private TextField txtNomeCinema;
    @FXML private TextField txtIndirizzoCinema;

    // --- TAB FILM ---
    @FXML private TableView<Film> filmTable;
    @FXML private TableColumn<Film, Integer> colIdFilm;
    @FXML private TableColumn<Film, String> colTitoloFilm;
    @FXML private TableColumn<Film, String> colGenereFilm;
    @FXML private TableColumn<Film, Integer> colDurataFilm;
    @FXML private TextField txtTitoloFilm;
    @FXML private TextField txtGenereFilm;
    @FXML private TextField txtDurataFilm;
    @FXML private TextArea txtDescrizioneFilm;

    // --- TAB UTENTI ---
    @FXML private TableView<Utente> utentiTable;
    @FXML private TableColumn<Utente, String> colNomeUtente;
    @FXML private TableColumn<Utente, String> colCognomeUtente;
    @FXML private TableColumn<Utente, String> colEmailUtente;
    @FXML private TableColumn<Utente, String> colRuoloUtente;

    @FXML private ComboBox<Cinema> cmbCinemaManager;
    @FXML private Label lblUtenteSelezionato;

    // --- TAB STATISTICHE ---
    @FXML private Label lblIncassiTotali;
    @FXML private Label lblBigliettiTotali;

    // --- DIPENDENZE ---
    private final CinemaDAO cinemaDAO = DaoFactory.getCinemaDAO();
    private final FilmDAO filmDAO = new FilmDAO();
    private final UtenteDAO utenteDAO = DaoFactory.getUtenteDAO();
    private final ReportingService reportingService = new ReportingService();

    // --- LISTE OSSERVABILI ---
    private final ObservableList<Cinema> listaCinema = FXCollections.observableArrayList();
    private final ObservableList<Film> listaFilm = FXCollections.observableArrayList();
    private final ObservableList<Utente> listaUtenti = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTabCinema();
        initTabFilm();
        initTabUtenti();
        loadStatistiche();
    }

    // ================= SEZIONE CINEMA =================
    private void initTabCinema() {
        colIdCinema.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomeCinema.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colIndirizzoCinema.setCellValueFactory(new PropertyValueFactory<>("indirizzo"));
        cinemaTable.setItems(listaCinema);
        caricaCinema();
    }

    private void caricaCinema() {
        try {
            listaCinema.setAll(cinemaDAO.findAll());
            // Aggiorna anche la combobox nella tab Utenti
            cmbCinemaManager.setItems(listaCinema);
            // Setup converter per mostrare il nome del cinema invece dell'oggetto
            cmbCinemaManager.setConverter(new StringConverter<>() {
                @Override public String toString(Cinema c) { return (c != null) ? c.getNome() : ""; }
                @Override public Cinema fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            mostraAlert(Alert.AlertType.ERROR, "Errore", "Impossibile caricare i cinema.");
        }
    }

    @FXML
    private void handleAggiungiCinema(ActionEvent event) {
        try {
            String nome = txtNomeCinema.getText();
            String indirizzo = txtIndirizzoCinema.getText();
            if (nome.isEmpty() || indirizzo.isEmpty()) throw new DatiNonValidiException("Campi vuoti");

            Cinema c = new Cinema(0, nome, indirizzo, "Default City");
            cinemaDAO.insert(c);
            caricaCinema();
            txtNomeCinema.clear();
            txtIndirizzoCinema.clear();
        } catch (Exception e) {
            mostraAlert(Alert.AlertType.ERROR, "Errore", e.getMessage());
        }
    }

    @FXML
    private void handleRimuoviCinema(ActionEvent event) {
        Cinema selezionato = cinemaTable.getSelectionModel().getSelectedItem();
        if (selezionato == null) return;
        try {
            cinemaDAO.delete(selezionato.getId());
            caricaCinema();
        } catch (SQLException e) {
            mostraAlert(Alert.AlertType.ERROR, "Errore", "Impossibile eliminare il cinema (potrebbe avere sale o manager associati).");
        }
    }

    // ================= SEZIONE FILM =================
    private void initTabFilm() {
        colIdFilm.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitoloFilm.setCellValueFactory(new PropertyValueFactory<>("titolo"));
        colGenereFilm.setCellValueFactory(new PropertyValueFactory<>("genere"));
        colDurataFilm.setCellValueFactory(new PropertyValueFactory<>("durataMinuti"));
        filmTable.setItems(listaFilm);
        caricaFilm();
    }

    private void caricaFilm() {
        try {
            listaFilm.setAll(filmDAO.trovaTutti());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAggiungiFilm(ActionEvent event) {
        try {
            String titolo = txtTitoloFilm.getText();
            String genere = txtGenereFilm.getText();
            String durataStr = txtDurataFilm.getText();
            String desc = txtDescrizioneFilm.getText();

            if(titolo.isEmpty() || durataStr.isEmpty()) throw new DatiNonValidiException("Titolo e durata sono obbligatori.");

            int durata = Integer.parseInt(durataStr);

            Film f = new Film(0, durata, titolo, desc, genere);
            filmDAO.inserisci(f);

            caricaFilm();
            txtTitoloFilm.clear();
            txtGenereFilm.clear();
            txtDurataFilm.clear();
            txtDescrizioneFilm.clear();
            mostraAlert(Alert.AlertType.INFORMATION, "Successo", "Film aggiunto al catalogo.");
        } catch (NumberFormatException e) {
            mostraAlert(Alert.AlertType.WARNING, "Errore Input", "La durata deve essere un numero intero.");
        } catch (SQLException | DatiNonValidiException e) {
            mostraAlert(Alert.AlertType.ERROR, "Errore Database", e.getMessage());
        }
    }

    @FXML
    private void handleRimuoviFilm(ActionEvent event) {
        Film selezionato = filmTable.getSelectionModel().getSelectedItem();
        if (selezionato != null) {
            try {
                filmDAO.elimina(selezionato.getId());
                caricaFilm();
            } catch (SQLException e) {
                mostraAlert(Alert.AlertType.ERROR, "Errore", "Impossibile eliminare il film.");
            }
        }
    }

    // ================= SEZIONE UTENTI & MANAGER =================
    private void initTabUtenti() {
        colNomeUtente.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCognomeUtente.setCellValueFactory(new PropertyValueFactory<>("cognome"));
        colEmailUtente.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRuoloUtente.setCellValueFactory(new PropertyValueFactory<>("ruolo"));

        utentiTable.setItems(listaUtenti);

        // Listener per la selezione utente nella tabella
        utentiTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblUtenteSelezionato.setText(newVal.getNome() + " " + newVal.getCognome());
            } else {
                lblUtenteSelezionato.setText("Nessuno");
            }
        });

        caricaUtenti();
    }

    private void caricaUtenti() {
        try {
            listaUtenti.setAll(utenteDAO.findAll());
        } catch (Exception e) {
            System.err.println("Errore caricamento utenti: " + e.getMessage());
        }
    }

    @FXML
    private void handlePromuoviManager(ActionEvent event) {
        Utente utenteSelezionato = utentiTable.getSelectionModel().getSelectedItem();
        Cinema cinemaSelezionato = cmbCinemaManager.getValue();

        if (utenteSelezionato == null) {
            mostraAlert(Alert.AlertType.WARNING, "Selezione Mancante", "Seleziona un utente dalla lista a destra.");
            return;
        }

        if (cinemaSelezionato == null) {
            mostraAlert(Alert.AlertType.WARNING, "Cinema Mancante", "Seleziona il cinema da assegnare.");
            return;
        }

        try {
            utenteDAO.promuoviAManager(utenteSelezionato.getId(), cinemaSelezionato.getId());

            // Aggiorna la vista locale senza ricaricare tutto il DB
            utenteSelezionato.setIdCinema(cinemaSelezionato.getId());
            utenteSelezionato.setRuolo(Ruolo.MANAGER);
            utentiTable.refresh();

            mostraAlert(Alert.AlertType.INFORMATION, "Operazione Completata",
                    "L'utente " + utenteSelezionato.getNome() + " è ora Manager di " + cinemaSelezionato.getNome());

        } catch (SQLException e) {
            mostraAlert(Alert.AlertType.ERROR, "Errore Database", "Impossibile assegnare il manager: " + e.getMessage());
        }
    }

    // ================= SEZIONE STATISTICHE =================
    private void loadStatistiche() {
        try {
            double incassi = reportingService.getIncassiGlobali();
            int biglietti = reportingService.getBigliettiVendutiTotali();

            lblIncassiTotali.setText(String.format("€ %.2f", incassi));
            lblBigliettiTotali.setText(String.valueOf(biglietti));
        } catch (SQLException e) {
            lblIncassiTotali.setText("Errore");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        SceneManager.cambiaScena("login.fxml");
    }

    private void mostraAlert(Alert.AlertType tipo, String titolo, String messaggio) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}