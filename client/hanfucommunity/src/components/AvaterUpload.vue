<!-- src/components/AvatarUpload.vue -->
<template>
  <div class="avatar-upload">
    <!-- 头像展示 -->
    <div
        class="avatar-preview"
        :style="{ backgroundImage: `url(${previewUrl})` }"
        @click="triggerFileInput"
    >
      <!-- 上传提示 -->
      <div v-if="!previewUrl" class="upload-hint">
        <el-icon class="upload-icon"><Upload /></el-icon>
        <span>点击上传头像</span>
      </div>

      <!-- 上传遮罩 -->
      <div v-if="isHover" class="upload-overlay">
        <el-icon class="overlay-icon"><Camera /></el-icon>
        <span>更换头像</span>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploading" class="upload-progress">
        <el-progress
            type="circle"
            :percentage="uploadProgress"
            :width="80"
            :stroke-width="4"
            color="#d4af37"
        />
      </div>
    </div>

    <!-- 文件选择输入框 -->
    <input
        ref="fileInput"
        type="file"
        accept="image/*"
        style="display: none"
        @change="handleFileSelect"
    />

    <!-- 图片预览对话框 -->
    <el-dialog
        v-model="previewDialogVisible"
        title="图片预览"
        width="500px"
        align-center
    >
      <div class="image-preview-container">
        <img :src="dialogImageUrl" class="preview-image" />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="previewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpload">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Camera } from '@element-plus/icons-vue'
import { uploadAvatar } from '@/api/modules/user'

const props = defineProps({
  // 当前头像URL
  avatarUrl: {
    type: String,
    default: ''
  },
  // 用户ID
  userId: {
    type: [Number, String],
    default: null
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 头像大小
  size: {
    type: Number,
    default: 120
  }
})

const emit = defineEmits(['update:avatarUrl', 'upload-success'])

// 响应式数据
const isHover = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const previewDialogVisible = ref(false)
const dialogImageUrl = ref('')
const fileInput = ref(null)
const selectedFile = ref(null)

// 计算属性
const previewUrl = computed(() => {
  if (props.avatarUrl) {
    // 如果是相对路径，加上baseURL
    if (props.avatarUrl.startsWith('/')) {
      return `http://localhost:8080${props.avatarUrl}`
    }
    return props.avatarUrl
  }
  // 默认头像
  return 'http://localhost:8080/uploads/avatars/default.jpg'
})

// 触发文件选择
const triggerFileInput = () => {
  if (props.disabled) return
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择图片文件（JPG/PNG/GIF/BMP/WebP）')
    return
  }

  // 验证文件大小（5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  // 保存选择的文件
  selectedFile.value = file

  // 显示预览
  const reader = new FileReader()
  reader.onload = (e) => {
    dialogImageUrl.value = e.target.result
    previewDialogVisible.value = true
  }
  reader.readAsDataURL(file)

  // 清空input，以便可以重复选择同一个文件
  event.target.value = ''
}

// 确认上传
const confirmUpload = async () => {
  if (!selectedFile.value || !props.userId) {
    ElMessage.error('请先选择图片或用户ID不存在')
    return
  }

  previewDialogVisible.value = false

  try {
    uploading.value = true
    uploadProgress.value = 0

    // 调用上传接口
    const response = await uploadAvatar(props.userId, selectedFile.value)

    clearInterval(progressInterval)
    uploadProgress.value = 100

    if (response && (response.code === 200 || response.code === 0)) {
      const avatarUrl = response.data?.avatarUrl || response.avatarUrl

      if (avatarUrl) {
        // 更新头像URL
        emit('update:avatarUrl', avatarUrl)
        emit('upload-success', {
          avatarUrl,
          file: selectedFile.value,
          response
        })

        // 延迟重置状态
        setTimeout(() => {
          uploading.value = false
          uploadProgress.value = 0
          selectedFile.value = null
        }, 500)

        ElMessage.success(response.message || '头像上传成功')
      } else {
        throw new Error('未返回头像URL')
      }
    } else {
      throw new Error(response?.message || '上传失败')
    }

  } catch (error) {
    console.error('头像上传失败:', error)
    uploading.value = false
    uploadProgress.value = 0

    // 错误信息已经在拦截器中显示过了
    if (!error.message.includes('JSON')) {
      ElMessage.error(error.message || '头像上传失败')
    }
  }
}

// 鼠标事件
const handleMouseEnter = () => {
  if (!props.disabled && !uploading.value) {
    isHover.value = true
  }
}

const handleMouseLeave = () => {
  isHover.value = false
}
</script>

<style scoped>
.avatar-upload {
  display: inline-block;
  position: relative;
}

.avatar-preview {
  width: v-bind(size + 'px');
  height: v-bind(size + 'px');
  border-radius: 50%;
  background-color: #f5f5f5;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  border: 3px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  overflow: hidden;
  position: relative;
  transition: all 0.3s;
}

.avatar-preview:hover {
  box-shadow: 0 4px 20px rgba(212, 175, 55, 0.3);
  transform: scale(1.05);
}

.upload-hint {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

.upload-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: #d4af37;
}

.upload-overlay {
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

.avatar-preview:hover .upload-overlay {
  opacity: 1;
}

.overlay-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.upload-progress {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.image-preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
}
</style>