package com.server.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;           // 资源标题
    private String description;     // 资源描述
    private String type;            // 类型: document/video/image/other
    private String fileUrl;         // 文件URL
    private Long fileSize;          // 文件大小(字节)
    private Integer downloadCount = 0;  // 下载次数
    private Long uploaderId;         // 上传者ID
    private String uploaderName;    // 上传者名称

    private Integer status = 1;     // 状态: 0-审核中, 1-正常

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
