package com.server.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.article.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    // 查询不重复的分类
    @Select("SELECT DISTINCT category FROM article WHERE status = 1 AND category IS NOT NULL ORDER BY category")
    List<String> selectCategories();

    // 增加浏览量
    @Update("UPDATE article SET views = views + 1 WHERE id = #{id}")
    int increaseViews(@Param("id") Long id);

    // 增加点赞数
    @Update("UPDATE article SET likes = likes + 1 WHERE id = #{id}")
    int increaseLikes(@Param("id") Long id);

    // 减少点赞数
    @Update("UPDATE article SET likes = likes - 1 WHERE id = #{id}")
    int decreaseLikes(@Param("id") Long id);
}