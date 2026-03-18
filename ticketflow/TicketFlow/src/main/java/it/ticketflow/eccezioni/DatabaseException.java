package it.ticketflow.eccezioni;

public class DatabaseException extends RuntimeException{

    //costruttore per lanciare il messaggio dell'eccezione
    public DatabaseException (String messaggio){

        super("Problema nella connessione al Database");
    }


    public DatabaseException (String messaggio, Throwable cause){

        super(messaggio, cause);
    }
}