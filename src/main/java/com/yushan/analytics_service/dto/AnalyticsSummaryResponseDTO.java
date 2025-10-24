package com.yushan.analytics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AnalyticsSummaryResponseDTO {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;
    
    private String period;
    
    // User metrics (from local history)
    private Long activeUsers;
    private Double userGrowthRate;
    
    // Novel metrics (from content service)
    private Long uniqueNovelsRead;
    private Double novelGrowthRate;
    
    // Reading activity metrics (from local history)
    private Long totalReadingSessions;
    private Double sessionGrowthRate;
    
    // Engagement metrics (aggregated)
    private Long totalReviews;
    private Long totalComments;
    private Double averageRating;
    
    public AnalyticsSummaryResponseDTO() {}
    
    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
    }
    
    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
    }
}

