package com.server.message.controller;

import com.server.common.R;
import com.server.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    public R<?> sendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam String content) {
        boolean success = messageService.sendMessage(senderId, receiverId, content);
        if (success) {
            return R.success("发送成功");
        }
        return R.error("发送失败");
    }

    /**
     * 获取与某个用户的聊天记录
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public R<?> getMessages(
            @RequestParam Long userId,
            @RequestParam Long otherUserId) {
        // 标记消息为已读
        messageService.markAsRead(userId, otherUserId);

        List<Map<String, Object>> messages = messageService.getMessages(userId, otherUserId);
        return R.success(messages);
    }

    /**
     * 获取会话列表（与所有用户的最新消息）
     */
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public R<?> getConversationList(@RequestParam Long userId) {
        List<Map<String, Object>> conversations = messageService.getConversationList(userId);
        return R.success(conversations);
    }

    /**
     * 获取总未读消息数
     */
    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public R<?> getUnreadCount(@RequestParam Long userId) {
        int count = messageService.getUnreadCount(userId);
        return R.success(Map.of("unreadCount", count));
    }

    /**
     * 获取每个用户的未读消息数
     */
    @GetMapping("/unread/by-user")
    @PreAuthorize("isAuthenticated()")
    public R<?> getUnreadCountByUser(@RequestParam Long userId) {
        Map<Long, Integer> unreadMap = messageService.getUnreadCountByUser(userId);
        return R.success(unreadMap);
    }

    /**
     * 标记与某个用户的聊天为已读
     */
    @PostMapping("/read")
    @PreAuthorize("isAuthenticated()")
    public R<?> markAsRead(
            @RequestParam Long userId,
            @RequestParam Long otherUserId) {
        boolean success = messageService.markAsRead(userId, otherUserId);
        return R.success("已标记为已读");
    }

    /**
     * 检查是否可以发送消息
     */
    @GetMapping("/can-send")
    @PreAuthorize("isAuthenticated()")
    public R<?> canSendMessage(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {
        boolean canSend = messageService.canSendMessage(senderId, receiverId);
        return R.success(Map.of("canSend", canSend));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    public R<?> deleteMessage(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        boolean success = messageService.deleteMessage(messageId, userId);
        if (success) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }
}