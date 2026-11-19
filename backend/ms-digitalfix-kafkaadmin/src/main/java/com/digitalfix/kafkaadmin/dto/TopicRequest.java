package com.digitalfix.kafkaadmin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record TopicRequest(

        @NotBlank(message = "El nombre del topic es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._\\-]{1,200}$",
                message = "El nombre del topic solo puede contener letras, números, '_', '-', y '.'")
        String name,

        @Min(value = 1, message = "El número de particiones debe ser al menos 1")
        @Max(value = 100, message = "El número de particiones no puede superar 100")
        int partitions,

        @Min(value = 1, message = "El factor de replicación debe ser al menos 1")
        @Max(value = 10, message = "El factor de replicación no puede superar 10")
        short replicas,

        @Positive(message = "La retención en milisegundos debe ser positiva")
        Long retentionMs,

        @Pattern(regexp = "delete|compact",
                message = "La política de limpieza debe ser delete o compact")
        String cleanupPolicy) {
}
