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
        private Double unit_price;
    }

    @Data
    public static class Parameter {
        private String param_type; // TAX or DISCOUNT
        private Double value_percent;
    }
}

