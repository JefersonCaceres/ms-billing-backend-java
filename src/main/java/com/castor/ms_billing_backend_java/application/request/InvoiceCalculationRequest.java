package com.castor.ms_billing_backend_java.application.request;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceCalculationRequest {

    private List<Item> items;
    private List<Parameter> parameters;

    @Data
    public static class Item {
        private String description;
        private Integer quantity;
        private Double unitPrice;
    }

    @Data
    public static class Parameter {
        private String paramType; // TAX or DISCOUNT
        private Double valuePercent;
    }
}

