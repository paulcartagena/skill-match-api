package com.paulcartagena.skillmatchapi.auth.service;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_returnsUser_whenEmailExists() {
        User user = new User();
        user.setEmail("user@user.com");
        when(userRepository.findByEmail("user@user.com")).thenReturn(Optional.of(user));

        assertThat(customUserDetailsService.loadUserByUsername("user@user.com")).isSameAs(user);
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFound_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("missing@user.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@user.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
