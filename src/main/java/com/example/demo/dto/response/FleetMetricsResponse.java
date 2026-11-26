package com.example.demo.dto.response;

public class FleetMetricsResponse {

    private long totalVehicles;
    private long activeVehicles;
    private long maintenanceVehicles;
    private long inactiveVehicles;

    public FleetMetricsResponse() {
    }

    public FleetMetricsResponse(long totalVehicles, long activeVehicles, long maintenanceVehicles, long inactiveVehicles) {
        this.totalVehicles = totalVehicles;
        this.activeVehicles = activeVehicles;
        this.maintenanceVehicles = maintenanceVehicles;
        this.inactiveVehicles = inactiveVehicles;
    }

    public long getTotalVehicles() {
        return totalVehicles;
    }

    public void setTotalVehicles(long totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public long getActiveVehicles() {
        return activeVehicles;
    }

    public void setActiveVehicles(long activeVehicles) {
        this.activeVehicles = activeVehicles;
    }

    public long getMaintenanceVehicles() {
        return maintenanceVehicles;
    }

    public void setMaintenanceVehicles(long maintenanceVehicles) {
        this.maintenanceVehicles = maintenanceVehicles;
    }

    public long getInactiveVehicles() {
        return inactiveVehicles;
    }

    public void setInactiveVehicles(long inactiveVehicles) {
        this.inactiveVehicles = inactiveVehicles;
    }
}

