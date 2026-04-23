package com.server.recommend.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendDTO {
    private Long workId;
    private String title;
    private String description;
    private String coverImage;
    private String type;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer views;
    private Integer likes;
    private Integer comments;
    private Double score; // 相似度得分
    private List<String> tags;
}
