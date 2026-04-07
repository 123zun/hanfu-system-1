package com.server.activity.dto;

import lombok.Data;

@Data
public class ActivityQuery {
    private Integer page = 1;
    private Integer size = 9;
    private String keyword;           // 搜索：活动名称
    private String status;           // 状态筛选：registering(正在报名), ended(已结束)
    private String full;            // 筛选：full(已满), available(有名额)
}
