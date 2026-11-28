package com.example.demo.model.enums;

/**
 * Supported vehicle maintenance categories in VMS.
 */
public enum MaintenanceType {
    ROUTINE_SERVICE("Routine Service"),
    REPAIR("Repair"),
    INSPECTION("Inspection"),
    OIL_CHANGE("Oil Change"),
    OTHER("Other");

    private final String label;

    MaintenanceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
