package com.example.demo.service;

import com.example.demo.dto.request.VehicleMaintenanceRequest;
import com.example.demo.dto.response.VehicleMaintenanceResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.entity.VehicleMaintenance;
import com.example.demo.model.enums.MaintenanceType;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.VehicleMaintenanceRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.VehicleMaintenanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleMaintenanceService Unit Tests")
class VehicleMaintenanceServiceTest {

    @Mock
    private VehicleMaintenanceRepository maintenanceRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleMaintenanceServiceImpl maintenanceService;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        maintenanceService = new VehicleMaintenanceServiceImpl(maintenanceRepository, vehicleRepository);
        sampleVehicle = new Vehicle(
                "VMS-01",
                "STK-1042",
                "Stark Logistics",
                VehicleType.TRUCK,
                "Scania",
                "R500",
                VehicleStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("createMaintenance saves and returns maintenance record for existing vehicle")
    void createMaintenance_Success() {
        VehicleMaintenanceRequest request = buildRequest(
                LocalDate.of(2026, 11, 20),
                MaintenanceType.OIL_CHANGE,
                new BigDecimal("150.00"),
                "Stark Auto Workshop",
                "Synthetic oil replacement"
        );

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(maintenanceRepository.save(any(VehicleMaintenance.class))).thenAnswer(invocation -> {
            VehicleMaintenance m = invocation.getArgument(0);
            m.setId("maint-1");
            m.setCreatedAt(Instant.now());
            m.setUpdatedAt(Instant.now());
            return m;
        });

        VehicleMaintenanceResponse response = maintenanceService.createMaintenance("VMS-01", request);

        assertThat(response.getId()).isEqualTo("maint-1");
        assertThat(response.getVehicleId()).isEqualTo("VMS-01");
        assertThat(response.getMaintenanceType()).isEqualTo(MaintenanceType.OIL_CHANGE);
        assertThat(response.getCost()).isEqualByComparingTo("150.00");
        assertThat(response.getVendorName()).isEqualTo("Stark Auto Workshop");
    }

    @Test
    @DisplayName("createMaintenance throws ResourceNotFoundException for unknown vehicle")
    void createMaintenance_UnknownVehicle_ThrowsNotFound() {
        VehicleMaintenanceRequest request = buildRequest(
                LocalDate.of(2026, 11, 20),
                MaintenanceType.REPAIR,
                new BigDecimal("200.00"),
                "Workshop",
                null
        );
        when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.createMaintenance("UNKNOWN", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getMaintenanceHistory returns records in repository order for existing vehicle")
    void getMaintenanceHistory_Success() {
        VehicleMaintenance newer = buildEntity("maint-2", LocalDate.of(2026, 11, 25), MaintenanceType.INSPECTION);
        VehicleMaintenance older = buildEntity("maint-1", LocalDate.of(2026, 11, 10), MaintenanceType.ROUTINE_SERVICE);

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(maintenanceRepository.findByVehicleIdOrderByNewestFirst("VMS-01"))
                .thenReturn(List.of(newer, older));

        List<VehicleMaintenanceResponse> history = maintenanceService.getMaintenanceHistory("VMS-01");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getId()).isEqualTo("maint-2");
        assertThat(history.get(1).getId()).isEqualTo("maint-1");
    }

    @Test
    @DisplayName("getMaintenanceById throws ResourceNotFoundException when record belongs to another vehicle")
    void getMaintenanceById_WrongVehicle_ThrowsNotFound() {
        when(vehicleRepository.existsById("VMS-02")).thenReturn(true);
        when(maintenanceRepository.findByIdAndVehicleId("maint-1", "VMS-02")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.getMaintenanceById("VMS-02", "maint-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateMaintenance updates existing record belonging to vehicle")
    void updateMaintenance_Success() {
        VehicleMaintenance existing = buildEntity("maint-1", LocalDate.of(2026, 11, 10), MaintenanceType.REPAIR);
        VehicleMaintenanceRequest updateReq = buildRequest(
                LocalDate.of(2026, 11, 12),
                MaintenanceType.ROUTINE_SERVICE,
                new BigDecimal("320.50"),
                "Updated Garage",
                "Updated notes"
        );

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(maintenanceRepository.findByIdAndVehicleId("maint-1", "VMS-01")).thenReturn(Optional.of(existing));
        when(maintenanceRepository.save(any(VehicleMaintenance.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleMaintenanceResponse updated = maintenanceService.updateMaintenance("VMS-01", "maint-1", updateReq);

        assertThat(updated.getMaintenanceType()).isEqualTo(MaintenanceType.ROUTINE_SERVICE);
        assertThat(updated.getCost()).isEqualByComparingTo("320.50");
        assertThat(updated.getVendorName()).isEqualTo("Updated Garage");
    }

    @Test
    @DisplayName("deleteMaintenance deletes existing record belonging to vehicle")
    void deleteMaintenance_Success() {
        VehicleMaintenance existing = buildEntity("maint-1", LocalDate.of(2026, 11, 10), MaintenanceType.OTHER);

        when(vehicleRepository.existsById("VMS-01")).thenReturn(true);
        when(maintenanceRepository.findByIdAndVehicleId("maint-1", "VMS-01")).thenReturn(Optional.of(existing));

        maintenanceService.deleteMaintenance("VMS-01", "maint-1");

        verify(maintenanceRepository).delete(existing);
    }

    private VehicleMaintenanceRequest buildRequest(LocalDate date, MaintenanceType type, BigDecimal cost,
                                                   String vendor, String notes) {
        VehicleMaintenanceRequest req = new VehicleMaintenanceRequest();
        req.setMaintenanceDate(date);
        req.setMaintenanceType(type);
        req.setCost(cost);
        req.setVendorName(vendor);
        req.setNotes(notes);
        return req;
    }

    private VehicleMaintenance buildEntity(String id, LocalDate date, MaintenanceType type) {
        VehicleMaintenance m = new VehicleMaintenance();
        m.setId(id);
        m.setVehicle(sampleVehicle);
        m.setMaintenanceDate(date);
        m.setMaintenanceType(type);
        m.setCost(new BigDecimal("100.00"));
        m.setVendorName("Stark Garage");
        m.setNotes("Routine check");
        m.setCreatedAt(Instant.now());
        m.setUpdatedAt(Instant.now());
        return m;
    }
}
