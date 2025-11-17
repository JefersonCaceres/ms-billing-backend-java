package com.castor.ms_billing_backend_java.application.service;

import com.castor.ms_billing_backend_java.domain.exception.ClientNotFoundException;
import com.castor.ms_billing_backend_java.domain.model.Client;
import com.castor.ms_billing_backend_java.domain.ports.in.ClientUseCase;
import com.castor.ms_billing_backend_java.domain.ports.out.ClientRepositoryPort;
import com.castor.ms_billing_backend_java.domain.ports.out.OracleClientRepositoryPort;
import com.castor.ms_billing_backend_java.infrastructure.helper.ClientLogHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClientUseCaseImpl implements ClientUseCase {
    private final ClientRepositoryPort clientRepositoryPort;
    private final OracleClientRepositoryPort oraclePort;
    private final ClientLogHelper clientLogHelper;

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
            System.out.println("⚠ Error replicating client to Oracle: " + e.getMessage());
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
            System.out.println("Error replicating update to Oracle: " + e.getMessage());
        }

        return updated;
    }

    @Override
    public Client findByDocument(String document) {
        return clientRepositoryPort.findByDocument(document)
                .orElseThrow(() -> new ClientNotFoundException(document));
    }

    @Override
    public void deleteByDocument(String document) {

        Client existing = clientRepositoryPort.findByDocument(document)
                .orElseThrow(() -> new ClientNotFoundException(document));

        clientRepositoryPort.deleteByDocument(document);
        clientLogHelper.log(
                existing.getId(),
                "DELETE",
                "Se elimina el cliente: " + existing.getDocument()
        );

        try {
            oraclePort.updateClient(
                    existing.getId(),
                    existing.getName(),
                    existing.getEmail(),
                    false
            );
        } catch (Exception e) {
            System.out.println("⚠ Error replicating delete to Oracle: " + e.getMessage());
        }
    }

}
