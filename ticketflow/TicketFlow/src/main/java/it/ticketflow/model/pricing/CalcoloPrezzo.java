package it.ticketflow.model.pricing;

import it.ticketflow.model.Spettacolo;
import it.ticketflow.model.Utente;

/**
 * Metodo per il calcolo del prezzo del singolo biglietto.
 * @author Timothy Giolito 20054431
 * */
public interface CalcoloPrezzo {

    //calcolo prezzo finale in base allo Spettacolo e all'Utente
    double calcola(Spettacolo s, Utente u);
}
