package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.util.JwtTestUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Test Controller for generating JWT tokens
 * This is only for development/testing purposes
 */
@Tag(name = "Test Utilities", description = "Test APIs for generating JWT tokens (Development only)")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JwtTestUtil jwtTestUtil;

    /**
     * Generate test JWT token for USER role
     */
    @Operation(summary = "[TEST] Generate USER token", description = "Generate test JWT token for USER role (Development only)")
    @GetMapping("/token/user")
    public ApiResponse<Map<String, String>> getUserToken() {
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("token", jwtTestUtil.generateTestUserToken());
        tokenData.put("role", "USER");
        tokenData.put("message", "Use this token in Authorization header: Bearer <token>");
        return ApiResponse.success("USER token generated successfully", tokenData);
    }

    /**
     * Generate test JWT token for ADMIN role
     */
    @Operation(summary = "[TEST] Generate ADMIN token", description = "Generate test JWT token for ADMIN role (Development only)")
    @GetMapping("/token/admin")
    public ApiResponse<Map<String, String>> getAdminToken() {
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("token", jwtTestUtil.generateTestAdminToken());
        tokenData.put("role", "ADMIN");
        tokenData.put("message", "Use this token in Authorization header: Bearer <token>");
        return ApiResponse.success("ADMIN token generated successfully", tokenData);
    }

    /**
     * Generate test JWT token for suspended user
     */
    @Operation(summary = "[TEST] Generate SUSPENDED token", description = "Generate test JWT token for suspended user (Development only)")
    @GetMapping("/token/suspended")
    public ApiResponse<Map<String, String>> getSuspendedToken() {
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("token", jwtTestUtil.generateTestSuspendedToken());
        tokenData.put("role", "USER (SUSPENDED)");
        tokenData.put("message", "This token should be rejected due to suspended status");
        return ApiResponse.success("SUSPENDED token generated successfully", tokenData);
    }
}
