package com.example.demo.repository;

import com.example.demo.model.entity.DriverVehicleAssignment;
import com.example.demo.model.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DriverVehicleAssignmentRepository extends JpaRepository<DriverVehicleAssignment, String> {

    boolean existsByDriverIdAndStatus(String driverId, AssignmentStatus status);

    boolean existsByVehicleIdAndStatus(String vehicleId, AssignmentStatus status);

    @Query("SELECT a FROM DriverVehicleAssignment a JOIN FETCH a.driver JOIN FETCH a.vehicle " +
           "ORDER BY a.assignedDate DESC, a.createdAt DESC")
    List<DriverVehicleAssignment> findAllWithDetails();

    @Query("SELECT a FROM DriverVehicleAssignment a JOIN FETCH a.driver JOIN FETCH a.vehicle " +
           "WHERE a.id = :id")
    Optional<DriverVehicleAssignment> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT a FROM DriverVehicleAssignment a JOIN FETCH a.driver JOIN FETCH a.vehicle " +
           "WHERE a.vehicle.id = :vehicleId ORDER BY a.assignedDate DESC, a.createdAt DESC")
    List<DriverVehicleAssignment> findByVehicleIdWithDetails(@Param("vehicleId") String vehicleId);

    @Query("SELECT a FROM DriverVehicleAssignment a JOIN FETCH a.driver JOIN FETCH a.vehicle " +
           "WHERE a.driver.id = :driverId ORDER BY a.assignedDate DESC, a.createdAt DESC")
    List<DriverVehicleAssignment> findByDriverIdWithDetails(@Param("driverId") String driverId);
}
