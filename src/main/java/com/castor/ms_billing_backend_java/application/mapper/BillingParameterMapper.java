package com.castor.ms_billing_backend_java.application.mapper;

import com.castor.ms_billing_backend_java.domain.model.BillingParameter;
import com.castor.ms_billing_backend_java.infrastructure.adapter.db.entity.BillingParameterEntity;

public final class BillingParameterMapper {

    private BillingParameterMapper() {
    }

    public static BillingParameter toDomain(BillingParameterEntity e) {
        return new BillingParameter(
                e.getId(),
                e.getParamType(),
                e.getName(),
                e.getDescription(),
                e.getValuePercent(),
                e.getValueAmount(),
                e.getMinPurchase(),
                e.getIsActive()
        );
    }
}

