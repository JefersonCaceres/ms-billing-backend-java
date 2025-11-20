package com.castor.ms_billing_backend_java.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientDto {

    private String documentType;
    private String document;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
