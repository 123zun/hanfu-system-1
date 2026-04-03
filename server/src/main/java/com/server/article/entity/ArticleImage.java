package com.server.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("article_image")
public class ArticleImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;    // 资讯ID
    private String imageUrl;   // 图片URL
    private Integer sortOrder = 0;  // 排序

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}