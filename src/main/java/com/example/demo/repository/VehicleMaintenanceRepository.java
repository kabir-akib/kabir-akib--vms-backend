package com.example.demo.repository;

import com.example.demo.model.entity.VehicleMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleMaintenanceRepository extends JpaRepository<VehicleMaintenance, String> {

    @Query("SELECT m FROM VehicleMaintenance m JOIN FETCH m.vehicle " +
           "WHERE m.vehicle.id = :vehicleId " +
           "ORDER BY m.maintenanceDate DESC, m.createdAt DESC")
    List<VehicleMaintenance> findByVehicleIdOrderByNewestFirst(@Param("vehicleId") String vehicleId);

    @Query("SELECT m FROM VehicleMaintenance m JOIN FETCH m.vehicle " +
           "WHERE m.id = :id AND m.vehicle.id = :vehicleId")
    Optional<VehicleMaintenance> findByIdAndVehicleId(@Param("id") String id, @Param("vehicleId") String vehicleId);

    @Query("SELECT COALESCE(SUM(m.cost), 0) FROM VehicleMaintenance m")
    java.math.BigDecimal sumTotalCost();

    long countByMaintenanceDateGreaterThanEqual(java.time.LocalDate sinceDate);
}
