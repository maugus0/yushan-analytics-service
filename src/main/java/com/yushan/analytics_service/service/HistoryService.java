package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dao.HistoryMapper;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.ChapterDTO;
import com.yushan.analytics_service.dto.HistoryResponseDTO;
import com.yushan.analytics_service.dto.NovelDetailResponseDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import com.yushan.analytics_service.entity.History;
import com.yushan.analytics_service.exception.ResourceNotFoundException;
import com.yushan.analytics_service.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private LibraryService libraryService;

    /**
     * Add or update a viewing history record
     */
    @Transactional
    public void addOrUpdateHistory(UUID userId, Integer novelId, Integer chapterId) {
        // Validate user exists via User Service
        if (!userServiceClient.validateUser(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Validate novel exists via Content Service
        try {
            ApiResponse<NovelDetailResponseDTO> novelResponse = contentServiceClient.getNovelById(novelId);
            if (novelResponse == null || !novelResponse.isSuccess() || novelResponse.getData() == null) {
                throw new ResourceNotFoundException("Novel not found with id: " + novelId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Novel not found with id: " + novelId);
        }

        // Validate chapter exists and belongs to novel
        try {
            ApiResponse<List<ChapterDTO>> chapterResponse = contentServiceClient.getChaptersBatch(java.util.List.of(chapterId));
            if (chapterResponse == null || !chapterResponse.isSuccess() || 
                chapterResponse.getData() == null || chapterResponse.getData().isEmpty()) {
                throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
            }
            ChapterDTO chapter = chapterResponse.getData().get(0);
            if (!chapter.getNovelId().equals(novelId)) {
                throw new ValidationException("Chapter doesn't belong to novel id: " + novelId);
            }
        } catch (ResourceNotFoundException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }
        History existingHistory = historyMapper.selectByUserAndNovel(userId, novelId);

        if (existingHistory != null) {
            // Update existing record
            existingHistory.setChapterId(chapterId);
            existingHistory.setUpdateTime(new Date());
            historyMapper.updateByPrimaryKeySelective(existingHistory);
        } else {
            // Create new record
            History newHistory = new History();
            newHistory.setUuid(UUID.randomUUID());
            newHistory.setUserId(userId);
            newHistory.setNovelId(novelId);
            newHistory.setChapterId(chapterId);
            Date now = new Date();
            newHistory.setCreateTime(now);
            newHistory.setUpdateTime(now);
            historyMapper.insertSelective(newHistory);
        }
    }

    /**
     * Get the user's viewing history with pagination
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<HistoryResponseDTO> getUserHistory(UUID userId, int page, int size) {
        int offset = page * size;
        long totalElements = historyMapper.countByUserId(userId);
        List<History> histories = historyMapper.selectByUserIdWithPagination(userId, offset, size);

        if (histories.isEmpty()) {
            return new PageResponseDTO<>(Collections.emptyList(), totalElements, page, size);
        }

        // Extract IDs
        List<Integer> novelIds = histories.stream()
                .map(History::getNovelId)
                .distinct()
                .collect(Collectors.toList());
        List<Integer> chapterIds = histories.stream()
                .map(History::getChapterId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch data from Content Service
        List<NovelDetailResponseDTO> novels = Collections.emptyList();
        List<ChapterDTO> chapters = Collections.emptyList();
        
        try {
            ApiResponse<List<NovelDetailResponseDTO>> novelResponse = contentServiceClient.getNovelsBatch(novelIds);
            if (novelResponse != null && novelResponse.isSuccess() && novelResponse.getData() != null) {
                novels = novelResponse.getData();
            }
        } catch (Exception e) {
            // Log error but continue with empty list
        }

        try {
            ApiResponse<List<ChapterDTO>> chapterResponse = contentServiceClient.getChaptersBatch(chapterIds);
            if (chapterResponse != null && chapterResponse.isSuccess() && chapterResponse.getData() != null) {
                chapters = chapterResponse.getData();
            }
        } catch (Exception e) {
            // Log error but continue with empty list
        }

        // Convert to maps for easy lookup
        Map<Integer, NovelDetailResponseDTO> novelMap = novels.stream()
                .collect(Collectors.toMap(NovelDetailResponseDTO::getId, n -> n));
        Map<Integer, ChapterDTO> chapterMap = chapters.stream()
                .collect(Collectors.toMap(ChapterDTO::getId, c -> c));

        // Category names are already in NovelDetailResponseDTO, no need to fetch separately
        Map<Integer, String> categoryMap = novels.stream()
                .filter(n -> n.getCategoryId() != null && n.getCategoryName() != null)
                .collect(Collectors.toMap(NovelDetailResponseDTO::getCategoryId, NovelDetailResponseDTO::getCategoryName, (a, b) -> a));

        // Check library status
        Map<Integer, Boolean> libraryStatusMap = libraryService.checkNovelsInLibrary(userId, novelIds);

        // Convert to DTOs
        List<HistoryResponseDTO> dtos = histories.stream()
                .map(history -> convertToRichDTO(history, novelMap, chapterMap, categoryMap, libraryStatusMap))
                .collect(Collectors.toList());

        return new PageResponseDTO<>(dtos, totalElements, page, size);
    }

    /**
     * Delete a single history record
     */
    public void deleteHistory(UUID userId, Integer historyId) {
        History history = historyMapper.selectByPrimaryKey(historyId);
        if (history == null || !history.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("History record not found or you don't have permission to delete it.");
        }
        historyMapper.deleteByPrimaryKey(historyId);
    }

    /**
     * Clear all user history
     */
    public void clearHistory(UUID userId) {
        historyMapper.deleteByUserId(userId);
    }

    private HistoryResponseDTO convertToRichDTO(
            History history,
            Map<Integer, NovelDetailResponseDTO> novelMap,
            Map<Integer, ChapterDTO> chapterMap,
            Map<Integer, String> categoryMap,
            Map<Integer, Boolean> libraryStatusMap) {

        HistoryResponseDTO dto = new HistoryResponseDTO();
        dto.setHistoryId(history.getId());
        dto.setChapterId(history.getChapterId());
        dto.setNovelId(history.getNovelId());
        dto.setViewTime(history.getUpdateTime());

        NovelDetailResponseDTO novel = novelMap.get(history.getNovelId());
        if (novel != null) {
            dto.setNovelTitle(novel.getTitle());
            dto.setNovelCover(novel.getCoverImgUrl());
            dto.setSynopsis(novel.getSynopsis());
            dto.setAvgRating(novel.getAvgRating());
            // Note: chapterCnt is not in NovelDetailResponseDTO, set to null or fetch separately if needed
            dto.setChapterCnt(null);
            dto.setCategoryId(novel.getCategoryId());

            String categoryName = categoryMap.get(novel.getCategoryId());
            if (categoryName != null) {
                dto.setCategoryName(categoryName);
            }
        }

        ChapterDTO chapter = chapterMap.get(history.getChapterId());
        if (chapter != null) {
            dto.setChapterNumber(chapter.getChapterNumber());
        }

        dto.setInLibrary(libraryStatusMap.getOrDefault(history.getNovelId(), false));

        return dto;
    }
}
