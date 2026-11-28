package com.example.demo.service;

import com.example.demo.dto.request.DriverRequest;
import com.example.demo.dto.response.DriverResponse;

import java.util.List;

public interface DriverService {

    DriverResponse createDriver(DriverRequest request);

    List<DriverResponse> getAllDrivers();

    DriverResponse getDriverById(String id);

    DriverResponse updateDriver(String id, DriverRequest request);

    void deleteDriver(String id);
}
