package com.server.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.server.common.R;
import com.server.article.dto.ArticleDTO;
import com.server.article.dto.ArticleQuery;
import com.server.article.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/article")
@CrossOrigin(origins = "*")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // 获取资讯列表
    @GetMapping("/list")
    public R<?> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isTop,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(defaultValue = "publish_time") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Long userId) {  // 添加userId参数

        try {
            log.info("接收资讯列表请求: page={}, size={}, category={}, keyword={}, userId={}",
                    page, size, category, keyword, userId);

            // 构建查询参数
            ArticleQuery query = new ArticleQuery();
            query.setPage(page);
            query.setSize(size);
            query.setCategory(category);
            query.setKeyword(keyword);
            query.setStatus(status);
            query.setIsTop(isTop);
            query.setIsHot(isHot);
            query.setSortField(sortField);
            query.setSortOrder(sortOrder);

            // 查询
            IPage<ArticleDTO> result = articleService.getArticleList(query, userId);

            return R.success("获取成功", result);

        } catch (Exception e) {
            log.error("获取资讯列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    // 获取热门资讯
    @GetMapping("/hot")
    public R<?> getHotArticles(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(required = false) Long userId) {  // 添加userId参数

        try {
            log.info("获取热门资讯, limit={}, userId={}", limit, userId);

            List<ArticleDTO> articles = articleService.getHotArticles(limit, userId);

            return R.success("获取成功", articles);

        } catch (Exception e) {
            log.error("获取热门资讯失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    // 获取资讯详情
    @GetMapping("/detail/{id}")
    public R<?> getArticleDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {  // 添加userId参数

        try {
            log.info("获取资讯详情: id={}, userId={}", id, userId);

            ArticleDTO article = articleService.getArticleDetail(id, userId);

            if (article == null) {
                return R.error("资讯不存在或无权限查看");
            }

            return R.success("获取成功", article);

        } catch (Exception e) {
            log.error("获取资讯详情失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    // 获取分类列表
    @GetMapping("/categories")
    public R<?> getArticleCategories() {
        try {
            log.info("获取分类列表");

            List<String> categories = articleService.getArticleCategories();

            return R.success("获取成功", categories);

        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    // 增加浏览量
    @PostMapping("/view/{id}")
    public R<?> increaseView(@PathVariable Long id) {
        try {
            log.info("增加浏览量: id={}", id);

            articleService.increaseViewCount(id);

            return R.success("操作成功");

        } catch (Exception e) {
            log.error("增加浏览量失败", e);
            return R.error("操作失败");
        }
    }

    // 健康检查
    @GetMapping("/health")
    public R<?> health() {
        return R.success("资讯服务运行正常");
    }
}