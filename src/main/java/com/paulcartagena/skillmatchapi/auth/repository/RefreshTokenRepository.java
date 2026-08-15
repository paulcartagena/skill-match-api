package com.paulcartagena.skillmatchapi.auth.repository;

import com.paulcartagena.skillmatchapi.auth.entity.RefreshToken;
import com.paulcartagena.skillmatchapi.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
