package com.yushan.analytics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DailyActiveUsersResponseDTO {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @EqualsAndHashCode.Include
    private Date date;
    
    @EqualsAndHashCode.Include
    private Long dau;  // Daily Active Users
    @EqualsAndHashCode.Include
    private Long wau;  // Weekly Active Users
    @EqualsAndHashCode.Include
    private Long mau;  // Monthly Active Users
    
    // Excluded from equals/hashCode to avoid redundant null checks
    private List<ActivityDataPoint> hourlyBreakdown;
    
    // Defensive copying for date
    public Date getDate() {
        return date != null ? new Date(date.getTime()) : null;
    }
    
    public void setDate(Date date) {
        this.date = date != null ? new Date(date.getTime()) : null;
    }
    
    // Defensive copying for hourlyBreakdown
    public List<ActivityDataPoint> getHourlyBreakdown() {
        return hourlyBreakdown != null ? new ArrayList<>(hourlyBreakdown) : new ArrayList<>();
    }
    
    public void setHourlyBreakdown(List<ActivityDataPoint> hourlyBreakdown) {
        this.hourlyBreakdown = hourlyBreakdown != null ? new ArrayList<>(hourlyBreakdown) : new ArrayList<>();
    }
    
    @Data
    public static class ActivityDataPoint {
        private Integer hour;
        private Long activeUsers;
        private Long newUsers;
        private Long readingSessions;
    }
}

