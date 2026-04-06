package com.server.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    // 查询评论列表（支持分页）
    @Select("<script>" +
            "SELECT c.*, u.username as userName, u.avatar as userAvatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.target_type = #{targetType} AND c.target_id = #{targetId} " +
            "AND c.status = 1 AND c.parent_id = 0 " +
            "ORDER BY c.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Comment> selectCommentsByTarget(@Param("targetType") String targetType,
                                          @Param("targetId") Long targetId,
                                          @Param("offset") int offset,
                                          @Param("limit") int limit);

    // 查询评论总数
    @Select("SELECT COUNT(*) FROM comment WHERE target_type = #{targetType} AND target_id = #{targetId} AND status = 1 AND parent_id = 0")
    int countCommentsByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    // 查询回复列表
    @Select("<script>" +
            "SELECT c.*, u.username as userName, u.avatar as userAvatar, " +
            "ru.username as replyToUserName " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_to_id = ru.id " +
            "WHERE c.parent_id = #{parentId} AND c.status = 1 " +
            "ORDER BY c.create_time ASC" +
            "</script>")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);

    // 增加点赞数
    @Update("UPDATE comment SET likes = likes + 1 WHERE id = #{id}")
    int increaseLikes(@Param("id") Long id);

    // 减少点赞数
    @Update("UPDATE comment SET likes = likes - 1 WHERE id = #{id} AND likes > 0")
    int decreaseLikes(@Param("id") Long id);

    // 更新评论数
    @Update("UPDATE article SET comments = comments + 1 WHERE id = #{articleId}")
    int increaseCommentCount(@Param("articleId") Long articleId);

    @Update("UPDATE article SET comments = comments - 1 WHERE id = #{articleId} AND comments > 0")
    int decreaseCommentCount(@Param("articleId") Long articleId);

    // 更新作品评论数
    @Update("UPDATE work SET comments = comments + 1 WHERE id = #{workId}")
    int increaseWorkCommentCount(@Param("workId") Long workId);

    @Update("UPDATE work SET comments = comments - 1 WHERE id = #{workId} AND comments > 0")
    int decreaseWorkCommentCount(@Param("workId") Long workId);

    // 查询作品评论总数（所有status=1的评论）
    @Select("SELECT COUNT(*) FROM comment WHERE target_type = 'work' AND target_id = #{workId} AND status = 1")
    int countWorkComments(@Param("workId") Long workId);
}
