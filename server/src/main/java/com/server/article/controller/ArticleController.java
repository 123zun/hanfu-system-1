package com.server.article.controller;

import com.server.common.R;
import com.server.article.dto.ArticleDTO;
import com.server.article.dto.ArticlePageDTO;
import com.server.article.dto.ArticleQuery;
import com.server.article.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/article")
@CrossOrigin(origins = "*")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // ==================== 文件上传接口 ====================

    /**
     * 上传文章封面图
     */
    @PostMapping("/upload-cover")
    public R<?> uploadCover(@RequestParam("file") MultipartFile file) {
        log.info("上传封面图: {}", file.getOriginalFilename());
        return uploadImage(file, "covers");
    }

    /**
     * 上传文章内容图片
     */
    @PostMapping("/upload-image")
    public R<?> uploadContentImage(@RequestParam("file") MultipartFile file) {
        log.info("上传内容图片: {}", file.getOriginalFilename());
        return uploadImage(file, "content");
    }

    /**
     * 通用图片上传方法
     * @param file 图片文件
     * @param subDir 子目录（covers 或 content）
     */
    private R<?> uploadImage(MultipartFile file, String subDir) {
        try {
            // 1. 验证文件是否为空
            if (file == null || file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }

            // 2. 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return R.error("文件名为空");
            }

            String fileExtension = getFileExtension(originalFilename);
            if (!isValidImageExtension(fileExtension)) {
                return R.error("只支持上传图片文件（JPG, PNG, GIF, WebP）");
            }

            // 3. 验证文件大小（限制5MB）
            long maxFileSize = 5 * 1024 * 1024; // 5MB
            if (file.getSize() > maxFileSize) {
                return R.error("文件大小不能超过5MB");
            }

            // 4. 创建上传目录：项目根目录/uploads/articles/{covers|content}/
            String projectPath = System.getProperty("user.dir");
            String uploadDirPath = projectPath + File.separator + "uploads" + File.separator + "articles" + File.separator + subDir;
            File uploadDir = new File(uploadDirPath);

            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    log.error("创建上传目录失败: {}", uploadDirPath);
                    return R.error("创建上传目录失败");
                }
                log.info("创建目录成功: {}", uploadDirPath);
            }

            // 5. 生成唯一文件名
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String newFilename = uuid + fileExtension;
            File destFile = new File(uploadDir, newFilename);

            // 6. 保存文件
            file.transferTo(destFile);
            log.info("文件保存成功: {}", destFile.getAbsolutePath());

            // 7. 构建访问URL
            String baseUrl = "http://localhost:8080";
            String imageUrl = baseUrl + "/uploads/articles/" + subDir + "/" + newFilename;

            // 8. 返回图片URL
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);
            data.put("size", String.valueOf(file.getSize()));

            return R.success("上传成功", data);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传过程中发生错误", e);
            return R.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    /**
     * 验证是否为有效的图片格式
     */
    private boolean isValidImageExtension(String extension) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        for (String ext : allowedExtensions) {
            if (ext.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 资讯 CRUD 接口 ====================

    /**
     * 获取资讯列表
     */
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
            @RequestParam(required = false) Long userId) {

        try {
            log.info("接收资讯列表请求: page={}, size={}, category={}, keyword={}, userId={}",
                    page, size, category, keyword, userId);

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
            query.setUserId(userId);

            ArticlePageDTO result = articleService.getArticleList(query, userId);
            return R.success("获取成功", result);

        } catch (Exception e) {
            log.error("获取资讯列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门资讯
     */
    @GetMapping("/hot")
    public R<?> getHotArticles(
            @RequestParam(defaultValue = "5") Integer limit,
            @RequestParam(required = false) Long userId) {

        try {
            log.info("获取热门资讯, limit={}, userId={}", limit, userId);
            List<ArticleDTO> articles = articleService.getHotArticles(limit, userId);
            return R.success("获取成功", articles);

        } catch (Exception e) {
            log.error("获取热门资讯失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取资讯详情
     */
    @GetMapping("/detail/{id}")
    public R<?> getArticleDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {

        try {
            log.info("获取资讯详情: id={}, userId={}", id, userId);
            ArticleDTO article = articleService.getArticleDetail(id, userId);

            if (article == null) {
                return R.error("资讯不存在");
            }

            return R.success("获取成功", article);

        } catch (Exception e) {
            log.error("获取资讯详情失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建资讯
     */
    @PostMapping("/create")
    public R<?> createArticle(@RequestBody ArticleDTO articleDTO) {
        try {
            log.info("创建资讯: title={}, authorId={}", articleDTO.getTitle(), articleDTO.getAuthorId());

            ArticleDTO result = articleService.createArticle(articleDTO);

            if (result != null) {
                return R.success("创建成功", result);
            } else {
                return R.error("创建失败");
            }

        } catch (Exception e) {
            log.error("创建资讯失败", e);
            return R.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新资讯
     */
    @PutMapping("/update/{id}")
    public R<?> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        try {
            log.info("更新资讯: id={}, title={}", id, articleDTO.getTitle());

            articleDTO.setId(id);
            ArticleDTO result = articleService.updateArticle(articleDTO);

            if (result != null) {
                return R.success("更新成功", result);
            } else {
                return R.error("更新失败");
            }

        } catch (Exception e) {
            log.error("更新资讯失败", e);
            return R.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除资讯（软删除）
     */
    @DeleteMapping("/delete/{id}")
    public R<?> deleteArticle(@PathVariable Long id) {
        try {
            log.info("删除资讯: id={}", id);

            boolean deleted = articleService.deleteArticle(id);

            if (deleted) {
                return R.success("删除成功");
            } else {
                return R.error("删除失败");
            }

        } catch (Exception e) {
            log.error("删除资讯失败", e);
            return R.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类列表
     */
    @GetMapping("/categories")
    public R<?> getArticleCategories() {
        try {
            List<String> categories = articleService.getArticleCategories();
            return R.success("获取成功", categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 增加浏览量
     */
    @PostMapping("/view/{id}")
    public R<?> increaseView(@PathVariable Long id) {
        try {
            System.out.println("增加浏览量: id={}"+id);
            articleService.increaseViewCount(id);
            return R.success("操作成功");
        } catch (Exception e) {
            log.error("增加浏览量失败", e);
            return R.error("操作失败");
        }
    }

    // 在 ArticleController.java 中添加以下方法

    /**
     * 点赞/取消点赞文章
     */
    @PostMapping("/like/{id}")
    public R<?> likeArticle(@PathVariable Long id, @RequestParam Long userId) {
        try {
            log.info("点赞/取消点赞文章: articleId={}, userId={}", id, userId);
            boolean result = articleService.likeArticle(id, userId);
            if (result) {
                return R.success("操作成功");
            } else {
                return R.error("操作失败");
            }
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否点赞了文章
     */
    @GetMapping("/like/check")
    public R<?> checkLiked(@RequestParam Long articleId, @RequestParam Long userId) {
        try {
            boolean liked = articleService.isLiked(articleId, userId);
            return R.success("查询成功", liked);
        } catch (Exception e) {
            log.error("查询点赞状态失败", e);
            return R.error("查询失败");
        }
    }

    /**
     * 收藏/取消收藏文章
     */
    @PostMapping("/collect/{id}")
    public R<?> collectArticle(@PathVariable Long id, @RequestParam Long userId) {
        try {
            log.info("收藏/取消收藏文章: articleId={}, userId={}", id, userId);
            boolean result = articleService.collectArticle(id, userId);
            if (result) {
                return R.success("操作成功");
            } else {
                return R.error("操作失败");
            }
        } catch (Exception e) {
            log.error("收藏操作失败", e);
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否收藏了文章
     */
    @GetMapping("/collect/check")
    public R<?> checkCollected(@RequestParam Long articleId, @RequestParam Long userId) {
        try {
            boolean collected = articleService.isCollected(articleId, userId);
            return R.success("查询成功", collected);
        } catch (Exception e) {
            log.error("查询收藏状态失败", e);
            return R.error("查询失败");
        }
    }

    /**
     * 获取用户收藏的资讯列表
     */
    @GetMapping("/my-collections")
    public R<?> getMyCollections(@RequestParam Long userId) {
        try {
            log.info("获取用户收藏资讯: userId={}", userId);
            return R.success(articleService.getUserCollections(userId));
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return R.error("获取失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public R<?> health() {
        return R.success("资讯服务运行正常");
    }
}