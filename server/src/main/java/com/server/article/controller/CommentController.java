package com.server.article.controller;

import com.server.article.dto.CommentDTO;
import com.server.article.service.CommentService;
import com.server.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取评论列表
     */
    @GetMapping("/list")
    public R<?> getComments(
            @RequestParam String targetType,
            @RequestParam Long targetId,
            @RequestParam(required = false) Long userId) {

        try {
            log.info("获取评论列表: targetType={}, targetId={}, userId={}", targetType, targetId, userId);
            List<CommentDTO> comments = commentService.getCommentsByTarget(targetType, targetId, userId);
            return R.success("获取成功", comments);

        } catch (Exception e) {
            log.error("获取评论列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public R<?> addComment(@RequestBody CommentDTO commentDTO) {
        try {
            log.info("添加评论: userId={}, targetType={}, targetId={}",
                    commentDTO.getUserId(), commentDTO.getTargetType(), commentDTO.getTargetId());

            CommentDTO result = commentService.addComment(
                    commentDTO.getUserId(),
                    commentDTO.getTargetType(),
                    commentDTO.getTargetId(),
                    commentDTO.getContent(),
                    commentDTO.getParentId(),
                    commentDTO.getReplyToId()
            );

            if (result != null) {
                return R.success("评论成功", result);
            } else {
                return R.error("评论失败");
            }

        } catch (Exception e) {
            log.error("添加评论失败", e);
            return R.error("评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    public R<?> deleteComment(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            log.info("删除评论: id={}, userId={}", id, userId);
            boolean deleted = commentService.deleteComment(id, userId);

            if (deleted) {
                return R.success("删除成功");
            } else {
                return R.error("删除失败");
            }

        } catch (Exception e) {
            log.error("删除评论失败", e);
            return R.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞评论
     */
    @PostMapping("/like/{id}")
    public R<?> likeComment(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            log.info("点赞/取消点赞评论: commentId={}, userId={}", id, userId);
            boolean result = commentService.likeComment(id, userId);

            if (result) {
                return R.success("操作成功");
            } else {
                return R.error("操作失败");
            }

        } catch (Exception e) {
            log.error("点赞评论失败", e);
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否点赞了评论
     */
    @GetMapping("/like/check")
    public R<?> checkLiked(
            @RequestParam Long commentId,
            @RequestParam Long userId) {

        try {
            boolean liked = commentService.isLiked(commentId, userId);
            return R.success("查询成功", liked);

        } catch (Exception e) {
            log.error("查询点赞状态失败", e);
            return R.error("查询失败");
        }
    }
}
