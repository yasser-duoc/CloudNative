package com.pedidos360.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
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
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/kpis/raw")
    public ResponseEntity<String> raw(@AuthenticationPrincipal Jwt jwt) {
        return reportClient.get()
                .uri("/api/report/kpis/raw")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    private String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }
}