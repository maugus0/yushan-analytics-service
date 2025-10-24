package com.yushan.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for novel rank information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NovelRankDTO {
    private Integer novelId;
    private Long rank;
    private Double score;
    private String rankingType;
}

