package com.server.work.service;

import com.server.work.dto.WorkDTO;
import com.server.work.dto.WorkPageDTO;
import com.server.work.dto.WorkQuery;
import java.util.List;

public interface WorkService {

    /**
     * 分页查询作品列表
     */
    WorkPageDTO getWorkList(WorkQuery query, Long currentUserId);

    /**
     * 获取作品详情
     */
    WorkDTO getWorkDetail(Long id, Long currentUserId);

    /**
     * 创建作品
     */
    WorkDTO createWork(WorkDTO workDTO, Long userId);

    /**
     * 更新作品
     */
    WorkDTO updateWork(WorkDTO workDTO);

    /**
     * 删除作品
     */
    boolean deleteWork(Long id, Long userId);

    /**
     * 获取类型列表
     */
    List<String> getWorkTypes();

    /**
     * 增加浏览量
     */
    void increaseViewCount(Long id);

    /**
     * 点赞/取消点赞作品
     */
    boolean likeWork(Long workId, Long userId);

    /**
     * 检查用户是否点赞了作品
     */
    boolean isLiked(Long workId, Long userId);

    /**
     * 收藏/取消收藏作品
     */
    boolean collectWork(Long workId, Long userId);

    /**
     * 检查用户是否收藏了作品
     */
    boolean isCollected(Long workId, Long userId);
}
