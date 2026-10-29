package com.digitalfix.bff.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<String> findAll(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to,
                                          @AuthenticationPrincipal Jwt jwt) {
        return workOrdersClient.get().uri(uriBuilder -> {
                    uriBuilder.path("/api/workorders");
                    if (status != null && !status.isBlank()) uriBuilder.queryParam("status", status);
                    if (from != null && !from.isBlank()) uriBuilder.queryParam("from", from);
                    if (to != null && !to.isBlank()) uriBuilder.queryParam("to", to);
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> findById(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return workOrdersClient.get()
                .uri("/api/workorders/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .exchange((request, response) -> sanitizedResponse(response));
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
        return workOrdersClient.put().uri("/api/workorders/{id}/status", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt)).contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .exchange((request, response) -> ResponseEntity.status(response.getStatusCode())
                        .headers(headers -> headers.setContentType(
                                response.getHeaders().getContentType() == null
                                        ? MediaType.APPLICATION_JSON
                                        : response.getHeaders().getContentType()))
                        .body(response.bodyTo(String.class)));
    }

    private String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }

    private ResponseEntity<String> sanitizedResponse(
            org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
        MediaType contentType = response.getHeaders().getContentType();
        return ResponseEntity.status(response.getStatusCode())
                .contentType(contentType == null ? MediaType.APPLICATION_JSON : contentType)
                .body(new String(response.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
    }
}
