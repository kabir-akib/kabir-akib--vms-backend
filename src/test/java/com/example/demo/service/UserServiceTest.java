package com.example.demo.service;

import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.UserSummaryResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.model.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("createUser hashes password with BCrypt and returns safe summary")
    void createUser_Success() {
        UserCreateRequest req = new UserCreateRequest();
        req.setName("Jane Employee");
        req.setEmail("Jane@StarkGroup.com");
        req.setPassword("secret123");
        req.setRole("EMPLOYEE");

        when(userRepository.existsByEmailIgnoreCase("jane@starkgroup.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("usr-1");
            u.setCreatedAt(Instant.now());
            return u;
        });

        UserSummaryResponse res = userService.createUser(req);

        assertThat(res.getId()).isEqualTo("usr-1");
        assertThat(res.getEmail()).isEqualTo("jane@starkgroup.com");
        assertThat(res.getName()).isEqualTo("Jane Employee");
        assertThat(res.getRole()).isEqualTo("EMPLOYEE");
        assertThat(res.isEnabled()).isTrue();
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    @DisplayName("createUser rejects duplicate email")
    void createUser_DuplicateEmail_ThrowsInvalidOperation() {
        UserCreateRequest req = new UserCreateRequest();
        req.setName("Duplicate");
        req.setEmail("admin@starkgroup.com");
        req.setPassword("secret123");
        req.setRole("EMPLOYEE");

        when(userRepository.existsByEmailIgnoreCase("admin@starkgroup.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("getAllUsers returns safe user summaries")
    void getAllUsers_Success() {
        User u = new User("emp@starkgroup.com", "hash", "Emp", "EMPLOYEE");
        u.setId("usr-2");
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(u));

        List<UserSummaryResponse> users = userService.getAllUsers();
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("emp@starkgroup.com");
    }
}
