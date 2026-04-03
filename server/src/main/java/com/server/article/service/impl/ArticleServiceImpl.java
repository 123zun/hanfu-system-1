package com.server.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.article.dto.ArticleDTO;
import com.server.article.dto.ArticleQuery;
import com.server.article.entity.Article;
import com.server.article.entity.ArticleImage;
import com.server.article.mapper.ArticleImageMapper;
import com.server.article.mapper.ArticleMapper;
import com.server.article.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleImageMapper articleImageMapper;

    @Override
    public IPage<ArticleDTO> getArticleList(ArticleQuery query, Long currentUserId) {
        log.info("查询资讯列表: {}", query);

        // 构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        // 默认只查询已发布的
        queryWrapper.eq(Article::getStatus, query.getStatus() != null ? query.getStatus() : 1);

        // 分类筛选
        if (StringUtils.hasText(query.getCategory())) {
            queryWrapper.eq(Article::getCategory, query.getCategory());
        }

        // 关键词搜索
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

    @Override
    public List<ArticleDTO> getHotArticles(Integer limit, Long currentUserId) {
        log.info("获取热门资讯, limit={}", limit);

        // 查询条件：已发布 + (置顶或热门或高浏览量)
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, 1)  // 已发布
                .and(wrapper -> wrapper
                        .eq(Article::getIsTop, 1)     // 置顶
                        .or()
                        .eq(Article::getIsHot, 1)     // 热门
                        .or()
                        .gt(Article::getViews, 100)   // 浏览量 > 100
                )
                .orderByDesc(Article::getIsTop)   // 先按置顶
                .orderByDesc(Article::getIsHot)   // 再按热门
                .orderByDesc(Article::getViews)   // 再按浏览量
                .orderByDesc(Article::getPublishTime)  // 最后按时间
                .last("LIMIT " + (limit != null ? limit : 5));

        List<Article> articles = this.list(queryWrapper);

        return articles.stream()
                .map(article -> convertToDTO(article, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public ArticleDTO getArticleDetail(Long id, Long currentUserId) {
        log.info("获取资讯详情: id={}", id);

        Article article = this.getById(id);
        if (article == null || article.getStatus() != 1) {
            return null;
        }

        return convertToDTO(article, currentUserId);
    }

    @Override
    public List<String> getArticleCategories() {
        log.info("获取资讯分类");
        return this.baseMapper.selectCategories();
    }

    @Override
    @Transactional
    public void increaseViewCount(Long id) {
        log.info("增加资讯浏览量: id={}", id);
        this.baseMapper.increaseViews(id);
    }

    // 转换为DTO
    private ArticleDTO convertToDTO(Article article, Long currentUserId) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtils.copyProperties(article, dto);

        // 这里可以添加额外的逻辑，比如：
        // 1. 查询图片列表
        // 2. 检查当前用户是否点赞/收藏

        return dto;
    }
}