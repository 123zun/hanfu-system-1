package com.server.controller;

import com.server.common.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时工具控制器 - 用于密码升级
 * 完成密码升级后可以删除此类
 */
@RestController
@RequestMapping("/api/dev")
public class DevController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 生成密码的 BCrypt 哈希（临时工具）
     * 示例：GET /api/dev/encode?password=12345678
     */
    @GetMapping("/encode")
    public R<?> encode(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        return R.success("BCrypt hash for '" + password + "': " + encoded);
    }
}
