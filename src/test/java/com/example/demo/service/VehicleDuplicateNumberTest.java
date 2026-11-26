package com.example.demo.service;

import com.example.demo.dto.request.VehicleCreateRequest;
import com.example.demo.dto.request.VehicleUpdateRequest;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.exception.DuplicateVehicleNumberException;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Number Uniqueness Rule Tests")
class VehicleDuplicateNumberTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle existingVehicle;

    @BeforeEach
    void setUp() {
        existingVehicle = new Vehicle(
                "VMS-01",
                "STK-1042",
                "Stark Logistics",
                VehicleType.TRUCK,
                "Scania",
                "R500 V8 Highline",
                VehicleStatus.ACTIVE
        );
        existingVehicle.setCreatedAt(Instant.now());
        existingVehicle.setUpdatedAt(Instant.now());
    }

    @Nested
    @DisplayName("Creation Duplicate Number Rule")
    class CreateVehicleDuplicateRule {

        @Test
        @DisplayName("Should throw DuplicateVehicleNumberException when vehicle number already exists (exact match)")
        void createVehicle_ExactDuplicate_ThrowsDuplicateVehicleNumberException() {
            VehicleCreateRequest request = new VehicleCreateRequest(
                    null,
                    "STK-1042",
                    "Stark Distribution",
                    VehicleType.VAN,
                    "Mercedes-Benz",
                    "Sprinter",
                    VehicleStatus.ACTIVE
            );

            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1042")).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.createVehicle(request))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessage("Vehicle with number 'STK-1042' already exists in the fleet.")
                    .satisfies(ex -> {
                        DuplicateVehicleNumberException dupEx = (DuplicateVehicleNumberException) ex;
                        assertThat(dupEx.getVehicleNumber()).isEqualTo("STK-1042");
                    });

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw DuplicateVehicleNumberException when vehicle number matches case-insensitively")
        void createVehicle_CaseInsensitiveDuplicate_ThrowsDuplicateVehicleNumberException() {
            VehicleCreateRequest request = new VehicleCreateRequest(
                    null,
                    "stk-1042",
                    "Stark Logistics",
                    VehicleType.TRUCK,
                    "Scania",
                    "R500",
                    VehicleStatus.ACTIVE
            );

            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1042")).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.createVehicle(request))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("STK-1042")
                    .satisfies(ex -> {
                        DuplicateVehicleNumberException dupEx = (DuplicateVehicleNumberException) ex;
                        assertThat(dupEx.getVehicleNumber()).isEqualTo("STK-1042");
                    });

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should normalize vehicle number by trimming whitespace and uppercase before duplicate check")
        void createVehicle_WhitespacePadded_NormalizesAndChecksDuplicate() {
            VehicleCreateRequest request = new VehicleCreateRequest(
                    null,
                    "   stk-1042   ",
                    "Stark Logistics",
                    VehicleType.TRUCK,
                    "Scania",
                    "R500",
                    VehicleStatus.ACTIVE
            );

            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-1042")).thenReturn(true);

            assertThatThrownBy(() -> vehicleService.createVehicle(request))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessageContaining("STK-1042");

            verify(vehicleRepository).existsByVehicleNumberIgnoreCase("STK-1042");
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should successfully persist vehicle when vehicle number does not exist")
        void createVehicle_UniqueNumber_Succeeds() {
            VehicleCreateRequest request = new VehicleCreateRequest(
                    null,
                    "STK-9999",
                    "Stark Logistics",
                    VehicleType.ELECTRIC,
                    "Volvo",
                    "FE Electric",
                    VehicleStatus.ACTIVE
            );

            when(vehicleRepository.existsByVehicleNumberIgnoreCase("STK-9999")).thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
                Vehicle v = invocation.getArgument(0);
                v.setCreatedAt(Instant.now());
                v.setUpdatedAt(Instant.now());
                return v;
            });

            VehicleResponse response = vehicleService.createVehicle(request);

            assertThat(response).isNotNull();
            assertThat(response.getVehicleNumber()).isEqualTo("STK-9999");
            verify(vehicleRepository).save(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("Update Duplicate Number Rule")
    class UpdateVehicleDuplicateRule {

        @Test
        @DisplayName("Should throw DuplicateVehicleNumberException when updating to another vehicle's number")
        void updateVehicle_DuplicateNumberBelongingToOther_ThrowsException() {
            VehicleUpdateRequest request = new VehicleUpdateRequest(
                    "STK-2019",
                    "Stark Logistics",
                    VehicleType.TRUCK,
                    "Scania",
                    "R500",
                    VehicleStatus.ACTIVE
            );

            when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-2019", "VMS-01"))
                    .thenReturn(true);

            assertThatThrownBy(() -> vehicleService.updateVehicle("VMS-01", request))
                    .isInstanceOf(DuplicateVehicleNumberException.class)
                    .hasMessage("Vehicle with number 'STK-2019' already exists in the fleet.")
                    .satisfies(ex -> {
                        DuplicateVehicleNumberException dupEx = (DuplicateVehicleNumberException) ex;
                        assertThat(dupEx.getVehicleNumber()).isEqualTo("STK-2019");
                    });

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should allow updating vehicle while keeping the same vehicle number")
        void updateVehicle_SameVehicleNumber_SucceedsWithoutDuplicateError() {
            VehicleUpdateRequest request = new VehicleUpdateRequest(
                    "STK-1042",
                    "Stark Logistics Updated",
                    VehicleType.TRUCK,
                    "Scania",
                    "R500 Highline",
                    VehicleStatus.MAINTENANCE
            );

            when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-1042", "VMS-01"))
                    .thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            VehicleResponse response = vehicleService.updateVehicle("VMS-01", request);

            assertThat(response).isNotNull();
            assertThat(response.getVehicleNumber()).isEqualTo("STK-1042");
            assertThat(response.getCompany()).isEqualTo("Stark Logistics Updated");
            assertThat(response.getStatus()).isEqualTo(VehicleStatus.MAINTENANCE);
            verify(vehicleRepository).save(existingVehicle);
        }

        @Test
        @DisplayName("Should allow updating vehicle while changing vehicle number casing without collision")
        void updateVehicle_SameVehicleNumberDifferentCase_Succeeds() {
            VehicleUpdateRequest request = new VehicleUpdateRequest(
                    "stk-1042",
                    "Stark Logistics",
                    VehicleType.TRUCK,
                    "Scania",
                    "R500",
                    VehicleStatus.ACTIVE
            );

            when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.existsByVehicleNumberIgnoreCaseAndIdNot("STK-1042", "VMS-01"))
                    .thenReturn(false);
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

            VehicleResponse response = vehicleService.updateVehicle("VMS-01", request);

            assertThat(response).isNotNull();
            assertThat(response.getVehicleNumber()).isEqualTo("STK-1042");
            verify(vehicleRepository).save(existingVehicle);
        }
    }
}

