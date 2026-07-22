package com.hammi.playground.exceptions;//
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.orm.jpa.JpaSystemException;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class ApiExceptionHandler {
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<Map<String, Object>> handleDatabaseExceptions(DataIntegrityViolationException ex) {
//        Map<String, Object> response = new HashMap<>();
//        Throwable cause = ex.getRootCause();
//
//        if (cause instanceof SQLException sqlEx && "45000".equals(sqlEx.getSQLState())) {
//            // Custom error oo ka yimid Postgres (RAISE EXCEPTION ... USING ERRCODE = '45000')
//            String cleanMessage = extractPostgresMessage(sqlEx.getMessage());
//
//            response.put("status", 0);
//            response.put("message", cleanMessage);
//
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//        }
//
//        // Khaladaadka database caadiga ah (unique, foreign key, iwm) — ama aan la aqoon
//        response.put("status", 0);
//        response.put("message", "Khalad database ah ayaa dhacay, fadlan mar kale isku day");
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }
//
//    private String extractPostgresMessage(String rawMessage) {
//        if (rawMessage == null) return "Khalad aan la garanayn";
//        String msg = rawMessage.replaceFirst("ERROR: ", "");
//        int newlineIndex = msg.indexOf("\n");
//        return newlineIndex > 0 ? msg.substring(0, newlineIndex) : msg;
//    }
//
//    /// /    @ExceptionHandler(DataIntegrityViolationException.class)
//    /// /    public ResponseEntity<Map<String, Object>> handleDatabaseExceptions(Exception ex) {
//    /// /        Map<String, Object> response = new HashMap<>();
//    /// /        response.put("status", 0);
//    /// /        response.put("message", ex.getMessage());
//    /// /
//    /// /        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//    /// /    }
/// /
/// /    @ExceptionHandler(DataIntegrityViolationException.class)
/// /    public ResponseEntity<Map<String, Object>> handleCustomDbError(DataIntegrityViolationException ex) {
/// /        Throwable cause = ex.getRootCause();
/// /
/// /        if (cause instanceof SQLException sqlEx && "45000".equals(sqlEx.getSQLState())) {
/// /            // Kani waa custom error — fariinta si toos ah ayaad u soo celin kartaa
/// /            String cleanMessage = extractPostgresMessage(sqlEx.getMessage());
/// /            return ResponseEntity.badRequest()
/// /                    .body(new ErrorResponse("BUSINESS_ERROR", cleanMessage));
/// /        }
/// /
/// /        // Haddii aanay ahayn 45000, waa khalad database caadi ah — halkan ku qabo mid kale ama generic
/// /        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
/// /                .body(new ErrorResponse("DB_ERROR", "Khalad database ah ayaa dhacay"));
/// /    }
//}
//
//
//@ExceptionHandler(MethodArgumentNotValidException.class)
//public ResponseEntity<Map<String, Object>> handler(MethodArgumentNotValidException ex) {
//
//    Map<String, String> validations = new HashMap<>();
//
//    ex.getBindingResult().getFieldErrors().forEach(error -> validations.put(error.getField(), error.getDefaultMessage()));
//
//    Map<String, Object> response = new HashMap<>();
//    response.put("status", 0);
//    response.put("message", "Validation failed");
//    response.put("validations", validations);
//
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//}
//
//
//@ExceptionHandler(ApiException.class)
//ResponseEntity<Map<String, Object>> handler(ApiException apiException) {
//    Map<String, Object> errors = new HashMap<>();
//    errors.put("status", 0);
//    errors.put("message", apiException.getMessage());
//    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
//}
//
//@ExceptionHandler(NotFoundException.class)
//ResponseEntity<Map<String, Object>> handler(NotFoundException exception) {
//    Map<String, Object> errors = new HashMap<>();
//    errors.put("status", 0);
//    errors.put("message", exception.getMessage());
//    errors.put("payload", null);
//    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
//
/// /    @ExceptionHandler(Exception.class)
/// /    public ResponseEntity<Map<String, Object>> handler(Exception ex) {
/// /
/// /        Throwable rootCause = ex;
/// /        while (rootCause.getCause() != null) {
/// /            rootCause = rootCause.getCause();
/// /        }
/// /
/// /        Map<String, Object> response = new HashMap<>();
/// /        response.put("status", 0);
/// /        response.put("message", "Something went wrong");
/// /        response.put("debug_message", rootCause.getMessage());
/// /        response.put("payload", null);
/// /
/// /        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
/// /                .body(response);
/// /    }
//}


import com.hammi.playground.exceptions.ApiException;
import com.hammi.playground.exceptions.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
//
//    @ExceptionHandler(DataIntegrityViolationException.class)
//    public ResponseEntity<Map<String, Object>> handleDatabaseExceptions(DataIntegrityViolationException ex) {
//        Map<String, Object> response = new HashMap<>();
//        Throwable cause = ex.getRootCause();
//
//        if (cause instanceof SQLException sqlEx && "45000".equals(sqlEx.getSQLState())) {
//            // Custom error oo ka yimid Postgres (RAISE EXCEPTION ... USING ERRCODE = '45000')
//            String cleanMessage = extractPostgresMessage(sqlEx.getMessage());
//
//            response.put("status", 0);
//            response.put("message", cleanMessage);
//
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
//        }
//
//        // Khaladaadka database caadiga ah (unique, foreign key, iwm) — ama aan la aqoon
//        response.put("status", 0);
//        response.put("message", "Khalad database ah ayaa dhacay, fadlan mar kale isku day");
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//    }

    @ExceptionHandler({DataIntegrityViolationException.class, JpaSystemException.class})
    public ResponseEntity<Map<String, Object>> handleDatabaseExceptions(RuntimeException ex) {
        Throwable cause = ex.getCause();
        while (cause != null && cause.getCause() != null && !(cause instanceof SQLException)) {
            cause = cause.getCause();
        }

        Map<String, Object> response = new HashMap<>();
        if (cause instanceof SQLException sqlEx && "45000".equals(sqlEx.getSQLState())) {
            response.put("status", 0);
            response.put("message", extractPostgresMessage(sqlEx.getMessage()));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        response.put("status", 0);
        response.put("message", "Khalad database ah ayaa dhacay");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String extractPostgresMessage(String rawMessage) {
        if (rawMessage == null) return "Khalad aan la garanayn";
        String msg = rawMessage.replaceFirst("ERROR: ", "");
        int newlineIndex = msg.indexOf("\n");
        return newlineIndex > 0 ? msg.substring(0, newlineIndex) : msg;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handler(MethodArgumentNotValidException ex) {
        Map<String, String> validations = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> validations.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> response = new HashMap<>();
        response.put("status", 0);
        response.put("message", "Validation failed");
        response.put("validations", validations);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handler(ApiException apiException) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", 0);
        errors.put("message", apiException.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handler(NotFoundException exception) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("status", 0);
        errors.put("message", exception.getMessage());
        errors.put("payload", null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }

    // Talo: haddii aad rabto inaad qabato khaladaad kale oo aanan halkan lagu qeexin,
    // waxaad ku dari kartaa fallback handler sida tan hoose:
    //
    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<Map<String, Object>> handler(Exception ex) {
    //     Throwable rootCause = ex;
    //     while (rootCause.getCause() != null) {
    //         rootCause = rootCause.getCause();
    //     }
    //
    //     Map<String, Object> response = new HashMap<>();
    //     response.put("status", 0);
    //     response.put("message", "Something went wrong");
    //     response.put("debug_message", rootCause.getMessage());
    //     response.put("payload", null);
    //
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }
}