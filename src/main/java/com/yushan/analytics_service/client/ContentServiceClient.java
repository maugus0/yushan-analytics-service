package com.yushan.analytics_service.client;

import com.yushan.analytics_service.config.FeignAuthConfig;
import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.CategoryDTO;
import com.yushan.analytics_service.dto.ChapterDTO;
import com.yushan.analytics_service.dto.NovelDetailResponseDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "content-service",
        url = "${services.content.url:http://yushan-content-service:8082}",
        configuration = FeignAuthConfig.class
)
public interface ContentServiceClient {

    @GetMapping("/api/v1/novels/{id}")
    ApiResponse<NovelDetailResponseDTO> getNovelById(@PathVariable("id") Integer id);

    @PostMapping("/api/v1/novels/batch/get")
    ApiResponse<List<NovelDetailResponseDTO>> getNovelsBatch(@RequestBody List<Integer> novelIds);

    @GetMapping("/api/v1/novels")
    ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> getNovels(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "50") Integer size
    );

    @GetMapping("/api/v1/novels/count")
    ApiResponse<Long> getNovelCount();

    @GetMapping("/api/v1/novels/author/{authorId}")
    ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> getNovelsByAuthor(
            @PathVariable("authorId") UUID authorId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "50") Integer size
    );

    @GetMapping("/api/v1/novels/category/{categoryId}")
    ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> getNovelsByCategory(
            @PathVariable("categoryId") Integer categoryId,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "50") Integer size
    );

    @GetMapping("/api/v1/categories/{id}")
    ApiResponse<CategoryDTO> getCategoryById(@PathVariable("id") Integer id);

    @GetMapping("/api/v1/categories")
    ApiResponse<List<CategoryDTO>> getAllCategories();

    @GetMapping("/api/v1/categories/{id}/statistics")
    ApiResponse<CategoryStatistics> getCategoryStatistics(@PathVariable("id") Integer id);

    // Chapter endpoints (needed for history service)
    @GetMapping("/api/v1/chapters/{uuid}")
    ApiResponse<ChapterDTO> getChapterByUuid(@PathVariable("uuid") UUID uuid);

    @PostMapping("/api/v1/chapters/batch/get")
    ApiResponse<List<ChapterDTO>> getChaptersBatch(@RequestBody List<Integer> chapterIds);
    
    // Nested class for category statistics
    // Fields are used by Jackson for deserialization
    @SuppressWarnings({"unused", "checkstyle:VisibilityModifier"})
    class CategoryStatistics {
        public Integer novelCount;
        public Integer totalViews;
        public Integer totalChapters;
    }
}
