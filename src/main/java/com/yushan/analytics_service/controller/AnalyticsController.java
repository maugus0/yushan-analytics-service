package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.AnalyticsRequestDTO;
import com.yushan.analytics_service.dto.AnalyticsSummaryResponseDTO;
import com.yushan.analytics_service.dto.AnalyticsTrendResponseDTO;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.DailyActiveUsersResponseDTO;
import com.yushan.analytics_service.dto.PlatformStatisticsResponseDTO;
import com.yushan.analytics_service.dto.ReadingActivityResponseDTO;
import com.yushan.analytics_service.dto.TopContentResponseDTO;
import com.yushan.analytics_service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Analytics (Admin)", description = "Admin analytics APIs for platform insights and metrics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Get user activity trends analytics
     * Admin only endpoint for analyzing user activity trends
     */
    @Operation(summary = "Get user activity trends", description = "Analyze user activity patterns over time")
    @GetMapping("/users/trends")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnalyticsTrendResponseDTO> getUserTrends(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(value = "startDate", required = false) String startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(value = "endDate", required = false) String endDate,
            
            @Parameter(description = "Period: daily, weekly, or monthly")
            @RequestParam(value = "period", defaultValue = "daily") String period,
            
            @Parameter(description = "Category ID filter")
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            
            @Parameter(description = "Author ID filter")
            @RequestParam(value = "authorId", required = false) String authorId,
            
            @Parameter(description = "Status filter")
            @RequestParam(value = "status", required = false) Integer status) {
        
        AnalyticsRequestDTO request = new AnalyticsRequestDTO();
        request.setPeriod(period);
        request.setCategoryId(categoryId);
        request.setAuthorId(authorId);
        request.setStatus(status);
        
        // Parse dates if provided
        if (startDate != null && !startDate.isEmpty()) {
            try {
                request.setStartDate(java.sql.Date.valueOf(startDate));
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid startDate format. Use YYYY-MM-DD");
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                request.setEndDate(java.sql.Date.valueOf(endDate));
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid endDate format. Use YYYY-MM-DD");
            }
        }

        AnalyticsTrendResponseDTO response = analyticsService.getUserTrends(request);
        return ApiResponse.success("User trends retrieved successfully", response);
    }

    /**
     * Get reading activity analytics
     * Admin only endpoint for analyzing reading activity trends
     */
    @Operation(summary = "Get reading activity trends", description = "Analyze reading activity patterns and engagement")
    @GetMapping("/reading/activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ReadingActivityResponseDTO> getReadingActivityTrends(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(value = "startDate", required = false) String startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(value = "endDate", required = false) String endDate,
            
            @Parameter(description = "Period: daily, weekly, or monthly")
            @RequestParam(value = "period", defaultValue = "daily") String period) {
        
        AnalyticsRequestDTO request = new AnalyticsRequestDTO();
        request.setPeriod(period);
        
        // Parse dates if provided
        if (startDate != null && !startDate.isEmpty()) {
            try {
                request.setStartDate(java.sql.Date.valueOf(startDate));
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid startDate format. Use YYYY-MM-DD");
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                request.setEndDate(java.sql.Date.valueOf(endDate));
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid endDate format. Use YYYY-MM-DD");
            }
        }

        ReadingActivityResponseDTO response = analyticsService.getReadingActivityTrends(request);
        return ApiResponse.success("Reading activity trends retrieved successfully", response);
    }

    /**
     * Get analytics summary
     * Admin only endpoint for getting comprehensive analytics summary
     */
    @Operation(summary = "Get analytics summary", description = "Get comprehensive analytics summary with key metrics")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AnalyticsSummaryResponseDTO> getAnalyticsSummary(
            @Parameter(description = "Start date (YYYY-MM-DD)")
            @RequestParam(value = "startDate", required = false) String startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)")
            @RequestParam(value = "endDate", required = false) String endDate,
            
            @Parameter(description = "Period: daily, weekly, or monthly")
            @RequestParam(value = "period", defaultValue = "daily") String period) {
        
        AnalyticsRequestDTO request = new AnalyticsRequestDTO();
        request.setPeriod(period);
        
        // Parse dates if provided
        if (startDate != null && !startDate.isEmpty()) {
            try {
                request.setStartDate(java.sql.Date.valueOf(startDate));
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid startDate format. Use YYYY-MM-DD");
            }
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                request.setEndDate(java.sql.Date.valueOf(endDate));
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid endDate format. Use YYYY-MM-DD");
            }
        }

        AnalyticsSummaryResponseDTO response = analyticsService.getAnalyticsSummary(request);
        return ApiResponse.success("Analytics summary retrieved successfully", response);
    }

    /**
     * Get platform-wide statistics overview
     * Admin only endpoint for comprehensive platform statistics
     */
    @Operation(summary = "Get platform statistics", description = "Get comprehensive platform-wide statistics")
    @GetMapping("/platform/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PlatformStatisticsResponseDTO> getPlatformStatistics() {
        PlatformStatisticsResponseDTO response = analyticsService.getPlatformStatistics();
        return ApiResponse.success("Platform statistics retrieved successfully", response);
    }

    /**
     * Get daily active users statistics
     * Admin only endpoint for DAU analysis
     */
    @Operation(summary = "Get daily active users", description = "Get daily active users with hourly breakdown")
    @GetMapping("/platform/dau")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DailyActiveUsersResponseDTO> getDailyActiveUsers(
            @Parameter(description = "Target date (YYYY-MM-DD)")
            @RequestParam(value = "date", required = false) String date) {
        
        Date targetDate = new Date();
        if (date != null && !date.isEmpty()) {
            try {
                targetDate = java.sql.Date.valueOf(date);
            } catch (Exception e) {
                return ApiResponse.error(400, "Invalid date format. Use YYYY-MM-DD");
            }
        }

        DailyActiveUsersResponseDTO response = analyticsService.getDailyActiveUsers(targetDate);
        return ApiResponse.success("Daily active users retrieved successfully", response);
    }

    /**
     * Get top content statistics
     * Admin only endpoint for top content analysis
     */
    @Operation(summary = "Get top content", description = "Get top novels, authors, and categories by engagement")
    @GetMapping("/platform/top-content")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TopContentResponseDTO> getTopContent(
            @Parameter(description = "Limit (1-100)")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        
        if (limit <= 0 || limit > 100) {
            return ApiResponse.error(400, "Limit must be between 1 and 100");
        }

        TopContentResponseDTO response = analyticsService.getTopContent(limit);
        return ApiResponse.success("Top content statistics retrieved successfully", response);
    }
}
