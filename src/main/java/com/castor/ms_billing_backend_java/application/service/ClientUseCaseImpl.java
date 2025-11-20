package com.castor.ms_billing_backend_java.application.service;

import com.castor.ms_billing_backend_java.application.request.InvoiceCalculationRequest;
import com.castor.ms_billing_backend_java.application.response.InvoiceCalculationResponse;
import com.castor.ms_billing_backend_java.domain.exception.ClientNotFoundException;
import com.castor.ms_billing_backend_java.domain.model.BillingParameter;
import com.castor.ms_billing_backend_java.domain.model.Client;
import com.castor.ms_billing_backend_java.domain.ports.in.ClientUseCase;
import com.castor.ms_billing_backend_java.domain.ports.in.OracleInvoicePort;
import com.castor.ms_billing_backend_java.domain.ports.in.TaxServicePort;
import com.castor.ms_billing_backend_java.domain.ports.out.BillingParameterRepositoryPort;
import com.castor.ms_billing_backend_java.domain.ports.out.ClientRepositoryPort;
import com.castor.ms_billing_backend_java.domain.ports.out.OracleClientRepositoryPort;
import com.castor.ms_billing_backend_java.infrastructure.helper.ClientLogHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientUseCaseImpl implements ClientUseCase {
    private final ClientRepositoryPort clientRepositoryPort;
    private final OracleClientRepositoryPort oraclePort;
    private final ClientLogHelper clientLogHelper;

    private final BillingParameterRepositoryPort parameterPort;
    private final TaxServicePort taxServicePort;
    private final OracleInvoicePort oracleInvoicePort;


    @Override
    @Transactional
    public Client create(Client client) {

        client.setCreatedAt(LocalDateTime.now());
        client.setActive(true);

        Client saved = clientRepositoryPort.save(client);
        clientLogHelper.log(
                saved.getId(),
                "CREATE",
                "Se creo el cliente: " + client.getDocument()
        );
        try {
            oraclePort.saveClient(
                    saved.getId(),
                    saved.getName(),
                    saved.getEmail(),
                    saved.isActive()
            );
        } catch (Exception e) {
            log.info("⚠ Error replicating client to Oracle: " + e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public Client update(String document, Client client) {

        Client existing = clientRepositoryPort.findByDocument(document)
                .orElseThrow(() -> new ClientNotFoundException(document));
        existing.setName(client.getName());
        existing.setEmail(client.getEmail());
        existing.setPhone(client.getPhone());
        existing.setAddress(client.getAddress());
        existing.setActive(client.isActive());
        existing.setUpdatedAt(LocalDateTime.now());
        Client updated = clientRepositoryPort.save(existing);
        clientLogHelper.log(
                updated.getId(),
                "UPDATE",
                "Se actualiza el cliente: " + existing.getDocument()
        );

        try {
            oraclePort.updateClient(
                    updated.getId(),
                    updated.getName(),
                    updated.getEmail(),
                    updated.isActive()
            );
        } catch (Exception e) {
            log.info("Error replicating update to Oracle: " + e.getMessage());
        }

        return updated;
    }

    @Override
    public Client findByDocument(String document) {
        return clientRepositoryPort.findByDocument(document)
                .orElseThrow(() -> new ClientNotFoundException(document));
    }

    @Override
    @Transactional
    public void deleteByDocument(String document) {

        Client existing = clientRepositoryPort.findByDocument(document)
                .orElseThrow(() -> new ClientNotFoundException(document));

        existing.setActive(false);
        clientRepositoryPort.save(existing);
        clientLogHelper.log(
                existing.getId(),
                "DELETE",
                "Se elimina logicamente el cliente: " + existing.getDocument()
        );

        try {
            oraclePort.updateClient(
                    existing.getId(),
                    existing.getName(),
                    existing.getEmail(),
                    false
            );
        } catch (Exception e) {
            log.info("Error replicating delete to Oracle: " + e.getMessage());
        }
    }

        @Override
        public InvoiceCalculationResponse createInvoice(
                String document,
                InvoiceCalculationRequest request) {

            // Buscar cliente en Postgres
            Optional<Client> clientOpt = clientRepositoryPort.findByDocument(document);

            boolean clientExists = clientOpt.isPresent();
            boolean clientActive = clientExists && clientOpt.get().isActive();

            Long clientId = clientExists ? clientOpt.get().getId() : null;

            //Calcular subtotal en Java
            double subtotal = request.getItems().stream()
                    .mapToDouble(i -> i.getQuantity() * i.getUnitPrice())
                    .sum();

            // Obtener parámetros desde PostgreSQL
            List<BillingParameter> params = parameterPort.findActiveParameters();

            //Construir parámetros TAX
            List<InvoiceCalculationRequest.Parameter> taxParams =
                    params.stream()
                            .filter(p -> p.getParamType().equalsIgnoreCase("TAX"))
                            .map(p -> {
                                InvoiceCalculationRequest.Parameter rp =
                                        new InvoiceCalculationRequest.Parameter();
                                rp.setParamType("TAX");
                                rp.setValuePercent(p.getValuePercent());
                                return rp;
                            }).toList();

            //Construir parámetros DISCOUNT SOLO si cliente activo
            List<InvoiceCalculationRequest.Parameter> discountParams = new ArrayList<>();

            if (clientActive) {
                discountParams = params.stream()
                        .filter(p -> p.getParamType().equalsIgnoreCase("DISCOUNT"))
                        .filter(p -> p.getMinPurchase() != null && subtotal >= p.getMinPurchase())
                        .max(Comparator.comparingDouble(p ->
                                p.getValuePercent() != null ? p.getValuePercent() : 0.0))
                        .map(p -> {
                            InvoiceCalculationRequest.Parameter rp =
                                    new InvoiceCalculationRequest.Parameter();
                            rp.setParamType("DISCOUNT");
                            rp.setValuePercent(p.getValuePercent());
                            return rp;
                        })
                        .map(List::of)
                        .orElse(new ArrayList<>());
            }

            // Preparar request final para Python
            InvoiceCalculationRequest pythonReq = new InvoiceCalculationRequest();
            pythonReq.setItems(request.getItems());

            List<InvoiceCalculationRequest.Parameter> finalParams = new ArrayList<>();
            finalParams.addAll(taxParams);
            finalParams.addAll(discountParams);

            pythonReq.setParameters(finalParams);

            // Llamar a Python
            InvoiceCalculationResponse result = taxServicePort.calculate(pythonReq);

            // Si cliente NO existe o NO está activo → retornar factura parcial
            if (!clientActive) {
                return new InvoiceCalculationResponse(
                        null,
                        null,
                        result.getSubtotal(),
                        result.getTax(),
                        0.0,
                        result.getSubtotal() + result.getTax(),
                        "Cliente no existe o está inactivo. Factura generada solo con impuestos."
                );
            }
            clientLogHelper.log(
                    clientId,
                    "BILL",
                    "Factura generada para el cliente: " + document +
                            " | Subtotal: " + subtotal +
                            " | Total: " + result.getTotal());


            // Crear factura en Oracle
            Long invoiceId = oracleInvoicePort.createInvoice(
                    clientId,
                    result.getSubtotal(),
                    result.getTax(),
                    result.getDiscount(),
                    result.getTotal()
            );

            // Retornar factura final
            return new InvoiceCalculationResponse(
                    invoiceId,
                    clientId,
                    result.getSubtotal(),
                    result.getTax(),
                    result.getDiscount(),
                    result.getTotal(),
                    "Factura generada correctamente."
            );
        }



}
