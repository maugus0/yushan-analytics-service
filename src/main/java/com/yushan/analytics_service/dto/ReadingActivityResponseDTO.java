package com.yushan.analytics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReadingActivityResponseDTO {
    
    @EqualsAndHashCode.Include
    private String period;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @EqualsAndHashCode.Include
    private Date startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @EqualsAndHashCode.Include
    private Date endDate;
    
    // Excluded from equals/hashCode to avoid redundant null checks
    private List<ActivityDataPoint> dataPoints;
    
    @EqualsAndHashCode.Include
    private Long totalActivity;
    
    @EqualsAndHashCode.Include
    private Double averageDailyActivity;
    
    @EqualsAndHashCode.Include
    private Long peakActivity;
    
    @EqualsAndHashCode.Include
    private String peakDate;
    
    // Defensive copying for dataPoints
    public List<ActivityDataPoint> getDataPoints() {
        return dataPoints != null ? new ArrayList<>(dataPoints) : new ArrayList<>();
    }
    
    public void setDataPoints(List<ActivityDataPoint> dataPoints) {
        this.dataPoints = dataPoints != null ? new ArrayList<>(dataPoints) : new ArrayList<>();
    }
    
    // Defensive copying for dates
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
    
    @Data
    public static class ActivityDataPoint {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private Date date;
        
        private String periodLabel;
        
        private Long views;
        
        private Long chaptersRead;
        
        private Long comments;
        
        private Long reviews;
        
        private Long votes;
        
        private Long totalActivity;
        
        public Date getDate() {
            return date != null ? new Date(date.getTime()) : null;
        }
        
        public void setDate(Date date) {
            this.date = date != null ? new Date(date.getTime()) : null;
        }
        
        // Calculate total activity
        public Long getTotalActivity() {
            if (totalActivity != null) {
                return totalActivity;
            }
            long total = 0;
            if (views != null) total += views;
            if (chaptersRead != null) total += chaptersRead;
            if (comments != null) total += comments;
            if (reviews != null) total += reviews;
            if (votes != null) total += votes;
            return total;
        }
    }
}

