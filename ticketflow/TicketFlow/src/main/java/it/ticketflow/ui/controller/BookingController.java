package it.ticketflow.ui.controller;

import it.ticketflow.dao.BigliettoDAO;
import it.ticketflow.dao.DaoFactory;
import it.ticketflow.eccezioni.DatabaseException;
import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.eccezioni.PostoOccupatoException;
import it.ticketflow.model.Biglietto;
import it.ticketflow.model.Posto;
import it.ticketflow.model.Spettacolo;
import it.ticketflow.model.Utente;
import it.ticketflow.service.TicketFlowService;
import it.ticketflow.ui.SceneManager;
import it.ticketflow.ui.UserSession;
import it.ticketflow.utils.QRCodeUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  Controller per la gestione della Prenotazione Biglietti e Pagamento.
 *  Gestisce l'inserimento dei dati di carta di credito e l'acquisto.
 * </p>
 *
 * @author Luca Franzon 20054744
 */
public class BookingController {

    @FXML private GridPane grigliaPosti;
    @FXML private Label lblTitoloSpettacolo;
    @FXML private Button btnConferma;
    @FXML private Button btnIndietro;

    // --- Campi per il Pagamento ---
    @FXML private TextField txtNumeroCarta;
    @FXML private TextField txtScadenza;
    @FXML private TextField txtCVV;

    private Spettacolo spettacoloCorrente;
    private final List<Posto> carrelloPosti = new ArrayList<>();
    private final BigliettoDAO bigliettoDAO = DaoFactory.getBigliettoDAO();
    private final TicketFlowService ticketFlowService = new TicketFlowService();

    @FXML
    public void initialize() {
        if (btnIndietro != null) {
            btnIndietro.setOnAction(e -> SceneManager.cambiaScena("user_dashboard.fxml"));
        }
        if (btnConferma != null) {
            btnConferma.setOnAction(e -> handlePaga());
        }
    }

    /**
     * Inizializza la vista con i dati dello spettacolo selezionato.
     * Pre-popola i campi carta se l'utente ha già salvato i dati in precedenza.
     */
    public void inizializzaDati(Spettacolo spettacolo) {
        this.spettacoloCorrente = spettacolo;
        this.carrelloPosti.clear();

        Utente u = UserSession.getInstance().getUtente();

        // Pre-popolamento campi se dati già presenti
        if (u != null && u.haDatiPagamento()) {
            if (txtNumeroCarta != null) txtNumeroCarta.setText(u.getNumeroCarta());
            if (txtScadenza != null) txtScadenza.setText(u.getScadenzaCarta());
            if (txtCVV != null) txtCVV.setText(u.getCvv());
        } else {
            if (txtNumeroCarta != null) txtNumeroCarta.clear();
            if (txtScadenza != null) txtScadenza.clear();
            if (txtCVV != null) txtCVV.clear();
        }

        if (lblTitoloSpettacolo != null && spettacolo != null) {
            lblTitoloSpettacolo.setText("Prenotazione: " + spettacolo.getFilm().getTitolo() +
                    " - " + spettacolo.getDataOra().toLocalTime());
        }
        aggiornaGriglia();
    }

    /**
     * Gestisce il pagamento:
     * 1. Recupera i dati carta dalla UI.
     * 2. Aggiorna l'utente con questi dati (persistenza).
     * 3. Procede all'acquisto.
     */
    @FXML
    private void handlePaga() {
        if (carrelloPosti.isEmpty()) {
            mostraAlert(Alert.AlertType.WARNING, "Carrello Vuoto", "Seleziona almeno un posto.");
            return;
        }

        Utente utenteCorrente = UserSession.getInstance().getUtente();
        if (utenteCorrente == null) {
            SceneManager.cambiaScena("login.fxml");
            return;
        }

        // Recupero Input Pagamento
        String carta = txtNumeroCarta != null ? txtNumeroCarta.getText().trim() : "";
        String scadenza = txtScadenza != null ? txtScadenza.getText().trim() : "";
        String cvv = txtCVV != null ? txtCVV.getText().trim() : "";

        try {
            // 1. Validazione e Salvataggio Dati Pagamento
            // Chiamiamo il service per salvare i nuovi dati inseriti nel profilo utente
            ticketFlowService.aggiornaDatiPagamento(utenteCorrente, carta, scadenza, cvv);

            // 2. Esecuzione Acquisto
            List<String> bigliettiInfo = new ArrayList<>();
            StringBuilder errori = new StringBuilder();

            for (Posto p : carrelloPosti) {
                try {
                    ticketFlowService.compraBiglietto(utenteCorrente, spettacoloCorrente, p.getRiga(), p.getColonna());
                    bigliettiInfo.add("TKT-" + spettacoloCorrente.getId() + "-" + p.getRiga() + "-" + p.getColonna());
                } catch (PostoOccupatoException e) {
                    errori.append("Posto ").append(p.getRiga()).append("-").append(p.getColonna()).append(" già occupato.\n");
                }
            }

            // 3. Feedback Finale
            if (!bigliettiInfo.isEmpty()) {
                // Generazione QR del primo biglietto (simulazione gruppo)
                Image qrCodeImage = QRCodeUtils.generaQR(bigliettiInfo.get(0));
                mostraSuccessoConQR(qrCodeImage, bigliettiInfo.size());
                SceneManager.cambiaScena("user_dashboard.fxml");
            } else {
                mostraAlert(Alert.AlertType.ERROR, "Errore Acquisto", "Nessun biglietto acquistato.\n" + errori);
                aggiornaGriglia();
            }

        } catch (DatiNonValidiException | DatabaseException e) {
            mostraAlert(Alert.AlertType.ERROR, "Dati Pagamento", e.getMessage());
        }
    }

    private void mostraSuccessoConQR(Image qrImage, int quantita) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acquisto Completato");
        alert.setHeaderText("Grazie per l'acquisto!");
        alert.setContentText("Hai acquistato " + quantita + " biglietti.");

        ImageView imageView = new ImageView(qrImage);
        imageView.setFitHeight(150);
        imageView.setFitWidth(150);

        VBox content = new VBox(10, new Label("Scansiona all'ingresso:"), imageView);
        alert.getDialogPane().setExpandableContent(content);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    // --- Metodi UI Griglia (Invariati) ---
    private void aggiornaGriglia() {
        if (spettacoloCorrente == null) return;
        grigliaPosti.getChildren().clear();
        try {
            List<Biglietto> venduti = bigliettoDAO.trovaPerSpettacolo(spettacoloCorrente.getId());
            for (int r = 1; r <= spettacoloCorrente.getSala().getRighe(); r++) {
                for (int c = 1; c <= spettacoloCorrente.getSala().getColonne(); c++) {
                    boolean occupato = isPostoVenduto(venduti, r, c);
                    Posto posto = new Posto(r, c, occupato);
                    posto.setOnAction(e -> gestisciClickPosto(posto));
                    grigliaPosti.add(posto, c - 1, r - 1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        aggiornaBottoneConferma();
    }

    private boolean isPostoVenduto(List<Biglietto> list, int r, int c) {
        return list.stream().anyMatch(b -> b.getFila() == r && b.getColonna() == c);
    }

    private void gestisciClickPosto(Posto p) {
        if (p.isOccupato()) return;
        if (p.isSelezionato()) { p.setSelezionato(false); carrelloPosti.remove(p); }
        else { p.setSelezionato(true); carrelloPosti.add(p); }
        aggiornaBottoneConferma();
    }

    private void aggiornaBottoneConferma() {
        if (btnConferma != null) {
            int n = carrelloPosti.size();
            btnConferma.setText(n > 0 ? "Paga e Conferma (" + n + ")" : "Seleziona Posti");
            btnConferma.setDisable(n == 0);
        }
    }

    private void mostraAlert(Alert.AlertType tipo, String titolo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}