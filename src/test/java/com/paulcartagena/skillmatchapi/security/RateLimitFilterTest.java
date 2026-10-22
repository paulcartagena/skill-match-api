package com.paulcartagena.skillmatchapi.security;

import com.paulcartagena.skillmatchapi.exception.ErrorResponseWriter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private static final int CAPACITY = 3;

    private RateLimitFilter rateLimitFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(new ErrorResponseWriter(JsonMapper.builder().build()));
        ReflectionTestUtils.setField(rateLimitFilter, "capacity", CAPACITY);
        ReflectionTestUtils.setField(rateLimitFilter, "refillMinutes", 1);
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilter_neverLimits_nonAuthEndpoints() throws Exception {
        for (int i = 0; i < CAPACITY + 5; i++) {
            MockHttpServletResponse response = send("/skills", "10.0.0.1", null);
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        verify(filterChain, times(CAPACITY + 5)).doFilter(any(), any());
    }

    @Test
    void doFilter_allowsRequestsUpToCapacity() throws Exception {
        for (int i = 0; i < CAPACITY; i++) {
            assertThat(send("/auth/login", "10.0.0.1", null).getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        verify(filterChain, times(CAPACITY)).doFilter(any(), any());
    }

    @Test
    void doFilter_returns429WithRetryAfterAndJsonBody_whenCapacityIsExceeded() throws Exception {
        exhaust("/auth/login", "10.0.0.1");

        MockHttpServletResponse response = send("/auth/login", "10.0.0.1", null);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(Long.parseLong(response.getHeader("Retry-After"))).isBetween(1L, 60L);
        assertThat(response.getContentAsString())
                .contains("\"status\":429")
                .contains("Too many attempts. Try again later.");
        verify(filterChain, times(CAPACITY)).doFilter(any(), any());
    }

    @Test
    void doFilter_tracksEachClientIpIndependently() throws Exception {
        exhaust("/auth/login", "10.0.0.1");

        assertThat(send("/auth/login", "10.0.0.2", null).getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void doFilter_tracksEachEndpointIndependently() throws Exception {
        exhaust("/auth/login", "10.0.0.1");

        assertThat(send("/auth/register", "10.0.0.1", null).getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(send("/auth/register-recruiter", "10.0.0.1", null).getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void doFilter_ignoresXForwardedForHeader_soClientsCannotSpoofTheirIp() throws Exception {
        // Same client rotating fake X-Forwarded-For values must still hit the limit
        for (int i = 0; i < CAPACITY; i++) {
            send("/auth/login", "203.0.113.7", "198.51.100." + i);
        }

        MockHttpServletResponse response = send("/auth/login", "203.0.113.7", "198.51.100.99");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
    }

    private void exhaust(String path, String ip) throws Exception {
        for (int i = 0; i < CAPACITY; i++) {
            send(path, ip, null);
        }
    }

    private MockHttpServletResponse send(String path, String remoteAddr, String xForwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(remoteAddr);
        if (xForwardedFor != null) {
            request.addHeader("X-Forwarded-For", xForwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilter(request, response, filterChain);
        return response;
    }
}
