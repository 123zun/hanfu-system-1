package com.server.follow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.common.R;
import com.server.follow.entity.Follow;
import com.server.follow.service.FollowService;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follow")
@CrossOrigin(origins = "*")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    @PreAuthorize("isAuthenticated()")
    public R<?> follow(@RequestParam Long followerId, @RequestParam Long followingId) {
        if (followerId.equals(followingId)) {
            return R.error("不能关注自己");
        }
        boolean success = followService.follow(followerId, followingId);
        if (success) {
            return R.success("关注成功");
        }
        return R.error("关注失败或已关注");
    }

    /**
     * 取消关注
     */
    @PostMapping("/unfollow")
    @PreAuthorize("isAuthenticated()")
    public R<?> unfollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        boolean success = followService.unfollow(followerId, followingId);
        if (success) {
            return R.success("取消关注成功");
        }
        return R.error("取消关注失败");
    }

    /**
     * 检查关注状态
     */
    @GetMapping("/check")
    public R<?> checkFollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        boolean isFollowing = followService.isFollowing(followerId, followingId);
        long followerCount = followService.getFollowerCount(followingId);
        long followingCount = followService.getFollowingCount(followingId);
        Map<String, Object> data = new HashMap<>();
        data.put("isFollowing", isFollowing);
        data.put("followerCount", followerCount);
        data.put("followingCount", followingCount);
        return R.success(data);
    }

    /**
     * 获取用户的关注数和粉丝数
     */
    @GetMapping("/counts")
    public R<?> getFollowCounts(@RequestParam Long userId) {
        long followerCount = followService.getFollowerCount(userId);
        long followingCount = followService.getFollowingCount(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("followerCount", followerCount);
        data.put("followingCount", followingCount);
        return R.success(data);
    }

    /**
     * 获取用户的粉丝列表
     */
    @GetMapping("/followers/{userId}")
    public R<?> getFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId) {
        try {
            LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Follow::getFollowingId, userId);
            wrapper.orderByDesc(Follow::getCreateTime);
            List<Follow> follows = followService.list(wrapper);

            if (follows.isEmpty()) {
                return R.success(new ArrayList<>());
            }

            // 获取粉丝用户信息
            List<Long> followerIds = follows.stream()
                    .map(Follow::getFollowerId)
                    .collect(Collectors.toList());

            List<UserInfo> users = userMapper.selectBatchIds(followerIds);
            users.forEach(u -> u.setPassword(null));

            // 检查当前用户是否关注了这些人
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserInfo user : users) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("username", user.getUsername());
                item.put("avatar", user.getAvatar());
                item.put("bio", user.getBio());
                item.put("gender", user.getGender());
                item.put("region", user.getRegion());
                item.put("age", user.getAge());
                if (currentUserId != null) {
                    item.put("isFollowing", followService.isFollowing(currentUserId, user.getId()));
                } else {
                    item.put("isFollowing", false);
                }
                result.add(item);
            }

            return R.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取粉丝列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户关注的列表
     */
    @GetMapping("/following/{userId}")
    public R<?> getFollowing(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId) {
        try {
            LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Follow::getFollowerId, userId);
            wrapper.orderByDesc(Follow::getCreateTime);
            List<Follow> follows = followService.list(wrapper);

            if (follows.isEmpty()) {
                return R.success(new ArrayList<>());
            }

            // 获取被关注用户信息
            List<Long> followingIds = follows.stream()
                    .map(Follow::getFollowingId)
                    .collect(Collectors.toList());

            List<UserInfo> users = userMapper.selectBatchIds(followingIds);
            users.forEach(u -> u.setPassword(null));

            // 检查当前用户是否关注了这些人
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserInfo user : users) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", user.getId());
                item.put("username", user.getUsername());
                item.put("avatar", user.getAvatar());
                item.put("bio", user.getBio());
                item.put("gender", user.getGender());
                item.put("region", user.getRegion());
                item.put("age", user.getAge());
                if (currentUserId != null) {
                    item.put("isFollowing", followService.isFollowing(currentUserId, user.getId()));
                } else {
                    item.put("isFollowing", false);
                }
                result.add(item);
            }

            return R.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("获取关注列表失败: " + e.getMessage());
        }
    }
}
