<template>
  <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="true"
      :lock-scroll="false"
      class="user-profile-dialog"
      @closed="onDialogClosed"
  >
    <div class="profile-content" v-loading="loading">
      <!-- 用户信息区 -->
      <div class="user-header">
        <div class="user-avatar-section">
          <el-avatar :size="80" :src="getImageUrl(userInfo.avatar)" class="user-avatar" />
        </div>
        <div class="user-info-section">
          <h3 class="username">{{ userInfo.username || '汉服爱好者' }}</h3>
          <p class="user-bio" v-if="userInfo.bio">{{ userInfo.bio }}</p>
          <p class="user-bio empty" v-else>这个用户很懒，没有留下简介</p>
          <div class="user-meta">
            <span class="meta-item" v-if="userInfo.gender">
              <el-icon><Male v-if="userInfo.gender === 'male'" /><Female v-else /></el-icon>
              {{ userInfo.gender === 'male' ? '男' : '女' }}
            </span>
            <span class="meta-item" v-if="userInfo.age">
              <el-icon><Clock /></el-icon>
              {{ userInfo.age }}岁
            </span>
            <span class="meta-item" v-if="userInfo.region">
              <el-icon><Location /></el-icon>
              {{ userInfo.region }}
            </span>
          </div>
          <div class="user-stats">
            <span class="stat-item">
              <strong>{{ followerCount }}</strong> 关注者
            </span>
            <span class="stat-item">
              <strong>{{ followingCount }}</strong> 关注中
            </span>
          </div>
        </div>
        <div class="action-section">
          <!-- 关注按钮 -->
          <el-button
              v-if="canFollow"
              :type="isFollowing ? 'default' : 'primary'"
              :icon="isFollowing ? 'Check' : 'Plus'"
              @click="handleFollowToggle"
              :loading="followLoading"
              class="follow-btn"
          >
            {{ isFollowing ? '已关注' : '关注' }}
          </el-button>
          <el-tag v-else type="info" class="self-tag">当前用户</el-tag>
        </div>
      </div>

      <!-- 用户帖子列表 -->
      <div class="user-posts-section">
        <h4 class="section-title">TA发布的帖子</h4>
        <div v-if="worksLoading" class="loading-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          加载中...
        </div>
        <div v-else-if="worksList.length === 0" class="empty-state">
          <el-empty description="暂无发布的帖子" :image-size="80" />
        </div>
        <div v-else class="posts-list">
          <div
              v-for="work in worksList"
              :key="work.id"
              class="post-item"
              @click="goToDetail(work)"
          >
            <div class="post-cover">
              <img :src="getImageUrl(work.coverImage)" :alt="work.title" />
            </div>
            <div class="post-info">
              <h5 class="post-title">{{ work.title }}</h5>
              <div class="post-meta">
                <span class="meta-item">
                  <el-icon><View /></el-icon>
                  {{ work.views || 0 }}
                </span>
                <span class="meta-item">
                  <el-icon><ChatDotRound /></el-icon>
                  {{ work.comments || 0 }}
                </span>
                <span class="type-tag">{{ getTypeName(work.type) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Male, Female, Clock, Location, View, ChatDotRound, Loading } from '@element-plus/icons-vue'
import { getUserProfile, followUser, unfollowUser } from '@/api/modules/user'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  userId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const router = useRouter()

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dialogTitle = computed(() => `${userInfo.value?.username || '用户'} 的主页`)

const loading = ref(false)
const worksLoading = ref(false)
const followLoading = ref(false)

const userInfo = ref({})
const worksList = ref([])
const followerCount = ref(0)
const followingCount = ref(0)
const isFollowing = ref(false)
const currentUserId = ref(null)

const canFollow = computed(() => {
  return currentUserId.value && String(currentUserId.value) !== String(props.userId)
})

watch(() => props.modelValue, (val) => {
  if (val) {
    loadUserProfile()
  }
})

watch(() => props.userId, (val) => {
  if (val && dialogVisible.value) {
    loadUserProfile()
  }
})

const loadUserProfile = async () => {
  if (!props.userId) return

  loading.value = true
  try {
    currentUserId.value = localStorage.getItem('current_user_id')

    const res = await getUserProfile(props.userId, currentUserId.value)
    if (res && res.code === 200) {
      const data = res.data
      userInfo.value = data.userInfo || {}
      followerCount.value = data.followerCount || 0
      followingCount.value = data.followingCount || 0
      isFollowing.value = data.isFollowing || false

      // 解析works
      if (data.works && data.works.records) {
        worksList.value = data.works.records || []
      } else {
        worksList.value = []
      }
    } else {
      ElMessage.error(res?.message || '加载用户信息失败')
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleFollowToggle = async () => {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    return
  }

  followLoading.value = true
  try {
    let res
    if (isFollowing.value) {
      res = await unfollowUser(currentUserId.value, props.userId)
    } else {
      res = await followUser(currentUserId.value, props.userId)
    }

    if (res && res.code === 200) {
      isFollowing.value = !isFollowing.value
      // 更新粉丝数
      if (isFollowing.value) {
        followerCount.value++
      } else {
        followerCount.value--
      }
      ElMessage.success(res.message || (isFollowing.value ? '关注成功' : '取消关注成功'))
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

const goToDetail = (work) => {
  dialogVisible.value = false
  router.push(`/work/detail/${work.id}`)
}

const onDialogClosed = () => {
  emit('close')
}

const getImageUrl = (path) => {
  if (!path) return 'http://localhost:8080/uploads/avatars/default.png'
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
.profile-content {
  max-height: 70vh;
  overflow-y: auto;
}

.user-header {
  display: flex;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0e6d6;
  margin-bottom: 20px;
}

.user-avatar-section {
  flex-shrink: 0;
}

.user-avatar {
  border: 3px solid #d4af37;
}

.user-info-section {
  flex: 1;
  min-width: 0;
}

.username {
  font-size: 1.3rem;
  color: #333;
  margin: 0 0 8px 0;
}

.user-bio {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.user-bio.empty {
  color: #999;
  font-style: italic;
}

.user-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 0.85rem;
}

.meta-item .el-icon {
  color: #d4af37;
}

.user-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  color: #666;
  font-size: 0.9rem;
}

.stat-item strong {
  color: #333;
  font-weight: 600;
  margin-right: 4px;
}

.action-section {
  flex-shrink: 0;
  display: flex;
  align-items: flex-start;
}

.follow-btn {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  color: white;
}

.follow-btn:hover {
  opacity: 0.9;
}

.self-tag {
  margin-top: 5px;
}

.user-posts-section {
  margin-top: 10px;
}

.section-title {
  font-size: 1rem;
  color: #333;
  margin: 0 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0e6d6;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 50px;
  height: 2px;
  background: #d4af37;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-item {
  display: flex;
  gap: 15px;
  padding: 12px;
  background: #f8f5f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.post-item:hover {
  background: white;
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateX(5px);
}

.post-cover {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.post-title {
  font-size: 0.95rem;
  color: #333;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.post-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 3px;
  color: #999;
  font-size: 0.8rem;
}

.post-meta .meta-item .el-icon {
  font-size: 12px;
}

.type-tag {
  background: rgba(212, 175, 55, 0.15);
  color: #b8860b;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 0.75rem;
}

.user-profile-dialog :deep(.el-dialog__header) {
  border-bottom: 1px solid #f0e6d6;
  padding-bottom: 15px;
}

.user-profile-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

@media (max-width: 500px) {
  .user-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .user-meta {
    justify-content: center;
  }

  .user-stats {
    justify-content: center;
  }
}
</style>