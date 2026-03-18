package it.ticketflow;

import it.ticketflow.db.SchemaInit;
import it.ticketflow.ui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point dell'applicazione JavaFX.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // 1. Inizializza il Database (crea tabelle se non esistono)
        SchemaInit.initializeDatabase();

        // 2. Configura il gestore delle scene con lo stage primario
        SceneManager.setStage(stage);

        // 3. Impostazioni finestra
        stage.setTitle("TicketFlow - Cinema Management System");
        stage.setResizable(false); // Opzionale: blocca il ridimensionamento

        // 4. Avvia con la schermata di Login
        SceneManager.cambiaScena("login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}