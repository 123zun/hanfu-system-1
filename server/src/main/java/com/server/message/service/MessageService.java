package com.server.message.service;

import com.server.message.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {
    // 发送消息
    boolean sendMessage(Long senderId, Long receiverId, String content);

    // 获取与某个用户的聊天记录
    List<Map<String, Object>> getMessages(Long userId, Long otherUserId);

    // 获取消息列表（与所有用户的最新消息，按时间排序）
    List<Map<String, Object>> getConversationList(Long userId);

    // 获取未读消息数
    int getUnreadCount(Long userId);

    // 标记消息为已读
    boolean markAsRead(Long userId, Long otherUserId);

    // 获取某个用户的未读消息数
    Map<Long, Integer> getUnreadCountByUser(Long userId);

    // 检查是否可以发送消息（需要互相关注，或者对方已回复过）
    boolean canSendMessage(Long senderId, Long receiverId);

    // 删除消息
    boolean deleteMessage(Long messageId, Long userId);
}