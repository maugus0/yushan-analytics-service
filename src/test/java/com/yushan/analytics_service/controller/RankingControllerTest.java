package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.*;
import com.yushan.analytics_service.service.RankingService;
import com.yushan.analytics_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RankingController.class)
@ActiveProfiles("test")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testRankNovel_Success() throws Exception {
        PageResponseDTO<NovelDetailResponseDTO> pageResponse = PageResponseDTO.of(
                new ArrayList<>(), 0L, 0, 50);

        when(rankingService.rankNovel(eq(0), eq(50), eq("view"), isNull(), eq("overall")))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/ranking/novel")
                        .param("page", "0")
                        .param("size", "50")
                        .param("sortType", "view")
                        .param("timeRange", "overall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(rankingService, times(1)).rankNovel(eq(0), eq(50), eq("view"), isNull(), eq("overall"));
    }

    @Test
    void testRankUser_Success() throws Exception {
        PageResponseDTO<UserProfileResponseDTO> pageResponse = PageResponseDTO.of(
                new ArrayList<>(), 0L, 0, 50);

        when(rankingService.rankUser(eq(0), eq(50), eq("overall")))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/ranking/user")
                        .param("page", "0")
                        .param("size", "50")
                        .param("timeRange", "overall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(rankingService, times(1)).rankUser(eq(0), eq(50), eq("overall"));
    }

    @Test
    void testRankAuthor_Success() throws Exception {
        PageResponseDTO<AuthorResponseDTO> pageResponse = PageResponseDTO.of(
                new ArrayList<>(), 0L, 0, 50);

        when(rankingService.rankAuthor(eq(0), eq(50), eq("vote"), eq("overall")))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/ranking/author")
                        .param("page", "0")
                        .param("size", "50")
                        .param("sortType", "vote")
                        .param("timeRange", "overall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(rankingService, times(1)).rankAuthor(eq(0), eq(50), eq("vote"), eq("overall"));
    }

    @Test
    void testGetNovelRank_Success() throws Exception {
        NovelRankDTO rankDTO = new NovelRankDTO();
        rankDTO.setNovelId(1);
        rankDTO.setRank(1L);
        rankDTO.setScore(1000.0);
        rankDTO.setRankingType("view");

        when(rankingService.getBestNovelRank(eq(1)))
                .thenReturn(rankDTO);

        mockMvc.perform(get("/api/v1/ranking/novel/1/rank"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(rankingService, times(1)).getBestNovelRank(eq(1));
    }
}

