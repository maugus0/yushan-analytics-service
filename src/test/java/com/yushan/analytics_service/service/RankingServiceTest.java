package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.GamificationServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dto.*;
import com.yushan.analytics_service.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private ContentServiceClient contentServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private GamificationServiceClient gamificationServiceClient;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private RankingService rankingService;

    private NovelDetailResponseDTO novelDTO;
    private UserProfileResponseDTO userDTO;

    @BeforeEach
    void setUp() {
        novelDTO = new NovelDetailResponseDTO();
        novelDTO.setId(1);
        novelDTO.setTitle("Test Novel");
        novelDTO.setViewCnt(1000);
        novelDTO.setVoteCnt(500);
        novelDTO.setCategoryId(1);

        userDTO = new UserProfileResponseDTO();
        userDTO.setUuid(UUID.randomUUID().toString());
        userDTO.setUsername("testuser");
    }

    @Test
    void testRankNovel_Success() {
        // Mock the content service response
        PageResponseDTO<NovelDetailResponseDTO> pageData = new PageResponseDTO<>();
        pageData.setContent(Arrays.asList(novelDTO));
        pageData.setTotalElements(1);
        pageData.setHasNext(false);
        
        ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(pageData);
        
        when(contentServiceClient.getNovels(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(response);

        PageResponseDTO<NovelDetailResponseDTO> result = 
                rankingService.rankNovel(0, 20, "view", null, null);

        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals(1, result.getTotalElements());
        verify(contentServiceClient, atLeastOnce()).getNovels(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void testRankUser_Success() {
        // Mock Redis responses
        when(redisUtil.zCard(anyString())).thenReturn(1L);
        Set<String> userUuids = new LinkedHashSet<>();
        userUuids.add(userDTO.getUuid());
        when(redisUtil.zReverseRange(anyString(), anyLong(), anyLong())).thenReturn(userUuids);
        
        // Mock user service response
        ApiResponse<List<UserProfileResponseDTO>> userResponse = new ApiResponse<>();
        userResponse.setCode(200);
        userResponse.setData(Arrays.asList(userDTO));
        
        when(userServiceClient.getUsersBatch(anyList()))
                .thenReturn(userResponse);
        
        // Mock gamification service response
        GamificationServiceClient.GamificationStats stats = new GamificationServiceClient.GamificationStats();
        stats.userId = userDTO.getUuid();
        stats.level = 5;
        stats.currentExp = 1000;
        
        ApiResponse<List<GamificationServiceClient.GamificationStats>> gamificationResponse = new ApiResponse<>();
        gamificationResponse.setCode(200);
        gamificationResponse.setData(Arrays.asList(stats));
        
        when(gamificationServiceClient.getBatchUsersStats(anyList()))
                .thenReturn(gamificationResponse);

        PageResponseDTO<UserProfileResponseDTO> result = 
                rankingService.rankUser(0, 20, "overall");

        assertNotNull(result);
        assertNotNull(result.getContent());
        verify(redisUtil, atLeastOnce()).zCard(anyString());
        verify(redisUtil, atLeastOnce()).zReverseRange(anyString(), anyLong(), anyLong());
        verify(userServiceClient, atLeastOnce()).getUsersBatch(anyList());
        verify(gamificationServiceClient, atLeastOnce()).getBatchUsersStats(anyList());
    }

    @Test
    void testGetBestNovelRank_Success() {
        ApiResponse<NovelDetailResponseDTO> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(novelDTO);
        when(contentServiceClient.getNovelById(1)).thenReturn(response);
        when(redisUtil.zReverseRank(anyString(), anyString())).thenReturn(0L);
        when(redisUtil.zScore(anyString(), anyString())).thenReturn(1000.0);

        NovelRankDTO result = rankingService.getBestNovelRank(1);

        assertNotNull(result);
        verify(redisUtil, atLeastOnce()).zReverseRank(anyString(), anyString());
    }
}

