package com.digitalfix.catalog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Locale;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**")
                        .hasAnyRole("Admin", "Supervisor", "Cliente")
                        .requestMatchers(HttpMethod.POST, "/api/catalog/**")
                        .hasAnyRole("Admin", "Supervisor")
                        .requestMatchers(HttpMethod.PUT, "/api/catalog/**")
                        .hasAnyRole("Admin", "Supervisor")
                        .requestMatchers(HttpMethod.DELETE, "/api/catalog/**")
                        .hasRole("Admin")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = jwt.getClaimAsStringList("roles");
            if (authorities == null) {
                return java.util.List.of();
            }
            return authorities.stream()
                    .map(this::normalizeRole)
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return converter;
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
