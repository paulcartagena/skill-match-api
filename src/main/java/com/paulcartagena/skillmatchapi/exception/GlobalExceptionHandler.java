package com.paulcartagena.skillmatchapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.databind.DatabindException;

import java.util.stream.Collectors;

/**
 * Turns every error into an {@link ErrorResponse}. Spring MVC errors (malformed body, wrong method,
 * unknown route...) get their proper 4xx status from {@link ResponseEntityExceptionHandler}; this class
 * only rewrites the body with a client-friendly message that doesn't leak internals.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Business errors (auth, jobs, applications...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return build(ex.getStatus(), ex.getMessage());
    }

    // Login incorrect credentials (by Spring Security)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
    }

    // @PreAuthorize denials thrown inside controllers
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ErrorMessages.FORBIDDEN);
    }

    // Unique/foreign key violations that slip past service checks (e.g. two concurrent registrations)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "The request conflicts with existing data.");
    }

    // Unexpected errors -> 500, without leaking internal details
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error: ", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.UNEXPECTED);
    }

    // Every Spring MVC exception handled by the parent class ends up here
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        // Keep the parent's headers (e.g. Allow on 405, Accept on 415)
        return ResponseEntity.status(status)
                .headers(headers)
                .body(new ErrorResponse(status.value(), clientMessage(ex, status)));
    }

    private String clientMessage(Exception ex, HttpStatus status) {
        return switch (ex) {
            case MethodArgumentNotValidException e -> e.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .orElse("Validation error.");
            case HttpMessageNotReadableException e -> unreadableBodyMessage(e);
            case MethodArgumentTypeMismatchException e -> "Invalid value for '" + e.getName() + "'.";
            case TypeMismatchException e -> "Invalid value for '" + e.getPropertyName() + "'.";
            case NoResourceFoundException e -> "Resource not found.";
            case HttpRequestMethodNotSupportedException e ->
                    "Method " + e.getMethod() + " is not supported for this endpoint.";
            case HttpMediaTypeNotSupportedException e -> "Content type not supported. Use application/json.";
            default -> status.getReasonPhrase() + ".";
        };
    }

    // Names the offending field when Jackson knows it (wrong type, unknown enum value...)
    private String unreadableBodyMessage(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof DatabindException databindException
                && !databindException.getPath().isEmpty()) {
            String field = databindException.getPath().stream()
                    .map(ref -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."))
                    .replace(".[", "[");
            return "Invalid value for '" + field + "'.";
        }
        return "Malformed or missing request body.";
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }
}
