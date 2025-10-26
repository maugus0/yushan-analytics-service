package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.GamificationServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.NovelDetailResponseDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import com.yushan.analytics_service.dto.UserProfileResponseDTO;
import com.yushan.analytics_service.util.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RankingUpdateService {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private GamificationServiceClient gamificationServiceClient;

    @Autowired
    private RedisUtil redisUtil;

    private static final String RANK_NOVEL_VIEW_ALL = "ranking:novel:view:all";
    private static final String RANK_NOVEL_VOTE_ALL = "ranking:novel:vote:all";
    private static final String RANK_NOVEL_VIEW_CATE_PREFIX = "ranking:novel:view:";
    private static final String RANK_NOVEL_VOTE_CATE_PREFIX = "ranking:novel:vote:";
    private static final String RANK_USER_EXP = "ranking:user:exp";
    private static final String RANK_AUTHOR_VOTE = "ranking:author:vote";
    private static final String RANK_AUTHOR_VIEW = "ranking:author:view";
    private static final String RANK_AUTHOR_NOVEL_NUM = "ranking:author:novelNum";

    @PostConstruct
    public void runUpdateOnStartup() {
        log.info("Initializing rankings on startup");
        try {
            updateAllRankings();
        } catch (Exception e) {
            log.warn("Failed to initialize rankings due to connection issue: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateAllRankings() {
        log.info("Starting scheduled ranking update");
        try {
            updateNovelRankings();
            updateUserRankings();
            updateAuthorRankings();
            log.info("Finished scheduled ranking update");
        } catch (Exception e) {
            log.error("Error during ranking update: {}", e.getMessage(), e);
        }
    }

    /**
     * Update novel rankings in Redis
     * Note: This requires pagination to fetch all novels since there's no batch endpoint
     */
    public void updateNovelRankings() {
        log.info("Updating novel rankings");
        try {
            // Fetch novels page by page
            List<NovelDetailResponseDTO> allNovels = new java.util.ArrayList<>();
            int page = 0;
            int size = 100;
            boolean hasMore = true;
            
            while (hasMore && page < 100) { // Limit to 100 pages (10000 novels) for safety
                ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> response = 
                        contentServiceClient.getNovels(page, size, "createTime", "desc");
                
                if (response == null || response.getCode() == null || !response.getCode().equals(200) || response.getData() == null) {
                    log.warn("Failed to fetch novels page {} for ranking", page);
                    break;
                }
                
                PageResponseDTO<NovelDetailResponseDTO> pageData = response.getData();
                if (pageData.getContent() == null || pageData.getContent().isEmpty()) {
                    break;
                }
                
                allNovels.addAll(pageData.getContent());
                hasMore = pageData.isHasNext();
                page++;
            }
            
            log.info("Fetched {} novels across {} pages", allNovels.size(), page);
            
            // Aggregate author statistics while we have all novels
            Map<UUID, AuthorStats> authorStatsMap = new java.util.HashMap<>();
            for (NovelDetailResponseDTO novel : allNovels) {
                if (novel.getAuthorId() != null) {
                    AuthorStats stats = authorStatsMap.computeIfAbsent(
                        novel.getAuthorId(), 
                        k -> new AuthorStats()
                    );
                    stats.novelCount++;
                    stats.totalViews += (novel.getViewCnt() != null ? novel.getViewCnt() : 0);
                    stats.totalVotes += (novel.getVoteCnt() != null ? novel.getVoteCnt() : 0);
                }
            }
            
            // Update author rankings
            updateAuthorRankingsFromStats(authorStatsMap);
            
            // Group novels by category
            Map<Integer, List<NovelDetailResponseDTO>> novelsByCategory = allNovels.stream()
                    .filter(novel -> novel.getCategoryId() != null)
                    .collect(Collectors.groupingBy(NovelDetailResponseDTO::getCategoryId));

            // Clear old ranking keys
            Set<String> oldKeys = redisUtil.keys("ranking:novel:*");
            if (oldKeys != null && !oldKeys.isEmpty()) {
                redisUtil.delete(oldKeys);
            }

            // Update all-novels rankings
            for (NovelDetailResponseDTO novel : allNovels) {
                if (novel.getViewCnt() != null) {
                    redisUtil.zAdd(RANK_NOVEL_VIEW_ALL, novel.getId().toString(), novel.getViewCnt());
                }
                if (novel.getVoteCnt() != null) {
                    redisUtil.zAdd(RANK_NOVEL_VOTE_ALL, novel.getId().toString(), novel.getVoteCnt());
                }
            }

            // Update category-specific rankings
            for (Map.Entry<Integer, List<NovelDetailResponseDTO>> entry : novelsByCategory.entrySet()) {
                String viewKey = RANK_NOVEL_VIEW_CATE_PREFIX + entry.getKey();
                String voteKey = RANK_NOVEL_VOTE_CATE_PREFIX + entry.getKey();
                
                for (NovelDetailResponseDTO novel : entry.getValue()) {
                    if (novel.getViewCnt() != null) {
                        redisUtil.zAdd(viewKey, novel.getId().toString(), novel.getViewCnt());
                    }
                    if (novel.getVoteCnt() != null) {
                        redisUtil.zAdd(voteKey, novel.getId().toString(), novel.getVoteCnt());
                    }
                }
            }
            
            log.info("Updated rankings for {} novels across {} categories", 
                    allNovels.size(), novelsByCategory.size());
        } catch (Exception e) {
            log.error("Error updating novel rankings: {}", e.getMessage(), e);
        }
    }

    /**
     * Update user rankings in Redis
     * Fetches users from user service and their gamification stats
     * Ranks by level first, then by currentExp
     */
    public void updateUserRankings() {
        log.info("Updating user rankings");
        try {
            // Step 1: Fetch all users from user service (paginated)
            List<String> allUserIds = new java.util.ArrayList<>();
            int page = 0;
            int size = 100;
            boolean hasMore = true;
            
            while (hasMore && page < 100) { // Limit to 100 pages for safety
                ApiResponse<PageResponseDTO<UserProfileResponseDTO>> userResponse = 
                        userServiceClient.getAllUsersForRanking(page, size, "createTime", "desc");
                
                if (userResponse == null || userResponse.getCode() == null || 
                        !userResponse.getCode().equals(200) || userResponse.getData() == null) {
                    log.warn("Failed to fetch users page {} for ranking", page);
                    break;
                }
                
                PageResponseDTO<UserProfileResponseDTO> pageData = userResponse.getData();
                if (pageData.getContent() == null || pageData.getContent().isEmpty()) {
                    break;
                }
                
                // Extract user IDs
                for (UserProfileResponseDTO user : pageData.getContent()) {
                    if (user.getUuid() != null) {
                        allUserIds.add(user.getUuid());
                    }
                }
                
                hasMore = pageData.isHasNext();
                page++;
            }
            
            log.info("Fetched {} users across {} pages", allUserIds.size(), page);
            
            if (allUserIds.isEmpty()) {
                log.warn("No users found for ranking");
                return;
            }
            
            // Step 2: Fetch gamification stats in batches
            List<GamificationServiceClient.GamificationStats> allStats = new java.util.ArrayList<>();
            int batchSize = 100;
            
            for (int i = 0; i < allUserIds.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, allUserIds.size());
                List<String> batch = allUserIds.subList(i, endIndex);
                
                try {
                    ApiResponse<List<GamificationServiceClient.GamificationStats>> statsResponse = 
                            gamificationServiceClient.getBatchUsersStats(batch);
                    
                    if (statsResponse != null && statsResponse.getCode() != null && 
                            statsResponse.getCode().equals(200) && statsResponse.getData() != null) {
                        allStats.addAll(statsResponse.getData());
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch gamification stats for batch starting at index {}: {}", i, e.getMessage());
                }
            }
            
            log.info("Fetched gamification stats for {} users", allStats.size());
            
            // Clear old ranking key
            redisUtil.delete(RANK_USER_EXP);
            
            // Step 3: Update user rankings
            // Score = level * 1000000 + currentExp (to rank by level first, then exp)
            for (GamificationServiceClient.GamificationStats stats : allStats) {
                if (stats.userId != null && stats.level != null && stats.currentExp != null) {
                    double score = (stats.level * 1000000.0) + stats.currentExp;
                    redisUtil.zAdd(RANK_USER_EXP, stats.userId, score);
                }
            }
            
            log.info("Updated rankings for {} users", allStats.size());
        } catch (Exception e) {
            log.error("Error updating user rankings: {}", e.getMessage(), e);
        }
    }

    /**
     * Update author rankings from aggregated novel statistics
     */
    private void updateAuthorRankingsFromStats(Map<UUID, AuthorStats> authorStatsMap) {
        log.info("Updating author rankings from novel statistics");
        
        // Clear old ranking keys
        redisUtil.delete(List.of(RANK_AUTHOR_VOTE, RANK_AUTHOR_VIEW, RANK_AUTHOR_NOVEL_NUM));
        
        // Update author rankings
        for (Map.Entry<UUID, AuthorStats> entry : authorStatsMap.entrySet()) {
            String authorUuid = entry.getKey().toString();
            AuthorStats stats = entry.getValue();
            
            redisUtil.zAdd(RANK_AUTHOR_VOTE, authorUuid, stats.totalVotes);
            redisUtil.zAdd(RANK_AUTHOR_VIEW, authorUuid, stats.totalViews);
            redisUtil.zAdd(RANK_AUTHOR_NOVEL_NUM, authorUuid, stats.novelCount);
        }
        
        log.info("Updated rankings for {} authors", authorStatsMap.size());
    }

    /**
     * Update author rankings - kept for manual invocation if needed
     */
    public void updateAuthorRankings() {
        log.info("Author rankings are updated as part of novel rankings update");
    }
    
    // Helper class to aggregate author statistics
    private static class AuthorStats {
        int novelCount = 0;
        long totalViews = 0;
        long totalVotes = 0;
    }
}

