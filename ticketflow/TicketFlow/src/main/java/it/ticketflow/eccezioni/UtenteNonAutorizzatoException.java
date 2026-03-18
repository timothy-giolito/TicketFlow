package it.ticketflow.eccezioni;

public class UtenteNonAutorizzatoException extends SecurityException
{
    public UtenteNonAutorizzatoException()
    {
        super("L'utente corrente non possiede i permessi necessari per eseguire questa operazione.");
    }

    public UtenteNonAutorizzatoException(String message)
    {
        super(message);
    }
}