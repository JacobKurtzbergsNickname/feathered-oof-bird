package com.paypalclone.featheredoofbird.auth.localjwt;

import com.paypalclone.featheredoofbird.auth.AuthenticationStrategy;
import com.paypalclone.featheredoofbird.shared.config.AppConfig;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

/** Validates JWTs issued locally by the application using a symmetric HMAC secret. */
@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = "local-jwt", matchIfMissing = true)
public class LocalJwtAuthenticationStrategy implements AuthenticationStrategy {

    private final AppConfig appConfig;

    public LocalJwtAuthenticationStrategy(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withSecretKey(secretKey())
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(appConfig.auth().localJwt().issuer()));
        return decoder;
    }

    @Override
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new JwtAuthenticationConverter();
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(
                appConfig.auth().localJwt().secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
    }
}
