package com.server.work.controller;

import com.server.common.R;
import com.server.common.dto.AuditRequest;
import com.server.security.SecurityUtils;
import com.server.work.dto.WorkDTO;
import com.server.work.dto.WorkPageDTO;
import com.server.work.dto.WorkQuery;
import com.server.work.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/work")
@CrossOrigin(origins = "*")
public class WorkController {

    @Autowired
    private WorkService workService;

    // ==================== 文件上传接口 ====================

    /**
     * 上传帖子封面图 - 需要登录
     */
    @PostMapping("/upload-cover")
    @PreAuthorize("isAuthenticated()")
    public R<?> uploadCover(@RequestParam("file") MultipartFile file) {
        log.info("上传帖子封面: {}", file.getOriginalFilename());
        return uploadImage(file, "covers");
    }

    /**
     * 上传帖子内容图片 - 需要登录
     */
    @PostMapping("/upload-image")
    @PreAuthorize("isAuthenticated()")
    public R<?> uploadContentImage(@RequestParam("file") MultipartFile file) {
        log.info("上传帖子内容图片: {}", file.getOriginalFilename());
        return uploadImage(file, "content");
    }

    /**
     * 通用图片上传方法
     */
    private R<?> uploadImage(MultipartFile file, String subDir) {
        try {
            if (file == null || file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return R.error("文件名为空");
            }

            String fileExtension = getFileExtension(originalFilename);
            if (!isValidImageExtension(fileExtension)) {
                return R.error("只支持上传图片文件（JPG, PNG, GIF, WebP）");
            }

            long maxFileSize = 5 * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                return R.error("文件大小不能超过5MB");
            }

            String projectPath = System.getProperty("user.dir");
            String uploadDirPath = projectPath + File.separator + "uploads" + File.separator + "work" + File.separator + subDir;
            File uploadDir = new File(uploadDirPath);

            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                if (!created) {
                    log.error("创建上传目录失败: {}", uploadDirPath);
                    return R.error("创建上传目录失败");
                }
                log.info("创建目录成功: {}", uploadDirPath);
            }

            String uuid = UUID.randomUUID().toString().replace("-", "");
            String newFilename = uuid + fileExtension;
            File destFile = new File(uploadDir, newFilename);

            file.transferTo(destFile);
            log.info("文件上传成功: {}", destFile.getAbsolutePath());

            String baseUrl = "http://localhost:8080";
            String imageUrl = baseUrl + "/uploads/work/" + subDir + "/" + newFilename;

            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);
            data.put("size", String.valueOf(file.getSize()));

            return R.success("上传成功", data);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("上传发生其他异常", e);
            return R.error("上传失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private boolean isValidImageExtension(String ext) {
        return ".jpg".equals(ext) || ".jpeg".equals(ext) || ".png".equals(ext) || ".gif".equals(ext) || ".webp".equals(ext);
    }

    // ==================== 帖子 CRUD 接口 ====================

    /**
     * 获取作品列表（分页） - 公开接口，无需登录
     */
    @GetMapping("/list")
    public R<?> getWorkList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Long userId) {

        try {
            WorkQuery query = new WorkQuery();
            query.setPage(page);
            query.setSize(size);
            query.setType(type);
            query.setKeyword(keyword);
            query.setSort(sort);
            query.setUserId(userId);

            WorkPageDTO result = workService.getWorkList(query, null);
            return R.success("获取成功", result);

        } catch (Exception e) {
            log.error("获取作品列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取作品详情 - 公开接口，无需登录
     */
    @GetMapping("/detail/{id}")
    public R<?> getWorkDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "false") Boolean silent) {

        try {
            WorkDTO work = workService.getWorkDetail(id, userId, silent);
            if (work == null) {
                return R.error("作品不存在");
            }
            return R.success("获取成功", work);

        } catch (Exception e) {
            log.error("获取作品详情失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取类型列表 - 公开接口
     */
    @GetMapping("/types")
    @PreAuthorize("permitAll()")
    public R<?> getTypes() {
        try {
            return R.success(workService.getWorkTypes());
        } catch (Exception e) {
            log.error("获取类型列表失败", e);
            return R.error("获取失败");
        }
    }

    /**
     * 创建作品 - 需要登录
     */
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public R<?> createWork(@RequestBody WorkDTO workDTO) {
        try {
            // 获取用户ID
            if (workDTO.getUserId() == null) {
                return R.error("用户未登录");
            }

            WorkDTO result = workService.createWork(workDTO, workDTO.getUserId());
            if (result != null) {
                return R.success("创建成功", result);
            } else {
                return R.error("创建失败");
            }
        } catch (Exception e) {
            log.error("创建作品失败", e);
            return R.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新作品 - 需要是本人或管理员
     */
    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public R<?> updateWork(@RequestBody WorkDTO workDTO) {
        try {
            if (workDTO.getUserId() == null) {
                return R.error("用户未登录");
            }
            WorkDTO result = workService.updateWork(workDTO, workDTO.getUserId());
            if (result != null) {
                return R.success("更新成功", result);
            } else {
                return R.error("更新失败");
            }
        } catch (Exception e) {
            log.error("更新作品失败", e);
            return R.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除作品 - 需要是本人或管理员
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> deleteWork(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            boolean deleted = workService.deleteWork(id, userId);
            if (deleted) {
                return R.success("删除成功");
            } else {
                return R.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除作品失败", e);
            return R.error("删除失败");
        }
    }

    /**
     * 点赞/取消点赞作品 - 需要登录
     */
    @PostMapping("/like/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> likeWork(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            boolean result = workService.likeWork(id, userId);
            return R.success(result ? "操作成功" : "操作失败");
        } catch (Exception e) {
            log.error("点赞作品失败", e);
            return R.error("操作失败");
        }
    }

    /**
     * 收藏/取消收藏作品 - 需要登录
     */
    @PostMapping("/collect/{id}")
    @PreAuthorize("isAuthenticated()")
    public R<?> collectWork(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            boolean result = workService.collectWork(id, userId);
            return R.success(result ? "操作成功" : "操作失败");
        } catch (Exception e) {
            log.error("收藏作品失败", e);
            return R.error("操作失败");
        }
    }

    /**
     * 获取热门帖子 - 公开接口
     */
    @GetMapping("/hot")
    @PreAuthorize("permitAll()")
    public R<?> getHotWorks(@RequestParam(defaultValue = "5") Integer limit) {
        try {
            log.info("获取热门帖子, limit={}", limit);
            return R.success(workService.getHotWorks(limit));
        } catch (Exception e) {
            log.error("获取热门帖子失败", e);
            return R.error("获取失败");
        }
    }

    /**
     * 审核作品 - 仅管理员
     */
    @PostMapping("/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public R<?> auditWork(
            @PathVariable Long id,
            @RequestBody AuditRequest request) {
        try {
            log.info("审核作品: id={}, approved={}, reason={}", id, request.getApproved(), request.getReason());
            boolean result = workService.auditWork(id, request.getAuditorId(), request.getApproved(), request.getReason());
            if (result) {
                return R.success("审核成功");
            }
            return R.error("审核失败");
        } catch (Exception e) {
            log.error("审核作品失败", e);
            return R.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核作品列表 - 仅管理员
     */
    @GetMapping("/pending-audit")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public R<?> getPendingAuditList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        try {
            com.server.work.dto.WorkQuery query = new com.server.work.dto.WorkQuery();
            query.setPage(page);
            query.setSize(size);
            return R.success(workService.getPendingAuditList(query));
        } catch (Exception e) {
            log.error("获取待审核列表失败", e);
            return R.error("获取失败");
        }
    }

    /**
     * 获取用户收藏的作品列表 - 需要登录
     */
    @GetMapping("/my-collections")
    @PreAuthorize("isAuthenticated()")
    public R<?> getMyCollections(@RequestParam Long userId) {
        try {
            log.info("获取用户收藏作品: userId={}", userId);
            return R.success(workService.getUserCollections(userId));
        } catch (Exception e) {
            log.error("获取收藏列表失败", e);
            return R.error("获取失败");
        }
    }
}
