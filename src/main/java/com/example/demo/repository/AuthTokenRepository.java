package com.example.demo.repository;

import com.example.demo.model.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

    Optional<AuthToken> findByToken(String token);
}
