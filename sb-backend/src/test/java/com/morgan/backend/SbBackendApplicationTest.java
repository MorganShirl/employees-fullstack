package com.morgan.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class SbBackendApplicationTest {

    // Checks that all Spring beans can be created and wired correctly.
    @Test
    void testLoadSpringContext() {}
}
