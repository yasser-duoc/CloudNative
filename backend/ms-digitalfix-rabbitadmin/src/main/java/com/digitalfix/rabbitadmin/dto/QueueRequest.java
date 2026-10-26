package com.digitalfix.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record QueueRequest(

        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9_.\\-]{1,200}$",
                message = "El nombre de la cola solo puede contener letras, números, '_', '-' y '.'")
        String name,

        Boolean durable,

        @Size(max = 200, message = "El dead letter exchange no puede superar 200 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_.\\-]*$", message = "Dead letter exchange con formato inválido")
        String deadLetterExchange,

        @Size(max = 255, message = "La dead letter routing key no puede superar 255 caracteres")
        String deadLetterRoutingKey) {
}
