package com.example.demo.model.entity;

import com.example.demo.model.enums.VehicleStatus;
import com.example.demo.model.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "vehicles",
        indexes = {
                @Index(name = "idx_vehicles_number", columnList = "vehicle_number", unique = true),
                @Index(name = "idx_vehicles_company", columnList = "company"),
                @Index(name = "idx_vehicles_status", columnList = "status")
        }
)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 64)
    private String id;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 32)
    private String vehicleNumber;

    @Column(name = "company", nullable = false, length = 120)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private VehicleType type;

    @Column(name = "brand", nullable = false, length = 80)
    private String brand;

    @Column(name = "model", nullable = false, length = 80)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private VehicleStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Vehicle() {
    }

    public Vehicle(String vehicleNumber, String company, VehicleType type,
                   String brand, String model, VehicleStatus status) {
        this(null, vehicleNumber, company, type, brand, model, status);
    }

    public Vehicle(String id, String vehicleNumber, String company, VehicleType type,
                   String brand, String model, VehicleStatus status) {
        this.id = id;
        this.vehicleNumber = vehicleNumber;
        this.company = company;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = VehicleStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDisplayName() {
        return (brand != null ? brand : "") + " " + (model != null ? model : "").trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", company='" + company + '\'' +
                ", type=" + type +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

