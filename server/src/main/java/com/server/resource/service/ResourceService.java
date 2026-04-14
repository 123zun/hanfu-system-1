package com.server.resource.service;

import com.server.resource.dto.ResourceDTO;
import com.server.resource.dto.ResourcePageDTO;
import com.server.resource.dto.ResourceQuery;
import java.util.List;

public interface ResourceService {

    /**
     * 分页查询资源列表
     */
    ResourcePageDTO getResourceList(ResourceQuery query, Long currentUserId);

    /**
     * 获取资源详情
     */
    ResourceDTO getResourceDetail(Long id, Long currentUserId);

    /**
     * 创建资源
     */
    ResourceDTO createResource(ResourceDTO resourceDTO);

    /**
     * 更新资源
     */
    ResourceDTO updateResource(ResourceDTO resourceDTO);

    /**
     * 删除资源
     */
    boolean deleteResource(Long id);

    /**
     * 获取类型列表
     */
    List<String> getResourceCategories();

    /**
     * 增加下载次数
     */
    void increaseDownloadCount(Long id);

    /**
     * 点赞/取消点赞资源
     */
    boolean likeResource(Long resourceId, Long userId);

    /**
     * 检查用户是否点赞了资源
     */
    boolean isLiked(Long resourceId, Long userId);

    /**
     * 收藏/取消收藏资源
     */
    boolean collectResource(Long resourceId, Long userId);

    /**
     * 检查用户是否收藏了资源
     */
    boolean isCollected(Long resourceId, Long userId);
}
