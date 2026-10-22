package com.paulcartagena.skillmatchapi.config;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.AccountStatus;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitialAdminCreatorTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InitialAdminCreator initialAdminCreator;

    @Test
    void run_createsActiveAdminWithEncodedPassword_whenNoAdminExists() {
        configure("admin@skillmatch.com", "admin-password");
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.findByEmail("admin@skillmatch.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin-password")).thenReturn("encoded-password");

        initialAdminCreator.run(null);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("admin@skillmatch.com")
                        && user.getPassword().equals("encoded-password")
                        && user.getRole() == UserRole.ADMIN
                        && user.getStatus() == AccountStatus.ACTIVE));
    }

    @Test
    void run_doesNothing_whenAnAdminAlreadyExists() {
        configure("admin@skillmatch.com", "admin-password");
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        initialAdminCreator.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_doesNothing_whenCredentialsAreNotConfigured() {
        configure("", "");
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        initialAdminCreator.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_doesNothing_whenPasswordIsTooShort() {
        configure("admin@skillmatch.com", "short");
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        initialAdminCreator.run(null);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void run_doesNotOverwriteExistingUser_withSameEmail() {
        configure("taken@skillmatch.com", "admin-password");
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.findByEmail("taken@skillmatch.com")).thenReturn(Optional.of(new User()));

        initialAdminCreator.run(null);

        verify(userRepository, never()).save(any());
    }

    private void configure(String email, String password) {
        ReflectionTestUtils.setField(initialAdminCreator, "email", email);
        ReflectionTestUtils.setField(initialAdminCreator, "password", password);
    }
}
