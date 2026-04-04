package com.server.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("collection")
public class Collection {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;           // 用户ID
    private String targetType;     // 目标类型：article, work
    private Long targetId;         // 目标ID
    private Integer status = 0;    // 状态：1-禁用，0-正常

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
