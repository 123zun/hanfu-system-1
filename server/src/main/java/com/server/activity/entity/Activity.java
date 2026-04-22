package com.server.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;

    private String title;                        // 活动标题
    private String description;                   // 活动描述
    private String coverImage;                   // 封面图
    private String location;                     // 活动地点

    @TableField("signup_start_time")
    private LocalDate signupStartTime;          // 报名开始时间

    @TableField("signup_end_time")
    private LocalDate signupEndTime;            // 报名结束时间

    @TableField("start_time")
    private LocalDate startTime;                // 活动开始时间

    @TableField("end_time")
    private LocalDate endTime;                  // 活动结束时间

    @TableField("organizer_id")
    private Long organizerId;                    // 组织者ID

    @TableField("max_participants")
    private Integer maxParticipants;            // 最大参与人数

    @TableField("current_participants")
    private Integer currentParticipants;         // 当前参与人数

    private Integer status;                      // 状态: 0-未开始, 1-进行中, 2-已结束
    private Integer auditStatus = 0;             // 审核状态: 0-待审核, 1-已通过, 2-已拒绝
    private String auditReason;                  // 审核不通过原因
    private Long auditorId;                      // 审核人ID
    private LocalDateTime auditTime;             // 审核时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
