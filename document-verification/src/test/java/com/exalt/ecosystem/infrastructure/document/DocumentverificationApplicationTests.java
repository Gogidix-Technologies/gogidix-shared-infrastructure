package com.exalt.ecosystem.infrastructure.document;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.exalt.ecosystem.shared.documentverification.DocumentVerificationApplication;

@SpringBootTest(classes = DocumentVerificationApplication.class)
@ActiveProfiles("test")
class DocumentverificationApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures the Spring context loads successfully
    }
}
