package com.yushan.analytics_service.dao;

import com.yushan.analytics_service.dto.AnalyticsTrendResponseDTO;
import com.yushan.analytics_service.dto.DailyActiveUsersResponseDTO;
import com.yushan.analytics_service.dto.ReadingActivityResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * MyBatis Mapper for Analytics queries
 * Note: In microservice architecture, this primarily works with local history data
 * Additional metrics from other services are fetched via Feign clients in the service layer
 */
@Mapper
public interface AnalyticsMapper {
    
    // User activity trends (based on history table)
    List<AnalyticsTrendResponseDTO.TrendDataPoint> getUserActivityTrends(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("period") String period);
    
    // Reading activity trends  
    List<ReadingActivityResponseDTO.ActivityDataPoint> getReadingActivityTrends(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("period") String period);
    
    // Active user counts
    Long getActiveUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    Long getDailyActiveUsers(@Param("date") Date date);
    
    Long getWeeklyActiveUsers(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    Long getMonthlyActiveUsers(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Hourly active users breakdown
    List<DailyActiveUsersResponseDTO.ActivityDataPoint> getHourlyActiveUsers(@Param("date") Date date);
    
    // Unique novel counts from history
    Long getUniqueNovelsRead(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Total history records (reading sessions)
    Long getTotalReadingSessions(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // Most read novels from history
    List<Integer> getMostReadNovelIds(@Param("limit") Integer limit);
    
    // Most active users
    List<UUID> getMostActiveUserIds(@Param("limit") Integer limit);
    
    // Most read novels in date range
    List<Integer> getMostReadNovelIdsByDateRange(
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate,
            @Param("limit") Integer limit);
}

