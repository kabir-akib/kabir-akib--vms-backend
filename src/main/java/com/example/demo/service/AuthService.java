package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.AuthUserResponse;
import com.example.demo.model.entity.User;

import java.util.Optional;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    void logout(String token);

    AuthUserResponse getCurrentUser(String email);

    Optional<User> validateToken(String token);
}
