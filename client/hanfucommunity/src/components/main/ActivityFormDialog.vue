<template>
    <el-dialog
        v-model="visible"
        :title="dialogTitle"
        width="600px"
        :close-on-click-modal="false"
        :lock-scroll="false"
        @close="handleClose"
    >
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
            <el-form-item label="活动名称" prop="title">
                <el-input v-model="form.title" placeholder="请输入活动名称" :disabled="isDetail" />
            </el-form-item>

            <el-form-item label="活动描述" prop="description">
                <el-input
                    v-model="form.description"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入活动描述"
                    :disabled="isDetail"
                />
            </el-form-item>

            <el-form-item label="活动地点" prop="location">
                <el-input v-model="form.location" placeholder="请输入活动地点" :disabled="isDetail" />
            </el-form-item>

            <el-form-item label="封面图片">
                <div class="cover-upload">
                    <div v-if="!form.coverImage" class="upload-area" @click="!isDetail && triggerUpload()">
                        <el-icon class="upload-icon"><Plus /></el-icon>
                        <p>上传封面图</p>
                        <p class="upload-tips">建议尺寸 800x400</p>
                    </div>
                    <div v-else class="cover-preview">
                        <img :src="getImageUrl(form.coverImage)" class="cover-image" />
                        <div class="cover-actions" v-if="!isDetail">
                            <el-button type="text" @click="triggerUpload()">更换</el-button>
                            <el-button type="text" @click="removeCover()">删除</el-button>
                        </div>
                    </div>
                    <input
                        ref="fileInputRef"
                        type="file"
                        accept="image/*"
                        style="display: none"
                        @change="handleFileChange"
                    />
                </div>
            </el-form-item>

            <el-form-item label="最大参与人数" prop="maxParticipants">
                <el-input-number
                    v-model="form.maxParticipants"
                    :min="1"
                    :max="9999"
                    :disabled="isDetail"
                />
            </el-form-item>

            <el-form-item label="报名开始时间" prop="registrationStartTime">
                <el-date-picker
                    v-model="form.registrationStartTime"
                    type="date"
                    placeholder="选择报名开始时间"
                    :disabled="isDetail"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                />
            </el-form-item>

            <el-form-item label="报名结束时间" prop="registrationEndTime">
                <el-date-picker
                    v-model="form.registrationEndTime"
                    type="date"
                    placeholder="选择报名结束时间"
                    :disabled="isDetail"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                />
            </el-form-item>

            <el-form-item label="活动开始时间" prop="startTime">
                <el-date-picker
                    v-model="form.startTime"
                    type="date"
                    placeholder="选择活动开始时间"
                    :disabled="isDetail"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                />
            </el-form-item>

            <el-form-item label="活动结束时间" prop="endTime">
                <el-date-picker
                    v-model="form.endTime"
                    type="date"
                    placeholder="选择活动结束时间"
                    :disabled="isDetail"
                    style="width: 100%"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                />
            </el-form-item>
        </el-form>

        <template #footer v-if="!isDetail">
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </template>
    </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const userInfo = JSON.parse(localStorage.getItem('hanfu_user') || '{}')
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { uploadCover, createActivity, updateActivity } from '@/api/modules/activity'

const props = defineProps({
    modelValue: Boolean,
    activity: Object,  // 编辑/详情时传入的活动数据
    mode: {
        type: String,
        default: 'create'  // create | edit | detail
    }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
})

const dialogTitle = computed(() => {
    return props.mode === 'create' ? '创建活动' : props.mode === 'edit' ? '编辑活动' : '活动详情'
})

const isDetail = computed(() => props.mode === 'detail')

const formRef = ref(null)
const submitting = ref(false)
const fileInputRef = ref(null)

const form = ref({
    title: '',
    description: '',
    location: '',
    coverImage: '',
    maxParticipants: 50,
    registrationStartTime: null,
    registrationEndTime: null,
    startTime: null,
    endTime: null
})

const rules = {
    title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
    registrationStartTime: [{ required: true, message: '请选择报名开始时间', trigger: 'change' }],
    registrationEndTime: [{ required: true, message: '请选择报名结束时间', trigger: 'change' }],
    startTime: [{ required: true, message: '请选择活动开始时间', trigger: 'change' }],
    endTime: [{ required: true, message: '请选择活动结束时间', trigger: 'change' }]
}

// 监听 activity 变化，回填表单
watch(() => props.activity, (val) => {
    if (val) {
        form.value = {
            id: val.id,
            title: val.title || '',
            description: val.description || '',
            location: val.location || '',
            coverImage: val.coverImage || '',
            maxParticipants: val.maxParticipants || 50,
            registrationStartTime: val.registrationStartTime ? String(val.registrationStartTime).split('T')[0] : null,
            registrationEndTime: val.registrationEndTime ? String(val.registrationEndTime).split('T')[0] : null,
            startTime: val.startTime ? String(val.startTime).split('T')[0] : null,
            endTime: val.endTime ? String(val.endTime).split('T')[0] : null
        }
    }
}, { immediate: true })

const triggerUpload = () => {
    fileInputRef.value?.click()
}

const handleFileChange = async (e) => {
    const file = e.target.files[0]
    if (!file) return

    try {
        const res = await uploadCover(file)
        if (res.code === 200 || res.code === 0) {
            form.value.coverImage = res.data?.url || res.data
            ElMessage.success('上传成功')
        } else {
            ElMessage.error(res.msg || '上传失败')
        }
    } catch (err) {
        ElMessage.error('上传失败')
    }

    e.target.value = ''
}

const removeCover = () => {
    form.value.coverImage = ''
}

const getImageUrl = (path) => {
    if (!path) return ''
    if (path.startsWith('http://') || path.startsWith('https://')) return path
    if (path.startsWith('/')) return `http://localhost:8080${path}`
    return `http://localhost:8080/${path}`
}

const handleSubmit = async () => {
    try {
        await formRef.value.validate()
    } catch {
        return
    }

    submitting.value = true
    try {
        const data = {
            ...form.value,
            organizerId: userInfo.id
        }

        const res = props.mode === 'create'
            ? await createActivity(data)
            : await updateActivity(data)

        if (res.code === 200 || res.code === 0) {
            ElMessage.success(props.mode === 'create' ? '创建成功' : '更新成功')
            emit('success')
            handleClose()
        } else {
            ElMessage.error(res.msg || '操作失败')
        }
    } catch (err) {
        ElMessage.error('操作失败')
    } finally {
        submitting.value = false
    }
}

const handleClose = () => {
    formRef.value?.resetFields()
    form.value = {
        title: '',
        description: '',
        location: '',
        coverImage: '',
        maxParticipants: 50,
        registrationStartTime: null,
        registrationEndTime: null,
        startTime: null,
        endTime: null
    }
    visible.value = false
}
</script>

<style scoped>
.cover-upload {
    width: 100%;
}

.upload-area {
    border: 1px dashed #d9d9d9;
    border-radius: 8px;
    padding: 30px;
    text-align: center;
    cursor: pointer;
    transition: border-color 0.3s;
    width: 100%;
}

.upload-area:hover {
    border-color: #9254de;
}

.upload-icon {
    font-size: 28px;
    color: #8c8c8c;
    margin-bottom: 8px;
}

.upload-area p {
    margin: 0;
    color: #8c8c8c;
    font-size: 14px;
}

.upload-tips {
    font-size: 12px !important;
    margin-top: 4px !important;
}

.cover-preview {
    position: relative;
    width: 200px;
}

.cover-image {
    width: 100%;
    height: 120px;
    object-fit: cover;
    border-radius: 8px;
}

.cover-actions {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 8px;
}
</style>
