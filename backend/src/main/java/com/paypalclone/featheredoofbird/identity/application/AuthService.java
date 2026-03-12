package com.paypalclone.featheredoofbird.identity.application;

import com.paypalclone.featheredoofbird.identity.domain.Role;
import com.paypalclone.featheredoofbird.identity.domain.User;
import com.paypalclone.featheredoofbird.identity.domain.UserStatus;
import com.paypalclone.featheredoofbird.identity.infrastructure.persistence.UserRepository;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "auth.provider", havingValue = "local-jwt", matchIfMissing = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthService(
            UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Transactional
    public AuthResult register(String email, String password, Role role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordHasher.hash(password));
        user.setRole(role == null ? Role.PERSONAL : role);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return createAuthResult(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        User user =
                userRepository
                        .findByEmailIgnoreCase(normalizeEmail(email))
                        .filter(User::isActive)
                        .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return createAuthResult(user);
    }

    private AuthResult createAuthResult(User user) {
        TokenIssuer.IssuedToken token = tokenIssuer.issueToken(user);
        return new AuthResult(
                token.accessToken(),
                "Bearer",
                token.expiresAt(),
                new AuthenticatedUser(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getStatus().name()));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public record AuthResult(
            String accessToken,
            String tokenType,
            java.time.Instant expiresAt,
            AuthenticatedUser user) {}

    public record AuthenticatedUser(Long id, String email, String role, String status) {}
}
