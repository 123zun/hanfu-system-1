package com.server.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.user.entity.UserInfo;

public interface UserService extends IService<UserInfo> {

    /**
     * 用户注册
     */
    UserInfo register(UserInfo user);

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 获取当前用户信息
     */
    UserInfo getCurrentUser();

    /**
     * 根据用户名查询用户
     */
    UserInfo getUserByUsername(String username);

    /**
     * 更新用户信息
     */
    boolean updateUserInfo(UserInfo user);

    /**
     * 修改密码
     */
    boolean changePassword(String oldPassword, String newPassword);

    /**
     * 检查用户名是否已存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否已存在
     */
    boolean checkEmailExists(String email);

    /**
     * 检查手机号是否已存在
     */
    boolean checkPhoneExists(String phone);
}
