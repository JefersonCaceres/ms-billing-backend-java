package com.castor.ms_billing_backend_java.domain.ports.in;

import com.castor.ms_billing_backend_java.application.request.InvoiceCalculationRequest;
import com.castor.ms_billing_backend_java.application.response.InvoiceCalculationResponse;

public interface TaxServicePort {
    InvoiceCalculationResponse calculate(InvoiceCalculationRequest request);
}

