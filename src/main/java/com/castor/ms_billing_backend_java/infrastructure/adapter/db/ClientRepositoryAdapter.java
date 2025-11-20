package com.castor.ms_billing_backend_java.infrastructure.adapter.db;

import com.castor.ms_billing_backend_java.domain.model.Client;
import com.castor.ms_billing_backend_java.domain.ports.out.ClientRepositoryPort;
import com.castor.ms_billing_backend_java.infrastructure.adapter.db.entity.ClientEntity;
import com.castor.ms_billing_backend_java.infrastructure.repository.ClientJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClientRepositoryAdapter implements ClientRepositoryPort {

    private final  ClientJpaRepository clientJpaRepository;

    public ClientRepositoryAdapter(ClientJpaRepository clientJpaRepository) {
        this.clientJpaRepository = clientJpaRepository;
    }

    @Override
    public Client save(Client client) {
        ClientEntity entity = toEntity(client);
        ClientEntity saved = clientJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Client> findByDocument(String document) {
        return clientJpaRepository.findByDocument(document)
                .map(this::toDomain);
    }

    @Override
    public void deleteByDocument(String document) {
        clientJpaRepository.deleteByDocument(document);
    }

    private ClientEntity toEntity(Client client) {
        ClientEntity entity = new ClientEntity();
        entity.setId(client.getId());
        entity.setDocumentType(client.getDocumentType());
        entity.setDocument(client.getDocument());
        entity.setName(client.getName());
        entity.setEmail(client.getEmail());
        entity.setPhone(client.getPhone());
        entity.setAddress(client.getAddress());
        entity.setActive(client.isActive());
        entity.setCreatedAt(client.getCreatedAt());
        entity.setUpdatedAt(client.getUpdatedAt());
        return entity;
    }

    private Client toDomain(ClientEntity entity) {
        return Client.builder()
                .id(entity.getId())
                .document(entity.getDocument())
                .documentType(entity.getDocumentType())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
