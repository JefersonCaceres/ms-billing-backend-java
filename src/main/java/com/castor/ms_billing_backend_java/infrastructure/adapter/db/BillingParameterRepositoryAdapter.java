package com.castor.ms_billing_backend_java.infrastructure.adapter.db;

import com.castor.ms_billing_backend_java.application.mapper.BillingParameterMapper;
import com.castor.ms_billing_backend_java.domain.model.BillingParameter;
import com.castor.ms_billing_backend_java.domain.ports.out.BillingParameterRepositoryPort;
import com.castor.ms_billing_backend_java.infrastructure.repository.BillingParameterJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BillingParameterRepositoryAdapter
        implements BillingParameterRepositoryPort {

    private final BillingParameterJpaRepository jpa;

    @Override
    public List<BillingParameter> findActiveParameters() {
        return jpa.findByIsActiveTrue()
                .stream()
                .map(BillingParameterMapper::toDomain)
                .toList();
    }
}

