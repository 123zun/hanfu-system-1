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
     * @param silent 如果为true，不增加浏览量
     */
    WorkDTO getWorkDetail(Long id, Long currentUserId, Boolean silent);

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

    /**
     * 获取热门帖子（按浏览量排序）
     */
    List<WorkDTO> getHotWorks(int limit);

    /**
     * 统计活跃帖子数（排除已删除）
     */
    long countActiveWorks();

    /**
     * 获取用户收藏的作品列表
     */
    List<WorkDTO> getUserCollections(Long userId);

    /**
     * 审核作品
     * @param workId 作品ID
     * @param auditorId 审核人ID
     * @param approved 是否通过
     * @param reason 不通过原因
     * @return 是否成功
     */
    boolean auditWork(Long workId, Long auditorId, boolean approved, String reason);

    /**
     * 获取待审核作品列表（管理员用）
     */
    WorkPageDTO getPendingAuditList(WorkQuery query);
}
