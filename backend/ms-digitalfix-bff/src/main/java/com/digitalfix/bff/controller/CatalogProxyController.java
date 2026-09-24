package com.digitalfix.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/catalog")
public class CatalogProxyController {

    private final RestClient catalogClient;

    public CatalogProxyController(@Qualifier("catalogRestClient") RestClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @GetMapping("/services")
    public ResponseEntity<String> listServices(@AuthenticationPrincipal Jwt jwt) {
        return catalogClient.get()
                .uri("/api/catalog/services")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/services")
    public ResponseEntity<String> createService(@RequestBody String payload, @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.post()
                .uri("/api/catalog/services")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping("/spareparts")
    public ResponseEntity<String> listSpareParts(@AuthenticationPrincipal Jwt jwt) {
        return catalogClient.get()
                .uri("/api/catalog/spareparts")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/spareparts")
    public ResponseEntity<String> createSparePart(@RequestBody String payload, @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.post()
                .uri("/api/catalog/spareparts")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toEntity(String.class);
    }

    @PatchMapping("/spareparts/{id}/stock")
    public ResponseEntity<String> updateStock(@PathVariable Long id, @RequestBody String payload,
                                              @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.patch()
                .uri("/api/catalog/spareparts/{id}/stock", id)
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