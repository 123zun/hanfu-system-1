<template>
  <div class="messages-view">
    <div class="messages-container">
      <!-- 左侧：用户列表 -->
      <div class="conversations-panel">
        <div class="panel-header">
          <h3>私信</h3>
        </div>
        <div class="conversations-list" v-loading="loadingConversations">
          <div v-if="conversations.length === 0 && !loadingConversations" class="empty-state">
            <el-empty description="暂无私信" :image-size="60" />
          </div>
          <div
              v-else
              v-for="conv in conversations"
              :key="conv.userId"
              class="conversation-item"
              :class="{ active: selectedUserId === conv.userId, 'has-unread': conv.hasUnread }"
              @click="selectConversation(conv)"
          >
            <div class="user-avatar-wrapper">
              <el-avatar :size="48" :src="getImageUrl(conv.avatar)" />
              <span v-if="conv.hasUnread" class="unread-badge">{{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}</span>
            </div>
            <div class="conversation-info">
              <div class="conversation-header">
                <span class="username">{{ conv.username }}</span>
                <span class="time">{{ formatTime(conv.lastMessageTime) }}</span>
              </div>
              <div class="last-message">
                <span v-if="conv.isFromMe" class="from-me">我：</span>
                <span class="message-content">{{ conv.lastMessage }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：消息列表 -->
      <div class="messages-panel">
        <div v-if="!selectedUserId" class="no-selection">
          <el-empty description="请选择要查看的对话" :image-size="80" />
        </div>
        <template v-else>
          <div class="messages-header">
            <el-button text @click="goBack" class="back-btn">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <el-avatar :size="40" :src="getImageUrl(selectedConversation?.avatar)" />
            <span class="selected-username">{{ selectedConversation?.username }}</span>
          </div>

          <div class="messages-list" ref="messagesListRef" v-loading="loadingMessages">
            <div v-if="messages.length === 0 && !loadingMessages" class="empty-messages">
              <p>暂无消息记录，开始聊天吧~</p>
            </div>
            <div
                v-else
                v-for="msg in messages"
                :key="msg.id"
                class="message-item"
                :class="{ mine: String(msg.senderId) === String(currentUserId) }"
            >
              <div class="message-bubble">
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-meta">
                  <span class="time">{{ formatTime(msg.createTime) }}</span>
                  <span v-if="String(msg.senderId) === String(currentUserId)" class="read-status">
                    {{ msg.isRead === 1 ? '已读' : '未读' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="message-input-area">
            <el-input
                v-model="messageContent"
                type="textarea"
                :rows="3"
                placeholder="输入消息..."
                @keydown.enter.ctrl="handleSend"
            />
            <el-button
                type="primary"
                :disabled="!messageContent.trim()"
                :loading="sending"
                @click="handleSend"
                class="send-btn"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Promotion, ArrowLeft } from '@element-plus/icons-vue'
import {
  getConversationList,
  getMessageHistory,
  sendMessage,
  markAsRead,
  getUnreadCount
} from '@/api/modules/message'
import { getFollowingList } from '@/api/modules/user'

const route = useRoute()
const router = useRouter()

const currentUserId = ref(null)
const conversations = ref([])
const messages = ref([])
const selectedUserId = ref(null)
const selectedConversation = ref(null)
const messageContent = ref('')
const sending = ref(false)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const messagesListRef = ref(null)

// 获取对话列表
const loadConversations = async () => {
  if (!currentUserId.value) return
  loadingConversations.value = true
  try {
    const res = await getConversationList(currentUserId.value)
    if (res && res.code === 200) {
      conversations.value = res.data || []
    }

    // 也加载关注列表，添加没有消息记录的已关注用户
    try {
      const followingRes = await getFollowingList(currentUserId.value)
      if (followingRes && followingRes.code === 200) {
        const followingUsers = followingRes.data || []
        // 将已关注但没有消息记录的用户也添加到列表
        for (const user of followingUsers) {
          const exists = conversations.value.find(c => String(c.userId) === String(user.id))
          if (!exists) {
            conversations.value.push({
              userId: user.id,
              username: user.username,
              avatar: user.avatar,
              lastMessage: '',
              lastMessageTime: null,
              unreadCount: 0,
              isFromMe: false,
              hasUnread: false,
              isFollowing: true
            })
          }
        }
        // 按最后消息时间排序（有消息的排前面），然后按用户名
        conversations.value.sort((a, b) => {
          if (!a.lastMessageTime && !b.lastMessageTime) return a.username.localeCompare(b.username)
          if (!a.lastMessageTime) return 1
          if (!b.lastMessageTime) return -1
          return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
        })
      }
    } catch (e) {
      console.error('加载关注列表失败:', e)
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loadingConversations.value = false
  }
}

// 选择会话
const selectConversation = async (conv) => {
  selectedUserId.value = conv.userId
  selectedConversation.value = conv

  // 加载消息历史
  await loadMessages()

  // 标记已读
  await markAsRead(currentUserId.value, conv.userId)

  // 重新加载会话列表（更新未读状态）
  await loadConversations()
}

// 加载消息历史
const loadMessages = async () => {
  if (!selectedUserId.value) return
  loadingMessages.value = true
  try {
    const res = await getMessageHistory(currentUserId.value, selectedUserId.value)
    if (res && res.code === 200) {
      messages.value = res.data || []
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loadingMessages.value = false
  }
}

// 发送消息
const handleSend = async () => {
  if (!messageContent.value.trim()) return

  sending.value = true
  try {
    const res = await sendMessage(currentUserId.value, selectedUserId.value, messageContent.value)
    if (res && res.code === 200) {
      messageContent.value = ''
      await loadMessages()
      await loadConversations()
    } else {
      ElMessage.error(res?.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesListRef.value) {
    messagesListRef.value.scrollTop = messagesListRef.value.scrollHeight
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const oneDay = 24 * 60 * 60 * 1000

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < oneDay) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 7 * oneDay) return `${Math.floor(diff / oneDay)}天前`

  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return 'http://localhost:8080/uploads/avatars/default.png'
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `http://localhost:8080${path}`
  return `http://localhost:8080/${path}`
}

onMounted(async () => {
  currentUserId.value = localStorage.getItem('current_user_id')
  if (!currentUserId.value) {
    ElMessage.error('请先登录')
    return
  }

  // 加载会话列表
  await loadConversations()

  // 如果URL有userId参数，直接打开对应会话
  const userIdParam = route.query.userId
  if (userIdParam) {
    const conv = conversations.value.find(c => String(c.userId) === String(userIdParam))
    if (conv) {
      await selectConversation(conv)
    } else {
      // 如果没有历史记录，创建一个虚拟会话并显示消息面板
      selectedUserId.value = parseInt(userIdParam)
      selectedConversation.value = { userId: userIdParam, username: '用户', avatar: '' }
      // 显示消息面板（因为selectedUserId已经有值）
      await loadMessages()
      await markAsRead(currentUserId.value, parseInt(userIdParam))
    }
  } else if (conversations.value.length > 0) {
    // 默认选中第一个用户
    await selectConversation(conversations.value[0])
  }
})
</script>

<style scoped>
.messages-view {
  height: calc(100vh - 60px);
  padding: 0;
  background: #f5f5f5;
}

.messages-container {
  display: flex;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.conversations-panel {
  width: 320px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  padding: 40px 20px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.conversation-item:hover {
  background: #f8f5f0;
}

.conversation-item.active {
  background: #f0e6d6;
}

.conversation-item.has-unread {
  background: #fffbe6;
}

.user-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #ff4d4f;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.username {
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 12px;
  color: #999;
}

.last-message {
  display: flex;
  align-items: center;
}

.last-message .from-me {
  color: #999;
  font-size: 12px;
}

.message-content {
  color: #666;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.messages-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.no-selection {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.messages-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
}

.back-btn {
  padding: 8px;
  margin-right: 5px;
}

.selected-username {
  font-weight: 500;
  color: #333;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.empty-messages {
  text-align: center;
  color: #999;
  padding: 40px;
}

.message-item {
  display: flex;
  justify-content: flex-start;
}

.message-item.mine {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.message-item.mine .message-bubble {
  background: linear-gradient(135deg, #a8d8ea, #89cff0);
  color: #333;
}

.read-status {
  font-size: 11px;
  color: #888;
}

.message-item.mine .read-status {
  color: #666;
}

.message-content {
  word-break: break-word;
  line-height: 1.5;
}

.message-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 5px;
  margin-top: 5px;
}

.message-meta .time {
  font-size: 11px;
  opacity: 0.7;
}

.message-meta .read-status {
  display: flex;
  align-items: center;
}

.message-input-area {
  padding: 15px 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.message-input-area .el-textarea {
  flex: 1;
}

.send-btn {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  height: 66px;
  padding: 0 20px;
}

.send-btn:hover {
  opacity: 0.9;
}
</style>