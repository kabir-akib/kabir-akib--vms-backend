package com.example.demo.service;

import com.example.demo.dto.response.DashboardSummaryResponse;
import com.example.demo.model.enums.DriverStatus;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.repository.DriverRepository;
import com.example.demo.repository.VehicleDocumentRepository;
import com.example.demo.repository.VehicleMaintenanceRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleDocumentRepository documentRepository;

    @Mock
    private VehicleMaintenanceRepository maintenanceRepository;

    @Mock
    private DriverRepository driverRepository;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                vehicleRepository, documentRepository, maintenanceRepository, driverRepository);
    }

    @Test
    @DisplayName("getDashboardSummary returns accurate aggregated counts and totals")
    void getDashboardSummary_Populated() {
        when(vehicleRepository.count()).thenReturn(10L);
        when(vehicleRepository.countByStatus(VehicleStatus.ACTIVE)).thenReturn(6L);
        when(vehicleRepository.countByStatus(VehicleStatus.INACTIVE)).thenReturn(2L);
        when(vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE)).thenReturn(2L);

        when(documentRepository.count()).thenReturn(15L);
        when(documentRepository.countExpiredDocuments(any(LocalDate.class))).thenReturn(3L);
        when(documentRepository.countExpiringSoonDocuments(any(LocalDate.class), any(LocalDate.class))).thenReturn(4L);

        when(maintenanceRepository.count()).thenReturn(8L);
        when(maintenanceRepository.sumTotalCost()).thenReturn(new BigDecimal("1250.50"));
        when(maintenanceRepository.countByMaintenanceDateGreaterThanEqual(any(LocalDate.class))).thenReturn(3L);

        when(driverRepository.count()).thenReturn(5L);
        when(driverRepository.countByStatus(DriverStatus.ACTIVE)).thenReturn(4L);
        when(driverRepository.countByStatus(DriverStatus.INACTIVE)).thenReturn(1L);

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();

        assertThat(summary.getVehicles().totalVehicles()).isEqualTo(10L);
        assertThat(summary.getVehicles().activeVehicles()).isEqualTo(6L);
        assertThat(summary.getVehicles().inactiveVehicles()).isEqualTo(2L);
        assertThat(summary.getVehicles().maintenanceVehicles()).isEqualTo(2L);

        assertThat(summary.getDocuments().totalDocuments()).isEqualTo(15L);
        assertThat(summary.getDocuments().expiredDocuments()).isEqualTo(3L);
        assertThat(summary.getDocuments().expiringSoonDocuments()).isEqualTo(4L);

        assertThat(summary.getMaintenance().totalRecords()).isEqualTo(8L);
        assertThat(summary.getMaintenance().totalCost()).isEqualByComparingTo("1250.50");
        assertThat(summary.getMaintenance().recentRecordsLast30Days()).isEqualTo(3L);

        assertThat(summary.getDrivers().totalDrivers()).isEqualTo(5L);
        assertThat(summary.getDrivers().activeDrivers()).isEqualTo(4L);
        assertThat(summary.getDrivers().inactiveDrivers()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getDashboardSummary returns zeros when database is empty")
    void getDashboardSummary_EmptyDatabase() {
        when(maintenanceRepository.sumTotalCost()).thenReturn(null);

        DashboardSummaryResponse summary = dashboardService.getDashboardSummary();

        assertThat(summary.getVehicles().totalVehicles()).isZero();
        assertThat(summary.getDocuments().totalDocuments()).isZero();
        assertThat(summary.getMaintenance().totalRecords()).isZero();
        assertThat(summary.getMaintenance().totalCost()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getDrivers().totalDrivers()).isZero();
    }
}
