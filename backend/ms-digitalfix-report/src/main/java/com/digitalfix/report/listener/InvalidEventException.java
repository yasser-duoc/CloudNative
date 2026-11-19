package com.digitalfix.report.listener;

/**
 * Payload defectuoso: el DefaultErrorHandler lo reintenta y, si persiste,
 * lo publica en el topic DLT del flujo, sin interrumpir al consumidor.
 */
public class InvalidEventException extends RuntimeException {

    public InvalidEventException(String message) {
        super(message);
    }
}
