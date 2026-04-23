package com.server.recommend.service;

import com.server.recommend.dto.RecommendDTO;
import com.server.work.entity.Work;
import java.util.List;

public interface RecommendService {

    /**
     * 获取用户个性化推荐列表
     * @param userId 用户ID
     * @param limit 返回数量
     * @return 推荐作品列表
     */
    List<RecommendDTO> getRecommendations(Long userId, Integer limit);

    /**
     * 重建所有用户 embedding（定时任务调用）
     */
    void rebuildAllUserEmbeddings();

    /**
     * 重建所有作品 embedding（定时任务调用）
     */
    void rebuildAllWorkEmbeddings();

    /**
     * 更新单个作品的 embedding
     */
    void updateWorkEmbedding(Long workId);

    /**
     * 更新单个用户的 embedding
     */
    void updateUserEmbedding(Long userId);

    /**
     * 刷新超过指定分钟数的作品embedding
     * @param minutes 超过多少分钟未更新则重新生成
     */
    void refreshStaleWorkEmbeddings(Integer minutes);

    /**
     * 查询没有embedding的作品
     * @return 需要生成embedding的作品列表
     */
    List<Work> findWorksWithoutEmbedding();
}
