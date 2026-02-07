package com.paypalclone.featheredoofbird.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String issuerUri;
    private final String audience;

    @Autowired(required = false)
    private JwtAuthenticationProvider jwtAuthenticationProvider;

    @Autowired(required = false)
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${auth0.audience}") String audience) {
        this.issuerUri = issuerUri;
        this.audience = audience;
    }

    /**
     * Spring Security looks for a SecurityFilterChain bean by type at runtime.
     * This method is not called directly in code, but is required for configuration.
     */
    @Bean
    @Profile("dev")
    @SuppressWarnings("unused")
    SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("USER")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(jwtAuthenticationProvider)
            .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }

    @Bean
    @Profile("!dev")
    @SuppressWarnings("unused")
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/actuator/health").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin:all")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(auth0JwtAuthConverter())));
        return http.build();
    }

    /**
     * Spring Security looks for a JwtDecoder bean by type at runtime.
     * This method is not called directly in code, but is required for configuration.
     */
    @Bean
    @Profile("!dev")
    @SuppressWarnings("unused")
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
                issuerValidator, audienceValidator);
        jwtDecoder.setJwtValidator(combinedValidator);
        return jwtDecoder;
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken> auth0JwtAuthConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            Object permissions = jwt.getClaim("permissions");
            if (permissions instanceof Collection<?> collection) {
                collection.stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("sub"));
        };
    }

    private static class AudienceValidator implements OAuth2TokenValidator<Jwt> {

        private final String audience;

        private AudienceValidator(String audience) {
            this.audience = audience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            Collection<String> tokenAudience = token.getAudience();
            if (tokenAudience != null && tokenAudience.contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Required audience is missing", null));
        }
    }
}
