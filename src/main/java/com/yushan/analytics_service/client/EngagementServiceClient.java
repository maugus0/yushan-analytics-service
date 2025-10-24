package com.yushan.analytics_service.client;

import com.yushan.analytics_service.config.FeignAuthConfig;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.PageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "engagement-service",
        url = "${services.engagement.url:http://yushan-engagement-service:8084}",
        configuration = FeignAuthConfig.class
)
public interface EngagementServiceClient {

    @GetMapping("/api/v1/reviews/novel/{novelId}/rating-stats")
    ApiResponse<RatingStats> getNovelRatingStats(@PathVariable("novelId") Integer novelId);

    @GetMapping("/api/v1/reviews/novel/{novelId}")
    ApiResponse<PageResponseDTO<Review>> getNovelReviews(
            @PathVariable("novelId") Integer novelId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    @GetMapping("/api/v1/comments/chapter/{chapterId}/statistics")
    ApiResponse<CommentStatistics> getChapterCommentStats(@PathVariable("chapterId") Integer chapterId);

    @GetMapping("/api/v1/comments/admin/statistics")
    ApiResponse<ModerationStatistics> getModerationStatistics();

    // Nested classes for response types
    class RatingStats {
        public Double averageRating;
        public Integer totalReviews;
        public Integer rating1Count;
        public Integer rating2Count;
        public Integer rating3Count;
        public Integer rating4Count;
        public Integer rating5Count;
    }

    class Review {
        public Integer id;
        public Integer novelId;
        public String userId;
        public Integer rating;
        public String content;
        public Integer likeCount;
        public String createTime;
    }

    class CommentStatistics {
        public Integer totalComments;
        public Integer spoilerComments;
        public Integer recentComments;
    }

    class ModerationStatistics {
        public Long totalComments;
        public Long pendingReports;
        public Long resolvedReports;
        public Long flaggedComments;
    }
}
