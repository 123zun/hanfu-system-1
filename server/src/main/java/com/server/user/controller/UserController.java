package com.server.user.controller;

import com.server.common.R;
import com.server.user.entity.UserInfo;
import com.server.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public R<?> register(@Valid @RequestBody UserInfo userInfo) {
        System.out.println("111111111111111111111");
        return userService.register(userInfo);
    }

    // 用户登录
    @PostMapping("/login")
    public R<?> login(@RequestBody Map<String, String> params) {
        System.out.println("2222222222222222222");
        String username = params.get("username");
        String password = params.get("password");
        return userService.login(username, password);
    }

    // 获取用户信息
    @GetMapping("/user/{id}")
    public R<?> getUserInfo(@PathVariable Long id) {
        return userService.getUserInfo(id);
    }

    // 更新用户信息
    @PutMapping("/user")
    public R<?> updateUserInfo(@RequestBody UserInfo userInfo) {
        return userService.updateUserInfo(userInfo);
    }

    // 修改密码
    @PutMapping("/user/password")
    public R<?> changePassword(@RequestBody Map<String, String> params) {
        Long userId = Long.parseLong(params.get("userId"));
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    // 上传头像
    @PostMapping("/user/avatar")
    public R<?> uploadAvatar(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        return userService.uploadAvatar(userId, file);
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
    public R<?> getUserList(@RequestParam Map<String, Object> params) {
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
}