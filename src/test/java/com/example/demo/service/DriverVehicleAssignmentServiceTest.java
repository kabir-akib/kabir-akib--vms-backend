package com.example.demo.service;

import com.example.demo.dto.request.AssignmentRequest;
import com.example.demo.dto.response.AssignmentResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.Driver;
import com.example.demo.model.entity.DriverVehicleAssignment;
import com.example.demo.model.entity.Vehicle;
import com.example.demo.model.enums.AssignmentStatus;
import com.example.demo.model.enums.DriverStatus;
import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import com.example.demo.repository.DriverRepository;
import com.example.demo.repository.DriverVehicleAssignmentRepository;
import com.example.demo.repository.VehicleRepository;
import com.example.demo.service.impl.DriverVehicleAssignmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DriverVehicleAssignmentService Unit Tests")
class DriverVehicleAssignmentServiceTest {

    @Mock
    private DriverVehicleAssignmentRepository assignmentRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private DriverVehicleAssignmentServiceImpl assignmentService;
    private Driver sampleDriver;
    private Vehicle sampleVehicle;

    @BeforeEach
    void setUp() {
        assignmentService = new DriverVehicleAssignmentServiceImpl(
                assignmentRepository, driverRepository, vehicleRepository);

        sampleDriver = new Driver();
        sampleDriver.setId("drv-1");
        sampleDriver.setName("John Doe");
        sampleDriver.setDrivingLicenceNumber("DL-1001");
        sampleDriver.setStatus(DriverStatus.ACTIVE);

        sampleVehicle = new Vehicle(
                "VMS-01", "STK-1042", "Stark Logistics",
                VehicleType.TRUCK, "Scania", "R500", VehicleStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("createAssignment succeeds when driver and vehicle exist and have no active assignments")
    void createAssignment_Success() {
        AssignmentRequest req = new AssignmentRequest();
        req.setDriverId("drv-1");
        req.setVehicleId("VMS-01");
        req.setAssignedDate(LocalDate.of(2026, 11, 28));

        when(driverRepository.findById("drv-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(assignmentRepository.existsByDriverIdAndStatus("drv-1", AssignmentStatus.ACTIVE)).thenReturn(false);
        when(assignmentRepository.existsByVehicleIdAndStatus("VMS-01", AssignmentStatus.ACTIVE)).thenReturn(false);
        when(assignmentRepository.save(any(DriverVehicleAssignment.class))).thenAnswer(inv -> {
            DriverVehicleAssignment a = inv.getArgument(0);
            a.setId("asgn-1");
            a.setCreatedAt(Instant.now());
            a.setUpdatedAt(Instant.now());
            return a;
        });

        AssignmentResponse res = assignmentService.createAssignment(req);

        assertThat(res.getId()).isEqualTo("asgn-1");
        assertThat(res.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
        assertThat(res.getDriverId()).isEqualTo("drv-1");
        assertThat(res.getVehicleId()).isEqualTo("VMS-01");
    }

    @Test
    @DisplayName("createAssignment rejects when driver already has an active assignment")
    void createAssignment_DriverAlreadyAssigned_ThrowsInvalidOperation() {
        AssignmentRequest req = new AssignmentRequest();
        req.setDriverId("drv-1");
        req.setVehicleId("VMS-01");
        req.setAssignedDate(LocalDate.now());

        when(driverRepository.findById("drv-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(assignmentRepository.existsByDriverIdAndStatus("drv-1", AssignmentStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> assignmentService.createAssignment(req))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("createAssignment rejects when vehicle already has an active assignment")
    void createAssignment_VehicleAlreadyAssigned_ThrowsInvalidOperation() {
        AssignmentRequest req = new AssignmentRequest();
        req.setDriverId("drv-1");
        req.setVehicleId("VMS-01");
        req.setAssignedDate(LocalDate.now());

        when(driverRepository.findById("drv-1")).thenReturn(Optional.of(sampleDriver));
        when(vehicleRepository.findById("VMS-01")).thenReturn(Optional.of(sampleVehicle));
        when(assignmentRepository.existsByDriverIdAndStatus("drv-1", AssignmentStatus.ACTIVE)).thenReturn(false);
        when(assignmentRepository.existsByVehicleIdAndStatus("VMS-01", AssignmentStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> assignmentService.createAssignment(req))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("createAssignment throws ResourceNotFoundException for unknown driver or vehicle")
    void createAssignment_UnknownDriverOrVehicle_ThrowsNotFound() {
        AssignmentRequest req = new AssignmentRequest();
        req.setDriverId("UNKNOWN");
        req.setVehicleId("VMS-01");
        req.setAssignedDate(LocalDate.now());

        when(driverRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assignmentService.createAssignment(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("endAssignment transitions ACTIVE to INACTIVE and rejects repeated end")
    void endAssignment_SuccessAndRejectsRepeated() {
        DriverVehicleAssignment active = new DriverVehicleAssignment();
        active.setId("asgn-1");
        active.setDriver(sampleDriver);
        active.setVehicle(sampleVehicle);
        active.setAssignedDate(LocalDate.now().minusDays(5));
        active.setStatus(AssignmentStatus.ACTIVE);

        when(assignmentRepository.findByIdWithDetails("asgn-1")).thenReturn(Optional.of(active));
        when(assignmentRepository.save(any(DriverVehicleAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        AssignmentResponse ended = assignmentService.endAssignment("asgn-1");
        assertThat(ended.getStatus()).isEqualTo(AssignmentStatus.INACTIVE);
        assertThat(ended.getEndDate()).isNotNull();

        assertThatThrownBy(() -> assignmentService.endAssignment("asgn-1"))
                .isInstanceOf(InvalidOperationException.class);
    }
}
