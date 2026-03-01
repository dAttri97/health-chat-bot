package com.curelink.test.dattri.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.curelink.test.dattri.llm.OpenAiException;

/**
 * Global exception handling for API: validation errors and other failures.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(OpenAiException.class)
    public ResponseEntity<Map<String, Object>> handleOpenAiException(OpenAiException ex) {
        log.error("OpenAI error: {}", ex.getMessage());
        int status = ex.getHttpStatus() > 0 ? ex.getHttpStatus() : 502;
        return ResponseEntity
                .status(status)
                .body(Map.of("error", "AI service error. Please try again shortly."));
    }
}
