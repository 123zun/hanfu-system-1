package com.server.follow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.follow.entity.Follow;

public interface FollowService extends IService<Follow> {

    /**
     * 关注用户
     */
    boolean follow(Long followerId, Long followingId);

    /**
     * 取消关注
     */
    boolean unfollow(Long followerId, Long followingId);

    /**
     * 检查是否已关注
     */
    boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取用户的关注数
     */
    long getFollowingCount(Long userId);

    /**
     * 获取用户的粉丝数
     */
    long getFollowerCount(Long userId);
}
