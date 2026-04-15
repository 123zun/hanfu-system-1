<template>
  <div class="users-view">
    <!-- 板块标题 -->
    <div class="section-header">
      <h2 class="section-title">
        <el-icon><User /></el-icon>
        用户管理
      </h2>
      <p class="section-subtitle">管理系统用户，查看用户信息，添加、编辑或删除用户账号</p>
    </div>

    <!-- 搜索和筛选 -->
    <div class="users-toolbar">
      <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名或邮箱..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="genderFilter" placeholder="性别筛选" clearable class="filter-select" @change="handleFilterChange">
        <el-option label="男" value="male" />
        <el-option label="女" value="female" />
      </el-select>

      <el-button type="primary" icon="Refresh" @click="refreshUsers">刷新</el-button>
      <el-button type="success" icon="Plus" @click="handleAdd">
        添加用户
      </el-button>
    </div>

    <!-- 用户统计 -->
    <div class="users-stats">
      <div class="stat-card">
        <el-icon><User /></el-icon>
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">全部用户</span>
      </div>
      <div class="stat-card">
        <el-icon><Male /></el-icon>
        <span class="stat-num">{{ maleCount }}</span>
        <span class="stat-label">男性用户</span>
      </div>
      <div class="stat-card">
        <el-icon><Female /></el-icon>
        <span class="stat-num">{{ femaleCount }}</span>
        <span class="stat-label">女性用户</span>
      </div>
    </div>

    <!-- 用户列表（Element Plus Table） -->
    <div class="users-table-wrapper">
      <el-table
          :data="userList"
          v-loading="loading"
          stripe
          class="users-table"
          header-cell-class-name="table-header"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />

        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-avatar :src="row.avatar" :size="40" class="user-avatar">
                {{ row.username?.charAt(0) }}
              </el-avatar>
              <div class="user-info-text">
                <div class="username">{{ row.username }}</div>
                <div class="user-id">ID: {{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />

        <el-table-column prop="phone" label="手机号" width="130" show-overflow-tooltip />

        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 'male'" type="primary" size="small">男</el-tag>
            <el-tag v-else-if="row.gender === 'female'" type="danger" size="small">女</el-tag>
            <el-tag v-else type="info" size="small">未知</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="region" label="地区" width="120" show-overflow-tooltip />

        <el-table-column prop="createTime" label="注册时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" icon="Delete" @click="handleDelete(row.id, row.username)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
        v-model="showEditDialog"
        :title="isEditMode ? '编辑用户' : '添加用户'"
        width="600px"
        :close-on-click-modal="false"
        :lock-scroll="false"
    >
      <el-form :model="editForm" label-width="80px" :rules="editRules" ref="editFormRef">
        <el-form-item label="用户名" prop="username" v-if="!isEditMode">
          <el-input v-model="editForm.username" placeholder="请输入用户名（2-20字符）" />
        </el-form-item>

        <el-form-item label="密码" :prop="isEditMode ? '' : 'password'">
          <el-input
              v-model="editForm.password"
              type="password"
              :placeholder="isEditMode ? '留空则不修改密码' : '请输入密码（至少6位）'"
              show-password
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="male" />
            <el-option label="女" value="female" />
          </el-select>
        </el-form-item>

        <el-form-item label="地区">
          <el-input v-model="editForm.region" placeholder="请输入地区" />
        </el-form-item>

        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="请输入个人简介" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saveLoading">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Search,
  Refresh,
  Plus,
  Edit,
  Delete,
  Male,
  Female
} from '@element-plus/icons-vue'
import {
  getUserList,
  deleteUser,
  addUser,
  updateUser
} from '@/api/modules/user'

// 搜索和筛选
const searchKeyword = ref('')
const genderFilter = ref('')

// 列表数据
const userList = reactive([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const maleCount = ref(0)
const femaleCount = ref(0)

// 对话框
const showEditDialog = ref(false)
const showAddDialog = ref(false)
const isEditMode = ref(false)
const saveLoading = ref(false)
const editFormRef = ref(null)

const editForm = reactive({
  id: null,
  username: '',
  password: '',
  email: '',
  phone: '',
  gender: 'male',
  region: '',
  bio: ''
})

const editRules = computed(() => ({
  username: isEditMode.value ? [] : [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' }
  ],
  password: isEditMode.value ? [] : [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}))

// 加载用户列表
const loadUsers = async () => {
  try {
    loading.value = true

    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }

    if (searchKeyword.value) {
      params.username = searchKeyword.value
    }
    if (genderFilter.value) {
      params.gender = genderFilter.value
    }

    const response = await getUserList(params)

    if (response && response.code === 200) {
      const data = response.data
      if (data && (data.list || data.records)) {
        const records = data.records || data.list
        userList.length = 0
        records.forEach(item => {
          userList.push(item)
        })
        total.value = data.total || 0

        // 统计男女用户（当前页）
        maleCount.value = records.filter(u => u.gender === 'male').length
        femaleCount.value = records.filter(u => u.gender === 'female').length
      }
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

// 刷新
const refreshUsers = () => {
  currentPage.value = 1
  loadUsers()
  ElMessage.success('已刷新')
}

// 筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  loadUsers()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  loadUsers()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadUsers()
}

// 打开添加对话框
const handleAdd = () => {
  isEditMode.value = false
  resetEditForm()
  showEditDialog.value = true
  nextTick(() => {
    editFormRef.value?.clearValidate()
  })
}

// 打开编辑对话框
const handleEdit = (row) => {
  isEditMode.value = true
  editForm.id = row.id
  editForm.username = row.username || ''
  editForm.password = ''
  editForm.email = row.email || ''
  editForm.phone = row.phone || ''
  editForm.gender = row.gender || 'male'
  editForm.region = row.region || ''
  editForm.bio = row.bio || ''
  showEditDialog.value = true
}

// 保存（添加或更新）
const handleSave = async () => {
  try {
    await editFormRef.value.validate()
  } catch (e) {
    return
  }

  try {
    saveLoading.value = true

    if (isEditMode.value) {
      // 编辑模式，不传空密码
      const updateData = {
        id: editForm.id,
        email: editForm.email,
        phone: editForm.phone,
        gender: editForm.gender,
        region: editForm.region,
        bio: editForm.bio
      }
      if (editForm.password) {
        updateData.password = editForm.password
      }

      const response = await updateUser(updateData)
      if (response && response.code === 200) {
        ElMessage.success('更新成功')
        showEditDialog.value = false
        loadUsers()
      } else {
        ElMessage.error(response?.message || '更新失败')
      }
    } else {
      // 添加模式
      const response = await addUser({
        username: editForm.username,
        password: editForm.password,
        email: editForm.email,
        phone: editForm.phone,
        gender: editForm.gender,
        region: editForm.region,
        bio: editForm.bio,
        avatar: 'http://localhost:8080/uploads/avatars/default.jpg'
      })
      if (response && response.code === 200) {
        ElMessage.success('添加成功')
        showEditDialog.value = false
        loadUsers()
      } else {
        ElMessage.error(response?.message || '添加失败')
      }
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    saveLoading.value = false
  }
}

// 删除
const handleDelete = async (id, username) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除用户"${username}"吗？此操作不可恢复！`,
        '删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const response = await deleteUser(id)

    if (response && response.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    } else {
      ElMessage.error(response?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置表单
const resetEditForm = () => {
  editForm.id = null
  editForm.username = ''
  editForm.password = ''
  editForm.email = ''
  editForm.phone = ''
  editForm.gender = 'male'
  editForm.region = ''
  editForm.bio = ''
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
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

// 页面加载
onMounted(() => {
  loadUsers()
})

// 暴露方法给外部使用
defineExpose({
  handleAdd
})
</script>

<style scoped>
.users-view {
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
.users-toolbar {
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

.filter-select {
  width: 130px;
}

/* 统计卡片 */
.users-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 150px;
  background: #f8f5f0;
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 10px;
  padding: 20px;
  text-align: center;
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

/* 表格 */
.users-table-wrapper {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid rgba(212, 175, 55, 0.15);
  margin-bottom: 20px;
}

.users-table {
  width: 100%;
}

.users-table :deep(.table-header) {
  background: #f8f5f0 !important;
  color: #333;
  font-weight: 600;
}

.users-table :deep(.el-table__row):hover > td {
  background: rgba(212, 175, 55, 0.05) !important;
}

/* 用户信息单元格 */
.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  background: linear-gradient(135deg, #d4af37, #8b4513);
  color: white;
  font-weight: bold;
  flex-shrink: 0;
}

.user-info-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-weight: 600;
  color: #333;
}

.user-id {
  font-size: 0.8rem;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .users-view {
    padding: 20px;
  }

  .users-toolbar {
    flex-direction: column;
  }

  .search-input,
  .filter-select {
    width: 100%;
    max-width: none;
  }

  .users-stats {
    flex-direction: column;
  }
}
</style>
