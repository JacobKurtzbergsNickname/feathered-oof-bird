package com.paypalclone.featheredoofbird.auth.auth0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paypalclone.featheredoofbird.auth.JwtDecoderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

class Auth0AuthenticationStrategyTest {

    private static final String ISSUER_URI = "https://tenant.auth0.com/";
    private static final String AUDIENCE = "https://my-api";

    private JwtDecoderFactory decoderFactory;
    private NimbusJwtDecoder mockDecoder;
    private Auth0AuthenticationStrategy strategy;

    @BeforeEach
    void setUp() {
        decoderFactory = mock(JwtDecoderFactory.class);
        mockDecoder = mock(NimbusJwtDecoder.class);
        when(decoderFactory.create(ISSUER_URI)).thenReturn(mockDecoder);
        strategy = new Auth0AuthenticationStrategy(ISSUER_URI, AUDIENCE, decoderFactory);
    }

    @Test
    void jwtDecoderUsesFactoryWithIssuerUri() {
        strategy.jwtDecoder();

        verify(decoderFactory).create(ISSUER_URI);
    }

    @Test
    void jwtDecoderSetsValidator() {
        JwtDecoder decoder = strategy.jwtDecoder();

        assertThat(decoder).isSameAs(mockDecoder);
        // Verify that setJwtValidator was called (with a combined validator)
        verify(mockDecoder).setJwtValidator(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void jwtAuthenticationConverterReturnsAuth0Converter() {
        Converter<Jwt, ? extends AbstractAuthenticationToken> converter =
                strategy.jwtAuthenticationConverter();

        assertThat(converter).isInstanceOf(Auth0JwtAuthenticationConverter.class);
    }
}
