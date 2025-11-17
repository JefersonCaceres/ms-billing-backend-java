package com.castor.ms_billing_backend_java.application.mapper;

import com.castor.ms_billing_backend_java.application.dto.ClientDto;
import com.castor.ms_billing_backend_java.domain.model.Client;

public class ClientMapper {
    private ClientMapper() {
    }

    public static Client toDomain(ClientDto dto) {
        if (dto == null) return null;

        return Client.builder()
                .document(dto.getDocument())
                .documentType(dto.getDocumentType())
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .active(dto.getActive())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static ClientDto toDto(Client client) {
        if (client == null) return null;

        ClientDto dto = new ClientDto();
        dto.setDocumentType(client.getDocumentType());
        dto.setDocument(client.getDocument());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAddress(client.getAddress());
        dto.setActive(client.isActive());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());
        return dto;
    }
}
