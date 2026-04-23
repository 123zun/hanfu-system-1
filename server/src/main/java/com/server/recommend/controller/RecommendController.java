package com.server.recommend.controller;

import com.server.common.Result;
import com.server.recommend.dto.RecommendDTO;
import com.server.recommend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /**
     * 获取个性化推荐
     * GET /api/recommend?userId=1&limit=10
     */
    @GetMapping
    public Result<List<RecommendDTO>> getRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "10") Integer limit) {
        if (userId == null) {
            return Result.error(400, "userId is required");
        }
        List<RecommendDTO> recommendations = recommendService.getRecommendations(userId, limit);
        return Result.success(recommendations);
    }

    /**
     * 手动触发重建所有 embeddings（管理员接口）
     * POST /api/recommend/rebuild
     */
    @PostMapping("/rebuild")
    public Result<String> rebuildEmbeddings() {
        try {
            recommendService.rebuildAllWorkEmbeddings();
            recommendService.rebuildAllUserEmbeddings();
            return Result.success("Rebuild started");
        } catch (Exception e) {
            return Result.error("Rebuild failed: " + e.getMessage());
        }
    }

    /**
     * 更新单个作品 embedding
     * POST /api/recommend/work/{workId}
     */
    @PostMapping("/work/{workId}")
    public Result<String> updateWorkEmbedding(@PathVariable Long workId) {
        try {
            recommendService.updateWorkEmbedding(workId);
            return Result.success("Work embedding updated");
        } catch (Exception e) {
            return Result.error("Update failed: " + e.getMessage());
        }
    }

    /**
     * 更新单个用户 embedding
     * POST /api/recommend/user/{userId}
     */
    @PostMapping("/user/{userId}")
    public Result<String> updateUserEmbedding(@PathVariable Long userId) {
        try {
            recommendService.updateUserEmbedding(userId);
            return Result.success("User embedding updated");
        } catch (Exception e) {
            return Result.error("Update failed: " + e.getMessage());
        }
    }
}
