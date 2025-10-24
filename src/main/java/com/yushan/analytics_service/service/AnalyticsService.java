package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.EngagementServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dao.AnalyticsMapper;
import com.yushan.analytics_service.dto.AnalyticsRequestDTO;
import com.yushan.analytics_service.dto.AnalyticsSummaryResponseDTO;
import com.yushan.analytics_service.dto.AnalyticsTrendResponseDTO;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.DailyActiveUsersResponseDTO;
import com.yushan.analytics_service.dto.NovelDetailResponseDTO;
import com.yushan.analytics_service.dto.PlatformStatisticsResponseDTO;
import com.yushan.analytics_service.dto.ReadingActivityResponseDTO;
import com.yushan.analytics_service.dto.TopContentResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsMapper analyticsMapper;

    @SuppressWarnings("unused") // Reserved for future user data fetching
    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private EngagementServiceClient engagementServiceClient;

    /**
     * Get user activity trends (based on reading history)
     */
    public AnalyticsTrendResponseDTO getUserTrends(AnalyticsRequestDTO request) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        String period = request.getPeriod();

        // Set default date range if not provided
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            if (endDate == null) {
                endDate = cal.getTime();
            }
            if (startDate == null) {
                cal.add(Calendar.DAY_OF_MONTH, -30); // Default to last 30 days
                startDate = cal.getTime();
            }
        }

        List<AnalyticsTrendResponseDTO.TrendDataPoint> dataPoints = 
            analyticsMapper.getUserActivityTrends(startDate, endDate, period);

        // Calculate growth rates
        calculateGrowthRates(dataPoints);

        AnalyticsTrendResponseDTO response = new AnalyticsTrendResponseDTO();
        response.setPeriod(period);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setDataPoints(dataPoints);
        response.setTotalCount(dataPoints.stream().mapToLong(dp -> dp.getCount()).sum());
        response.setAverageGrowth(calculateAverageGrowth(dataPoints));
        
        // Find peak value
        if (!dataPoints.isEmpty()) {
            AnalyticsTrendResponseDTO.TrendDataPoint peak = dataPoints.stream()
                .max((dp1, dp2) -> Long.compare(dp1.getCount(), dp2.getCount()))
                .orElse(null);
            if (peak != null) {
                response.setPeakValue(peak.getCount());
                response.setPeakDate(peak.getPeriodLabel());
            }
        }

        return response;
    }

    /**
     * Get reading activity trends
     */
    public ReadingActivityResponseDTO getReadingActivityTrends(AnalyticsRequestDTO request) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        String period = request.getPeriod();

        // Set default date range if not provided
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            if (endDate == null) {
                endDate = cal.getTime();
            }
            if (startDate == null) {
                cal.add(Calendar.DAY_OF_MONTH, -30); // Default to last 30 days
                startDate = cal.getTime();
            }
        }

        List<ReadingActivityResponseDTO.ActivityDataPoint> dataPoints = 
            analyticsMapper.getReadingActivityTrends(startDate, endDate, period);

        ReadingActivityResponseDTO response = new ReadingActivityResponseDTO();
        response.setPeriod(period);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setDataPoints(dataPoints);
        
        // Calculate totals and averages
        Long totalActivity = dataPoints.stream()
            .mapToLong(dp -> dp.getTotalActivity() != null ? dp.getTotalActivity() : 0L)
            .sum();
        response.setTotalActivity(totalActivity);
        
        if (!dataPoints.isEmpty()) {
            response.setAverageDailyActivity(totalActivity.doubleValue() / dataPoints.size());
            
            // Find peak activity
            ReadingActivityResponseDTO.ActivityDataPoint peak = dataPoints.stream()
                .max((dp1, dp2) -> Long.compare(
                    dp1.getTotalActivity() != null ? dp1.getTotalActivity() : 0L,
                    dp2.getTotalActivity() != null ? dp2.getTotalActivity() : 0L))
                .orElse(null);
            if (peak != null) {
                response.setPeakActivity(peak.getTotalActivity() != null ? peak.getTotalActivity() : 0L);
                response.setPeakDate(peak.getPeriodLabel());
            }
        }

        return response;
    }

    /**
     * Get analytics summary
     */
    public AnalyticsSummaryResponseDTO getAnalyticsSummary(AnalyticsRequestDTO request) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();

        // Set default date range if not provided
        if (startDate == null || endDate == null) {
            Calendar cal = Calendar.getInstance();
            if (endDate == null) {
                endDate = cal.getTime();
            }
            if (startDate == null) {
                cal.add(Calendar.DAY_OF_MONTH, -30); // Default to last 30 days
                startDate = cal.getTime();
            }
        }

        AnalyticsSummaryResponseDTO response = new AnalyticsSummaryResponseDTO();
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setPeriod(request.getPeriod());

        // Get metrics from local analytics database
        Long activeUsers = analyticsMapper.getActiveUserCount(startDate, endDate);
        Long uniqueNovels = analyticsMapper.getUniqueNovelsRead(startDate, endDate);
        Long readingSessions = analyticsMapper.getTotalReadingSessions(startDate, endDate);

        response.setActiveUsers(activeUsers);
        response.setUniqueNovelsRead(uniqueNovels);
        response.setTotalReadingSessions(readingSessions);

        // Calculate growth rates
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        long daysDiff = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
        cal.add(Calendar.DAY_OF_MONTH, -(int)daysDiff);
        Date previousStartDate = cal.getTime();
        Date previousEndDate = new Date(startDate.getTime() - 1);
        
        // User growth rate
        Long previousActiveUsers = analyticsMapper.getActiveUserCount(previousStartDate, previousEndDate);
        response.setUserGrowthRate(calculateGrowthRate(previousActiveUsers, activeUsers));
        
        // Novel growth rate
        Long previousNovels = analyticsMapper.getUniqueNovelsRead(previousStartDate, previousEndDate);
        response.setNovelGrowthRate(calculateGrowthRate(previousNovels, uniqueNovels));
        
        // Session growth rate
        Long previousSessions = analyticsMapper.getTotalReadingSessions(previousStartDate, previousEndDate);
        response.setSessionGrowthRate(calculateGrowthRate(previousSessions, readingSessions));

        // Get engagement statistics from engagement service
        try {
            ApiResponse<EngagementServiceClient.ModerationStatistics> stats = 
                    engagementServiceClient.getModerationStatistics();
            if (stats != null && stats.isSuccess() && stats.getData() != null) {
                response.setTotalComments(stats.getData().totalComments);
                // Reviews count would need a separate endpoint
                response.setTotalReviews(0L);
            }
        } catch (Exception e) {
            log.warn("Could not fetch engagement statistics: {}", e.getMessage());
            response.setTotalComments(0L);
            response.setTotalReviews(0L);
        }

        response.setAverageRating(0.0); // Would need to aggregate from all novels

        return response;
    }

    /**
     * Get platform-wide statistics overview
     */
    public PlatformStatisticsResponseDTO getPlatformStatistics() {
        PlatformStatisticsResponseDTO response = new PlatformStatisticsResponseDTO();
        response.setTimestamp(new Date());

        // Calculate date ranges
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        
        cal.add(Calendar.DATE, -7);
        Date weekStart = cal.getTime();
        
        cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date monthStart = cal.getTime();
        
        Calendar yearCal = Calendar.getInstance();
        yearCal.add(Calendar.YEAR, -100); // Very old date for "all time"
        Date veryOldDate = yearCal.getTime();

        // Get activity statistics from local database
        response.setDailyActiveUsers(analyticsMapper.getDailyActiveUsers(today));
        response.setWeeklyActiveUsers(analyticsMapper.getWeeklyActiveUsers(weekStart, today));
        response.setMonthlyActiveUsers(analyticsMapper.getMonthlyActiveUsers(monthStart, today));
        response.setTotalReadingSessions(analyticsMapper.getTotalReadingSessions(veryOldDate, today));

        // Get total novels from content service
        try {
            ApiResponse<Long> novelCountResponse = contentServiceClient.getNovelCount();
            if (novelCountResponse != null && novelCountResponse.isSuccess() && novelCountResponse.getData() != null) {
                response.setTotalNovels(novelCountResponse.getData());
            } else {
                response.setTotalNovels(0L);
            }
        } catch (Exception e) {
            log.warn("Could not fetch novel count from content service: {}", e.getMessage());
            response.setTotalNovels(0L);
        }

        // Get engagement statistics
        try {
            ApiResponse<EngagementServiceClient.ModerationStatistics> stats = 
                    engagementServiceClient.getModerationStatistics();
            if (stats != null && stats.isSuccess() && stats.getData() != null) {
                response.setTotalComments(stats.getData().totalComments);
            } else {
                response.setTotalComments(0L);
            }
        } catch (Exception e) {
            log.warn("Could not fetch engagement statistics: {}", e.getMessage());
            response.setTotalComments(0L);
        }

        response.setTotalReviews(0L); // Would need dedicated endpoint

        return response;
    }

    /**
     * Get daily active users with hourly breakdown
     */
    public DailyActiveUsersResponseDTO getDailyActiveUsers(Date date) {
        if (date == null) {
            date = new Date();
        }

        DailyActiveUsersResponseDTO response = new DailyActiveUsersResponseDTO();
        response.setDate(date);
        response.setDau(analyticsMapper.getDailyActiveUsers(date));

        // Calculate weekly and monthly active users
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -7);
        Date weekStart = cal.getTime();
        response.setWau(analyticsMapper.getWeeklyActiveUsers(weekStart, date));

        cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        Date monthStart = cal.getTime();
        response.setMau(analyticsMapper.getMonthlyActiveUsers(monthStart, date));

        // Get hourly breakdown
        List<DailyActiveUsersResponseDTO.ActivityDataPoint> hourlyData = 
            analyticsMapper.getHourlyActiveUsers(date);
        response.setHourlyBreakdown(hourlyData);

        return response;
    }

    /**
     * Get top content statistics
     */
    public TopContentResponseDTO getTopContent(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // Default to top 10
        }

        TopContentResponseDTO response = new TopContentResponseDTO();
        response.setDate(new Date());

        // Get most read novels from local database
        List<Integer> topNovelIds = analyticsMapper.getMostReadNovelIds(limit);
        
        // Fetch novel details from content service
        List<TopContentResponseDTO.TopNovel> topNovels = Collections.emptyList();
        if (!topNovelIds.isEmpty()) {
            try {
                ApiResponse<List<NovelDetailResponseDTO>> novelsResponse = 
                    contentServiceClient.getNovelsBatch(topNovelIds);
                if (novelsResponse != null && novelsResponse.isSuccess() && novelsResponse.getData() != null) {
                    topNovels = novelsResponse.getData().stream()
                        .map(this::convertToTopNovel)
                        .toList();
                }
            } catch (Exception e) {
                log.error("Error fetching top novels from content service: {}", e.getMessage());
            }
        }
        response.setTopNovels(topNovels);

        // Get most active users
        // List<UUID> topUserIds = analyticsMapper.getMostActiveUserIds(limit);
        // TODO: Fetch user details from user service and convert to authors
        // For now, return empty lists
        response.setTopAuthors(Collections.emptyList());
        response.setTopCategories(Collections.emptyList());

        return response;
    }

    /**
     * Calculate growth rates for trend data points
     */
    private void calculateGrowthRates(List<AnalyticsTrendResponseDTO.TrendDataPoint> dataPoints) {
        for (int i = 0; i < dataPoints.size(); i++) {
            AnalyticsTrendResponseDTO.TrendDataPoint current = dataPoints.get(i);
            if (i > 0) {
                AnalyticsTrendResponseDTO.TrendDataPoint previous = dataPoints.get(i - 1);
                if (previous.getCount() > 0) {
                    double growthRate = ((current.getCount() - previous.getCount()) / (double) previous.getCount()) * 100;
                    current.setGrowthRate(growthRate);
                } else {
                    current.setGrowthRate(0.0);
                }
            } else {
                current.setGrowthRate(0.0);
            }
        }
    }

    /**
     * Calculate average growth rate
     */
    private Double calculateAverageGrowth(List<AnalyticsTrendResponseDTO.TrendDataPoint> dataPoints) {
        if (dataPoints.isEmpty()) {
            return 0.0;
        }

        double totalGrowth = dataPoints.stream()
            .filter(dp -> dp.getGrowthRate() != null)
            .mapToDouble(AnalyticsTrendResponseDTO.TrendDataPoint::getGrowthRate)
            .sum();

        long validPoints = dataPoints.stream()
            .filter(dp -> dp.getGrowthRate() != null)
            .count();

        return validPoints > 0 ? totalGrowth / validPoints : 0.0;
    }

    /**
     * Calculate growth rate between two periods
     */
    private Double calculateGrowthRate(Long previousValue, Long currentValue) {
        if (previousValue == null || previousValue == 0) {
            return currentValue != null && currentValue > 0 ? 100.0 : 0.0;
        }
        
        if (currentValue == null) {
            return -100.0;
        }
        
        return ((currentValue - previousValue) / (double) previousValue) * 100.0;
    }

    /**
     * Convert NovelDetailResponseDTO to TopNovel
     */
    private TopContentResponseDTO.TopNovel convertToTopNovel(NovelDetailResponseDTO novel) {
        TopContentResponseDTO.TopNovel topNovel = new TopContentResponseDTO.TopNovel();
        topNovel.setId(novel.getId());
        topNovel.setTitle(novel.getTitle());
        topNovel.setAuthorName(novel.getAuthorUsername());
        topNovel.setCategoryName(novel.getCategoryName());
        topNovel.setViewCount(novel.getViewCnt() != null ? novel.getViewCnt().longValue() : 0L);
        topNovel.setVoteCount(novel.getVoteCnt() != null ? novel.getVoteCnt().longValue() : 0L);
        topNovel.setRating(novel.getAvgRating() != null ? novel.getAvgRating().doubleValue() : 0.0);
        topNovel.setChapterCount(0); // Not available in NovelDetailResponseDTO
        topNovel.setWordCount(0L); // Not available in NovelDetailResponseDTO
        return topNovel;
    }
}

