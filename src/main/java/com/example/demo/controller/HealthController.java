package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lightweight application-layer health check endpoint.
 *
 * <p>Complements Spring Actuator's {@code /actuator/health} with a simpler,
 * API-versioned response at {@code GET /api/v1/health} that:
 * <ul>
 *   <li>Confirms the application is running and reachable</li>
 *   <li>Returns the active Spring profile and application name</li>
 *   <li>Can be called without authentication in later security configurations</li>
 * </ul>
 *
 * <p>No database queries are performed here — use {@code /actuator/health}
 * for datasource/JPA liveness details.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @Value("${spring.application.name:vms-backend}")
    private String applicationName;

    @Value("${info.app.version:unknown}")
    private String applicationVersion;

    @GetMapping
    public ResponseEntity<Map<String, Object>> check() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", applicationName);
        body.put("version", applicationVersion);
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}

