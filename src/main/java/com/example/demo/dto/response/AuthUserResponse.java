package com.example.demo.dto.response;

import com.example.demo.model.entity.User;

public class AuthUserResponse {

    private String id;
    private String email;
    private String fullName;
    private String role;

    public AuthUserResponse() {
    }

    public AuthUserResponse(String id, String email, String fullName, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public static AuthUserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
