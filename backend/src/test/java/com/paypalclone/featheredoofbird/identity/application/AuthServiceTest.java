package com.paypalclone.featheredoofbird.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.paypalclone.featheredoofbird.identity.domain.Role;
import com.paypalclone.featheredoofbird.identity.domain.User;
import com.paypalclone.featheredoofbird.identity.domain.UserStatus;
import com.paypalclone.featheredoofbird.identity.infrastructure.persistence.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private TokenIssuer tokenIssuer;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        tokenIssuer = mock(TokenIssuer.class);
        authService = new AuthService(userRepository, passwordHasher, tokenIssuer);
    }

    @Test
    void registerHashesPasswordAndReturnsAuthResult() {
        User savedUser = new User();
        savedUser.setId(7L);
        savedUser.setEmail("new@example.com");
        savedUser.setRole(Role.PERSONAL);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser.setPasswordHash("encoded");

        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordHasher.hash("secret123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenIssuer.issueToken(savedUser))
                .thenReturn(
                        new TokenIssuer.IssuedToken(
                                "token-value", Instant.parse("2026-03-11T12:00:00Z")));

        AuthService.AuthResult result =
                authService.register("New@example.com", "secret123", Role.PERSONAL);

        assertThat(result.accessToken()).isEqualTo("token-value");
        assertThat(result.user().email()).isEqualTo("new@example.com");
        verify(passwordHasher).hash("secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(
                        () -> authService.register("taken@example.com", "secret123", Role.PERSONAL))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void loginReturnsTokenWhenCredentialsMatch() {
        User user = new User();
        user.setId(3L);
        user.setEmail("user@example.com");
        user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash("encoded");

        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("secret123", "encoded")).thenReturn(true);
        when(tokenIssuer.issueToken(user))
                .thenReturn(
                        new TokenIssuer.IssuedToken(
                                "jwt-token", Instant.parse("2026-03-11T13:00:00Z")));

        AuthService.AuthResult result = authService.login("USER@example.com", "secret123");

        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.user().role()).isEqualTo("ADMIN");
    }

    @Test
    void loginRejectsInvalidCredentials() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash("encoded");

        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrongpass", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("user@example.com", "wrongpass"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
