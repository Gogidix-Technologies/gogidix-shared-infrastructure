package com.gogidix.ecommerce.admin.websocket.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

/**
 * Test configuration for WebSocket security tests.
 */
@TestConfiguration
public class WebSocketSecurityTestConfig {

    @Bean
    @Primary
    public JwtTokenProvider testJwtTokenProvider() {
        return new JwtTokenProvider() {
            @Override
            public boolean validateToken(String authToken) {
                return authToken != null && authToken.startsWith("test-token-");
            }

            @Override
            public String getUsernameFromToken(String token) {
                return token.replace("test-token-", "");
            }

            @Override
            public Authentication getAuthentication(String token) {
                String username = getUsernameFromToken(token);
                UserDetails userDetails = User.withUsername(username)
                        .password("password")
                        .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                        .build();
                return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
            }
        };
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("testuser")
                .password("{noop}password")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}
