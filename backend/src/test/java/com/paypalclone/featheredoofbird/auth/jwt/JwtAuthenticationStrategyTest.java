package com.paypalclone.featheredoofbird.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.paypalclone.featheredoofbird.auth.JwtDecoderFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationStrategyTest {

    private static final String ISSUER_URI = "https://issuer.example.com/";

    private JwtDecoderFactory decoderFactory;
    private NimbusJwtDecoder mockDecoder;
    private JwtAuthenticationStrategy strategy;

    @BeforeEach
    void setUp() {
        decoderFactory = mock(JwtDecoderFactory.class);
        mockDecoder = mock(NimbusJwtDecoder.class);
        when(decoderFactory.create(ISSUER_URI)).thenReturn(mockDecoder);
        strategy = new JwtAuthenticationStrategy(ISSUER_URI, decoderFactory);
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
        verify(mockDecoder).setJwtValidator(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void jwtAuthenticationConverterReturnsStandardConverter() {
        Converter<Jwt, ? extends AbstractAuthenticationToken> converter =
                strategy.jwtAuthenticationConverter();

        assertThat(converter).isInstanceOf(JwtAuthenticationConverter.class);
    }
}
