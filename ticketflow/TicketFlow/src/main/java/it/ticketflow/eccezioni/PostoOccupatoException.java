package it.ticketflow.eccezioni;

public class PostoOccupatoException extends Exception{

    //costruttore per lanciare il messaggio dell'eccezione
    public PostoOccupatoException(String messaggio){

        super("Il posto selezionato e' occupato!");
    }
}