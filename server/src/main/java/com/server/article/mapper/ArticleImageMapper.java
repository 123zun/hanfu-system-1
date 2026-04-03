package com.server.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.article.entity.ArticleImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ArticleImageMapper extends BaseMapper<ArticleImage> {

    // 根据资讯ID查询图片
    @Select("SELECT * FROM article_image WHERE article_id = #{articleId} ORDER BY sort_order, create_time")
    List<ArticleImage> selectByArticleId(@Param("articleId") Long articleId);

    // 批量插入图片
    int batchInsert(@Param("images") List<ArticleImage> images);
}