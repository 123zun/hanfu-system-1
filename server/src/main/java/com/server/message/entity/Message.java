package com.server.message.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead = 0;
    private Integer senderDeleted = 0;
    private Integer receiverDeleted = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}