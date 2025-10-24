package com.yushan.analytics_service.integration;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.EngagementServiceClient;
import com.yushan.analytics_service.client.GamificationServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.config.TestRedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false"
})
@Import(TestRedisConfig.class)
@ActiveProfiles("integration-test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.discovery.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.config.import=optional:configserver:",
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.user.name=user-service",
        "services.user.url=http://localhost:8081",
        "services.content.name=content-service",
        "services.content.url=http://localhost:8082",
        "services.engagement.name=engagement-service",
        "services.engagement.url=http://localhost:8084",
        "services.gamification.name=gamification-service",
        "services.gamification.url=http://localhost:8085",
        "jwt.secret=testSecretKeyForTestingPurposesOnly123456789012345678901234567890",
        "jwt.issuer=test-micro-service",
        "jwt.algorithm=HS256",
        "jwt.expiration=3600000",
        "spring.main.lazy-initialization=true"
})
class AnalyticsServiceIntegrationTest {

    @MockBean
    private ContentServiceClient contentServiceClient;

    @MockBean
    private UserServiceClient userServiceClient;

    @MockBean
    private EngagementServiceClient engagementServiceClient;

    @MockBean
    private GamificationServiceClient gamificationServiceClient;

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully in integration test mode
    }

}

