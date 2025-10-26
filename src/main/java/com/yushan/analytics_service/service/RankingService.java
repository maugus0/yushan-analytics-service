package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.GamificationServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.AuthorResponseDTO;
import com.yushan.analytics_service.dto.NovelDetailResponseDTO;
import com.yushan.analytics_service.dto.NovelRankDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import com.yushan.analytics_service.dto.UserProfileResponseDTO;
import com.yushan.analytics_service.exception.ResourceNotFoundException;
import com.yushan.analytics_service.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RankingService {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private GamificationServiceClient gamificationServiceClient;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * Get novel ranking with pagination - fetches all novels and sorts by votes
     */
    public PageResponseDTO<NovelDetailResponseDTO> rankNovel(
            Integer page, Integer size, String sortType, Integer categoryId, String timeRange) {
        
        log.info("Fetching novel ranking: page={}, size={}, sortType={}, categoryId={}", 
                page, size, sortType, categoryId);
        
        try {
            // Fetch ALL novels from content service (paginate through all pages)
            List<NovelDetailResponseDTO> allNovels = new ArrayList<>();
            int fetchPage = 0;
            int fetchSize = 100;
            boolean hasMore = true;
            
            while (hasMore && fetchPage < 50) { // Limit to 50 pages for safety
                ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> response = 
                        contentServiceClient.getNovels(fetchPage, fetchSize, "createTime", "desc");
                
                if (response == null || response.getCode() == null || !response.getCode().equals(200) || response.getData() == null) {
                    log.warn("Failed to fetch novels page {}", fetchPage);
                    break;
                }
                
                PageResponseDTO<NovelDetailResponseDTO> pageData = response.getData();
                if (pageData.getContent() == null || pageData.getContent().isEmpty()) {
                    break;
                }
                
                allNovels.addAll(pageData.getContent());
                hasMore = pageData.isHasNext();
                fetchPage++;
            }
            
            log.info("Fetched {} total novels", allNovels.size());
            
            // Filter by category if specified
            if (categoryId != null) {
                allNovels = allNovels.stream()
                        .filter(novel -> categoryId.equals(novel.getCategoryId()))
                        .collect(Collectors.toList());
            }
            
            // Sort by votes (voteCnt)
            allNovels.sort(Comparator.comparing(NovelDetailResponseDTO::getVoteCnt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            
            // Paginate the results
            int totalElements = allNovels.size();
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            
            if (start >= totalElements) {
                return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
            }
            
            List<NovelDetailResponseDTO> paginatedNovels = allNovels.subList(start, end);
            
            return PageResponseDTO.of(paginatedNovels, totalElements, page, size);
            
        } catch (Exception e) {
            log.error("Error fetching novel ranking: {}", e.getMessage(), e);
            return PageResponseDTO.of(Collections.emptyList(), 0, page, size);
        }
    }

    /**
     * Get the best rank for a novel across all ranking types
     */
    public NovelRankDTO getBestNovelRank(Integer novelId) {
        // Verify novel exists
        try {
            ApiResponse<NovelDetailResponseDTO> response = contentServiceClient.getNovelById(novelId);
            if (response == null || response.getCode() == null || !response.getCode().equals(200) || response.getData() == null) {
                throw new ResourceNotFoundException("Novel not found, id: " + novelId);
            }
            NovelDetailResponseDTO novel = response.getData();

            NovelRankDTO bestRank = null;

            // Check all-time views ranking
            NovelRankDTO allViewsRank = getNovelRank(novelId, "view", null);
            if (allViewsRank != null) {
                bestRank = new NovelRankDTO(novelId, allViewsRank.getRank(), 
                        allViewsRank.getScore(), "All-Time Views Ranking");
            }

            // Check all-time votes ranking
            NovelRankDTO allVotesRank = getNovelRank(novelId, "vote", null);
            if (allVotesRank != null) {
                if (bestRank == null || allVotesRank.getRank() < bestRank.getRank()) {
                    bestRank = new NovelRankDTO(novelId, allVotesRank.getRank(), 
                            allVotesRank.getScore(), "All-Time Votes Ranking");
                }
            }

            // Check category-specific rankings
            Integer categoryId = novel.getCategoryId();
            if (categoryId != null && novel.getCategoryName() != null) {
                NovelRankDTO categoryViewRank = getNovelRank(novelId, "view", categoryId);
                if (categoryViewRank != null) {
                    if (bestRank == null || categoryViewRank.getRank() < bestRank.getRank()) {
                        bestRank = new NovelRankDTO(novelId, categoryViewRank.getRank(), 
                                categoryViewRank.getScore(), novel.getCategoryName() + " Views Ranking");
                    }
                }

                NovelRankDTO categoryVoteRank = getNovelRank(novelId, "vote", categoryId);
                if (categoryVoteRank != null) {
                    if (bestRank == null || categoryVoteRank.getRank() < bestRank.getRank()) {
                        bestRank = new NovelRankDTO(novelId, categoryVoteRank.getRank(), 
                                categoryVoteRank.getScore(), novel.getCategoryName() + " Votes Ranking");
                    }
                }
            }

            return bestRank;
        } catch (Exception e) {
            log.error("Error getting best novel rank for novelId {}: {}", novelId, e.getMessage());
            throw new ResourceNotFoundException("Novel not found, id: " + novelId);
        }
    }

    /**
     * Get user ranking with pagination - uses Redis data populated by RankingUpdateService
     */
    public PageResponseDTO<UserProfileResponseDTO> rankUser(Integer page, Integer size, String timeRange) {
        log.info("Fetching user ranking from Redis: page={}, size={}", page, size);
        
        String redisKey = "ranking:user:exp";
        long offset = (long) page * size;
        
        // Get total count from Redis
        Long totalInRedis = redisUtil.zCard(redisKey);
        long totalElements = totalInRedis != null ? totalInRedis : 0;
        
        if (totalElements == 0) {
            log.warn("No user ranking data in Redis. Please trigger ranking update.");
            return PageResponseDTO.of(Collections.emptyList(), 0, page, size);
        }
        
        if (offset >= totalElements) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }
        
        long end = offset + size - 1;
        Set<String> userUuidsStr = redisUtil.zReverseRange(redisKey, offset, end);
        
        if (userUuidsStr == null || userUuidsStr.isEmpty()) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }
        
        List<String> orderedUserUuids = new ArrayList<>(userUuidsStr);
        
        // Fetch user profiles from user service
        List<UUID> uuidList = orderedUserUuids.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        
        List<UserProfileResponseDTO> users;
        try {
            ApiResponse<List<UserProfileResponseDTO>> response = userServiceClient.getUsersBatch(uuidList);
            users = (response != null && response.getCode() != null && response.getCode().equals(200) && response.getData() != null) 
                    ? response.getData() 
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching users from user service: {}", e.getMessage());
            users = Collections.emptyList();
        }
        
        // Fetch gamification stats for these users
        Map<String, GamificationServiceClient.GamificationStats> statsMap = new HashMap<>();
        try {
            ApiResponse<List<GamificationServiceClient.GamificationStats>> statsResponse = 
                    gamificationServiceClient.getBatchUsersStats(orderedUserUuids);
            
            if (statsResponse != null && statsResponse.getCode() != null && 
                    statsResponse.getCode().equals(200) && statsResponse.getData() != null) {
                statsMap = statsResponse.getData().stream()
                        .collect(Collectors.toMap(
                                stats -> stats.userId,
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));
                log.info("Fetched gamification stats for {} users", statsMap.size());
            } else {
                log.warn("Failed to fetch gamification stats: response code = {}", 
                        statsResponse != null ? statsResponse.getCode() : "null");
            }
        } catch (Exception e) {
            log.error("Error fetching gamification stats: {}", e.getMessage(), e);
        }
        
        // Make final for lambda
        final Map<String, GamificationServiceClient.GamificationStats> finalStatsMap = statsMap;
        
        // Create map for quick lookup
        Map<String, UserProfileResponseDTO> userMap = users.stream()
                .collect(Collectors.toMap(UserProfileResponseDTO::getUuid, Function.identity()));
        
        // Build result list in order, enriched with level and exp
        List<UserProfileResponseDTO> sortedUsers = orderedUserUuids.stream()
                .map(uuid -> {
                    UserProfileResponseDTO user = userMap.get(uuid);
                    if (user != null) {
                        // Enrich with gamification stats
                        GamificationServiceClient.GamificationStats stats = finalStatsMap.get(uuid);
                        if (stats != null) {
                            user.setLevel(stats.level != null ? stats.level : 0);
                            user.setCurrentExp(stats.currentExp != null ? stats.currentExp : 0);
                        } else {
                            user.setLevel(0);
                            user.setCurrentExp(0);
                        }
                    }
                    return user;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        return PageResponseDTO.of(sortedUsers, totalElements, page, size);
    }

    /**
     * Get author ranking with pagination - aggregates novels by author and sorts by total votes
     */
    public PageResponseDTO<AuthorResponseDTO> rankAuthor(Integer page, Integer size, String sortType, String timeRange) {
        log.info("Fetching author ranking: page={}, size={}, sortType={}", page, size, sortType);
        
        try {
            // Fetch ALL novels from content service
            List<NovelDetailResponseDTO> allNovels = new ArrayList<>();
            int fetchPage = 0;
            int fetchSize = 100;
            boolean hasMore = true;
            
            while (hasMore && fetchPage < 50) {
                ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> response = 
                        contentServiceClient.getNovels(fetchPage, fetchSize, "createTime", "desc");
                
                if (response == null || response.getCode() == null || !response.getCode().equals(200) || response.getData() == null) {
                    log.warn("Failed to fetch novels page {}", fetchPage);
                    break;
                }
                
                PageResponseDTO<NovelDetailResponseDTO> pageData = response.getData();
                if (pageData.getContent() == null || pageData.getContent().isEmpty()) {
                    break;
                }
                
                allNovels.addAll(pageData.getContent());
                hasMore = pageData.isHasNext();
                fetchPage++;
            }
            
            log.info("Fetched {} total novels for author ranking", allNovels.size());
            
            // Aggregate by author
            Map<String, AuthorStats> authorStatsMap = new HashMap<>();
            for (NovelDetailResponseDTO novel : allNovels) {
                if (novel.getAuthorId() == null) {
                    continue;
                }
                
                String authorId = novel.getAuthorId().toString();
                AuthorStats stats = authorStatsMap.computeIfAbsent(authorId, k -> new AuthorStats());
                stats.authorId = authorId;
                stats.authorName = novel.getAuthorUsername();
                stats.totalVotes += (novel.getVoteCnt() != null ? novel.getVoteCnt() : 0);
                stats.totalViews += (novel.getViewCnt() != null ? novel.getViewCnt() : 0);
                stats.novelCount++;
            }
            
            // Convert to list and sort by votes
            List<AuthorResponseDTO> authors = authorStatsMap.values().stream()
                    .map(stats -> {
                        AuthorResponseDTO dto = new AuthorResponseDTO();
                        dto.setUuid(stats.authorId);
                        dto.setUsername(stats.authorName);
                        dto.setTotalVoteCnt(stats.totalVotes);
                        dto.setTotalViewCnt(stats.totalViews);
                        dto.setNovelNum(stats.novelCount);
                        return dto;
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(AuthorResponseDTO::getTotalVoteCnt).reversed())
                    .collect(Collectors.toList());
            
            // Paginate
            int totalElements = authors.size();
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            
            if (start >= totalElements) {
                return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
            }
            
            List<AuthorResponseDTO> paginatedAuthors = authors.subList(start, end);
            
            return PageResponseDTO.of(paginatedAuthors, totalElements, page, size);
            
        } catch (Exception e) {
            log.error("Error fetching author ranking: {}", e.getMessage(), e);
            return PageResponseDTO.of(Collections.emptyList(), 0, page, size);
        }
    }
    
    // Helper class for aggregating author stats
    private static class AuthorStats {
        String authorId;
        String authorName;
        int totalVotes;
        int totalViews;
        int novelCount;
    }

    /**
     * Get rank for a specific novel in a specific ranking
     */
    private NovelRankDTO getNovelRank(Integer novelId, String sortType, Integer categoryId) {
        String redisKey = buildNovelRedisKey(sortType, categoryId);

        Long rank = redisUtil.zReverseRank(redisKey, novelId.toString());

        if (rank == null) {
            return null; // Novel not found in ranking
        }

        Double score = redisUtil.zScore(redisKey, novelId.toString());
        return new NovelRankDTO(novelId, rank + 1, score, redisKey);
    }

    /**
     * Build Redis key for novel ranking
     */
    private String buildNovelRedisKey(String sortType, Integer categoryId) {
        String baseKey = "ranking:novel:" + ("view".equalsIgnoreCase(sortType) ? "view" : "vote");
        return (categoryId == null || categoryId <= 0) ? baseKey + ":all" : baseKey + ":" + categoryId;
    }

    /**
     * Generic method to get paginated ranking
     */
    private <T> PageResponseDTO<T> getPaginatedRanking(int page, int size, String redisKey,
                                                       Function<List<UUID>, List<T>> fetcher,
                                                       Function<T, UUID> uuidExtractor) {
        long offset = (long) page * size;

        Long totalInRedis = redisUtil.zCard(redisKey);
        long totalElements = totalInRedis != null ? Math.min(totalInRedis, 100) : 0;

        if (offset >= totalElements) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }

        Set<String> uuidsStr = redisUtil.zReverseRange(redisKey, offset, offset + size - 1);
        if (uuidsStr == null || uuidsStr.isEmpty()) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }

        List<UUID> orderedUuids = uuidsStr.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        List<T> dtoList = fetcher.apply(orderedUuids);

        Map<UUID, T> dtoMap = dtoList.stream()
                .collect(Collectors.toMap(uuidExtractor, Function.identity()));

        List<T> sortedDtoList = orderedUuids.stream()
                .map(dtoMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResponseDTO.of(sortedDtoList, totalElements, page, size);
    }

}

