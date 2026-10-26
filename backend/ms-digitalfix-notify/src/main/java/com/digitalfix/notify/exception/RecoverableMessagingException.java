package com.digitalfix.notify.exception;

/**
 * Error transitorio de mensajería (p. ej. el envío de una notificación no está
 * disponible por el momento). El consumidor la relanza para que el contenedor
 * la reintente; si agota los reintentos, el mensaje se envía a la DLQ.
 */
public class RecoverableMessagingException extends RuntimeException {

    public RecoverableMessagingException(String message) {
        super(message);
    }

    public RecoverableMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}
