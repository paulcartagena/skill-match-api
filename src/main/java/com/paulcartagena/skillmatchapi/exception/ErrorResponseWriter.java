package com.paulcartagena.skillmatchapi.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

/**
 * Writes an {@link ErrorResponse} straight to the servlet response, for errors raised in security
 * filters before the request reaches a controller (and therefore the {@link GlobalExceptionHandler}).
 * Uses Spring's JsonMapper so the JSON matches the handler's responses.
 */
@Component
public class ErrorResponseWriter {

    private final JsonMapper jsonMapper;

    public ErrorResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonMapper.writeValueAsString(new ErrorResponse(status.value(), message)));
    }
}
