package com.company.txn.exception;

import com.jayway.jsonpath.PathNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidation(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "reason", ex.getMessage()
        ));
    }

    @ExceptionHandler(PathNotFoundException.class)
    public ResponseEntity<?> handleJsonPath(PathNotFoundException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "reason", "Missing required JSON field"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "ERROR",
                "reason", "Internal server error"
        ));
    }
}
