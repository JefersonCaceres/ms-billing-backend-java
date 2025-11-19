package com.castor.ms_billing_backend_java.domain.ports.out;

import com.castor.ms_billing_backend_java.domain.model.Client;

import java.util.Optional;

public interface ClientRepositoryPort {
    Client save(Client client);
    Optional<Client> findByDocument(String document);
    void deleteByDocument(String document);
}