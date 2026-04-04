package com.server.article.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.entity.Likes;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LikeMapper extends BaseMapper<Likes> {

    // 检查是否已点赞
    @Select("SELECT COUNT(*) FROM `likes` WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND status = 0")
    int checkLiked(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    // 获取点赞记录
    @Select("SELECT * FROM `likes` WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    Likes getLikeRecord(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
}
