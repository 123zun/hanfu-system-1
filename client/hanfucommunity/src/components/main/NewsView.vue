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

      <el-select
          v-model="categoryFilter"
          placeholder="分类筛选"
          clearable
          class="category-select"
      >
        <el-option label="行业动态" value="industry" />
        <el-option label="活动资讯" value="activity" />
        <el-option label="学术研究" value="academic" />
        <el-option label="穿搭分享" value="fashion" />
      </el-select>

      <el-button type="primary" icon="Refresh" @click="refreshNews">刷新</el-button>
    </div>

    <!-- 资讯列表 -->
    <div class="news-list">
      <!-- 热门资讯 -->
      <div class="featured-news">
        <h3 class="list-title">热门资讯</h3>
        <div class="featured-item" @click="viewNewsDetail(1)">
          <div class="featured-image">
            <img src="https://images.unsplash.com/photo-1544717305-99670f9c28f4?auto=format&fit=crop&w=600&q=80" alt="汉服文化" />
            <div class="image-overlay">
              <span class="hot-tag">热门</span>
            </div>
          </div>
          <div class="featured-content">
            <h4>汉服文化复兴研讨会在京成功举办</h4>
            <p class="news-excerpt">近日，全国汉服文化复兴研讨会在北京国家会议中心成功举办。来自全国各地的汉服爱好者、专家学者、行业代表等200余人参加了此次盛会...</p>
            <div class="news-meta">
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                2026-03-20
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                1,234 浏览
              </span>
              <span class="meta-item">
                <el-icon><ChatDotRound /></el-icon>
                56 评论
              </span>
              <span class="meta-item category-tag">行业动态</span>
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
              @click="viewNewsDetail(item.id)"
          >
            <div class="news-card-image">
              <img :src="item.image" :alt="item.title" />
            </div>
            <div class="news-card-content">
              <h4>{{ item.title }}</h4>
              <p class="news-card-excerpt">{{ item.excerpt }}</p>
              <div class="news-card-meta">
                <span class="meta-item">{{ item.date }}</span>
                <span class="meta-item">{{ item.views }} 浏览</span>
              </div>
              <span class="category-badge">{{ item.category }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载更多 -->
      <div class="load-more">
        <el-button
            type="primary"
            :loading="loading"
            @click="loadMore"
            class="load-more-btn"
        >
          {{ loading ? '加载中...' : '加载更多' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Reading,
  Search,
  Calendar,
  View,
  ChatDotRound,
  Refresh
} from '@element-plus/icons-vue'

// 搜索关键词
const searchKeyword = ref('')
// 分类筛选
const categoryFilter = ref('')
// 加载状态
const loading = ref(false)

// 资讯列表数据
const newsList = reactive([
  {
    id: 1,
    title: '汉服文化复兴研讨会在京成功举办',
    excerpt: '近日，全国汉服文化复兴研讨会在北京国家会议中心成功举办。来自全国各地的汉服爱好者、专家学者、行业代表等200余人参加了此次盛会。',
    image: 'https://images.unsplash.com/photo-1544717305-99670f9c28f4?auto=format&fit=crop&w=400&q=80',
    date: '2026-03-20',
    views: 1234,
    category: '行业动态'
  },
  {
    id: 2,
    title: '传统节日活动预告：清明踏青汉服游园会',
    excerpt: '为弘扬传统文化，本社区将于清明节期间举办汉服游园会，邀请广大汉服爱好者共同参与，体验传统节日的魅力。',
    image: 'https://images.unsplash.com/photo-1519167758481-83f550bb49b3?auto=format&fit=crop&w=400&q=80',
    date: '2026-03-18',
    views: 856,
    category: '活动资讯'
  },
  {
    id: 3,
    title: '汉服穿搭技巧：春夏季节的汉服选择与搭配',
    excerpt: '春夏季节是穿着汉服的好时节，如何根据不同场合选择适合的汉服款式？本文为您详细介绍春夏汉服的穿搭技巧。',
    image: 'https://images.unsplash.com/photo-1560557776-5d5f72b6c96d?auto=format&fit=crop&w=400&q=80',
    date: '2026-03-15',
    views: 942,
    category: '穿搭分享'
  },
  {
    id: 4,
    title: '汉服摄影大赛开始报名，丰厚奖品等你来拿',
    excerpt: '第四届汉韵华章汉服摄影大赛正式启动，面向全国汉服爱好者征集优秀摄影作品，最高奖金5000元。',
    image: 'https://images.unsplash.com/photo-1551103782-8ab07afd45c1?auto=format&fit=crop&w=400&q=80',
    date: '2026-03-12',
    views: 671,
    category: '活动资讯'
  },
  {
    id: 5,
    title: '传统文化讲座直播：汉服的历史演变与发展',
    excerpt: '本周末将举办线上汉服文化讲座，邀请知名历史学者讲解汉服的历史演变与现代发展。',
    image: 'https://images.unsplash.com/photo-1578662996442-48f60103fc96?auto=format&fit=crop&w=400&q=80',
    date: '2026-03-10',
    views: 523,
    category: '学术研究'
  },
  {
    id: 6,
    title: '汉服制作工艺：传统刺绣技法的现代应用',
    excerpt: '介绍传统汉服刺绣工艺及其在现代汉服设计中的应用，展现传统手工艺的魅力。',
    image: 'https://images.unsplash.com/photo-1582555172866-f73bb12a2ab3?auto=format&fit=crop&w=400&q=80',
    date: '2026-03-08',
    views: 789,
    category: '学术研究'
  }
])

// 查看资讯详情
const viewNewsDetail = (id) => {
  ElMessage.info(`查看资讯详情 ID: ${id}`)
  // 这里可以跳转到详情页
}

// 搜索资讯
const handleSearch = () => {
  if (searchKeyword.value) {
    ElMessage.info(`搜索: ${searchKeyword.value}`)
  }
}

// 刷新资讯
const refreshNews = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('资讯已刷新')
  }, 1000)
}

// 加载更多
const loadMore = () => {
  loading.value = true
  setTimeout(() => {
    // 模拟加载更多数据
    const newItems = [
      {
        id: newsList.length + 1,
        title: '汉服文化走进校园，培养学生文化自信',
        excerpt: '越来越多的学校将汉服文化引入课堂，通过汉服体验活动培养学生的文化自信和民族认同感。',
        image: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=400&q=80',
        date: '2026-03-05',
        views: 432,
        category: '行业动态'
      },
      {
        id: newsList.length + 2,
        title: '汉服租赁服务兴起，降低汉服体验门槛',
        excerpt: '随着汉服热的兴起，汉服租赁服务逐渐普及，让更多人能够以较低成本体验汉服文化。',
        image: 'https://images.unsplash.com/photo-1520004434532-668416a08753?auto=format&fit=crop&w=400&q=80',
        date: '2026-03-03',
        views: 315,
        category: '行业动态'
      }
    ]
    newsList.push(...newItems)
    loading.value = false
    ElMessage.success(`新增 ${newItems.length} 条资讯`)
  }, 1500)
}
</script>

<style scoped>
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