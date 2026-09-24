package com.digitalfix.bff.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalFixJwtAuthenticationConverterTest {

    private final DigitalFixJwtAuthenticationConverter converter = new DigitalFixJwtAuthenticationConverter();

    @Test
    void mapsRolesAndScopesToAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-123")
                .claim("preferred_username", "test@digitalfix.cl")
                .claim("roles", List.of("Admin", "Auditor"))
                .claim("scp", List.of("access_as_user"))
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        List<String> authorities = token.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        assertThat(authorities).contains("ROLE_Admin", "ROLE_Auditor", "SCOPE_access_as_user");
        assertThat(token.getName()).isEqualTo("test@digitalfix.cl");
    }

    @Test
    void usesSubjectAsFallbackPrincipalWhenPreferredUsernameMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("user-123")
                .build();

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
        assertThat(token.getName()).isEqualTo("user-123");
    }
}
