package com.hammi.playground.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handler(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", 0);
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }


    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> handler(ApiException apiException) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", 0);
        errors.put("message", apiException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Map<String, Object>> handler(NotFoundException exception) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", 0);
        errors.put("message", exception.getMessage());
        errors.put("payload", null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errors);
    }
}
