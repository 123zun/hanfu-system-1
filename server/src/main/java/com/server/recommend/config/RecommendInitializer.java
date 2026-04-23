package com.server.recommend.config;

import com.server.recommend.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RecommendInitializer implements ApplicationRunner {

    @Autowired
    private RecommendService recommendService;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("========== [推荐系统] 开始初始化 Embeddings ==========");
        try {
            // 重建所有作品 embedding
            recommendService.rebuildAllWorkEmbeddings();
            System.out.println("========== [推荐系统] 作品 Embedding 初始化完成 ==========");
        } catch (Exception e) {
            System.err.println("作品 Embedding 初始化失败: " + e.getMessage());
        }
    }
}
