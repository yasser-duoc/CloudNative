package com.digitalfix.kafkaadmin.exception;

public class InvalidResourceException extends RuntimeException {

    public InvalidResourceException(String message) {
        super(message);
    }
}
