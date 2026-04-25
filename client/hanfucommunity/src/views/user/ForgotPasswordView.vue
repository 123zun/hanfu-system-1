<template>
  <div class="forgot-container">
    <div class="forgot-wrapper">
      <router-link to="/login" class="back-login">
        <el-icon><ArrowLeft /></el-icon>
        返回登录
      </router-link>

      <div class="forgot-card">
        <div class="forgot-header">
          <h1>找回密码</h1>
          <p>通过验证用户名和邮箱重置密码</p>
        </div>

        <!-- Step 1: 验证用户名和邮箱 -->
        <div v-if="step === 1" class="step-content">
          <el-form
              ref="verifyFormRef"
              :model="verifyForm"
              :rules="verifyRules"
              class="forgot-form"
          >
            <el-form-item prop="username">
              <el-input
                  v-model="verifyForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  :prefix-icon="User"
                  clearable
              />
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                  v-model="verifyForm.email"
                  placeholder="请输入注册邮箱"
                  size="large"
                  :prefix-icon="Message"
                  clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  size="large"
                  :loading="verifying"
                  @click="handleVerify"
                  class="verify-btn"
              >
                验证
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 2: 发送验证码 -->
        <div v-else-if="step === 2" class="step-content">
          <div class="email-info">
            <el-icon><Message /></el-icon>
            <span>验证码已发送至：{{ verifyForm.email }}</span>
          </div>
          <el-form
              ref="codeFormRef"
              :model="codeForm"
              :rules="codeRules"
              class="forgot-form"
          >
            <el-form-item prop="code">
              <el-input
                  v-model="codeForm.code"
                  placeholder="请输入6位验证码"
                  size="large"
                  :prefix-icon="Key"
                  maxlength="6"
                  clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  size="large"
                  :loading="sendingCode"
                  @click="handleSendCode"
                  class="send-btn"
                  :disabled="codeCooldown > 0"
              >
                {{ codeCooldown > 0 ? `${codeCooldown}秒后重发` : '发送验证码' }}
              </el-button>
            </el-form-item>
            <el-form-item>
              <el-button
                  type="success"
                  size="large"
                  @click="handleVerifyCode"
                  class="next-btn"
                  :loading="verifyingCode"
              >
                下一步
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Step 3: 重置密码 -->
        <div v-else-if="step === 3" class="step-content">
          <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              class="forgot-form"
          >
            <el-form-item prop="newPassword">
              <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  clearable
              />
            </el-form-item>
            <div class="password-hint">
              密码要求：8-16位，包含数字、英文、特殊符号
            </div>
            <el-form-item prop="confirmPassword">
              <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                  clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button
                  type="success"
                  size="large"
                  @click="handleResetPassword"
                  class="reset-btn"
                  :loading="resetting"
              >
                重置密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 成功 -->
        <div v-else-if="step === 4" class="step-content success-content">
          <el-icon class="success-icon"><SuccessFilled /></el-icon>
          <h2>密码重置成功！</h2>
          <p>请使用新密码登录</p>
          <router-link to="/login" class="login-link">返回登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Message, Key, Lock, ArrowLeft, SuccessFilled } from '@element-plus/icons-vue'

// 步骤：1-验证信息，2-验证码，3-重置密码，4-成功
const step = ref(1)

// 验证表单
const verifyFormRef = ref()
const verifyForm = reactive({
  username: '',
  email: ''
})
const verifyRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}
const verifying = ref(false)

// 验证码表单
const codeFormRef = ref()
const codeForm = reactive({
  code: ''
})
const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位', trigger: 'blur' }
  ]
}
const sendingCode = ref(false)
const verifyingCode = ref(false)
const codeCooldown = ref(0)
let cooldownTimer = null

// 密码表单
const passwordFormRef = ref()
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 16, message: '密码8-16位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}
const resetting = ref(false)

// 验证用户名和邮箱
const handleVerify = async () => {
  if (!verifyFormRef.value) return
  await verifyFormRef.value.validate(async (valid) => {
    if (!valid) return

    verifying.value = true
    try {
      const res = await fetch(`/api/user/forgot/verify?username=${verifyForm.username}&email=${verifyForm.email}`)
      const data = await res.json()
      if (data.code === 200) {
        step.value = 2
        // 自动发送验证码
        handleSendCode()
      } else {
        ElMessage.error(data.message || '验证失败')
      }
    } catch (error) {
      ElMessage.error('验证失败')
    } finally {
      verifying.value = false
    }
  })
}

// 发送验证码
const handleSendCode = async () => {
  if (codeCooldown.value > 0) return

  sendingCode.value = true
  try {
    const res = await fetch('/api/user/forgot/send-code', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: verifyForm.username,
        email: verifyForm.email
      })
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('验证码已发送')
      codeCooldown.value = 60
      cooldownTimer = setInterval(() => {
        codeCooldown.value--
        if (codeCooldown.value <= 0) {
          clearInterval(cooldownTimer)
        }
      }, 1000)
    } else {
      ElMessage.error(data.message || '发送失败')
    }
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    sendingCode.value = false
  }
}

// 验证验证码
const handleVerifyCode = async () => {
  if (!codeFormRef.value) return
  await codeFormRef.value.validate(async (valid) => {
    if (!valid) return

    verifyingCode.value = true
    try {
      const res = await fetch('/api/user/forgot/verify-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: verifyForm.email,
          code: codeForm.code
        })
      })
      const data = await res.json()
      if (data.code === 200) {
        step.value = 3
      } else {
        ElMessage.error(data.message || '验证码错误')
      }
    } catch (error) {
      ElMessage.error('验证失败')
    } finally {
      verifyingCode.value = false
    }
  })
}

// 重置密码
const handleResetPassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    resetting.value = true
    try {
      const res = await fetch('/api/user/forgot/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: verifyForm.email,
          code: codeForm.code,
          newPassword: passwordForm.newPassword
        })
      })
      const data = await res.json()
      if (data.code === 200) {
        step.value = 4
      } else {
        ElMessage.error(data.message || '重置失败')
      }
    } catch (error) {
      ElMessage.error('重置失败')
    } finally {
      resetting.value = false
    }
  })
}

// 清理定时器
onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
</script>

<style scoped>
.forgot-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0e6d6 100%);
  padding: 20px;
}

.forgot-wrapper {
  width: 100%;
  max-width: 420px;
}

.back-login {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #666;
  margin-bottom: 20px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  transition: all 0.3s;
  text-decoration: none;
}

.back-login:hover {
  background: rgba(255, 255, 255, 1);
  color: #d4af37;
}

.forgot-card {
  background: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.forgot-header {
  text-align: center;
  margin-bottom: 30px;
}

.forgot-header h1 {
  font-size: 1.8rem;
  color: #333;
  margin: 0 0 10px 0;
}

.forgot-header p {
  color: #999;
  font-size: 0.9rem;
}

.step-content {
  margin-top: 20px;
}

.email-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #67c23a;
  font-size: 0.9rem;
  margin-bottom: 20px;
  padding: 12px;
  background: #f0f9eb;
  border-radius: 8px;
}

.forgot-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 15px;
}

.forgot-form :deep(.el-input__prefix) {
  margin-right: 10px;
}

.verify-btn,
.send-btn,
.next-btn,
.reset-btn {
  width: 100%;
  height: 45px;
  border-radius: 10px;
  font-size: 1rem;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
}

.verify-btn:hover,
.send-btn:hover,
.next-btn:hover,
.reset-btn:hover {
  opacity: 0.9;
}

.password-hint {
  font-size: 0.8rem;
  color: #999;
  margin: -10px 0 15px 0;
  text-align: center;
}

.success-content {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 60px;
  color: #67c23a;
  margin-bottom: 20px;
}

.success-content h2 {
  color: #333;
  margin-bottom: 10px;
}

.success-content p {
  color: #666;
  margin-bottom: 30px;
}

.login-link {
  display: inline-block;
  padding: 12px 40px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  color: white;
  border-radius: 10px;
  text-decoration: none;
  font-size: 1rem;
}
</style>