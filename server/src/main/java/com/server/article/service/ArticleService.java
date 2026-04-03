package com.server.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.server.article.dto.ArticleDTO;
import com.server.article.dto.ArticleQuery;
import com.server.article.entity.Article;
import java.util.List;

public interface ArticleService {

    // 分页查询资讯列表
    IPage<ArticleDTO> getArticleList(ArticleQuery query, Long currentUserId);

    // 获取热门资讯（置顶+热门）
    List<ArticleDTO> getHotArticles(Integer limit, Long currentUserId);

    // 获取资讯详情
    ArticleDTO getArticleDetail(Long id, Long currentUserId);

    // 获取分类列表
    List<String> getArticleCategories();

    // 增加浏览量
    void increaseViewCount(Long id);
}