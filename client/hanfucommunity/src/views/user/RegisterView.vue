<template>
  <div class="register-container">
    <div class="register-wrapper">
      <!-- 顶部导航按钮 -->
      <div class="top-buttons">
        <router-link to="/" class="nav-btn back-home">
          <span>返回首页</span>
        </router-link>
        <router-link to="/login" class="nav-btn back-login">
          <span>返回登录</span>
        </router-link>
      </div>

      <!-- 注册卡片 -->
      <div class="register-card">
        <!-- 头部 -->
        <div class="register-header">
          <h1 class="logo">汉韵华章</h1>
          <p class="subtitle">加入汉服文化社区</p>
          <p class="welcome">创建新账号，开启汉服之旅</p>
        </div>

        <!-- 注册表单 -->
        <form class="register-form" @submit.prevent="handleRegister">
          <!-- 用户名 -->
          <div class="form-group">
            <div class="form-label">
              <span class="required">*</span>用户名
            </div>
            <div class="form-input-wrapper">
              <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  clearable
                  @input="validateUsername"
                  @blur="validateUsername"
              />
            </div>
            <div v-if="usernameError" class="error-message">{{ usernameError }}</div>
          </div>

          <!-- 邮箱 -->
          <div class="form-group">
            <div class="form-label">
              <span class="required">*</span>邮箱
            </div>
            <div class="form-input-wrapper">
              <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱地址"
                  clearable
                  @input="validateEmail"
                  @blur="validateEmail"
              />
            </div>
            <div v-if="emailError" class="error-message">{{ emailError }}</div>
          </div>

          <!-- 手机号 -->
          <div class="form-group">
            <div class="form-label">
              <span class="required">*</span>手机号
            </div>
            <div class="form-input-wrapper">
              <el-input
                  v-model="registerForm.phone"
                  placeholder="请输入手机号码"
                  clearable
                  @input="validatePhone"
                  @blur="validatePhone"
              />
            </div>
            <div v-if="phoneError" class="error-message">{{ phoneError }}</div>
          </div>

          <!-- 密码 -->
          <div class="form-group">
            <div class="form-label">
              <span class="required">*</span>密码
            </div>
            <div class="form-input-wrapper">
              <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码（6-20位）"
                  clearable
                  show-password
                  @input="validatePassword"
                  @blur="validatePassword"
              />
            </div>
            <div v-if="passwordError" class="error-message">{{ passwordError }}</div>
            <div v-else-if="registerForm.password" class="password-strength">
              密码强度：{{ passwordStrength }}
              <div class="strength-indicator">
                <div :class="['strength-bar', getStrengthClass(0)]"></div>
                <div :class="['strength-bar', getStrengthClass(1)]"></div>
                <div :class="['strength-bar', getStrengthClass(2)]"></div>
              </div>
            </div>
          </div>

          <!-- 确认密码 -->
          <div class="form-group">
            <div class="form-label">
              <span class="required">*</span>确认密码
            </div>
            <div class="form-input-wrapper">
              <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  clearable
                  show-password
                  @input="validateConfirmPassword"
                  @blur="validateConfirmPassword"
              />
            </div>
            <div v-if="confirmPasswordError" class="error-message">{{ confirmPasswordError }}</div>
          </div>

          <!-- 性别 -->
          <div class="form-group">
            <div class="form-label">
              <span class="required">*</span>性别
            </div>
            <div class="gender-options">
              <el-radio-group v-model="registerForm.gender">
                <el-radio label="male">男</el-radio>
                <el-radio label="female">女</el-radio>
              </el-radio-group>
            </div>
          </div>

          <!-- 注册按钮 -->
          <button type="submit" class="register-btn" @click="handleRegister">
            {{ loading ? '注册中...' : '立即注册' }}
          </button>
        </form>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账号？
          <router-link to="/login" class="login-btn">立即登录</router-link>
        </div>
      </div>

      <!-- 版权信息 -->
      <div class="footer">
        <p>© 2026 汉韵华章汉服文化社区 | 传承华夏文明，弘扬汉服文化</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register as registerApi } from '@/api/modules/user'

const router = useRouter()

const loading = ref(false)
// 添加标志，防止注册成功后继续验证
let isSubmitting = false
let isSuccess = false

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  gender: 'male'
})

// 错误信息
const usernameError = ref('')
const emailError = ref('')
const phoneError = ref('')
const passwordError = ref('')
const confirmPasswordError = ref('')

// 计算密码强度
const passwordStrength = computed(() => {
  const password = registerForm.password
  if (!password) return ''

  let strength = 0
  if (password.length >= 6) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^a-zA-Z0-9]/.test(password)) strength++

  if (strength === 0) return ''
  if (strength === 1) return '弱'
  if (strength === 2) return '中'
  return '强'
})

// 获取密码强度样式类
const getStrengthClass = (index) => {
  const strength = passwordStrength.value
  if (strength === '弱' && index === 0) return 'weak'
  if (strength === '中' && index <= 1) return 'medium'
  if (strength === '强' && index <= 2) return 'strong'
  return ''
}

// 验证用户名 - 添加防重复验证标志
const validateUsername = () => {
  // 如果正在提交或已成功，不进行验证
  if (isSubmitting || isSuccess) {
    return
  }

  const username = registerForm.username
  if (!username) {
    usernameError.value = '用户名不能为空'
  } else if (username.length < 2 || username.length > 20) {
    usernameError.value = '用户名长度在2-20个字符之间'
  } else {
    usernameError.value = ''
  }
}

// 验证邮箱
const validateEmail = () => {
  if (isSubmitting || isSuccess) return

  const email = registerForm.email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

  if (!email) {
    emailError.value = '邮箱不能为空'
  } else if (!emailRegex.test(email)) {
    emailError.value = '请输入有效的邮箱地址'
  } else {
    emailError.value = ''
  }
}

// 验证手机号
const validatePhone = () => {
  if (isSubmitting || isSuccess) return

  const phone = registerForm.phone
  const phoneRegex = /^1[3-9]\d{9}$/

  if (!phone) {
    phoneError.value = '手机号不能为空'
  } else if (!phoneRegex.test(phone)) {
    phoneError.value = '请输入有效的手机号码'
  } else {
    phoneError.value = ''
  }
}

// 验证密码
const validatePassword = () => {
  if (isSubmitting || isSuccess) return

  const password = registerForm.password
  if (!password) {
    passwordError.value = '密码不能为空'
  } else if (password.length < 6 || password.length > 20) {
    passwordError.value = '密码长度在6-20个字符之间'
  } else {
    passwordError.value = ''
  }
}

// 验证确认密码
const validateConfirmPassword = () => {
  if (isSubmitting || isSuccess) return

  const confirm = registerForm.confirmPassword
  if (!confirm) {
    confirmPasswordError.value = '请确认密码'
  } else if (confirm !== registerForm.password) {
    confirmPasswordError.value = '两次输入的密码不一致'
  } else {
    confirmPasswordError.value = ''
  }
}

// 处理注册
const handleRegister = async () => {
  // 防止重复提交
  if (loading.value || isSubmitting || isSuccess) {
    console.log('正在处理中，跳过重复提交')
    return
  }

  // 先验证所有字段
  validateUsername()
  validateEmail()
  validatePhone()
  validatePassword()
  validateConfirmPassword()

  // 如果有错误，不提交
  if (usernameError.value || emailError.value || phoneError.value ||
      passwordError.value || confirmPasswordError.value) {
    ElMessage.error('请填写完整的注册信息')
    return
  }

  // 检查表单是否完整
  if (!registerForm.username || !registerForm.email || !registerForm.phone ||
      !registerForm.password || !registerForm.confirmPassword || !registerForm.gender) {
    ElMessage.error('请填写所有必填项')
    return
  }

  isSubmitting = true
  loading.value = true

  try {
    console.log('发送注册请求:', registerForm)

    // 调用注册API
    const res = await registerApi({
      username: registerForm.username,
      email: registerForm.email,
      phone: registerForm.phone,
      password: registerForm.password,
      gender: registerForm.gender
    })

    console.log('注册响应:', res)

    if (res.code === 200) {
      // 标记为成功，防止后续验证
      isSuccess = true

      // 清除错误信息
      usernameError.value = ''
      emailError.value = ''
      phoneError.value = ''
      passwordError.value = ''
      confirmPasswordError.value = ''

      ElMessage.success(res.message || '注册成功！请登录')

      // 延迟跳转，确保消息显示完整
      setTimeout(() => {
        // 跳转前清除标志
        isSuccess = false
        isSubmitting = false

        router.push({
          path: '/login',
          query: {
            username: registerForm.username,
            registered: 'true'
          }
        })
      }, 1500)
    } else {
      // 注册失败，重置标志
      isSuccess = false
      isSubmitting = false
      ElMessage.error(res.message || '注册失败')
    }

  } catch (error) {
    console.error('注册失败:', error)
    isSuccess = false
    isSubmitting = false
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 组件卸载前清理
onBeforeUnmount(() => {
  isSubmitting = false
  isSuccess = false
})
</script>


<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0e6d6 100%);
  padding: 20px;
}

.register-wrapper {
  width: 100%;
  max-width: 500px;
  position: relative;
}

/* 顶部按钮容器 */
.top-buttons {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.nav-btn {
  padding: 10px 20px;
  color: #666;
  text-decoration: none;
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.nav-btn:hover {
  background: #f8f5f0;
  color: #d4af37;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.register-card {
  background: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  font-size: 2.5rem;
  background: linear-gradient(135deg, #d4af37, #8b4513);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 10px 0;
  font-family: 'STKaiti', 'KaiTi', serif;
}

.subtitle {
  color: #666;
  font-size: 1rem;
  letter-spacing: 4px;
  margin: 0 0 10px 0;
}

.welcome {
  color: #999;
  font-size: 0.9rem;
  margin: 0;
}

.register-form {
  margin: 30px 0;
}

/* 表单组 - 标签和输入框水平对齐 */
.form-group {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  position: relative;
  flex-wrap: wrap;
}

.form-label {
  width: 120px;
  color: #333;
  font-size: 0.95rem;
  font-weight: 500;
  margin-right: -30px;
  flex-shrink: 0;
}

.required {
  color: #ff4d4f;
  margin-right: 4px;
}

/* 输入框容器 */
.form-input-wrapper {
  flex: 1;
  position: relative;
  min-width: 0;
}

/* 自定义 Element Plus 输入框样式 */
:deep(.el-input) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  width: 100%;
  padding: 0 12px;
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 0 0 1px #ddd inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d4af37 inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2) inset, 0 0 0 1px #d4af37 inset;
}

:deep(.el-input__clear) {
  font-size: 16px;
  color: #999;
  transition: all 0.2s;
}

:deep(.el-input__clear:hover) {
  color: #666;
}

:deep(.el-input__suffix) {
  display: flex;
  align-items: center;
}

.error-message {
  color: #ff4d4f;
  font-size: 0.85rem;
  margin-top: 5px;
  min-height: 20px;
  width: 100%;
  margin-left: 135px; /* 与输入框对齐 */
}

.password-strength {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.85rem;
  color: #666;
  margin-top: 5px;
  min-height: 20px;
  width: 100%;
  margin-left: 135px; /* 与输入框对齐 */
}

.strength-indicator {
  display: flex;
  gap: 4px;
}

.strength-bar {
  width: 20px;
  height: 6px;
  border-radius: 3px;
  background: #e0e0e0;
  transition: all 0.3s;
}

.strength-bar.weak {
  background: #ff4d4f;
}

.strength-bar.medium {
  background: #faad14;
}

.strength-bar.strong {
  background: #52c41a;
}

/* 性别选项 - 使用 Element Plus 的单选按钮组 */
.gender-options {
  display: flex;
  align-items: center;
  margin-left: 0;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 30px;
}

:deep(.el-radio) {
  margin-right: 0;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #d4af37;
  border-color: #d4af37;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #d4af37;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 20px;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.login-link {
  text-align: center;
  color: #666;
  font-size: 0.95rem;
  padding-top: 20px;
  border-top: 1px solid #eee;
  margin-top: 20px;
}

.login-btn {
  color: #d4af37;
  font-weight: 500;
  margin-left: 5px;
  text-decoration: none;
}

.login-btn:hover {
  text-decoration: underline;
}

.footer {
  text-align: center;
  color: #999;
  font-size: 0.85rem;
  margin-top: 30px;
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    padding: 30px 20px;
  }

  .logo {
    font-size: 2rem;
  }

  .top-buttons {
    flex-direction: column;
    align-items: stretch;
  }

  .nav-btn {
    text-align: center;
  }

  .form-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-label {
    width: 100%;
    margin-bottom: 8px;
    margin-right: 0;
  }

  .form-input-wrapper {
    width: 100%;
  }

  .error-message,
  .password-strength {
    margin-left: 0;
  }

  :deep(.el-radio-group) {
    gap: 20px;
  }
}
</style>