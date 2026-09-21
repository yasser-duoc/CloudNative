package com.pedidos360.bff.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AudienceValidatorTest {

    private static final String AUDIENCE = "api://12345678-1234-1234-1234-123456789012";

    private final AudienceValidator validator = new AudienceValidator(AUDIENCE);

    private Jwt jwtWithAudience(String audience) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .audience(List.of(audience))
                .build();
    }

    @Test
    void acceptsMatchingAudience() {
        Jwt jwt = jwtWithAudience(AUDIENCE);
        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void acceptsPlainClientIdWithoutApiPrefix() {
        Jwt jwt = jwtWithAudience("12345678-1234-1234-1234-123456789012");
        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void rejectsWrongAudience() {
        Jwt jwt = jwtWithAudience("api://otro-client-id");
        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
