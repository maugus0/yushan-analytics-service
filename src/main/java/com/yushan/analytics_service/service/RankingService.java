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
     * Get user ranking with pagination - fetches users directly from user service
     * and hardcodes level/exp for demonstration
     */
    public PageResponseDTO<UserProfileResponseDTO> rankUser(Integer page, Integer size, String timeRange) {
        log.info("Fetching user ranking: page={}, size={}", page, size);
        
        try {
            // Fetch users from user service
            ApiResponse<PageResponseDTO<UserProfileResponseDTO>> userResponse = 
                    userServiceClient.getAllUsersForRanking(page, size, "createTime", "desc");
            
            if (userResponse == null || userResponse.getCode() == null || 
                    !userResponse.getCode().equals(200) || userResponse.getData() == null) {
                log.warn("Failed to fetch users for ranking");
                return PageResponseDTO.of(Collections.emptyList(), 0, page, size);
            }
            
            PageResponseDTO<UserProfileResponseDTO> pageData = userResponse.getData();
            List<UserProfileResponseDTO> users = pageData.getContent();
            
            if (users == null || users.isEmpty()) {
                return PageResponseDTO.of(Collections.emptyList(), 0, page, size);
            }
            
            // Calculate the absolute position for hardcoding level/exp
            int startPosition = page * size;
            
            // Hardcode level and exp for users based on their position
            for (int i = 0; i < users.size(); i++) {
                UserProfileResponseDTO user = users.get(i);
                int absolutePosition = startPosition + i;
                
                // Hardcode levels and exp for users in decreasing order
                if (absolutePosition < 3) {
                    // First 3 users: Level 9, 3000+ exp
                    user.setLevel(9);
                    user.setCurrentExp(3000 + (2 - absolutePosition) * 100);
                } else if (absolutePosition < 5) {
                    // Next 2 users: Level 8, 2000+ exp
                    user.setLevel(8);
                    user.setCurrentExp(2000 + (4 - absolutePosition) * 100);
                } else if (absolutePosition < 8) {
                    // Next 3 users: Level 7, 1500+ exp
                    user.setLevel(7);
                    user.setCurrentExp(1500 + (7 - absolutePosition) * 100);
                } else if (absolutePosition < 10) {
                    // Next 2 users: Level 6, 1000+ exp
                    user.setLevel(6);
                    user.setCurrentExp(1000 + (9 - absolutePosition) * 100);
                } else if (absolutePosition < 15) {
                    // Positions 10-14: Level 5, 800+ exp
                    user.setLevel(5);
                    user.setCurrentExp(800 + (14 - absolutePosition) * 20);
                } else if (absolutePosition < 20) {
                    // Positions 15-19: Level 4, 600+ exp
                    user.setLevel(4);
                    user.setCurrentExp(600 + (19 - absolutePosition) * 20);
                } else if (absolutePosition < 30) {
                    // Positions 20-29: Level 3, 400+ exp
                    user.setLevel(3);
                    user.setCurrentExp(400 + (29 - absolutePosition) * 10);
                } else if (absolutePosition < 50) {
                    // Positions 30-49: Level 2, 200+ exp
                    user.setLevel(2);
                    user.setCurrentExp(200 + (49 - absolutePosition) * 5);
                } else {
                    // Positions 50+: Level 1, decreasing exp
                    user.setLevel(1);
                    user.setCurrentExp(Math.max(50, 200 - (absolutePosition - 50) * 2));
                }
            }
            
            return PageResponseDTO.of(
                    users, 
                    pageData.getTotalElements(), 
                    page, 
                    size
            );
            
        } catch (Exception e) {
            log.error("Error fetching user ranking: {}", e.getMessage(), e);
            return PageResponseDTO.of(Collections.emptyList(), 0, page, size);
        }
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

