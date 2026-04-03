package com.server.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;           // 标题
    private String content;         // 内容
    private String excerpt;         // 摘要
    private String coverImage;      // 封面图

    private String category;        // 分类
    private Long authorId;          // 作者ID
    private String authorName;      // 作者姓名
    private String authorAvatar;    // 作者头像

    private Integer views = 0;      // 浏览量
    private Integer likes = 0;      // 点赞数
    private Integer comments = 0;   // 评论数

    private Integer status = 1;     // 状态：1-已发布，0-草稿
    private Integer isTop = 0;      // 是否置顶
    private Integer isHot = 0;      // 是否热门

    private LocalDateTime publishTime;  // 发布时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}