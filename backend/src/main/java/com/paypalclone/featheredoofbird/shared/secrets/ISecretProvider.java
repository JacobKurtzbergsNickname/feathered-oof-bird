package com.paypalclone.featheredoofbird.shared.secrets;

import java.util.Optional;

/**
 * Port for resolving secrets at runtime.
 *
 * <p>Concrete adapters may back this port with environment variables, a cloud secret manager (AWS
 * Secrets Manager, HashiCorp Vault, GCP Secret Manager, etc.) or any other external store.
 * Production code should always depend on this interface, never on a specific adapter, so that the
 * backing store can be swapped without touching business logic.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Service
 * class SomeService {
 *     private final ISecretProvider secrets;
 *
 *     SomeService(ISecretProvider secrets) { this.secrets = secrets; }
 *
 *     void doWork() {
 *         String apiKey = secrets.getSecret("external.api.key");
 *     }
 * }
 * }</pre>
 */
public interface ISecretProvider {

    /**
     * Returns the plaintext value for the given {@code key}.
     *
     * @param key the logical name of the secret (e.g. {@code "db.password"} or {@code
     *     "external.api.key"})
     * @return the secret value – never {@code null}
     * @throws SecretNotFoundException if no value is bound to {@code key} in the active provider
     */
    String getSecret(String key);

    /**
     * Returns the plaintext value for the given {@code key}, or {@link Optional#empty()} when the
     * key is not bound.
     *
     * <p>Prefer {@link #getSecret(String)} when the secret is mandatory; use this overload only for
     * truly optional secrets to avoid swallowing missing-configuration bugs.
     *
     * @param key the logical name of the secret
     * @return an {@link Optional} containing the value, or empty if absent
     */
    Optional<String> findSecret(String key);
}
