package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.AuthUserResponse;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Signed in successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            authService.logout(authHeader.substring(BEARER_PREFIX.length()).trim());
        }
        return ResponseEntity.ok(ApiResponse.success("Signed out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getCurrentUser(Authentication authentication) {
        AuthUserResponse user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
