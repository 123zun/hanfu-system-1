<template>
  <div class="work-create-view">
    <div class="back-section">
      <el-button type="text" icon="ArrowLeft" @click="goBack" class="back-btn">
        返回帖子列表
      </el-button>
    </div>

    <div class="create-form-container">
      <h2 class="form-title">
        <el-icon><Edit /></el-icon>
        {{ isEdit ? '编辑帖子' : '发布帖子' }}
      </h2>

      <el-form ref="workFormRef" :model="workForm" :rules="workRules" label-width="100px" class="work-form">
        <el-form-item label="标&emsp;&emsp;题" prop="title">
          <el-input v-model="workForm.title" placeholder="请输入帖子标题" clearable maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="类&emsp;&emsp;别" prop="type">
          <el-select v-model="workForm.type" placeholder="请选择类别" class="type-select">
            <el-option label="摄影" value="photography" />
            <el-option label="设计" value="design" />
            <el-option label="穿搭" value="dressing" />
          </el-select>
        </el-form-item>

        <el-form-item label="封&ensp;面&ensp;图">
          <div class="cover-upload">
            <div v-if="!workForm.coverImage" class="upload-area" @click="triggerCoverUpload">
              <el-icon class="upload-icon"><Picture /></el-icon>
              <p>点击上传封面图</p>
              <p class="upload-tips">建议尺寸 800×400 像素</p>
            </div>
            <div v-else class="cover-preview">
              <img :src="getImageUrl(workForm.coverImage)" class="cover-image" />
              <div class="cover-actions">
                <el-button type="text" @click="removeCoverImage" class="remove-btn">移除</el-button>
                <el-button type="text" @click="triggerCoverUpload" class="change-btn">更换</el-button>
              </div>
            </div>
            <input ref="coverInput" type="file" accept="image/*" style="display: none" @change="handleCoverSelect" />
          </div>
        </el-form-item>

        <el-form-item label="内&emsp;&emsp;容" prop="content">
          <RichTextEditor v-model="workForm.content" placeholder="请输入帖子内容，支持图文混排..." />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" class="submit-btn">
            {{ submitting ? '提交中...' : isEdit ? '保存修改' : '发布帖子' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Picture } from '@element-plus/icons-vue'
import { getWorkDetail, createWorkPost, updateWorkPost, uploadWorkCover } from '@/api/modules/work'
import RichTextEditor from '@/components/common/WorkRichTextEditor.vue'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const workFormRef = ref(null)
const coverInput = ref(null)
const submitting = ref(false)

const workForm = reactive({
  id: null,
  title: '',
  type: 'photography',
  coverImage: '',
  content: ''
})

const workRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在2-200个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类别', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

onMounted(() => {
  if (isEdit.value) {
    loadWorkData()
  }
})

const loadWorkData = async () => {
  try {
    const response = await getWorkDetail(route.params.id)
    if (response && response.code === 200) {
      const data = response.data
      workForm.id = data.id
      workForm.title = data.title || ''
      workForm.type = data.type || 'photography'
      workForm.coverImage = data.coverImage || ''
      workForm.content = data.description || ''
    } else {
      ElMessage.error('加载帖子失败')
      goBack()
    }
  } catch (error) {
    console.error('加载帖子详情失败:', error)
    ElMessage.error('加载失败')
  }
}

const triggerCoverUpload = () => {
  coverInput.value?.click()
}

const handleCoverSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  if (!validateImageFile(file)) return

  try {
    const formData = new FormData()
    formData.append('file', file)
    const response = await uploadWorkCover(formData)

    if (response && response.code === 200) {
      const imageUrl = response.data?.url
      if (imageUrl) {
        workForm.coverImage = imageUrl
        ElMessage.success('封面上传成功')
      } else {
        ElMessage.error('封面上传失败：未返回URL')
      }
    } else {
      ElMessage.error(response?.message || '封面上传失败')
    }
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败')
  }

  event.target.value = ''
}

const removeCoverImage = () => {
  workForm.coverImage = ''
}

const validateImageFile = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择图片文件（JPG/PNG/GIF/WebP）')
    return false
  }
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!workFormRef.value) return

  try {
    await workFormRef.value.validate()
  } catch (e) {
    return
  }

  const userStr = localStorage.getItem('hanfu_user')
  if (!userStr) {
    ElMessage.error('用户信息不存在，请重新登录')
    return
  }

  const user = JSON.parse(userStr)
  submitting.value = true

  try {
    const workData = {
      title: workForm.title,
      type: workForm.type,
      coverImage: workForm.coverImage,
      description: workForm.content,
      userId: user.id
    }

    let response
    if (isEdit.value) {
      workData.id = workForm.id
      response = await updateWorkPost(workData)
    } else {
      response = await createWorkPost(workData)
    }

    if (response && response.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      goBack()
    } else {
      ElMessage.error(response?.message || (isEdit.value ? '修改失败' : '发布失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '修改失败' : '发布失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/main')
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/')) return `http://localhost:8080${path}`
  return `http://localhost:8080/${path}`
}
</script>

<style scoped>
.work-create-view {
  padding: 30px;
  min-height: 100vh;
  background: #f5f5f5;
}
.back-section { max-width: 900px; margin: 0 auto 20px; }
.back-btn { color: #666; font-size: 14px; }
.back-btn:hover { color: #d4af37; }
.create-form-container {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.form-title {
  font-size: 1.4rem;
  color: #333;
  margin: 0 0 30px 0;
  display: flex;
  align-items: center;
  gap: 10px;
}
.form-title .el-icon { color: #d4af37; }
.type-select { width: 200px; }
.cover-upload { width: 100%; }
.upload-area {
  border: 2px dashed #ddd;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
}
.upload-area:hover { border-color: #d4af37; background: #fffbf0; }
.upload-icon { font-size: 40px; color: #ccc; margin-bottom: 10px; }
.upload-area p { margin: 5px 0; color: #666; font-size: 14px; }
.upload-tips { font-size: 12px !important; color: #999 !important; }
.cover-preview { position: relative; display: inline-block; width: 100%; max-width: 400px; }
.cover-image { width: 100%; max-height: 200px; object-fit: cover; border-radius: 8px; }
.cover-actions { display: flex; gap: 10px; justify-content: center; margin-top: 10px; }
.remove-btn { color: #f56c6c; }
.change-btn { color: #409eff; }
.submit-btn {
  min-width: 120px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
}
.submit-btn:hover { opacity: 0.9; }
@media (max-width: 768px) {
  .work-create-view { padding: 15px; }
  .create-form-container { padding: 20px; }
}
</style>
