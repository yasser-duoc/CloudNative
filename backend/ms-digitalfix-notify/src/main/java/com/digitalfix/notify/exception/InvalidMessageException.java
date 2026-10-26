package com.digitalfix.notify.exception;

/**
 * Mensaje inválido o error de negocio no recuperable. Procesarlo de nuevo no
 * tiene sentido: el consumidor hace NACK sin requeue para enviar el mensaje
 * directamente a la DLQ.
 */
public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(String message) {
        super(message);
    }
}
