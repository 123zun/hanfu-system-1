package com.server.work.controller;

import com.server.common.R;
import com.server.work.dto.WorkDTO;
import com.server.work.dto.WorkPageDTO;
import com.server.work.dto.WorkQuery;
import com.server.work.service.WorkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
     * 上传帖子封面图
     */
    @PostMapping("/upload-cover")
    public R<?> uploadCover(@RequestParam("file") MultipartFile file) {
        log.info("上传帖子封面: {}", file.getOriginalFilename());
        return uploadImage(file, "covers");
    }

    /**
     * 上传帖子内容图片
     */
    @PostMapping("/upload-image")
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
     * 获取作品列表（分页）
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
     * 获取作品详情
     */
    @GetMapping("/detail/{id}")
    public R<?> getWorkDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {

        try {
            WorkDTO work = workService.getWorkDetail(id, userId);
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
     * 获取类型列表
     */
    @GetMapping("/types")
    public R<?> getTypes() {
        try {
            return R.success(workService.getWorkTypes());
        } catch (Exception e) {
            log.error("获取类型列表失败", e);
            return R.error("获取失败");
        }
    }

    /**
     * 创建作品
     */
    @PostMapping("/create")
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
     * 更新作品
     */
    @PutMapping("/update")
    public R<?> updateWork(@RequestBody WorkDTO workDTO) {
        try {
            WorkDTO result = workService.updateWork(workDTO);
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
     * 删除作品
     */
    @DeleteMapping("/delete/{id}")
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
     * 点赞/取消点赞作品
     */
    @PostMapping("/like/{id}")
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
     * 收藏/取消收藏作品
     */
    @PostMapping("/collect/{id}")
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
}
