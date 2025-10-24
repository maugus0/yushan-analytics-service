package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dao.HistoryMapper;
import com.yushan.analytics_service.dto.*;
import com.yushan.analytics_service.entity.History;
import com.yushan.analytics_service.exception.ResourceNotFoundException;
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
class HistoryServiceTest {

    @Mock
    private HistoryMapper historyMapper;

    @Mock
    private ContentServiceClient contentServiceClient;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private LibraryService libraryService;

    @InjectMocks
    private HistoryService historyService;

    private UUID testUserId;
    private Integer testNovelId;
    private Integer testChapterId;
    private History testHistory;
    private NovelDetailResponseDTO novelDTO;
    private ChapterDTO chapterDTO;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testNovelId = 1;
        testChapterId = 1;

        testHistory = new History();
        testHistory.setId(1);
        testHistory.setUserId(testUserId);
        testHistory.setNovelId(testNovelId);
        testHistory.setChapterId(testChapterId);

        novelDTO = new NovelDetailResponseDTO();
        novelDTO.setId(testNovelId);
        novelDTO.setTitle("Test Novel");

        chapterDTO = new ChapterDTO();
        chapterDTO.setId(testChapterId);
        chapterDTO.setTitle("Chapter 1");
        chapterDTO.setNovelId(testNovelId);
    }

    @Test
    void testGetUserHistory_Success() {
        List<History> histories = Arrays.asList(testHistory);
        when(historyMapper.selectByUserIdWithPagination(eq(testUserId), anyInt(), anyInt()))
                .thenReturn(histories);
        when(historyMapper.countByUserId(testUserId)).thenReturn(1L);

        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setCode(0);
        novelResponse.setData(novelDTO);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        ApiResponse<List<ChapterDTO>> chapterResponse = new ApiResponse<>();
        chapterResponse.setCode(0);
        chapterResponse.setData(Arrays.asList(chapterDTO));
        when(contentServiceClient.getChaptersBatch(anyList())).thenReturn(chapterResponse);

        PageResponseDTO<HistoryResponseDTO> result = 
                historyService.getUserHistory(testUserId, 0, 10);

        assertNotNull(result);
        verify(historyMapper, times(1)).selectByUserIdWithPagination(eq(testUserId), eq(0), eq(10));
    }

    @Test
    void testAddOrUpdateHistory_Success() {
        when(userServiceClient.validateUser(testUserId)).thenReturn(true);

        ApiResponse<NovelDetailResponseDTO> novelResponse = new ApiResponse<>();
        novelResponse.setCode(0);
        novelResponse.setData(novelDTO);
        when(contentServiceClient.getNovelById(testNovelId)).thenReturn(novelResponse);

        ApiResponse<List<ChapterDTO>> chapterResponse = new ApiResponse<>();
        chapterResponse.setCode(0);
        chapterResponse.setData(Arrays.asList(chapterDTO));
        when(contentServiceClient.getChaptersBatch(anyList())).thenReturn(chapterResponse);

        when(historyMapper.selectByUserAndNovel(testUserId, testNovelId)).thenReturn(null);
        when(historyMapper.insertSelective(any(History.class))).thenReturn(1);

        assertDoesNotThrow(() -> 
                historyService.addOrUpdateHistory(testUserId, testNovelId, testChapterId));

        verify(historyMapper, times(1)).insertSelective(any(History.class));
    }

    @Test
    void testDeleteHistory_Success() {
        when(historyMapper.selectByPrimaryKey(1)).thenReturn(testHistory);
        when(historyMapper.deleteByPrimaryKey(1)).thenReturn(1);

        assertDoesNotThrow(() -> historyService.deleteHistory(testUserId, 1));

        verify(historyMapper, times(1)).deleteByPrimaryKey(1);
    }

    @Test
    void testDeleteHistory_NotFound() {
        when(historyMapper.selectByPrimaryKey(1)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            historyService.deleteHistory(testUserId, 1);
        });

        verify(historyMapper, never()).deleteByPrimaryKey(anyInt());
    }

    @Test
    void testClearHistory_Success() {
        when(historyMapper.deleteByUserId(testUserId)).thenReturn(5);

        assertDoesNotThrow(() -> historyService.clearHistory(testUserId));

        verify(historyMapper, times(1)).deleteByUserId(testUserId);
    }
}

