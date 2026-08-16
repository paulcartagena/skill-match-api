package com.paulcartagena.skillmatchapi.exception;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String message;

    public ErrorResponse(int status, String message) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
    }
}
