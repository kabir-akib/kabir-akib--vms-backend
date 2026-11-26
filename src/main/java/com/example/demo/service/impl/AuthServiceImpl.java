package com.example.demo.service.impl;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.AuthResponse;
import com.example.demo.dto.response.AuthUserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.entity.AuthToken;
import com.example.demo.model.entity.User;
import com.example.demo.repository.AuthTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final Duration TOKEN_TTL = Duration.ofHours(24);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           AuthTokenRepository authTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String opaqueToken = generateOpaqueToken();
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        authTokenRepository.save(new AuthToken(opaqueToken, user, expiresAt));

        log.info("Authenticated user email={} role={}", user.getEmail(), user.getRole());
        return new AuthResponse(opaqueToken, "Bearer", expiresAt, AuthUserResponse.fromEntity(user));
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        authTokenRepository.findByToken(token.trim()).ifPresent(authToken -> {
            authToken.setRevoked(true);
            authTokenRepository.save(authToken);
            log.info("Revoked auth token for user email={}", authToken.getUser().getEmail());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return AuthUserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> validateToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return authTokenRepository.findByToken(token.trim())
                .filter(AuthToken::isValid)
                .map(AuthToken::getUser)
                .filter(User::isEnabled);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
