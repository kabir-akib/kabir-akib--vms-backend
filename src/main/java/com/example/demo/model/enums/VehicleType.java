package com.example.demo.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehicleType {
    TRUCK("Truck"),
    VAN("Delivery Van"),
    HEAVY_HAULER("Heavy Hauler"),
    SEDAN("Sedan"),
    SUV("SUV"),
    ELECTRIC("Electric Utility");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static VehicleType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim();
        for (VehicleType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)
                    || type.label.equalsIgnoreCase(normalized)
                    || type.name().replace("_", "").equalsIgnoreCase(normalized.replace(" ", "").replace("_", ""))) {
                return type;
            }
        }
        // Handle camelCase from frontend (e.g., heavyHauler)
        for (VehicleType type : values()) {
            if (toCamelCase(type.name()).equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown vehicle type: " + value + 
                ". Allowed values: TRUCK, VAN, HEAVY_HAULER, SEDAN, SUV, ELECTRIC");
    }

    private static String toCamelCase(String s) {
        String[] parts = s.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)))
                  .append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }
}

