package com.server.user.service;

import com.server.common.R;
import com.server.user.entity.UserInfo;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface UserService {

    /**
     * 用户注册
     */
    R<?> register(UserInfo userInfo);

    /**
     * 用户登录
     */
    R<?> login(String username, String password);

    /**
     * 根据ID获取用户信息
     */
    R<?> getUserInfo(Long id);

    /**
     * 更新用户信息
     */
    R<?> updateUserInfo(UserInfo userInfo);

    /**
     * 修改密码
     */
    R<?> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 上传头像
     */
    R<?> uploadAvatar(Long userId, MultipartFile file);

    /**
     * 检查用户名是否可用
     */
    R<?> checkUsername(String username);

    /**
     * 检查邮箱是否可用
     */
    R<?> checkEmail(String email);

    /**
     * 获取用户列表（分页+条件查询）
     */
    R<?> getUserList(Map<String, Object> params);

    /**
     * 删除用户
     */
    R<?> deleteUser(Long id);
}