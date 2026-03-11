package com.paypalclone.featheredoofbird.auth;

import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Factory for creating {@link NimbusJwtDecoder} instances. Extracted so that strategies can be
 * unit-tested without reaching out to a real JWKS endpoint.
 */
@FunctionalInterface
public interface JwtDecoderFactory {

    NimbusJwtDecoder create(String issuerUri);
}
