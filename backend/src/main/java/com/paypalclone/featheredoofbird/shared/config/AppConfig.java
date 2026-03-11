package com.paypalclone.featheredoofbird.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Immutable, validated application configuration sourced from {@code app.*} properties.
 *
 * <p>Values are populated from environment variables, system properties, or config files — in that
 * order of precedence. <strong>No secrets may be hardcoded here</strong>; use {@code
 * ${ENV_VAR:defaultForDev}} syntax in {@code application.properties} and supply real values via
 * environment variables in every non-local environment.
 *
 * <p>This record is the single injection point for cross-cutting application configuration.
 * Components that need a specific setting should prefer constructor-injecting this record over raw
 * {@code @Value} fields, which makes dependencies explicit and unit tests trivial (just construct
 * the record directly).
 *
 * <h2>Adding new settings</h2>
 *
 * <ol>
 *   <li>Add a record component (or a nested record) here with the appropriate validation
 *       annotation.
 *   <li>Add the corresponding {@code app.<section>.<key>=${ENV_VAR:devDefault}} entry to {@code
 *       application.properties}.
 *   <li>Override with a safe dev placeholder in {@code application-dev.properties} if needed.
 * </ol>
 */
@ConfigurationProperties(prefix = "app")
@Validated
public record AppConfig(@Valid @NotNull Auth auth) {

    /**
     * Auth / identity-provider settings.
     *
     * <p>The active provider determines which nested properties must be populated. Auth0 settings
     * may stay blank when local JWT is enabled, while local JWT settings are always given safe
     * defaults for development.
     */
    public record Auth(
            @NotBlank(message = "app.auth.provider must be set") String provider,
            String issuerUri,
            String audience,
            @Valid @NotNull LocalJwt localJwt) {

        /**
         * Returns {@code true} when the active provider has all required values.
         *
         * <p>Useful for conditional behaviour in components that must adapt at runtime (e.g. the
         * security configuration selecting the correct JWT strategy).
         */
        public boolean isFullyConfigured() {
            return isLocalJwtProvider()
                    ? localJwt.isFullyConfigured()
                    : isExternalProviderConfigured();
        }

        public boolean isLocalJwtProvider() {
            return "local-jwt".equals(provider);
        }

        public boolean isExternalProviderConfigured() {
            return issuerUri != null
                    && !issuerUri.isBlank()
                    && audience != null
                    && !audience.isBlank();
        }
    }

    public record LocalJwt(
            @NotBlank(message = "app.auth.local-jwt.issuer must be set") String issuer,
            @NotBlank(message = "app.auth.local-jwt.secret must be set") String secret,
            @NotNull(message = "app.auth.local-jwt.access-token-ttl must be set")
                    Duration accessTokenTtl) {

        public boolean isFullyConfigured() {
            return !issuer.isBlank() && !secret.isBlank() && !accessTokenTtl.isZero();
        }
    }
}
