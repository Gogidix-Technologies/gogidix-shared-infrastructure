package com.exalt.ecosystem.shared.kycservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class ApplicationTests {

    @Test
    void contextLoads() {
        // Basic test to ensure Spring context loads
    }
}
