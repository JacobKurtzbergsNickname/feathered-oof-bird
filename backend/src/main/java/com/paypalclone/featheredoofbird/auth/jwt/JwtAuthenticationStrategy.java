package com.paypalclone.featheredoofbird.auth.jwt;

import com.paypalclone.featheredoofbird.auth.AuthenticationStrategy;
import com.paypalclone.featheredoofbird.auth.JwtDecoderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

/**
 * Standard JWT authentication strategy. Validates the issuer claim only and uses Spring Security's
 * default scope-to-authority mapping.
 */
@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = "jwt")
public class JwtAuthenticationStrategy implements AuthenticationStrategy {

    private final String issuerUri;
    private final JwtDecoderFactory decoderFactory;

    public JwtAuthenticationStrategy(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            JwtDecoderFactory decoderFactory) {
        this.issuerUri = issuerUri;
        this.decoderFactory = decoderFactory;
    }

    @Override
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = decoderFactory.create(issuerUri);
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    @Override
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }
}
