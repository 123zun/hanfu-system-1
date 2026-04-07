<template>
    <el-dialog
        v-model="visible"
        title="选择用户"
        width="600px"
        :close-on-click-modal="false"
        :lock-scroll="false"
    >
        <div class="search-bar">
            <el-input
                v-model="keyword"
                placeholder="搜索用户名"
                prefix-icon="Search"
                clearable
                style="width: 300px"
                @input="handleSearch"
            />
            <span class="search-tip">双击行或点击选择</span>
        </div>

        <el-table
            ref="tableRef"
            :data="filteredUsers"
            height="350px"
            @row-dblclick="handleSelect"
            @select="handleRowSelect"
            @select-all="handleSelectAll"
            highlight-current-row
            style="width: 100%; margin-top: 12px;"
        >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" />
            <el-table-column prop="avatar" label="头像" width="100">
                <template #default="{ row }">
                    <el-avatar :size="32" :src="getImageUrl(row.avatar)">
                        {{ row.username?.charAt(0) }}
                    </el-avatar>
                </template>
            </el-table-column>
        </el-table>

        <template #footer>
            <el-button @click="visible = false">取消</el-button>
            <el-button type="primary" @click="handleConfirm" :disabled="selectedUsers.length === 0">
                确定选择 ({{ selectedUsers.length }})
            </el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
    modelValue: Boolean,
    allUsers: {
        type: Array,
        default: () => []
    },
    excludedUserIds: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const keyword = ref('')
const selectedUsers = ref([])
const tableRef = ref(null)

const filteredUsers = computed(() => {
    if (!keyword.value) {
        return props.allUsers.filter(u => !props.excludedUserIds.includes(u.id))
    }
    return props.allUsers
        .filter(u => !props.excludedUserIds.includes(u.id))
        .filter(u => u.username?.toLowerCase().includes(keyword.value.toLowerCase()))
})

const handleSearch = () => {
    tableRef.value?.clearSelection()
    selectedUsers.value = []
}

const handleRowSelect = (selection, row) => {
    selectedUsers.value = selection.map(s => s.id)
}

const handleSelectAll = (selection) => {
    selectedUsers.value = selection.map(s => s.id)
}

const handleSelect = (row) => {
    const idx = selectedUsers.value.indexOf(row.id)
    if (idx > -1) {
        tableRef.value?.toggleRowSelection(row, false)
        selectedUsers.value.splice(idx, 1)
    } else {
        tableRef.value?.toggleRowSelection(row, true)
        selectedUsers.value.push(row.id)
    }
}

const handleConfirm = () => {
    const selected = props.allUsers.filter(u => selectedUsers.value.includes(u.id))
    emit('confirm', selected)
    visible.value = false
    selectedUsers.value = []
    keyword.value = ''
}

const getImageUrl = (path) => {
    if (!path) return ''
    if (path.startsWith('http://') || path.startsWith('https://')) return path
    if (path.startsWith('/')) return `http://localhost:8080${path}`
    return `http://localhost:8080/${path}`
}

watch(visible, (val) => {
    if (!val) {
        selectedUsers.value = []
        keyword.value = ''
        tableRef.value?.clearSelection()
    }
})
</script>

<style scoped>
.search-bar {
    display: flex;
    align-items: center;
    gap: 16px;
}

.search-tip {
    color: #8c8c8c;
    font-size: 12px;
}
</style>
