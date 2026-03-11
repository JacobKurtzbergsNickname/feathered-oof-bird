package com.paypalclone.featheredoofbird.identity.application;

import com.paypalclone.featheredoofbird.identity.domain.User;
import java.time.Instant;

public interface TokenIssuer {

    IssuedToken issueToken(User user);

    record IssuedToken(String accessToken, Instant expiresAt) {}
}
