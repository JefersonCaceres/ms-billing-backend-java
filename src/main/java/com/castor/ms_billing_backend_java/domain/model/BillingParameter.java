package com.castor.ms_billing_backend_java.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingParameter {

    private Long id;

    private String paramType;      // TAX | DISCOUNT

    private String name;

    private String description;

    private Double valuePercent;   // Porcentaje (TAX/DISCOUNT)

    private Double valueAmount;    // Si en el futuro deseas usar valor fijo

    private Double minPurchase;    // Mínimo de compra para aplicar descuento

    private Boolean isActive;
}
