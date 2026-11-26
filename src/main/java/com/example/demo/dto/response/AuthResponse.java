package com.example.demo.dto.response;

import java.time.Instant;

public class AuthResponse {

    private String token;
    private String tokenType;
    private Instant expiresAt;
    private AuthUserResponse user;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tokenType, Instant expiresAt, AuthUserResponse user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public AuthUserResponse getUser() {
        return user;
    }

    public void setUser(AuthUserResponse user) {
        this.user = user;
    }
}
