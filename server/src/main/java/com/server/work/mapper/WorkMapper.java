package com.server.work.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.work.entity.Work;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface WorkMapper extends BaseMapper<Work> {

    // 查询不重复的类型
    @Select("SELECT DISTINCT type FROM work WHERE status = 1 AND type IS NOT NULL ORDER BY type")
    List<String> selectTypes();

    // 增加浏览量
    @Update("UPDATE work SET views = views + 1 WHERE id = #{id}")
    int increaseViews(@Param("id") Long id);

    // 增加点赞数
    @Update("UPDATE work SET likes = likes + 1 WHERE id = #{id}")
    int increaseLikes(@Param("id") Long id);

    // 减少点赞数
    @Update("UPDATE work SET likes = likes - 1 WHERE id = #{id} AND likes > 0")
    int decreaseLikes(@Param("id") Long id);

    // 增加评论数
    @Update("UPDATE work SET comments = comments + 1 WHERE id = #{id}")
    int increaseComments(@Param("id") Long id);

    // 减少评论数
    @Update("UPDATE work SET comments = comments - 1 WHERE id = #{id} AND comments > 0")
    int decreaseComments(@Param("id") Long id);

    // 查询热门作品（用于推荐降级）
    @Select("SELECT * FROM work WHERE status = 1 ORDER BY views DESC, likes DESC LIMIT #{limit}")
    List<Work> selectHotWorks(@Param("limit") Integer limit);

    // 查询所有已发布的作品（用于重建embedding）
    @Select("SELECT * FROM work WHERE status = 1")
    List<Work> selectAllPublishedWorks();

    // 查询用户点赞过的作品（用于构建用户行为向量）
    @Select("SELECT w.* FROM work w INNER JOIN likes l ON w.id = l.target_id " +
           "WHERE l.user_id = #{userId} AND l.target_type = 'work' AND w.status = 1 " +
           "ORDER BY l.create_time DESC LIMIT 20")
    List<Work> selectLikedWorksByUserId(@Param("userId") Long userId);

}
