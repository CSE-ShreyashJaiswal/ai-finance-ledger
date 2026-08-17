package com.financeledger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Temporary security configuration — permits all requests.
 *
 * <p><b>This will be replaced in Week 3</b> with proper JWT-based auth
 * that scopes endpoints to authenticated users.
 *
 * <p>We include Spring Security from day one so the dependency is
 * wired in and tested, but we don't want it blocking access
 * during initial development.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                     // Disable CSRF for REST API
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()                      // TODO: Lock down in Week 3
            );

        return http.build();
    }
}
