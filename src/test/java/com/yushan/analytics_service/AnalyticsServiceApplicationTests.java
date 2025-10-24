package com.yushan.analytics_service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.EngagementServiceClient;
import com.yushan.analytics_service.client.GamificationServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.user.name=user-service",
        "services.user.url=http://localhost:8081",
        "services.content.name=content-service",
        "services.content.url=http://localhost:8082",
        "services.engagement.url=http://localhost:8084",
        "jwt.secret=testSecretKeyForTestingPurposesOnly123456789012345678901234567890",
        "jwt.issuer=test-micro-service",
        "jwt.algorithm=HS256",
        "jwt.expiration=3600000"
})
class AnalyticsServiceApplicationTests {

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

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
        // This test will pass if the Spring context loads successfully
    }

}

