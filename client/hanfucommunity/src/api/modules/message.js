import request from '../request'

// 发送消息
export const sendMessage = (senderId, receiverId, content) => {
    return request({
        url: '/message/send',
        method: 'POST',
        params: { senderId, receiverId, content }
    })
}

// 获取与某个用户的聊天记录
export const getMessageHistory = (userId, otherUserId) => {
    return request({
        url: '/message/history',
        method: 'GET',
        params: { userId, otherUserId }
    })
}

// 获取会话列表
export const getConversationList = (userId) => {
    return request({
        url: '/message/conversations',
        method: 'GET',
        params: { userId }
    })
}

// 获取总未读消息数
export const getUnreadCount = (userId) => {
    return request({
        url: '/message/unread',
        method: 'GET',
        params: { userId }
    })
}

// 获取每个用户的未读消息数
export const getUnreadCountByUser = (userId) => {
    return request({
        url: '/message/unread/by-user',
        method: 'GET',
        params: { userId }
    })
}

// 标记消息为已读
export const markAsRead = (userId, otherUserId) => {
    return request({
        url: '/message/read',
        method: 'POST',
        params: { userId, otherUserId }
    })
}

// 检查是否可以发送消息
export const canSendMessage = (senderId, receiverId) => {
    return request({
        url: '/message/can-send',
        method: 'GET',
        params: { senderId, receiverId }
    })
}

// 删除消息
export const deleteMessage = (messageId, userId) => {
    return request({
        url: `/message/${messageId}`,
        method: 'DELETE',
        params: { userId }
    })
}