package com.guilhermesemog.unimove.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Argument validation failed")
                .details(errors.values().toString())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetails> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Data integrity validation failed")
                .details(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleInvalidEnum(HttpMessageNotReadableException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Invalid enum value")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDetails> handleAuthenticationException(AuthenticationException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Authentication failed")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(401).body(errorDetails);
    }

    @ExceptionHandler(CpfAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleCpfAlreadyExistsException(CpfAlreadyExistsException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("CPF already exists")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(409).body(errorDetails);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Access denied")
                .details("You do not have permission to access this resource")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(403).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception e) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("An unexpected error occurred")
                .details(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.internalServerError().body(errorDetails);
    }
}
