package com.paypalclone.featheredoofbird.auth.auth0;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class Auth0JwtAuthenticationConverterTest {

    private final Auth0JwtAuthenticationConverter converter = new Auth0JwtAuthenticationConverter();

    @Test
    void mapsPermissionsClaimToAuthorities() {
        Jwt jwt =
                buildJwt(
                        Map.of(
                                "sub", "user|123",
                                "scope", "openid profile",
                                "permissions", List.of("read:transactions", "write:transactions")));

        AbstractAuthenticationToken token = converter.convert(jwt);

        List<String> authorities =
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).contains("read:transactions", "write:transactions");
    }

    @Test
    void preservesStandardScopeAuthorities() {
        Jwt jwt =
                buildJwt(
                        Map.of(
                                "sub", "user|123",
                                "scope", "openid profile"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        List<String> authorities =
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).contains("SCOPE_openid", "SCOPE_profile");
    }

    @Test
    void handlesJwtWithoutPermissionsClaim() {
        Jwt jwt =
                buildJwt(
                        Map.of(
                                "sub", "user|456",
                                "scope", "openid"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        List<String> authorities =
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactly("SCOPE_openid");
    }

    @Test
    void handlesEmptyPermissionsList() {
        Jwt jwt =
                buildJwt(
                        Map.of(
                                "sub", "user|789",
                                "scope", "openid",
                                "permissions", List.of()));

        AbstractAuthenticationToken token = converter.convert(jwt);

        List<String> authorities =
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactly("SCOPE_openid");
    }

    @Test
    void usesSubClaimAsTokenName() {
        Jwt jwt =
                buildJwt(
                        Map.of(
                                "sub", "auth0|abc",
                                "scope", "openid"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getName()).isEqualTo("auth0|abc");
    }

    @Test
    void combinesScopesAndPermissions() {
        Jwt jwt =
                buildJwt(
                        Map.of(
                                "sub", "user|combo",
                                "scope", "openid",
                                "permissions", List.of("admin:all")));

        AbstractAuthenticationToken token = converter.convert(jwt);

        List<String> authorities =
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactlyInAnyOrder("SCOPE_openid", "admin:all");
    }

    private Jwt buildJwt(Map<String, Object> claims) {
        Jwt.Builder builder =
                Jwt.withTokenValue("token")
                        .header("alg", "RS256")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(300));
        claims.forEach(builder::claim);
        return builder.build();
    }
}
