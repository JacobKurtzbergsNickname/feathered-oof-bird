package com.paypalclone.featheredoofbird.shared.config;

import com.paypalclone.featheredoofbird.auth.AuthenticationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationStrategy authenticationStrategy;

    public SecurityConfig(AuthenticationStrategy authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    @Bean
    @SuppressWarnings("unused")
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(
                                                "/",
                                                "/index.html",
                                                "/assets/**",
                                                "/favicon.ico",
                                                "/error",
                                                "/public/**",
                                                "/actuator/health",
                                                "/api/auth/**")
                                        .permitAll()
                                        .requestMatchers("/api/admin/**")
                                        .hasAuthority("SCOPE_admin:all")
                                        .requestMatchers("/api/**")
                                        .authenticated()
                                        .anyRequest()
                                        .denyAll())
                .oauth2ResourceServer(
                        oauth ->
                                oauth.jwt(
                                        jwt ->
                                                jwt.jwtAuthenticationConverter(
                                                        authenticationStrategy
                                                                .jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @SuppressWarnings("unused")
    JwtDecoder jwtDecoder() {
        return authenticationStrategy.jwtDecoder();
    }
}
