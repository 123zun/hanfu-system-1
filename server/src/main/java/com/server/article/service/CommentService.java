package com.server.article.service;

import com.server.article.dto.CommentDTO;

import java.util.List;

public interface CommentService {

    /**
     * 获取评论列表
     */
    List<CommentDTO> getCommentsByTarget(String targetType, Long targetId, Long currentUserId);

    /**
     * 添加评论
     */
    CommentDTO addComment(Long userId, String targetType, Long targetId, String content, Long parentId, Long replyToId);

    /**
     * 删除评论
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 点赞/取消点赞评论
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 检查用户是否点赞了评论
     */
    boolean isLiked(Long commentId, Long userId);
}
