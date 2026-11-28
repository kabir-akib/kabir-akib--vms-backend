package com.example.demo.service;

import com.example.demo.dto.request.AssignmentRequest;
import com.example.demo.dto.response.AssignmentResponse;

import java.util.List;

public interface DriverVehicleAssignmentService {

    AssignmentResponse createAssignment(AssignmentRequest request);

    List<AssignmentResponse> getAllAssignments();

    AssignmentResponse getAssignmentById(String id);

    AssignmentResponse endAssignment(String id);

    List<AssignmentResponse> getAssignmentsForVehicle(String vehicleId);

    List<AssignmentResponse> getAssignmentsForDriver(String driverId);
}
