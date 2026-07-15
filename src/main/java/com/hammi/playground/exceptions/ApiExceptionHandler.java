package com.hammi.playground.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handler(DataIntegrityViolationException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handler(MethodArgumentNotValidException ex) {

        Map<String, String> validations = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validations.put(
                        error.getField(),
                        error.getDefaultMessage()
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", "Validation failed");
        response.put("validations", validations);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
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

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handler(Exception ex) {
//
//        Throwable rootCause = ex;
//        while (rootCause.getCause() != null) {
//            rootCause = rootCause.getCause();
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", 0);
//        response.put("message", "Something went wrong");
//        response.put("debug_message", rootCause.getMessage());
//        response.put("payload", null);
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(response);
//    }
}
