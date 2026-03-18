module it.ticketflow {
    // Moduli JavaFX necessari per la GUI
    requires javafx.controls;
    requires javafx.fxml;

    opens it.ticketflow.model to javafx.base;
    // Moduli per il Database
    requires java.sql;              // Per JDBC (Connection, SQLException, ecc.)
    requires org.xerial.sqlitejdbc; // Driver SQLite


    opens it.ticketflow.ui.controller to javafx.fxml;

    // Esporta il package principale dove risiede la classe App (che estende Application)
    exports it.ticketflow;
    exports it.ticketflow.service.payment;
    exports it.ticketflow.model.pricing;
}