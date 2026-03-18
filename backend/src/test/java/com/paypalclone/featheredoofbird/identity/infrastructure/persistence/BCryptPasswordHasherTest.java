package com.paypalclone.featheredoofbird.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordHasherTest {

    private BCryptPasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new BCryptPasswordHasher(new BCryptPasswordEncoder());
    }

    @Test
    void hash_returnsNonBlankString() {
        assertThat(hasher.hash("password123")).isNotBlank();
    }

    @Test
    void hash_resultIsDifferentFromRawPassword() {
        String hashed = hasher.hash("password123");

        assertThat(hashed).isNotEqualTo("password123");
    }

    @Test
    void hash_resultStartsWithBCryptPrefix() {
        String hashed = hasher.hash("anypassword");

        assertThat(hashed).startsWith("$2");
    }

    @Test
    void hash_samePasswordProducesDifferentHashesEachTime() {
        String first = hasher.hash("password");
        String second = hasher.hash("password");

        // BCrypt uses random salts
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void matches_returnsTrueForCorrectPassword() {
        String hashed = hasher.hash("secret");

        assertThat(hasher.matches("secret", hashed)).isTrue();
    }

    @Test
    void matches_returnsFalseForWrongPassword() {
        String hashed = hasher.hash("correct");

        assertThat(hasher.matches("wrong", hashed)).isFalse();
    }

    @Test
    void matches_isCaseSensitive() {
        String hashed = hasher.hash("Password123");

        assertThat(hasher.matches("password123", hashed)).isFalse();
    }
}
