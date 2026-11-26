package com.example.demo.exception;

/**
 * Exception thrown when an attempt is made to register or update a vehicle
 * with a vehicle number that is already in use within the fleet.
 */
public class DuplicateVehicleNumberException extends DuplicateResourceException {

    private final String vehicleNumber;

    public DuplicateVehicleNumberException(String vehicleNumber) {
        super(String.format("Vehicle with number '%s' already exists in the fleet.", vehicleNumber));
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
}

