package it.ticketflow.ui.controller;

import it.ticketflow.eccezioni.DatabaseException;
import it.ticketflow.model.Spettacolo;
import it.ticketflow.service.TicketFlowService;
import it.ticketflow.ui.SceneManager;
import it.ticketflow.ui.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller per la Dashboard Utente.
 * Gestisce l'interfaccia principale per i clienti (ricerca film, visualizzazione card).
 * @author Luca Franzon 20054744
 */
public class UserDashboardController {

    // --- Componenti FXML ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TilePane spettacoliContainer;

    // --- Servizi e Dati ---
    private final TicketFlowService ticketFlowService = new TicketFlowService();
    private List<Spettacolo> listaCompletaSpettacoli;

    /**
     * Inizializzazione: Carica gli spettacoli e popola i filtri.
     */
    @FXML
    public void initialize() {
        caricaDati();
        // Listener per la ricerca automatica mentre si digita
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applicaFiltri());
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        applicaFiltri();
    }

    @FXML
    public void handleReset(ActionEvent event) {
        searchField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        visualizzaSpettacoli(listaCompletaSpettacoli);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.getInstance().cleanUserSession();
        SceneManager.cambiaScena("login.fxml");
    }

    // ================= METODI PRIVATI =================

    private void caricaDati() {
        try {
            listaCompletaSpettacoli = ticketFlowService.getTuttiSpettacoli();

            // Estrae dinamicamente i generi dai film caricati
            Set<String> generiDisponibili = listaCompletaSpettacoli.stream()
                    .map(s -> s.getFilm().getGenere())
                    .collect(Collectors.toSet());

            genreComboBox.getItems().setAll(generiDisponibili);
            visualizzaSpettacoli(listaCompletaSpettacoli);

        } catch (DatabaseException e) {
            mostraErrore("Errore Caricamento", "Impossibile recuperare gli spettacoli: " + e.getMessage());
        }
    }

    private void applicaFiltri() {
        if (listaCompletaSpettacoli == null) return;

        String queryTitolo = searchField.getText().toLowerCase().trim();
        String genereScelto = genreComboBox.getValue();
        LocalDate dataScelta = datePicker.getValue();

        List<Spettacolo> risultatiFiltrati = listaCompletaSpettacoli.stream()
                .filter(s -> {
                    boolean matchTitolo = queryTitolo.isEmpty() || s.getFilm().getTitolo().toLowerCase().contains(queryTitolo);
                    boolean matchGenere = genereScelto == null || s.getFilm().getGenere().equalsIgnoreCase(genereScelto);
                    boolean matchData = dataScelta == null || s.getDataOra().toLocalDate().equals(dataScelta);
                    return matchTitolo && matchGenere && matchData;
                })
                .collect(Collectors.toList());

        visualizzaSpettacoli(risultatiFiltrati);
    }

    private void visualizzaSpettacoli(List<Spettacolo> spettacoli) {
        spettacoliContainer.getChildren().clear();

        if (spettacoli.isEmpty()) {
            Label placeholder = new Label("Nessuno spettacolo trovato.");
            placeholder.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            spettacoliContainer.getChildren().add(placeholder);
            return;
        }

        for (Spettacolo s : spettacoli) {
            spettacoliContainer.getChildren().add(creaCardSpettacolo(s));
        }
    }

    /**
     * Crea graficamente una "Card" per ogni spettacolo.
     * Stile coerente con il CSS: sfondo scuro per contrasto con la dashboard chiara.
     */
    private VBox creaCardSpettacolo(Spettacolo s) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_CENTER);

        // --- Immagine ---
        ImageView imgView = new ImageView();
        imgView.setFitHeight(250);
        imgView.setFitWidth(180);
        imgView.setPreserveRatio(true);

        // Gestione caricamento immagine con fallback
        try {
            String url = s.getUrlLocandina();
            if (url == null || url.isBlank()) {
                url = s.getFilm().getUrlLocandina();
            }

            if (url != null && !url.isBlank()) {
                imgView.setImage(new Image(url, true));
            } else {
                imgView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder_poster.png")));
            }
        } catch (Exception e) {
            try {
                imgView.setImage(new Image(getClass().getResourceAsStream("/images/placeholder_poster.png")));
            } catch (Exception ignored) {}
        }

        // --- Titolo ---
        Label lblTitolo = new Label(s.getFilm().getTitolo());
        lblTitolo.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 16px;");
        lblTitolo.setWrapText(true);
        lblTitolo.setAlignment(Pos.CENTER);

        // --- Info ---
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        Label lblInfo = new Label(s.getDataOra().format(timeFormatter) + " - " + s.getSala().getNome());
        lblInfo.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        // --- Bottone ---
        Button btnPrenota = new Button("Prenota € " + String.format("%.2f", s.getPrezzoBase()));
        btnPrenota.getStyleClass().add("button-primary"); // Usa stile CSS
        btnPrenota.setMaxWidth(Double.MAX_VALUE);
        btnPrenota.setOnAction(e -> apriPrenotazione(s, btnPrenota));

        card.getChildren().addAll(imgView, lblTitolo, lblInfo, btnPrenota);
        return card;
    }

    private void apriPrenotazione(Spettacolo s, Button source) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/booking.fxml"));
            Parent root = loader.load();

            BookingController controller = loader.getController();
            controller.inizializzaDati(s);

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostraErrore("Errore Navigazione", "Impossibile aprire la pagina di prenotazione.");
        }
    }

    private void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}