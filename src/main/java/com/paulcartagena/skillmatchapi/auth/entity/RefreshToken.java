package com.paulcartagena.skillmatchapi.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token; // UUID random

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "expires_at")
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }
}
