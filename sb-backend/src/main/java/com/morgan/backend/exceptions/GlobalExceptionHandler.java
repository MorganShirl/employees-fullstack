package com.morgan.backend.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ────────────────────────────────────────────────────────────────────────
    // 1. Handle Custom Business Exceptions
    // ────────────────────────────────────────────────────────────────────────
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problem.setTitle("Resource Not Found");
        return problem; // Spring Boot 3 automatically wraps ProblemDetail in a ResponseEntity
    }

    // ────────────────────────────────────────────────────────────────────────
    // 2. Override Standard Validation
    // ────────────────────────────────────────────────────────────────────────
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request) {

        // Let Spring create the basic ProblemDetail for us
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Validation failed for request.");
        problem.setTitle("Validation Error");

        // Collect field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        // Add the custom property
        problem.setProperty("fieldErrors", errors);

        // Return using the parent's helper to ensure headers match
        return createResponseEntity(problem, headers, status, request);
    }

    // ────────────────────────────────────────────────────────────────────────
    // 3. Fallback for unexpected errors
    // ────────────────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex) {
        log.error("An unexpected error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An internal server error occurred. Please contact support."
        );
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
