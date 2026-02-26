package com.compassed.compassed_api.api;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = ex.getMessage() == null ? "Bad request" : ex.getMessage();
        String normalized = msg.toLowerCase();
        if (normalized.contains("unauthorized") || normalized.contains("invalid token")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (normalized.contains("forbidden")) {
            status = HttpStatus.FORBIDDEN;
        } else if (normalized.contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", msg);
        return ResponseEntity.status(status).body(body);
    }
}
