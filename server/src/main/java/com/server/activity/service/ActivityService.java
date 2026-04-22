package com.server.activity.service;

import com.server.activity.dto.ActivityDTO;
import com.server.activity.dto.ActivityPageDTO;
import com.server.activity.dto.ActivityQuery;
import com.server.activity.entity.ActivitySignup;
import java.util.List;

public interface ActivityService {

    /**
     * 分页查询活动列表
     */
    ActivityPageDTO getActivityList(ActivityQuery query, Long currentUserId);

    /**
     * 获取活动详情
     * @param silent 如果为true，不增加浏览量
     */
    ActivityDTO getActivityDetail(Long id, Long currentUserId, Boolean silent);

    /**
     * 创建活动
     */
    ActivityDTO createActivity(ActivityDTO dto);

    /**
     * 更新活动
     */
    ActivityDTO updateActivity(ActivityDTO dto);

    /**
     * 删除活动
     */
    boolean deleteActivity(Long id);

    /**
     * 报名活动
     */
    boolean registerActivity(Long activityId, Long userId);

    /**
     * 取消报名
     */
    boolean cancelRegistration(Long activityId, Long userId);

    /**
     * 获取活动参与人员列表
     */
    List<ActivitySignup> getParticipants(Long activityId);

    /**
     * 新增参与人员（批量）
     */
    boolean addParticipants(Long activityId, List<Long> userIds);

    /**
     * 删除参与人员（批量）
     */
    boolean removeParticipants(Long activityId, List<Long> signupIds);

    /**
     * 获取所有可用用户
     */
    List<ActivitySignup> getAllAvailableUsers();

    /**
     * 统计活跃活动数（排除已删除）
     */
    long countActiveActivities();

    /**
     * 审核活动
     * @param activityId 活动ID
     * @param auditorId 审核人ID
     * @param approved 是否通过
     * @param reason 不通过原因
     * @return 是否成功
     */
    boolean auditActivity(Long activityId, Long auditorId, boolean approved, String reason);

    /**
     * 获取待审核活动列表（管理员用）
     */
    ActivityPageDTO getPendingAuditList(ActivityQuery query);
}
