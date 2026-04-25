package com.server.user.controller;

import com.server.common.R;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import com.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 忘记密码控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    // 存储验证码：邮箱 -> 验证码和时间
    private static final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();

    // 验证码有效期：5分钟
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000;

    /**
     * 验证用户名和邮箱是否存在
     */
    @GetMapping("/forgot/verify")
    public R<?> verifyUsernameAndEmail(
            @RequestParam String username,
            @RequestParam String email) {
        // 查找用户
        UserInfo user = userMapper.selectByUsername(username);
        if (user == null) {
            return R.error("用户名不存在");
        }

        // 验证邮箱是否匹配
        if (!email.equals(user.getEmail())) {
            return R.error("邮箱不匹配");
        }

        return R.success("验证成功");
    }

    /**
     * 发送验证码到邮箱
     */
    @PostMapping("/forgot/send-code")
    public R<?> sendVerificationCode(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String email = params.get("email");

        // 验证用户和邮箱
        UserInfo user = userMapper.selectByUsername(username);
        if (user == null) {
            return R.error("用户名不存在");
        }
        if (!email.equals(user.getEmail())) {
            return R.error("邮箱不匹配");
        }

        // 生成6位验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        // 保存验证码
        codeStore.put(email, new CodeInfo(code, System.currentTimeMillis()));

        // 发送邮件
        try {
            sendEmail(email, code);
            return R.success("验证码已发送");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 验证验证码并重置密码
     */
    @PostMapping("/forgot/reset-password")
    public R<?> resetPassword(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        // 验证验证码
        CodeInfo codeInfo = codeStore.get(email);
        if (codeInfo == null) {
            return R.error("请先获取验证码");
        }

        long now = System.currentTimeMillis();
        if (now - codeInfo.createTime > CODE_EXPIRE_MS) {
            codeStore.remove(email);
            return R.error("验证码已过期");
        }

        if (!codeInfo.code.equals(code)) {
            return R.error("验证码错误");
        }

        // 验证新密码格式：8-16位，包含数字、英文、特殊符号
        if (!isValidPassword(newPassword)) {
            return R.error("密码必须8-16位，包含数字、英文和特殊符号");
        }

        // 根据邮箱找到用户并更新密码
        UserInfo user = userMapper.selectByEmail(email);
        if (user == null) {
            return R.error("用户不存在");
        }

        // 更新密码
        boolean success = userService.updatePassword(user.getId(), newPassword);

        if (success) {
            // 删除已使用的验证码
            codeStore.remove(email);
            return R.success("密码重置成功");
        } else {
            return R.error("密码重置失败");
        }
    }

    /**
     * 验证验证码（前端检查用）
     */
    @PostMapping("/forgot/verify-code")
    public R<?> verifyCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");

        CodeInfo codeInfo = codeStore.get(email);
        if (codeInfo == null) {
            return R.error("请先获取验证码");
        }

        long now = System.currentTimeMillis();
        if (now - codeInfo.createTime > CODE_EXPIRE_MS) {
            codeStore.remove(email);
            return R.error("验证码已过期");
        }

        if (!codeInfo.code.equals(code)) {
            return R.error("验证码错误");
        }

        return R.success("验证成功");
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String toEmail, String code) throws Exception {
        final String fromEmail = "3361466487@qq.com";
        final String authCode = "hbtueqfdcmvvcjea";
        final String smtpHost = "smtp.qq.com";

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, authCode);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject("汉韵华章 - 找回密码验证码");
        message.setContent("<h2>汉韵华章 - 找回密码验证码</h2>" +
                "<p>您好，您正在找回密码。您的验证码是：</p>" +
                "<h1 style='color: #d4af37; font-size: 32px;'>" + code + "</h1>" +
                "<p>验证码有效期为5分钟，请在有效期内完成验证。</p>" +
                "<p>如果这不是您本人操作，请忽略此邮件。</p>", "text/html; charset=UTF-8");

        Transport.send(message);
    }

    /**
     * 验证密码格式
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 16) {
            return false;
        }
        boolean hasDigit = false;
        boolean hasLetter = false;
        boolean hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) hasDigit = true;
            else if (Character.isLetter(c)) hasLetter = true;
            else hasSpecial = true;
        }
        return hasDigit && hasLetter && hasSpecial;
    }

    /**
     * 验证码信息类
     */
    private static class CodeInfo {
        String code;
        long createTime;

        CodeInfo(String code, long createTime) {
            this.code = code;
            this.createTime = createTime;
        }
    }
}