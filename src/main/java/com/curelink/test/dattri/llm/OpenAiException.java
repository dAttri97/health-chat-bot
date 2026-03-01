package com.curelink.test.dattri.llm;

/**
 * Wraps any error from the OpenAI API call (network, auth, rate limit, bad response).
 */
public class OpenAiException extends RuntimeException {

    private final int httpStatus;

    public OpenAiException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public OpenAiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = -1;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
