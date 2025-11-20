package com.castor.ms_billing_backend_java.infrastructure.adapter.db.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "billing_parameter", schema = "billing")
@Data
public class BillingParameterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_type")
    private String paramType;

    private String name;

    private String description;

    @Column(name = "value_percent")
    private Double valuePercent;

    @Column(name = "value_amount")
    private Double valueAmount;

    @Column(name = "min_purchase")
    private Double minPurchase;

    @Column(name = "is_active")
    private Boolean isActive;
}
