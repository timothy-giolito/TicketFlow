package it.ticketflow.eccezioni;

public class DatiNonValidiException extends IllegalArgumentException
{
    public DatiNonValidiException()
    {
        super("I dati forniti non sono validi o violano i vincoli di business");
    }

    public DatiNonValidiException(String message){
        super(message);
    }
}