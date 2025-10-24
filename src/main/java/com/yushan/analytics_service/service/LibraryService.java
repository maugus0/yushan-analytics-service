package com.yushan.analytics_service.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LibraryService {

    /**
     * Check if novels are in user's library
     * TODO: Implement actual library check via User Service client
     *
     * @param userId User ID
     * @param novelIds List of novel IDs
     * @return Map of novel ID to library status
     */
    public Map<Integer, Boolean> checkNovelsInLibrary(UUID userId, List<Integer> novelIds) {
        // Stub implementation - returns false for all novels
        // This will be implemented later when User Service library endpoint is ready
        Map<Integer, Boolean> libraryStatusMap = new HashMap<>();
        for (Integer novelId : novelIds) {
            libraryStatusMap.put(novelId, false);
        }
        return libraryStatusMap;
    }
}