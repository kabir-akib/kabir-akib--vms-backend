package com.example.demo.controller;

import com.example.demo.dto.request.AssignmentRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AssignmentResponse;
import com.example.demo.service.DriverVehicleAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DriverVehicleAssignmentController {

    private final DriverVehicleAssignmentService assignmentService;

    public DriverVehicleAssignmentController(DriverVehicleAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<AssignmentResponse>> createAssignment(
            @Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse created = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver assigned to vehicle successfully", created));
    }

    @GetMapping("/assignments")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAllAssignments() {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAllAssignments()));
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getAssignmentById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentById(id)));
    }

    @PatchMapping("/assignments/{id}/end")
    public ResponseEntity<ApiResponse<AssignmentResponse>> endAssignment(@PathVariable String id) {
        AssignmentResponse ended = assignmentService.endAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment ended successfully", ended));
    }

    @GetMapping("/vehicles/{vehicleId}/assignments")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getVehicleAssignments(
            @PathVariable String vehicleId) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentsForVehicle(vehicleId)));
    }

    @GetMapping("/drivers/{driverId}/assignments")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getDriverAssignments(
            @PathVariable String driverId) {
        return ResponseEntity.ok(ApiResponse.success(assignmentService.getAssignmentsForDriver(driverId)));
    }
}
