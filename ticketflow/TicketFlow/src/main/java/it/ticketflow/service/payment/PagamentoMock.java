package it.ticketflow.service.payment;

/**
 * <p>
 * Implementazione Mock del servizio di pagamento.
 * Simula una transazione bancaria con un ritardo prefissato.
 * </p>
 *
 * @author Luca Franzon 20054744
 */
public class PagamentoMock implements ServizioPagamento {

    @Override
    public boolean elaboraPagamento(String numeroCarta, double importo) {
        try {
            // Simula un ritardo di 2 secondi (2000 millisecondi)
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Gestisce l'eventuale interruzione del thread durante lo sleep
            Thread.currentThread().interrupt();
            return false;
        }

        // Restituisce sempre true come richiesto dal documento di visione
        return true;
    }
}