package it.ticketflow.service.payment;

/**
 * Interfaccia per la gestione dei pagamenti.
 */
public interface ServizioPagamento {

    /**
     * Elabora una transazione di pagamento.
     * * @param numeroCarta Il numero della carta di credito.
     * @param importo L'ammontare da addebitare.
     * @return true se il pagamento è andato a buon fine.
     */
    boolean elaboraPagamento(String numeroCarta, double importo);
}