package com.paypalclone.featheredoofbird.auth.localjwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jwt.SignedJWT;
import com.paypalclone.featheredoofbird.identity.application.TokenIssuer;
import com.paypalclone.featheredoofbird.identity.domain.Role;
import com.paypalclone.featheredoofbird.identity.domain.User;
import com.paypalclone.featheredoofbird.identity.domain.UserStatus;
import com.paypalclone.featheredoofbird.shared.config.AppConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

class LocalJwtTokenIssuerTest {

    // HmacSHA256 requires at least 32 bytes
    private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";
    private static final String ISSUER = "http://localhost";
    private static final Duration TTL = Duration.ofHours(1);

    private LocalJwtTokenIssuer issuer;

    @BeforeEach
    void setUp() {
        AppConfig.LocalJwt localJwt = new AppConfig.LocalJwt(ISSUER, SECRET, TTL);
        AppConfig.Auth auth = new AppConfig.Auth("local-jwt", null, null, localJwt);
        issuer = new LocalJwtTokenIssuer(new AppConfig(auth));
    }

    private User personalUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.PERSONAL);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    @Test
    void issueToken_returnsNonBlankToken() {
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());

        assertThat(token.accessToken()).isNotBlank();
    }

    @Test
    void issueToken_tokenHasThreeJwtParts() {
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());

        assertThat(token.accessToken().split("\\.")).hasSize(3);
    }

    @Test
    void issueToken_expiresAtIsInFuture() {
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());

        assertThat(token.expiresAt()).isAfter(Instant.now());
    }

    @Test
    void issueToken_expiresApproximatelyAfterConfiguredTtl() {
        Instant before = Instant.now();
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());
        Instant after = Instant.now();

        assertThat(token.expiresAt())
                .isBetween(before.plus(TTL).minusSeconds(2), after.plus(TTL).plusSeconds(2));
    }

    @Test
    void issueToken_subjectIsUserId() throws Exception {
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());

        SignedJWT jwt = SignedJWT.parse(token.accessToken());
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("1");
    }

    @Test
    void issueToken_containsEmailClaim() throws Exception {
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());

        SignedJWT jwt = SignedJWT.parse(token.accessToken());
        assertThat(jwt.getJWTClaimsSet().getStringClaim("email")).isEqualTo("user@example.com");
    }

    @Test
    void issueToken_personalRoleContainsWriteTransactionsScope() throws Exception {
        TokenIssuer.IssuedToken token = issuer.issueToken(personalUser());

        SignedJWT jwt = SignedJWT.parse(token.accessToken());
        assertThat(jwt.getJWTClaimsSet().getStringClaim("scope")).contains("write:transactions");
    }

    @Test
    void issueToken_adminRoleContainsBothScopes() throws Exception {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        TokenIssuer.IssuedToken token = issuer.issueToken(admin);

        SignedJWT jwt = SignedJWT.parse(token.accessToken());
        String scope = jwt.getJWTClaimsSet().getStringClaim("scope");
        assertThat(scope).contains("write:transactions").contains("admin:all");
    }
}
