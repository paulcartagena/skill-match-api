package com.paulcartagena.skillmatchapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

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
    void handleValidation_returns400WithFirstFieldError() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "must be a well-formed email address"));
        bindingResult.addError(new FieldError("request", "password", "must not be blank"));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

        assertError(response, HttpStatus.BAD_REQUEST, "email: must be a well-formed email address");
    }

    @Test
    void handleValidation_returnsGenericMessage_whenThereAreNoFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");

        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult));

        assertError(response, HttpStatus.BAD_REQUEST, "Validation error.");
    }

    @Test
    void handleAuthorizationDenied_returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAuthorizatinoDenied(new AuthorizationDeniedException("Access Denied"));

        assertError(response, HttpStatus.FORBIDDEN, "You don´t have permission to access this resource.");
    }

    @Test
    void handleUnexpected_returns500_withoutLeakingInternalMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new IllegalStateException("db password is hunter2"));

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
    }

    private void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(status.value());
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
