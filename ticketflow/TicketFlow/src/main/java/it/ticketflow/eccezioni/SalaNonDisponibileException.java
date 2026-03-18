package it.ticketflow.eccezioni;

public class SalaNonDisponibileException extends Exception
{
    public SalaNonDisponibileException()
    {
        super("Sala non disponibile");
    }

    public SalaNonDisponibileException(String message) {
        super(message);
    }
}