package com.server.user.controller;

import com.server.common.R;
import com.server.security.UserPrincipal;
import com.server.user.dto.LoginRequest;
import com.server.user.entity.UserInfo;
import com.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    // 从配置文件中读取上传路径
    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${server.port:8080}")
    private String serverPort;

    // 用户注册
    @PostMapping("/register")
    public R<?> register(@RequestBody @Validated UserInfo userInfo) {
        try {
            R<?> result = userService.register(userInfo);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(500, "服务器内部错误: " + e.getMessage());
        }
    }

    // 用户登录
    @PostMapping("/login")
    public R<?> login(@RequestBody @Validated LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    // 退出登录（JWT无状态，服务端直接返回成功即可）
    @PostMapping("/logout")
    public R<?> logout() {
        return R.success("退出成功");
    }

    // 获取当前登录用户信息（通过JWT解析）
    @GetMapping("/info")
    public R<?> getCurrentUserInfo(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return R.error(401, "未登录");
        }
        return userService.getUserInfo(principal.getUserId());
    }

    // 根据ID获取用户信息
    @PostMapping("/info")
    public R<?> getUserInfo(@RequestBody Long id) {
        return userService.getUserInfo(id);
    }

    // 更新用户信息
    @PostMapping("/update")
    public R<?> updateUserInfo(@RequestBody UserInfo userInfo, @AuthenticationPrincipal UserPrincipal principal) {
        // 防止普通用户修改他人信息（如果前端传了其他用户ID）
        if (userInfo.getId() == null && principal != null) {
            userInfo.setId(principal.getUserId());
        }
        return userService.updateUserInfo(userInfo);
    }

    // 修改密码
    @PostMapping("/password")
    public R<?> changePassword(@RequestBody Map<String, String> params, @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = Long.parseLong(params.get("id"));
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    // 检查用户名
    @GetMapping("/check/username")
    public R<?> checkUsername(@RequestParam String username) {
        return userService.checkUsername(username);
    }

    // 检查邮箱
    @GetMapping("/check/email")
    public R<?> checkEmail(@RequestParam String email) {
        return userService.checkEmail(email);
    }

    // 获取用户列表
    @GetMapping("/users")
    public R<?> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("gender", gender);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        return userService.getUserList(params);
    }

    // 删除用户
    @DeleteMapping("/user/{id}")
    public R<?> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    // 健康检查
    @GetMapping("/health")
    public R<?> health() {
        return R.success("汉韵华章后端服务运行正常");
    }

    // 上传头像
    @PostMapping("/avatar")
    public R<?> uploadAvatar(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        System.out.println("上传文件: " + file.getOriginalFilename());
        try {
            // 验证文件
            if (file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return R.error("文件名为空");
            }

            // 获取文件扩展名
            String fileExtension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                fileExtension = originalFilename.substring(dotIndex).toLowerCase();
            }

            // 允许的图片类型
            String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
            boolean isValidExtension = false;
            for (String ext : allowedExtensions) {
                if (fileExtension.equals(ext)) {
                    isValidExtension = true;
                    break;
                }
            }

            if (!isValidExtension) {
                return R.error("只支持上传图片文件（JPG, PNG, GIF, BMP, WebP）");
            }

            // 验证文件大小（限制10MB）
            long maxFileSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxFileSize) {
                return R.error("文件大小不能超过10MB");
            }

            // ========== 修改这里：使用 static 目录 ==========
            // 获取项目根目录
            String projectPath = System.getProperty("user.dir");
            // 创建上传目录：项目根目录/src/main/resources/static/uploads/avatars/
            String avatarDir = projectPath + File.separator + "uploads" + File.separator + "avatars";

            File dir = new File(avatarDir);

            System.out.println("上传目录路径: " + dir.getAbsolutePath());

            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return R.error("创建上传目录失败: " + dir.getAbsolutePath());
                }
                System.out.println("创建目录成功: " + dir.getAbsolutePath());
            }

            // 生成唯一文件名
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String newFilename = uuid + fileExtension;

            // 保存文件
            File destFile = new File(dir, newFilename);
            file.transferTo(destFile);

            System.out.println("文件保存成功: " + destFile.getAbsolutePath());

            // 构建访问URL（相对于static目录）
            //String avatarUrl = "/uploads/avatars/" + newFilename;
            String baseUrl = "http://localhost:8080";
            String avatarUrl = baseUrl + "/uploads/avatars/" + newFilename;

            // 更新用户头像信息
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setAvatar(avatarUrl);
            R<?> updateResult = userService.updateUserInfo(userInfo);

            if (updateResult.getCode() == 200) {
                Map<String, String> data = Map.of(
                        "avatarUrl", avatarUrl,
                        "message", "头像上传成功"
                );
                return R.success("头像上传成功", data);
            } else {
                // 如果更新数据库失败，删除已上传的文件
                if (destFile.exists()) {
                    destFile.delete();
                }
                return R.error("更新用户信息失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return R.error("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("上传过程中发生错误: " + e.getMessage());
        }
    }
}
