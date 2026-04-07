package com.server.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_join")
public class ActivitySignup {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("activity_id")
    private Long activityId;          // 活动ID

    @TableField("user_id")
    private Long userId;             // 用户ID

    @TableField("username")
    private String userName;          // 用户名

    @TableField("user_avatar")
    private String userAvatar;       // 用户头像

    @TableField("signup_time")
    private LocalDateTime signupTime; // 报名时间

    private Integer status = 0;      // 状态：0-正常 1-删除
}
