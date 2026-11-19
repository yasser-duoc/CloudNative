package com.digitalfix.kafkaadmin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record TopicUpdateRequest(

        @Positive(message = "La retención en milisegundos debe ser positiva")
        Long retentionMs,

        @Pattern(regexp = "delete|compact",
                message = "La política de limpieza debe ser delete o compact")
        String cleanupPolicy,

        @Min(value = 1, message = "El número de particiones debe ser al menos 1")
        Integer partitions) {
}
