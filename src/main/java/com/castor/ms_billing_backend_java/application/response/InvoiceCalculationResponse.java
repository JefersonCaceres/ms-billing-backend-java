package com.castor.ms_billing_backend_java.application.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceCalculationResponse {
    private Long invoiceId;
    private Long clientId;

    private Double subtotal;
    private Double tax;
    private Double discount;
    private Double total;

    private String message;
}

