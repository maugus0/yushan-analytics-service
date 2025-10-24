package com.yushan.analytics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PlatformStatisticsResponseDTO {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date timestamp;
    
    // Content statistics (from content service)
    private Long totalNovels;
    
    // Activity statistics (from local history)
    private Long dailyActiveUsers;
    private Long weeklyActiveUsers;
    private Long monthlyActiveUsers;
    private Long totalReadingSessions;
    
    // Engagement statistics (from engagement service - if available)
    private Long totalComments;
    private Long totalReviews;
    
    public Date getTimestamp() {
        return timestamp != null ? new Date(timestamp.getTime()) : null;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp != null ? new Date(timestamp.getTime()) : null;
    }
}

