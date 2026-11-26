package com.example.demo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Test
    @DisplayName("GET /api/v1/health - should return 200 OK with status=UP")
    void check_ReturnsUp() {
        ResponseEntity<Map<String, Object>> response = healthController.check();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("status", "UP");
        assertThat(response.getBody()).containsKey("application");
        assertThat(response.getBody()).containsKey("version");
        assertThat(response.getBody()).containsKey("timestamp");
    }
}

