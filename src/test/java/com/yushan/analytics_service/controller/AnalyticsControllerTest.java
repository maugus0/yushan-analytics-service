package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.*;
import com.yushan.analytics_service.service.AnalyticsService;
import com.yushan.analytics_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtUtil jwtUtil;

    private AnalyticsSummaryResponseDTO summaryDTO;
    private PlatformStatisticsResponseDTO platformStatsDTO;
    private AnalyticsTrendResponseDTO trendDTO;
    private DailyActiveUsersResponseDTO dauDTO;
    private TopContentResponseDTO topContentDTO;

    @BeforeEach
    void setUp() {
        summaryDTO = new AnalyticsSummaryResponseDTO();
        platformStatsDTO = new PlatformStatisticsResponseDTO();
        trendDTO = new AnalyticsTrendResponseDTO();
        dauDTO = new DailyActiveUsersResponseDTO();
        topContentDTO = new TopContentResponseDTO();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetUserTrends_Success() throws Exception {
        when(analyticsService.getUserTrends(any(AnalyticsRequestDTO.class)))
                .thenReturn(trendDTO);

        mockMvc.perform(get("/api/v1/admin/analytics/users/trends")
                        .param("period", "daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analyticsService, times(1)).getUserTrends(any(AnalyticsRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetReadingActivityTrends_Success() throws Exception {
        when(analyticsService.getReadingActivityTrends(any(AnalyticsRequestDTO.class)))
                .thenReturn(new ReadingActivityResponseDTO());

        mockMvc.perform(get("/api/v1/admin/analytics/reading/activity")
                        .param("period", "daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analyticsService, times(1)).getReadingActivityTrends(any(AnalyticsRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAnalyticsSummary_Success() throws Exception {
        when(analyticsService.getAnalyticsSummary(any(AnalyticsRequestDTO.class)))
                .thenReturn(summaryDTO);

        mockMvc.perform(get("/api/v1/admin/analytics/summary")
                        .param("period", "daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analyticsService, times(1)).getAnalyticsSummary(any(AnalyticsRequestDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetAnalyticsSummary_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/summary"))
                .andExpect(status().isForbidden());

        verify(analyticsService, never()).getAnalyticsSummary(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetPlatformStatistics_Success() throws Exception {
        when(analyticsService.getPlatformStatistics())
                .thenReturn(platformStatsDTO);

        mockMvc.perform(get("/api/v1/admin/analytics/platform/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analyticsService, times(1)).getPlatformStatistics();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetDailyActiveUsers_Success() throws Exception {
        when(analyticsService.getDailyActiveUsers(any()))
                .thenReturn(dauDTO);

        mockMvc.perform(get("/api/v1/admin/analytics/platform/dau"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analyticsService, times(1)).getDailyActiveUsers(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetTopContent_Success() throws Exception {
        when(analyticsService.getTopContent(eq(10)))
                .thenReturn(topContentDTO);

        mockMvc.perform(get("/api/v1/admin/analytics/platform/top-content")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analyticsService, times(1)).getTopContent(eq(10));
    }

    @Test
    void testAnalyticsEndpoints_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics/summary"))
                .andExpect(status().isUnauthorized());

        verify(analyticsService, never()).getAnalyticsSummary(any());
    }
}

