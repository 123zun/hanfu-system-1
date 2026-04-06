package com.server.article.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentDTO {

    private Long id;
    private String content;           // 评论内容
    private Long userId;              // 评论用户ID
    private String userName;          // 评论用户名
    private String userAvatar;        // 评论用户头像
    private String targetType;        // 目标类型
    private Long targetId;            // 目标ID
    private Long parentId;           // 父评论ID
    private Long replyToId;          // 回复目标ID（指向被回复的评论ID）
    private String replyToUserName;  // 被回复的用户名
    private Integer likes = 0;       // 点赞数
    private Boolean liked = false;   // 是否已点赞
    private LocalDateTime createTime;
    private List<CommentDTO> replies; // 回复列表
}
