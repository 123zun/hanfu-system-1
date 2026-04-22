package com.server.activity.controller;

import com.server.activity.dto.ActivityDTO;
import com.server.activity.dto.ActivityPageDTO;
import com.server.activity.dto.ActivityQuery;
import com.server.activity.dto.AddParticipantsRequest;
import com.server.activity.entity.ActivitySignup;
import com.server.activity.service.ActivityService;
import com.server.common.R;
import com.server.common.dto.AuditRequest;
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
@RequestMapping("/api/activity")
@CrossOrigin(origins = "*")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    private static final String UPLOAD_DIR = "./uploads/activity/";

    // ==================== 文件上传 ====================

    @PostMapping("/upload-cover")
    public R<?> uploadCover(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "covers");
    }

    private R<?> uploadImage(MultipartFile file, String subDir) {
        try {
            if (file == null || file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return R.error("文件名为空");
            }

            String ext = getFileExtension(originalFilename);
            if (!isValidImageExtension(ext)) {
                return R.error("只支持上传图片文件（JPG, PNG, GIF, WebP）");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return R.error("文件大小不能超过5MB");
            }

            String projectPath = System.getProperty("user.dir");
            String uploadDirPath = projectPath + File.separator + "uploads" + File.separator + "activity" + File.separator + subDir;
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String newFilename = UUID.randomUUID().toString().replace("-", "") + ext;
            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);

            String imageUrl = "http://localhost:8080/uploads/activity/" + subDir + "/" + newFilename;

            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);

            return R.success("上传成功", data);

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private boolean isValidImageExtension(String ext) {
        return ".jpg".equals(ext) || ".jpeg".equals(ext) || ".png".equals(ext)
                || ".gif".equals(ext) || ".webp".equals(ext);
    }

    // ==================== 活动 CRUD ====================

    @GetMapping("/list")
    public R<?> getActivityList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "9") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String full,
            @RequestParam(required = false) Long userId) {

        try {
            ActivityQuery query = new ActivityQuery();
            query.setPage(page);
            query.setSize(size);
            query.setKeyword(keyword);
            query.setStatus(status);
            query.setFull(full);

            ActivityPageDTO result = activityService.getActivityList(query, userId);
            return R.success("获取成功", result);

        } catch (Exception e) {
            log.error("获取活动列表失败", e);
            return R.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
    public R<?> getActivityDetail(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "false") Boolean silent) {

        try {
            ActivityDTO activity = activityService.getActivityDetail(id, userId, silent);
            if (activity == null) {
                return R.error("活动不存在");
            }
            return R.success("获取成功", activity);

        } catch (Exception e) {
            log.error("获取活动详情失败", e);
            return R.error("获取失败");
        }
    }

    @PostMapping("/create")
    public R<?> createActivity(@RequestBody ActivityDTO dto) {
        try {
            ActivityDTO result = activityService.createActivity(dto);
            if (result != null) {
                return R.success("创建成功", result);
            }
            return R.error("创建失败");
        } catch (Exception e) {
            log.error("创建活动失败", e);
            return R.error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public R<?> updateActivity(@RequestBody ActivityDTO dto) {
        try {
            ActivityDTO result = activityService.updateActivity(dto);
            if (result != null) {
                return R.success("更新成功", result);
            }
            return R.error("更新失败");
        } catch (Exception e) {
            log.error("更新活动失败", e);
            return R.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public R<?> deleteActivity(@PathVariable Long id) {
        try {
            boolean deleted = activityService.deleteActivity(id);
            if (deleted) {
                return R.success("删除成功");
            }
            return R.error("删除失败");
        } catch (Exception e) {
            log.error("删除活动失败", e);
            return R.error("删除失败");
        }
    }

    // ==================== 报名 ====================

    @PostMapping("/register/{id}")
    public R<?> registerActivity(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            boolean result = activityService.registerActivity(id, userId);
            if (result) {
                return R.success("报名成功");
            }
            return R.error("报名失败，可能已报名或名额已满");
        } catch (Exception e) {
            log.error("报名失败", e);
            return R.error("报名失败: " + e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public R<?> cancelRegistration(
            @PathVariable Long id,
            @RequestParam Long userId) {

        try {
            boolean result = activityService.cancelRegistration(id, userId);
            if (result) {
                return R.success("取消报名成功");
            }
            return R.error("取消报名失败");
        } catch (Exception e) {
            log.error("取消报名失败", e);
            return R.error("取消报名失败");
        }
    }

    // ==================== 参与人员管理 ====================

    @GetMapping("/participants/{activityId}")
    public R<?> getParticipants(@PathVariable Long activityId) {
        try {
            List<ActivitySignup> participants = activityService.getParticipants(activityId);
            return R.success("获取成功", participants);
        } catch (Exception e) {
            log.error("获取参与人员失败", e);
            return R.error("获取失败");
        }
    }

    @PostMapping("/participants/add")
    public R<?> addParticipants(@RequestBody AddParticipantsRequest request) {

        try {
            boolean result = activityService.addParticipants(request.getActivityId(), request.getUserIds());
            if (result) {
                return R.success("添加成功");
            }
            return R.error("添加失败，可能名额不足");
        } catch (Exception e) {
            log.error("添加参与人员失败", e);
            return R.error("添加失败");
        }
    }

    @DeleteMapping("/participants/remove")
    public R<?> removeParticipants(@RequestBody AddParticipantsRequest request) {
        try {
            boolean result = activityService.removeParticipants(request.getActivityId(), request.getSignupIds());
            if (result) {
                return R.success("删除成功");
            }
            return R.error("删除失败");
        } catch (Exception e) {
            log.error("删除参与人员失败", e);
            return R.error("删除失败");
        }
    }

    // 获取所有可用用户（用于参与人员选择）
    @GetMapping("/users")
    public R<?> getAllUsers() {
        try {
            List<ActivitySignup> users = activityService.getAllAvailableUsers();
            return R.success("获取成功", users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return R.error("获取失败");
        }
    }

    // ==================== 审核管理 ====================

    /**
     * 审核活动
     */
    @PostMapping("/audit/{id}")
    public R<?> auditActivity(
            @PathVariable Long id,
            @RequestBody AuditRequest request) {
        try {
            log.info("审核活动: id={}, approved={}, reason={}", id, request.getApproved(), request.getReason());
            boolean result = activityService.auditActivity(id, request.getAuditorId(), request.getApproved(), request.getReason());
            if (result) {
                return R.success("审核成功");
            }
            return R.error("审核失败");
        } catch (Exception e) {
            log.error("审核活动失败", e);
            return R.error("审核失败: " + e.getMessage());
        }
    }

    /**
     * 获取待审核活动列表（管理员）
     */
    @GetMapping("/pending-audit")
    public R<?> getPendingAuditList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "9") Integer size) {
        try {
            ActivityQuery query = new ActivityQuery();
            query.setPage(page);
            query.setSize(size);
            return R.success(activityService.getPendingAuditList(query));
        } catch (Exception e) {
            log.error("获取待审核列表失败", e);
            return R.error("获取失败");
        }
    }
}
