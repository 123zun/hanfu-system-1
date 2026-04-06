<template>
  <div class="news-view">
    <!-- 板块标题 -->
    <div class="section-header">
      <h2 class="section-title">
        <el-icon><Reading /></el-icon>
        汉服资讯
      </h2>
      <p class="section-subtitle">了解最新汉服动态，关注传统文化发展</p>
    </div>

    <!-- 搜索和筛选 -->
    <div class="news-toolbar">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索资讯..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <!-- 分类筛选部分修改 -->
      <el-select
          v-model="categoryFilter"
          placeholder="分类筛选"
          clearable
          class="category-select"
          @change="handleCategoryChange"
      >
        <el-option
            v-for="category in categories"
            :key="category.code"
            :label="category.name"
            :value="category.code"
        />
      </el-select>

      <el-button type="primary" icon="Refresh" @click="refreshNews">刷新</el-button>
      <el-button
          type="success"
          icon="Plus"
          @click="handleAddArticle"
      >
        新增
      </el-button>
    </div>

    <!-- 资讯列表 -->
    <div class="news-list">
      <!-- 热门资讯 -->
      <div class="featured-news">
        <h3 class="list-title">热门资讯</h3>
        <div
            v-for="item in featuredList"
            :key="item.id"
            class="featured-item"
        >
          <div class="featured-image">
            <img :src="getImageUrl(item.coverImage)" alt="汉服文化" />
            <div class="image-overlay">
              <span class="hot-tag" v-if="item.isHot || item.views > 1000">热门</span>
            </div>
          </div>
          <div class="featured-content">
            <h4>{{ item.title }}</h4>
            <p class="news-excerpt">{{ item.excerpt || '暂无摘要' }}...</p>
            <div class="news-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(item.publishTime) }}
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                {{ item.views || 0 }} 浏览
              </span>
              <span class="meta-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ item.comments || 0 }} 评论
              </span>
              <span class="meta-item category-tag">{{ getCategoryName(item.category) }}</span>
            </div>
            <div class="article-actions">
              <el-button
                  type="info"
                  link
                  size="small"
                  @click.stop="viewNewsDetail(item.id)"
                  icon="View">查看</el-button>
              <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEdit(item.id)"
                  icon="Edit">编辑</el-button>
              <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(item.id, item.title)"
                  icon="Delete">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 最新资讯 -->
      <div class="latest-news">
        <h3 class="list-title">最新资讯</h3>
        <div class="news-grid">
          <div
              v-for="item in newsList"
              :key="item.id"
              class="news-card"
          >
            <div class="news-card-image">
              <img :src="getImageUrl(item.coverImage)" :alt="item.title" />
            </div>
            <div class="news-card-content">
              <h4>{{ item.title }}</h4>
              <p class="news-card-excerpt">{{ item.excerpt || '暂无摘要' }}</p>
              <div class="news-card-meta">
                <span class="meta-item">{{ formatDate(item.publishTime) }}</span>
                <span class="meta-item">{{ item.views || 0 }} 浏览</span>
              </div>
              <span class="category-badge">{{ getCategoryName(item.category) }}</span>
            </div>
            <div class="card-actions">
              <el-button
                  type="info"
                  link
                  size="small"
                  @click.stop="viewNewsDetail(item.id)"
                  icon="View">查看</el-button>
              <el-button
                  type="primary"
                  link
                  size="small"
                  @click="handleEdit(item.id)"
                  icon="Edit">编辑</el-button>
              <el-button
                  type="danger"
                  link
                  size="small"
                  @click="handleDelete(item.id, item.title)"
                  icon="Delete">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :page-sizes="[6, 10, 15]"
              :total="total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Reading,
  Search,
  Calendar,
  View,
  ChatDotRound,
  Refresh,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { getArticleList, getHotArticles ,getArticleCategories ,deleteArticle ,increaseArticleView} from '@/api/modules/article'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')
// 分类筛选
const categoryFilter = ref('')
// 加载状态
const loading = ref(false)
// 分类列表
const categories = ref([])

// 资讯列表数据
const featuredList = reactive([])  // 热门资讯
const newsList = reactive([])  // 普通资讯

// 分页参数
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)

// 页面加载
onMounted(() => {
  loadCategories()
  loadInitialData()
})

// 查看资讯详情
const viewNewsDetail = (id) => {
  increaseArticleView(id)
  router.push(`/article/detail/${id}`)
}

const handleAddArticle = () => {
  router.push('/article')
}

// 编辑资讯
const handleEdit = (id) => {
  router.push(`/article/edit/${id}`)
}

// 删除资讯
const handleDelete = async (id, title) => {
  try {
    // 确认对话框
    await ElMessageBox.confirm(
        `确定要删除资讯《${title}》吗？`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          confirmButtonClass: 'el-button--danger'
        }
    )

    // 调用删除接口
    const response = await deleteArticle(id)

    if (response && response.code === 200) {
      ElMessage.success('删除成功')

      // 刷新列表
      refreshNews()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 加载初始数据
const loadInitialData = async () => {
  try {
    loading.value = true

    // 加载热门资讯
    await loadHotArticles()

    // 加载普通资讯
    await loadArticles()

  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await getArticleCategories()
    console.log('分类接口返回:', response)

    if (response && response.code === 200) {
      const data = response.data

      if (Array.isArray(data)) {
        // 后端返回的是字符串数组，我们需要转换为对象数组
        categories.value = data.map(category => {
          // 假设后端返回的格式是: ['industry', 'activity', 'academic']
          return {
            code: category,
            name: getCategoryDisplayName(category)  // 将编码转为中文显示名
          }
        })

        console.log('转换后的分类列表:', categories.value)
      } else {
        // 如果返回格式不对，使用默认分类
        setDefaultCategories()
      }
    } else {
      console.warn('分类接口返回错误:', response?.message)
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
    'fashion': '穿搭分享',
    'news': '新闻资讯',
    'guide': '攻略指南'
  }

  return categoryMap[categoryCode] || categoryCode || '未分类'
}

// 加载热门资讯
const loadHotArticles = async () => {
  try {
    const response = await getHotArticles(2)  // 只取2条作为热门

    if (response && response.code === 200) {
      // 清空原有数据
      featuredList.length = 0

      // 添加新的数据
      if (response.data && Array.isArray(response.data)) {
        response.data.forEach(item => {
          featuredList.push(item)
        })
      }
    } else {
      // 如果接口失败，从普通资讯中取前2条
      console.warn('热门资讯接口失败，使用默认数据')
    }
  } catch (error) {
    console.error('加载热门资讯失败:', error)
  }
}

// 加载资讯列表
const loadArticles = async () => {
  try {
    loading.value = true
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }

    if (categoryFilter.value) {
      params.category = categoryFilter.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const response = await getArticleList(params)

    console.log("资讯列表",response);

    if (response && response.code === 200) {
      const data = response.data

      if (data && data.records) {
        // 清空原有数据
        newsList.length = 0

        // 添加新的数据
        data.records.forEach(item => {
          newsList.push(item)
        })

        // 更新总数
        total.value = data.total || 0

        // 如果热门资讯为空，用前2条普通资讯作为热门
        if (featuredList.length === 0 && data.records.length >= 2) {
          featuredList.length = 0
          featuredList.push(...data.records.slice(0, 2))
        }
      } else {
        // 处理不同的返回格式
        if (Array.isArray(data)) {
          newsList.length = 0
          data.forEach(item => {
            newsList.push(item)
          })
          total.value = data.length || 0

          if (featuredList.length === 0 && data.length >= 2) {
            featuredList.length = 0
            featuredList.push(...data.slice(0, 2))
          }
        }
      }
    } else {
      console.warn('资讯列表接口返回错误:', response?.message)
    }
  } catch (error) {
    console.error('加载资讯失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索资讯
const handleSearch = () => {
  currentPage.value = 1
  loadArticles()
}

// 刷新资讯
const refreshNews = () => {
  currentPage.value = 1
  loadInitialData()
  ElMessage.success('资讯已刷新')
}

// 分页大小改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadArticles()
}

// 页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  loadArticles()
}

// 辅助函数：格式化日期
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

// 辅助函数：获取图片URL
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

// 分类筛选
const handleCategoryChange = () => {
  currentPage.value = 1
  loadArticles()
}

// 辅助函数：获取分类名称
const getCategoryName = (categoryCode) => {
  if (!categoryCode) return '未分类'

  const category = categories.value.find(item => item.code === categoryCode)
  return category ? category.name : '未分类'
}
</script>

<style scoped>
/* 保持原有样式完全不变 */

.article-actions {
  margin-top: 15px;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
  padding-top: 10px;
}

.card-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.article-actions .el-button,
.card-actions .el-button {
  font-size: 13px;
}

.article-actions .el-button:hover,
.card-actions .el-button:hover {
  transform: scale(1.05);
}

.news-view {
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

.news-toolbar {
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
  flex: 1;
  min-width: 200px;
  max-width: 400px;
}

.category-select {
  width: 150px;
}

.news-list {
  max-width: 1200px;
  margin: 0 auto;
}

.list-title {
  font-size: 1.3rem;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0e6d6;
  position: relative;
}

.list-title::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 60px;
  height: 2px;
  background: #d4af37;
}

/* 热门资讯 */
.featured-news {
  margin-bottom: 40px;
}

.featured-item {
  display: flex;
  gap: 30px;
  padding: 20px;
  background: #f8f5f0;
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  cursor: pointer;
  transition: all 0.3s;
  flex-wrap: wrap;
}

.featured-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(212, 175, 55, 0.1);
  background: white;
}

.featured-image {
  flex: 0 0 300px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.featured-image img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  transition: transform 0.3s;
}

.featured-item:hover .featured-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, rgba(0,0,0,0.1), rgba(0,0,0,0.3));
}

.hot-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ff4d4f;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
}

.featured-content {
  flex: 1;
  min-width: 300px;
}

.featured-content h4 {
  font-size: 1.4rem;
  color: #333;
  margin: 0 0 15px 0;
  line-height: 1.3;
}

.news-excerpt {
  color: #666;
  line-height: 1.6;
  margin: 0 0 15px 0;
  font-size: 0.95rem;
}

.news-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 0.9rem;
}

.meta-item .el-icon {
  font-size: 1rem;
}

.category-tag {
  background: rgba(212, 175, 55, 0.1);
  color: #d4af37;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

/* 最新资讯 */
.latest-news {
  margin-bottom: 30px;
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.news-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(212, 175, 55, 0.1);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.news-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.3);
}

.news-card-image {
  height: 180px;
  overflow: hidden;
}

.news-card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.news-card:hover .news-card-image img {
  transform: scale(1.05);
}

.news-card-content {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.news-card-content h4 {
  font-size: 1.1rem;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
  flex: 1;
}

.news-card-excerpt {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.5;
  margin: 0 0 15px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.news-card-meta .meta-item {
  font-size: 0.85rem;
}

.category-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(212, 175, 55, 0.1);
  color: #d4af37;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.8rem;
  border: 1px solid rgba(212, 175, 55, 0.3);
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30px 0;
}

.load-more-btn {
  min-width: 150px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  padding: 12px 30px;
  font-size: 1rem;
}

.load-more-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}

/* 分页样式 */
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

.pagination-wrapper :deep(.el-pagination .el-select .el-input) {
  width: 110px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .news-view {
    padding: 20px;
  }

  .featured-item {
    flex-direction: column;
  }

  .featured-image {
    flex: none;
    width: 100%;
  }

  .news-grid {
    grid-template-columns: 1fr;
  }

  .news-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .category-select {
    width: 100%;
    max-width: none;
  }
}
</style>