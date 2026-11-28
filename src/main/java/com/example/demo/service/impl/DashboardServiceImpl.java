package com.example.demo.service.impl;

import com.example.demo.dto.response.DashboardSummaryResponse;
import com.example.demo.model.enums.DriverStatus;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.repository.DriverRepository;
import com.example.demo.repository.VehicleDocumentRepository;
import com.example.demo.repository.VehicleMaintenanceRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentRepository documentRepository;
    private final VehicleMaintenanceRepository maintenanceRepository;
    private final DriverRepository driverRepository;

    public DashboardServiceImpl(VehicleRepository vehicleRepository,
                                VehicleDocumentRepository documentRepository,
                                VehicleMaintenanceRepository maintenanceRepository,
                                DriverRepository driverRepository) {
        this.vehicleRepository = vehicleRepository;
        this.documentRepository = documentRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);
        LocalDate thirtyDaysAgo = today.minusDays(30);

        DashboardSummaryResponse.VehicleSummary vehicleSummary = new DashboardSummaryResponse.VehicleSummary(
                vehicleRepository.count(),
                vehicleRepository.countByStatus(VehicleStatus.ACTIVE),
                vehicleRepository.countByStatus(VehicleStatus.INACTIVE),
                vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE)
        );

        DashboardSummaryResponse.DocumentSummary documentSummary = new DashboardSummaryResponse.DocumentSummary(
                documentRepository.count(),
                documentRepository.countExpiredDocuments(today),
                documentRepository.countExpiringSoonDocuments(today, thirtyDaysFromNow)
        );

        BigDecimal totalCost = maintenanceRepository.sumTotalCost();
        DashboardSummaryResponse.MaintenanceSummary maintenanceSummary = new DashboardSummaryResponse.MaintenanceSummary(
                maintenanceRepository.count(),
                totalCost != null ? totalCost : BigDecimal.ZERO,
                maintenanceRepository.countByMaintenanceDateGreaterThanEqual(thirtyDaysAgo)
        );

        DashboardSummaryResponse.DriverSummary driverSummary = new DashboardSummaryResponse.DriverSummary(
                driverRepository.count(),
                driverRepository.countByStatus(DriverStatus.ACTIVE),
                driverRepository.countByStatus(DriverStatus.INACTIVE)
        );

        return new DashboardSummaryResponse(vehicleSummary, documentSummary, maintenanceSummary, driverSummary);
    }
}
