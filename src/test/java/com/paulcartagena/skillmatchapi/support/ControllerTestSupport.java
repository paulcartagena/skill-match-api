package com.paulcartagena.skillmatchapi.support;

import com.paulcartagena.skillmatchapi.auth.entity.User;
import com.paulcartagena.skillmatchapi.auth.enums.UserRole;
import com.paulcartagena.skillmatchapi.auth.service.CustomUserDetailsService;
import com.paulcartagena.skillmatchapi.config.SecurityConfig;
import com.paulcartagena.skillmatchapi.exception.ErrorResponseWriter;
import com.paulcartagena.skillmatchapi.security.JwtAccessDeniedHandler;
import com.paulcartagena.skillmatchapi.security.JwtAuthenticationEntryPoint;
import com.paulcartagena.skillmatchapi.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Base for @WebMvcTest controller tests: loads the real security chain (JWT entry point, access denied
 * handler, @PreAuthorize) and the GlobalExceptionHandler, so tests cover routing, auth, validation and
 * error format. Services are mocked in each test; their logic is covered by the service tests.
 */
@Import({SecurityConfig.class, ErrorResponseWriter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
@TestPropertySource(properties = "rate-limit.login.capacity=1000")
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    // Needed by JwtFilter, which @WebMvcTest picks up as a servlet filter
    @MockitoBean
    protected JwtService jwtService;
    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

    protected static User userWithRole(UserRole role) {
        User user = new User();
        user.setId(switch (role) {
            case CANDIDATE -> 1L;
            case RECRUITER -> 2L;
            case ADMIN -> 3L;
        });
        user.setEmail(role.name().toLowerCase() + "@test.com");
        user.setRole(role);
        return user;
    }

    protected static RequestPostProcessor as(UserRole role) {
        return user(userWithRole(role));
    }

    protected String json(Object body) {
        return jsonMapper.writeValueAsString(body);
    }
}
