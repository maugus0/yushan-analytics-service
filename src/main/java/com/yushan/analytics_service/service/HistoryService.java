package com.yushan.analytics_service.service;

import com.yushan.analytics_service.client.ContentServiceClient;
import com.yushan.analytics_service.client.UserServiceClient;
import com.yushan.analytics_service.dao.HistoryMapper;
import com.yushan.analytics_service.dto.CategoryDTO;
import com.yushan.analytics_service.dto.ChapterDTO;
import com.yushan.analytics_service.dto.HistoryResponseDTO;
import com.yushan.analytics_service.dto.NovelDTO;
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
        NovelDTO novel = contentServiceClient.getNovelById(novelId);
        if (novel == null) {
            throw new ResourceNotFoundException("Novel not found with id: " + novelId);
        }

        // Validate chapter exists and belongs to novel
        ChapterDTO chapter = contentServiceClient.getChapterById(chapterId);
        if (chapter == null) {
            throw new ResourceNotFoundException("Chapter not found with id: " + chapterId);
        }
        if (!chapter.getNovelId().equals(novelId)) {
            throw new ValidationException("Chapter doesn't belong to novel id: " + novelId);
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
        List<NovelDTO> novels = contentServiceClient.getNovelsByIds(novelIds);
        List<ChapterDTO> chapters = contentServiceClient.getChaptersByIds(chapterIds);

        // Convert to maps for easy lookup
        Map<Integer, NovelDTO> novelMap = novels.stream()
                .collect(Collectors.toMap(NovelDTO::getId, n -> n));
        Map<Integer, ChapterDTO> chapterMap = chapters.stream()
                .collect(Collectors.toMap(ChapterDTO::getId, c -> c));

        // Fetch categories
        List<Integer> categoryIds = novels.stream()
                .map(NovelDTO::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        List<CategoryDTO> categories = contentServiceClient.getCategoriesByIds(categoryIds);
        Map<Integer, CategoryDTO> categoryMap = categories.stream()
                .collect(Collectors.toMap(CategoryDTO::getId, c -> c));

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
            Map<Integer, NovelDTO> novelMap,
            Map<Integer, ChapterDTO> chapterMap,
            Map<Integer, CategoryDTO> categoryMap,
            Map<Integer, Boolean> libraryStatusMap) {

        HistoryResponseDTO dto = new HistoryResponseDTO();
        dto.setHistoryId(history.getId());
        dto.setChapterId(history.getChapterId());
        dto.setNovelId(history.getNovelId());
        dto.setViewTime(history.getUpdateTime());

        NovelDTO novel = novelMap.get(history.getNovelId());
        if (novel != null) {
            dto.setNovelTitle(novel.getTitle());
            dto.setNovelCover(novel.getCoverImgUrl());
            dto.setSynopsis(novel.getSynopsis());
            dto.setAvgRating(novel.getAvgRating());
            dto.setChapterCnt(novel.getChapterCnt());
            dto.setCategoryId(novel.getCategoryId());

            CategoryDTO category = categoryMap.get(novel.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
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
