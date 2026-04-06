<template>
  <div class="posts-view">
    <!-- 板块标题 -->
    <div class="section-header">
      <h2 class="section-title">
        <el-icon><ChatLineSquare /></el-icon>
        互动帖子
      </h2>
      <p class="section-subtitle">与社区成员交流汉服心得，分享你的故事</p>
    </div>

    <!-- 工具栏 -->
    <div class="posts-toolbar">
      <!-- 搜索框 -->
      <el-input
          v-model="searchKeyword"
          placeholder="搜索帖子..."
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
        <el-option
            v-for="t in types"
            :key="t"
            :label="getTypeName(t)"
            :value="t"
        />
      </el-select>

      <!-- 排序选择 -->
      <el-select
          v-model="sortType"
          placeholder="排序方式"
          class="sort-select"
          @change="handleSortChange"
      >
        <el-option
            label="最新发布"
            value="latest"
        />
        <el-option
            label="最热浏览"
            value="hottest"
        />
      </el-select>

      <el-button type="primary" icon="Refresh" @click="refreshPosts">刷新</el-button>
      <el-button
          type="success"
          icon="Plus"
          @click="handleAddPost"
      >
        新增
      </el-button>
    </div>

    <!-- 帖子列表 -->
    <div class="posts-list">
      <div class="posts-grid">
        <div
            v-for="item in postsList"
            :key="item.id"
            class="post-card"
        >
          <div class="post-image" @click="viewPostDetail(item.id)">
            <img :src="getImageUrl(item.coverImage)" :alt="item.title" />
            <span class="type-badge">{{ getTypeName(item.type) }}</span>
          </div>
          <div class="post-content">
            <h4 @click="viewPostDetail(item.id)">{{ item.title }}</h4>
            <p class="post-excerpt">{{ item.description || '暂无描述' }}</p>
            <div class="post-meta">
              <span class="meta-item">
                <el-avatar :size="20" :src="getImageUrl(item.userAvatar)" />
                {{ item.userName || '匿名用户' }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ item.views || 0 }}
              </span>
              <span class="meta-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ item.comments || 0 }}
              </span>
            </div>
            <div class="post-actions">
              <el-button
                  type="info"
                  link
                  size="small"
                  @click.stop="viewPostDetail(item.id)"
              >
                查看
              </el-button>
              <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEdit(item.id)"
              >
                编辑
              </el-button>
              <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(item.id, item.title)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="postsList.length === 0 && !loading" class="empty-posts">
        <el-empty description="暂无帖子，快来发布第一篇吧~" />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[6, 12, 24]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatLineSquare,
  Search,
  View,
  ChatDotRound,
  Refresh,
  Plus
} from '@element-plus/icons-vue'
import { getWorkList, deleteWork, getWorkTypes } from '@/api/modules/work'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')
// 类型筛选
const typeFilter = ref('')
// 排序类型
const sortType = ref('latest')
// 加载状态
const loading = ref(false)
// 类型列表
const types = ref([])

// 帖子列表
const postsList = reactive([])

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 页面加载
onMounted(() => {
  loadTypes()
  loadPosts()
})

// 加载类型列表
const loadTypes = async () => {
  try {
    const response = await getWorkTypes()
    if (response && response.code === 200 && Array.isArray(response.data)) {
      types.value = response.data
    }
  } catch (error) {
    console.error('加载类型失败:', error)
  }
}

// 加载帖子列表
const loadPosts = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortType.value,
      keyword: searchKeyword.value || undefined,
      type: typeFilter.value || undefined
    }

    const response = await getWorkList(params)

    if (response && response.code === 200) {
      const data = response.data
      if (data && data.records) {
        postsList.length = 0
        data.records.forEach(item => {
          postsList.push(item)
        })
        total.value = data.total || 0
      }
    } else {
      console.warn('加载帖子失败:', response?.message)
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadPosts()
}

// 类型筛选
const handleTypeChange = () => {
  currentPage.value = 1
  loadPosts()
}

// 查看帖子详情
const viewPostDetail = (id) => {
  router.push(`/work/detail/${id}`)
}

// 新增帖子
const handleAddPost = () => {
  router.push('/work/create')
}

// 编辑帖子
const handleEdit = (id) => {
  router.push(`/work/edit/${id}`)
}

// 删除帖子
const handleDelete = async (id, title) => {
  try {
    const userStr = localStorage.getItem('hanfu_user')
    const user = userStr ? JSON.parse(userStr) : null
    if (!user) {
      ElMessage.warning('请先登录')
      router.push('/login')
      return
    }

    await ElMessageBox.confirm(
        `确定要删除帖子《${title || '未命名'}》吗？`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const response = await deleteWork(id, user.id)
    if (response && response.code === 200) {
      ElMessage.success('删除成功')
      refreshPosts()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 刷新
const refreshPosts = () => {
  currentPage.value = 1
  loadPosts()
  ElMessage.success('已刷新')
}

// 排序改变
const handleSortChange = () => {
  currentPage.value = 1
  loadPosts()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadPosts()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadPosts()
}

// 获取图片URL
const getImageUrl = (path) => {
  if (!path) {
    return 'https://images.unsplash.com/photo-1544717305-99670f9c28f4?auto=format&fit=crop&w=400&q=80'
  }
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }
  if (path.startsWith('/')) {
    return `http://localhost:8080${path}`
  }
  return `http://localhost:8080/${path}`
}

// 获取类型名称
const getTypeName = (type) => {
  const typeMap = {
    'photography': '摄影',
    'design': '设计',
    'dressing': '穿搭'
  }
  return typeMap[type] || type || '摄影'
}
</script>

<style scoped>
.posts-view {
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

.posts-toolbar {
  display: flex;
  gap: 15px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  align-items: center;
  padding: 20px;
  background: #f8f5f0;
  border-radius: 10px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.search-input {
  width: 200px;
}

.type-select {
  width: 150px;
}

.sort-select {
  width: 150px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(212, 175, 55, 0.1);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.3);
}

.post-image {
  height: 200px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.post-card:hover .post-image img {
  transform: scale(1.05);
}

.type-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(212, 175, 55, 0.9);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.post-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-content h4 {
  font-size: 1.1rem;
  color: #333;
  margin: 0 0 10px 0;
  cursor: pointer;
  line-height: 1.4;
}

.post-content h4:hover {
  color: #d4af37;
}

.post-excerpt {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.post-meta {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.post-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #999;
  font-size: 0.85rem;
}

.post-meta .el-avatar {
  margin-right: 2px;
}

.post-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
  padding-top: 10px;
}

.post-actions .el-button {
  font-size: 13px;
}

.empty-posts {
  padding: 60px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding: 20px 0;
}

.pagination-wrapper :deep(.el-pagination) {
  font-weight: 500;
}

.pagination-wrapper :deep(.el-pagination__total) {
  font-size: 14px;
}

@media (max-width: 768px) {
  .posts-view {
    padding: 20px;
  }

  .posts-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .type-select,
  .sort-select {
    width: 100%;
  }

  .posts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
