package com.digitalfix.bff.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class DigitalFixJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(extractRoleAuthorities(jwt));
        authorities.addAll(scopesConverter.convert(jwt));

        String principalName = jwt.getClaimAsString("preferred_username");
        if (principalName == null) {
            principalName = jwt.getSubject();
        }
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> extractRoleAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(this::normalizeRole)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "" : role
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
        if (normalized.endsWith(" del dominio")) {
            normalized = normalized.substring(0, normalized.length() - " del dominio".length()).trim();
        }
        return switch (normalized) {
            case "admin", "administrador" -> "Admin";
            case "supervisor" -> "Supervisor";
            case "cliente", "cliente final" -> "Cliente";
            case "auditor" -> "Auditor";
            default -> role;
        };
    }
}
