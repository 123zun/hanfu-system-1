package com.server.recommend.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserEmbedding {
    private Long id;
    private Long userId;
    private String embeddingVector; // 存储为字符串，格式：0.123,0.456,0.789,...
    private LocalDateTime updatedAt;
}
