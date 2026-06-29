package com.hammi.playground.exceptions;


public class ApiException extends RuntimeException {
    private String message;

    public ApiException(String message) {
        super(message);
    }
}
