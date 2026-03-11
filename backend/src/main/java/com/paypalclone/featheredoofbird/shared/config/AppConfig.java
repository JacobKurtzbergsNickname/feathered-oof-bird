package com.paypalclone.featheredoofbird.shared.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     * <p>Both fields are mandatory in every non-dev environment; the {@code @NotBlank} constraints
     * cause a startup failure if they are absent, surfacing the misconfiguration immediately rather
     * than at request time. The {@code dev} profile supplies safe placeholder values in {@code
     * application-dev.properties} so the application can boot without real Auth0 credentials.
     */
    public record Auth(
            @NotBlank(message = "app.auth.issuer-uri must be set (env: AUTH0_ISSUER_URI)")
                    String issuerUri,
            @NotBlank(message = "app.auth.audience must be set (env: AUTH0_AUDIENCE)")
                    String audience) {

        /**
         * Returns {@code true} when both {@code issuerUri} and {@code audience} are non-blank.
         *
         * <p>Useful for conditional behaviour in components that must adapt at runtime (e.g. the
         * security configuration skipping JWT validation in the dev profile).
         */
        public boolean isFullyConfigured() {
            return !issuerUri.isBlank() && !audience.isBlank();
        }
    }
}
