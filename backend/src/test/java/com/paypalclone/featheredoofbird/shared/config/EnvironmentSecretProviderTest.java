package com.paypalclone.featheredoofbird.shared.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.paypalclone.featheredoofbird.shared.secrets.SecretNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

/**
 * Unit tests for {@link EnvironmentSecretProvider}.
 *
 * <p>The class under test is package-private by design; these tests live in the same package so
 * they can instantiate it directly without exposing it as public API.
 *
 * <p>Spring's {@link Environment} is mocked with Mockito, so tests are pure unit tests — no Spring
 * context is started, making the suite fast and deterministic.
 */
@ExtendWith(MockitoExtension.class)
class EnvironmentSecretProviderTest {

    @Mock private Environment environment;

    @InjectMocks private EnvironmentSecretProvider provider;

    // ── getSecret() ───────────────────────────────────────────────────────────

    @Nested
    class GetSecret {

        @Test
        void returnsValue_whenKeyIsBound() {
            when(environment.getProperty("my.secret")).thenReturn("top-secret-value");

            String result = provider.getSecret("my.secret");

            assertThat(result).isEqualTo("top-secret-value");
        }

        @Test
        void throwsSecretNotFoundException_whenKeyIsNotBound() {
            when(environment.getProperty("unknown.key")).thenReturn(null);

            assertThatThrownBy(() -> provider.getSecret("unknown.key"))
                    .isInstanceOf(SecretNotFoundException.class)
                    .hasMessageContaining("unknown.key");
        }

        @Test
        void exceptionCarriesTheExactKey() {
            when(environment.getProperty("db.password")).thenReturn(null);

            assertThatThrownBy(() -> provider.getSecret("db.password"))
                    .isInstanceOf(SecretNotFoundException.class)
                    .extracting(ex -> ((SecretNotFoundException) ex).getKey())
                    .isEqualTo("db.password");
        }
    }

    // ── findSecret() ──────────────────────────────────────────────────────────

    @Nested
    class FindSecret {

        @Test
        void returnsPresent_whenKeyIsBound() {
            when(environment.getProperty("api.key")).thenReturn("abc-123");

            assertThat(provider.findSecret("api.key")).contains("abc-123");
        }

        @Test
        void returnsEmpty_whenKeyIsNotBound() {
            when(environment.getProperty("missing")).thenReturn(null);

            assertThat(provider.findSecret("missing")).isEmpty();
        }

        @Test
        void neverThrows_whenKeyIsNotBound() {
            when(environment.getProperty("any.key")).thenReturn(null);

            // findSecret must not propagate SecretNotFoundException
            assertThat(provider.findSecret("any.key")).isEmpty();
        }
    }

    // ── SecretNotFoundException contract ──────────────────────────────────────

    @Nested
    class SecretNotFoundExceptionContract {

        @Test
        void messageContainsKey() {
            var ex = new SecretNotFoundException("sensitive.key");

            assertThat(ex.getMessage()).contains("sensitive.key");
        }

        @Test
        void getKeyReturnsOriginalKey() {
            var ex = new SecretNotFoundException("jwt.secret");

            assertThat(ex.getKey()).isEqualTo("jwt.secret");
        }

        @Test
        void isUncheckedException() {
            assertThat(new SecretNotFoundException("x")).isInstanceOf(RuntimeException.class);
        }
    }
}
