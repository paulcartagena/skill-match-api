package com.paulcartagena.skillmatchapi.security;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private User user;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        user = new User();
        user.setEmail("user@user.com");
        user.setRole(UserRole.CANDIDATE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_skipsAuthentication_whenAuthorizationHeaderIsMissing() throws Exception {
        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, customUserDetailsService);
    }

    @Test
    void doFilter_skipsAuthentication_whenHeaderIsNotBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, customUserDetailsService);
    }

    @Test
    void doFilter_setsAuthentication_whenTokenIsValid() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.getUsername("valid-token")).thenReturn("user@user.com");
        when(customUserDetailsService.loadUserByUsername("user@user.com")).thenReturn(user);
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

        jwtFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isSameAs(user);
        assertThat(authentication.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_CANDIDATE");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_doesNotAuthenticate_whenTokenIsNotValidForUser() throws Exception {
        request.addHeader("Authorization", "Bearer stale-token");
        when(jwtService.getUsername("stale-token")).thenReturn("user@user.com");
        when(customUserDetailsService.loadUserByUsername("user@user.com")).thenReturn(user);
        when(jwtService.isTokenValid("stale-token", user)).thenReturn(false);

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_continuesChainWithoutAuthentication_whenTokenParsingFails() throws Exception {
        request.addHeader("Authorization", "Bearer malformed-token");
        when(jwtService.getUsername("malformed-token")).thenThrow(new MalformedJwtException("bad token"));

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(customUserDetailsService);
    }

    @Test
    void doFilter_keepsExistingAuthentication_whenContextIsAlreadyAuthenticated() throws Exception {
        Authentication existing = new UsernamePasswordAuthenticationToken("someone", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existing);
        request.addHeader("Authorization", "Bearer valid-token");
        when(jwtService.getUsername("valid-token")).thenReturn("user@user.com");

        jwtFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
