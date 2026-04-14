<template>
  <div class="resources-view">
    <!-- 板块标题 -->
    <div class="section-header">
      <h2 class="section-title">
        <el-icon><FolderOpened /></el-icon>
        资源共享
      </h2>
      <p class="section-subtitle">上传、下载、预览汉服相关图片、视频与文档资料</p>
    </div>

    <!-- 搜索和筛选 -->
    <div class="resources-toolbar">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索资源..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <!-- 类型筛选 -->
      <el-select
          v-model="typeFilter"
          placeholder="类型筛选"
          clearable
          class="type-select"
          @change="handleTypeChange"
      >
        <el-option label="全部" value="" />
        <el-option label="图片" value="image" />
        <el-option label="视频" value="video" />
        <el-option label="文档" value="document" />
      </el-select>

      <el-button type="primary" icon="Refresh" @click="refreshResources">刷新</el-button>
      <el-button type="success" icon="Upload" @click="showUploadDialog = true">
        上传资源
      </el-button>
    </div>

    <!-- 资源统计 -->
    <div class="resources-stats">
      <div class="stat-card" @click="typeFilter = ''; handleTypeChange()">
        <el-icon><Files /></el-icon>
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">全部资源</span>
      </div>
      <div class="stat-card" @click="typeFilter = 'image'; handleTypeChange()">
        <el-icon><Picture /></el-icon>
        <span class="stat-num">{{ typeStats.image || 0 }}</span>
        <span class="stat-label">图片</span>
      </div>
      <div class="stat-card" @click="typeFilter = 'video'; handleTypeChange()">
        <el-icon><VideoPlay /></el-icon>
        <span class="stat-num">{{ typeStats.video || 0 }}</span>
        <span class="stat-label">视频</span>
      </div>
      <div class="stat-card" @click="typeFilter = 'document'; handleTypeChange()">
        <el-icon><Document /></el-icon>
        <span class="stat-num">{{ typeStats.document || 0 }}</span>
        <span class="stat-label">文档</span>
      </div>
    </div>

    <!-- 资源列表 -->
    <div class="resources-grid" v-if="resourceList.length > 0">
      <div
          v-for="item in resourceList"
          :key="item.id"
          class="resource-card"
      >
        <!-- 预览区 -->
        <div class="resource-preview" @click="handlePreview(item)">
          <img
              v-if="item.type === 'image'"
              :src="getFileUrl(item.fileUrl)"
              :alt="item.title"
              class="preview-image"
          />
          <div v-else-if="item.type === 'video'" class="preview-video">
            <el-icon><VideoPlay /></el-icon>
            <span>点击播放</span>
          </div>
          <div v-else-if="item.type === 'document'" class="preview-document">
            <el-icon><Document /></el-icon>
            <span>{{ getFileExtension(item.fileUrl) }}</span>
          </div>
          <div v-else class="preview-other">
            <el-icon><Files /></el-icon>
            <span>点击查看</span>
          </div>
          <div class="preview-overlay">
            <span class="type-badge">{{ getTypeName(item.type) }}</span>
          </div>
        </div>

        <!-- 资源信息 -->
        <div class="resource-info">
          <h4 class="resource-title" :title="item.title">{{ item.title }}</h4>
          <p class="resource-desc" v-if="item.description">{{ item.description }}</p>
          <div class="resource-meta">
            <span class="meta-item">
              <el-icon><User /></el-icon>
              {{ item.uploaderName || '未知' }}
            </span>
            <span class="meta-item">
              <el-icon><Download /></el-icon>
              {{ item.downloadCount || 0 }}
            </span>
          </div>
          <div class="resource-size">
            <el-icon><Document /></el-icon>
            {{ item.fileSizeFormat || '未知大小' }}
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="resource-actions">
          <el-button
              type="primary"
              link
              size="small"
              icon="View"
              @click="handlePreview(item)"
          >预览</el-button>
          <el-button
              type="success"
              link
              size="small"
              icon="Download"
              @click="handleDownload(item.id)"
          >下载</el-button>
          <el-button
              type="info"
              link
              size="small"
              icon="Edit"
              @click="handleEdit(item)"
          >编辑</el-button>
          <el-button
              type="danger"
              link
              size="small"
              icon="Delete"
              @click="handleDelete(item.id, item.title)"
          >删除</el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <el-icon><Folder /></el-icon>
      <p>暂无资源，试试上传或调整筛选条件</p>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 上传对话框 -->
    <el-dialog
        v-model="showUploadDialog"
        title="上传资源"
        width="600px"
        :close-on-click-modal="false"
        :lock-scroll="false"
    >
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="资源标题" required>
          <el-input v-model="uploadForm.title" placeholder="请输入资源标题（不填则用文件名）" />
        </el-form-item>

        <el-form-item label="资源描述">
          <el-input
              v-model="uploadForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入资源描述（可选）"
          />
        </el-form-item>

        <el-form-item label="资源类型" required>
          <el-select v-model="uploadForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="图片" value="image" />
            <el-option label="视频" value="video" />
            <el-option label="文档" value="document" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择文件" required>
          <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :file-list="fileList"
              drag
              class="upload-area"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持图片、视频、文档等文件，单个文件不超过100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpload" :loading="uploading">
          上传
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog
        v-model="showEditDialog"
        title="编辑资源"
        width="600px"
        :close-on-click-modal="false"
        :lock-scroll="false"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="资源标题" required>
          <el-input v-model="editForm.title" placeholder="请输入资源标题" />
        </el-form-item>

        <el-form-item label="资源描述">
          <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="3"
              placeholder="请输入资源描述"
          />
        </el-form-item>

        <el-form-item label="资源类型" required>
          <el-select v-model="editForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="图片" value="image" />
            <el-option label="视频" value="video" />
            <el-option label="文档" value="document" />
          </el-select>
        </el-form-item>

        <el-form-item label="文件信息">
          <div class="edit-file-info">
            <span>文件名：{{ editForm.fileUrl ? editForm.fileUrl.split('/').pop() : '未知' }}</span>
            <span class="ml-10">大小：{{ editForm.fileSizeFormat || '未知' }}</span>
            <span class="ml-10">上传者：{{ editForm.uploaderName || '未知' }}</span>
          </div>
          <div class="edit-file-tip">如需更换文件，请删除后重新上传</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate" :loading="updateLoading">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
        v-model="showPreviewDialog"
        :title="previewItem?.title"
        width="800px"
        class="preview-dialog"
        :lock-scroll="false"
    >
      <!-- 图片预览 -->
      <div v-if="previewItem?.type === 'image'" class="preview-content">
        <img :src="getFileUrl(previewItem.fileUrl)" :alt="previewItem.title" />
      </div>

      <!-- 视频预览 -->
      <div v-else-if="previewItem?.type === 'video'" class="preview-content">
        <video
            :src="getFileUrl(previewItem.fileUrl)"
            controls
            autoplay
            class="preview-video-player"
        >
          您的浏览器不支持视频播放
        </video>
      </div>

      <!-- 文档预览（通过后端预览接口内嵌显示） -->
      <div v-else class="preview-content preview-document-detail">
        <iframe
            :src="`http://localhost:8080/api/resource/file/${previewItem?.id}?preview=true`"
            class="preview-doc-frame"
            frameborder="0"
        />
        <div class="preview-doc-footer">
          <el-button type="primary" @click="handleDownload(previewItem?.id)">
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
        </div>
      </div>

      <!-- 文件信息 -->
      <div class="preview-info">
        <div class="info-item">
          <span class="label">上传者：</span>
          <span>{{ previewItem?.uploaderName || '未知' }}</span>
        </div>
        <div class="info-item">
          <span class="label">类型：</span>
          <span>{{ getTypeName(previewItem?.type) }}</span>
        </div>
        <div class="info-item">
          <span class="label">下载次数：</span>
          <span>{{ previewItem?.downloadCount || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">上传时间：</span>
          <span>{{ formatDate(previewItem?.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  FolderOpened,
  Search,
  Refresh,
  Upload,
  Files,
  Picture,
  VideoPlay,
  Document,
  User,
  Download,
  Edit,
  Delete,
  Folder,
  UploadFilled,
  View
} from '@element-plus/icons-vue'
import {
  getResourceList,
  getResourceTypes,
  deleteResource,
  downloadResource,
  uploadResource,
  updateResource
} from '@/api/modules/resource'

// 搜索和筛选
const searchKeyword = ref('')
const typeFilter = ref('')

// 资源列表
const resourceList = reactive([])
const typeStats = reactive({})

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 上传对话框
const showUploadDialog = ref(false)
const uploading = ref(false)
const uploadRef = ref(null)
const fileList = ref([])

const uploadForm = reactive({
  title: '',
  description: '',
  type: 'document'
})

// 预览对话框
const showPreviewDialog = ref(false)
const previewItem = ref(null)

// 编辑对话框
const showEditDialog = ref(false)
const updateLoading = ref(false)
const editForm = reactive({
  id: null,
  title: '',
  description: '',
  type: 'document',
  fileUrl: '',
  fileSizeFormat: '',
  uploaderName: ''
})

// 当前用户
const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem('hanfu_user')
    if (userStr) {
      return JSON.parse(userStr)
    }
  } catch (e) {
    console.error('获取用户失败:', e)
  }
  return null
}

// 页面加载
onMounted(() => {
  loadTypeStats()
  loadResources()
})

// 加载资源列表
const loadResources = async () => {
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (typeFilter.value) {
      params.type = typeFilter.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await getResourceList(params)

    if (response && response.code === 200) {
      const data = response.data
      if (data && data.records) {
        resourceList.length = 0
        data.records.forEach(item => {
          resourceList.push(item)
        })
        total.value = data.total || 0
      }
    }
  } catch (error) {
    console.error('加载资源失败:', error)
    ElMessage.error('加载资源失败')
  }
}

// 加载各类型统计
const loadTypeStats = async () => {
  try {
    const types = ['image', 'video', 'document']
    for (const type of types) {
      const response = await getResourceList({ type, size: 1, status: 1 })
      if (response && response.code === 200 && response.data) {
        typeStats[type] = response.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadResources()
}

// 刷新
const refreshResources = () => {
  currentPage.value = 1
  loadResources()
  loadTypeStats()
  ElMessage.success('已刷新')
}

// 类型筛选
const handleTypeChange = () => {
  currentPage.value = 1
  loadResources()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadResources()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadResources()
}

// 文件变化
const handleFileChange = (file, files) => {
  fileList.value = files
  // 自动设置标题为文件名
  if (!uploadForm.title && file.name) {
    uploadForm.title = file.name.substring(0, file.name.lastIndexOf('.'))
  }
}

// 文件移除
const handleFileRemove = () => {
  fileList.value = []
}

// 上传资源
const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  try {
    uploading.value = true

    const user = getCurrentUser()
    const file = fileList.value[0].raw

    const uploadParams = {
      title: uploadForm.title,
      description: uploadForm.description,
      type: uploadForm.type,
      uploaderId: user?.id,
      uploaderName: user?.username || user?.nickname || '未知用户'
    }

    const response = await uploadResource(file, uploadParams)

    if (response && response.code === 200) {
      ElMessage.success('上传成功')

      // 重置表单
      uploadForm.title = ''
      uploadForm.description = ''
      uploadForm.type = 'document'
      fileList.value = []
      showUploadDialog.value = false

      // 刷新列表
      loadResources()
      loadTypeStats()
    } else {
      ElMessage.error(response?.message || '上传失败')
    }
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    uploading.value = false
  }
}

// 预览
const handlePreview = (item) => {
  previewItem.value = item
  showPreviewDialog.value = true
}

// 下载（通过隐藏iframe触发真实下载）
const handleDownload = async (id) => {
  try {
    const response = await downloadResource(id)

    if (response && response.code === 200) {
      // 通过隐藏的iframe访问后端下载接口，后端设置 Content-Disposition: attachment 强制下载
      const iframe = document.createElement('iframe')
      iframe.style.display = 'none'
      iframe.src = `http://localhost:8080/api/resource/file/${id}`
      document.body.appendChild(iframe)
      setTimeout(() => {
        document.body.removeChild(iframe)
      }, 5000)
      ElMessage.success('开始下载')
      loadResources()
    } else {
      ElMessage.error(response?.message || '获取下载链接失败')
    }
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请稍后重试')
  }
}

// 编辑
const handleEdit = (item) => {
  // 回显编辑表单
  editForm.id = item.id
  editForm.title = item.title || ''
  editForm.description = item.description || ''
  editForm.type = item.type || 'document'
  editForm.fileUrl = item.fileUrl || ''
  editForm.fileSizeFormat = item.fileSizeFormat || ''
  editForm.uploaderName = item.uploaderName || ''
  showEditDialog.value = true
}

// 保存编辑
const handleUpdate = async () => {
  if (!editForm.title) {
    ElMessage.warning('请输入资源标题')
    return
  }

  try {
    updateLoading.value = true

    const response = await updateResource(editForm.id, {
      title: editForm.title,
      description: editForm.description,
      type: editForm.type
    })

    if (response && response.code === 200) {
      ElMessage.success('更新成功')
      showEditDialog.value = false
      loadResources()
    } else {
      ElMessage.error(response?.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败，请稍后重试')
  } finally {
    updateLoading.value = false
  }
}

// 删除
const handleDelete = async (id, title) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除资源《${title}》吗？`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const response = await deleteResource(id)

    if (response && response.code === 200) {
      ElMessage.success('删除成功')
      loadResources()
      loadTypeStats()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 工具函数
const getFileUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  if (path.startsWith('/')) {
    return `http://localhost:8080${path}`
  }
  return `http://localhost:8080/${path}`
}

const getFileExtension = (path) => {
  if (!path) return ''
  const ext = path.substring(path.lastIndexOf('.') + 1).toUpperCase()
  return ext || 'FILE'
}

const getTypeName = (type) => {
  const typeMap = {
    image: '图片',
    video: '视频',
    document: '文档',
  }
  return typeMap[type] || '文档'
}

const formatDate = (dateString) => {
  if (!dateString) return '未知日期'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch (e) {
    return dateString
  }
}
</script>

<style scoped>
.resources-view {
  padding: 30px;
  min-height: 600px;
}

.section-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0e6d6;
  position: relative;
}

.section-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 2px;
  background: #d4af37;
}

.section-title {
  font-size: 1.8rem;
  color: #333;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.section-title .el-icon {
  color: #d4af37;
  font-size: 1.8rem;
}

.section-subtitle {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

/* 工具栏 */
.resources-toolbar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  align-items: center;
  padding: 20px;
  background: #f8f5f0;
  border-radius: 10px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.search-input {
  flex: 1;
  min-width: 200px;
  max-width: 300px;
}

.type-select {
  width: 130px;
}

/* 统计卡片 */
.resources-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 120px;
  background: #f8f5f0;
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  background: white;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.15);
}

.stat-card .el-icon {
  font-size: 1.8rem;
  color: #d4af37;
  margin-bottom: 8px;
}

.stat-num {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

/* 资源网格 */
.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.resource-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(212, 175, 55, 0.15);
  transition: all 0.3s;
}

.resource-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.4);
}

/* 预览区 */
.resource-preview {
  position: relative;
  height: 160px;
  background: #f5f5f5;
  cursor: pointer;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.resource-card:hover .preview-image {
  transform: scale(1.05);
}

.preview-video,
.preview-document,
.preview-other {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: linear-gradient(135deg, #f8f5f0, #e8e0d0);
}

.preview-video .el-icon,
.preview-document .el-icon,
.preview-other .el-icon {
  font-size: 2.5rem;
  color: #d4af37;
}

.preview-video span,
.preview-document span,
.preview-other span {
  font-size: 0.85rem;
  color: #666;
}

.preview-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
}

.type-badge {
  background: rgba(212, 175, 55, 0.9);
  color: white;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

/* 资源信息 */
.resource-info {
  padding: 15px;
}

.resource-title {
  font-size: 1rem;
  color: #333;
  margin: 0 0 8px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-desc {
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 0.8rem;
}

.meta-item .el-icon {
  font-size: 0.9rem;
}

.resource-size {
  font-size: 0.8rem;
  color: #999;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 操作按钮 */
.resource-actions {
  padding: 10px 15px;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.resource-actions .el-button {
  font-size: 13px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.empty-state .el-icon {
  font-size: 3rem;
  color: #d4af37;
  margin-bottom: 15px;
}

.empty-state p {
  font-size: 1rem;
  margin: 0;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 上传对话框 */
.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  background: #f8f5f0;
  border-color: rgba(212, 175, 55, 0.4);
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #d4af37;
}

/* 预览对话框 */
.preview-content {
  text-align: center;
  margin-bottom: 20px;
}

.preview-content img {
  max-width: 100%;
  max-height: 500px;
  border-radius: 8px;
}

.preview-video-player {
  width: 100%;
  max-height: 500px;
  border-radius: 8px;
}

.preview-document-detail {
  padding: 40px;
  background: #f8f5f0;
  border-radius: 10px;
}

.preview-document-detail .el-icon {
  color: #d4af37;
}

.preview-document-detail p {
  margin: 15px 0 5px;
  font-size: 1.1rem;
  color: #333;
}

.file-info {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 20px;
}

.preview-info {
  background: #f8f5f0;
  border-radius: 8px;
  padding: 15px 20px;
}

.info-item {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid rgba(212, 175, 55, 0.1);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #666;
  width: 80px;
}

/* 编辑对话框 */
.edit-file-info {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.6;
}

.edit-file-info .ml-10 {
  margin-left: 15px;
}

.edit-file-tip {
  color: #999;
  font-size: 0.8rem;
  margin-top: 5px;
}

.info-item span:last-child {
  color: #333;
}

/* 响应式 */
@media (max-width: 768px) {
  .resources-view {
    padding: 20px;
  }

  .resources-grid {
    grid-template-columns: 1fr;
  }

  .resources-toolbar {
    flex-direction: column;
  }

  .search-input,
  .type-select {
    width: 100%;
    max-width: none;
  }

  .resources-stats {
    flex-direction: column;
  }
}
</style>
