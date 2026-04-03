package com.server.article.dto;

import lombok.Data;

@Data
public class ArticleQuery {
    private Integer page = 1;
    private Integer size = 10;
    private String category;     // 分类筛选
    private String keyword;      // 关键词搜索
    private Integer status = 1;  // 默认只查已发布的
    private Integer isTop;       // 是否置顶
    private Integer isHot;       // 是否热门
    private String sortField = "publish_time";  // 排序字段
    private String sortOrder = "desc";          // 排序方向
}