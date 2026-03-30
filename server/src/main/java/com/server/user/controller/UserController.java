package com.server.user.controller;

import com.server.utils.R;
import com.server.user.entity.UserInfo;
import com.server.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public R getUserInfo() {
        UserInfo user = userService.getCurrentUser();
        if (user == null) {
            return R.error("用户不存在");
        }

        // 隐藏敏感信息
        user.setPassword(null);
        return R.success(user);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/update")
    public R updateUserInfo(@Valid @RequestBody UserInfo user) {
        try {
            boolean success = userService.updateUserInfo(user);
            if (success) {
                return R.success("更新成功");
            } else {
                return R.error("更新失败");
            }
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    @ApiOperation("修改密码")
    @PostMapping("/change-password")
    public R changePassword(@RequestParam @NotBlank(message = "原密码不能为空") String oldPassword,
                            @RequestParam @NotBlank(message = "新密码不能为空") String newPassword) {
        try {
            boolean success = userService.changePassword(oldPassword, newPassword);
            if (success) {
                return R.success("密码修改成功");
            } else {
                return R.error("密码修改失败");
            }
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    @ApiOperation("上传头像")
    @PostMapping("/upload-avatar")
    public R uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return R.error("请选择要上传的文件");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return R.error("只能上传图片文件");
            }

            // 验证文件大小（限制2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return R.error("文件大小不能超过2MB");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            String uploadDir = "./uploads/avatar/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File dest = new File(uploadDir + filename);
            file.transferTo(dest);

            // 更新用户头像
            String avatarUrl = "/uploads/avatar/" + filename;
            UserInfo currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                currentUser.setAvatar(avatarUrl);
                userService.updateUserInfo(currentUser);
            }

            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);
            return R.success("上传成功", data);

        } catch (IOException e) {
            e.printStackTrace();
            return R.error("上传失败");
        }
    }

    @ApiOperation("检查用户名是否可用")
    @GetMapping("/check-username")
    public R checkUsername(@RequestParam @NotBlank(message = "用户名不能为空") String username) {
        boolean exists = userService.checkUsernameExists(username);
        Map<String, Object> data = new HashMap<>();
        data.put("available", !exists);
        data.put("message", exists ? "用户名已存在" : "用户名可用");
        return R.success(data);
    }

    @ApiOperation("检查邮箱是否可用")
    @GetMapping("/check-email")
    public R checkEmail(@RequestParam @NotBlank(message = "邮箱不能为空") String email) {
        boolean exists = userService.checkEmailExists(email);
        Map<String, Object> data = new HashMap<>();
        data.put("available", !exists);
        data.put("message", exists ? "邮箱已存在" : "邮箱可用");
        return R.success(data);
    }
}
