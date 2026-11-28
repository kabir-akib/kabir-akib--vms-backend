package com.example.demo.service;

import com.example.demo.dto.response.VehicleResponse;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Status & Search Service Unit Tests")
class VehicleStatusAndSearchServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleServiceImpl vehicleService;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository);
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
    @DisplayName("updateVehicleStatus updates status to ACTIVE, INACTIVE, or MAINTENANCE")
    void updateVehicleStatus_Success() {
        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        VehicleResponse maintenance = vehicleService.updateVehicleStatus("VMS-01", VehicleStatus.MAINTENANCE);
        assertThat(maintenance.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);

        VehicleResponse inactive = vehicleService.updateVehicleStatus("VMS-01", VehicleStatus.INACTIVE);
        assertThat(inactive.getStatus()).isEqualTo(VehicleStatus.INACTIVE);

        VehicleResponse active = vehicleService.updateVehicleStatus("VMS-01", VehicleStatus.ACTIVE);
        assertThat(active.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
    }

    @Test
    @DisplayName("updateVehicleStatus throws ResourceNotFoundException for unknown vehicle")
    void updateVehicleStatus_UnknownVehicle_ThrowsNotFound() {
        when(vehicleRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.updateVehicleStatus("UNKNOWN", VehicleStatus.INACTIVE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("searchVehicles trims blank parameters to null and delegates to repository")
    void searchVehicles_TrimsBlankParamsAndDelegates() {
        when(vehicleRepository.advancedSearchVehicles(
                isNull(),
                eq("STK-10"),
                eq("Stark"),
                eq(VehicleType.TRUCK),
                isNull(),
                isNull(),
                eq(VehicleStatus.ACTIVE),
                any(Sort.class)))
                .thenReturn(List.of(sampleVehicle));

        List<VehicleResponse> results = vehicleService.searchVehicles(
                "   ",
                " STK-10 ",
                " Stark ",
                VehicleType.TRUCK,
                "",
                null,
                VehicleStatus.ACTIVE
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getVehicleNumber()).isEqualTo("STK-1042");
        verify(vehicleRepository).advancedSearchVehicles(
                isNull(),
                eq("STK-10"),
                eq("Stark"),
                eq(VehicleType.TRUCK),
                isNull(),
                isNull(),
                eq(VehicleStatus.ACTIVE),
                any(Sort.class));
    }
}
