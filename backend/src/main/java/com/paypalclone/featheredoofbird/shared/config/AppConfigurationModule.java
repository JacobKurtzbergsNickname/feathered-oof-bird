package com.paypalclone.featheredoofbird.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Activates typed {@code @ConfigurationProperties} records for the application.
 *
 * <p>Declaring {@link EnableConfigurationProperties} here rather than on the main application class
 * makes it trivial to import only this module in slice tests, giving tests a minimal Spring context
 * that binds configuration without standing up the entire application.
 *
 * <p>Example in a test:
 *
 * <pre>{@code
 * new ApplicationContextRunner()
 *     .withUserConfiguration(AppConfigurationModule.class)
 *     .withPropertyValues("app.auth.issuer-uri=...", "app.auth.audience=...")
 *     .run(ctx -> { ... });
 * }</pre>
 */
@Configuration
@EnableConfigurationProperties(AppConfig.class)
public class AppConfigurationModule {}
