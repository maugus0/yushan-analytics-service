package com.yushan.analytics_service.client;

import com.yushan.analytics_service.config.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * Feign client for Gamification Service
 * Currently not used for ranking, but available for future integration
 */
@FeignClient(
        name = "gamification-service",
        url = "${services.gamification.url:http://yushan-gamification-service:8085}",
        configuration = FeignAuthConfig.class
)
public interface GamificationServiceClient {
    // Future endpoints can be added here as needed
}
