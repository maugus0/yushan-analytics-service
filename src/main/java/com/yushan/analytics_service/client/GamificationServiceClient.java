package com.yushan.analytics_service.client;

import com.yushan.analytics_service.config.FeignAuthConfig;
import com.yushan.analytics_service.dto.ApiResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Feign client for Gamification Service
 */
@FeignClient(
        name = "gamification-service",
        url = "${services.gamification.url:http://yushan-gamification-service:8085}",
        configuration = FeignAuthConfig.class
)
public interface GamificationServiceClient {

    @GetMapping("/api/v1/gamification/stats/all")
    ApiResponse<List<GamificationStats>> getAllUsersStats();

    @GetMapping("/api/v1/gamification/stats/userId/{userId}")
    ApiResponse<GamificationStats> getUserStats(@PathVariable("userId") String userId);

    @PostMapping("/api/v1/gamification/stats/batch")
    ApiResponse<List<GamificationStats>> getBatchUsersStats(@RequestBody List<String> userIds);

    // Inner class for gamification stats - fields are used by Jackson for deserialization
    @SuppressWarnings({"unused", "checkstyle:VisibilityModifier"})
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD",
            justification = "Fields are populated by Jackson during JSON deserialization")
    class GamificationStats {
        public String userId;
        public Integer level;
        public Integer currentExp;
        public Integer totalExpForNextLevel;
        public Integer yuanBalance;
    }
}
