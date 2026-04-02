package com.server.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.common.R;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import com.server.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> register(UserInfo userInfo) {
        try {
            // 1. 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            userInfo.setCreateTime(now);
            userInfo.setUpdateTime(now);

            // 3. 尝试插入（让数据库唯一约束来保证唯一性）
            int result = userMapper.insert(userInfo);

            if (result > 0) {
                userInfo.setPassword(null);
                return R.success("注册成功", userInfo);
            }

            return R.error("注册失败");

        } catch (DuplicateKeyException e) {
            // 捕获唯一键冲突异常，返回友好提示
            String message = e.getMessage();
            if (message.contains("uk_username")) {
                return R.error("用户名已存在");
            } else if (message.contains("uk_email")) {
                return R.error("邮箱已被注册");
            } else if (message.contains("uk_phone")) {
                return R.error("手机号已被注册");
            }
            return R.error("注册失败，请重试");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("服务器内部错误");
        }
    }

    @Override
    public R<?> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return R.error("用户名和密码不能为空");
        }

        // 查询用户
        UserInfo userInfo = userMapper.selectByUsername(username);
        if (userInfo == null) {
            return R.error("用户不存在");
        }

        // 验证密码
        if (!password.equals(userInfo.getPassword())) {
            return R.error("密码错误");
        }

        // 登录成功，隐藏密码
        userInfo.setPassword(null);

        // 返回用户信息
        return R.success("登录成功", userInfo);
    }

    @Override
    public R<?> getUserInfo(Long id) {
        UserInfo userInfo = userMapper.selectById(id);
        if (userInfo == null) {
            return R.error("用户不存在");
        }

        return R.success(userInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> updateUserInfo(UserInfo userInfo) {
        if (userInfo.getId() == null) {
            return R.error("用户ID不能为空");
        }

        // 检查用户是否存在
        UserInfo existingUser = userMapper.selectById(userInfo.getId());
        if (existingUser == null) {
            return R.error("用户不存在");
        }

        // 如果修改了邮箱，检查邮箱是否被占用
        if (StringUtils.hasText(userInfo.getEmail()) &&
                !userInfo.getEmail().equals(existingUser.getEmail())) {
            UserInfo emailUser = userMapper.selectByEmail(userInfo.getEmail());
            if (emailUser != null && !emailUser.getId().equals(userInfo.getId())) {
                return R.error("邮箱已被使用");
            }
        }

        // 如果修改了手机号，检查手机号是否被占用
        if (StringUtils.hasText(userInfo.getPhone()) &&
                !userInfo.getPhone().equals(existingUser.getPhone())) {
            UserInfo phoneUser = userMapper.selectByPhone(userInfo.getPhone());
            if (phoneUser != null && !phoneUser.getId().equals(userInfo.getId())) {
                return R.error("手机号已被使用");
            }
        }

        // 更新信息（updateTime会自动填充）
        int result = userMapper.updateById(userInfo);

        if (result > 0) {
            UserInfo updatedUser = userMapper.selectById(userInfo.getId());
            updatedUser.setPassword(null);
            return R.success("更新成功", updatedUser);
        }

        return R.error("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> changePassword(Long userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return R.error("原密码和新密码不能为空");
        }

        if (newPassword.length() < 6) {
            return R.error("新密码长度不能少于6位");
        }

        // 查询用户
        UserInfo userInfo = userMapper.selectById(userId);
        if (userInfo == null) {
            return R.error("用户不存在");
        }

        // 验证原密码
        String encryptedOldPassword = encryptPassword(oldPassword);
        if (!encryptedOldPassword.equals(userInfo.getPassword())) {
            return R.error("原密码错误");
        }

        // 更新密码
        String encryptedNewPassword = encryptPassword(newPassword);
        int result = userMapper.updatePassword(userId, encryptedNewPassword);

        if (result > 0) {
            return R.success("密码修改成功");
        }

        return R.error("密码修改失败");
    }

    @Override
    public R<?> uploadAvatar(Long userId, MultipartFile file) {
        return null;
    }

    @Override
    public R<?> checkUsername(String username) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUsername, username)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("available", count == 0);
        data.put("message", count == 0 ? "用户名可用" : "用户名已存在");
        return R.success(data);
    }

    @Override
    public R<?> checkEmail(String email) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getEmail, email)
        );
        Map<String, Object> data = new HashMap<>();
        data.put("available", count == 0);
        data.put("message", count == 0 ? "邮箱可用" : "邮箱已存在");
        return R.success(data);
    }

    @Override
    public R<?> getUserList(Map<String, Object> params) {
        // 获取分页参数
        Integer pageNum = (Integer) params.getOrDefault("pageNum", 1);
        Integer pageSize = (Integer) params.getOrDefault("pageSize", 10);

        // 创建分页对象
        Page<UserInfo> page = new Page<>(pageNum, pageSize);

        // 创建查询条件
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();

        // 添加条件
        if (params.get("username") != null) {
            wrapper.like(UserInfo::getUsername, params.get("username"));
        }
        if (params.get("email") != null) {
            wrapper.like(UserInfo::getEmail, params.get("email"));
        }
        if (params.get("gender") != null) {
            wrapper.eq(UserInfo::getGender, params.get("gender"));
        }
        if (params.get("startTime") != null) {
            wrapper.ge(UserInfo::getCreateTime, params.get("startTime"));
        }
        if (params.get("endTime") != null) {
            wrapper.le(UserInfo::getCreateTime, params.get("endTime"));
        }

        // 排序
        wrapper.orderByDesc(UserInfo::getCreateTime);

        // 执行分页查询
        IPage<UserInfo> resultPage = userMapper.selectPage(page, wrapper);

        // 隐藏密码
        for (UserInfo user : resultPage.getRecords()) {
            user.setPassword(null);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", resultPage.getRecords());
        data.put("total", resultPage.getTotal());
        data.put("pages", resultPage.getPages());
        data.put("current", resultPage.getCurrent());
        data.put("size", resultPage.getSize());

        return R.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<?> deleteUser(Long id) {
        int result = userMapper.deleteById(id);
        if (result > 0) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    // 密码加密方法
    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    // 批量删除
    public R<?> deleteBatch(List<Long> ids) {
        int result = userMapper.deleteBatch(ids);
        if (result > 0) {
            return R.success("批量删除成功，删除了" + result + "条记录");
        }
        return R.error("批量删除失败");
    }
}