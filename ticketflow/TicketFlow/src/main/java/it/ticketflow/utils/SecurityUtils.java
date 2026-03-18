package it.ticketflow.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Classe di utilità per la gestione della sicurezza e della crittografia.
 * <p>
 * Questa classe fornisce metodi statici per operazioni comuni come l'hashing
 * delle password e la verifica delle credenziali, centralizzando la logica di sicurezza.
 * </p>
 * @author Stefano Bellan 20054330
 */
public class SecurityUtils {

    /**
     * Costruttore privato per nascondere quello pubblico implicito.
     * Impedisce l'istanziazione della classe utility.
     */
    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Esegue l'hashing di una password in chiaro utilizzando l'algoritmo SHA-256.
     * <p>
     * L'algoritmo SHA-256 produce un output irreversibile (digest) di 256 bit.
     * Questo metodo converte il risultato in una stringa esadecimale leggibile.
     * </p>
     *
     * @param password La password in chiaro da criptare.
     * @return Una stringa contenente la rappresentazione esadecimale dell'hash.
     * @throws RuntimeException Se l'algoritmo SHA-256 non è disponibile nell'ambiente Java corrente.
     */
    public static String hashPassword(String password) {
        try {
            // Otteniamo un'istanza del motore di digest per l'algoritmo SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convertiamo la stringa della password in un array di byte usando UTF-8
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

            // Eseguiamo l'hashing effettivo ottenendo un array di byte
            byte[] encodedhash = digest.digest(passwordBytes);

            // Convertiamo l'array di byte in una stringa esadecimale formattata
            return HexFormat.of().formatHex(encodedhash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore critico: Algoritmo SHA-256 non trovato.", e);
        }
    }

    /**
     * Verifica se una password in chiaro corrisponde a un hash salvato.
     * <p>
     * Questo metodo ricalcola l'hash della password fornita in input e lo confronta
     * con l'hash memorizzato nel database per determinare se coincidono.
     * </p>
     *
     * @param passwordPlain La password in chiaro inserita dall'utente.
     * @param storedHash L'hash della password recuperato dal database.
     * @return true se l'hash calcolato corrisponde a quello salvato, false altrimenti.
     */
    public static boolean verificaPassword(String passwordPlain, String storedHash) {
        // Controllo preventivo per evitare NullPointerException
        if (passwordPlain == null || storedHash == null) {
            return false;
        }

        // Calcoliamo l'hash della password appena inserita
        String hashInput = hashPassword(passwordPlain);

        // Confrontiamo l'hash calcolato con quello originale
        return hashInput.equals(storedHash);
    }
}