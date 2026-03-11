package com.paypalclone.featheredoofbird.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Verifies that {@link AppConfig} binds correctly from property sources and that Bean Validation
 * constraints reject missing or blank values at startup.
 *
 * <p>Tests use {@link ApplicationContextRunner} — a lightweight, synchronous harness that avoids
 * starting a full Spring Boot application context. No real databases, no Spring Security, no
 * network — just property binding and validation.
 */
class AppConfigTest {

    /** Shared runner pre-loaded with only the configuration module under test. */
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(AppConfigurationModule.class);

    // ── Happy path ────────────────────────────────────────────────────────────

    @Nested
    class WhenAllRequiredPropertiesArePresent {

        private final ApplicationContextRunner happyRunner =
                runner.withPropertyValues(
                        "app.auth.provider=local-jwt",
                        "app.auth.issuer-uri=https://test.example.auth0.com/",
                        "app.auth.audience=test-audience",
                        "app.auth.local-jwt.issuer=http://localhost:8080",
                        "app.auth.local-jwt.secret=test-secret",
                        "app.auth.local-jwt.access-token-ttl=PT8H");

        @Test
        void bindsIssuerUri() {
            happyRunner.run(
                    ctx -> {
                        AppConfig config = ctx.getBean(AppConfig.class);
                        assertThat(config.auth().issuerUri())
                                .isEqualTo("https://test.example.auth0.com/");
                    });
        }

        @Test
        void bindsAudience() {
            happyRunner.run(
                    ctx -> {
                        AppConfig config = ctx.getBean(AppConfig.class);
                        assertThat(config.auth().audience()).isEqualTo("test-audience");
                    });
        }

        @Test
        void bindsLocalJwtSettings() {
            happyRunner.run(
                    ctx -> {
                        AppConfig config = ctx.getBean(AppConfig.class);
                        assertThat(config.auth().localJwt().issuer())
                                .isEqualTo("http://localhost:8080");
                        assertThat(config.auth().localJwt().accessTokenTtl())
                                .isEqualTo(Duration.ofHours(8));
                    });
        }

        @Test
        void contextStartsSuccessfully() {
            happyRunner.run(ctx -> assertThat(ctx).hasNotFailed());
        }

        @Test
        void isFullyConfigured_returnsTrueWhenBothValuesPresent() {
            happyRunner.run(
                    ctx -> {
                        AppConfig config = ctx.getBean(AppConfig.class);
                        assertThat(config.auth().isFullyConfigured()).isTrue();
                    });
        }
    }

    // ── isFullyConfigured() unit-style (no Spring context needed) ─────────────

    @Nested
    class IsFullyConfiguredMethod {

        @Test
        void returnsFalse_whenIssuerUriIsBlank() {
            var auth =
                    new AppConfig.Auth(
                            "auth0",
                            "",
                            "some-audience",
                            new AppConfig.LocalJwt(
                                    "http://localhost:8080", "test-secret", Duration.ofHours(8)));
            assertThat(auth.isFullyConfigured()).isFalse();
        }

        @Test
        void returnsFalse_whenAudienceIsBlank() {
            var auth =
                    new AppConfig.Auth(
                            "auth0",
                            "https://example.auth0.com/",
                            "",
                            new AppConfig.LocalJwt(
                                    "http://localhost:8080", "test-secret", Duration.ofHours(8)));
            assertThat(auth.isFullyConfigured()).isFalse();
        }

        @Test
        void returnsFalse_whenBothBlank() {
            var auth =
                    new AppConfig.Auth(
                            "auth0",
                            "",
                            "",
                            new AppConfig.LocalJwt(
                                    "http://localhost:8080", "test-secret", Duration.ofHours(8)));
            assertThat(auth.isFullyConfigured()).isFalse();
        }

        @Test
        void returnsTrue_whenBothNonBlank() {
            var auth =
                    new AppConfig.Auth(
                            "auth0",
                            "https://example.auth0.com/",
                            "my-audience",
                            new AppConfig.LocalJwt(
                                    "http://localhost:8080", "test-secret", Duration.ofHours(8)));
            assertThat(auth.isFullyConfigured()).isTrue();
        }

        @Test
        void returnsTrue_forLocalJwtWhenLocalSettingsPresent() {
            var auth =
                    new AppConfig.Auth(
                            "local-jwt",
                            "",
                            "",
                            new AppConfig.LocalJwt(
                                    "http://localhost:8080", "test-secret", Duration.ofHours(8)));
            assertThat(auth.isFullyConfigured()).isTrue();
        }
    }

    // ── Validation failure cases ───────────────────────────────────────────────

    @Nested
    class WhenRequiredPropertiesAreMissing {

        @Test
        void failsToStart_whenIssuerUriIsBlank() {
            runner.withPropertyValues(
                            "app.auth.provider=auth0",
                            "app.auth.issuer-uri=",
                            "app.auth.audience=test-audience",
                            "app.auth.local-jwt.issuer=http://localhost:8080",
                            "app.auth.local-jwt.secret=test-secret",
                            "app.auth.local-jwt.access-token-ttl=PT8H")
                    .run(
                            ctx ->
                                    assertThat(
                                                    ctx.getBean(AppConfig.class)
                                                            .auth()
                                                            .isFullyConfigured())
                                            .isFalse());
        }

        @Test
        void failsToStart_whenAudienceIsBlank() {
            runner.withPropertyValues(
                            "app.auth.provider=auth0",
                            "app.auth.issuer-uri=https://test.example.auth0.com/",
                            "app.auth.audience=",
                            "app.auth.local-jwt.issuer=http://localhost:8080",
                            "app.auth.local-jwt.secret=test-secret",
                            "app.auth.local-jwt.access-token-ttl=PT8H")
                    .run(
                            ctx ->
                                    assertThat(
                                                    ctx.getBean(AppConfig.class)
                                                            .auth()
                                                            .isFullyConfigured())
                                            .isFalse());
        }

        @Test
        void startsForLocalJwt_whenLocalSettingsArePresent() {
            runner.withPropertyValues(
                            "app.auth.provider=local-jwt",
                            "app.auth.local-jwt.issuer=http://localhost:8080",
                            "app.auth.local-jwt.secret=test-secret",
                            "app.auth.local-jwt.access-token-ttl=PT8H")
                    .run(ctx -> assertThat(ctx).hasNotFailed());
        }
    }
}
