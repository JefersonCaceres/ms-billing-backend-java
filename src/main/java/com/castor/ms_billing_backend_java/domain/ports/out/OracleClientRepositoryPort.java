package com.castor.ms_billing_backend_java.domain.ports.out;

public interface  OracleClientRepositoryPort {
    void saveClient(Long id, String name, String email, boolean active);

    void updateClient(Long id, String name, String email, boolean active);
}
