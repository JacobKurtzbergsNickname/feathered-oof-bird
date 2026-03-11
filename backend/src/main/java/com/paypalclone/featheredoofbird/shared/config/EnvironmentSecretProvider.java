package com.paypalclone.featheredoofbird.shared.config;

import com.paypalclone.featheredoofbird.shared.secrets.ISecretProvider;
import com.paypalclone.featheredoofbird.shared.secrets.SecretNotFoundException;
import java.util.Optional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Default {@link ISecretProvider} adapter that resolves secrets from the active Spring {@link
 * Environment}.
 *
 * <p>The {@code Environment} already aggregates multiple property sources in priority order:
 *
 * <ol>
 *   <li>OS environment variables
 *   <li>JVM system properties ({@code -Dprop=value})
 *   <li>Application property files ({@code application.properties}, profile overrides, etc.)
 * </ol>
 *
 * <p>This makes it an appropriate default for local development and any environment where secrets
 * are injected as environment variables (e.g. Kubernetes Secrets, Fly.io / Railway / Render secret
 * env-blocks, Docker {@code --env-file}). For production workloads requiring a dedicated secret
 * manager, replace this adapter with one backed by AWS Secrets Manager, HashiCorp Vault, or similar
 * — without changing any call-site code.
 *
 * <p>This class is intentionally package-private to prevent direct construction from other modules;
 * callers should depend on {@link ISecretProvider} only.
 */
@Component
class EnvironmentSecretProvider implements ISecretProvider {

    private final Environment environment;

    EnvironmentSecretProvider(Environment environment) {
        this.environment = environment;
    }

    /**
     * {@inheritDoc}
     *
     * @throws SecretNotFoundException if the key is not bound in any active property source
     */
    @Override
    public String getSecret(String key) {
        return findSecret(key).orElseThrow(() -> new SecretNotFoundException(key));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> findSecret(String key) {
        return Optional.ofNullable(environment.getProperty(key));
    }
}
