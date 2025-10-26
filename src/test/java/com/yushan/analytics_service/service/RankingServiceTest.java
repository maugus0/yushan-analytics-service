package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
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

        userDTO = new UserProfileResponseDTO();
        userDTO.setUuid(UUID.randomUUID().toString());
        userDTO.setUsername("testuser");
    }

    @Test
    void testRankNovel_Success() {
        Set<String> novelIds = new LinkedHashSet<>(Arrays.asList("1"));
        when(redisUtil.zCard(anyString())).thenReturn(1L);
        when(redisUtil.zReverseRange(anyString(), anyLong(), anyLong())).thenReturn(novelIds);

        ApiResponse<List<NovelDetailResponseDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(Arrays.asList(novelDTO));
        when(contentServiceClient.getNovelsBatch(anyList())).thenReturn(response);

        PageResponseDTO<NovelDetailResponseDTO> result = 
                rankingService.rankNovel(0, 20, "view", null, null);

        assertNotNull(result);
        verify(redisUtil, times(1)).zCard(anyString());
    }

    @Test
    void testRankUser_Success() {
        Set<String> userUuids = new LinkedHashSet<>(Arrays.asList(userDTO.getUuid()));
        when(redisUtil.zCard(anyString())).thenReturn(1L);
        when(redisUtil.zReverseRange(anyString(), anyLong(), anyLong())).thenReturn(userUuids);

        ApiResponse<List<UserProfileResponseDTO>> response = new ApiResponse<>();
        response.setCode(200);
        response.setData(Arrays.asList(userDTO));
        when(userServiceClient.getUsersBatch(anyList())).thenReturn(response);

        PageResponseDTO<UserProfileResponseDTO> result = 
                rankingService.rankUser(0, 20, "overall");

        assertNotNull(result);
        verify(redisUtil, times(1)).zCard(anyString());
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

