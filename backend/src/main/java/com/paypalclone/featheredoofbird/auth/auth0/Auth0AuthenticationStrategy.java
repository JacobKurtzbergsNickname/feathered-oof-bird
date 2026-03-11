package com.paypalclone.featheredoofbird.auth.auth0;

import com.paypalclone.featheredoofbird.auth.AudienceValidator;
import com.paypalclone.featheredoofbird.auth.AuthenticationStrategy;
import com.paypalclone.featheredoofbird.auth.JwtDecoderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Auth0-specific authentication strategy. Validates issuer and audience, and maps the {@code
 * permissions} claim to authorities.
 */
@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = "auth0", matchIfMissing = true)
public class Auth0AuthenticationStrategy implements AuthenticationStrategy {

    private final String issuerUri;
    private final String audience;
    private final JwtDecoderFactory decoderFactory;

    public Auth0AuthenticationStrategy(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${auth0.audience}") String audience,
            JwtDecoderFactory decoderFactory) {
        this.issuerUri = issuerUri;
        this.audience = audience;
        this.decoderFactory = decoderFactory;
    }

    @Override
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = decoderFactory.create(issuerUri);
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));
        return decoder;
    }

    @Override
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new Auth0JwtAuthenticationConverter();
    }
}
