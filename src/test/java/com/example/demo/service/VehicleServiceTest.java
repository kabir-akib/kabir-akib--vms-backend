package com.example.demo.service;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.FleetMetricsResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.DuplicateVehicleNumberException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        sampleVehicle = new Vehicle("VMS-01", "STK-1042", "Stark Logistics",
                VehicleType.TRUCK, "Scania", "R500 V8 Highline", VehicleStatus.ACTIVE);
        sampleVehicle.setCreatedAt(Instant.now());
        sampleVehicle.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("Should successfully register a new vehicle")
    void createVehicle_Success() {
        VehicleCreateRequest request = new VehicleCreateRequest(
                "VMS-01", "stk-1042", "Stark Logistics",
                VehicleType.TRUCK, "Scania", "R500 V8 Highline", VehicleStatus.ACTIVE);

        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1042")).thenReturn(false);
        when(vehicleRepository.existsById("VMS-01")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        VehicleResponse response = vehicleService.createVehicle(request);

        assertThat(response).isNotNull();
        assertThat(response.getVehicleNumber()).isEqualTo("STK-1042");
        assertThat(response.getCompany()).isEqualTo("Stark Logistics");
        assertThat(response.getType()).isEqualTo(VehicleType.TRUCK);
        assertThat(response.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when vehicle number already exists")
    void createVehicle_DuplicateNumber_ThrowsException() {
        VehicleCreateRequest request = new VehicleCreateRequest(
                null, "STK-1042", "Stark Logistics",
                VehicleType.TRUCK, "Scania", "R500", VehicleStatus.ACTIVE);

        when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1042")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(request))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessageContaining("STK-1042");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve vehicle by ID successfully")
    void getVehicleById_Success() {
        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));

        VehicleResponse response = vehicleService.getVehicleById("VMS-01");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("VMS-01");
        assertThat(response.getVehicleNumber()).isEqualTo("STK-1042");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when vehicle ID does not exist")
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getVehicleById("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    @DisplayName("Should update an existing vehicle successfully")
    void updateVehicle_Success() {
        VehicleUpdateRequest updateRequest = new VehicleUpdateRequest(
                "STK-1042", "Stark Nordic Transport", VehicleType.HEAVY_HAULER,
                "Volvo", "FH16", VehicleStatus.MAINTENANCE);

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-1042", "VMS-01")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse updated = vehicleService.updateVehicle("VMS-01", updateRequest);

        assertThat(updated.getCompany()).isEqualTo("Stark Nordic Transport");
        assertThat(updated.getType()).isEqualTo(VehicleType.HEAVY_HAULER);
        assertThat(updated.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
        verify(vehicleRepository).save(sampleVehicle);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating with another vehicle's number")
    void updateVehicle_DuplicateNumber_ThrowsException() {
        VehicleUpdateRequest updateRequest = new VehicleUpdateRequest(
                "STK-9999", "Stark Logistics", VehicleType.TRUCK, "Scania", "R500", VehicleStatus.ACTIVE);

        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-9999", "VMS-01")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.updateVehicle("VMS-01", updateRequest))
                .isInstanceOf(DuplicateVehicleNumberException.class)
                .hasMessageContaining("STK-9999");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete vehicle when found")
    void deleteVehicle_Success() {
        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));

        vehicleService.deleteVehicle("VMS-01");

        verify(vehicleRepository).delete(sampleVehicle);
    }

    @Test
    @DisplayName("Should correctly calculate fleet operational metrics")
    void getFleetMetrics_Success() {
        when(vehicleRepository.count()).thenReturn(10L);
        when(vehicleRepository.countByStatus(VehicleStatus.ACTIVE)).thenReturn(6L);
        when(vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE)).thenReturn(3L);
        when(vehicleRepository.countByStatus(VehicleStatus.INACTIVE)).thenReturn(1L);

        FleetMetricsResponse metrics = vehicleService.getFleetMetrics();

        assertThat(metrics.getTotalVehicles()).isEqualTo(10L);
        assertThat(metrics.getActiveVehicles()).isEqualTo(6L);
        assertThat(metrics.getMaintenanceVehicles()).isEqualTo(3L);
        assertThat(metrics.getInactiveVehicles()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should search and filter vehicles")
    void getVehicles_Filtered() {
        when(vehicleRepository.searchVehicles(eq("Scania"), eq(VehicleStatus.ACTIVE), eq("Stark Logistics"), any(Sort.class)))
                .thenReturn(List.of(sampleVehicle));

        List<VehicleResponse> list = vehicleService.getVehicles("Scania", VehicleStatus.ACTIVE, "Stark Logistics");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getBrand()).isEqualTo("Scania");
    }

    @Test
    @DisplayName("Should retrieve available companies")
    void getAvailableCompanies_Success() {
        when(vehicleRepository.findDistinctCompanies()).thenReturn(List.of("Stark Distribution", "Stark Logistics"));

        List<String> companies = vehicleService.getAvailableCompanies();

        assertThat(companies).containsExactly("Stark Distribution", "Stark Logistics");
    }

    @Test
    @DisplayName("Vehicle entity should have @Id and @GeneratedValue(strategy = GenerationType.UUID)")
    void vehicleEntity_HasGeneratedValueAnnotation() throws NoSuchFieldException {
        var idField = Vehicle.class.getDeclaredField("id");
        assertThat(idField.isAnnotationPresent(jakarta.persistence.Id.class)).isTrue();
        assertThat(idField.isAnnotationPresent(jakarta.persistence.GeneratedValue.class)).isTrue();
        jakarta.persistence.GeneratedValue generatedValue = idField.getAnnotation(jakarta.persistence.GeneratedValue.class);
        assertThat(generatedValue.strategy()).isEqualTo(jakarta.persistence.GenerationType.UUID);
    }
}

