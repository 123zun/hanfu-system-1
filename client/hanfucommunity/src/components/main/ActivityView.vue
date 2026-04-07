<template>
    <div class="activity-view">
        <!-- 板块标题 -->
        <div class="section-header">
            <h2 class="section-title">
                <el-icon><Calendar /></el-icon>
                活动中心
            </h2>
            <p class="section-subtitle">汇聚汉服活动，体验传统魅力</p>
        </div>

        <!-- 搜索和筛选 -->
        <div class="activity-toolbar">
            <el-input
                v-model="searchKeyword"
                placeholder="搜索活动名称..."
                clearable
                class="search-input"
                @keyup.enter="handleSearch"
            >
                <template #prefix>
                    <el-icon><Search /></el-icon>
                </template>
            </el-input>
            <el-select v-model="statusFilter" placeholder="报名状态" clearable @change="handleSearch">
                <el-option label="正在报名" value="registering" />
                <el-option label="报名已结束" value="ended" />
            </el-select>
            <el-select v-model="fullFilter" placeholder="名额状态" clearable @change="handleSearch">
                <el-option label="有名额" value="available" />
                <el-option label="已满" value="full" />
            </el-select>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button type="primary" @click="handleCreate">创建活动</el-button>
        </div>

        <!-- 活动列表 -->
        <div v-loading="loading" class="activity-grid">
            <div
                v-for="item in activityList"
                :key="item.id"
                class="activity-card"
                @click="handleDetail(item)"
            >
                <!-- 封面图 -->
                <div class="activity-card-image">
                    <img
                        :src="getImageUrl(item.coverImage)"
                        :alt="item.title"
                        @error="handleImageError"
                    />
                    <span class="status-badge" :class="getStatusClass(item)">
                        {{ getStatusText(item) }}
                    </span>
                </div>

                <!-- 活动信息 -->
                <div class="activity-card-content">
                    <h4 class="card-title" :title="item.title">{{ item.title }}</h4>
                    <p class="card-desc">{{ item.description || '暂无描述' }}</p>

                    <div class="card-meta">
                        <span class="meta-item">
                            <el-icon><Location /></el-icon>
                            {{ item.location || '地点待定' }}
                        </span>
                        <span class="meta-item" @click.stop="openParticipantDialog(item)">
                            <el-icon><User /></el-icon>
                            <a href="javascript:;" class="participant-link">
                                {{ item.currentParticipants || 0 }}/{{ item.maxParticipants }} 人
                            </a>
                        </span>
                    </div>

                    <div class="card-time-info">
                        <div class="time-item">
                            <span class="time-label">报名</span>
                            <span class="time-value">{{ formatDate(item.registrationStartTime) }} ~ {{ formatDate(item.registrationEndTime) }}</span>
                        </div>
                        <div class="time-item">
                            <span class="time-label">活动</span>
                            <span class="time-value">{{ formatDate(item.startTime) }} ~ {{ formatDate(item.endTime) }}</span>
                        </div>
                    </div>

                    <div class="card-actions" @click.stop>
                        <el-button type="info" link size="small" @click="handleDetail(item)">查看</el-button>
                        <el-button
                            :type="item.registered ? 'warning' : 'success'"
                            size="small"
                            :disabled="item.registered || item.currentParticipants >= item.maxParticipants || isSignupEnded(item)"
                            @click="handleRegister(item)"
                        >
                            {{ item.registered ? '已报名' : '报名' }}
                        </el-button>
                        <el-button type="primary" link size="small" @click="handleEdit(item)">修改</el-button>
                        <el-button type="danger" link size="small" @click="handleDelete(item)">删除</el-button>
                    </div>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!loading && activityList.length === 0" class="empty-state">
                <el-icon class="empty-icon"><Document /></el-icon>
                <p>暂无活动</p>
            </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="total"
                :page-sizes="[9, 18, 27, 36]"
                layout="total, sizes, prev, pager, next"
                background
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
            />
        </div>

        <!-- 创建/编辑/详情弹窗 -->
        <ActivityFormDialog
            v-model="showFormDialog"
            :activity="currentActivity"
            :mode="dialogMode"
            @success="loadActivityList"
        />

        <!-- 参与人员弹窗 -->
        <ParticipantDialog
            v-model="showParticipantDialog"
            :activity-id="currentActivityId"
            :activity-title="currentActivityTitle"
            @refresh="loadActivityList"
        />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, User, Document, Location, Calendar } from '@element-plus/icons-vue'
import { getActivityList, deleteActivity, registerActivity } from '@/api/modules/activity'
import ActivityFormDialog from './ActivityFormDialog.vue'
import ParticipantDialog from './ParticipantDialog.vue'

const userInfo = JSON.parse(localStorage.getItem('hanfu_user') || '{}')
const userId = userInfo.id

// 数据
const activityList = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(9)

// 搜索筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const fullFilter = ref('')

// 弹窗状态
const showFormDialog = ref(false)
const showParticipantDialog = ref(false)
const currentActivity = ref(null)
const dialogMode = ref('create')
const currentActivityId = ref(null)
const currentActivityTitle = ref('')

const getImageUrl = (path) => {
    if (!path) return 'http://localhost:8080/default/zixundefault.png'
    if (path.startsWith('http://') || path.startsWith('https://')) return path
    if (path.startsWith('/')) return `http://localhost:8080${path}`
    return `http://localhost:8080/${path}`
}

const handleImageError = (e) => {
    e.target.src = 'http://localhost:8080/default/zixundefault.png'
}

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const isSignupEnded = (item) => {
    if (!item.registrationEndTime) return false
    return new Date(item.registrationEndTime) < new Date()
}

const getStatusClass = (item) => {
    const today = new Date()
    const regStart = item.registrationStartTime ? new Date(item.registrationStartTime) : null
    const regEnd = item.registrationEndTime ? new Date(item.registrationEndTime) : null
    const actStart = item.startTime ? new Date(item.startTime) : null
    const actEnd = item.endTime ? new Date(item.endTime) : null

    if (actEnd && today > actEnd) return 'status-ended'
    if (actStart && today >= actStart) return 'status-ongoing'
    if (regStart && today < regStart) return 'status-upcoming'
    if (regEnd && today > regEnd) return 'status-ended'
    return 'status-registering'
}

const getStatusText = (item) => {
    const cls = getStatusClass(item)
    const map = {
        'status-registering': '报名中',
        'status-ended': '已结束',
        'status-ongoing': '进行中',
        'status-upcoming': '即将开始'
    }
    return map[cls] || '报名中'
}

const loadActivityList = async () => {
    loading.value = true
    try {
        const params = {
            page: currentPage.value,
            size: pageSize.value,
            keyword: searchKeyword.value || null,
            status: statusFilter.value || null,
            full: fullFilter.value || null,
            userId: userId || null
        }

        const res = await getActivityList(params)
        if (res.code === 200 || res.code === 0) {
            activityList.value = res.data?.records || []
            total.value = res.data?.total || 0
        } else {
            ElMessage.error(res.msg || '获取活动列表失败')
        }
    } catch (err) {
        ElMessage.error('获取活动列表失败')
    } finally {
        loading.value = false
    }
}

const handleSearch = () => {
    currentPage.value = 1
    loadActivityList()
}

const handleSizeChange = (val) => {
    pageSize.value = val
    currentPage.value = 1
    loadActivityList()
}

const handlePageChange = (val) => {
    currentPage.value = val
    loadActivityList()
}

const handleCreate = () => {
    currentActivity.value = null
    dialogMode.value = 'create'
    showFormDialog.value = true
}

const handleDetail = (item) => {
    currentActivity.value = { ...item }
    dialogMode.value = 'detail'
    showFormDialog.value = true
}

const handleEdit = (item) => {
    currentActivity.value = { ...item }
    dialogMode.value = 'edit'
    showFormDialog.value = true
}

const handleDelete = async (item) => {
    try {
        await ElMessageBox.confirm(
            `确定要删除活动「${item.title}」吗？`,
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        const res = await deleteActivity(item.id)
        if (res.code === 200 || res.code === 0) {
            ElMessage.success('删除成功')
            loadActivityList()
        } else {
            ElMessage.error(res.msg || '删除失败')
        }
    } catch {
        // 用户取消
    }
}

const handleRegister = async (item) => {
    if (!userId) {
        ElMessage.warning('请先登录')
        return
    }

    if (item.registered) {
        ElMessage.info('您已报名该活动')
        return
    }

    if (item.currentParticipants >= item.maxParticipants) {
        ElMessage.warning('报名名额已满')
        return
    }

    if (isSignupEnded(item)) {
        ElMessage.warning('报名已结束')
        return
    }

    try {
        await ElMessageBox.confirm(
            `确定要报名参加「${item.title}」吗？`,
            '报名确认',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
            }
        )

        const res = await registerActivity(item.id, userId)
        if (res.code === 200 || res.code === 0) {
            ElMessage.success('报名成功')
            loadActivityList()
        } else {
            ElMessage.error(res.msg || '报名失败')
        }
    } catch {
        // 用户取消
    }
}

const openParticipantDialog = (item) => {
    currentActivityId.value = item.id
    currentActivityTitle.value = item.title
    showParticipantDialog.value = true
}

onMounted(() => {
    loadActivityList()
})
</script>

<style scoped>
.activity-view {
    padding: 30px;
    background: linear-gradient(135deg, #f5f3ef 0%, #faf9f7 100%);
    min-height: calc(100vh - 120px);
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

.activity-toolbar {
    display: flex;
    gap: 15px;
    margin-bottom: 30px;
    align-items: center;
    padding: 20px;
    background: #f8f5f0;
    border-radius: 10px;
    border: 1px solid rgba(212, 175, 55, 0.2);
}

.activity-toolbar .search-input {
    width: 260px;
}

.activity-toolbar .el-select {
    width: 150px;
}

.activity-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 30px;
    min-height: 300px;
}

.activity-card {
    background: white;
    border-radius: 10px;
    overflow: hidden;
    border: 1px solid rgba(212, 175, 55, 0.1);
    cursor: pointer;
    display: flex;
    flex-direction: column;
    transition: all 0.3s;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.activity-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 20px rgba(212, 175, 55, 0.15);
    border-color: rgba(212, 175, 55, 0.3);
}

.activity-card-image {
    height: 180px;
    overflow: hidden;
    position: relative;
}

.activity-card-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
}

.activity-card:hover .activity-card-image img {
    transform: scale(1.05);
}

.status-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    color: #fff;
}

.status-registering {
    background: linear-gradient(135deg, #67c23a, #85ce61);
}

.status-ended {
    background: linear-gradient(135deg, #909399, #b1b3b8);
}

.status-ongoing {
    background: linear-gradient(135deg, #e6a23c, #f5c78a);
}

.status-upcoming {
    background: linear-gradient(135deg, #409eff, #79bbff);
}

.activity-card-content {
    padding: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.card-title {
    font-size: 1.1rem;
    color: #333;
    margin: 0 0 10px 0;
    line-height: 1.4;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.card-desc {
    color: #666;
    font-size: 0.9rem;
    line-height: 1.5;
    margin: 0 0 15px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 2.8em;
}

.card-meta {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 12px;
}

.card-meta .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 0.85rem;
    color: #666;
}

.participant-link {
    color: #d48c1e;
    text-decoration: none;
    font-weight: 500;
}

.participant-link:hover {
    color: #b0741a;
    text-decoration: underline;
}

.card-time-info {
    margin-bottom: 12px;
    padding: 10px;
    background: #f9f7f4;
    border-radius: 6px;
}

.time-item {
    display: flex;
    gap: 8px;
    font-size: 0.8rem;
    margin-bottom: 4px;
}

.time-item:last-child {
    margin-bottom: 0;
}

.time-label {
    color: #999;
    flex-shrink: 0;
    width: 28px;
}

.time-value {
    color: #666;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.card-actions {
    margin-top: auto;
    display: flex;
    gap: 12px;
    justify-content: flex-end;
}

.card-actions .el-button {
    font-size: 13px;
}

.empty-state {
    grid-column: 1 / -1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    color: #999;
    font-size: 1rem;
}

.empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.5;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 30px;
}

@media (max-width: 1200px) {
    .activity-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 1200px) {
    .activity-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 768px) {
    .activity-grid {
        grid-template-columns: 1fr;
    }

    .activity-toolbar {
        flex-wrap: wrap;
    }

    .activity-toolbar .search-input {
        width: 100%;
    }
}
</style>
