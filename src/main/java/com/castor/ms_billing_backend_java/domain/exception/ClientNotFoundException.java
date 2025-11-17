package com.castor.ms_billing_backend_java.domain.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String document) {
        super("Client not found with document: " + document);
    }
}
