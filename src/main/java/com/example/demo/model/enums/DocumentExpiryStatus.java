package com.example.demo.model.enums;

/**
 * Expiry classification for tracked vehicle compliance documents.
 */
public enum DocumentExpiryStatus {
    EXPIRED("Expired"),
    EXPIRING_SOON("Expiring Soon"),
    VALID("Valid");

    private final String label;

    DocumentExpiryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
