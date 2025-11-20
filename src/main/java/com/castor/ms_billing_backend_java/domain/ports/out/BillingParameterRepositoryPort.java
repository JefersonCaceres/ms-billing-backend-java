package com.castor.ms_billing_backend_java.domain.ports.out;

import com.castor.ms_billing_backend_java.domain.model.BillingParameter;

import java.util.List;

public interface BillingParameterRepositoryPort {
    List<BillingParameter> findActiveParameters();
}

