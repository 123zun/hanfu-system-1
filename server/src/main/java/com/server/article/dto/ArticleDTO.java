package com.server.article.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDTO {
    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private String coverImage;
    private String category;

    private Long authorId;
    private String authorName;
    private String authorAvatar;

    private Integer views = 0;
    private Integer likes = 0;
    private Integer comments = 0;

    private Integer status = 1;
    private Integer isTop = 0;
    private Integer isHot = 0;

    private Boolean liked = false;      // 当前用户是否点赞
    private Boolean collected = false;  // 当前用户是否收藏

    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<String> images;  // 内容中的图片列表
}