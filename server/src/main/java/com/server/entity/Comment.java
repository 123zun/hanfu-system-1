package com.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String content;           // 评论内容

    @TableField("user_id")
    private Long userId;              // 评论用户ID

    @TableField("target_type")
    private String targetType;        // 目标类型：article, comment

    @TableField("target_id")
    private Long targetId;           // 目标ID

    @TableField("parent_id")
    private Long parentId;           // 父评论ID（用于回复）

    @TableField("reply_to_id")
    private Long replyToId;         // 回复目标ID（指向被回复的评论ID）

    private Integer likes = 0;       // 点赞数
    private Integer status = 1;      // 状态：1-正常，0-隐藏

    // 以下字段不映射数据库，仅用于联表查询
    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private String replyToUserName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
