package com.paypalclone.featheredoofbird.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class AuthConfig {

    @Bean
    @ConditionalOnMissingBean
    JwtDecoderFactory jwtDecoderFactory() {
        return issuerUri -> NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }
}
