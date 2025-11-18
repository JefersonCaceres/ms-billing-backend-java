package com.castor.ms_billing_backend_java.infrastructure.repository;

import com.castor.ms_billing_backend_java.infrastructure.adapter.db.entity.BillingParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingParameterJpaRepository
        extends JpaRepository<BillingParameterEntity, Long> {
    List<BillingParameterEntity> findByIsActiveTrue();
}

