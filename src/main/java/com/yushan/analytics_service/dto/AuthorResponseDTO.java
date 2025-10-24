package com.yushan.analytics_service.dto;

import lombok.Data;

/**
 * DTO for author ranking response
 */
@Data
public class AuthorResponseDTO {
    private String uuid;
    private String username;
    private String avatarUrl;
    private Integer novelNum;
    private Integer totalViewCnt;
    private Integer totalVoteCnt;
}

