package com.exalt.shared.admin.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Base security configuration for all admin applications.
 * Provides standardized security settings across all admin dashboards.
 */
@Configuration
@EnableWebSecurity
public class BaseAdminSecurityConfig {

    /**
     * Configures HTTP security settings with standard policies for admin dashboards.
     * Can be customized by domain-specific security configurations by overriding specific settings.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // For admin APIs, usually disabled and handled by tokens
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "ANALYST")
                .requestMatchers("/api/health/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt()
            );
        
        return http.build();
    }
    
    /**
     * This method can be overridden by domain-specific configurations
     * to add additional security settings or customizations.
     */
    protected void configureDomainSpecificSecurity(HttpSecurity http) throws Exception {
        // Override in domain-specific security configs
    }
}
