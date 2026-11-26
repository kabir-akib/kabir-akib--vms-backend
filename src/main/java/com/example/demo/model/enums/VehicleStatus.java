package com.example.demo.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehicleStatus {
    ACTIVE("Active", "Ready and active for daily fleet operations"),
    MAINTENANCE("Maintenance", "Under scheduled inspection or repair"),
    INACTIVE("Inactive", "Idle, unassigned, or decommissioned");

    private final String label;
    private final String description;

    VehicleStatus(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static VehicleStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim();
        for (VehicleStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized) || status.label.equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle status: " + value + 
                ". Allowed values: ACTIVE, MAINTENANCE, INACTIVE");
    }
}

