package com.digitalfix.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BindingRequest(

        @NotBlank(message = "El exchange de origen es obligatorio")
        String exchange,

        @NotBlank(message = "El destino del binding es obligatorio")
        String destination,

        @NotBlank(message = "El tipo de destino es obligatorio")
        @Pattern(regexp = "queue|exchange",
                message = "El tipo de destino debe ser queue o exchange")
        String destinationType,

        @Size(max = 255, message = "La routing key no puede superar 255 caracteres")
        String routingKey) {
}
