package com.server.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import com.server.user.service.UserService;
import com.server.utils.JwtUtil;
import com.server.config.SecurityConfig;
import com.server.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo register(UserInfo user) {
        // 检查用户名是否已存在
        if (checkUsernameExists(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) && checkEmailExists(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(user.getPhone()) && checkPhoneExists(user.getPhone())) {
            throw new RuntimeException("手机号已存在");
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 保存用户
        userMapper.insert(user);

        return user;
    }

    @Override
    public String login(String username, String password) {
        // 根据用户名查询用户
        UserInfo user = getUserByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 生成JWT token
        return jwtUtil.generateToken(user);
    }

    @Override
    public UserInfo getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }

    @Override
    public UserInfo getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public boolean updateUserInfo(UserInfo user) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        user.setId(userId);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        UserInfo user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean checkUsernameExists(String username) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkEmailExists(String email) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        return userMapper.selectCount(wrapper) > 0;
    }
}
