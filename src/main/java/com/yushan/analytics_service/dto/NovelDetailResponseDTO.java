package com.yushan.analytics_service.dto;

import lombok.Data;

import java.util.UUID;

/**
 * DTO for detailed novel response used in rankings
 */
@Data
public class NovelDetailResponseDTO {
    private Integer id;
    private UUID uuid;
    private String title;
    private UUID authorId;
    private String authorUsername;
    private Float avgRating;
    private Integer viewCnt;
    private Integer voteCnt;
    private String coverImgUrl;
    private Integer categoryId;
    private String categoryName;
    private String synopsis;
    private Boolean isCompleted;
}

