package com.paypalclone.featheredoofbird.shared.secrets;

/**
 * Thrown by an {@link ISecretProvider} when a required secret cannot be resolved.
 *
 * <p>This is an unchecked exception: callers are expected to treat a missing secret as an
 * unrecoverable startup or configuration error rather than a normal control-flow case.
 */
public class SecretNotFoundException extends RuntimeException {

    private final String key;

    /**
     * Constructs the exception with a message that includes the missing {@code key}.
     *
     * @param key the secret key that could not be resolved
     */
    public SecretNotFoundException(String key) {
        super("Secret not found for key: '" + key + "'");
        this.key = key;
    }

    /** Returns the secret key that triggered the exception. */
    public String getKey() {
        return key;
    }
}
