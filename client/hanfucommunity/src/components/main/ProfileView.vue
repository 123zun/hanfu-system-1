<template>
  <div class="profile-view">
    <!-- 个人中心头部 -->
    <div class="profile-header">
      <div class="profile-banner">
        <div class="banner-overlay"></div>
        <div class="profile-avatar" style="margin-top:15px;">
          <img :src="userInfo.avatar || defaultAvatar" />
          <div class="avatar-upload" @click="triggerAvatarUpload">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </div>
        </div>
        <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarSelect"
        />
      </div>

      <div class="profile-info" style="margin-top:100px;">
        <h2 class="profile-name">{{ userInfo.username || '汉服爱好者' }}</h2>
        <p class="profile-bio">{{ userInfo.bio || '传承华夏文明，弘扬汉服文化' }}</p>
        <div class="profile-stats">
          <div class="stat-item" @click="openMyPostsDialog">
            <span class="stat-number">{{ myPostCount }}</span>
            <span class="stat-label">帖子</span>
          </div>
          <div class="stat-item" @click="openFollowersDialog">
            <span class="stat-number">{{ followerCount }}</span>
            <span class="stat-label">关注者</span>
          </div>
          <div class="stat-item" @click="openFollowingDialog">
            <span class="stat-number">{{ followingCount }}</span>
            <span class="stat-label">关注中</span>
          </div>
          <div class="stat-item" @click="openMyCollectionsDialog">
            <span class="stat-number">{{ myCollectionCount }}</span>
            <span class="stat-label">收藏</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="profile-content">
      <!-- 左侧：个人信息表单 -->
      <div class="info-form">
        <h3 class="form-title">个人信息</h3>

        <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
        >
          <el-form-item label="用&ensp;户&ensp;名" prop="username">
            <el-input
                v-model="profileForm.username"
                placeholder="请输入用户名"
                clearable
            />
          </el-form-item>

          <el-form-item label="邮&emsp;&emsp;箱" prop="email">
            <el-input
                v-model="profileForm.email"
                placeholder="请输入邮箱地址"
                clearable
            />
          </el-form-item>

          <el-form-item label="手&ensp;机&ensp;号" prop="phone">
            <el-input
                v-model="profileForm.phone"
                placeholder="请输入手机号码"
                clearable
            />
          </el-form-item>

          <el-form-item label="性&emsp;&emsp;别" prop="gender">
            <el-radio-group v-model="profileForm.gender">
              <el-radio label="male">男</el-radio>
              <el-radio label="female">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="年&emsp;&emsp;龄">
            <el-input
                v-model="profileForm.age"
                placeholder="请输入年龄"
                clearable
            />
          </el-form-item>

          <el-form-item label="所在地区">
            <el-input
                v-model="profileForm.region"
                placeholder="请输入所在地区"
                clearable
            />
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input
                v-model="profileForm.bio"
                type="textarea"
                :rows="4"
                placeholder="介绍一下自己吧..."
                maxlength="200"
                show-word-limit
            />
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                :loading="saving"
                @click="handleSaveProfile"
                class="save-btn"
            >
              {{ saving ? '保存中...' : '保存修改' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 右侧：其他信息 -->
      <div class="additional-info">
        <div class="info-card">
          <h3 class="info-title">账号信息</h3>
          <div class="info-item">
            <span class="item-label">用户ID：</span>
            <span class="item-value">{{ userInfo.id || 'N/A' }}</span>
          </div>
          <div class="info-item">
            <span class="item-label">注册时间：</span>
            <span class="item-value">{{ formatDate(userInfo.createTime) || '2026-01-01' }}</span>
          </div>
          <div class="info-item">
            <span class="item-label">最后登录：</span>
            <span class="item-value">{{ formatDate(userInfo.lastLogin) || '刚刚' }}</span>
          </div>
          <div class="info-item">
            <span class="item-label">账号状态：</span>
            <span class="item-value status-active">正常</span>
          </div>
        </div>

        <div class="info-card">
          <h3 class="info-title">安全设置</h3>
          <el-button type="warning" class="security-btn" @click="openPasswordDialog">
            <el-icon><Lock /></el-icon>
            修改密码
          </el-button>
        </div>

        <div class="info-card">
          <h3 class="info-title">我的成就</h3>
          <div class="achievements">
            <div class="achievement-item" v-for="achievement in achievements" :key="achievement.id">
              <el-icon :class="achievement.iconClass"><component :is="achievement.icon" /></el-icon>
              <div class="achievement-info">
                <span class="achievement-name">{{ achievement.name }}</span>
                <span class="achievement-desc">{{ achievement.description }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像上传对话框 -->
    <el-dialog
        v-model="avatarDialogVisible"
        title="更换头像"
        width="500px"
        align-center
        :lock-scroll="false"
    >
      <div class="avatar-upload-dialog">
        <div v-if="!selectedAvatar" class="upload-area" @click="triggerAvatarUpload">
          <el-icon class="upload-icon"><Upload /></el-icon>
          <p>点击选择图片</p>
          <p class="upload-tips">支持 JPG、PNG、GIF 格式，大小不超过 5MB</p>
        </div>

        <div v-else class="preview-area">
          <img :src="avatarPreview" class="preview-image" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="avatarDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="confirmAvatarUpload">
            {{ uploading ? '上传中...' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
  <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="480px"
      align-center
      :close-on-click-modal="false"
      :lock-scroll="false"
  >
    <div class="password-change-dialog">
      <div class="dialog-header">
        <div class="header-icon">
          <el-icon><Lock /></el-icon>
        </div>
        <h3 class="header-title">修改密码</h3>
        <p class="header-subtitle">为了保护您的账户安全，请谨慎修改密码</p>
      </div>

      <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="0"
          class="password-form"
      >
        <!-- 原密码 -->
        <el-form-item prop="oldPassword">
          <div class="form-item-label">
            <el-icon><Key /></el-icon>
            原密码
          </div>
          <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入当前使用的密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              class="password-input"
          />
        </el-form-item>

        <!-- 新密码 -->
        <el-form-item prop="newPassword">
          <div class="form-item-label">
            <el-icon><Edit /></el-icon>
            新密码
          </div>
          <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入6-20位新密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              class="password-input"
              @input="checkPasswordStrength"
          />

          <!-- 密码强度提示 -->
          <div v-if="passwordForm.newPassword" class="password-strength-hint">
            <span class="hint-text">密码强度：</span>
            <span :class="['strength-text', strengthClass]">{{ strengthText }}</span>
            <div class="strength-bar">
              <div :class="['bar-segment', barClass(0)]"></div>
              <div :class="['bar-segment', barClass(1)]"></div>
              <div :class="['bar-segment', barClass(2)]"></div>
            </div>
          </div>
        </el-form-item>

        <!-- 确认新密码 -->
        <el-form-item prop="confirmPassword">
          <div class="form-item-label">
            <el-icon><Check /></el-icon>
            确认新密码
          </div>
          <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              clearable
              class="password-input"
          />
        </el-form-item>

        <!-- 密码提示 -->
        <div class="password-tips">
          <p><el-icon><InfoFilled /></el-icon> 密码长度6-20位，建议包含字母、数字</p>
          <p><el-icon><Warning /></el-icon> 修改成功后需要重新登录</p>
        </div>

        <!-- 操作按钮 -->
        <div class="form-actions">
          <el-button
              @click="closePasswordDialog"
              class="cancel-btn"
          >
            取消
          </el-button>
          <el-button
              type="primary"
              :loading="changing"
              @click="handleChangePassword"
              class="submit-btn"
          >
            {{ changing ? '修改中...' : '确认修改' }}
          </el-button>
        </div>
      </el-form>
    </div>
  </el-dialog>

  <!-- 我的帖子弹窗 -->
  <el-dialog v-model="myPostsDialogVisible" title="我的帖子" width="700px" :lock-scroll="false">
    <div class="my-items-dialog">
      <div v-if="myPostsList.length === 0" class="empty-tip">暂无发布的帖子</div>
      <ul class="item-list" v-else>
        <li v-for="item in myPostsList" :key="item.id" class="item-row">
          <div class="item-info" @click="goToDetail(item)">
            <span class="item-title">{{ item.title }}</span>
            <span class="item-type">{{ item.type === 'article' ? '资讯' : '帖子' }}</span>
          </div>
          <el-button type="danger" size="small" @click="handleDeletePost(item)">删除</el-button>
        </li>
      </ul>
    </div>
  </el-dialog>

  <!-- 我的收藏弹窗 -->
  <el-dialog v-model="myCollectionsDialogVisible" title="我的收藏" width="700px" :lock-scroll="false">
    <div class="my-items-dialog">
      <div v-if="myCollectionsList.length === 0" class="empty-tip">暂无收藏内容</div>
      <ul class="item-list" v-else>
        <li v-for="item in myCollectionsList" :key="item.id" class="item-row">
          <div class="item-info" @click="goToCollectionDetail(item)">
            <span class="item-title">{{ item.title }}</span>
            <span class="item-type">{{ item.type === 'article' ? '资讯' : '帖子' }}</span>
          </div>
        </li>
      </ul>
    </div>
  </el-dialog>

  <!-- 关注者列表弹窗 -->
  <FollowListDialog
      v-model="followersDialogVisible"
      type="followers"
      :user-id="currentUserId"
  />

  <!-- 关注列表弹窗 -->
  <FollowListDialog
      v-model="followingDialogVisible"
      type="following"
      :user-id="currentUserId"
  />

  <!-- 用户主页弹窗 -->
  <UserProfileDialog
      v-model="userProfileDialogVisible"
      :user-id="profileDialogUserId"
  />
</template>

<script setup>
import { ref, onMounted, reactive,computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Camera,
  Upload,
  Trophy,
  Star,
  Medal,
  Lock,
  Key,
  Edit,
  Check,
  InfoFilled,
  Warning
} from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, uploadAvatar, changePassword, getFollowCounts } from '@/api/modules/user'
import { getMyArticleCollections } from '@/api/modules/article'
import { getWorkList, deleteWork, getMyWorkCollections } from '@/api/modules/work'
import { getArticleList } from '@/api/modules/article'
import FollowListDialog from './FollowListDialog.vue'
import UserProfileDialog from './UserProfileDialog.vue'

import { useRouter } from 'vue-router'

const router = useRouter()

// 默认头像
const defaultAvatar = 'http://localhost:8080/uploads/avatars/default.png'

// 帖子和收藏相关
const myPostCount = ref(0)
const myCollectionCount = ref(0)
const myPostsList = ref([])
const myCollectionsList = ref([])
const myPostsDialogVisible = ref(false)
const myCollectionsDialogVisible = ref(false)

// 关注相关
const followerCount = ref(0)
const followingCount = ref(0)
const followersDialogVisible = ref(false)
const followingDialogVisible = ref(false)
const userProfileDialogVisible = ref(false)
const profileDialogUserId = ref(null)
const currentUserId = ref(null)

// 表单引用
const profileFormRef = ref()
const avatarInput = ref(null)

// 用户信息
const userInfo = ref({})
// 表单数据
const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  gender: 'male',
  birthday: '',
  region: '',
  bio: '',
  age: '',
  password:''
})

// 成就列表
const achievements = [
  { id: 1, name: '汉服新人', description: '注册用户', icon: Trophy, iconClass: 'gold' },
  { id: 2, name: '活跃用户', description: '发帖超过10篇', icon: Star, iconClass: 'silver' },
  { id: 3, name: '文化传承者', description: '参加3次活动', icon: Medal, iconClass: 'bronze' },
  { id: 4, name: '汉服爱好者', description: '连续登录30天', icon: 'StarFilled', iconClass: 'gold' }
]

// 头像上传相关
const avatarDialogVisible = ref(false)
const selectedAvatar = ref(null)
const avatarPreview = ref('')
const uploading = ref(false)
const saving = ref(false)

// 表单验证规则
const profileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const passwordDialogVisible = ref(false)
const changing = ref(false)
const passwordStrength = ref(0)

// 表单引用
const passwordFormRef = ref()

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证原密码
const validateOldPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入原密码'))
    return
  }
  callback()
}

// 验证新密码
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
    return
  }

  if (value.length < 6 || value.length > 20) {
    callback(new Error('密码长度在6-20个字符'))
    return
  }

  // 新密码不能与原密码相同
  if (value === profileForm.password) {
    callback(new Error('新密码不能与原密码相同'))
    return
  }

  callback()
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认新密码'))
    return
  }

  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 密码规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { validator: validateOldPassword, trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 页面加载
onMounted(() => {
  currentUserId.value = localStorage.getItem('current_user_id')
  loadUserProfile()
  loadMyPosts()
  loadMyCollections()
})

// 加载用户信息
const loadUserProfile = async () => {
  try {
    const userId = localStorage.getItem('current_user_id')
    if (!userId) {
      ElMessage.error('请先登录')
      return
    }

    const response = await getUserInfo(userId)
    console.log("个人中心",response);
    if (response && (response.code === 200 || response.code === 0)) {
      const data = response.data || response
      userInfo.value = data

      // 填充表单
      Object.assign(profileForm, {
        username: data.username || '',
        email: data.email || '',
        phone: data.phone || '',
        gender: data.gender || 'male',
        bio: data.bio || '',
        age: data.age || '',
        region: data.region || '',
        password: data.password || ''
      })
    }

    // 加载关注/粉丝数
    try {
      const countsRes = await getFollowCounts(userId)
      if (countsRes && countsRes.code === 200) {
        followerCount.value = countsRes.data?.followerCount || 0
        followingCount.value = countsRes.data?.followingCount || 0
      }
    } catch (e) {
      console.error('加载关注数失败:', e)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 触发头像上传
const triggerAvatarUpload = () => {
  // 创建隐藏的文件输入元素
  const fileInput = document.createElement('input')
  fileInput.type = 'file'
  fileInput.accept = 'image/*'
  fileInput.style.display = 'none'

  // 添加change事件监听
  fileInput.onchange = (event) => {
    const file = event.target.files[0]
    if (!file) return

    handleAvatarSelect(file)

    // 清理
    document.body.removeChild(fileInput)
  }

  // 添加到页面并触发点击
  document.body.appendChild(fileInput)
  fileInput.click()
}

// 处理头像选择
const handleAvatarSelect = (file) => {
  if (!file) return

  if (!validateImageFile(file)) return

  selectedAvatar.value = file

  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
  }
  reader.readAsDataURL(file)

  // 自动打开对话框（如果还没打开的话）
  if (!avatarDialogVisible.value) {
    avatarDialogVisible.value = true
  }
}

// 验证图片文件
const validateImageFile = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择图片文件（JPG/PNG/GIF/BMP/WebP）')
    return false
  }

  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }

  return true
}

// 确认上传头像
const confirmAvatarUpload = async () => {
  if (!selectedAvatar.value) {
    ElMessage.error('请先选择图片')
    return
  }

  const userId = localStorage.getItem('current_user_id')
  if (!userId) {
    ElMessage.error('用户ID不存在')
    return
  }

  uploading.value = true

  try {
    console.log('开始上传头像，用户ID:', userId, '文件:', selectedAvatar.value.name)

    // 创建FormData
    const formData = new FormData()
    formData.append('userId', userId)
    formData.append('file', selectedAvatar.value)

    console.log('FormData内容:', {
      userId: userId,
      fileName: selectedAvatar.value.name,
      fileSize: selectedAvatar.value.size,
      fileType: selectedAvatar.value.type
    })

    // 调用上传接口
    const response = await uploadAvatar(formData)

    console.log('头像上传响应:', response)

    if (response && (response.code === 200 || response.code === 0)) {
      const avatarUrl = response.data?.avatarUrl || response.avatarUrl || response.data?.avatar
      console.log('获取到头像URL:', avatarUrl)

      if (avatarUrl) {
        // 更新用户信息中的头像
        userInfo.value.avatar = avatarUrl

        // 更新表单中的头像
        if (profileForm.avatar !== undefined) {
          profileForm.avatar = avatarUrl
        }

        ElMessage.success(response.message || '头像上传成功')

        avatarDialogVisible.value = false

        // 更新localStorage中的用户信息
        const userStr = localStorage.getItem('hanfu_user')
        if (userStr) {
          const user = JSON.parse(userStr)
          user.avatar = avatarUrl
          localStorage.setItem('hanfu_user', JSON.stringify(user))
        }

        // 触发页面刷新显示新头像
        setTimeout(() => {
          location.reload()
        }, 500)
      } else {
        ElMessage.error('未返回头像URL')
      }
    } else {
      ElMessage.error(response?.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败: ' + (error.message || '请稍后重试'))
  } finally {
    uploading.value = false
  }
}

// 保存个人信息
const handleSaveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()

    saving.value = true

    const userId = localStorage.getItem('current_user_id')
    if (!userId) {
      ElMessage.error('用户ID不存在')
      return
    }

    const response = await updateUserInfo({
      id: userId,
      ...profileForm
    })

    if (response && (response.code === 200 || response.code === 0)) {
      ElMessage.success('个人信息更新成功')

      // 更新本地用户信息
      Object.assign(userInfo.value, profileForm)

      // 更新localStorage
      const userStr = localStorage.getItem('hanfu_user')
      if (userStr) {
        const user = JSON.parse(userStr)
        Object.assign(user, profileForm)
        localStorage.setItem('hanfu_user', JSON.stringify(user))
      }
    } else {
      ElMessage.error(response?.message || '更新失败')
    }

  } catch (error) {
    console.error('保存个人信息失败:', error)
  } finally {
    saving.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } catch (e) {
    return dateString
  }
}

// 修改密码点击
const handleChangePassword = () => {
  if (!passwordFormRef.value) return

  passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changing.value = true

    try {
      const userId = localStorage.getItem('current_user_id')
      if (!userId) {
        ElMessage.error('用户ID不存在')
        return
      }

      // 这里调用修改密码API
      const response = await changePassword({
        id: userId,
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })

      if (response && (response.code === 200 || response.code === 0)) {
        ElMessage.success('密码修改成功，请重新登录')

        // 关闭对话框
        closePasswordDialog()

        // 延迟跳转到登录页
        setTimeout(() => {
          // 清除登录状态
          localStorage.removeItem('is_logged_in')
          localStorage.removeItem('hanfu_user')
          localStorage.removeItem('current_user_id')

          // 跳转到登录页
          router.push('/login')
        }, 1500)
      } else {
        ElMessage.error(response?.message || '密码修改失败')
      }
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('修改密码失败')
    } finally {
      changing.value = false
    }
  })
}

// 检查密码强度
const checkPasswordStrength = () => {
  const password = passwordForm.newPassword
  if (!password) {
    passwordStrength.value = 0
    return
  }

  let strength = 0

  // 长度检查
  if (password.length >= 8) strength++

  // 复杂度检查
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/\d/.test(password)) strength++
  if (/[^a-zA-Z0-9]/.test(password)) strength++

  passwordStrength.value = Math.min(strength, 3)
}

// 密码强度文本
const strengthText = computed(() => {
  switch (passwordStrength.value) {
    case 0: return '极弱'
    case 1: return '弱'
    case 2: return '中'
    case 3: return '强'
    default: return ''
  }
})

// 密码强度样式类
const strengthClass = computed(() => {
  switch (passwordStrength.value) {
    case 0: return 'very-weak'
    case 1: return 'weak'
    case 2: return 'medium'
    case 3: return 'strong'
    default: return ''
  }
})

// 强度条样式
const barClass = (index) => {
  if (passwordStrength.value > index) {
    return strengthClass.value
  }
  return ''
}

// 关闭密码对话框
const closePasswordDialog = () => {
  passwordDialogVisible.value = false
  resetPasswordForm()
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordStrength.value = 0

  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

// 打开修改密码对话框（新增这个函数）
const openPasswordDialog = () => {
  resetPasswordForm()  // 重置表单
  passwordDialogVisible.value = true
}

// 打开我的帖子弹窗
const openMyPostsDialog = async () => {
  myPostsDialogVisible.value = true
  await loadMyPosts()
}

// 打开我的收藏弹窗
const openMyCollectionsDialog = async () => {
  myCollectionsDialogVisible.value = true
  await loadMyCollections()
}

// 打开关注者列表弹窗
const openFollowersDialog = () => {
  followersDialogVisible.value = true
}

// 打开关注列表弹窗
const openFollowingDialog = () => {
  followingDialogVisible.value = true
}

// 加载我的帖子（资讯+作品合并）
const loadMyPosts = async () => {
  const userId = localStorage.getItem('current_user_id')
  if (!userId) return
  try {
    myPostsList.value = []
    // 查资讯
    const articlesRes = await getArticleList({ userId, page: 1, size: 100 })
    if (articlesRes?.code === 200 && articlesRes.data?.records) {
      articlesRes.data.records.forEach(item => {
        myPostsList.value.push({ ...item, type: 'article' })
      })
    }
    // 查作品
    const worksRes = await getWorkList({ userId, page: 1, size: 100 })
    if (worksRes?.code === 200 && worksRes.data?.records) {
      worksRes.data.records.forEach(item => {
        myPostsList.value.push({ ...item, type: 'work' })
      })
    }
    myPostCount.value = myPostsList.value.length
  } catch (e) {
    console.error('加载帖子失败', e)
  }
}

// 加载我的收藏（资讯+作品合并）
const loadMyCollections = async () => {
  const userId = localStorage.getItem('current_user_id')
  if (!userId) return
  try {
    myCollectionsList.value = []
    const [articleRes, workRes] = await Promise.all([
      getMyArticleCollections(userId),
      getMyWorkCollections(userId)
    ])
    if (articleRes?.code === 200 && articleRes.data) {
      articleRes.data.forEach(item => {
        myCollectionsList.value.push({ ...item, type: 'article' })
      })
    }
    if (workRes?.code === 200 && workRes.data) {
      workRes.data.forEach(item => {
        myCollectionsList.value.push({ ...item, type: 'work' })
      })
    }
    myCollectionCount.value = myCollectionsList.value.length
  } catch (e) {
    console.error('加载收藏失败', e)
  }
}

// 删除帖子（仅删除作品，资讯不可删）
const handleDeletePost = async (item) => {
  try {
    await ElMessageBox.confirm('确定删除该帖子吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    if (item.type === 'work') {
      const res = await deleteWork(item.id, localStorage.getItem('current_user_id'))
      if (res?.code === 200) {
        ElMessage.success('删除成功')
        myPostsList.value = myPostsList.value.filter(i => i.id !== item.id)
        myPostCount.value = myPostsList.value.length
      } else {
        ElMessage.error(res?.message || '删除失败')
      }
    }
  } catch (e) {
    console.error('删除失败', e)
  }
}

// 跳转到帖子详情
const goToDetail = (item) => {
  if (item.type === 'work') {
    router.push(`/work/detail/${item.id}`)
  } else {
    router.push(`/article/detail/${item.id}`)
  }
}

// 跳转到收藏详情
const goToCollectionDetail = (item) => {
  if (item.type === 'work') {
    router.push(`/work/detail/${item.id}`)
  } else {
    router.push(`/article/detail/${item.id}`)
  }
}
</script>

<style scoped>
.profile-view {
  min-height: 600px;
}

/* 个人中心头部 */
.profile-header {
  position: relative;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border-radius: 15px 15px 0 0;
  padding: 40px;
  margin-bottom: 30px;
  overflow: hidden;
  min-height: 200px;
}

.profile-banner {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url('https://images.unsplash.com/photo-1544717305-99670f9c28f4?auto=format&fit=crop&w=1200&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.3;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.profile-avatar {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid white;
  overflow: hidden;
  margin: 0 auto 20px;
  cursor: pointer;
  z-index: 1;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.profile-avatar:hover .avatar-upload {
  opacity: 1;
}

.avatar-upload .el-icon {
  font-size: 1.5rem;
  margin-bottom: 5px;
}

.profile-info {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.profile-name {
  font-size: 1.8rem;
  margin: 0 0 10px 0;
  font-weight: bold;
}

.profile-bio {
  font-size: 1rem;
  margin: 0 0 20px 0;
  opacity: 0.9;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.5;
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s;
  padding: 10px;
  border-radius: 8px;
}

.stat-item:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.1);
}

.stat-number {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

/* 内容区域 */
.profile-content {
  display: flex;
  gap: 30px;
  padding: 0 30px 30px;
  flex-wrap: wrap;
}

.info-form {
  flex: 2;
  min-width: 300px;
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0e6d6;
}

.form-title {
  color: #333;
  font-size: 1.3rem;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0e6d6;
  position: relative;
}

.form-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 60px;
  height: 2px;
  background: #d4af37;
}

.profile-form :deep(.el-form-item__label) {
  color: #666;
  font-weight: 500;
}

.additional-info {
  flex: 1;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0e6d6;
}

.info-title {
  color: #333;
  font-size: 1.1rem;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0e6d6;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item:last-child {
  margin-bottom: 0;
}

.item-label {
  color: #666;
  font-size: 0.9rem;
}

.item-value {
  color: #333;
  font-weight: 500;
}

.status-active {
  color: #52c41a;
  font-weight: bold;
}

.security-btn {
  width: 100%;
  margin-bottom: 10px;
  justify-content: flex-start;
  padding: 12px 15px;
}

.security-btn:last-child {
  margin-bottom: 0;
}

.security-btn .el-icon {
  margin-right: 8px;
}

.achievements {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.achievement-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f5f0;
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  transition: transform 0.3s;
}

.achievement-item:hover {
  transform: translateX(5px);
  background: white;
  box-shadow: 0 2px 8px rgba(212, 175, 55, 0.1);
}

.achievement-item .el-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.achievement-item .el-icon.gold {
  color: #d4af37;
}

.achievement-item .el-icon.silver {
  color: #c0c0c0;
}

.achievement-item .el-icon.bronze {
  color: #cd7f32;
}

.achievement-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.achievement-name {
  color: #333;
  font-weight: 500;
  font-size: 0.95rem;
}

.achievement-desc {
  color: #666;
  font-size: 0.85rem;
}

.save-btn {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  padding: 10px 25px;
  font-size: 1rem;
  min-width: 120px;
}

.save-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}

/* 头像上传对话框 */
.avatar-upload-dialog {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-area {
  text-align: center;
  padding: 40px;
  border: 2px dashed #ddd;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  width: 100%;
}

.upload-area:hover {
  border-color: #d4af37;
  background: #f8f5f0;
}

.upload-icon {
  font-size: 48px;
  color: #999;
  margin-bottom: 10px;
}

.upload-tips {
  font-size: 0.9rem;
  color: #999;
  margin-top: 5px;
}

.preview-area {
  width: 100%;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 10px;
  border: 1px solid #f0e6d6;
}

/* 修改密码对话框样式 */
.password-change-dialog {
  padding: 10px 0 20px;
}

/* 对话框头部 */
.dialog-header {
  text-align: center;
  margin-bottom: 30px;
}

.header-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f8f5f0, #f0e6d6);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  border: 2px solid #f0e6d6;
}

.header-icon .el-icon {
  font-size: 28px;
  color: #d4af37;
}

.header-title {
  font-size: 20px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.header-subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

/* 表单 */
.password-form {
  max-width: 400px;
  margin: 0 auto;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
  padding-left: 4px;
}

.form-item-label .el-icon {
  color: #d4af37;
  font-size: 16px;
}

.password-input {
  width: 100%;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 15px;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  border-color: #d4af37;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #d4af37;
  box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.1);
}

:deep(.el-input__prefix) {
  color: #999;
  margin-right: 10px;
}

/* 密码强度提示 */
.password-strength-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  gap: 8px;
}

.hint-text {
  color: #666;
}

.strength-text {
  font-weight: bold;
  min-width: 30px;
}

.strength-text.very-weak {
  color: #ff4d4f;
}

.strength-text.weak {
  color: #fa8c16;
}

.strength-text.medium {
  color: #d4af37;
}

.strength-text.strong {
  color: #52c41a;
}

.strength-bar {
  display: flex;
  gap: 4px;
  flex: 1;
  height: 6px;
  border-radius: 3px;
  overflow: hidden;
  background: #f0f0f0;
}

.bar-segment {
  flex: 1;
  height: 100%;
  transition: all 0.3s;
}

.bar-segment.very-weak {
  background: #ff4d4f;
}

.bar-segment.weak {
  background: #fa8c16;
}

.bar-segment.medium {
  background: #d4af37;
}

.bar-segment.strong {
  background: #52c41a;
}

/* 密码提示 */
.password-tips {
  background: #f8f5f0;
  border-radius: 8px;
  padding: 12px 16px;
  margin: 20px 0 30px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.password-tips p {
  margin: 8px 0;
  color: #666;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 1.4;
}

.password-tips .el-icon {
  color: #d4af37;
  font-size: 14px;
  flex-shrink: 0;
}

/* 操作按钮 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.cancel-btn {
  min-width: 100px;
  padding: 10px 20px;
  border-radius: 6px;
  border: 1px solid #ddd;
  color: #666;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #f5f5f5;
  border-color: #ccc;
  color: #333;
}

.submit-btn {
  min-width: 100px;
  padding: 10px 20px;
  border-radius: 6px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  color: white;
  transition: all 0.3s;
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.2);
}

.submit-btn:active {
  transform: translateY(0);
}

/* 帖子/收藏弹窗 */
.my-items-dialog {
  min-height: 200px;
}

.empty-tip {
  text-align: center;
  color: #999;
  padding: 40px 0;
  font-size: 0.95rem;
}

.item-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 8px;
  border-bottom: 1px solid #f0e6d6;
  gap: 12px;
}

.item-row:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  min-width: 0;
}

.item-info:hover .item-title {
  color: #d4af37;
}

.item-title {
  color: #333;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s;
}

.item-type {
  font-size: 0.8rem;
  color: #fff;
  background: #d4af37;
  padding: 2px 8px;
  border-radius: 10px;
  white-space: nowrap;
  flex-shrink: 0;
}
</style>