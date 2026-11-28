package com.example.demo.service.impl;

import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.UserSummaryResponse;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.model.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserSummaryResponse createUser(UserCreateRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new InvalidOperationException("An account with email '" + normalizedEmail + "' already exists.");
        }

        User user = new User(
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                request.getName().trim(),
                request.getRole().trim().toUpperCase()
        );

        User saved = userRepository.save(user);
        log.info("Created user account id={} email={} role={}", saved.getId(), saved.getEmail(), saved.getRole());
        return UserSummaryResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
