package com.insurance.claimsprocessing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle "Not Found" (Policy not found, Claim not found) -> 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Not Found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 2. Handle "Validation Logic" (Fraud, Workflow rules) -> 400
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Map<String, String>> handleBusinessError(BusinessValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Business Rule Violation");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 3. Handle DTO Validation (Negative numbers, missing fields) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 4. Handle "No Endpoint Hit" (Wrong URL) -> 404
    // Note: Requires config in application.properties (see Step 5)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoHandlerFound(NoHandlerFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid Endpoint");
        error.put("message", "The URL you are trying to reach does not exist. Please check the path.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 5. Catch-all for unexpected crashes -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    // Handle Database Constraint Violations (e.g., Deleting a policy that has claims)
    // Handle Database Constraints (Duplicate Email, Foreign Keys, etc.)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleSQLViolation(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Data Integrity Violation");

        // Custom message for duplicate entry
        if (ex.getMessage().contains("Duplicate entry")) {
            error.put("message", "An IT Guy with this email already exists!");
        } else {
            error.put("message", "Cannot perform this action because this record is linked to other data (e.g., existing claims).");
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}