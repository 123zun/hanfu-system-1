<template>
  <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="true"
      :lock-scroll="false"
      class="follow-list-dialog"
      @closed="onClosed"
  >
    <div class="follow-list-content" v-loading="loading">
      <div v-if="userList.length === 0 && !loading" class="empty-state">
        <el-empty :description="emptyText" :image-size="60" />
      </div>
      <div v-else class="user-list">
        <div
            v-for="user in userList"
            :key="user.id"
            class="user-item"
            @click="openUserProfile(user)"
        >
          <el-avatar :size="48" :src="getImageUrl(user.avatar)" class="user-avatar" />
          <div class="user-info">
            <span class="user-name">{{ user.username }}</span>
            <span class="user-bio" v-if="user.bio">{{ user.bio }}</span>
            <span class="user-bio empty" v-else>暂无简介</span>
          </div>
          <div class="user-action">
            <el-button
                v-if="String(user.id) !== String(currentUserId)"
                :type="user.isFollowing ? 'default' : 'primary'"
                size="small"
                @click.stop="handleFollowToggle(user)"
                :loading="user.followLoading"
            >
              {{ user.isFollowing ? '已关注' : '关注' }}
            </el-button>
            <el-tag v-else type="info" size="small">自己</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户主页弹窗 -->
    <UserProfileDialog
        v-model="profileDialogVisible"
        :user-id="selectedUserId"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { followUser, unfollowUser } from '@/api/modules/user'
import request from '@/api/request'
import UserProfileDialog from './UserProfileDialog.vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'followers', // 'followers' or 'following'
    validator: (val) => ['followers', 'following'].includes(val)
  },
  userId: {
    type: [Number, String],
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const dialogTitle = computed(() => props.type === 'followers' ? '关注者列表' : '关注列表')
const emptyText = computed(() => props.type === 'followers' ? '暂无关注者' : '暂无关注')

const loading = ref(false)
const userList = ref([])
const currentUserId = ref(null)
const profileDialogVisible = ref(false)
const selectedUserId = ref(null)

watch(() => props.modelValue, (val) => {
  if (val) {
    loadUserList()
  }
})

const loadUserList = async () => {
  if (!props.userId) return

  loading.value = true
  userList.value = []
  currentUserId.value = localStorage.getItem('current_user_id')

  try {
    const endpoint = props.type === 'followers'
      ? `/follow/followers/${props.userId}`
      : `/follow/following/${props.userId}`

    const res = await request({
      url: endpoint,
      method: 'GET',
      params: { currentUserId: currentUserId.value }
    })

    if (res && res.code === 200) {
      userList.value = (res.data || []).map(u => ({
        ...u,
        followLoading: false
      }))
    }
  } catch (error) {
    console.error('加载列表失败:', error)
  } finally {
    loading.value = false
  }
}

const openUserProfile = (user) => {
  selectedUserId.value = user.id
  profileDialogVisible.value = true
}

const handleFollowToggle = async (user) => {
  if (!currentUserId.value) {
    ElMessage.warning('请先登录')
    return
  }

  user.followLoading = true
  try {
    let res
    if (user.isFollowing) {
      res = await unfollowUser(currentUserId.value, user.id)
    } else {
      res = await followUser(currentUserId.value, user.id)
    }

    if (res && res.code === 200) {
      user.isFollowing = !user.isFollowing
      ElMessage.success(res.message || (user.isFollowing ? '关注成功' : '取消关注成功'))
    } else {
      ElMessage.error(res?.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    user.followLoading = false
  }
}

const onClosed = () => {
  emit('close')
}

const getImageUrl = (path) => {
  if (!path) return 'http://localhost:8080/uploads/avatars/default.png'
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `http://localhost:8080${path}`
  return `http://localhost:8080/${path}`
}
</script>

<style scoped>
.follow-list-content {
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.empty-state {
  padding: 30px 0;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f5f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.user-item:hover {
  background: white;
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateX(5px);
}

.user-avatar {
  flex-shrink: 0;
  border: 2px solid #d4af37;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 0.95rem;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-bio {
  font-size: 0.8rem;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-bio.empty {
  color: #999;
  font-style: italic;
}

.user-action {
  flex-shrink: 0;
}

.follow-list-dialog :deep(.el-dialog__body) {
  padding: 15px 20px;
}
</style>