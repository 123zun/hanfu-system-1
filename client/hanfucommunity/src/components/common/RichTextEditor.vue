<!-- src/components/common/RichTextEditor.vue -->
<template>
  <div class="rich-text-editor">
    <!-- 工具栏 -->
    <div class="editor-toolbar">
      <el-button-group class="toolbar-group">
        <el-button
            size="small"
            @click="execCommand('bold')"
            :type="isActive('bold') ? 'primary' : 'default'"
        >
          <strong>B</strong>
        </el-button>
        <el-button
            size="small"
            @click="execCommand('italic')"
            :type="isActive('italic') ? 'primary' : 'default'"
        >
          <em>I</em>
        </el-button>
        <el-button
            size="small"
            @click="execCommand('underline')"
            :type="isActive('underline') ? 'primary' : 'default'"
        >
          <u>U</u>
        </el-button>
      </el-button-group>

      <el-button-group class="toolbar-group">
        <el-button size="small" @click="execCommand('insertUnorderedList')">
          无序列表
        </el-button>
        <el-button size="small" @click="execCommand('insertOrderedList')">
          有序列表
        </el-button>
      </el-button-group>

      <el-button-group class="toolbar-group">
        <el-button size="small" @click="execCommand('justifyLeft')">
          左对齐
        </el-button>
        <el-button size="small" @click="execCommand('justifyCenter')">
          居中
        </el-button>
        <el-button size="small" @click="execCommand('justifyRight')">
          右对齐
        </el-button>
      </el-button-group>

      <el-button-group class="toolbar-group">
        <el-button size="small" @click="execCommand('formatBlock', '<h2>')">
          H2
        </el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<h3>')">
          H3
        </el-button>
        <el-button size="small" @click="execCommand('formatBlock', '<p>')">
          正文
        </el-button>
      </el-button-group>

      <el-button
          type="primary"
          size="small"
          icon="Picture"
          @click="triggerImageUpload"
      >
        插入图片
      </el-button>

      <input
          ref="imageInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleImageUpload"
          multiple
      />
    </div>

    <!-- 编辑区域 -->
    <div
        ref="editorRef"
        class="editor-content"
        contenteditable="true"
        @input="handleInput"
        @paste="handlePaste"
    ></div>

    <!-- 字数统计 -->
    <div class="editor-footer">
      <span class="char-count">字数：{{ charCount }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadContentImage } from '@/api/modules/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const imageInput = ref(null)
const charCount = ref(0)

// 执行命令
const execCommand = (command, value = null) => {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
  handleInput()
}

// 检查当前格式是否激活
const isActive = (command) => {
  return document.queryCommandState(command)
}

// 处理输入
const handleInput = () => {
  if (editorRef.value) {
    const html = editorRef.value.innerHTML
    // 提取纯文本统计字数
    const text = editorRef.value.innerText || ''
    charCount.value = text.length
    emit('update:modelValue', html)
  }
}

// 处理粘贴（过滤样式）
const handlePaste = (e) => {
  e.preventDefault()
  const text = e.clipboardData.getData('text/plain')
  document.execCommand('insertText', false, text)
}

// 触发图片上传
const triggerImageUpload = () => {
  imageInput.value?.click()
}

// 处理图片上传
const handleImageUpload = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) return

  for (const file of files) {
    // 验证文件
    if (!file.type.startsWith('image/')) {
      ElMessage.warning('请选择图片文件')
      continue
    }

    if (file.size > 5 * 1024 * 1024) {
      ElMessage.warning('图片大小不能超过5MB')
      continue
    }

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await uploadContentImage(formData)

      if (response && response.code === 200) {
        const imageUrl = response.data?.url || response.data?.imageUrl

        if (imageUrl) {
          // 在光标位置插入图片
          const imgHtml = `<img src="${imageUrl}" alt="图片" style="max-width: 100%; margin: 10px 0; border-radius: 8px;" />`
          document.execCommand('insertHTML', false, imgHtml)
          handleInput()
          ElMessage.success('图片上传成功')
        } else {
          ElMessage.error('图片上传失败：未返回URL')
        }
      } else {
        ElMessage.error(response?.message || '图片上传失败')
      }
    } catch (error) {
      loading.close()
      console.error('图片上传失败:', error)
      ElMessage.error('图片上传失败，请稍后重试')
    }
  }

  // 重置input
  event.target.value = ''
}

// 设置编辑器内容
const setContent = (html) => {
  if (editorRef.value && html !== undefined) {
    editorRef.value.innerHTML = html || ''
    handleInput()
  }
}

// 插入图片（外部调用）
const insertImage = (imageUrl) => {
  const imgHtml = `<img src="${imageUrl}" alt="图片" style="max-width: 100%; margin: 10px 0; border-radius: 8px;" />`
  document.execCommand('insertHTML', false, imgHtml)
  handleInput()
}

// 监听外部值变化
watch(() => props.modelValue, (newVal) => {
  if (editorRef.value && newVal !== editorRef.value.innerHTML) {
    editorRef.value.innerHTML = newVal || ''
    handleInput()
  }
})

// 初始化
onMounted(() => {
  if (editorRef.value) {
    editorRef.value.innerHTML = props.modelValue || ''
    editorRef.value.focus()

    // 设置placeholder
    editorRef.value.setAttribute('placeholder', props.placeholder)
    handleInput()
  }
})

// 暴露方法给父组件
defineExpose({
  insertImage,
  setContent
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.toolbar-group {
  margin-right: 5px;
}

.editor-content {
  min-height: 400px;
  padding: 15px;
  outline: none;
  line-height: 1.6;
  font-size: 14px;
  overflow-y: auto;
}

.editor-content:empty:before {
  content: attr(placeholder);
  color: #c0c4cc;
}

.editor-content img {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
  border-radius: 8px;
  cursor: pointer;
}

.editor-content img:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.editor-content h2 {
  font-size: 1.5rem;
  margin: 1em 0 0.5em;
}

.editor-content h3 {
  font-size: 1.2rem;
  margin: 1em 0 0.5em;
}

.editor-content ul,
.editor-content ol {
  margin: 0.5em 0 0.5em 1.5em;
}

.editor-footer {
  padding: 8px 12px;
  background: #f5f7fa;
  border-top: 1px solid #dcdfe6;
  text-align: right;
}

.char-count {
  font-size: 12px;
  color: #909399;
}
</style>