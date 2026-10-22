package com.paulcartagena.skillmatchapi.exception;

// Messages shared by the MVC exception handler and the security filters, so both return the same text
public final class ErrorMessages {

    public static final String UNAUTHORIZED = "Authentication required or token invalid/expired.";
    public static final String FORBIDDEN = "You don't have permission to access this resource.";
    public static final String TOO_MANY_REQUESTS = "Too many attempts. Try again later.";
    public static final String UNEXPECTED = "Unexpected error occurred.";

    private ErrorMessages() {
    }
}
