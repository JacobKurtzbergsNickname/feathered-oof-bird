package com.paypalclone.featheredoofbird.auth;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Strategy interface for configuring JWT-based authentication.
 * Implementations provide a {@link JwtDecoder} (token validation rules)
 * and a {@link Converter} (authority extraction logic).
 */
public interface AuthenticationStrategy {

    /**
     * Creates a configured {@link JwtDecoder} with provider-specific validation
     * (e.g. issuer-only for plain JWT, issuer + audience for Auth0).
     */
    JwtDecoder jwtDecoder();

    /**
     * Creates a converter that extracts Spring Security authorities from the JWT.
     */
    Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter();
}
