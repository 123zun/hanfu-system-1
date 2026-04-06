package com.server.work.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work")
public class Work {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;           // 标题
    private String description;       // 描述
    private String type;             // 类型: photography/design/dressing
    private Long userId;             // 用户ID

    @TableField("cover_image")
    private String coverImage;       // 封面图

    private String images;           // 图片列表(JSON)
    private String tags;             // 标签(JSON)
    private Integer views = 0;      // 浏览数
    private Integer likes = 0;       // 点赞数
    private Integer comments = 0;    // 评论数
    private Integer status = 1;      // 状态：1-已发布，0-审核中

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
