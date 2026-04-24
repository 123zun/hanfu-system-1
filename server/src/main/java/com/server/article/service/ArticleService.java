package com.server.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.server.article.dto.ArticleDTO;
import com.server.article.dto.ArticlePageDTO;
import com.server.article.dto.ArticleQuery;
import java.util.List;

public interface ArticleService {

    /**
     * 分页查询资讯列表
     */
    ArticlePageDTO getArticleList(ArticleQuery query, Long currentUserId);

    /**
     * 获取热门资讯
     */
    List<ArticleDTO> getHotArticles(Integer limit, Long currentUserId);

    /**
     * 获取资讯详情
     */
    ArticleDTO getArticleDetail(Long id, Long currentUserId);

    /**
     * 创建资讯
     */
    ArticleDTO createArticle(ArticleDTO articleDTO);

    /**
     * 更新资讯
     */
    ArticleDTO updateArticle(ArticleDTO articleDTO);

    /**
     * 删除资讯（软删除）
     */
    boolean deleteArticle(Long id);

    /**
     * 判断当前用户是否有权限删除指定资讯（管理员可删任意，普通用户只能删自己的）
     */
    boolean canDelete(Long articleId, Long currentUserId);

    /**
     * 判断当前用户是否有权限更新指定资讯（管理员可改任意，普通用户只能改自己的）
     */
    boolean canUpdate(Long articleId, Long currentUserId);

    /**
     * 获取分类列表
     */
    List<String> getArticleCategories();

    /**
     * 增加浏览量
     */
    void increaseViewCount(Long id);

    /**
     * 点赞/取消点赞文章
     */
    boolean likeArticle(Long articleId, Long userId);

    /**
     * 检查用户是否点赞了文章
     */
    boolean isLiked(Long articleId, Long userId);

    /**
     * 收藏/取消收藏文章
     */
    boolean collectArticle(Long articleId, Long userId);

    /**
     * 检查用户是否收藏了文章
     */
    boolean isCollected(Long articleId, Long userId);

    /**
     * 统计活跃资讯数（排除已删除）
     */
    long countActiveArticles();

    /**
     * 获取用户收藏的资讯列表
     */
    List<ArticleDTO> getUserCollections(Long userId);
}
