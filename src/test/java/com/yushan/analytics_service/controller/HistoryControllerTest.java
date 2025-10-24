package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.HistoryResponseDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import com.yushan.analytics_service.service.HistoryService;
import com.yushan.analytics_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryController.class)
@ActiveProfiles("test")
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoryService historyService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void testGetUserHistory_Success() throws Exception {
        PageResponseDTO<HistoryResponseDTO> pageResponse = PageResponseDTO.of(
                new ArrayList<>(), 0L, 0, 20);

        when(historyService.getUserHistory(any(UUID.class), eq(0), eq(20)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/history")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(historyService, times(1)).getUserHistory(any(UUID.class), eq(0), eq(20));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void testAddHistory_Success() throws Exception {
        doNothing().when(historyService).addOrUpdateHistory(any(UUID.class), eq(1), eq(1));

        mockMvc.perform(post("/api/history/novels/1/chapters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(historyService, times(1)).addOrUpdateHistory(any(UUID.class), eq(1), eq(1));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void testDeleteHistory_Success() throws Exception {
        doNothing().when(historyService).deleteHistory(any(UUID.class), eq(1));

        mockMvc.perform(delete("/api/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(historyService, times(1)).deleteHistory(any(UUID.class), eq(1));
    }

    @Test
    @WithMockUser(username = "test-user", roles = {"USER"})
    void testClearHistory_Success() throws Exception {
        doNothing().when(historyService).clearHistory(any(UUID.class));

        mockMvc.perform(delete("/api/history/clear"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(historyService, times(1)).clearHistory(any(UUID.class));
    }
}

