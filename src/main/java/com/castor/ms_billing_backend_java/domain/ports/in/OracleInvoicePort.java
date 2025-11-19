package com.castor.ms_billing_backend_java.domain.ports.in;

public interface OracleInvoicePort {
    Long createInvoice(
            Long clientId,
            Double subtotal,
            Double tax,
            Double discount,
            Double total
    );
}

