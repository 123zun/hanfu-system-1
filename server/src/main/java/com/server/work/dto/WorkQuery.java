package com.server.work.dto;

import lombok.Data;

@Data
public class WorkQuery {
    private Integer page = 1;
    private Integer size = 12;
    private String type;      // 类型筛选
    private String sort = "latest";  // latest, hottest
    private Integer status = 1;      // 默认只查已发布的
    private Long userId;      // 用户ID筛选
    private String keyword;    // 关键词搜索
}
