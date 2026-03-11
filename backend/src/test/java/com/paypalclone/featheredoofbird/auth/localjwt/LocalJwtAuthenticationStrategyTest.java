package com.paypalclone.featheredoofbird.auth.localjwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.paypalclone.featheredoofbird.shared.config.AppConfig;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class LocalJwtAuthenticationStrategyTest {

    private LocalJwtAuthenticationStrategy strategy;

    @BeforeEach
    void setUp() {
        AppConfig appConfig =
                new AppConfig(
                        new AppConfig.Auth(
                                "local-jwt",
                                "",
                                "",
                                new AppConfig.LocalJwt(
                                        "http://localhost:8080",
                                        "dev-only-local-jwt-signing-secret-change-me",
                                        Duration.ofHours(8))));
        strategy = new LocalJwtAuthenticationStrategy(appConfig);
    }

    @Test
    void jwtDecoderIsConfigured() {
        JwtDecoder jwtDecoder = strategy.jwtDecoder();

        assertThat(jwtDecoder).isNotNull();
    }

    @Test
    void jwtAuthenticationConverterReturnsStandardConverter() {
        var converter = strategy.jwtAuthenticationConverter();

        assertThat(converter).isInstanceOf(JwtAuthenticationConverter.class);
    }
}
