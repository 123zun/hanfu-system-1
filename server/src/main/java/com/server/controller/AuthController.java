package com.server.controller;

import com.server.utils.R;
import com.server.user.entity.UserInfo;
import com.server.user.service.UserService;
import com.server.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public R register(@Valid @RequestBody UserInfo user) {
        try {
            UserInfo registeredUser = userService.register(user);

            Map<String, Object> data = new HashMap<>();
            data.put("user", registeredUser);
            data.put("message", "注册成功");

            return R.success(data);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R login(@RequestParam @NotBlank(message = "用户名不能为空") String username,
                   @RequestParam @NotBlank(message = "密码不能为空") String password) {
        try {
            String token = userService.login(username, password);

            // 获取用户信息
            UserInfo user = userService.getUserByUsername(username);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            data.put("expire", jwtUtil.getExpiration());

            return R.success("登录成功", data);
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public R logout() {
        return R.success("退出成功");
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/current")
    public R getCurrentUser() {
        UserInfo user = userService.getCurrentUser();
        if (user == null) {
            return R.error("用户未登录");
        }
        return R.success(user);
    }
}
