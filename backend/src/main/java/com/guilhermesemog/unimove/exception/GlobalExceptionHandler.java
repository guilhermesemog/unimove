package com.guilhermesemog.unimove.exception;

import com.guilhermesemog.unimove.exception.type.CpfAlreadyExistsException;
import com.guilhermesemog.unimove.exception.type.IllegalUpdateException;
import com.guilhermesemog.unimove.exception.type.ResourceAlreadyExists;
import com.guilhermesemog.unimove.exception.type.ResourceNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDetails> handleResponseStatusException(ResponseStatusException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message(ex.getReason() == null ? "Request could not be processed" : ex.getReason())
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(errorDetails);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDetails> handleExpiredJwtException(ExpiredJwtException e) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("JWT token has expired")
                .details("Sign in again to continue")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(401).body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleMethodNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
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
    public ResponseEntity<ErrorDetails> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Data integrity validation failed")
                .details("The request conflicts with stored data")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Resource not found")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(404).body(errorDetails);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDetails> handleMessageNotReadableException(HttpMessageNotReadableException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Message not readable")
                .details("The request body is invalid")
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Access denied")
                .details("You do not have permission to access this resource")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(403).body(errorDetails);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(DisabledException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Access denied")
                .details("This account is disabled. Please contact the administrator.")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(403).body(errorDetails);
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

    @ExceptionHandler(ResourceAlreadyExists.class)
    public ResponseEntity<ErrorDetails> handleResourceAlreadyExists(ResourceAlreadyExists ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Resource already exists")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(409).body(errorDetails);
    }

    @ExceptionHandler(IllegalUpdateException.class)
    public ResponseEntity<ErrorDetails> handleIllegalUpdateException(IllegalUpdateException ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("Illegal update operation")
                .details(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(409).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex) {
        ErrorDetails errorDetails = ErrorDetails.builder()
                .message("An unexpected error occurred")
                .details("No additional details are available")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.internalServerError().body(errorDetails);
    }
}
