package com.server.recommend.config;

import com.server.recommend.entity.WorkEmbedding;
import com.server.work.entity.Work;
import com.server.recommend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
public class RecommendScheduler {

    @Autowired
    private RecommendService recommendService;

    /**
     * 每10分钟检查一次作品embedding
     * 1. 新作品（没有embedding）→ 立即生成
     * 2. 已有的embedding超过2小时 → 重新生成
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600秒 = 600000毫秒
    public void refreshWorkEmbeddings() {
        try {
            System.out.println("========== [推荐系统] 开始检查作品 Embedding ==========");
            
            // 1. 处理没有embedding的新作品
            List<Work> worksWithoutEmbedding = recommendService.findWorksWithoutEmbedding();
            if (!worksWithoutEmbedding.isEmpty()) {
                System.out.println("[推荐系统] 发现 \" + worksWithoutEmbedding.size() + \" 个新作品需要生成Embedding");
                for (Work work : worksWithoutEmbedding) {
                    try {
                        recommendService.updateWorkEmbedding(work.getId());
                        System.out.println("[推荐系统] 作品ID=" + work.getId() + " Embedding生成成功");
                    } catch (Exception e) {
                        System.err.println("[推荐系统] 作品ID=" + work.getId() + " Embedding生成失败: " + e.getMessage());
                    }
                }
            }
            
            // 2. 处理超过2小时未更新的embedding
            recommendService.refreshStaleWorkEmbeddings(120);
            
            System.out.println("========== [推荐系统] 作品 Embedding 检查完成 ==========");
        } catch (Exception e) {
            System.err.println("[推荐系统] 检查作品Embedding失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
