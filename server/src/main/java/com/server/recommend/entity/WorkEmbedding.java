package com.server.recommend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkEmbedding {
    private Long id;
    private Long workId;
    private String embeddingVector; // 存储为字符串
    private LocalDateTime updatedAt;
}
