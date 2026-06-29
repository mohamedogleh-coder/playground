package com.hammi.playground.exceptions;
public class NotFoundException extends RuntimeException {
    String message;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException() {
    }
}