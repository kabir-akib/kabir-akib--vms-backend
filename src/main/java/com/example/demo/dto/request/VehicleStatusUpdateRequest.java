package com.example.demo.dto.request;

import com.example.demo.model.enums.VehicleStatus;
import jakarta.validation.constraints.NotNull;

public class VehicleStatusUpdateRequest {

    @NotNull(message = "Vehicle status is required")
    private VehicleStatus status;

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
