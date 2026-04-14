package com.server.resource.dto;

import lombok.Data;

@Data
public class ResourceQuery {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;        // 搜索关键词
    private String type;            // 资源类型: document/video/image/other
    private Integer status = 1;    // 状态
    private String sortField = "create_time";
    private String sortOrder = "desc";
    private Long userId;            // 当前登录用户ID
}
