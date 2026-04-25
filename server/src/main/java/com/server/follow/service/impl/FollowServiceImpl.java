package com.server.follow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.follow.entity.Follow;
import com.server.follow.mapper.FollowMapper;
import com.server.follow.service.FollowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return false;
        }
        // 检查是否已经关注
        if (isFollowing(followerId, followingId)) {
            return false;
        }
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        return this.save(follow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfollow(Long followerId, Long followingId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
               .eq(Follow::getFollowingId, followingId);
        return this.remove(wrapper);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, followerId)
               .eq(Follow::getFollowingId, followingId);
        return this.count(wrapper) > 0;
    }

    @Override
    public long getFollowingCount(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowerId, userId);
        return this.count(wrapper);
    }

    @Override
    public long getFollowerCount(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowingId, userId);
        return this.count(wrapper);
    }
}
