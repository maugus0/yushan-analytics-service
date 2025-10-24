package com.yushan.analytics_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "eureka.client.enabled=false",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "services.user.name=user-service",
        "services.user.url=http://localhost:8081",
        "services.content.name=content-service",
        "services.content.url=http://localhost:8082",
        "jwt.secret=testSecretKeyForTestingPurposesOnly1234567890",
        "jwt.issuer=test-micro-service",
        "jwt.algorithm=HS256",
        "jwt.expiration=3600000"
})
class AnalyticsServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}