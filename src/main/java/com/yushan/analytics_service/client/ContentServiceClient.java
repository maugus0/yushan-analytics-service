package com.yushan.analytics_service.client;

import com.yushan.analytics_service.dto.ChapterDTO;
import com.yushan.analytics_service.dto.NovelDTO;
import com.yushan.analytics_service.dto.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "${services.content.name}",
        url = "${services.content.url}"
)
public interface ContentServiceClient {

    @GetMapping("/api/novels/{id}")
    NovelDTO getNovelById(@PathVariable("id") Integer id);

    @GetMapping("/api/novels/batch")
    List<NovelDTO> getNovelsByIds(@RequestParam("ids") List<Integer> ids);

    @GetMapping("/api/chapters/{id}")
    ChapterDTO getChapterById(@PathVariable("id") Integer id);

    @GetMapping("/api/chapters/batch")
    List<ChapterDTO> getChaptersByIds(@RequestParam("ids") List<Integer> ids);

    @GetMapping("/api/categories/{id}")
    CategoryDTO getCategoryById(@PathVariable("id") Integer id);

    @GetMapping("/api/categories/batch")
    List<CategoryDTO> getCategoriesByIds(@RequestParam("ids") List<Integer> ids);
}
