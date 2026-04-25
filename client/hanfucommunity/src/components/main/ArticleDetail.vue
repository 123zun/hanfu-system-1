<!-- src/views/article/ArticleDetail.vue -->
<template>
  <div class="article-detail-view">
    <div class="detail-container">
      <!-- 返回按钮 -->
      <div class="back-section">
        <el-button type="text" icon="ArrowLeft" @click="goBack" class="back-btn">
          返回资讯列表
        </el-button>
      </div>

      <!-- 文章内容 -->
      <div class="article-card" v-loading="loading">
        <!-- 文章头部 -->
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>

          <div class="article-meta">
            <div class="author-info" @click="openUserProfile" style="cursor:pointer;">
              <el-avatar :size="40" :src="getImageUrl(article.authorAvatar)" />
              <div class="author-detail">
                <span class="author-name">{{ article.authorName || '匿名用户' }}</span>
                <span class="publish-time">{{ formatDate(article.publishTime) }}</span>
              </div>
            </div>

            <div class="stats-info">
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ article.views || 0 }} 浏览
              </span>
              <span class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ article.comments || 0 }} 评论
              </span>
            </div>
          </div>
        </div>

        <!-- 封面图 -->
        <div class="article-cover" v-if="article.coverImage">
          <img :src="getImageUrl(article.coverImage)" :alt="article.title" />
        </div>

        <!-- 摘要 -->
        <div class="article-excerpt" v-if="article.excerpt">
          <div class="excerpt-label">
            <el-icon><Document /></el-icon>
            摘要
          </div>
          <div class="excerpt-content">{{ article.excerpt }}</div>
        </div>

        <!-- 文章内容 -->
        <div class="article-content" v-html="article.content"></div>

        <!-- 底部操作栏 -->
        <div class="article-actions-bar">
          <div class="actions-left">
            <span class="category-tag">{{ getCategoryName(article.category) }}</span>
          </div>
          <div class="actions-right">
            <el-button
                :type="article.liked ? 'primary' : 'default'"
                @click="handleLike"
                class="action-btn"
            >
              <el-icon><Star /></el-icon>
              {{ article.liked ? '已点赞' : '点赞' }} ({{ article.likes || 0 }})
            </el-button>
            <el-button
                :type="article.collected ? 'warning' : 'default'"
                @click="handleCollect"
                class="action-btn"
            >
              <el-icon><StarFilled /></el-icon>
              {{ article.collected ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <h3 class="section-title">
            <el-icon><ChatDotRound /></el-icon>
            评论 ({{ comments.length }})
          </h3>

          <!-- 评论输入框 -->
          <div class="comment-input-wrapper">
            <el-avatar :size="36" :src="getImageUrl(currentUser?.avatar)" />
            <div class="comment-input-area">
              <el-input
                  v-model="commentContent"
                  type="textarea"
                  :rows="3"
                  placeholder="写下你的评论..."
                  maxlength="500"
                  show-word-limit
              />
              <div class="comment-actions">
                <el-button type="primary" @click="submitComment" :loading="submitting">
                  发表评论
                </el-button>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list" v-if="comments.length > 0">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="36" :src="getImageUrl(comment.userAvatar)" class="clickable-avatar" @click="openCommentUserProfile(comment.userId)" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-user clickable-name" @click="openCommentUserProfile(comment.userId)">{{ comment.userName }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-footer">
                  <el-button
                      link
                      size="small"
                      @click="toggleReplyInput(comment.id)"
                  >
                    <el-icon><ChatDotRound /></el-icon>
                    回复
                  </el-button>
                  <el-button
                      link
                      size="small"
                      :type="comment.liked ? 'primary' : 'default'"
                      @click="handleCommentLike(comment.id)"
                  >
                    <el-icon><star /></el-icon>
                    点赞 ({{ comment.likes || 0 }})
                  </el-button>
                  <el-button
                      v-if="currentUser && (isAdminUser || currentUser.id === comment.userId)"
                      link
                      size="small"
                      type="danger"
                      @click="handleDeleteComment(comment.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyTarget === comment.id" class="reply-input-wrapper">
                  <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      :placeholder="replyToUserName ? '回复 @' + replyToUserName + '：' : '写下你的回复...'"
                      size="small"
                  />
                  <div class="reply-actions">
                    <el-button size="small" @click="cancelReply">取消</el-button>
                    <el-button size="small" type="primary" @click="submitReply(comment.id)">
                      回复
                    </el-button>
                  </div>
                </div>

                <!-- 回复列表 -->
                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <el-avatar :size="28" :src="getImageUrl(reply.userAvatar)" class="reply-avatar" @click="openCommentUserProfile(reply.userId)" />
                    <div class="reply-content">
                      <div class="reply-header">
                        <span class="reply-user clickable-name" @click="openCommentUserProfile(reply.userId)">{{ reply.userName }}</span>
                        <span v-if="reply.replyToUserName" class="reply-to">回复 @{{ reply.replyToUserName }}</span>
                        <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                      </div>
                      <div class="reply-text">{{ reply.content }}</div>
                    <div class="reply-footer">
                      <el-button
                          link
                          size="small"
                          @click="toggleReplyInput(comment.id, reply.userName, reply.id)"
                      >
                        <el-icon><ChatDotRound /></el-icon>
                        回复
                      </el-button>
                      <el-button
                          link
                          size="small"
                          :type="reply.liked ? 'primary' : 'default'"
                          @click="handleReplyLike(reply.id)"
                      >
                        <el-icon><star /></el-icon>
                        点赞 ({{ reply.likes || 0 }})
                      </el-button>
                      <el-button
                          v-if="currentUser && (isAdminUser || currentUser.id === reply.userId)"
                          link
                          size="small"
                          type="danger"
                          @click="handleDeleteComment(reply.id)"
                      >
                        <el-icon><Delete /></el-icon>
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-comments">
            <el-empty description="暂无评论，快来抢沙发吧~" />
          </div>

          <!-- 加载更多评论 -->
          <div v-if="hasMoreComments" class="load-more-comments">
            <el-button link @click="loadMoreComments" :loading="loadingComments">
              加载更多评论
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户主页弹窗 -->
    <UserProfileDialog
        v-model="userProfileDialogVisible"
        :user-id="profileDialogUserId"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  ArrowLeft,
  View,
  ChatDotRound,
  Star,
  StarFilled,
  Delete,
  Share,
  Document
} from '@element-plus/icons-vue'
import { getArticleDetail, likeArticle, collectArticle, getComments, addComment, likeComment as apiLikeComment, deleteComment } from '@/api/modules/article'
import { isAdmin } from '@/utils/permission'
import UserProfileDialog from './UserProfileDialog.vue'

const router = useRouter()
const route = useRoute()

// 文章ID
const articleId = ref(null)
// 加载状态
const loading = ref(false)
// 文章数据
const article = ref({
  id: null,
  title: '',
  content: '',
  excerpt: '',
  coverImage: '',
  category: '',
  authorId: null,
  authorName: '',
  authorAvatar: '',
  views: 0,
  likes: 0,
  comments: 0,
  liked: false,
  collected: false,
  publishTime: null
})

// 评论相关
const comments = ref([])
const commentContent = ref('')
const submitting = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replyToUserName = ref('')
const replyToId = ref(null)  // 被回复的评论ID
const loadingComments = ref(false)
const hasMoreComments = ref(false)
let commentPage = 1
const commentPageSize = 10

// 当前用户
const currentUser = ref(null)
const isAdminUser = isAdmin()
const userProfileDialogVisible = ref(false)
const profileDialogUserId = ref(null)

// 分类映射
const categoryMap = {
  'industry': '行业动态',
  'activity': '活动资讯',
  'academic': '学术研究',
  'fashion': '穿搭分享'
}

onMounted(() => {
  // 获取文章ID
  articleId.value = route.params.id
  // 获取当前用户信息
  const userStr = localStorage.getItem('hanfu_user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  }
  // 加载文章详情
  loadArticleDetail()
  // 加载评论列表
  loadComments()
})

// 加载文章详情
const loadArticleDetail = async () => {
  if (!articleId.value) return

  loading.value = true
  try {
    const response = await getArticleDetail(articleId.value)
    if (response && response.code === 200) {
      article.value = response.data
    } else {
      ElMessage.error('加载文章失败')
      goBack()
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 点赞文章
const handleLike = async () => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await likeArticle(article.value.id, currentUser.value.id)

    if (response && response.code === 200) {
      // 切换点赞状态
      article.value.liked = !article.value.liked
      article.value.likes += article.value.liked ? 1 : -1
      ElMessage.success(article.value.liked ? '点赞成功' : '取消点赞')
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 收藏文章
const handleCollect = async () => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await collectArticle(article.value.id, currentUser.value.id)

    if (response && response.code === 200) {
      // 切换收藏状态
      article.value.collected = !article.value.collected
      ElMessage.success(article.value.collected ? '收藏成功' : '取消收藏')
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 加载评论列表
const loadComments = async () => {
  if (!articleId.value) return

  loadingComments.value = true
  try {
    const response = await getComments('article', articleId.value, currentUser.value?.id)

    if (response && response.code === 200) {
      comments.value = response.data || []
    } else {
      console.warn('加载评论失败:', response?.message)
      comments.value = []
    }

    hasMoreComments.value = false
  } catch (error) {
    console.error('加载评论失败:', error)
    comments.value = []
  } finally {
    loadingComments.value = false
  }
}

// 提交评论
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true
  try {
    const response = await addComment({
      userId: currentUser.value.id,
      targetType: 'article',
      targetId: articleId.value,
      content: commentContent.value,
      parentId: null
    })

    if (response && response.code === 200) {
      // 重新加载评论列表
      await loadComments()
      commentContent.value = ''
      ElMessage.success('评论成功')
      
      // 更新文章评论数
      article.value.comments = (article.value.comments || 0) + 1
    } else {
      ElMessage.error(response?.message || '评论失败')
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  } finally {
    submitting.value = false
  }
}

// 显示回复输入框
const toggleReplyInput = (commentId, replyToUser = null, replyId = null) => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  replyTarget.value = replyTarget.value === commentId ? null : commentId
  replyContent.value = ''
  replyToUserName.value = replyToUser || ''
  replyToId.value = replyId || null
}

// 取消回复
const cancelReply = () => {
  replyTarget.value = null
  replyContent.value = ''
  replyToUserName.value = ''
  replyToId.value = null
}

// 提交回复
const submitReply = async (commentId) => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  try {
    const response = await addComment({
      userId: currentUser.value.id,
      targetType: 'article',
      targetId: articleId.value,
      content: replyContent.value,
      parentId: commentId,
      replyToId: replyToId.value
    })

    if (response && response.code === 200) {
      // 重新加载评论列表
      await loadComments()
      replyContent.value = ''
      replyToId.value = null
      ElMessage.success('回复成功')
    } else {
      ElMessage.error(response?.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  }

  replyTarget.value = null
  replyToUserName.value = ''
}

// 评论点赞
const handleCommentLike = async (commentId) => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const comment = findCommentById(commentId)
  if (!comment) return

  try {
    const response = await apiLikeComment(commentId, currentUser.value.id)

    if (response && response.code === 200) {
      comment.liked = !comment.liked
      comment.likes += comment.liked ? 1 : -1
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

// 回复点赞
const handleReplyLike = async (replyId) => {
  if (!currentUser.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const reply = findReplyById(replyId)
  if (!reply) return

  try {
    const response = await apiLikeComment(replyId, currentUser.value.id)

    if (response && response.code === 200) {
      reply.liked = !reply.liked
      reply.likes += reply.liked ? 1 : -1
    } else {
      ElMessage.error(response?.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除评论
const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await deleteComment(commentId, currentUser.value.id)

    if (response && response.code === 200) {
      ElMessage.success('删除成功')
      // 重新加载评论列表
      await loadComments()
      // 更新文章评论数
      article.value.comments = Math.max(0, (article.value.comments || 1) - 1)
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 查找评论
const findCommentById = (id) => {
  return comments.value.find(c => c.id === id)
}

// 查找回复
const findReplyById = (id) => {
  for (const comment of comments.value) {
    if (comment.replies) {
      const reply = comment.replies.find(r => r.id === id)
      if (reply) return reply
    }
  }
  return null
}

// 加载更多评论
const loadMoreComments = () => {
  commentPage++
  loadComments()
}

// 获取分类名称
const getCategoryName = (categoryCode) => {
  return categoryMap[categoryCode] || categoryCode || '未分类'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知日期'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return dateString
  }
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `http://localhost:8080${path}`
  return `http://localhost:8080/${path}`
}

// 返回
const goBack = () => {
  router.push('/main')
}

// 打开用户主页弹窗
const openUserProfile = () => {
  if (article.value && article.value.authorId) {
    profileDialogUserId.value = article.value.authorId
    userProfileDialogVisible.value = true
  }
}

// 打开评论用户的主页弹窗
const openCommentUserProfile = (userId) => {
  if (!userId) return
  profileDialogUserId.value = userId
  userProfileDialogVisible.value = true
}
</script>

<style scoped>
/* 原有样式保持不变，添加摘要样式 */

/* 摘要样式 */
.article-excerpt {
  background: #f8f5f0;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  border-left: 4px solid #d4af37;
}

.excerpt-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #d4af37;
  margin-bottom: 10px;
}

.excerpt-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.article-detail-view {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 30px;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.back-section {
  margin-bottom: 20px;
}

.back-btn {
  color: #666;
  font-size: 14px;
}

.back-btn:hover {
  color: #d4af37;
}

.article-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.05);
}

.article-header {
  border-bottom: 1px solid #f0e6d6;
  padding-bottom: 20px;
  margin-bottom: 30px;
}

.article-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 20px 0;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

.stats-info {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #999;
}

.article-cover {
  margin-bottom: 30px;
  border-radius: 12px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #444;
  margin-bottom: 40px;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 20px 0;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.article-content :deep(p) {
  margin: 0 0 1em 0;
}

.article-content :deep(h2) {
  margin: 1.5em 0 0.5em;
  font-size: 1.5rem;
}

.article-content :deep(h3) {
  margin: 1.2em 0 0.5em;
  font-size: 1.2rem;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 0 0 1em 1.5em;
}

.article-content :deep(blockquote) {
  margin: 1em 0;
  padding-left: 1em;
  border-left: 3px solid #d4af37;
  color: #666;
  font-style: italic;
}

.article-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-top: 1px solid #f0e6d6;
  border-bottom: 1px solid #f0e6d6;
  margin-bottom: 40px;
}

.category-tag {
  background: rgba(212, 175, 55, 0.1);
  color: #d4af37;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

.actions-right {
  display: flex;
  gap: 15px;
}

.action-btn {
  border-radius: 20px;
}

/* 评论区样式 */
.comment-section {
  margin-top: 20px;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-input-wrapper {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f5f0;
  border-radius: 12px;
}

.comment-input-area {
  flex: 1;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-list {
  margin-top: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0e6d6;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.clickable-name { cursor: pointer; }
.clickable-name:hover { color: #d4af37; }
.clickable-avatar { cursor: pointer; }

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-footer {
  display: flex;
  gap: 15px;
}

.reply-input-wrapper {
  margin-top: 12px;
  padding: 12px;
  background: #f8f5f0;
  border-radius: 8px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.reply-list {
  margin-top: 12px;
  padding-left: 20px;
  border-left: 2px solid #f0e6d6;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 12px 0;
  border-bottom: 1px solid #f0e6d6;
}

.reply-avatar { cursor: pointer; flex-shrink: 0; }

.reply-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.reply-user {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.reply-to {
  font-size: 12px;
  color: #d4af37;
  margin: 0 4px;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-text {
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;
}

.reply-footer {
  display: flex;
  gap: 15px;
}

.empty-comments {
  padding: 40px 0;
}

.load-more-comments {
  text-align: center;
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .article-detail-view {
    padding: 15px;
  }

  .article-card {
    padding: 20px;
  }

  .article-title {
    font-size: 22px;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
  }

  .article-actions-bar {
    flex-direction: column;
    gap: 15px;
  }

  .comment-input-wrapper {
    flex-direction: column;
  }
}
</style>