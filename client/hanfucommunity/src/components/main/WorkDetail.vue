<template>
  <div class="work-detail-view">
    <div class="detail-container">
      <div class="back-section">
        <el-button type="text" @click="goBack" class="back-btn">返回帖子列表</el-button>
      </div>

      <div class="work-card" v-loading="loading">
        <div class="work-header">
          <h1 class="work-title">{{ work.title }}</h1>
          <div class="work-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="getImageUrl(work.userAvatar)" />
              <div class="author-detail">
                <span class="author-name">{{ work.userName || '匿名用户' }}</span>
                <span class="publish-time">{{ formatDate(work.createTime) }}</span>
              </div>
            </div>
            <div class="stats-info">
              <span class="stat-item">{{ work.views || 0 }} 浏览</span>
              <span class="stat-item">{{ work.comments || 0 }} 评论</span>
            </div>
          </div>
        </div>

        <div class="work-cover" v-if="work.coverImage">
          <img :src="getImageUrl(work.coverImage)" :alt="work.title" />
        </div>

        <div class="work-content" v-html="work.description"></div>

        <div class="work-actions-bar">
          <div class="actions-left">
            <span class="type-tag">{{ getTypeName(work.type) }}</span>
          </div>
          <div class="actions-right">
            <el-button :type="work.liked ? 'primary' : 'default'" @click="handleLike">
              {{ work.liked ? '已点赞' : '点赞' }} ({{ work.likes || 0 }})
            </el-button>
            <el-button :type="work.collected ? 'warning' : 'default'" @click="handleCollect">
              {{ work.collected ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <div class="comment-section">
          <h3 class="section-title">评论 ({{ work.comments || 0 }})</h3>

          <div class="comment-input-wrapper">
            <el-avatar :size="36" :src="getImageUrl(currentUser?.avatar)" />
            <div class="comment-input-area">
              <el-input v-model="commentContent" type="textarea" :rows="3" placeholder="写下你的评论..." maxlength="500" show-word-limit />
              <div class="comment-actions">
                <el-button type="primary" @click="submitComment" :loading="submitting">发表评论</el-button>
              </div>
            </div>
          </div>

          <div class="comment-list" v-if="comments.length > 0">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <el-avatar :size="36" :src="getImageUrl(comment.userAvatar)" />
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-user">{{ comment.userName }}</span>
                  <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                </div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-footer">
                  <el-button link size="small" @click="toggleReplyInput(comment.id)">回复</el-button>
                  <el-button link size="small" :type="comment.liked ? 'primary' : 'default'" @click="handleCommentLike(comment.id)">
                    点赞 ({{ comment.likes || 0 }})
                  </el-button>
                  <el-button v-if="currentUser && (isAdminUser || currentUser.id === comment.userId)" link size="small" type="danger" @click="handleDeleteComment(comment.id)">
                    删除
                  </el-button>
                </div>

                <div v-if="replyTarget === comment.id" class="reply-input-wrapper">
                  <el-input v-model="replyContent" type="textarea" :rows="2" :placeholder="replyToUserName ? '回复 @' + replyToUserName + '：' : '写下你的回复...'" size="small" />
                  <div class="reply-actions">
                    <el-button size="small" @click="cancelReply">取消</el-button>
                    <el-button size="small" type="primary" @click="submitReply(comment.id)">回复</el-button>
                  </div>
                </div>

                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <div class="reply-header">
                      <span class="reply-user">{{ reply.userName }}</span>
                      <span v-if="reply.replyToUserName" class="reply-to">回复 @{{ reply.replyToUserName }}</span>
                      <span class="reply-time">{{ formatDate(reply.createTime) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>
                    <div class="reply-footer">
                      <el-button link size="small" @click="toggleReplyInput(comment.id, reply.userName, reply.id)">回复</el-button>
                      <el-button link size="small" :type="reply.liked ? 'primary' : 'default'" @click="handleReplyLike(reply.id)">
                        点赞 ({{ reply.likes || 0 }})
                      </el-button>
                      <el-button v-if="currentUser && (isAdminUser || currentUser.id === reply.userId)" link size="small" type="danger" @click="handleDeleteComment(reply.id)">
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-comments">
            <el-empty description="暂无评论，快来抢沙发吧~" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElAvatar, ElButton, ElInput, ElEmpty } from 'element-plus'
import { getWorkDetail, likeWork, collectWork, getWorkComments, addWorkComment, likeWorkComment, deleteWorkComment } from '@/api/modules/work'
import { isAdmin } from '@/utils/permission'

const router = useRouter()
const route = useRoute()

const workId = ref(null)
const loading = ref(false)
const work = ref({ id: null, title: '', description: '', type: '', userName: '', userAvatar: '', coverImage: '', views: 0, likes: 0, liked: false, collected: false, createTime: null })
const comments = ref([])
const commentContent = ref('')
const submitting = ref(false)
const replyTarget = ref(null)
const replyContent = ref('')
const replyToUserName = ref('')
const replyToId = ref(null)
const currentUser = ref(null)
const isAdminUser = isAdmin()

onMounted(() => {
  workId.value = route.params.id
  const userStr = localStorage.getItem('hanfu_user')
  if (userStr) currentUser.value = JSON.parse(userStr)
  loadWorkDetail()
  loadComments()
})

const loadWorkDetail = async () => {
  if (!workId.value) return
  loading.value = true
  try {
    const response = await getWorkDetail(workId.value)
    if (response && response.code === 200) {
      work.value = response.data
    } else {
      ElMessage.error('加载帖子失败')
      goBack()
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  if (!currentUser.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    const response = await likeWork(work.value.id, currentUser.value.id)
    if (response && response.code === 200) {
      work.value.liked = !work.value.liked
      work.value.likes += work.value.liked ? 1 : -1
    }
  } catch (error) { ElMessage.error('操作失败') }
}

const handleCollect = async () => {
  if (!currentUser.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  try {
    const response = await collectWork(work.value.id, currentUser.value.id)
    if (response && response.code === 200) {
      work.value.collected = !work.value.collected
    }
  } catch (error) { ElMessage.error('操作失败') }
}

const loadComments = async () => {
  if (!workId.value) return
  try {
    const response = await getWorkComments('work', workId.value, currentUser.value?.id)
    console.log("评论",response);
    if (response && response.code === 200) comments.value = response.data || []
    else comments.value = []
  } catch (error) { comments.value = [] }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) { ElMessage.warning('请输入评论内容'); return }
  if (!currentUser.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  submitting.value = true
  try {
    const response = await addWorkComment({ userId: currentUser.value.id, targetType: 'work', targetId: workId.value, content: commentContent.value, parentId: null })
    if (response && response.code === 200) {
      await loadComments()
      commentContent.value = ''
      work.value.comments = (work.value.comments || 0) + 1
    }
  } catch (error) { ElMessage.error('评论失败') }
  finally { submitting.value = false }
}

const toggleReplyInput = (commentId, replyToUser = null, replyId = null) => {
  if (!currentUser.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  replyTarget.value = replyTarget.value === commentId ? null : commentId
  replyContent.value = ''
  replyToUserName.value = replyToUser || ''
  replyToId.value = replyId || null
}

const cancelReply = () => { replyTarget.value = null; replyContent.value = ''; replyToUserName.value = ''; replyToId.value = null }

const submitReply = async (commentId) => {
  if (!replyContent.value.trim()) { ElMessage.warning('请输入回复内容'); return }
  // 获取被回复的用户ID
  let replyToUserId = null
  const parentComment = comments.value.find(c => c.id === commentId)
  if (replyToId.value) {
    // 回复子评论时，取子评论的userId
    if (parentComment && parentComment.replies) {
      const reply = parentComment.replies.find(r => r.id === replyToId.value)
      if (reply) replyToUserId = reply.userId
    }
  } else {
    // 回复父评论时，取父评论的userId
    if (parentComment) replyToUserId = parentComment.userId
  }
  try {
    const response = await addWorkComment({ userId: currentUser.value.id, targetType: 'work', targetId: workId.value, content: replyContent.value, parentId: commentId, replyToId: replyToUserId })
    if (response && response.code === 200) {
      await loadComments()
      replyContent.value = ''
      replyToId.value = null
    }
  } catch (error) { ElMessage.error('回复失败') }
  replyTarget.value = null
  replyToUserName.value = ''
}

const handleCommentLike = async (commentId) => {
  if (!currentUser.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) return
  try {
    const response = await likeWorkComment(commentId, currentUser.value.id)
    if (response && response.code === 200) { comment.liked = !comment.liked; comment.likes += comment.liked ? 1 : -1 }
  } catch (error) { ElMessage.error('操作失败') }
}

const handleReplyLike = async (replyId) => {
  if (!currentUser.value) { ElMessage.warning('请先登录'); router.push('/login'); return }
  for (const comment of comments.value) {
    if (comment.replies) {
      const reply = comment.replies.find(r => r.id === replyId)
      if (reply) {
        try {
          const response = await likeWorkComment(replyId, currentUser.value.id)
          if (response && response.code === 200) { reply.liked = !reply.liked; reply.likes += reply.liked ? 1 : -1 }
        } catch (error) { ElMessage.error('操作失败') }
        return
      }
    }
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' })
    const response = await deleteWorkComment(commentId, currentUser.value.id)
    if (response && response.code === 200) {
      ElMessage.success('删除成功')
      await loadComments()
      work.value.comments = Math.max(0, (work.value.comments || 1) - 1)
    }
  } catch (error) { if (error !== 'cancel') ElMessage.error('删除失败') }
}

const goBack = () => { router.push('/main') }

const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
  } catch (e) { return dateString }
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `http://localhost:8080${path}`
  return `http://localhost:8080/${path}`
}

const getTypeName = (type) => {
  const map = { photography: '摄影', design: '设计', dressing: '穿搭' }
  return map[type] || type || '摄影'
}
</script>

<style scoped>
.work-detail-view { padding: 30px; min-height: 100vh; background: #f5f5f5; }
.detail-container { max-width: 900px; margin: 0 auto; }
.back-section { margin-bottom: 20px; }
.back-btn { color: #666; font-size: 14px; }
.work-card { background: white; border-radius: 12px; padding: 30px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.work-header { margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #eee; }
.work-title { font-size: 1.8rem; color: #333; margin: 0 0 15px 0; line-height: 1.4; }
.work-meta { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.author-info { display: flex; align-items: center; gap: 12px; }
.author-name { font-weight: 500; color: #333; }
.publish-time { font-size: 0.85rem; color: #999; }
.stats-info { display: flex; gap: 20px; }
.stat-item { color: #999; font-size: 0.9rem; }
.work-cover { margin-bottom: 20px; border-radius: 8px; overflow: hidden; }
.work-cover img { width: 100%; max-height: 400px; object-fit: cover; }
.work-content { font-size: 1rem; line-height: 1.8; color: #555; margin-bottom: 20px; }
.work-content :deep(img) { max-width: 100%; border-radius: 8px; margin: 10px 0; }
.work-actions-bar { display: flex; justify-content: space-between; align-items: center; padding: 20px 0; border-top: 1px solid #eee; border-bottom: 1px solid #eee; margin-bottom: 30px; }
.type-tag { background: rgba(212,175,55,0.1); color: #d4af37; padding: 6px 16px; border-radius: 20px; font-size: 0.9rem; font-weight: 500; }
.actions-right { display: flex; gap: 10px; }
.comment-section { margin-top: 20px; }
.section-title { font-size: 1.2rem; color: #333; margin: 0 0 20px 0; }
.comment-input-wrapper { display: flex; gap: 15px; margin-bottom: 30px; }
.comment-input-area { flex: 1; }
.comment-actions { display: flex; justify-content: flex-end; margin-top: 10px; }
.comment-list { display: flex; flex-direction: column; gap: 20px; }
.comment-item { display: flex; gap: 12px; }
.comment-content { flex: 1; }
.comment-header { display: flex; gap: 10px; align-items: center; margin-bottom: 5px; }
.comment-user { font-size: 14px; font-weight: 500; color: #333; }
.comment-time { font-size: 12px; color: #999; }
.comment-text { font-size: 14px; color: #555; line-height: 1.6; margin-bottom: 8px; }
.comment-footer { display: flex; gap: 15px; }
.comment-footer .el-button { font-size: 13px; }
.reply-input-wrapper { margin-top: 10px; margin-bottom: 10px; }
.reply-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 8px; }
.reply-list { margin-top: 15px; padding: 15px; background: #f8f8f8; border-radius: 8px; }
.reply-item { margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid #eee; }
.reply-item:last-child { margin-bottom: 0; padding-bottom: 0; border-bottom: none; }
.reply-header { display: flex; gap: 8px; align-items: center; margin-bottom: 5px; flex-wrap: wrap; }
.reply-user { font-size: 13px; font-weight: 500; color: #333; }
.reply-to { font-size: 12px; color: #d4af37; margin: 0 4px; }
.reply-time { font-size: 11px; color: #999; }
.reply-text { font-size: 13px; color: #555; line-height: 1.5; margin-bottom: 6px; }
.reply-footer { display: flex; gap: 12px; }
.reply-footer .el-button { font-size: 12px; }
.empty-comments { padding: 30px 0; }
@media (max-width: 768px) {
  .work-detail-view { padding: 15px; }
  .work-card { padding: 20px; }
  .work-title { font-size: 1.4rem; }
  .work-meta { flex-direction: column; align-items: flex-start; }
  .work-actions-bar { flex-direction: column; gap: 15px; align-items: flex-start; }
  .actions-right { width: 100%; }
}
</style>
