package com.paypalclone.featheredoofbird.auth.localjwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import com.paypalclone.featheredoofbird.identity.application.TokenIssuer;
import com.paypalclone.featheredoofbird.identity.domain.User;
import com.paypalclone.featheredoofbird.shared.config.AppConfig;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.StringJoiner;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "auth.provider", havingValue = "local-jwt", matchIfMissing = true)
public class LocalJwtTokenIssuer implements TokenIssuer {

    private final AppConfig appConfig;
    private final JwtEncoder jwtEncoder;

    public LocalJwtTokenIssuer(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey()));
    }

    @Override
    public IssuedToken issueToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(appConfig.auth().localJwt().accessTokenTtl());

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(appConfig.auth().localJwt().issuer())
                        .issuedAt(issuedAt)
                        .expiresAt(expiresAt)
                        .subject(String.valueOf(user.getId()))
                        .claim("email", user.getEmail())
                        .claim("role", user.getRole().name())
                        .claim("scope", scopes(user))
                        .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, expiresAt);
    }

    private String scopes(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        for (String scope : user.getRole().scopes()) {
            joiner.add(scope);
        }
        return joiner.toString();
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(
                appConfig.auth().localJwt().secret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256");
    }
}
