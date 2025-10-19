package com.yushan.analytics_service.dto;

import lombok.Data;

@Data
public class NovelDTO {
    private Integer id;
    private String title;
    private String synopsis;
    private String coverImgUrl;
    private Integer categoryId;
    private Float avgRating;
    private Integer chapterCnt;
    private Integer status; // 2 = PUBLISHED
}
