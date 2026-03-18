package it.ticketflow.ui.controller;

import it.ticketflow.eccezioni.DatiNonValidiException;
import it.ticketflow.eccezioni.DatabaseException;
import it.ticketflow.model.Ruolo;
import it.ticketflow.model.Utente;
import it.ticketflow.service.TicketFlowService;
import it.ticketflow.ui.SceneManager;
import it.ticketflow.utils.SecurityUtils; // Import necessario per l'hashing
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller per la schermata di registrazione.
 * Gestisce la creazione di nuovi account utente (Cliente).
 * @author Luca Franzon 20054744
 */
public class RegisterController {

    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private TextField etaField; // Campo aggiunto per l'età
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final TicketFlowService service = new TicketFlowService();

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
    }

    /**
     * Gestisce l'azione del pulsante "Crea Account".
     * Raccoglie i dati, valida, esegue l'hash della password e invoca il servizio.
     */
    @FXML
    public void handleRegister(ActionEvent event) {
        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        String etaStr = etaField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1. Validazione Campi Vuoti
        if (nome.isBlank() || cognome.isBlank() || etaStr.isBlank() || email.isBlank() || password.isBlank()) {
            mostraMessaggio("Compila tutti i campi.", true);
            return;
        }

        try {
            // 2. Parsing Età
            int eta = Integer.parseInt(etaStr);
            if (eta < 14 || eta > 120) {
                mostraMessaggio("Età non valida (minimo 14 anni).", true);
                return;
            }

            // 3. Hashing della Password
            // Utente.java richiede la password GIA' hashata nel costruttore
            String passwordHash = SecurityUtils.hashPassword(password);

            // 4. Creazione Utente (Fix costruttore corretto)
            // Firma: Utente(nome, cognome, email, passwordHash, ruolo, eta)
            Utente nuovoUtente = new Utente(nome, cognome, email, passwordHash, Ruolo.CLIENTE, eta);

            // 5. Invio al Service (Fix firma corretta)
            // Il service si aspetta solo l'oggetto Utente popolato
            service.registraUtente(nuovoUtente);

            // 6. Successo
            mostraMessaggio("Registrazione avvenuta con successo! Reindirizzamento...", false);

            // Ritardo simulato per UX (opzionale) o cambio scena diretto
            SceneManager.cambiaScena("login.fxml");

        } catch (NumberFormatException e) {
            mostraMessaggio("L'età deve essere un numero valido.", true);
        } catch (DatiNonValidiException e) {
            mostraMessaggio("Dati non validi: " + e.getMessage(), true);
        } catch (DatabaseException e) {
            mostraMessaggio("Errore: " + e.getMessage(), true); // Es. Email duplicata
        } catch (Exception e) {
            mostraMessaggio("Errore imprevisto: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        SceneManager.cambiaScena("login.fxml");
    }

    private void mostraMessaggio(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // Rosso
        } else {
            statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); // Verde
        }
    }
}