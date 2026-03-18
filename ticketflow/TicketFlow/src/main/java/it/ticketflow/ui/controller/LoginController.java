package it.ticketflow.ui.controller;

import it.ticketflow.eccezioni.DatabaseException;
import it.ticketflow.eccezioni.UtenteNonAutorizzatoException;
import it.ticketflow.model.Utente;
import it.ticketflow.service.TicketFlowService;
import it.ticketflow.ui.SceneManager;
import it.ticketflow.ui.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller per la gestione dell'autenticazione utente.
 * Gestisce il login, la validazione delle credenziali e l'instradamento
 * verso la dashboard specifica per il ruolo dell'utente.
 * @author Stefano Bellan 20054330
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final TicketFlowService service = new TicketFlowService();

    /**
     * Inizializza il controller pulendo eventuali messaggi di errore precedenti.
     */
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    /**
     * Gestisce l'azione del pulsante di Login.
     * Recupera le credenziali, autentica l'utente e cambia la scena.
     *
     * @param event L'evento scatenato dal click sul pulsante "ACCEDI".
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            mostraErrore("Inserisci email e password.");
            return;
        }

        try {
            // 1. Autenticazione tramite Service
            Utente utenteLoggato = service.login(email.trim(), password);

            // 2. Setup Sessione globale
            UserSession.cleanUserSession();
            UserSession.getInstance(utenteLoggato);

            // 3. Routing alla Dashboard corretta in base al ruolo
            String dashboardFXML = determinaDashboard(utenteLoggato);
            SceneManager.cambiaScena(dashboardFXML);

        } catch (UtenteNonAutorizzatoException e) {
            mostraErrore("Credenziali non valide.");
        } catch (DatabaseException e) {
            mostraErrore("Errore di connessione al database.");
            e.printStackTrace();
        } catch (Exception e) {
            mostraErrore("Errore imprevisto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Naviga verso la schermata di registrazione.
     */
    @FXML
    public void goToRegistration(ActionEvent event) {
        SceneManager.cambiaScena("register.fxml");
    }

    /**
     * Placeholder per reset password.
     */
    @FXML
    public void goToResetPwd(ActionEvent event) {
        mostraErrore("Funzionalità non ancora disponibile.");
    }

    /**
     * Determina il file FXML della dashboard in base al ruolo dell'utente.
     *
     * @param user L'utente loggato.
     * @return Il nome del file FXML della dashboard corretta.
     */
    private String determinaDashboard(Utente user) {
        if (user.getRuolo() == null) {
            throw new IllegalStateException("L'utente non ha un ruolo definito.");
        }

        switch (user.getRuolo()) {
            case SUPER_ADMIN:
                return "admin_dashboard.fxml";
            case MANAGER:
                return "manager_dashboard.fxml";
            case CLIENTE:
                return "user_dashboard.fxml";
            default:
                throw new IllegalStateException("Ruolo non gestito: " + user.getRuolo());
        }
    }

    private void mostraErrore(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
    }
}