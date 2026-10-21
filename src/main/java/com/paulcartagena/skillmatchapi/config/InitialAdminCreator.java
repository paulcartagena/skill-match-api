package com.paulcartagena.skillmatchapi.config;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.AccountStatus;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN user on startup, since there is no endpoint to create admins.
 * Runs only when no ADMIN exists yet and ADMIN_EMAIL / ADMIN_PASSWORD are set;
 * otherwise it does nothing, so it is safe to leave the variables configured.
 */
@Slf4j
@Component
public class InitialAdminCreator implements ApplicationRunner {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username:}")
    private String email;

    @Value("${admin.password:}")
    private String password;

    public InitialAdminCreator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            return;
        }

        if (email.isBlank() || password.isBlank()) {
            log.warn("No ADMIN user exists. Set ADMIN_EMAIL and ADMIN_PASSWORD to create one on startup.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            log.error("Initial admin not created: ADMIN_PASSWORD must have at least {} characters.", MIN_PASSWORD_LENGTH);
            return;
        }
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("Initial admin not created: a non-admin user with email {} already exists.", email);
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(AccountStatus.ACTIVE);
        userRepository.save(admin);

        log.info("Initial admin user created: {}", email);
    }
}
