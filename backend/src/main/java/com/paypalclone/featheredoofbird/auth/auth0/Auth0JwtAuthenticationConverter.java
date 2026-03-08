package com.paypalclone.featheredoofbird.auth.auth0;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Converts an Auth0 JWT into a {@link JwtAuthenticationToken} by combining
 * standard scope-based authorities with Auth0's {@code permissions} claim.
 */
public class Auth0JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopeConverter;

    public Auth0JwtAuthenticationConverter() {
        this(new JwtGrantedAuthoritiesConverter());
    }

    Auth0JwtAuthenticationConverter(JwtGrantedAuthoritiesConverter scopeConverter) {
        this.scopeConverter = scopeConverter;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));

        Object permissions = jwt.getClaim("permissions");
        if (permissions instanceof Collection<?> collection) {
            collection.stream()
                    .map(Object::toString)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("sub"));
    }
}
