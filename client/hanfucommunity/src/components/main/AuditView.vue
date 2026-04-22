<template>
  <div class="audit-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>内容审核</h2>
      <p class="subtitle">审核用户发布的作品和活动</p>
    </div>

    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" class="audit-tabs">
      <!-- 待审核作品 -->
      <el-tab-pane label="待审核作品" name="work">
        <div class="tab-content">
          <!-- 筛选栏 -->
          <div class="filter-bar">
            <el-select v-model="workFilter" placeholder="筛选状态" style="width: 150px">
              <el-option label="全部" value="all" />
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </div>

          <!-- 作品列表 -->
          <div v-if="workList.length > 0" class="work-list">
            <div v-for="work in filteredWorkList" :key="work.id" class="work-card">
              <div class="card-header">
                <div class="work-type">
                  <el-tag :type="getWorkTypeTag(work.type)" size="small">
                    {{ getWorkTypeName(work.type) }}
                  </el-tag>
                  <el-tag v-if="work.auditStatus === 0" type="warning" size="small">待审核</el-tag>
                  <el-tag v-else-if="work.auditStatus === 1" type="success" size="small">已通过</el-tag>
                  <el-tag v-else-if="work.auditStatus === 2" type="danger" size="small">已拒绝</el-tag>
                </div>
                <span class="create-time">{{ formatDate(work.createTime) }}</span>
              </div>
              <h3 class="work-title">{{ work.title }}</h3>
              <p class="work-desc">{{ work.description }}</p>
              <div class="work-cover" v-if="work.coverImage">
                <img :src="work.coverImage" alt="作品封面" />
              </div>
              <div class="card-footer">
                <div class="author-info">
                  <span>发布者：{{ work.userName || '未知' }}</span>
                </div>
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="showWorkDetail(work)">
                    查看详情
                  </el-button>
                  <el-button v-if="work.auditStatus !== 1" type="success" size="small" @click="handleAudit(work.id, true)">
                    通过
                  </el-button>
                  <el-button v-if="work.auditStatus !== 2" type="danger" size="small" @click="showRejectDialog(work.id, 'work')">
                    拒绝
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待审核作品" />

          <!-- 分页 -->
          <div class="pagination" v-if="workList.length > 0">
            <el-pagination
              v-model:current-page="workPage"
              :page-size="workPageSize"
              :total="workTotal"
              layout="prev, pager, next"
              @current-change="loadWorkList"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 待审核活动 -->
      <el-tab-pane label="待审核活动" name="activity">
        <div class="tab-content">
          <!-- 筛选栏 -->
          <div class="filter-bar">
            <el-select v-model="activityFilter" placeholder="筛选状态" style="width: 150px">
              <el-option label="全部" value="all" />
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </div>

          <!-- 活动列表 -->
          <div v-if="activityList.length > 0" class="activity-list">
            <div v-for="activity in filteredActivityList" :key="activity.id" class="activity-card">
              <div class="card-header">
                <div class="activity-status">
                  <el-tag v-if="activity.auditStatus === 0" type="warning" size="small">待审核</el-tag>
                  <el-tag v-else-if="activity.auditStatus === 1" type="success" size="small">已通过</el-tag>
                  <el-tag v-else-if="activity.auditStatus === 2" type="danger" size="small">已拒绝</el-tag>
                </div>
                <span class="create-time">{{ formatDate(activity.createTime) }}</span>
              </div>
              <h3 class="activity-title">{{ activity.title }}</h3>
              <div class="activity-info">
                <p><el-icon><Location /></el-icon> {{ activity.location }}</p>
                <p><el-icon><Calendar /></el-icon> {{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}</p>
                <p><el-icon><User /></el-icon> 组织者ID: {{ activity.organizerId }}</p>
              </div>
              <p class="activity-desc">{{ activity.description }}</p>
              <div class="card-footer">
                <div class="action-buttons">
                  <el-button type="primary" size="small" @click="showActivityDetail(activity)">
                    查看详情
                  </el-button>
                  <el-button v-if="activity.auditStatus !== 1" type="success" size="small" @click="handleAudit(activity.id, true)">
                    通过
                  </el-button>
                  <el-button v-if="activity.auditStatus !== 2" type="danger" size="small" @click="showRejectDialog(activity.id, 'activity')">
                    拒绝
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待审核活动" />

          <!-- 分页 -->
          <div class="pagination" v-if="activityList.length > 0">
            <el-pagination
              v-model:current-page="activityPage"
              :page-size="activityPageSize"
              :total="activityTotal"
              layout="prev, pager, next"
              @current-change="loadActivityList"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 作品详情对话框 -->
    <el-dialog v-model="workDetailVisible" title="作品详情" width="700px" :lock-scroll="false">
      <div v-if="currentWork" class="detail-content">
        <div class="detail-header">
          <h3>{{ currentWork.title }}</h3>
          <div class="work-type">
            <el-tag :type="getWorkTypeTag(currentWork.type)" size="small">
              {{ getWorkTypeName(currentWork.type) }}
            </el-tag>
          </div>
        </div>
        <div class="detail-meta">
          <span>发布者：{{ currentWork.userName || '未知' }}</span>
          <span>浏览：{{ currentWork.views || 0 }}</span>
          <span>点赞：{{ currentWork.likes || 0 }}</span>
          <span>评论：{{ currentWork.comments || 0 }}</span>
        </div>
        <div class="detail-images" v-if="currentWork.images && currentWork.images.length > 0">
          <h4>作品图片</h4>
          <div class="image-grid">
            <img v-for="(img, idx) in currentWork.images" :key="idx" :src="img" alt="作品图片" />
          </div>
        </div>
        <div class="detail-desc">
          <h4>描述</h4>
          <div class="work-content-html" v-html="currentWork.description || '无'"></div>
        </div>
        <div class="detail-cover" v-if="currentWork.coverImage">
          <h4>封面图</h4>
          <img :src="currentWork.coverImage" alt="封面" class="cover-img" />
        </div>
      </div>
      <template #footer>
        <template v-if="currentWork">
          <el-button v-if="currentWork.auditStatus !== 1" type="success" @click="handleAudit(currentWork.id, true)">通过</el-button>
          <el-button v-if="currentWork.auditStatus !== 2" type="danger" @click="showRejectDialog(currentWork.id, 'work')">拒绝</el-button>
        </template>
        <el-button @click="workDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 活动详情对话框 -->
    <el-dialog v-model="activityDetailVisible" title="活动详情" width="700px" :lock-scroll="false">
      <div v-if="currentActivity" class="detail-content">
        <div class="detail-header">
          <h3>{{ currentActivity.title }}</h3>
        </div>
        <div class="detail-meta">
          <span>组织者ID：{{ currentActivity.organizerId }}</span>
          <span>最大参与人数：{{ currentActivity.maxParticipants }}</span>
          <span>当前报名人数：{{ currentActivity.currentParticipants }}</span>
        </div>
        <div class="detail-info">
          <p><strong>活动地点：</strong>{{ currentActivity.location }}</p>
          <p><strong>报名时间：</strong>{{ formatDate(currentActivity.registrationStartTime) }} 至 {{ formatDate(currentActivity.registrationEndTime) }}</p>
          <p><strong>活动时间：</strong>{{ formatDate(currentActivity.startTime) }} 至 {{ formatDate(currentActivity.endTime) }}</p>
        </div>
        <div class="detail-desc">
          <h4>活动描述</h4>
          <div class="work-content-html" v-html="currentActivity.description || '无'"></div>
        </div>
        <div class="detail-cover" v-if="currentActivity.coverImage">
          <h4>封面图</h4>
          <img :src="currentActivity.coverImage" alt="封面" class="cover-img" />
        </div>
      </div>
      <template #footer>
        <template v-if="currentActivity">
          <el-button v-if="currentActivity.auditStatus !== 1" type="success" @click="handleAudit(currentActivity.id, true)">通过</el-button>
          <el-button v-if="currentActivity.auditStatus !== 2" type="danger" @click="showRejectDialog(currentActivity.id, 'activity')">拒绝</el-button>
        </template>
        <el-button @click="activityDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px" :lock-scroll="false">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因（可选）"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Location, User } from '@element-plus/icons-vue'
import { getPendingWorkAuditList, auditWork, getWorkDetail } from '@/api/modules/audit'
import { getPendingActivityAuditList, auditActivity, getActivityDetail } from '@/api/modules/audit'
import { getCurrentUserId } from '@/utils/permission'

// 标签页
const activeTab = ref('work')

// 作品相关
const workList = ref([])
const workPage = ref(1)
const workPageSize = ref(12)
const workTotal = ref(0)
const workFilter = ref('all')

// 活动相关
const activityList = ref([])
const activityPage = ref(1)
const activityPageSize = ref(9)
const activityTotal = ref(0)
const activityFilter = ref('all')

// 详情对话框
const workDetailVisible = ref(false)
const activityDetailVisible = ref(false)
const currentWork = ref(null)
const currentActivity = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const pendingRejectId = ref(null)
const pendingRejectType = ref('')

// 加载作品列表
const loadWorkList = async () => {
  try {
    const res = await getPendingWorkAuditList({
      page: workPage.value,
      size: workPageSize.value
    })
    if (res && res.code === 200) {
      workList.value = res.data.records || []
      workTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载作品列表失败:', error)
  }
}

// 加载活动列表
const loadActivityList = async () => {
  try {
    const res = await getPendingActivityAuditList({
      page: activityPage.value,
      size: activityPageSize.value
    })
    if (res && res.code === 200) {
      activityList.value = res.data.records || []
      activityTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载活动列表失败:', error)
  }
}

// 筛选后的作品列表
const filteredWorkList = computed(() => {
  if (workFilter.value === 'all') return workList.value
  return workList.value.filter(work => {
    if (workFilter.value === 'pending') return work.auditStatus === 0
    if (workFilter.value === 'approved') return work.auditStatus === 1
    if (workFilter.value === 'rejected') return work.auditStatus === 2
    return true
  })
})

// 筛选后的活动列表
const filteredActivityList = computed(() => {
  if (activityFilter.value === 'all') return activityList.value
  return activityList.value.filter(activity => {
    if (activityFilter.value === 'pending') return activity.auditStatus === 0
    if (activityFilter.value === 'approved') return activity.auditStatus === 1
    if (activityFilter.value === 'rejected') return activity.auditStatus === 2
    return true
  })
})

// 查看作品详情（silent=true不计入浏览量）
const showWorkDetail = async (work) => {
  try {
    const res = await getWorkDetail(work.id, true)
    if (res && res.code === 200) {
      currentWork.value = res.data
      workDetailVisible.value = true
    }
  } catch (error) {
    console.error('获取作品详情失败:', error)
  }
}

// 查看活动详情（silent=true不计入浏览量）
const showActivityDetail = async (activity) => {
  try {
    const res = await getActivityDetail(activity.id, true)
    if (res && res.code === 200) {
      currentActivity.value = res.data
      activityDetailVisible.value = true
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
  }
}

// 执行审核
const handleAudit = async (id, approved) => {
  const action = approved ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(
      `确定要${action}这个${activeTab.value === 'work' ? '作品' : '活动'}吗？`,
      '审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: approved ? 'success' : 'warning' }
    )

    const userId = getCurrentUserId()
    if (activeTab.value === 'work') {
      const res = await auditWork(id, { approved, reason: '', auditorId: userId })
      if (res && res.code === 200) {
        ElMessage.success('审核成功')
        loadWorkList()
        workDetailVisible.value = false
      }
    } else {
      const res = await auditActivity(id, { approved, reason: '', auditorId: userId })
      if (res && res.code === 200) {
        ElMessage.success('审核成功')
        loadActivityList()
        activityDetailVisible.value = false
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败')
    }
  }
}

// 显示拒绝对话框
const showRejectDialog = (id, type) => {
  pendingRejectId.value = id
  pendingRejectType.value = type
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  try {
    const userId = getCurrentUserId()
    if (pendingRejectType.value === 'work') {
      const res = await auditWork(pendingRejectId.value, {
        approved: false,
        reason: rejectReason.value,
        auditorId: userId
      })
      if (res && res.code === 200) {
        ElMessage.success('已拒绝')
        loadWorkList()
        workDetailVisible.value = false
      }
    } else {
      const res = await auditActivity(pendingRejectId.value, {
        approved: false,
        reason: rejectReason.value,
        auditorId: userId
      })
      if (res && res.code === 200) {
        ElMessage.success('已拒绝')
        loadActivityList()
        activityDetailVisible.value = false
      }
    }
    rejectDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 获取作品类型名称
const getWorkTypeName = (type) => {
  const map = { photography: '摄影', design: '设计', dressing: '穿搭' }
  return map[type] || type
}

// 获取作品类型标签颜色
const getWorkTypeTag = (type) => {
  const map = { photography: '', design: 'success', dressing: 'warning' }
  return map[type] || 'info'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '无'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

// 初始化
onMounted(() => {
  loadWorkList()
  loadActivityList()
})
</script>

<style scoped>
.audit-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 5px 0;
  color: #333;
}

.page-header .subtitle {
  margin: 0;
  color: #999;
  font-size: 14px;
}

.audit-tabs {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
}

.tab-content {
  min-height: 400px;
}

.filter-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

/* 作品卡片 */
.work-list, .activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.work-card, .activity-card {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  transition: all 0.3s;
}

.work-card:hover, .activity-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.work-type, .activity-status {
  display: flex;
  gap: 8px;
}

.create-time {
  color: #999;
  font-size: 12px;
}

.work-title, .activity-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.work-desc, .activity-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-info {
  margin-bottom: 10px;
}

.activity-info p {
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  font-size: 14px;
}

.work-cover {
  margin-bottom: 10px;
}

.work-cover img {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.author-info {
  color: #999;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 详情对话框 */
.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.detail-header h3 {
  margin: 0;
}

.detail-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.detail-info {
  margin-bottom: 15px;
}

.detail-info p {
  margin: 8px 0;
  color: #666;
}

.detail-images h4, .detail-desc h4, .detail-cover h4 {
  margin: 15px 0 10px 0;
  color: #333;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.image-grid img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-img {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 4px;
}

.work-content-html {
  font-size: 14px;
  line-height: 1.8;
  color: #555;
}

.work-content-html :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 10px 0;
}
</style>
