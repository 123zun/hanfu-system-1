package com.server.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.activity.dto.ActivityDTO;
import com.server.activity.dto.ActivityPageDTO;
import com.server.activity.dto.ActivityQuery;
import com.server.activity.entity.Activity;
import com.server.activity.entity.ActivitySignup;
import com.server.activity.mapper.ActivityMapper;
import com.server.activity.mapper.ActivitySignupMapper;
import com.server.activity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivitySignupMapper signupMapper;

    @Override
    public ActivityPageDTO getActivityList(ActivityQuery query, Long currentUserId) {
        log.info("查询活动列表: {}", query);

        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Activity::getTitle, query.getKeyword().trim());
        }

        wrapper.orderByDesc(Activity::getCreateTime);

        // 先查全量
        List<Activity> allActivities = activityMapper.selectList(wrapper);

        // 手动分页
        int pageSize = query.getSize() != null ? query.getSize() : 9;
        int start = (query.getPage() - 1) * pageSize;
        int end = Math.min(start + pageSize, allActivities.size());
        List<Activity> pagedActivities = start < allActivities.size() ? allActivities.subList(start, end) : List.of();

        // 转换为DTO
        List<ActivityDTO> records = pagedActivities.stream()
                .map(activity -> convertToDTO(activity, currentUserId))
                .collect(Collectors.toList());

        // 如果需要筛选报名状态，在结果中过滤
        if ("registering".equals(query.getStatus())) {
            final LocalDate today = LocalDate.now();
            records = records.stream()
                    .filter(a -> !a.getRegistrationStartTime().isAfter(today) && !a.getRegistrationEndTime().isBefore(today))
                    .collect(Collectors.toList());
        } else if ("ended".equals(query.getStatus())) {
            final LocalDate today = LocalDate.now();
            records = records.stream()
                    .filter(a -> a.getRegistrationEndTime().isBefore(today))
                    .collect(Collectors.toList());
        }

        // 如果需要筛选是否已满
        if ("full".equals(query.getFull())) {
            records = records.stream()
                    .filter(a -> a.getCurrentParticipants() != null && a.getCurrentParticipants() >= a.getMaxParticipants())
                    .collect(Collectors.toList());
        } else if ("available".equals(query.getFull())) {
            records = records.stream()
                    .filter(a -> a.getCurrentParticipants() == null || a.getCurrentParticipants() < a.getMaxParticipants())
                    .collect(Collectors.toList());
        }

        long total = allActivities.size();
        long pages = (total + pageSize - 1) / pageSize;

        return new ActivityPageDTO(records, total, query.getPage(), pageSize, pages);
    }

    @Override
    public ActivityDTO getActivityDetail(Long id, Long currentUserId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return null;
        }
        return convertToDTO(activity, currentUserId);
    }

    @Override
    @Transactional
    public ActivityDTO createActivity(ActivityDTO dto) {
        log.info("创建活动: {}", dto.getTitle());

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        String coverImage = dto.getCoverImage();
        activity.setCoverImage(coverImage != null && !coverImage.trim().isEmpty()
                ? coverImage
                : "http://localhost:8080/default/zixundefault.png");
        activity.setMaxParticipants(dto.getMaxParticipants() != null ? dto.getMaxParticipants() : 50);
        activity.setSignupStartTime(dto.getRegistrationStartTime());
        activity.setSignupEndTime(dto.getRegistrationEndTime());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setLocation(dto.getLocation());
        activity.setOrganizerId(dto.getOrganizerId());
        activity.setCurrentParticipants(0);

        activityMapper.insert(activity);

        return convertToDTO(activity, null);
    }

    @Override
    @Transactional
    public ActivityDTO updateActivity(ActivityDTO dto) {
        log.info("更新活动: id={}", dto.getId());

        Activity activity = activityMapper.selectById(dto.getId());
        if (activity == null) {
            return null;
        }

        if (StringUtils.hasText(dto.getTitle())) {
            activity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            activity.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            activity.setLocation(dto.getLocation());
        }
        if (dto.getCoverImage() != null && !dto.getCoverImage().trim().isEmpty()) {
            activity.setCoverImage(dto.getCoverImage());
        }
        if (dto.getMaxParticipants() != null) {
            activity.setMaxParticipants(dto.getMaxParticipants());
        }
        if (dto.getRegistrationStartTime() != null) {
            activity.setSignupStartTime(dto.getRegistrationStartTime());
        }
        if (dto.getRegistrationEndTime() != null) {
            activity.setSignupEndTime(dto.getRegistrationEndTime());
        }
        if (dto.getStartTime() != null) {
            activity.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            activity.setEndTime(dto.getEndTime());
        }

        activityMapper.updateById(activity);

        return convertToDTO(activity, null);
    }

    @Override
    @Transactional
    public boolean deleteActivity(Long id) {
        log.info("逻辑删除活动: id={}", id);
        return activityMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean registerActivity(Long activityId, Long userId) {
        log.info("报名活动: activityId={}, userId={}", activityId, userId);

        // 检查是否已报名
        ActivitySignup existing = signupMapper.selectByActivityAndUser(activityId, userId);
        if (existing != null) {
            log.warn("用户已报名该活动");
            return false;
        }

        // 检查活动报名是否在有效期内
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (activity.getSignupStartTime() != null && today.isBefore(activity.getSignupStartTime())) {
            log.warn("报名还未开始");
            return false;
        }
        if (activity.getSignupEndTime() != null && today.isAfter(activity.getSignupEndTime())) {
            log.warn("报名已结束");
            return false;
        }

        // 检查是否已满
        int currentCount = signupMapper.countByActivityId(activityId);
        if (currentCount >= activity.getMaxParticipants()) {
            log.warn("报名已满");
            return false;
        }

        if (signupMapper.insertSignup(activityId, userId) > 0) {
            // 更新活动的当前参与人数
            Activity activityToUpdate = activityMapper.selectById(activityId);
            activityToUpdate.setCurrentParticipants(activityToUpdate.getCurrentParticipants() + 1);
            activityMapper.updateById(activityToUpdate);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelRegistration(Long activityId, Long userId) {
        ActivitySignup signup = signupMapper.selectByActivityAndUser(activityId, userId);
        if (signup == null) {
            return false;
        }
        if (signupMapper.deleteById(signup.getId()) > 0) {
            // 更新活动的当前参与人数
            Activity activity = activityMapper.selectById(activityId);
            activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - 1));
            activityMapper.updateById(activity);
            return true;
        }
        return false;
    }

    @Override
    public List<ActivitySignup> getParticipants(Long activityId) {
        return signupMapper.selectByActivityId(activityId);
    }

    @Override
    @Transactional
    public boolean addParticipants(Long activityId, List<Long> userIds) {
        log.info("批量添加参与人员: activityId={}, userIds={}", activityId, userIds);

        // 检查活动是否存在
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return false;
        }

        int currentCount = signupMapper.countByActivityId(activityId);
        int available = activity.getMaxParticipants() - currentCount;

        if (userIds.size() > available) {
            log.warn("添加人数超过可用名额");
            return false;
        }

        int addedCount = 0;
        for (Long userId : userIds) {
            // 检查是否已报名
            ActivitySignup existing = signupMapper.selectByActivityAndUser(activityId, userId);
            if (existing == null) {
                signupMapper.insertSignup(activityId, userId);
                addedCount++;
            }
        }

        // 更新活动的当前参与人数
        if (addedCount > 0) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() + addedCount);
            activityMapper.updateById(activity);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean removeParticipants(Long activityId, List<Long> signupIds) {
        log.info("删除参与人员: activityId={}, ids={}", activityId, signupIds);
        if (signupIds == null || signupIds.isEmpty()) {
            return false;
        }

        int removedCount = 0;
        for (Long id : signupIds) {
            ActivitySignup signup = signupMapper.selectById(id);
            if (signup != null) {
                signupMapper.deleteById(id);
                removedCount++;
            }
        }

        // 更新活动的当前参与人数
        if (removedCount > 0 && activityId != null) {
            Activity activity = activityMapper.selectById(activityId);
            if (activity != null) {
                activity.setCurrentParticipants(Math.max(0, activity.getCurrentParticipants() - removedCount));
                activityMapper.updateById(activity);
            }
        }

        return removedCount > 0;
    }

    @Override
    public List<ActivitySignup> getAllAvailableUsers() {
        return signupMapper.selectAllAvailableUsers();
    }

    /**
     * 转换Activity为ActivityDTO
     */
    private ActivityDTO convertToDTO(Activity activity, Long currentUserId) {
        ActivityDTO dto = new ActivityDTO();
        BeanUtils.copyProperties(activity, dto);

        // 处理字段名不一致的情况
        dto.setRegistrationStartTime(activity.getSignupStartTime());
        dto.setRegistrationEndTime(activity.getSignupEndTime());

        // 获取当前报名人数（使用数据库中的值）
        dto.setCurrentParticipants(activity.getCurrentParticipants());

        // 检查当前用户是否已报名
        if (currentUserId != null) {
            int count = signupMapper.countByActivityAndUser(activity.getId(), currentUserId);
            dto.setRegistered(count > 0);
        }

        return dto;
    }
}
