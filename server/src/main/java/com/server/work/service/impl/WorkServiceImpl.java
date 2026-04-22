package com.server.work.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.article.mapper.CollectionMapper;
import com.server.article.mapper.CommentMapper;
import com.server.article.mapper.LikeMapper;
import com.server.entity.Collection;
import com.server.entity.Likes;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import com.server.work.dto.WorkDTO;
import com.server.work.dto.WorkPageDTO;
import com.server.work.dto.WorkQuery;
import com.server.work.entity.Work;
import com.server.work.mapper.WorkMapper;
import com.server.work.service.WorkService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public WorkPageDTO getWorkList(WorkQuery query, Long currentUserId) {
        log.info("查询作品列表: {}", query);

        LambdaQueryWrapper<Work> queryWrapper = new LambdaQueryWrapper<>();

        // 状态筛选（只查询已发布的）
        queryWrapper.eq(Work::getStatus, 1);
        // 排除审核不通过的作品
        queryWrapper.ne(Work::getAuditStatus, 2);

        // 类型筛选
        if (StringUtils.hasText(query.getType())) {
            queryWrapper.eq(Work::getType, query.getType());
        }

        // 用户筛选
        if (query.getUserId() != null) {
            queryWrapper.eq(Work::getUserId, query.getUserId());
        }

        // 关键词搜索（标题、描述）
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(Work::getTitle, keyword)
                    .or()
                    .like(Work::getDescription, keyword)
            );
        }

        // 排序
        if ("hottest".equals(query.getSort())) {
            queryWrapper.orderByDesc(Work::getViews);
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(Work::getCreateTime);
        }

        // 先查总数
        long total = workMapper.selectCount(queryWrapper);
        log.info("作品总数: total={}", total);

        // 再查全量后手动分页
        List<Work> allWorks = workMapper.selectList(queryWrapper);
        int pageSize = query.getSize() != null ? query.getSize() : 12;
        int start = (query.getPage() - 1) * pageSize;
        int end = Math.min(start + pageSize, allWorks.size());
        List<Work> pagedWorks = start < allWorks.size() ? allWorks.subList(start, end) : List.of();

        // 转换为DTO
        List<WorkDTO> records = pagedWorks.stream()
                .map(work -> convertToDTO(work, currentUserId))
                .collect(Collectors.toList());

        long pages = (total + pageSize - 1) / pageSize;
        return new WorkPageDTO(records, total, query.getPage(), pageSize, pages);
    }

    @Override
    public WorkDTO getWorkDetail(Long id, Long currentUserId, Boolean silent) {
        log.info("获取作品详情: id={}, silent={}", id, silent);

        Work work = workMapper.selectById(id);
        if (work == null) {
            return null;
        }

        WorkDTO dto = convertToDTO(work, currentUserId);

        // silent=true时不增加浏览量（用于审核模块查看详情）
        if (silent == null || !silent) {
            workMapper.increaseViews(id);
        }

        return dto;
    }

    @Override
    @Transactional
    public WorkDTO createWork(WorkDTO workDTO, Long userId) {
        log.info("创建作品: userId={}", userId);

        // 获取用户信息
        UserInfo user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }

        Work work = new Work();
        work.setTitle(workDTO.getTitle());
        work.setDescription(workDTO.getDescription());
        work.setType(workDTO.getType());
        work.setUserId(userId);
        work.setCoverImage(workDTO.getCoverImage() != null && !workDTO.getCoverImage().isEmpty() ? workDTO.getCoverImage() : "http://localhost:8080/default/tiezidefault.png");
        work.setImages(toJson(workDTO.getImages()));
        work.setTags(toJson(workDTO.getTags()));
        work.setViews(0);
        work.setLikes(0);
        work.setComments(0);
        work.setStatus(1);

        workMapper.insert(work);

        return convertToDTO(work, userId);
    }

    @Override
    @Transactional
    public WorkDTO updateWork(WorkDTO workDTO) {
        log.info("更新作品: id={}", workDTO.getId());

        Work work = workMapper.selectById(workDTO.getId());
        if (work == null) {
            return null;
        }

        if (StringUtils.hasText(workDTO.getTitle())) {
            work.setTitle(workDTO.getTitle());
        }
        if (StringUtils.hasText(workDTO.getDescription())) {
            work.setDescription(workDTO.getDescription());
        }
        if (StringUtils.hasText(workDTO.getType())) {
            work.setType(workDTO.getType());
        }
        if (workDTO.getCoverImage() != null && !workDTO.getCoverImage().isEmpty()) {
            work.setCoverImage(workDTO.getCoverImage());
        } else {
            work.setCoverImage("http://localhost:8080/default/tiezidefault.png");
        }
        if (workDTO.getImages() != null) {
            work.setImages(toJson(workDTO.getImages()));
        }
        if (workDTO.getTags() != null) {
            work.setTags(toJson(workDTO.getTags()));
        }

        workMapper.updateById(work);
        return convertToDTO(work, null);
    }

    @Override
    @Transactional
    public boolean deleteWork(Long id, Long userId) {
        log.info("删除作品: id={}, userId={}", id, userId);

        Work work = workMapper.selectById(id);
        if (work == null) {
            return false;
        }

        // 只能删除自己的作品
        if (!work.getUserId().equals(userId)) {
            return false;
        }

        // 软删除（设置为审核不通过）
        work.setStatus(0);
        return workMapper.updateById(work) > 0;
    }

    @Override
    public List<String> getWorkTypes() {
        return workMapper.selectTypes();
    }

    @Override
    public void increaseViewCount(Long id) {
        workMapper.increaseViews(id);
    }

    @Override
    @Transactional
    public boolean likeWork(Long workId, Long userId) {
        log.info("点赞/取消点赞作品: workId={}, userId={}", workId, userId);

        // 检查是否已经点赞
        Likes existingLike = likeMapper.getLikeRecord(userId, "work", workId);

        if (existingLike == null) {
            // 未点赞，新增点赞记录
            Likes like = new Likes();
            like.setUserId(userId);
            like.setTargetType("work");
            like.setTargetId(workId);
            like.setStatus(0);
            int inserted = likeMapper.insert(like);

            if (inserted > 0) {
                workMapper.increaseLikes(workId);
                return true;
            }
        } else if (existingLike.getStatus() == 0) {
            // 已点赞，取消点赞
            existingLike.setStatus(1);
            int updated = likeMapper.updateById(existingLike);

            if (updated > 0) {
                workMapper.decreaseLikes(workId);
                return true;
            }
        } else {
            // 已取消点赞，重新点赞
            existingLike.setStatus(0);
            int updated = likeMapper.updateById(existingLike);

            if (updated > 0) {
                workMapper.increaseLikes(workId);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isLiked(Long workId, Long userId) {
        if (userId == null) return false;
        int count = likeMapper.checkLiked(userId, "work", workId);
        return count > 0;
    }

    @Override
    @Transactional
    public boolean collectWork(Long workId, Long userId) {
        log.info("收藏/取消收藏作品: workId={}, userId={}", workId, userId);

        Collection existing = collectionMapper.getCollectionRecord(userId, "work", workId);

        if (existing == null) {
            // 未收藏，新增收藏记录
            Collection collection = new Collection();
            collection.setUserId(userId);
            collection.setTargetType("work");
            collection.setTargetId(workId);
            collection.setStatus(0);
            int inserted = collectionMapper.insert(collection);

            return inserted > 0;
        } else if (existing.getStatus() == 0) {
            // 已收藏，取消收藏
            existing.setStatus(1);
            return collectionMapper.updateById(existing) > 0;
        } else {
            // 已取消收藏，重新收藏
            existing.setStatus(0);
            return collectionMapper.updateById(existing) > 0;
        }
    }

    @Override
    public boolean isCollected(Long workId, Long userId) {
        if (userId == null) return false;
        int count = collectionMapper.checkCollected(userId, "work", workId);
        return count > 0;
    }

    /**
     * 转换Work为WorkDTO
     */
    private WorkDTO convertToDTO(Work work, Long currentUserId) {
        WorkDTO dto = new WorkDTO();
        BeanUtils.copyProperties(work, dto);

        // 解析images和tags的JSON
        dto.setImages(fromJson(work.getImages(), new TypeReference<List<String>>() {}));
        dto.setTags(fromJson(work.getTags(), new TypeReference<List<String>>() {}));

        // 动态获取评论数（统计所有status=1的评论，包含子评论）
        dto.setComments(commentMapper.countWorkComments(work.getId()));

        // 获取用户信息
        if (work.getUserId() != null) {
            UserInfo user = userMapper.selectById(work.getUserId());
            if (user != null) {
                dto.setUserName(user.getUsername());
                dto.setUserAvatar(user.getAvatar());
            }
        }

        // 检查点赞和收藏状态
        if (currentUserId != null) {
            dto.setLiked(isLiked(work.getId(), currentUserId));
            dto.setCollected(isCollected(work.getId(), currentUserId));
        }

        return dto;
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("JSON反序列化失败: {}", json, e);
            return null;
        }
    }

    @Override
    public List<WorkDTO> getHotWorks(int limit) {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getStatus, 1)
               .ne(Work::getAuditStatus, 2)
               .orderByDesc(Work::getViews)
               .last("LIMIT " + limit);
        List<Work> works = workMapper.selectList(wrapper);
        return works.stream().map(w -> convertToDTO(w, null)).toList();
    }

    @Override
    public long countActiveWorks() {
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Work::getStatus, 1)
               .ne(Work::getAuditStatus, 2);
        return workMapper.selectCount(wrapper);
    }

    @Override
    public List<WorkDTO> getUserCollections(Long userId) {
        if (userId == null) return List.of();
        LambdaQueryWrapper<com.server.entity.Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.server.entity.Collection::getUserId, userId)
               .eq(com.server.entity.Collection::getTargetType, "work")
               .eq(com.server.entity.Collection::getStatus, 0);
        List<com.server.entity.Collection> collections = collectionMapper.selectList(wrapper);
        List<WorkDTO> result = new java.util.ArrayList<>();
        for (com.server.entity.Collection c : collections) {
            Work work = workMapper.selectById(c.getTargetId());
            if (work != null && work.getStatus() == 1 && work.getAuditStatus() != 2) {
                result.add(convertToDTO(work, userId));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public boolean auditWork(Long workId, Long auditorId, boolean approved, String reason) {
        log.info("审核作品: workId={}, auditorId={}, approved={}, reason={}", workId, auditorId, approved, reason);

        Work work = workMapper.selectById(workId);
        if (work == null) {
            log.error("作品不存在: workId={}", workId);
            return false;
        }

        if (approved) {
            // 通过：设置审核状态为已通过，恢复显示
            work.setAuditStatus(1);
            work.setStatus(1);
        } else {
            // 不通过：设置审核状态为已拒绝，逻辑删除
            work.setAuditStatus(2);
            work.setStatus(0);
        }
        work.setAuditReason(reason);
        work.setAuditorId(auditorId);
        work.setAuditTime(java.time.LocalDateTime.now());

        int updated = workMapper.updateById(work);
        return updated > 0;
    }

    @Override
    public WorkPageDTO getPendingAuditList(WorkQuery query) {
        log.info("查询待审核作品列表: {}", query);

        LambdaQueryWrapper<Work> queryWrapper = new LambdaQueryWrapper<>();

        // 查询所有作品，不限制审核状态
        queryWrapper.orderByAsc(Work::getAuditStatus)
                    .orderByDesc(Work::getCreateTime);

        // 先查总数
        long total = workMapper.selectCount(queryWrapper);

        // 再查全量后手动分页
        List<Work> allWorks = workMapper.selectList(queryWrapper);
        int pageSize = query.getSize() != null ? query.getSize() : 12;
        int start = (query.getPage() - 1) * pageSize;
        int end = Math.min(start + pageSize, allWorks.size());
        List<Work> pagedWorks = start < allWorks.size() ? allWorks.subList(start, end) : List.of();

        // 转换为DTO
        List<WorkDTO> records = pagedWorks.stream()
                .map(work -> convertToDTO(work, null))
                .collect(Collectors.toList());

        long pages = (total + pageSize - 1) / pageSize;
        return new WorkPageDTO(records, total, query.getPage(), pageSize, pages);
    }
}
