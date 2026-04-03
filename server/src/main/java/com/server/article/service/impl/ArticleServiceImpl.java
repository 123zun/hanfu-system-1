package com.server.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.article.dto.ArticleDTO;
import com.server.article.dto.ArticleQuery;
import com.server.article.entity.Article;
import com.server.article.mapper.ArticleMapper;
import com.server.article.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    /**
     * 分页查询资讯列表
     */
    @Override
    public IPage<ArticleDTO> getArticleList(ArticleQuery query, Long currentUserId) {
        log.info("查询资讯列表: {}", query);

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        // 状态筛选（默认只查询已发布的）
        queryWrapper.eq(Article::getStatus, query.getStatus() != null ? query.getStatus() : 1);

        // 分类筛选
        if (StringUtils.hasText(query.getCategory())) {
            queryWrapper.eq(Article::getCategory, query.getCategory());
        }

        // 关键词搜索（标题、摘要、内容）
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getExcerpt, keyword)
                    .or()
                    .like(Article::getContent, keyword)
            );
        }

        // 置顶筛选
        if (query.getIsTop() != null) {
            queryWrapper.eq(Article::getIsTop, query.getIsTop());
        }

        // 热门筛选
        if (query.getIsHot() != null) {
            queryWrapper.eq(Article::getIsHot, query.getIsHot());
        }

        // 排序
        if ("views".equals(query.getSortField())) {
            queryWrapper.orderByDesc(Article::getViews);
        } else if ("likes".equals(query.getSortField())) {
            queryWrapper.orderByDesc(Article::getLikes);
        } else if ("publish_time".equals(query.getSortField())) {
            if ("desc".equalsIgnoreCase(query.getSortOrder())) {
                queryWrapper.orderByDesc(Article::getPublishTime);
            } else {
                queryWrapper.orderByAsc(Article::getPublishTime);
            }
        } else {
            // 默认按发布时间倒序
            queryWrapper.orderByDesc(Article::getPublishTime);
        }

        // 执行分页查询
        IPage<Article> page = new Page<>(query.getPage(), query.getSize());
        IPage<Article> articlePage = this.page(page, queryWrapper);

        // 转换为DTO
        return articlePage.convert(article -> convertToDTO(article, currentUserId));
    }

    /**
     * 获取热门资讯
     */
    @Override
    public List<ArticleDTO> getHotArticles(Integer limit, Long currentUserId) {
        log.info("获取热门资讯, limit={}", limit);

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, 1)
                .and(wrapper -> wrapper
                        .eq(Article::getIsTop, 1)
                        .or()
                        .eq(Article::getIsHot, 1)
                        .or()
                        .gt(Article::getViews, 100)
                )
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getIsHot)
                .orderByDesc(Article::getViews)
                .orderByDesc(Article::getPublishTime);

        List<Article> articles = this.list(queryWrapper);

        // 限制数量
        if (limit != null && articles.size() > limit) {
            articles = articles.subList(0, limit);
        }

        return articles.stream()
                .map(article -> convertToDTO(article, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 获取资讯详情
     */
    @Override
    public ArticleDTO getArticleDetail(Long id, Long currentUserId) {
        log.info("获取资讯详情: id={}", id);

        Article article = this.getById(id);
        if (article == null) {
            return null;
        }

        return convertToDTO(article, currentUserId);
    }

    /**
     * 创建资讯
     */
    @Override
    @Transactional
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        log.info("创建资讯: {}", articleDTO);

        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);

        // 设置默认值
        article.setStatus(articleDTO.getStatus() != null ? articleDTO.getStatus() : 1);
        article.setIsTop(0);
        article.setIsHot(0);
        article.setViews(0);
        article.setLikes(0);
        article.setComments(0);
        article.setPublishTime(LocalDateTime.now());

        boolean saved = this.save(article);

        if (saved) {
            log.info("资讯创建成功: id={}", article.getId());
            articleDTO.setId(article.getId());

            // 提取内容中的图片URL
            if (StringUtils.hasText(article.getContent())) {
                List<String> images = extractImageUrls(article.getContent());
                articleDTO.setImages(images);
            }

            return articleDTO;
        }

        return null;
    }

    /**
     * 更新资讯
     */
    @Override
    @Transactional
    public ArticleDTO updateArticle(ArticleDTO articleDTO) {
        log.info("更新资讯: id={}", articleDTO.getId());

        Article existingArticle = this.getById(articleDTO.getId());
        if (existingArticle == null) {
            log.error("资讯不存在: id={}", articleDTO.getId());
            return null;
        }

        // 更新字段
        if (StringUtils.hasText(articleDTO.getTitle())) {
            existingArticle.setTitle(articleDTO.getTitle());
        }
        if (StringUtils.hasText(articleDTO.getContent())) {
            existingArticle.setContent(articleDTO.getContent());
        }
        if (StringUtils.hasText(articleDTO.getExcerpt())) {
            existingArticle.setExcerpt(articleDTO.getExcerpt());
        }
        if (StringUtils.hasText(articleDTO.getCoverImage())) {
            existingArticle.setCoverImage(articleDTO.getCoverImage());
        }
        if (StringUtils.hasText(articleDTO.getCategory())) {
            existingArticle.setCategory(articleDTO.getCategory());
        }
        if (articleDTO.getStatus() != null) {
            existingArticle.setStatus(articleDTO.getStatus());
        }

        boolean updated = this.updateById(existingArticle);

        if (updated) {
            log.info("资讯更新成功: id={}", existingArticle.getId());

            // 提取内容中的图片URL
            if (StringUtils.hasText(existingArticle.getContent())) {
                List<String> images = extractImageUrls(existingArticle.getContent());
                articleDTO.setImages(images);
            }

            return articleDTO;
        }

        return null;
    }

    /**
     * 删除资讯（软删除）
     */
    @Override
    @Transactional
    public boolean deleteArticle(Long id) {
        log.info("删除资讯: id={}", id);

        Article article = this.getById(id);
        if (article == null) {
            log.error("资讯不存在: id={}", id);
            return false;
        }

        // 软删除：将状态改为0
        article.setStatus(0);
        return this.updateById(article);
    }

    /**
     * 获取分类列表
     */
    @Override
    public List<String> getArticleCategories() {
        log.info("获取资讯分类");
        return this.baseMapper.selectCategories();
    }

    /**
     * 增加浏览量
     */
    @Override
    @Transactional
    public void increaseViewCount(Long id) {
        log.info("增加资讯浏览量: id={}", id);
        this.baseMapper.increaseViews(id);
    }

    /**
     * 提取HTML内容中的图片URL
     */
    private List<String> extractImageUrls(String html) {
        if (!StringUtils.hasText(html)) {
            return List.of();
        }

        List<String> imageUrls = new java.util.ArrayList<>();
        Pattern pattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    /**
     * 将Article实体转换为ArticleDTO
     */
    private ArticleDTO convertToDTO(Article article, Long currentUserId) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtils.copyProperties(article, dto);

        // 提取内容中的图片URL
        if (StringUtils.hasText(article.getContent())) {
            List<String> images = extractImageUrls(article.getContent());
            dto.setImages(images);
        }

        // 这里可以添加点赞、收藏状态判断
        // dto.setLiked(checkUserLiked(article.getId(), currentUserId));
        // dto.setCollected(checkUserCollected(article.getId(), currentUserId));

        return dto;
    }
}