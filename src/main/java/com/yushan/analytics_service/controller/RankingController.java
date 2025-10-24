package com.yushan.analytics_service.controller;

import com.yushan.analytics_service.dto.ApiResponse;
import com.yushan.analytics_service.dto.AuthorResponseDTO;
import com.yushan.analytics_service.dto.NovelDetailResponseDTO;
import com.yushan.analytics_service.dto.NovelRankDTO;
import com.yushan.analytics_service.dto.PageResponseDTO;
import com.yushan.analytics_service.dto.UserProfileResponseDTO;
import com.yushan.analytics_service.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ranking")
@CrossOrigin(origins = "*")
@Tag(name = "Ranking", description = "Ranking APIs for novels, users, and authors")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /**
     * Get novel ranking with pagination and filtering
     *
     * @param page Page number (0-indexed)
     * @param size Page size (default 50)
     * @param sortType Sort type: "view" or "vote" (default "view")
     * @param categoryId Category ID for filtering (optional)
     * @param timeRange Time range: "weekly", "monthly", or "overall" (default "overall")
     * @return Paginated novel ranking
     */
    @Operation(summary = "Get novel ranking", description = "Retrieve paginated novel rankings with optional filtering by category and sort type")
    @GetMapping("/novel")
    public ApiResponse<PageResponseDTO<NovelDetailResponseDTO>> getNovelRanking(
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size") 
            @RequestParam(value = "size", defaultValue = "50") Integer size,
            
            @Parameter(description = "Sort type: view or vote") 
            @RequestParam(value = "sortType", defaultValue = "view") String sortType,
            
            @Parameter(description = "Category ID (optional)") 
            @RequestParam(value = "category", required = false) Integer categoryId,
            
            @Parameter(description = "Time range: weekly, monthly, or overall") 
            @RequestParam(value = "timeRange", defaultValue = "overall") String timeRange) {
        
        PageResponseDTO<NovelDetailResponseDTO> response = 
                rankingService.rankNovel(page, size, sortType, categoryId, timeRange);
        return ApiResponse.success("Novels retrieved successfully", response);
    }

    /**
     * Get user ranking with pagination
     *
     * @param page Page number (0-indexed)
     * @param size Page size (default 50)
     * @param timeRange Time range: "weekly", "monthly", or "overall" (default "overall")
     * @return Paginated user ranking
     */
    @Operation(summary = "Get user ranking", description = "Retrieve paginated user rankings based on experience points")
    @GetMapping("/user")
    public ApiResponse<PageResponseDTO<UserProfileResponseDTO>> getUserRanking(
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size") 
            @RequestParam(value = "size", defaultValue = "50") Integer size,
            
            @Parameter(description = "Time range: weekly, monthly, or overall") 
            @RequestParam(value = "timeRange", defaultValue = "overall") String timeRange) {
        
        PageResponseDTO<UserProfileResponseDTO> response = 
                rankingService.rankUser(page, size, timeRange);
        return ApiResponse.success("Users retrieved successfully", response);
    }

    /**
     * Get author ranking with pagination and sorting
     *
     * @param page Page number (0-indexed)
     * @param size Page size (default 50)
     * @param sortType Sort type: "novelNum", "view", or "vote" (default "vote")
     * @param timeRange Time range: "weekly", "monthly", or "overall" (default "overall")
     * @return Paginated author ranking
     */
    @Operation(summary = "Get author ranking", description = "Retrieve paginated author rankings with configurable sort type")
    @GetMapping("/author")
    public ApiResponse<PageResponseDTO<AuthorResponseDTO>> getAuthorRanking(
            @Parameter(description = "Page number (0-indexed)") 
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size") 
            @RequestParam(value = "size", defaultValue = "50") Integer size,
            
            @Parameter(description = "Sort type: novelNum, view, or vote") 
            @RequestParam(value = "sortType", defaultValue = "vote") String sortType,
            
            @Parameter(description = "Time range: weekly, monthly, or overall") 
            @RequestParam(value = "timeRange", defaultValue = "overall") String timeRange) {
        
        PageResponseDTO<AuthorResponseDTO> response = 
                rankingService.rankAuthor(page, size, sortType, timeRange);
        return ApiResponse.success("Authors retrieved successfully", response);
    }

    /**
     * Get the best rank for a specific novel
     *
     * @param novelId Novel ID
     * @return Novel's best rank across all ranking types
     */
    @Operation(summary = "Get novel's best rank", description = "Retrieve the best rank for a novel across all ranking categories")
    @GetMapping("/novel/{novelId}/rank")
    public ApiResponse<NovelRankDTO> getNovelRank(
            @Parameter(description = "Novel ID") 
            @PathVariable Integer novelId) {
        
        NovelRankDTO rank = rankingService.getBestNovelRank(novelId);

        if (rank == null) {
            return ApiResponse.success("Novel is not in the top 100 for any ranking.", null);
        }

        return ApiResponse.success("Novel rank retrieved successfully", rank);
    }
}

