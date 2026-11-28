package com.example.demo.service;

import com.example.demo.dto.response.PagedResponse;
import com.example.demo.dto.response.VehicleResponse;
import com.example.demo.exception.InvalidOperationException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Pagination & Filtering Service Unit Tests")
class VehiclePaginationServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleServiceImpl vehicleService;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository);
        sampleVehicle = new Vehicle(
                "VMS-01", "STK-1042", "Stark Logistics",
                VehicleType.TRUCK, "Scania", "R500", VehicleStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("getVehiclesPaginated returns PagedResponse with metadata")
    void getVehiclesPaginated_Success() {
        Pageable pageable = PageRequest.of(0, 5);
        when(vehicleRepository.findFilteredVehicles(
                isNull(), eq("STK"), isNull(), eq(VehicleType.TRUCK),
                isNull(), isNull(), eq(VehicleStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleVehicle), pageable, 1));

        PagedResponse<VehicleResponse> result = vehicleService.getVehiclesPaginated(
                0, 5, "vehicleNumber", "asc", null, "STK", null,
                VehicleType.TRUCK, null, null, VehicleStatus.ACTIVE);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrevious()).isFalse();
    }

    @Test
    @DisplayName("getVehiclesPaginated rejects invalid page, size, sortBy, and sortDir")
    void getVehiclesPaginated_InvalidParams_ThrowsInvalidOperation() {
        assertThatThrownBy(() -> vehicleService.getVehiclesPaginated(
                -1, 10, "createdAt", "desc", null, null, null, null, null, null, null))
                .isInstanceOf(InvalidOperationException.class);

        assertThatThrownBy(() -> vehicleService.getVehiclesPaginated(
                0, 0, "createdAt", "desc", null, null, null, null, null, null, null))
                .isInstanceOf(InvalidOperationException.class);

        assertThatThrownBy(() -> vehicleService.getVehiclesPaginated(
                0, 10, "unknownField", "desc", null, null, null, null, null, null, null))
                .isInstanceOf(InvalidOperationException.class);

        assertThatThrownBy(() -> vehicleService.getVehiclesPaginated(
                0, 10, "createdAt", "sideways", null, null, null, null, null, null, null))
                .isInstanceOf(InvalidOperationException.class);
    }
}
