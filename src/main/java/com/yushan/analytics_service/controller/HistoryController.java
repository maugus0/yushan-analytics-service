package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.HistoryResponseDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import com.yushan.analytics_service.exception.UnauthorizedException;
import com.yushan.analytics_service.exception.ValidationException;
import com.yushan.analytics_service.security.CustomUserDetails;
import com.yushan.analytics_service.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /**
     * Add or update a viewing history record
     */
    @PostMapping("/novels/{novelId}/chapters/{chapterId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> addOrUpdateHistory(
            @PathVariable Integer novelId,
            @PathVariable Integer chapterId,
            Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        historyService.addOrUpdateHistory(userId, novelId, chapterId);
        return ApiResponse.success("History created/updated successfully");
    }

    /**
     * Get the user's viewing history with pagination
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponseDTO<HistoryResponseDTO>> getUserHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        PageResponseDTO<HistoryResponseDTO> historyPage = historyService.getUserHistory(userId, page, size);
        return ApiResponse.success("History retrieved successfully", historyPage);
    }

    /**
     * Delete a single history record by its ID
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> deleteHistory(@PathVariable("id") Integer historyId, Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        historyService.deleteHistory(userId, historyId);
        return ApiResponse.success("History record deleted successfully");
    }

    /**
     * Clear all the user's history
     */
    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> clearHistory(Authentication authentication) {
        UUID userId = getCurrentUserId(authentication);
        historyService.clearHistory(userId);
        return ApiResponse.success("All history records have been cleared");
    }

    protected UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        Object principal = authentication.getPrincipal();
        // FIXED: Removed CustomUserDetailsService prefix
        if (principal instanceof CustomUserDetails) {
            String id = ((CustomUserDetails) principal).getUserId();
            if (id != null) {
                return UUID.fromString(id);
            } else {
                throw new ValidationException("User ID not found");
            }
        }
        throw new UnauthorizedException("Invalid authentication");
    }
}
