package com.paulcartagena.skillmatchapi.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityErrorHandlersTest {

    @Test
    void authenticationEntryPoint_writes401JsonBody() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtAuthenticationEntryPoint().commence(
                new MockHttpServletRequest(), response, new InsufficientAuthenticationException("no token"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getContentAsString())
                .contains("\"status\":401")
                .contains("Authentication required or token invalid/expired.")
                .contains("\"timestamp\":\"");
    }

    @Test
    void accessDeniedHandler_writes403JsonBody() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JwtAccessDeniedHandler().handle(
                new MockHttpServletRequest(), response, new AccessDeniedException("denied"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getContentAsString())
                .contains("\"status\":403")
                .contains("\"timestamp\":\"");
    }
}
