package com.server.activity.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ActivityDTO {
    private Long id;
    private String title;
    private String description;
    private String coverImage;
    private String location;            // 活动地点
    private Integer maxParticipants;
    private Integer currentParticipants;  // 当前报名人数
    private LocalDate registrationStartTime;
    private LocalDate registrationEndTime;
    private LocalDate startTime;
    private LocalDate endTime;
    private Long organizerId;
    private Integer auditStatus = 0;        // 审核状态: 0-待审核, 1-已通过, 2-已拒绝
    private String auditReason;             // 审核不通过原因
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 用户信息（创建者）
    private Long userId;
    private String userName;
    private String userAvatar;

    // 当前用户是否已报名
    private Boolean registered = false;
}
