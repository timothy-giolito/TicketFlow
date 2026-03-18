package it.ticketflow.ui;

import it.ticketflow.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

/**
 * Gestore centralizzato per la navigazione e il caricamento delle scene FXML.
 *
 * @author Timothy Giolito 20054431
 */
public class SceneManager {

    private static Stage stage;

    /**
     * Imposta lo stage principale dell'applicazione.
     */
    public static void setStage(Stage stage) {
        SceneManager.stage = stage;
    }

    /**
     * Carica e visualizza una nuova scena FXML.
     *
     * @param fxmlFileName Il nome del file FXML (es. "login.fxml").
     */
    public static void cambiaScena(String fxmlFileName) {
        try {
            String path = "/view/" + fxmlFileName;
            URL resource = App.class.getResource(path);

            if (resource == null) {
                throw new IOException("File FXML non trovato: " + path);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Errore caricamento scena: " + fxmlFileName);
            e.printStackTrace();
            mostraErroreFatale("Impossibile caricare la schermata: " + fxmlFileName + "\n" + e.getMessage());
        }
    }

    private static void mostraErroreFatale(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore Applicazione");
        alert.setHeaderText("Errore di Navigazione");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}