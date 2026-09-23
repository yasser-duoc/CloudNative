package com.pedidos360.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/workorders")
public class WorkOrderProxyController {

    private final RestClient workOrdersClient;

    public WorkOrderProxyController(@Qualifier("workOrdersRestClient") RestClient workOrdersClient) {
        this.workOrdersClient = workOrdersClient;
    }

    @GetMapping
    public ResponseEntity<String> findAll(@AuthenticationPrincipal Jwt jwt) {
        return workOrdersClient.get()
                .uri("/api/workorders")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return workOrdersClient.get()
                .uri("/api/workorders/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String payload, @AuthenticationPrincipal Jwt jwt) {
        return workOrdersClient.post()
                .uri("/api/workorders")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toEntity(String.class);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody String payload,
                                                @AuthenticationPrincipal Jwt jwt) {
        return workOrdersClient.put()
                .uri("/api/workorders/{id}/status", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toEntity(String.class);
    }

    private String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }
}
