package com.paypalclone.featheredoofbird.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("https://my-api");

    @Test
    void succeedsWhenAudienceMatches() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of("https://my-api", "https://other"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void failsWhenAudienceMissing() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of("https://wrong-api"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .anyMatch(e -> e.getDescription().contains("Required audience is missing"));
    }

    @Test
    void failsWhenAudienceIsNull() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(null);

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }

    @Test
    void failsWhenAudienceListIsEmpty() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of());

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
    }
}
