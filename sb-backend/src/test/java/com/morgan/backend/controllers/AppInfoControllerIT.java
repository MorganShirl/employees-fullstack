package com.morgan.backend.controllers;

import com.morgan.backend.controllers.AppInfoController.RequestThreadInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AppInfoControllerIT {

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void testHttpRequestRunsOnVirtualThread() {
        var response = testRestTemplate.getForEntity("/api/info/request-thread", RequestThreadInfoDto.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().virtual()).isTrue();
    }
}
