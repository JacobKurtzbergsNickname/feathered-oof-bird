package com.paypalclone.featheredoofbird.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import com.paypalclone.featheredoofbird.auth.AuthenticationStrategy;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private DevAuthenticationProvider devAuthenticationProvider;

    @Autowired(required = false)
    private AuthenticationStrategy authenticationStrategy;

    /**
     * Spring Security looks for a SecurityFilterChain bean by type at runtime.
     * This method is not called directly in code, but is required for configuration.
     */
    @Bean
    @Profile("dev")
    SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("USER")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .authenticationProvider(devAuthenticationProvider)
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
                        .jwtAuthenticationConverter(authenticationStrategy.jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    @Profile("!dev")
    @SuppressWarnings("unused")
    JwtDecoder jwtDecoder() {
        return authenticationStrategy.jwtDecoder();
    }
}
