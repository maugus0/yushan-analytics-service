package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
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

import java.util.Collections;
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
    private RedisUtil redisUtil;

    /**
     * Get novel ranking with pagination
     */
    public PageResponseDTO<NovelDetailResponseDTO> rankNovel(
            Integer page, Integer size, String sortType, Integer categoryId, String timeRange) {

        String redisKey = buildNovelRedisKey(sortType, categoryId);
        long offset = (long) page * size;

        Long totalInRedis = redisUtil.zCard(redisKey);
        long totalElements = totalInRedis != null ? Math.min(totalInRedis, 100) : 0;

        if (offset >= totalElements) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }

        long end = offset + size - 1;
        Set<String> novelIdsStr = redisUtil.zReverseRange(redisKey, offset, end);

        if (novelIdsStr == null || novelIdsStr.isEmpty()) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }

        List<Integer> orderedNovelIds = novelIdsStr.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // Fetch novels from content service
        List<NovelDetailResponseDTO> novelsFromService;
        try {
            ApiResponse<List<NovelDetailResponseDTO>> response = contentServiceClient.getNovelsBatch(orderedNovelIds);
            novelsFromService = (response != null && response.getCode() != null && response.getCode().equals(200) && response.getData() != null) 
                    ? response.getData() 
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching novels from content service: {}", e.getMessage());
            novelsFromService = Collections.emptyList();
        }

        Map<Integer, NovelDetailResponseDTO> novelMap = novelsFromService.stream()
                .collect(Collectors.toMap(NovelDetailResponseDTO::getId, Function.identity()));

        List<NovelDetailResponseDTO> sortedNovels = orderedNovelIds.stream()
                .map(novelMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Category names are already in the NovelDetailResponseDTO from content service
        // No need to fetch separately

        return PageResponseDTO.of(sortedNovels, totalElements, page, size);
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
     * Get user ranking with pagination
     */
    public PageResponseDTO<UserProfileResponseDTO> rankUser(Integer page, Integer size, String timeRange) {
        String redisKey = "ranking:user:exp";
        return getPaginatedRanking(page, size, redisKey,
                uuids -> {
                    try {
                        ApiResponse<List<UserProfileResponseDTO>> response = userServiceClient.getUsersBatch(uuids);
                        return (response != null && response.getCode() != null && response.getCode().equals(200) && response.getData() != null) 
                                ? response.getData() 
                                : Collections.emptyList();
                    } catch (Exception e) {
                        log.error("Error fetching users from user service: {}", e.getMessage());
                        return Collections.emptyList();
                    }
                },
                dto -> UUID.fromString(dto.getUuid())
        );
    }

    /**
     * Get author ranking with pagination
     */
    public PageResponseDTO<AuthorResponseDTO> rankAuthor(Integer page, Integer size, String sortType, String timeRange) {
        String redisKey = "ranking:author:" + sortType;
        long offset = (long) page * size;

        Long totalInRedis = redisUtil.zCard(redisKey);
        long totalElements = totalInRedis != null ? Math.min(totalInRedis, 100) : 0;

        if (offset >= totalElements) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }

        Set<String> authorUuidsStr = redisUtil.zReverseRange(redisKey, offset, offset + size - 1);
        if (authorUuidsStr == null || authorUuidsStr.isEmpty()) {
            return PageResponseDTO.of(Collections.emptyList(), totalElements, page, size);
        }

        List<UUID> orderedAuthorUuids = authorUuidsStr.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        // Fetch user profiles for these authors
        List<UserProfileResponseDTO> userProfiles;
        try {
            ApiResponse<List<UserProfileResponseDTO>> response = userServiceClient.getUsersBatch(orderedAuthorUuids);
            userProfiles = (response != null && response.getCode() != null && response.getCode().equals(200) && response.getData() != null) 
                    ? response.getData() 
                    : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching user profiles for authors: {}", e.getMessage());
            userProfiles = Collections.emptyList();
        }

        // Create map for quick lookup
        Map<UUID, UserProfileResponseDTO> profileMap = userProfiles.stream()
                .filter(p -> p.getUuid() != null)
                .collect(Collectors.toMap(p -> UUID.fromString(p.getUuid()), Function.identity()));

        // Combine user profiles with author statistics from Redis
        List<AuthorResponseDTO> authorDTOs = orderedAuthorUuids.stream()
                .map(uuid -> {
                    UserProfileResponseDTO profile = profileMap.get(uuid);
                    if (profile == null) {
                        return null;
                    }
                    
                    AuthorResponseDTO dto = new AuthorResponseDTO();
                    dto.setUuid(uuid.toString());
                    dto.setUsername(profile.getUsername());
                    dto.setAvatarUrl(profile.getAvatarUrl());
                    
                    // Get statistics from Redis
                    Double voteCount = redisUtil.zScore("ranking:author:vote", uuid.toString());
                    Double viewCount = redisUtil.zScore("ranking:author:view", uuid.toString());
                    Double novelCount = redisUtil.zScore("ranking:author:novelNum", uuid.toString());
                    
                    dto.setTotalVoteCnt(voteCount != null ? voteCount.intValue() : 0);
                    dto.setTotalViewCnt(viewCount != null ? viewCount.intValue() : 0);
                    dto.setNovelNum(novelCount != null ? novelCount.intValue() : 0);
                    
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResponseDTO.of(authorDTOs, totalElements, page, size);
    }

    /**
     * Get rank for a specific novel in a specific ranking
     */
    private NovelRankDTO getNovelRank(Integer novelId, String sortType, Integer categoryId) {
        String redisKey = buildNovelRedisKey(sortType, categoryId);

        Long rank = redisUtil.zReverseRank(redisKey, novelId.toString());

        if (rank == null || rank >= 100) {
            return null;
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

