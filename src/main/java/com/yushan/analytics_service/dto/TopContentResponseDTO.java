package com.yushan.analytics_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TopContentResponseDTO {
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @EqualsAndHashCode.Include
    private Date date;
    
    // Excluded from equals/hashCode to avoid redundant null checks
    private List<TopNovel> topNovels;
    private List<TopAuthor> topAuthors;
    private List<TopCategory> topCategories;
    
    // Defensive copying for date
    public Date getDate() {
        return date != null ? new Date(date.getTime()) : null;
    }
    
    public void setDate(Date date) {
        this.date = date != null ? new Date(date.getTime()) : null;
    }
    
    // Defensive copying for lists
    public List<TopNovel> getTopNovels() {
        return topNovels != null ? new ArrayList<>(topNovels) : new ArrayList<>();
    }
    
    public void setTopNovels(List<TopNovel> topNovels) {
        this.topNovels = topNovels != null ? new ArrayList<>(topNovels) : new ArrayList<>();
    }
    
    public List<TopAuthor> getTopAuthors() {
        return topAuthors != null ? new ArrayList<>(topAuthors) : new ArrayList<>();
    }
    
    public void setTopAuthors(List<TopAuthor> topAuthors) {
        this.topAuthors = topAuthors != null ? new ArrayList<>(topAuthors) : new ArrayList<>();
    }
    
    public List<TopCategory> getTopCategories() {
        return topCategories != null ? new ArrayList<>(topCategories) : new ArrayList<>();
    }
    
    public void setTopCategories(List<TopCategory> topCategories) {
        this.topCategories = topCategories != null ? new ArrayList<>(topCategories) : new ArrayList<>();
    }
    
    @Data
    public static class TopNovel {
        private Integer id;
        private String title;
        private String authorName;
        private String categoryName;
        private Long viewCount;
        private Long voteCount;
        private Double rating;
        private Integer chapterCount;
        private Long wordCount;
    }
    
    @Data
    public static class TopAuthor {
        private UUID authorId;
        private String authorName;
        private Integer novelCount;
        private Long totalViews;
        private Long totalVotes;
        private Double averageRating;
        private Long totalWords;
    }
    
    @Data
    public static class TopCategory {
        private Integer categoryId;
        private String categoryName;
        private Integer novelCount;
        private Long totalViews;
        private Long totalVotes;
        private Double averageRating;
    }
}

