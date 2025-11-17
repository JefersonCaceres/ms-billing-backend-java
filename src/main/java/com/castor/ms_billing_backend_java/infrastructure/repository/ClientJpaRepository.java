package com.castor.ms_billing_backend_java.infrastructure.repository;

import com.castor.ms_billing_backend_java.infrastructure.adapter.db.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  ClientJpaRepository extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByDocument(String document);
    void deleteByDocument(String document);
}
