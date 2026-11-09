package com.digitalfix.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @PostMapping("/services")
    public ResponseEntity<String> createService(@RequestBody String payload, @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.post()
                .uri("/api/catalog/services")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @GetMapping("/spareparts")
    public ResponseEntity<String> listSpareParts(@AuthenticationPrincipal Jwt jwt) {
        return catalogClient.get()
                .uri("/api/catalog/spareparts")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @PostMapping("/spareparts")
    public ResponseEntity<String> createSparePart(@RequestBody String payload, @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.post()
                .uri("/api/catalog/spareparts")
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @PatchMapping("/spareparts/{id}/stock")
    public ResponseEntity<String> updateStock(@PathVariable Long id, @RequestBody String payload,
                                              @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.patch()
                .uri("/api/catalog/spareparts/{id}/stock", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<String> updateService(@PathVariable Long id, @RequestBody String payload,
                                                @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.put().uri("/api/catalog/services/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt)).contentType(MediaType.APPLICATION_JSON)
                .body(payload).exchange((request, response) -> sanitizedResponse(response));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<String> deleteService(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.delete().uri("/api/catalog/services/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt))
                .exchange((request, response) -> sanitizedResponse(response));
    }

    @PutMapping("/spareparts/{id}")
    public ResponseEntity<String> updateSparePart(@PathVariable Long id, @RequestBody String payload,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.put().uri("/api/catalog/spareparts/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer(jwt)).contentType(MediaType.APPLICATION_JSON)
                .body(payload).exchange((request, response) -> sanitizedResponse(response));
    }

    @DeleteMapping("/spareparts/{id}")
    public ResponseEntity<String> deleteSparePart(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return catalogClient.delete().uri("/api/catalog/spareparts/{id}", id)
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