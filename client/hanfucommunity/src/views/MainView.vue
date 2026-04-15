<!-- src/views/MainView.vue -->
<template>
  <div class="main-container">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-left">
        <div class="logo">
          <h1>汉韵华章</h1>
          <p class="subtitle">汉服文化社区</p>
        </div>
      </div>

      <div class="navbar-center">
        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <el-menu
              :default-active="activeMenu"
              mode="horizontal"
              @select="handleMenuSelect"
              class="nav-menu-list"
          >
            <el-menu-item index="news">
              <el-icon><Reading /></el-icon>
              资讯
            </el-menu-item>
            <el-menu-item index="posts">
              <el-icon><ChatLineSquare /></el-icon>
              互动帖子
            </el-menu-item>
            <el-menu-item index="activities">
              <el-icon><Calendar /></el-icon>
              活动
            </el-menu-item>
            <el-menu-item index="resources">
              <el-icon><Collection /></el-icon>
              资源
            </el-menu-item>
            <el-menu-item v-if="isAdminUser" index="users">
              <el-icon><UserFilled /></el-icon>
              用户管理
            </el-menu-item>
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-menu-item>
          </el-menu>
        </nav>
      </div>

      <div class="navbar-right">
        <!-- 用户信息 -->
        <div class="user-info" @click="goToProfile">
          <div class="user-avatar">
            <img :src="userInfo.avatar || defaultAvatar" alt="用户头像" />
          </div>
          <div class="user-details">
            <div class="username">{{ userInfo.username || '汉服爱好者' }}</div>
            <div class="user-status">
              <span class="status-dot online"></span>
              <span class="status-text">在线</span>
            </div>
          </div>
        </div>

        <!-- 退出登录 -->
        <el-button
            type="text"
            class="logout-btn"
            @click="handleLogout"
        >
          <el-icon><SwitchButton /></el-icon>
          退出
        </el-button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 左侧边栏 -->
      <aside class="sidebar" v-if="showSidebar">
        <div class="sidebar-content">
          <!-- 用户信息卡片 -->
          <div class="user-card">
            <div class="user-card-avatar">
              <img :src="userInfo.avatar || defaultAvatar" alt="用户头像" />
            </div>
            <h3 class="user-card-name">{{ userInfo.username || '汉服爱好者' }}</h3>
            <p class="user-card-bio">{{ userInfo.bio || '传承华夏文明，弘扬汉服文化' }}</p>
            <div class="user-card-stats">
              <div class="stat-item">
                <span class="stat-number">{{ userInfo.postCount || 0 }}</span>
                <span class="stat-label">帖子</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userInfo.followers || 0 }}</span>
                <span class="stat-label">关注者</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userInfo.following || 0 }}</span>
                <span class="stat-label">关注中</span>
              </div>
            </div>
          </div>

          <div class="sidebar-section">
            <h3>今日热点</h3>
            <ul class="hot-list">
              <li
                  v-for="item in hotList"
                  :key="item.id + item.type"
                  @click="goToHotItem(item)"
              >
                {{ item.title }}
                <span class="hot-views">{{ item.views || 0 }}浏览</span>
              </li>
              <li v-if="hotList.length === 0" class="empty-tip">暂无热点内容</li>
            </ul>
          </div>

          <div class="sidebar-section">
            <h3>社区数据</h3>
            <div class="community-stats">
              <div class="stat-item">
                <span class="stat-number">{{ communityStats.activityCount }}</span>
                <span class="stat-label">开展活动</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ communityStats.userCount }}</span>
                <span class="stat-label">注册用户</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ communityStats.articleCount + communityStats.workCount }}</span>
                <span class="stat-label">总帖子数</span>
              </div>
            </div>
          </div>

          <!-- 快速导航 -->
          <div class="sidebar-section">
            <h3>快速导航</h3>
            <div class="quick-nav">
              <el-button
                  type="primary"
                  icon="Plus"
                  class="quick-nav-btn"
                  @click="handleCreatePost"
              >
                发布帖子
              </el-button>
              <el-button
                  type="success"
                  icon="Calendar"
                  class="quick-nav-btn"
                  @click="handleJoinActivity"
              >
                参加活动
              </el-button>
              <el-button
                  type="warning"
                  icon="Setting"
                  class="quick-nav-btn"
                  @click="goToProfile"
              >
                个人设置
              </el-button>
            </div>
          </div>
        </div>
      </aside>

      <!-- 右侧内容区 -->
      <section class="content-area">
        <!-- 动态组件，根据当前菜单显示不同内容 -->
        <component :is="currentComponent" />
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h4>汉韵华章</h4>
          <p>传承华夏文明，弘扬汉服文化</p>
          <p>让传统服饰走进现代生活</p>
        </div>
        <div class="footer-section">
          <h4>联系我们</h4>
          <p><el-icon><Message /></el-icon> 邮箱：contact@hanyunhuazhang.com</p>
          <p><el-icon><Phone /></el-icon> 电话：400-123-4567</p>
          <p><el-icon><Location /></el-icon> 地址：中国·成都</p>
        </div>
        <div class="footer-section">
          <h4>关注我们</h4>
          <div class="social-links">
            <el-tooltip content="微信公众号" placement="top">
              <el-icon><ChatDotRound /></el-icon>
            </el-tooltip>
            <el-tooltip content="微博" placement="top">
              <el-icon><VideoCamera /></el-icon>
            </el-tooltip>
            <el-tooltip content="抖音" placement="top">
              <el-icon><Camera /></el-icon>
            </el-tooltip>
            <el-tooltip content="B站" placement="top">
              <el-icon><Film /></el-icon>
            </el-tooltip>
          </div>
          <p class="social-tips">扫码关注，获取最新资讯</p>
        </div>
        <div class="footer-section">
          <h4>友情链接</h4>
          <div class="friend-links">
            <a href="#" class="friend-link">汉服网</a>
            <a href="#" class="friend-link">传统文化论坛</a>
            <a href="#" class="friend-link">汉服制作教程</a>
            <a href="#" class="friend-link">传统节日日历</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2026 汉韵华章汉服文化社区 | 传承华夏文明，弘扬汉服文化 | 蜀ICP备12345678号</p>
        <p class="footer-copyright">本网站所有内容均为汉服文化爱好者原创，未经许可不得转载</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Reading,
  ChatLineSquare,
  Calendar,
  Collection,
  User,
  UserFilled,
  SwitchButton,
  ChatDotRound,
  VideoCamera,
  Camera,
  Film,
  Message,
  Phone,
  Location,
  Plus,
  Setting
} from '@element-plus/icons-vue'

// 导入各个板块组件
import NewsView from '@/components/main/NewsView.vue'
import ProfileView from '@/components/main/ProfileView.vue'
import PostsView from '@/components/main/PostsView.vue'
import ActivitiesView from '@/components/main/ActivityView.vue'
import ResourcesView from '@/components/main/ResourcesView.vue'
import UsersView from '@/components/main/UsersView.vue'

// API
import { getHotArticles } from '@/api/modules/article'
import { getHotWorks, getWorkList } from '@/api/modules/work'
import { getDashboardStats } from '@/api/modules/dashboard'
import { isAdmin } from '@/utils/permission'

const router = useRouter()

// 默认头像
const defaultAvatar = 'http://localhost:8080/uploads/avatars/default.png'

// 用户信息
const userInfo = ref({})

// 是否是管理员
const isAdminUser = isAdmin()

// 今日热点（合并热门资讯和热门帖子）
const hotList = ref([])

// 社区统计数据
const communityStats = ref({
  activityCount: 0,
  userCount: 0,
  articleCount: 0,
  workCount: 0
})

// 当前激活的菜单
const activeMenu = ref('news')

// 是否显示侧边栏
const showSidebar = ref(true)

// 组件映射
const componentMap = {
  news: markRaw(NewsView),
  posts: markRaw(PostsView),
  activities: markRaw(ActivitiesView),
  resources: markRaw(ResourcesView),
  users: markRaw(UsersView),
  profile: markRaw(ProfileView)
}

// 当前显示的组件
const currentComponent = computed(() => {
  return componentMap[activeMenu.value] || markRaw(NewsView)
})

// 页面加载
onMounted(() => {
  loadUserInfo()
  loadHotList()
  loadCommunityStats()
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const userStr = localStorage.getItem('hanfu_user')
    if (userStr) {
      const user = JSON.parse(userStr)
      userInfo.value = user
      // 加载当前用户帖子数
      await loadUserPostCount(user.id)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 加载当前用户帖子数
const loadUserPostCount = async (userId) => {
  try {
    const res = await getWorkList({ userId, page: 1, size: 1, sort: 'latest' })
    if (res && res.code === 200 && res.data) {
      userInfo.value.postCount = res.data.total || 0
    }
  } catch (error) {
    console.error('加载帖子数失败:', error)
  }
}

// 加载今日热点（热门资讯 + 热门帖子，按浏览量排序取前5）
const loadHotList = async () => {
  try {
    const [articlesRes, worksRes] = await Promise.all([
      getHotArticles(5),
      getHotWorks(5)
    ])

    const items = []

    if (articlesRes && articlesRes.code === 200 && articlesRes.data) {
      articlesRes.data.forEach(a => {
        items.push({
          id: a.id,
          title: a.title,
          views: a.views,
          type: 'article'
        })
      })
    }

    if (worksRes && worksRes.code === 200 && worksRes.data) {
      worksRes.data.forEach(w => {
        items.push({
          id: w.id,
          title: w.title,
          views: w.views,
          type: 'work'
        })
      })
    }

    // 按浏览量排序，取前5
    items.sort((a, b) => (b.views || 0) - (a.views || 0))
    hotList.value = items.slice(0, 5)
  } catch (error) {
    console.error('加载热点失败:', error)
  }
}

// 加载社区统计数据
const loadCommunityStats = async () => {
  try {
    const res = await getDashboardStats()
    if (res && res.code === 200 && res.data) {
      communityStats.value = {
        activityCount: res.data.activityCount || 0,
        userCount: res.data.userCount || 0,
        articleCount: res.data.articleCount || 0,
        workCount: res.data.workCount || 0
      }
    }
  } catch (error) {
    console.error('加载社区数据失败:', error)
  }
}

// 跳转热点内容
const goToHotItem = (item) => {
  if (item.type === 'article') {
    router.push(`/article/detail/${item.id}`)
  } else {
    router.push(`/work/detail/${item.id}`)
  }
}

// 处理菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

// 跳转到个人中心
const goToProfile = () => {
  activeMenu.value = 'profile'
}

// 跳转到资讯
const goToNews = () => {
  activeMenu.value = 'news'
}

// 发布帖子
const handleCreatePost = () => {
  activeMenu.value = 'posts'
}

// 参加活动
const handleJoinActivity = () => {
  activeMenu.value = 'activities'
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    center: true
  }).then(() => {
    // 清除登录状态
    localStorage.removeItem('hanfu_token')
    localStorage.removeItem('hanfu_user')
    localStorage.removeItem('current_user_id')

    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 取消退出
  })
}
</script>

<style scoped>
.main-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0e6d6 100%);
  position: relative;
  overflow-x: hidden;
}

/* 顶部导航栏样式 */
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 80px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 250, 240, 0.95) 100%);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.navbar-left {
  flex: 0 0 auto;
}

.logo h1 {
  font-size: 1.8rem;
  background: linear-gradient(135deg, #d4af37, #8b4513);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  font-family: 'STKaiti', 'KaiTi', serif;
  letter-spacing: 1px;
}

.logo .subtitle {
  font-size: 0.8rem;
  color: #666;
  margin: 2px 0 0 0;
  letter-spacing: 2px;
}

.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  width: auto;
}

.nav-menu-list {
  background: transparent;
  border: none;
  height: 80px;
}

.nav-menu-list :deep(.el-menu-item) {
  height: 80px;
  line-height: 80px;
  font-size: 1rem;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  margin: 0 5px;
}

.nav-menu-list :deep(.el-menu-item):hover {
  background: transparent;
  color: #d4af37;
  border-bottom-color: #d4af37;
}

.nav-menu-list :deep(.el-menu-item.is-active) {
  color: #d4af37;
  background: transparent;
  border-bottom-color: #d4af37;
  font-weight: bold;
}

.nav-menu-list :deep(.el-menu-item .el-icon) {
  margin-right: 8px;
  font-size: 1.2rem;
}

.navbar-right {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.user-info:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.1);
  transform: translateY(-2px);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #d4af37;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.user-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #52c41a;
}

.status-dot.online {
  background: #52c41a;
}

.status-text {
  font-size: 0.8rem;
  color: #666;
}

.logout-btn {
  padding: 8px 16px;
  color: #666;
  transition: all 0.3s;
}

.logout-btn:hover {
  color: #d4af37;
  background: rgba(212, 175, 55, 0.1);
  border-radius: 5px;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  max-width: 1600px;
  margin: 0 auto;
  padding: 30px 20px;
  width: 100%;
  gap: 30px;
  min-height: calc(100vh - 240px);
}

/* 侧边栏 */
.sidebar {
  flex: 0 0 300px;
  position: sticky;
  top: 110px;
  height: fit-content;
}

.sidebar-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(212, 175, 55, 0.1);
  backdrop-filter: blur(10px);
}

/* 用户卡片 */
.user-card {
  text-align: center;
  padding: 20px 0;
  margin-bottom: 25px;
  border-bottom: 1px solid #f0e6d6;
}

.user-card-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #d4af37;
  margin: 0 auto 15px;
}

.user-card-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-card-name {
  font-size: 1.2rem;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.user-card-bio {
  font-size: 0.9rem;
  color: #666;
  margin: 0 0 15px 0;
  line-height: 1.4;
}

.user-card-stats {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.user-card-stats .stat-item {
  text-align: center;
}

.user-card-stats .stat-number {
  display: block;
  font-size: 1.1rem;
  font-weight: bold;
  color: #d4af37;
  margin-bottom: 2px;
}

.user-card-stats .stat-label {
  font-size: 0.8rem;
  color: #999;
}

.sidebar-section {
  margin-bottom: 25px;
}

.sidebar-section:last-child {
  margin-bottom: 0;
}

.sidebar-section h3 {
  font-size: 1rem;
  color: #333;
  margin: 0 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0e6d6;
  position: relative;
  font-weight: 600;
}

.sidebar-section h3::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 40px;
  height: 2px;
  background: #d4af37;
}

.hot-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.hot-list li {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  color: #666;
  font-size: 0.9rem;
  transition: all 0.3s;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.hot-list li:before {
  content: '•';
  color: #d4af37;
  margin-right: 8px;
  font-size: 1.2rem;
}

.hot-list li:last-child {
  border-bottom: none;
}

.hot-list li:hover {
  color: #d4af37;
  padding-left: 8px;
}

.hot-views {
  font-size: 0.75rem;
  color: #999;
  margin-left: 6px;
}

.empty-tip {
  color: #999;
  font-size: 0.85rem;
  cursor: default;
}

.empty-tip:hover {
  color: #999;
  padding-left: 0;
}

.community-stats {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.community-stats .stat-item {
  text-align: center;
  flex: 1;
  min-width: 80px;
  padding: 12px 8px;
  background: #f8f5f0;
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.community-stats .stat-number {
  display: block;
  font-size: 1.1rem;
  font-weight: bold;
  color: #d4af37;
  margin-bottom: 4px;
}

.community-stats .stat-label {
  font-size: 0.8rem;
  color: #666;
}

.quick-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-nav-btn {
  width: 100%;
  justify-content: flex-start;
  padding: 10px 15px;
  border-radius: 8px;
}

.quick-nav-btn:first-child {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
}

/* 内容区域 */
.content-area {
  flex: 1;
  min-width: 0;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(212, 175, 55, 0.1);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

/* 页脚 */
.footer {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 250, 240, 0.95) 100%);
  border-top: 1px solid rgba(212, 175, 55, 0.2);
  padding: 40px 0 20px;
  margin-top: 40px;
  backdrop-filter: blur(10px);
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
}

.footer-section h4 {
  color: #333;
  font-size: 1.1rem;
  margin: 0 0 15px 0;
  font-weight: 600;
  position: relative;
  padding-bottom: 8px;
}

.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background: #d4af37;
}

.footer-section p {
  color: #666;
  font-size: 0.9rem;
  margin: 8px 0;
  line-height: 1.5;
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-section p .el-icon {
  color: #d4af37;
}

.social-links {
  display: flex;
  gap: 15px;
  margin: 15px 0;
}

.social-links .el-icon {
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px;
  border-radius: 50%;
  background: #f5f5f5;
}

.social-links .el-icon:hover {
  color: #d4af37;
  background: rgba(212, 175, 55, 0.1);
  transform: translateY(-3px);
}

.social-tips {
  color: #999;
  font-size: 0.85rem;
  font-style: italic;
}

.friend-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.friend-link {
  color: #666;
  font-size: 0.9rem;
  text-decoration: none;
  transition: all 0.3s;
  padding: 5px 0;
}

.friend-link:hover {
  color: #d4af37;
  padding-left: 5px;
}

.footer-bottom {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
}

.footer-bottom p {
  color: #999;
  font-size: 0.85rem;
  margin: 5px 0;
}

.footer-copyright {
  font-size: 0.8rem;
  color: #ccc;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .navbar {
    padding: 0 20px;
  }

  .main-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    flex: none;
    position: static;
  }

  .content-area {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .navbar {
    flex-wrap: wrap;
    height: auto;
    padding: 15px;
  }

  .navbar-left,
  .navbar-center,
  .navbar-right {
    flex: 1 0 100%;
    justify-content: center;
    margin-bottom: 10px;
  }

  .nav-menu-list {
    height: auto;
  }

  .nav-menu-list :deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
  }

  .footer-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .footer-section h4::after {
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>