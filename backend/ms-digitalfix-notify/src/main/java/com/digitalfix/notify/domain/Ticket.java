package com.digitalfix.notify.domain;

import java.time.Instant;

public record Ticket(
        String ticketNumber,
        Long workOrderId,
        String customerName,
        String generatedBy,
        Instant generatedAt) {
}
