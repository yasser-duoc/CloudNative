package com.digitalfix.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/report")
public class ReportProxyController {

    private final RestClient reportClient;

    public ReportProxyController(@Qualifier("reportRestClient") RestClient reportClient) {
        this.reportClient = reportClient;
    }

    @GetMapping("/kpis")
    public ResponseEntity<String> summary(@AuthenticationPrincipal Jwt jwt) {
        return reportClient.get()
                .uri("/api/report/kpis")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @GetMapping("/kpis/raw")
    public ResponseEntity<String> raw(@AuthenticationPrincipal Jwt jwt) {
        return reportClient.get()
                .uri("/api/report/kpis/raw")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .exchange((request, response) -> sanitizedResponse(response));
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