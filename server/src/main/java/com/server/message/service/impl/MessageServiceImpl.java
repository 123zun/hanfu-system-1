package com.server.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.follow.service.FollowService;
import com.server.message.entity.Message;
import com.server.message.mapper.MessageMapper;
import com.server.message.service.MessageService;
import com.server.user.entity.UserInfo;
import com.server.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            return false;
        }
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content.trim());
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());

        return messageMapper.insert(message) > 0;
    }

    @Override
    public List<Map<String, Object>> getMessages(Long userId, Long otherUserId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .eq(Message::getSenderId, userId).eq(Message::getReceiverId, otherUserId)
            .or()
            .eq(Message::getSenderId, otherUserId).eq(Message::getReceiverId, userId)
        );
        wrapper.and(w -> w
            .eq(Message::getSenderDeleted, 0).or().isNull(Message::getSenderDeleted)
        );
        wrapper.and(w -> w
            .eq(Message::getReceiverDeleted, 0).or().isNull(Message::getReceiverDeleted)
        );
        wrapper.orderByAsc(Message::getCreateTime);

        List<Message> messages = messageMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", msg.getId());
            item.put("senderId", msg.getSenderId());
            item.put("receiverId", msg.getReceiverId());
            item.put("content", msg.getContent());
            item.put("isRead", msg.getIsRead());
            item.put("createTime", msg.getCreateTime());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getConversationList(Long userId) {
        // 获取所有涉及该用户的聊天记录
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
            .eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId)
        );
        wrapper.eq(Message::getSenderDeleted, 0);
        wrapper.eq(Message::getReceiverDeleted, 0);
        wrapper.orderByDesc(Message::getCreateTime);

        List<Message> messages = messageMapper.selectList(wrapper);

        // 按对话分组（以对方用户ID分组）
        Map<Long, Message> latestMessageMap = new LinkedHashMap<>();
        for (Message msg : messages) {
            Long otherUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();
            // 只保留最新的消息
            if (!latestMessageMap.containsKey(otherUserId)) {
                latestMessageMap.put(otherUserId, msg);
            }
        }

        // 获取每个对话的未读数
        Map<Long, Integer> unreadMap = getUnreadCountByUser(userId);

        // 构建返回列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestMessageMap.entrySet()) {
            Long otherUserId = entry.getKey();
            Message latestMsg = entry.getValue();
            UserInfo userInfo = userMapper.selectById(otherUserId);
            if (userInfo == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("userId", otherUserId);
            item.put("username", userInfo.getUsername());
            item.put("avatar", userInfo.getAvatar());
            item.put("lastMessage", latestMsg.getContent());
            item.put("lastMessageTime", latestMsg.getCreateTime());
            item.put("unreadCount", unreadMap.getOrDefault(otherUserId, 0));
            item.put("isFromMe", latestMsg.getSenderId().equals(userId));

            // 检查是否有未读消息（对方发送的未读）
            int unread = unreadMap.getOrDefault(otherUserId, 0);
            item.put("hasUnread", unread > 0);

            result.add(item);
        }

        // 排序：优先未读，然后按最后消息时间
        result.sort((a, b) -> {
            boolean aUnread = (Boolean) a.getOrDefault("hasUnread", false);
            boolean bUnread = (Boolean) b.getOrDefault("hasUnread", false);
            if (aUnread && !bUnread) return -1;
            if (!aUnread && bUnread) return 1;

            LocalDateTime aTime = (LocalDateTime) a.get("lastMessageTime");
            LocalDateTime bTime = (LocalDateTime) b.get("lastMessageTime");
            if (aTime == null) return 1;
            if (bTime == null) return -1;
            return bTime.compareTo(aTime);
        });

        return result;
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.eq(Message::getReceiverDeleted, 0);
        return messageMapper.selectCount(wrapper).intValue();
    }

    @Override
    public boolean markAsRead(Long userId, Long otherUserId) {
        // 标记用户收到的来自otherUser的消息为已读
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getSenderId, otherUserId);
        wrapper.eq(Message::getIsRead, 0);

        List<Message> messages = messageMapper.selectList(wrapper);
        for (Message msg : messages) {
            msg.setIsRead(1);
            messageMapper.updateById(msg);
        }
        return true;
    }

    @Override
    public Map<Long, Integer> getUnreadCountByUser(Long userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId);
        wrapper.eq(Message::getIsRead, 0);
        wrapper.eq(Message::getReceiverDeleted, 0);

        List<Message> messages = messageMapper.selectList(wrapper);

        // 按发送者分组
        Map<Long, Integer> result = new HashMap<>();
        for (Message msg : messages) {
            Long senderId = msg.getSenderId();
            result.put(senderId, result.getOrDefault(senderId, 0) + 1);
        }
        return result;
    }

    @Override
    public boolean canSendMessage(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) return false;

        // 检查是否互相关注
        boolean isFollowing = followService.isFollowing(senderId, receiverId);
        boolean isFollowed = followService.isFollowing(receiverId, senderId);

        if (isFollowing && isFollowed) {
            // 互相关注，可以无限发送
            return true;
        }

        if (isFollowing && !isFollowed) {
            // 单方面关注（我关注了对方，对方没关注我），可以发送一条消息
            // 检查是否已经发送过第一条消息（即检查对方是否回复过）
            LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Message::getSenderId, receiverId);
            wrapper.eq(Message::getReceiverId, senderId);
            wrapper.eq(Message::getSenderDeleted, 0);
            wrapper.eq(Message::getReceiverDeleted, 0);

            long count = messageMapper.selectCount(wrapper);
            // 如果对方已回复，可以继续发送
            if (count > 0) return true;

            // 如果还没回复，检查sender是否已经发过一条消息（单向发送过消息）
            LambdaQueryWrapper<Message> sentWrapper = new LambdaQueryWrapper<>();
            sentWrapper.eq(Message::getSenderId, senderId);
            sentWrapper.eq(Message::getReceiverId, receiverId);
            sentWrapper.eq(Message::getSenderDeleted, 0);
            sentWrapper.eq(Message::getReceiverDeleted, 0);
            long sentCount = messageMapper.selectCount(sentWrapper);

            // 已经发过消息了（第一条），就不能再发了，要等对方回复
            if (sentCount > 0) return false;

            // 还没发过，可以发第一条
            return true;
        }

        // 互未关注，不能发送
        return false;
    }

    @Override
    public boolean deleteMessage(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) return false;

        if (message.getSenderId().equals(userId)) {
            message.setSenderDeleted(1);
        } else if (message.getReceiverId().equals(userId)) {
            message.setReceiverDeleted(1);
        } else {
            return false;
        }

        return messageMapper.updateById(message) > 0;
    }
}