package com.example.demo.model.enums;

public enum DriverStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String label;

    DriverStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
