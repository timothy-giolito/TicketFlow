package it.ticketflow.ui.controller;

import it.ticketflow.dao.DaoFactory;
import it.ticketflow.model.Film;
import it.ticketflow.model.Sala;
import it.ticketflow.model.Cinema;
import it.ticketflow.model.Spettacolo;
import it.ticketflow.model.Utente;
import it.ticketflow.service.ReportingService;
import it.ticketflow.service.TicketFlowService;
import it.ticketflow.ui.SceneManager;
import it.ticketflow.ui.UserSession;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller per la Dashboard del Manager.
 * Gestisce la logica di visualizzazione, modifica spettacoli e statistiche.
 * @author Stefano Bellan 20054330
 */
public class ManagerDashboardController {

    // --- CONTAINER VISTE ---
    @FXML private VBox menuView;
    @FXML private BorderPane spettacoliView;
    @FXML private BorderPane saleView;
    @FXML private BorderPane statsView;

    // --- MODULO SPETTACOLI ---
    @FXML private ComboBox<Film> comboFilm;
    @FXML private ComboBox<Sala> comboSala;
    @FXML private DatePicker datePicker;
    @FXML private TextField oraField;
    @FXML private TextField prezzoField;
    @FXML private Label statusLabel;
    @FXML private Label lblFileSelected;
    @FXML private Label lblTitoloFormSpettacolo;
    @FXML private Button btnConfermaSpettacolo;
    @FXML private Button btnAnnullaModifica;
    @FXML private TableView<Spettacolo> tabellaSpettacoli;

    // --- MODULO SALE ---
    @FXML private TextField nomeSalaField;
    @FXML private ComboBox<String> comboTipoSala;
    @FXML private Spinner<Integer> spinnerRighe;
    @FXML private Spinner<Integer> spinnerColonne;
    @FXML private GridPane gridAnteprimaSala;
    @FXML private Label statusSalaLabel;

    // --- MODULO STATISTICHE ---
    @FXML private Label lblIncassi;
    @FXML private Label lblBiglietti;

    // Variabili stato
    private File locandinaSelezionata;
    private Spettacolo spettacoloInModifica = null;
    private final TicketFlowService service = new TicketFlowService();
    private final ReportingService reportingService = new ReportingService();

    @FXML
    public void initialize() {
        showMenu(); // Mostra il menu principale all'avvio
        initCombos();
        initSpinner();
        setupTabella();
    }

    private void initCombos() {
        try {
            if (comboFilm != null) {
                comboFilm.getItems().setAll(DaoFactory.getFilmDAO().trovaTutti());
                comboFilm.setConverter(new StringConverter<>() {
                    @Override public String toString(Film f) { return (f != null) ? f.getTitolo() : ""; }
                    @Override public Film fromString(String s) { return null; }
                });
            }
            if (comboSala != null) {
                comboSala.getItems().setAll(DaoFactory.getSalaDAO().trovaTutti());
                comboSala.setConverter(new StringConverter<>() {
                    @Override public String toString(Sala s) { return (s != null) ? s.getNome() : ""; }
                    @Override public Sala fromString(String s) { return null; }
                });
            }
            if (comboTipoSala != null) {
                comboTipoSala.getItems().setAll("Standard", "IMAX", "Dolby Atmos", "VIP");
                comboTipoSala.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSpinner() {
        if(spinnerRighe != null) spinnerRighe.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 10));
        if(spinnerColonne != null) spinnerColonne.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 15));
    }

    private void setupTabella() {
        if (tabellaSpettacoli == null) return;

        TableColumn<Spettacolo, String> colFilm = new TableColumn<>("Film");
        colFilm.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getFilm().getTitolo()));

        TableColumn<Spettacolo, String> colSala = new TableColumn<>("Sala");
        colSala.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getSala().getNome()));

        TableColumn<Spettacolo, String> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(s -> new SimpleStringProperty(s.getValue().getDataOra().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        TableColumn<Spettacolo, String> colPrezzo = new TableColumn<>("Prezzo");
        colPrezzo.setCellValueFactory(s -> new SimpleStringProperty(String.format("€ %.2f", s.getValue().getPrezzoBase())));

        tabellaSpettacoli.getColumns().setAll(colFilm, colSala, colData, colPrezzo);

        // Listener selezione per modifica
        tabellaSpettacoli.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) caricaSpettacoloInForm(val);
        });
    }

    private void loadSpettacoli() {
        try {
            List<Spettacolo> list = service.getTuttiSpettacoli();
            tabellaSpettacoli.getItems().setAll(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void caricaSpettacoloInForm(Spettacolo s) {
        spettacoloInModifica = s;
        lblTitoloFormSpettacolo.setText("Modifica Spettacolo ID: " + s.getId());
        btnConfermaSpettacolo.setText("SALVA MODIFICHE");
        btnAnnullaModifica.setVisible(true);
        btnAnnullaModifica.setManaged(true);

        // Seleziona nella combo l'oggetto corretto
        comboFilm.getItems().stream().filter(f -> f.getId() == s.getFilm().getId()).findFirst().ifPresent(comboFilm.getSelectionModel()::select);
        comboSala.getItems().stream().filter(sa -> sa.getId() == s.getSala().getId()).findFirst().ifPresent(comboSala.getSelectionModel()::select);

        datePicker.setValue(s.getDataOra().toLocalDate());
        oraField.setText(s.getDataOra().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        prezzoField.setText(String.valueOf(s.getPrezzoBase()));

        lblFileSelected.setText(s.getUrlLocandina() != null ? "Locandina esistente" : "Nessun file");
        locandinaSelezionata = null;
    }

    @FXML
    public void pulisciCampiSpettacolo() {
        spettacoloInModifica = null;
        lblTitoloFormSpettacolo.setText("Nuovo Spettacolo");
        btnConfermaSpettacolo.setText("CREA SPETTACOLO");
        btnAnnullaModifica.setVisible(false);
        btnAnnullaModifica.setManaged(false);

        comboFilm.getSelectionModel().clearSelection();
        comboSala.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        oraField.clear();
        prezzoField.clear();
        lblFileSelected.setText("Nessun file");
        locandinaSelezionata = null;
        tabellaSpettacoli.getSelectionModel().clearSelection();
        mostraMessaggio(statusLabel, "", false);
    }

    @FXML
    public void handleCreaSpettacolo() {
        try {
            Film f = comboFilm.getValue();
            Sala s = comboSala.getValue();
            LocalDate d = datePicker.getValue();
            String ora = oraField.getText();
            String prezzoStr = prezzoField.getText();

            if (f == null || s == null || d == null || ora.isEmpty() || prezzoStr.isEmpty()) {
                mostraMessaggio(statusLabel, "Compila tutti i campi!", true);
                return;
            }

            LocalDateTime dt = LocalDateTime.of(d, LocalTime.parse(ora));
            double p = Double.parseDouble(prezzoStr.replace(",", "."));
            String url = (locandinaSelezionata != null) ? locandinaSelezionata.toURI().toString() : null;

            if (spettacoloInModifica == null) {
                service.creaSpettacolo(f.getId(), s.getId(), dt, p, url);
                mostraMessaggio(statusLabel, "Spettacolo creato!", false);
            } else {
                spettacoloInModifica.setFilm(f);
                spettacoloInModifica.setSala(s);
                spettacoloInModifica.setDataOra(dt);
                spettacoloInModifica.setPrezzoBase(p);
                if (url != null) spettacoloInModifica.setUrlLocandina(url);
                service.modificaSpettacolo(spettacoloInModifica);
                mostraMessaggio(statusLabel, "Modifica salvata!", false);
            }
            pulisciCampiSpettacolo();
            loadSpettacoli();

        } catch (Exception e) {
            mostraMessaggio(statusLabel, "Errore: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleUploadLocandina() {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(menuView.getScene().getWindow());
        if (f != null) {
            locandinaSelezionata = f;
            lblFileSelected.setText(f.getName());
        }
    }

    // --- GESTIONE SALE ---
    @FXML
    public void handleSalvaSala() {
        try {
            Utente manager = UserSession.getInstance().getUtente();
            if (manager == null) return;

            // Fallback: se l'utente non ha cinema associato (es. test), usa cinema ID 1
            int idCinema = (manager.getIdCinema() > 0) ? manager.getIdCinema() : 1;
            Cinema c = DaoFactory.getCinemaDAO().trovaPerId(idCinema);

            Sala s = new Sala(c, nomeSalaField.getText(), spinnerRighe.getValue(), spinnerColonne.getValue(), comboTipoSala.getValue());
            service.creaSala(s);
            mostraMessaggio(statusSalaLabel, "Sala creata con successo!", false);
        } catch (Exception e) {
            mostraMessaggio(statusSalaLabel, "Errore: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleAnteprimaSala() {
        if(gridAnteprimaSala == null) return;
        gridAnteprimaSala.getChildren().clear();
        int r = spinnerRighe.getValue();
        int c = spinnerColonne.getValue();

        for(int i=0; i<r; i++) {
            for(int j=0; j<c; j++) {
                Rectangle rect = new Rectangle(20, 20, Color.LIGHTGREEN);
                rect.setArcWidth(5);
                rect.setArcHeight(5);
                gridAnteprimaSala.add(rect, j, i);
            }
        }
    }

    // --- NAVIGAZIONE & VISUALIZZAZIONE ---
    @FXML public void showMenu() { switchView(menuView); }
    @FXML public void showSpettacoli() { switchView(spettacoliView); loadSpettacoli(); }
    @FXML public void showSale() { switchView(saleView); }
    @FXML public void showStatistiche() { switchView(statsView); loadStatistiche(); }

    private void switchView(Region visibleView) {
        if(menuView!=null) { menuView.setVisible(false); menuView.setManaged(false); }
        if(spettacoliView!=null) { spettacoliView.setVisible(false); spettacoliView.setManaged(false); }
        if(saleView!=null) { saleView.setVisible(false); saleView.setManaged(false); }
        if(statsView!=null) { statsView.setVisible(false); statsView.setManaged(false); }

        if(visibleView!=null) { visibleView.setVisible(true); visibleView.setManaged(true); }
    }

    private void loadStatistiche() {
        try {
            Utente u = UserSession.getInstance().getUtente();
            if(u == null) return;

            int idC = u.getIdCinema();
            double incassi;
            int biglietti;

            // Se idCinema è valido (>0), mostra stats specifiche, altrimenti globali
            if (idC > 0) {
                incassi = reportingService.getIncassiCinema(idC);
                biglietti = reportingService.getBigliettiVendutiCinema(idC);
            } else {
                incassi = reportingService.getIncassiGlobali();
                biglietti = reportingService.getBigliettiVendutiTotali();
            }

            lblIncassi.setText(String.format("€ %.2f", incassi));
            lblBiglietti.setText(String.valueOf(biglietti));
        } catch (SQLException e) {
            lblIncassi.setText("N/A");
            e.printStackTrace();
        }
    }

    private void mostraMessaggio(Label label, String msg, boolean errore) {
        if(label != null) {
            label.setText(msg);
            label.setStyle(errore ? "-fx-text-fill: red; -fx-font-weight: bold;" : "-fx-text-fill: green; -fx-font-weight: bold;");
        }
    }

    @FXML
    public void handleLogout() {
        UserSession.cleanUserSession();
        SceneManager.cambiaScena("login.fxml");
    }
}