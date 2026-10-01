package com.digitalfix.workorders.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestAuditEventClient implements AuditEventClient {

    private static final Logger log = LoggerFactory.getLogger(RestAuditEventClient.class);
    private final RestClient client;

    public RestAuditEventClient(RestClient.Builder builder,
                                @Value("${digitalfix.audit-service-url:http://localhost:8084}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void record(WorkOrderEvent event, String bearerToken) {
        var request = client.post()
                .uri("/api/audit/internal/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event);
        if (bearerToken != null && !bearerToken.isBlank()) {
            request.header("Authorization", bearerToken.startsWith("Bearer ")
                    ? bearerToken : "Bearer " + bearerToken);
        } else {
            log.warn("Recording audit event {} without bearer token", event.eventId());
        }
        request.retrieve().toBodilessEntity();
    }
}
