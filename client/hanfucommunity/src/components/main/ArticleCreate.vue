<template>
  <div class="article-create-view">
    <!-- 返回按钮 -->
    <div class="back-section">
      <el-button
          type="text"
          icon="ArrowLeft"
          @click="goBack"
          class="back-btn"
      >
        返回资讯列表
      </el-button>
    </div>

    <!-- 创建表单 -->
    <div class="create-form-container">
      <h2 class="form-title">
        <el-icon><Edit /></el-icon>
        {{ isEdit ? '编辑资讯' : '创建资讯' }}
      </h2>

      <el-form
          ref="articleFormRef"
          :model="articleForm"
          :rules="articleRules"
          label-width="100px"
          class="article-form"
      >
        <!-- 标题 -->
        <el-form-item label="标&emsp;&emsp;题" prop="title">
          <el-input
              v-model="articleForm.title"
              placeholder="请输入资讯标题"
              clearable
              maxlength="200"
              show-word-limit
          />
        </el-form-item>

        <!-- 分类 -->
        <el-form-item label="分&emsp;&emsp;类" prop="category">
          <el-select
              v-model="articleForm.category"
              placeholder="请选择分类"
              clearable
              class="category-select"
          >
            <el-option
                v-for="category in categories"
                :key="category.code"
                :label="category.name"
                :value="category.code"
            />
          </el-select>
        </el-form-item>

        <!-- 封面图 -->
        <el-form-item label="封面图">
          <div class="cover-upload">
            <div
                v-if="!articleForm.coverImage"
                class="upload-area"
                @click="triggerCoverUpload"
            >
              <el-icon class="upload-icon"><Picture /></el-icon>
              <p>点击上传封面图</p>
              <p class="upload-tips">建议尺寸 800×400 像素</p>
            </div>
            <div v-else class="cover-preview">
              <img :src="getImageUrl(articleForm.coverImage)" class="cover-image" />
              <div class="cover-actions">
                <el-button
                    type="text"
                    icon="Delete"
                    @click="removeCoverImage"
                    class="remove-btn"
                >
                  移除
                </el-button>
                <el-button
                    type="text"
                    icon="Upload"
                    @click="triggerCoverUpload"
                    class="change-btn"
                >
                  更换
                </el-button>
              </div>
            </div>
            <input
                ref="coverInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleCoverSelect"
            />
          </div>
        </el-form-item>

        <!-- 摘要 -->
        <el-form-item label="摘&emsp;&emsp;要" prop="excerpt">
          <el-input
              v-model="articleForm.excerpt"
              type="textarea"
              :rows="3"
              placeholder="请输入资讯摘要（选填）"
              maxlength="500"
              show-word-limit
          />
        </el-form-item>

        <!-- 内容（使用富文本编辑器） -->
        <el-form-item label="内&emsp;&emsp;容" prop="content">
          <RichTextEditor
              v-model="articleForm.content"
              placeholder="请输入资讯内容，支持图文混排..."
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
              class="submit-btn"
          >
            {{ submitting ? '提交中...' : isEdit ? '更新资讯' : '发布资讯' }}
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
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Picture,
  Delete,
  Upload
} from '@element-plus/icons-vue'
import { getArticleCategories, createArticle, updateArticle, getArticleDetail } from '@/api/modules/article'
import { uploadCover, uploadContentImage } from '@/api/modules/upload'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

const router = useRouter()
const route = useRoute()

// 是否是编辑模式
const isEdit = computed(() => route.name === 'article-edit' || route.query.id)

// 表单引用
const articleFormRef = ref()
const coverInput = ref(null)

// 表单数据
const articleForm = reactive({
  id: null,
  title: '',
  category: '',
  coverImage: '',
  excerpt: '',
  content: ''
})

// 分类列表
const categories = ref([])

// 加载状态
const submitting = ref(false)
const savingDraft = ref(false)

// 表单验证规则
const articleRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度在5-200个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.replace(/<[^>]+>/g, '').trim().length < 10) {
          callback(new Error('内容不能少于10个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 页面加载
onMounted(() => {
  loadCategories()

  // 检查是否有文章ID（编辑模式）
  const articleId = route.params.id || route.query.id
  if (articleId) {
    loadArticleData(articleId)
  }

  // 尝试加载草稿
  loadDraft()
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await getArticleCategories()
    if (response && response.code === 200 && Array.isArray(response.data)) {
      categories.value = response.data.map(category => ({
        code: category,
        name: getCategoryDisplayName(category)
      }))
    } else {
      setDefaultCategories()
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    setDefaultCategories()
  }
}

// 设置默认分类
const setDefaultCategories = () => {
  categories.value = [
    { code: 'industry', name: '行业动态' },
    { code: 'activity', name: '活动资讯' },
    { code: 'academic', name: '学术研究' },
    { code: 'fashion', name: '穿搭分享' }
  ]
}

// 将分类编码转为中文显示名
const getCategoryDisplayName = (categoryCode) => {
  const categoryMap = {
    'industry': '行业动态',
    'activity': '活动资讯',
    'academic': '学术研究',
    'fashion': '穿搭分享'
  }
  return categoryMap[categoryCode] || categoryCode || '未分类'
}

// 加载文章数据（编辑模式）
const loadArticleData = async (id) => {
  try {
    const response = await getArticleDetail(id)
    if (response && response.code === 200) {
      const data = response.data
      articleForm.id = data.id
      articleForm.title = data.title || ''
      articleForm.category = data.category || ''
      articleForm.coverImage = data.coverImage || ''
      articleForm.excerpt = data.excerpt || ''
      articleForm.content = data.content || ''
    } else {
      ElMessage.error('加载文章失败')
    }
  } catch (error) {
    console.error('加载文章详情失败:', error)
    ElMessage.error('加载失败')
  }
}

// 加载草稿
const loadDraft = () => {
  try {
    const draftStr = localStorage.getItem('article_draft')
    if (draftStr) {
      const draft = JSON.parse(draftStr)
      // 可选：询问是否加载草稿
      if (draft.title || draft.content) {
        ElMessageBox.confirm('检测到未发布的草稿，是否继续编辑？', '提示', {
          confirmButtonText: '加载草稿',
          cancelButtonText: '忽略',
          type: 'info'
        }).then(() => {
          articleForm.title = draft.title || ''
          articleForm.category = draft.category || ''
          articleForm.coverImage = draft.coverImage || ''
          articleForm.excerpt = draft.excerpt || ''
          articleForm.content = draft.content || ''
          ElMessage.success('草稿加载成功')
        }).catch(() => {
          localStorage.removeItem('article_draft')
        })
      }
    }
  } catch (error) {
    console.error('加载草稿失败:', error)
  }
}

// 触发封面图上传
const triggerCoverUpload = () => {
  if (coverInput.value) {
    coverInput.value.click()
  }
}

// 处理封面图选择
const handleCoverSelect = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件
  if (!validateImageFile(file)) return

  try {
    const formData = new FormData()
    formData.append('file', file)

    const response = await uploadCover(formData)

    if (response && response.code === 200) {
      const imageUrl = response.data?.url || response.data?.coverUrl
      if (imageUrl) {
        articleForm.coverImage = imageUrl
        ElMessage.success('封面上传成功')
      } else {
        ElMessage.error('封面上传失败：未返回URL')
      }
    } else {
      ElMessage.error(response?.message || '封面上传失败')
    }
  } catch (error) {
    loading.close()
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败')
  }

  // 重置input
  event.target.value = ''
}

// 移除封面图
const removeCoverImage = () => {
  articleForm.coverImage = ''
}

// 验证图片文件
const validateImageFile = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择图片文件（JPG/PNG/GIF/WebP）')
    return false
  }

  const maxSize = 5 * 1024 * 1024 // 5MB
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }

  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!articleFormRef.value) return

  try {
    await articleFormRef.value.validate()

    submitting.value = true

    // 获取当前用户信息
    const userStr = localStorage.getItem('hanfu_user')
    if (!userStr) {
      ElMessage.error('用户信息不存在，请重新登录')
      return
    }

    const user = JSON.parse(userStr)

    // 准备提交数据
    const articleData = {
      title: articleForm.title,
      content: articleForm.content,
      excerpt: articleForm.excerpt,
      coverImage: articleForm.coverImage,
      category: articleForm.category,
      authorId: user.id,
      authorName: user.username || user.nickname || '匿名',
      authorAvatar: user.avatar || '',
      status: 1 // 已发布
    }

    let response

    if (isEdit.value && articleForm.id) {
      // 更新文章
      response = await updateArticle(articleForm.id, articleData)
    } else {
      // 创建文章
      response = await createArticle(articleData)
    }

    if (response && response.code === 200) {
      ElMessage.success(isEdit.value ? '资讯更新成功' : '资讯发布成功')

      // 清除草稿
      localStorage.removeItem('article_draft')

      // 延迟跳转
      setTimeout(() => {
        router.push('/main')
      }, 1000)
    } else {
      ElMessage.error(response?.message || '操作失败')
    }

  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 返回
const goBack = () => {
  // 如果有未保存的内容，提示保存
  if (articleForm.title || articleForm.content) {
    ElMessageBox.confirm('内容未保存，是否保存？', '提示', {
      confirmButtonText: '保存',
      cancelButtonText: '不保存',
      type: 'warning'
    }).then(() => {
      handleSubmit()
      setTimeout(() => {
        router.push('/main')
      }, 500)
    }).catch(() => {
      router.push('/main')
    })
  } else {
    router.push('/main')
  }
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  if (path.startsWith('data:')) {
    return path
  }
  if (path.startsWith('/')) {
    return `http://localhost:8080${path}`
  }
  return `http://localhost:8080/${path}`
}
</script>

<style scoped>
/* 保持原有样式不变，添加富文本编辑器相关样式 */
.article-create-view {
  padding: 30px;
  min-height: 600px;
  max-width: 1000px;
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

.create-form-container {
  background: white;
  border-radius: 10px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0e6d6;
}

.form-title {
  font-size: 1.5rem;
  color: #333;
  margin: 0 0 30px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0e6d6;
  position: relative;
}

.form-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100px;
  height: 2px;
  background: #d4af37;
}

.form-title .el-icon {
  color: #d4af37;
  font-size: 1.5rem;
}

.article-form {
  max-width: 800px;
  margin: 0 auto;
}

.cover-upload {
  width: 100%;
}

.upload-area {
  width: 100%;
  padding: 40px 20px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f8f5f0;
}

.upload-area:hover {
  border-color: #d4af37;
  background: white;
}

.upload-icon {
  font-size: 48px;
  color: #999;
  margin-bottom: 10px;
}

.upload-tips {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.cover-preview {
  position: relative;
  width: 100%;
  max-width: 400px;
}

.cover-image {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #f0e6d6;
}

.cover-actions {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  gap: 10px;
  background: rgba(0, 0, 0, 0.6);
  padding: 5px 10px;
  border-radius: 4px;
}

.cover-actions .el-button {
  color: white;
  padding: 4px 8px;
}

.cover-actions .el-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.submit-btn {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  padding: 10px 25px;
  min-width: 120px;
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}
</style>