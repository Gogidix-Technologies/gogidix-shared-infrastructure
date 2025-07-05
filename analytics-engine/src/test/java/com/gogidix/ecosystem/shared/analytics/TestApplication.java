package com.gogidix.ecosystem.shared.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;

/**
 * Test application configuration that excludes problematic auto-configurations
 */
@SpringBootApplication(
    scanBasePackages = "com.gogidix.ecosystem.shared.analytics.controller",
    exclude = {
        SecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        RedisAutoConfiguration.class,
        FlywayAutoConfiguration.class
    }
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}