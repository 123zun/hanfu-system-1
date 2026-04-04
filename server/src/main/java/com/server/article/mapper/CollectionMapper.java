package com.server.article.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.entity.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollectionMapper extends BaseMapper<Collection> {

    // 检查是否已收藏
    @Select("SELECT COUNT(*) FROM collection WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND status = 0")
    int checkCollected(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);

    // 获取收藏记录
    @Select("SELECT * FROM collection WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    Collection getCollectionRecord(@Param("userId") Long userId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
}
