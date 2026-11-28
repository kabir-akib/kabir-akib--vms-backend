package com.example.demo.service;

import com.example.demo.dto.request.DriverRequest;
import com.example.demo.dto.response.DriverResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Driver;
import com.example.demo.model.enums.DriverStatus;
import com.example.demo.repository.DriverRepository;
import com.example.demo.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("DriverService Unit Tests")
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    private DriverServiceImpl driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverServiceImpl(driverRepository);
    }

    @Test
    @DisplayName("createDriver saves and returns new driver")
    void createDriver_Success() {
        DriverRequest req = buildRequest("DL-1001");
        when(driverRepository.existsByDrivingLicenceNumberIgnoreCase("DL-1001")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> {
            Driver d = inv.getArgument(0);
            d.setId("drv-1");
            d.setCreatedAt(Instant.now());
            d.setUpdatedAt(Instant.now());
            return d;
        });

        DriverResponse res = driverService.createDriver(req);

        assertThat(res.getId()).isEqualTo("drv-1");
        assertThat(res.getDrivingLicenceNumber()).isEqualTo("DL-1001");
        assertThat(res.getStatus()).isEqualTo(DriverStatus.ACTIVE);
    }

    @Test
    @DisplayName("createDriver rejects duplicate driving licence number")
    void createDriver_DuplicateLicence_ThrowsInvalidOperation() {
        DriverRequest req = buildRequest("DL-1001");
        when(driverRepository.existsByDrivingLicenceNumberIgnoreCase("DL-1001")).thenReturn(true);

        assertThatThrownBy(() -> driverService.createDriver(req))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("getDriverById throws ResourceNotFoundException for unknown id")
    void getDriverById_Unknown_ThrowsNotFound() {
        when(driverRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getDriverById("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getAllDrivers returns list of drivers")
    void getAllDrivers_Success() {
        Driver d = new Driver();
        d.setId("drv-1");
        d.setName("John Doe");
        d.setPhoneNumber("+8801711111111");
        d.setDrivingLicenceNumber("DL-1001");
        d.setLicenceExpiryDate(LocalDate.now().plusYears(2));
        d.setAddress("Dhaka");
        d.setStatus(DriverStatus.ACTIVE);

        when(driverRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(d));

        assertThat(driverService.getAllDrivers()).hasSize(1);
    }

    @Test
    @DisplayName("deleteDriver deletes existing driver")
    void deleteDriver_Success() {
        Driver d = new Driver();
        d.setId("drv-1");
        d.setDrivingLicenceNumber("DL-1001");
        when(driverRepository.findById("drv-1")).thenReturn(Optional.of(d));

        driverService.deleteDriver("drv-1");

        verify(driverRepository).delete(d);
    }

    private DriverRequest buildRequest(String licence) {
        DriverRequest req = new DriverRequest();
        req.setName("John Doe");
        req.setPhoneNumber("+8801711111111");
        req.setDrivingLicenceNumber(licence);
        req.setLicenceExpiryDate(LocalDate.now().plusYears(2));
        req.setAddress("123 Stark Tower, Dhaka");
        req.setStatus(DriverStatus.ACTIVE);
        return req;
    }
}
