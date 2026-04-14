package com.server.resource.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResourceDTO {

    private Long id;
    private String title;
    private String description;
    private String type;
    private String fileUrl;
    private Long fileSize;
    private String fileSizeFormat;  // 格式化后的文件大小
    private Integer downloadCount;
    private Long uploaderId;
    private String uploaderName;
    private Integer status;
    private LocalDateTime createTime;

    // 用户相关状态
    private Boolean liked;      // 是否点赞
    private Boolean collected;   // 是否收藏
}
