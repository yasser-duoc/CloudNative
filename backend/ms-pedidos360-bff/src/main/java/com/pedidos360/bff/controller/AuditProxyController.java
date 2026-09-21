package com.pedidos360.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/audit")
public class AuditProxyController {

    private final RestClient auditClient;

    public AuditProxyController(@Qualifier("auditRestClient") RestClient auditClient) {
        this.auditClient = auditClient;
    }

    @GetMapping
    public ResponseEntity<String> findAll(@AuthenticationPrincipal Jwt jwt) {
        return auditClient.get()
                .uri("/api/audit")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/workorders/{workOrderId}")
    public ResponseEntity<String> timeline(@PathVariable Long workOrderId, @AuthenticationPrincipal Jwt jwt) {
        return auditClient.get()
                .uri("/api/audit/workorders/{workOrderId}", workOrderId)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    private String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }
}