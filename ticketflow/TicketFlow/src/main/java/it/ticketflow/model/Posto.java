package it.ticketflow.model;

import javafx.scene.control.Button;

// 1. "extends Button" significa che questa classe eredita tutto ciò che fa un normale bottone
public class Posto extends Button {

    // 2. Le etichette interne (Chi sono?)
    private int riga;
    private int colonna;
    private boolean occupato; // true = rosso (venduto), false = verde (libero)
    private boolean selezionato; // true = giallo (lo sto comprando ora)

    /**
     * Costruttore: Viene chiamato quando creiamo un nuovo Posto.
     * @param riga Il numero della riga
     * @param colonna Il numero della colonna
     * @param occupato Se il posto è già stato venduto
     * @author Luca Franzon 20054744
     */
    public Posto(int riga, int colonna, boolean occupato) {
        this.riga = riga;
        this.colonna = colonna;
        this.occupato = occupato;
        this.selezionato = false; // Di base, nessuno lo ha ancora selezionato

        // Impostiamo il testo del bottone (es. "R1-C5") per riconoscerlo visivamente
        this.setText(riga + "-" + colonna);

        // Impostiamo una dimensione fissa per farli sembrare dei quadratini
        this.setPrefSize(50, 50);

        // Chiamiamo il metodo per "vestire" il bottone del colore giusto
        aggiornaStile();
    }

    /**
     * Questo metodo decide il colore del bottone in base al suo stato.
     */
    public void aggiornaStile() {
        if (occupato) {
            // Se occupato -> Rosso e NON cliccabile
            this.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
            this.setDisable(true); // Disabilita il click
        } else if (selezionato) {
            // Se lo sto selezionando ora -> Giallo
            this.setStyle("-fx-background-color: #feca57; -fx-text-fill: black;");
            this.setDisable(false);
        } else {
            // Se libero -> Verde
            this.setStyle("-fx-background-color: #1dd1a1; -fx-text-fill: white;");
            this.setDisable(false);
        }
    }

    // --- Metodi Getter e Setter (per leggere e modificare i dati) ---

    public int getRiga() {
        return riga;
    }

    public int getColonna() {
        return colonna;
    }

    public boolean isOccupato() {
        return occupato;
    }

    public boolean isSelezionato() {
        return selezionato;
    }

    public void setSelezionato(boolean selezionato) {
        this.selezionato = selezionato;
        aggiornaStile(); // Ogni volta che cambia stato, ricalcoliamo il colore!
    }
}