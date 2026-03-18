package it.ticketflow.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.util.Random;

/**
 * <p>
 * Classe di utilità per la generazione grafica dei codici QR.
 * Non richiede librerie esterne; genera un pattern visivo basato sui dati del biglietto.
 * </p>
 * @author Luca Franzon 20054744
 */
public class QRCodeUtils {

    // Dimensione dell'immagine quadrata (es. 200x200 pixel)
    private static final int DIMENSIONE = 200;

    /**
     * Genera un'immagine fittizia che rappresenta un codice QR univoco per i dati forniti.
     * * @param datiBiglietto La stringa univoca del biglietto (es. "TKT-12345-1-1")
     * @return Un oggetto Image di JavaFX visualizzabile nell'interfaccia.
     */
    public static Image generaQR(String datiBiglietto) {
        // Crea un'immagine vuota su cui possiamo "disegnare" pixel per pixel
        WritableImage immagine = new WritableImage(DIMENSIONE, DIMENSIONE);
        PixelWriter writer = immagine.getPixelWriter();

        // Usiamo la stringa dei dati come "seme" per il generatore casuale.
        // In questo modo, la stessa stringa genererà SEMPRE lo stesso identico disegno (determinismo).
        long seed = datiBiglietto != null ? datiBiglietto.hashCode() : System.currentTimeMillis();
        Random random = new Random(seed);

        // Disegniamo lo sfondo bianco
        for (int x = 0; x < DIMENSIONE; x++) {
            for (int y = 0; y < DIMENSIONE; y++) {
                writer.setColor(x, y, Color.WHITE);
            }
        }

        // Creiamo un pattern a "blocchi" per simulare l'aspetto di un QR
        int dimensioneBlocco = 10; // Grandezza di ogni "quadratino" del QR

        for (int x = 0; x < DIMENSIONE; x += dimensioneBlocco) {
            for (int y = 0; y < DIMENSIONE; y += dimensioneBlocco) {

                // Lasciamo un bordo bianco attorno (padding)
                if (x < 20 || y < 20 || x > DIMENSIONE - 20 || y > DIMENSIONE - 20) {
                    continue;
                }

                // Decide casualmente (ma in modo fisso grazie al seed) se il blocco è nero o bianco
                boolean isNero = random.nextBoolean();

                // Disegna il blocco
                if (isNero) {
                    disegnaBlocco(writer, x, y, dimensioneBlocco, Color.BLACK);
                }
            }
        }

        // Aggiungiamo i tre quadrati tipici agli angoli dei QR code per realismo
        disegnaQuadratoPosizionamento(writer, 20, 20, 40); // In alto a sinistra
        disegnaQuadratoPosizionamento(writer, DIMENSIONE - 60, 20, 40); // In alto a destra
        disegnaQuadratoPosizionamento(writer, 20, DIMENSIONE - 60, 40); // In basso a sinistra

        return immagine;
    }

    /**
     * Metodo helper per colorare un blocco di pixel.
     */
    private static void disegnaBlocco(PixelWriter writer, int xStart, int yStart, int size, Color colore) {
        for (int x = xStart; x < xStart + size; x++) {
            for (int y = yStart; y < yStart + size; y++) {
                // Controllo per non uscire dai bordi dell'immagine
                if (x < DIMENSIONE && y < DIMENSIONE) {
                    writer.setColor(x, y, colore);
                }
            }
        }
    }

    /**
     * Disegna i classici quadrati concentrici agli angoli dei QR code.
     */
    private static void disegnaQuadratoPosizionamento(PixelWriter writer, int x, int y, int size) {
        // Quadrato esterno nero
        for (int i = x; i < x + size; i++) {
            for (int j = y; j < y + size; j++) {
                writer.setColor(i, j, Color.BLACK);
            }
        }
        // Quadrato interno bianco
        int bordo = 10;
        for (int i = x + bordo; i < x + size - bordo; i++) {
            for (int j = y + bordo; j < y + size - bordo; j++) {
                writer.setColor(i, j, Color.WHITE);
            }
        }
        // Quadrato centrale nero
        int centro = 15;
        for (int i = x + centro; i < x + size - centro; i++) {
            for (int j = y + centro; j < y + size - centro; j++) {
                writer.setColor(i, j, Color.BLACK);
            }
        }
    }
}