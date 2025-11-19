package com.castor.ms_billing_backend_java.application.service;

import com.castor.ms_billing_backend_java.application.request.InvoiceCalculationRequest;
import com.castor.ms_billing_backend_java.application.response.InvoiceCalculationResponse;
import com.castor.ms_billing_backend_java.domain.ports.in.TaxServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TaxServiceAdapter implements TaxServicePort {

    private final WebClient webClient;

    public TaxServiceAdapter(@Value("${tax-service.base-url}") String taxServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(taxServiceUrl)
                .build();
    }

    @Override
    public InvoiceCalculationResponse calculate(InvoiceCalculationRequest request) {
        return webClient.post()
                .uri("/calculate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(InvoiceCalculationResponse.class)
                .block();
    }
}

