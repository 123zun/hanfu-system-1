package com.server.resource.controller;

import com.server.common.R;
import com.server.resource.dto.ResourceDTO;
import com.server.resource.dto.ResourcePageDTO;
import com.server.resource.dto.ResourceQuery;
import com.server.resource.entity.Resource;
import com.server.resource.service.ResourceService;
import com.server.security.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/resource")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    // ==================== 文件上传接口 ====================

    /**
     * 上传资源文件（同时写入数据库记录） - 公开接口
     */
    @PostMapping("/upload")
    @PreAuthorize("permitAll()")
    @Transactional
    public R<?> uploadResource(@RequestParam("file") MultipartFile file,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) Long uploaderId,
                               @RequestParam(required = false) String uploaderName) {
        log.info("上传资源文件: {}, title={}, type={}, uploaderId={}", file.getOriginalFilename(), title, type, uploaderId);

        try {
            // 1. 验证文件是否为空
            if (file == null || file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }

            // 2. 验证文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return R.error("文件名为空");
            }

            // 3. 根据文件扩展名判断类型
            String fileExtension = getFileExtension(originalFilename).toLowerCase();
            String determinedType = determineFileType(fileExtension);
            if (type == null || type.isEmpty()) {
                type = determinedType;
            }

            // 4. 验证文件大小（限制100MB）
            long maxFileSize = 100 * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                return R.error("文件大小不能超过100MB");
            }

            // 5. 创建上传目录
            String projectPath = System.getProperty("user.dir");
            String uploadDirPath = projectPath + File.separator + "uploads" + File.separator + "resources" + File.separator + type;
            File uploadDir = new File(uploadDirPath);

            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    log.error("创建上传目录失败: {}", uploadDirPath);
                    return R.error("创建上传目录失败");
                }
            }

            // 6. 生成唯一文件名
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String newFilename = uuid + fileExtension;
            File destFile = new File(uploadDir, newFilename);

            // 7. 保存文件
            file.transferTo(destFile);
            log.info("文件保存成功: {}", destFile.getAbsolutePath());

            // 8. 构建访问URL
            String baseUrl = "http://localhost:8080";
            String fileUrl = baseUrl + "/uploads/resources/" + type + "/" + newFilename;

            // 9. 构建资源信息并保存到数据库
            ResourceDTO resourceDTO = new ResourceDTO();
            resourceDTO.setTitle(title != null && !title.isEmpty() ? title : originalFilename);
            resourceDTO.setDescription(description);
            resourceDTO.setType(type);
            resourceDTO.setFileUrl(fileUrl);
            resourceDTO.setFileSize(file.getSize());
            resourceDTO.setUploaderId(uploaderId);
            resourceDTO.setUploaderName(uploaderName);
            resourceDTO.setStatus(1);

            // 保存到数据库
            ResourceDTO savedResource = resourceService.createResource(resourceDTO);

            if (savedResource == null) {
                destFile.delete();
                return R.error("资源保存失败");
            }

            log.info("资源记录创建成功: id={}", savedResource.getId());

            // 10. 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("id", savedResource.getId());
            data.put("url", fileUrl);
            data.put("filename", newFilename);
            data.put("originalFilename", originalFilename);
            data.put("size", file.getSize());
            data.put("fileSizeFormat", formatFileSize(file.getSize()));
            data.put("type", type);

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
     * 预览/下载文件（通过URL参数区分preview或download） - 公开接口
     */
    @GetMapping("/file/{id}")
    @PreAuthorize("permitAll()")
    public void serveFile(@PathVariable Long id,
                          @RequestParam(required = false, defaultValue = "false") Boolean preview,
                          HttpServletResponse response) {
        log.info("服务文件: id={}, preview={}", id, preview);

        try {
            ResourceDTO resource = resourceService.getResourceDetail(id, null);

            if (resource == null || resource.getFileUrl() == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 提取文件路径
            String fileUrl = resource.getFileUrl();
            String filePath = fileUrl.replace("http://localhost:8080", "");
            String fullPath = System.getProperty("user.dir") + filePath.replace("/", File.separator);

            File file = new File(fullPath);
            if (!file.exists() || !file.isFile()) {
                log.error("文件不存在: {}", fullPath);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 根据文件扩展名设置正确的MIME类型，确保浏览器能内嵌显示
            String ext = file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
            String contentType = getMimeType(ext);

            // preview=true用inline（浏览器内嵌显示），否则用attachment（下载）
            // 下载时文件名使用资源标题，保留原始扩展名
            String downloadFilename = (resource.getTitle() != null && !resource.getTitle().isEmpty())
                    ? resource.getTitle() + ext
                    : file.getName();
            String disposition = preview
                    ? "inline; filename=" + java.net.URLEncoder.encode(file.getName(), "UTF-8")
                    : "attachment; filename=" + java.net.URLEncoder.encode(downloadFilename, "UTF-8");

            // 预览不增加下载次数
            if (!preview) {
                resourceService.increaseDownloadCount(id);
            }

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", disposition);
            response.setHeader("Content-Length", String.valueOf(file.length()));
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");

            Files.copy(file.toPath(), response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            log.error("文件服务失败", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ex) {
                log.error("设置响应状态失败", ex);
            }
        }
    }

    /**
     * 获取资源下载信息 - 公开接口
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("permitAll()")
    public R<?> downloadResource(@PathVariable Long id) {
        log.info("下载资源: id={}", id);

        try {
            ResourceDTO resource = resourceService.getResourceDetail(id, null);

            if (resource == null) {
                return R.error("资源不存在");
            }

            // 增加下载次数
            resourceService.increaseDownloadCount(id);

            Map<String, Object> data = new HashMap<>();
            data.put("id", resource.getId());
            data.put("title", resource.getTitle());
            data.put("fileUrl", resource.getFileUrl());
            data.put("downloadCount", resource.getDownloadCount() + 1);

            return R.success("获取下载链接成功", data);

        } catch (Exception e) {
            log.error("获取下载信息失败", e);
            return R.error("获取下载信息失败: " + e.getMessage());
        }
    }

    // ==================== 资源 CRUD 接口 ====================

    /**
     * 获取资源列表 - 公开接口
     */
    @GetMapping("/list")
    @PreAuthorize("permitAll()")
    public R<?> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "create_time") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Long userId) {

        try {
            log.info("接收资源列表请求: page={}, size={}, type={}, keyword={}, userId={}",
                    page, size, type, keyword, userId);

            ResourceQuery query = new ResourceQuery();
            query.setPage(page);
            query.setSize(size);
            query.setKeyword(keyword);
            query.setType(type);
            query.setStatus(status);
            query.setSortField(sortField);
            query.setSortOrder(sortOrder);
            query.setUserId(userId);

            ResourcePageDTO result = resourceService.getResourceList(query, userId);
            return R.success("获取成功", result);

        } catch (Exception e) {
            log.error("获取资源列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取资源详情 - 公开接口
     */
    @GetMapping("/detail/{id}")
    @PreAuthorize("permitAll()")
    public R<?> getResourceDetail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            log.info("获取资源详情: id={}, userId={}", id, userId);
            ResourceDTO resource = resourceService.getResourceDetail(id, userId);

            if (resource == null) {
                return R.error("资源不存在");
            }

            return R.success("获取成功", resource);

        } catch (Exception e) {
            log.error("获取资源详情失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建资源（手动创建，不上传文件） - 需要登录
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public R<?> createResource(@RequestBody ResourceDTO resourceDTO) {
        try {
            log.info("创建资源: title={}, uploaderId={}", resourceDTO.getTitle(), resourceDTO.getUploaderId());

            ResourceDTO result = resourceService.createResource(resourceDTO);

            if (result != null) {
                return R.success("创建成功", result);
            } else {
                return R.error("创建失败");
            }

        } catch (Exception e) {
            log.error("创建资源失败", e);
            return R.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新资源 - 需要是本人或管理员
     */
    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> updateResource(@PathVariable Long id, @RequestBody ResourceDTO resourceDTO) {
        try {
            log.info("更新资源: id={}", id);

            resourceDTO.setId(id);
            ResourceDTO result = resourceService.updateResource(resourceDTO);

            if (result != null) {
                return R.success("更新成功", result);
            } else {
                return R.error("更新失败");
            }

        } catch (Exception e) {
            log.error("更新资源失败", e);
            return R.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除资源 - 需要是本人或管理员
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> deleteResource(@PathVariable Long id) {
        try {
            log.info("删除资源: id={}", id);

            boolean deleted = resourceService.deleteResource(id);

            if (deleted) {
                return R.success("删除成功");
            } else {
                return R.error("删除失败");
            }

        } catch (Exception e) {
            log.error("删除资源失败", e);
            return R.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取类型列表 - 公开接口
     */
    @GetMapping("/types")
    @PreAuthorize("permitAll()")
    public R<?> getResourceTypes() {
        try {
            List<String> types = resourceService.getResourceCategories();
            return R.success("获取成功", types);
        } catch (Exception e) {
            log.error("获取类型列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞资源 - 需要登录
     */
    @PostMapping("/like/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> likeResource(@PathVariable Long id, @RequestParam Long userId) {
        try {
            log.info("点赞/取消点赞资源: resourceId={}, userId={}", id, userId);
            boolean result = resourceService.likeResource(id, userId);
            return result ? R.success("操作成功") : R.error("操作失败");
        } catch (Exception e) {
            log.error("点赞操作失败", e);
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否点赞了资源 - 需要登录
     */
    @GetMapping("/like/check")
    @PreAuthorize("isAuthenticated()")
    public R<?> checkLiked(@RequestParam Long resourceId, @RequestParam Long userId) {
        try {
            boolean liked = resourceService.isLiked(resourceId, userId);
            return R.success("查询成功", liked);
        } catch (Exception e) {
            log.error("查询点赞状态失败", e);
            return R.error("查询失败");
        }
    }

    /**
     * 收藏/取消收藏资源 - 需要登录
     */
    @PostMapping("/collect/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> collectResource(@PathVariable Long id, @RequestParam Long userId) {
        try {
            log.info("收藏/取消收藏资源: resourceId={}, userId={}", id, userId);
            boolean result = resourceService.collectResource(id, userId);
            return result ? R.success("操作成功") : R.error("操作失败");
        } catch (Exception e) {
            log.error("收藏操作失败", e);
            return R.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否收藏了资源 - 需要登录
     */
    @GetMapping("/collect/check")
    @PreAuthorize("isAuthenticated()")
    public R<?> checkCollected(@RequestParam Long resourceId, @RequestParam Long userId) {
        try {
            boolean collected = resourceService.isCollected(resourceId, userId);
            return R.success("查询成功", collected);
        } catch (Exception e) {
            log.error("查询收藏状态失败", e);
            return R.error("查询失败");
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public R<?> health() {
        return R.success("资源服务运行正常");
    }

    // ==================== 私有工具方法 ====================

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(dotIndex);
        }
        return "";
    }

    private String determineFileType(String extension) {
        String[] imageExts = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg"};
        String[] videoExts = {".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv", ".webm"};
        String[] documentExts = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".rtf"};

        for (String ext : imageExts) {
            if (ext.equals(extension)) return "image";
        }
        for (String ext : videoExts) {
            if (ext.equals(extension)) return "video";
        }
        for (String ext : documentExts) {
            if (ext.equals(extension)) return "document";
        }
        return "other";
    }

    private String getMimeType(String ext) {
        return switch (ext) {
            case ".pdf" -> "application/pdf";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".xls" -> "application/vnd.ms-excel";
            case ".xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case ".ppt" -> "application/vnd.ms-powerpoint";
            case ".pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case ".txt" -> "text/plain";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".mp4" -> "video/mp4";
            case ".avi" -> "video/x-msvideo";
            case ".mov" -> "video/quicktime";
            case ".wmv" -> "video/x-ms-wmv";
            default -> "application/octet-stream";
        };
    }

    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = (double) bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }
}
