package com.paypalclone.featheredoofbird.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

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
                        "app.auth.issuer-uri=https://test.example.auth0.com/",
                        "app.auth.audience=test-audience");

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
            var auth = new AppConfig.Auth("", "some-audience");
            assertThat(auth.isFullyConfigured()).isFalse();
        }

        @Test
        void returnsFalse_whenAudienceIsBlank() {
            var auth = new AppConfig.Auth("https://example.auth0.com/", "");
            assertThat(auth.isFullyConfigured()).isFalse();
        }

        @Test
        void returnsFalse_whenBothBlank() {
            var auth = new AppConfig.Auth("", "");
            assertThat(auth.isFullyConfigured()).isFalse();
        }

        @Test
        void returnsTrue_whenBothNonBlank() {
            var auth = new AppConfig.Auth("https://example.auth0.com/", "my-audience");
            assertThat(auth.isFullyConfigured()).isTrue();
        }
    }

    // ── Validation failure cases ───────────────────────────────────────────────

    @Nested
    class WhenRequiredPropertiesAreMissing {

        @Test
        void failsToStart_whenIssuerUriIsBlank() {
            runner.withPropertyValues("app.auth.issuer-uri=", "app.auth.audience=test-audience")
                    .run(ctx -> assertThat(ctx).hasFailed());
        }

        @Test
        void failsToStart_whenAudienceIsBlank() {
            runner.withPropertyValues(
                            "app.auth.issuer-uri=https://test.example.auth0.com/",
                            "app.auth.audience=")
                    .run(ctx -> assertThat(ctx).hasFailed());
        }

        @Test
        void failsToStart_whenBothAuthValuesAreMissing() {
            runner.run(ctx -> assertThat(ctx).hasFailed());
        }
    }
}
