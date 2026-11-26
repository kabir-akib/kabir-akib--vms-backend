package com.example.demo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Server-side opaque Bearer token entity for stateless API authentication.
 */
@Entity
@Table(
        name = "auth_tokens",
        indexes = {
                @Index(name = "idx_auth_tokens_token", columnList = "token", unique = true)
        }
)
public class AuthToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 64)
    private String id;

    @Column(name = "token", nullable = false, unique = true, length = 128)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AuthToken() {
    }

    public AuthToken(String token, User user, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public boolean isValid() {
        return !revoked && expiresAt != null && expiresAt.isAfter(Instant.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
