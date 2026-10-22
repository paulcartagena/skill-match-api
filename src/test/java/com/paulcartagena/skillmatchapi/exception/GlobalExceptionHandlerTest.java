package com.paulcartagena.skillmatchapi.exception;

import com.paulcartagena.skillmatchapi.job.dto.JobRequest;
import com.paulcartagena.skillmatchapi.user.dto.CandidateSkillRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void handleApiException_usesExceptionStatusAndMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleApiException(ApiException.notFound("Skill not found"));

        assertError(response, HttpStatus.NOT_FOUND, "Skill not found");
    }

    @Test
    void handleBadCredentials_returns401WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("internal detail"));

        assertError(response, HttpStatus.UNAUTHORIZED, "Invalid credentials.");
    }

    @Test
    void handleAuthorizationDenied_returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAuthorizationDenied(new AuthorizationDeniedException("Access Denied"));

        assertError(response, HttpStatus.FORBIDDEN, ErrorMessages.FORBIDDEN);
    }

    @Test
    void handleDataIntegrityViolation_returns409_withoutLeakingConstraintName() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("duplicate key value violates unique constraint uk_companies_name"));

        assertError(response, HttpStatus.CONFLICT, "The request conflicts with existing data.");
    }

    @Test
    void handleUnexpected_returns500_withoutLeakingInternalMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new IllegalStateException("db password is hunter2"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED);
    }

    @Test
    void validationError_returns400WithFirstFieldError() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must be a well-formed email address"));
        bindingResult.addError(new FieldError("request", "password", "must not be blank"));

        ResponseEntity<Object> response = handle(
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

        assertError(response, HttpStatus.BAD_REQUEST, "email: must be a well-formed email address");
    }

    @Test
    void validationError_returnsGenericMessage_whenThereAreNoFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

        ResponseEntity<Object> response = handle(
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

        assertError(response, HttpStatus.BAD_REQUEST, "Validation error.");
    }

    @Test
    void malformedBody_returns400() throws Exception {
        ResponseEntity<Object> response = handle(unreadable("{\"level\":", CandidateSkillRequest.class));

        assertError(response, HttpStatus.BAD_REQUEST, "Malformed or missing request body.");
    }

    @Test
    void invalidEnumValue_returns400NamingTheField() throws Exception {
        ResponseEntity<Object> response = handle(unreadable("{\"level\":\"GURU\"}", CandidateSkillRequest.class));

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid value for 'level'.");
    }

    @Test
    void invalidNestedValue_returns400NamingTheFullPath() throws Exception {
        ResponseEntity<Object> response =
                handle(unreadable("{\"skills\":[{\"skillId\":\"abc\"}]}", JobRequest.class));

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid value for 'skills[0].skillId'.");
    }

    @Test
    void pathVariableTypeMismatch_returns400NamingTheParameter() throws Exception {
        ResponseEntity<Object> response = handle(new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", mock(MethodParameter.class), new NumberFormatException()));

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid value for 'id'.");
    }

    @Test
    void unknownRoute_returns404() throws Exception {
        ResponseEntity<Object> response = handle(new NoResourceFoundException(HttpMethod.GET, "/api/nope", "api/nope"));

        assertError(response, HttpStatus.NOT_FOUND, "Resource not found.");
    }

    @Test
    void unsupportedMethod_returns405WithAllowHeader() throws Exception {
        ResponseEntity<Object> response = handle(new HttpRequestMethodNotSupportedException("DELETE", List.of("GET", "POST")));

        assertError(response, HttpStatus.METHOD_NOT_ALLOWED, "Method DELETE is not supported for this endpoint.");
        assertThat(response.getHeaders().getAllow()).containsExactlyInAnyOrder(HttpMethod.GET, HttpMethod.POST);
    }

    @Test
    void unsupportedContentType_returns415() throws Exception {
        ResponseEntity<Object> response = handle(new HttpMediaTypeNotSupportedException(
                MediaType.TEXT_PLAIN, List.of(MediaType.APPLICATION_JSON)));

        assertError(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type not supported. Use application/json.");
    }

    @Test
    void otherSpringErrors_useReasonPhrase() throws Exception {
        ResponseEntity<Object> response = handle(new MissingServletRequestParameterException("page", "int"));

        assertError(response, HttpStatus.BAD_REQUEST, "Bad Request.");
    }

    private ResponseEntity<Object> handle(Exception ex) throws Exception {
        return handler.handleException(ex, new ServletWebRequest(new MockHttpServletRequest()));
    }

    // Builds the exception Spring raises when Jackson can't read the body, with the real Jackson cause
    private HttpMessageNotReadableException unreadable(String json, Class<?> type) {
        JacksonException cause = catchThrowableOfType(JacksonException.class, () -> jsonMapper.readValue(json, type));
        return new HttpMessageNotReadableException("JSON parse error", cause, mock(HttpInputMessage.class));
    }

    private void assertError(ResponseEntity<?> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body.getStatus()).isEqualTo(status.value());
        assertThat(body.getMessage()).isEqualTo(message);
        assertThat(body.getTimestamp()).isNotNull();
    }
}
