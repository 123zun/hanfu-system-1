package com.server.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.article.dto.CommentDTO;
import com.server.article.mapper.CommentMapper;
import com.server.article.service.CommentService;
import com.server.entity.Comment;
import com.server.entity.Likes;
import com.server.article.mapper.LikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Override
    public List<CommentDTO> getCommentsByTarget(String targetType, Long targetId, Long currentUserId) {
        log.info("获取评论列表: targetType={}, targetId={}", targetType, targetId);

        // 查询顶级评论
        List<Comment> comments = commentMapper.selectCommentsByTarget(targetType, targetId, 0, 100);

        // 转换为DTO并查询回复
        return comments.stream()
                .map(comment -> convertToDTO(comment, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDTO addComment(Long userId, String targetType, Long targetId, String content, Long parentId, Long replyToId) {
        log.info("添加评论: userId={}, targetType={}, targetId={}, parentId={}, replyToId={}", userId, targetType, targetId, parentId, replyToId);

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setContent(content);
        comment.setParentId(parentId != null ? parentId : 0);
        comment.setReplyToId(replyToId);
        comment.setLikes(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());

        int inserted = commentMapper.insert(comment);

        if (inserted > 0) {
            // 增加评论数
            if ("article".equals(targetType)) {
                commentMapper.increaseCommentCount(targetId);
            } else if ("work".equals(targetType)) {
                commentMapper.increaseWorkCommentCount(targetId);
            }

            CommentDTO dto = new CommentDTO();
            BeanUtils.copyProperties(comment, dto);
            return dto;
        }

        return null;
    }

    @Override
    @Transactional
    public boolean deleteComment(Long commentId, Long userId) {
        log.info("删除评论: commentId={}, userId={}", commentId, userId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            return false;
        }

        // 软删除
        comment.setStatus(0);
        int updated = commentMapper.updateById(comment);

        if (updated > 0) {
            if ("article".equals(comment.getTargetType())) {
                commentMapper.decreaseCommentCount(comment.getTargetId());
            } else if ("work".equals(comment.getTargetType())) {
                commentMapper.decreaseWorkCommentCount(comment.getTargetId());
            }
        }

        return updated > 0;
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        log.info("点赞/取消点赞评论: commentId={}, userId={}", commentId, userId);

        // 检查是否已经点赞
        Likes existingLike = likeMapper.getLikeRecord(userId, "comment", commentId);

        if (existingLike == null) {
            // 未点赞，新增点赞记录
            Likes like = new Likes();
            like.setUserId(userId);
            like.setTargetType("comment");
            like.setTargetId(commentId);
            like.setStatus(0);
            int inserted = likeMapper.insert(like);

            if (inserted > 0) {
                commentMapper.increaseLikes(commentId);
                return true;
            }
        } else if (existingLike.getStatus() == 0) {
            // 已点赞，取消点赞
            existingLike.setStatus(1);
            int updated = likeMapper.updateById(existingLike);

            if (updated > 0) {
                commentMapper.decreaseLikes(commentId);
                return true;
            }
        } else {
            // 已取消点赞，重新点赞
            existingLike.setStatus(0);
            int updated = likeMapper.updateById(existingLike);

            if (updated > 0) {
                commentMapper.increaseLikes(commentId);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isLiked(Long commentId, Long userId) {
        if (userId == null) return false;
        int count = likeMapper.checkLiked(userId, "comment", commentId);
        return count > 0;
    }

    /**
     * 转换Comment为CommentDTO
     */
    private CommentDTO convertToDTO(Comment comment, Long currentUserId) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // 查询回复
        List<Comment> replies = commentMapper.selectRepliesByParentId(comment.getId());
        if (replies != null && !replies.isEmpty()) {
            List<CommentDTO> replyDTOs = replies.stream()
                    .map(reply -> {
                        CommentDTO replyDTO = new CommentDTO();
                        BeanUtils.copyProperties(reply, replyDTO);
                        if (currentUserId != null) {
                            replyDTO.setLiked(isLiked(reply.getId(), currentUserId));
                        }
                        return replyDTO;
                    })
                    .collect(Collectors.toList());
            dto.setReplies(replyDTOs);
        } else {
            dto.setReplies(new ArrayList<>());
        }

        // 检查点赞状态
        if (currentUserId != null) {
            dto.setLiked(isLiked(comment.getId(), currentUserId));
        }

        return dto;
    }
}
