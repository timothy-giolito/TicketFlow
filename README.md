# TicketFlow

## Descrizione del Progetto

**Nome del progetto:** TicketFlow

**Descrizione dettagliata:**
TicketFlow è una piattaforma software desktop sviluppata in Java, concepita per la gestione integrata e scalabile di un cinema multisala. Il sistema è progettato per ottimizzare il flusso di lavoro sia per il personale amministrativo che per la clientela finale, garantendo un'esperienza utente fluida grazie a un'interfaccia grafica moderna realizzata con **JavaFX**.

L'architettura del software segue rigorosamente il pattern architetturale **MVC (Model-View-Controller)**, separando nettamente la logica di business, l'interfaccia utente e la gestione dei dati. La persistenza delle informazioni è gestita tramite un database relazionale, interfacciato al software tramite l'utilizzo del **Pattern DAO (Data Access Object)** e **Factory**, che garantiscono modularità e facilità di manutenzione.

Le principali funzionalità del sistema sono suddivise in base ai ruoli utente:

* **Area Cliente:**
    * Consultazione del catalogo film e degli orari degli spettacoli.
    * Processo di prenotazione interattivo con selezione visuale dei posti in sala in tempo reale.
    * Sistema di calcolo dinamico del prezzo (gestione sconti, es. riduzione studenti).
    * Simulazione del processo di pagamento elettronico.

* **Area Manager:**
    * Gestione completa del palinsesto cinematografico (inserimento, modifica e rimozione film).
    * Configurazione delle sale e programmazione degli orari degli spettacoli.
    * Monitoraggio dello stato delle prenotazioni.

* **Area Admin:**
    * Gestione gerarchica degli utenti e dei permessi di accesso.
    * Supervisione globale del sistema e manutenzione delle anagrafiche.

Il progetto è gestito tramite **Maven** per le dipendenze e include una suite di **Unit Test (JUnit)** per verificare la correttezza della logica di business e de
