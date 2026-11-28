package com.example.demo.model.enums;

/**
 * Supported vehicle document categories for VMS compliance and records.
 */
public enum DocumentType {
    REGISTRATION_CERTIFICATE("Registration Certificate"),
    TAX_TOKEN("Tax Token"),
    INSURANCE("Insurance"),
    FITNESS_CERTIFICATE("Fitness Certificate"),
    OTHER("Other");

    private final String label;

    DocumentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
