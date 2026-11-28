package com.example.demo.model.enums;

public enum AssignmentStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String label;

    AssignmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
