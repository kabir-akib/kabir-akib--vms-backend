package com.example.demo.dto.response;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private VehicleSummary vehicles;
    private DocumentSummary documents;
    private MaintenanceSummary maintenance;
    private DriverSummary drivers;

    public DashboardSummaryResponse(VehicleSummary vehicles,
                                    DocumentSummary documents,
                                    MaintenanceSummary maintenance,
                                    DriverSummary drivers) {
        this.vehicles = vehicles;
        this.documents = documents;
        this.maintenance = maintenance;
        this.drivers = drivers;
    }

    public VehicleSummary getVehicles() {
        return vehicles;
    }

    public DocumentSummary getDocuments() {
        return documents;
    }

    public MaintenanceSummary getMaintenance() {
        return maintenance;
    }

    public DriverSummary getDrivers() {
        return drivers;
    }

    public record VehicleSummary(long totalVehicles, long activeVehicles, long inactiveVehicles, long maintenanceVehicles) {
    }

    public record DocumentSummary(long totalDocuments, long expiredDocuments, long expiringSoonDocuments) {
    }

    public record MaintenanceSummary(long totalRecords, BigDecimal totalCost, long recentRecordsLast30Days) {
    }

    public record DriverSummary(long totalDrivers, long activeDrivers, long inactiveDrivers) {
    }
}
