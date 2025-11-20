package com.castor.ms_billing_backend_java.domain.ports.in;

import com.castor.ms_billing_backend_java.application.request.InvoiceCalculationRequest;
import com.castor.ms_billing_backend_java.application.response.InvoiceCalculationResponse;
import com.castor.ms_billing_backend_java.domain.model.Client;
public interface ClientUseCase {
    Client create(Client client);
    Client update(String document, Client client);
    Client findByDocument(String document);
    void deleteByDocument(String document);
    InvoiceCalculationResponse createInvoice(
            String document,
            InvoiceCalculationRequest request);
}
