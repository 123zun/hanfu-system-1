package com.server.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.article.mapper.CollectionMapper;
import com.server.article.mapper.LikeMapper;
import com.server.entity.Collection;
import com.server.entity.Likes;
import com.server.resource.dto.ResourceDTO;
import com.server.resource.dto.ResourcePageDTO;
import com.server.resource.dto.ResourceQuery;
import com.server.resource.entity.Resource;
import com.server.resource.mapper.ResourceMapper;
import com.server.resource.service.ResourceService;
import com.server.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CollectionMapper collectionMapper;

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes.doubleValue();
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * 分页查询资源列表
     */
    @Override
    public ResourcePageDTO getResourceList(ResourceQuery query, Long currentUserId) {
        log.info("查询资源列表: {}", query);

        LambdaQueryWrapper<Resource> queryWrapper = new LambdaQueryWrapper<>();

        // 状态筛选（默认只查询正常的）
        queryWrapper.eq(Resource::getStatus, query.getStatus() != null ? query.getStatus() : 1);

        // 类型筛选
        if (StringUtils.hasText(query.getType())) {
            queryWrapper.eq(Resource::getType, query.getType());
        }

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            queryWrapper.and(wrapper -> wrapper
                    .like(Resource::getTitle, keyword)
                    .or()
                    .like(Resource::getDescription, keyword)
            );
        }

        // 排序
        if ("download_count".equals(query.getSortField())) {
            queryWrapper.orderByDesc(Resource::getDownloadCount);
        } else if ("create_time".equals(query.getSortField())) {
            if ("desc".equalsIgnoreCase(query.getSortOrder())) {
                queryWrapper.orderByDesc(Resource::getCreateTime);
            } else {
                queryWrapper.orderByAsc(Resource::getCreateTime);
            }
        } else {
            queryWrapper.orderByDesc(Resource::getCreateTime);
        }

        // 查询总数
        long total = this.count(queryWrapper);
        log.info("资源总数: total={}", total);

        // 手动分页
        List<Resource> allResources = this.list(queryWrapper);
        int start = (query.getPage() - 1) * query.getSize();
        int end = Math.min(start + query.getSize(), allResources.size());
        List<Resource> pagedResources = start < allResources.size() ? allResources.subList(start, end) : List.of();

        // 转换为DTO
        List<ResourceDTO> records = pagedResources.stream()
                .map(resource -> convertToDTO(resource, currentUserId))
                .collect(Collectors.toList());

        long pages = (total + query.getSize() - 1) / query.getSize();
        return new ResourcePageDTO(records, total, query.getPage(), query.getSize(), pages);
    }

    /**
     * 获取资源详情
     */
    @Override
    public ResourceDTO getResourceDetail(Long id, Long currentUserId) {
        log.info("获取资源详情: id={}", id);
        Resource resource = this.getById(id);
        if (resource == null) {
            return null;
        }
        return convertToDTO(resource, currentUserId);
    }

    /**
     * 创建资源
     */
    @Override
    @Transactional
    public ResourceDTO createResource(ResourceDTO resourceDTO) {
        log.info("创建资源: {}", resourceDTO);

        Resource resource = new Resource();
        BeanUtils.copyProperties(resourceDTO, resource);

        resource.setStatus(1);
        resource.setDownloadCount(0);
        resource.setCreateTime(LocalDateTime.now());

        boolean saved = this.save(resource);

        if (saved) {
            log.info("资源创建成功: id={}", resource.getId());
            resourceDTO.setId(resource.getId());
            return resourceDTO;
        }

        return null;
    }

    /**
     * 更新资源 - 需要是本人或管理员
     */
    @Override
    @Transactional
    public ResourceDTO updateResource(ResourceDTO resourceDTO) {
        log.info("更新资源: id={}", resourceDTO.getId());

        Resource existingResource = this.getById(resourceDTO.getId());
        if (existingResource == null) {
            log.error("资源不存在: id={}", resourceDTO.getId());
            return null;
        }

        // 检查权限：必须是本人或管理员
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !existingResource.getUploaderId().equals(currentUserId)) {
            log.warn("无权更新资源: resourceId={}, currentUserId={}, uploaderId={}",
                    resourceDTO.getId(), currentUserId, existingResource.getUploaderId());
            return null;
        }

        if (StringUtils.hasText(resourceDTO.getTitle())) {
            existingResource.setTitle(resourceDTO.getTitle());
        }
        if (StringUtils.hasText(resourceDTO.getDescription())) {
            existingResource.setDescription(resourceDTO.getDescription());
        }
        if (StringUtils.hasText(resourceDTO.getType())) {
            existingResource.setType(resourceDTO.getType());
        }
        if (StringUtils.hasText(resourceDTO.getFileUrl())) {
            existingResource.setFileUrl(resourceDTO.getFileUrl());
        }
        if (resourceDTO.getFileSize() != null) {
            existingResource.setFileSize(resourceDTO.getFileSize());
        }
        if (resourceDTO.getStatus() != null) {
            existingResource.setStatus(resourceDTO.getStatus());
        }

        boolean updated = this.updateById(existingResource);

        if (updated) {
            log.info("资源更新成功: id={}", existingResource.getId());
            return resourceDTO;
        }

        return null;
    }

    /**
     * 删除资源 - 需要是本人或管理员
     */
    @Override
    @Transactional
    public boolean deleteResource(Long id) {
        log.info("删除资源: id={}", id);

        Resource resource = this.getById(id);
        if (resource == null) {
            log.error("资源不存在: id={}", id);
            return false;
        }

        // 检查权限：必须是本人或管理员
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.isAdmin() && !resource.getUploaderId().equals(currentUserId)) {
            log.warn("无权删除资源: resourceId={}, currentUserId={}, uploaderId={}",
                    id, currentUserId, resource.getUploaderId());
            return false;
        }

        // 软删除：将状态改为0
        resource.setStatus(0);
        return this.updateById(resource);
    }

    /**
     * 获取类型列表（固定四种类型）
     */
    @Override
    public List<String> getResourceCategories() {
        log.info("获取资源类型列表");
        return List.of("image", "video", "document", "other");
    }

    /**
     * 增加下载次数
     */
    @Override
    @Transactional
    public void increaseDownloadCount(Long id) {
        log.info("增加资源下载次数: id={}", id);
        this.baseMapper.increaseDownloadCount(id);
    }

    /**
     * 转换为DTO
     */
    private ResourceDTO convertToDTO(Resource resource, Long currentUserId) {
        ResourceDTO dto = new ResourceDTO();
        BeanUtils.copyProperties(resource, dto);
        dto.setFileSizeFormat(formatFileSize(resource.getFileSize()));

        if (currentUserId != null) {
            dto.setLiked(isLiked(resource.getId(), currentUserId));
            dto.setCollected(isCollected(resource.getId(), currentUserId));
        }

        return dto;
    }

    /**
     * 点赞/取消点赞资源
     */
    @Override
    @Transactional
    public boolean likeResource(Long resourceId, Long userId) {
        log.info("点赞/取消点赞资源: resourceId={}, userId={}", resourceId, userId);

        Likes existingLike = likeMapper.getLikeRecord(userId, "resource", resourceId);

        if (existingLike == null) {
            Likes like = new Likes();
            like.setUserId(userId);
            like.setTargetType("resource");
            like.setTargetId(resourceId);
            like.setStatus(0);
            int inserted = likeMapper.insert(like);
            return inserted > 0;
        } else if (existingLike.getStatus() == 0) {
            existingLike.setStatus(1);
            return likeMapper.updateById(existingLike) > 0;
        } else {
            existingLike.setStatus(0);
            return likeMapper.updateById(existingLike) > 0;
        }
    }

    /**
     * 检查用户是否点赞了资源
     */
    @Override
    public boolean isLiked(Long resourceId, Long userId) {
        if (userId == null) return false;
        return likeMapper.checkLiked(userId, "resource", resourceId) > 0;
    }

    /**
     * 收藏/取消收藏资源
     */
    @Override
    @Transactional
    public boolean collectResource(Long resourceId, Long userId) {
        log.info("收藏/取消收藏资源: resourceId={}, userId={}", resourceId, userId);

        Collection existingCollection = collectionMapper.getCollectionRecord(userId, "resource", resourceId);

        if (existingCollection == null) {
            Collection collection = new Collection();
            collection.setUserId(userId);
            collection.setTargetType("resource");
            collection.setTargetId(resourceId);
            collection.setStatus(0);
            int inserted = collectionMapper.insert(collection);
            return inserted > 0;
        } else if (existingCollection.getStatus() == 0) {
            existingCollection.setStatus(1);
            return collectionMapper.updateById(existingCollection) > 0;
        } else {
            existingCollection.setStatus(0);
            return collectionMapper.updateById(existingCollection) > 0;
        }
    }

    /**
     * 检查用户是否收藏了资源
     */
    @Override
    public boolean isCollected(Long resourceId, Long userId) {
        if (userId == null) return false;
        return collectionMapper.checkCollected(userId, "resource", resourceId) > 0;
    }
}
