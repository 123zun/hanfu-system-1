<template>
    <el-dialog
        v-model="visible"
        :title="`参与人员 - ${activityTitle}`"
        width="800px"
        :close-on-click-modal="false"
        :lock-scroll="false"
    >
        <!-- 工具栏 -->
        <div class="toolbar">
            <el-input
                v-model="keyword"
                placeholder="搜索用户名"
                prefix-icon="Search"
                clearable
                style="width: 250px"
                @input="handleSearch"
            />
            <div class="toolbar-actions">
                <el-button type="primary" @click="handleAdd" :loading="loadingUsers">
                    新增
                </el-button>
                <el-button
                    type="danger"
                    :disabled="selectedRows.length === 0"
                    @click="handleRemove"
                >
                    删除 ({{ selectedRows.length }})
                </el-button>
            </div>
        </div>

        <!-- 参与人员表格 -->
        <el-table
            ref="tableRef"
            :data="pagedParticipants"
            height="350px"
            @selection-change="handleSelectionChange"
            style="width: 100%; margin-top: 12px;"
        >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="报名ID" width="100" />
            <el-table-column prop="userName" label="用户名" />
            <el-table-column prop="userAvatar" label="头像" width="100">
                <template #default="{ row }">
                    <el-avatar :size="32" :src="getImageUrl(row.userAvatar)">
                        {{ row.userName?.charAt(0) }}
                    </el-avatar>
                </template>
            </el-table-column>
            <el-table-column prop="signupTime" label="报名时间" width="180">
                <template #default="{ row }">
                    {{ formatDate(row.signupTime) }}
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-wrapper">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :total="filteredParticipants.length"
                :page-sizes="[5, 10, 20, 50]"
                layout="total, sizes, prev, pager, next"
                background
            />
        </div>

        <!-- 用户选择弹窗 -->
        <UserSelectDialog
            v-model="showUserSelect"
            :all-users="allUsers"
            :excluded-user-ids="excludedUserIds"
            @confirm="handleUserSelected"
        />
    </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getParticipants } from '@/api/modules/activity'
import { getActivityDetail } from '@/api/modules/activity'
import UserSelectDialog from './UserSelectDialog.vue'
import request from '@/api/request'

const props = defineProps({
    modelValue: Boolean,
    activityId: Number,
    activityTitle: String
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const activityId = computed(() => props.activityId)

const allParticipants = ref([])
const filteredParticipants = ref([])
const selectedRows = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const tableRef = ref(null)
const showUserSelect = ref(false)
const loadingUsers = ref(false)
const allUsers = ref([])
const excludedUserIds = computed(() => allParticipants.value.map(p => p.userId))

const pagedParticipants = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredParticipants.value.slice(start, end)
})

const loadParticipants = async () => {
    try {
        const res = await getParticipants(activityId.value)
      console.log("参与人员",res);
        if (res.code === 200 || res.code === 0) {
            allParticipants.value = res.data || []
            handleSearch()
        } else {
            ElMessage.error(res.msg || '获取参与人员失败')
        }
    } catch (err) {
        ElMessage.error('获取参与人员失败')
    }
}

const handleSearch = () => {
    if (!keyword.value) {
        filteredParticipants.value = [...allParticipants.value]
    } else {
        filteredParticipants.value = allParticipants.value.filter(p =>
            p.userName?.toLowerCase().includes(keyword.value.toLowerCase())
        )
    }
    currentPage.value = 1
}

const handleSelectionChange = (selection) => {
    selectedRows.value = selection
}

const handleAdd = async () => {
    loadingUsers.value = true
    try {
        // 获取所有可用用户（未删除的）
        const res = await request({
            url: '/activity/users',
            method: 'get'
        })
        if (res.code === 200 || res.code === 0) {
            allUsers.value = res.data.map(u => ({
                id: u.id,
                username: u.userName,
                avatar: u.userAvatar
            }))
            showUserSelect.value = true
        } else {
            ElMessage.error('获取用户列表失败')
        }
    } catch (err) {
        ElMessage.error('获取用户列表失败')
    } finally {
        loadingUsers.value = false
    }
}

const handleUserSelected = async (selectedUsersList) => {
    // 批量新增用户
    try {
        const userIds = selectedUsersList.map(u => u.id)
        const res = await request({
            url: `/activity/participants/add`,
            method: 'post',
            data: {
                activityId: activityId.value,
                userIds: userIds
            }
        })
        if (res.code === 200 || res.code === 0) {
            ElMessage.success('添加成功')
            loadParticipants()
            emit('refresh')
        } else {
            ElMessage.error(res.msg || '添加失败')
        }
    } catch (err) {
        ElMessage.error('添加失败')
    }
}


const handleRemove = async () => {
    if (selectedRows.value.length === 0) return

    try {
        await ElMessageBox.confirm(
            `确定要删除选中的 ${selectedRows.value.length} 名参与人员吗？`,
            '提示',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )

        const ids = selectedRows.value.map(r => r.id)
        const res = await request({
            url: '/activity/participants/remove',
            method: 'delete',
            data: {
                activityId: activityId.value,
                signupIds: ids
            }
        })
        if (res.code === 200 || res.code === 0) {
            ElMessage.success('删除成功')
            loadParticipants()
            emit('refresh')
        } else {
            ElMessage.error(res.msg || '删除失败')
        }
    } catch {
        // 用户取消
    }
}

const formatDate = (dateStr) => {
    if (!dateStr) return '-'
    const d = new Date(dateStr)
    return d.toLocaleString('zh-CN')
}

const getImageUrl = (path) => {
    if (!path) return ''
    if (path.startsWith('http://') || path.startsWith('https://')) return path
    if (path.startsWith('/')) return `http://localhost:8080${path}`
    return `http://localhost:8080/${path}`
}

watch(visible, (val) => {
    if (val) {
        loadParticipants()
        currentPage.value = 1
        keyword.value = ''
        selectedRows.value = []
    }
})
</script>

<style scoped>
.toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.toolbar-actions {
    display: flex;
    gap: 8px;
}

.pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
}
</style>
