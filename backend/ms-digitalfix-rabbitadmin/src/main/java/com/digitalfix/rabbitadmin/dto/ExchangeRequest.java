package com.digitalfix.rabbitadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ExchangeRequest(

        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9_.\\-]{1,200}$",
                message = "El nombre del exchange solo puede contener letras, números, '_', '-' y '.'")
        String name,

        @NotBlank(message = "El tipo del exchange es obligatorio")
        @Pattern(regexp = "direct|topic|fanout|headers",
                message = "El tipo de exchange debe ser direct, topic, fanout o headers")
        String type,

        Boolean durable) {
}
