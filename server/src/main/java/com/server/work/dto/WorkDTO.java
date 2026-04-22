package com.server.work.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Long userId;
    private String userName;        // 用户名
    private String userAvatar;      // 用户头像
    private String coverImage;
    private List<String> images;
    private List<String> tags;
    private Integer views = 0;
    private Integer likes = 0;
    private Integer comments = 0;
    private Boolean liked = false;     // 当前用户是否点赞
    private Boolean collected = false; // 当前用户是否收藏
    private Integer status = 1;
    private Integer auditStatus = 0;      // 审核状态: 0-待审核(已发布), 1-已通过, 2-已拒绝
    private String auditReason;         // 审核不通过原因
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
