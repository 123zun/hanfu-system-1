<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-background">
      <div class="decoration-left">
        <div class="pattern"></div>
      </div>
      <div class="decoration-right">
        <div class="pattern"></div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="login-wrapper">
      <!-- 返回首页按钮 -->
      <router-link to="/" class="back-home">
        <el-icon><House /></el-icon>
        返回首页
      </router-link>

      <!-- 登录卡片 -->
      <div class="login-card">
        <!-- 头部 -->
        <div class="login-header">
          <div class="logo">
            <h1>汉韵华章</h1>
            <p class="subtitle">汉服文化社区</p>
          </div>
          <p class="welcome">欢迎回来，请登录您的账号</p>
        </div>

        <!-- 登录表单 -->
        <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名/邮箱/手机号"
                size="large"
                :prefix-icon="User"
                clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
            />
          </el-form-item>

          <el-form-item prop="remember" class="remember-item">
            <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
            <router-link to="/forgot-password" class="forgot-password">忘记密码？</router-link>
          </el-form-item>

          <el-form-item>
            <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-btn"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 注册链接 -->
        <div class="register-link">
          还没有账号？
          <router-link to="/register" class="register-btn">立即注册</router-link>
        </div>
      </div>

      <!-- 版权信息 -->
      <div class="footer">
        <p>© 2026 汉韵华章汉服文化社区 | 弘扬传统文化，传承华夏衣冠</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, House } from '@element-plus/icons-vue'
import { login } from '@/api/modules/user'

// 路由
const router = useRouter()
const route = useRoute()

// 表单引用
const loginFormRef = ref()

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名/邮箱/手机号', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  // 验证表单
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      console.log('🚀 发送登录请求:', {
        username: loginForm.username,
        password: '***' // 不显示真实密码
      })

      // 调用用户登录API
      const response = await login({
        username: loginForm.username,
        password: loginForm.password
      })

      console.log('✅ 登录API返回:', response)

      // 新的响应格式: { code: 200, message: 'xxx', data: {token: 'xxx', userInfo: {...}} }
      if (response && (response.code === 200 || response.code === 0)) {
        const data = response.data || {}
        const token = data.token
        const userInfo = data.userInfo || data

        if (token) {
          // 保存到localStorage
          localStorage.setItem('hanfu_token', token)
          localStorage.setItem('hanfu_user', JSON.stringify(userInfo))

          // 如果记住我
          if (loginForm.remember) {
            localStorage.setItem('remember_me', 'true')
          } else {
            localStorage.removeItem('remember_me')
          }

          console.log('登录成功，token已保存')
          ElMessage.success(response.message || '登录成功！')

          // 跳转到首页或来源页面
          const redirect = route.query.redirect || '/'
          router.push(redirect)
        } else {
          console.warn('登录成功但未返回token')
          ElMessage.success(response.message || '登录成功！')
        }
      } else {
        // 处理其他code
        ElMessage.error(response?.message || '登录失败')
      }

    } catch (error) {
      console.error('❌ 登录失败错误:', error)

      // 错误信息已经在拦截器中显示过了
      // 这里可以添加额外的错误处理逻辑
      if (!error.response && error.message && !error.message.includes('JSON')) {
        ElMessage.error('登录失败: ' + error.message)
      }
    } finally {
      loading.value = false
    }
  })
}

// 页面加载时检查记住我
onMounted(() => {
  const remember = localStorage.getItem('remember_me')
  if (remember === 'true') {
    loginForm.remember = true
  }

  // 如果有预填的用户名
  if (route.query.username) {
    loginForm.username = route.query.username
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0e6d6 100%);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.decoration-left,
.decoration-right {
  position: absolute;
  top: 0;
  height: 100%;
  width: 300px;
  opacity: 0.1;
}

.decoration-left {
  left: 0;
  background: linear-gradient(135deg, transparent 0%, rgba(212, 175, 55, 0.1) 100%);
}

.decoration-right {
  right: 0;
  background: linear-gradient(225deg, transparent 0%, rgba(139, 69, 19, 0.1) 100%);
}

.pattern {
  width: 100%;
  height: 100%;
  background-image:
      repeating-linear-gradient(45deg, transparent, transparent 10px,
      rgba(212, 175, 55, 0.1) 10px, rgba(212, 175, 55, 0.1) 20px);
}

.login-wrapper {
  width: 100%;
  max-width: 450px;
  padding: 20px;
  position: relative;
  z-index: 1;
}

.back-home {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #666;
  margin-bottom: 20px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  transition: all 0.3s;
}

.back-home:hover {
  background: rgba(255, 255, 255, 1);
  color: #d4af37;
  text-decoration: none;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo h1 {
  font-size: 2.5rem;
  background: linear-gradient(135deg, #d4af37, #8b4513);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0;
  font-family: 'STKaiti', 'KaiTi', serif;
}

.subtitle {
  color: #666;
  font-size: 1rem;
  margin-top: 5px;
  letter-spacing: 4px;
}

.welcome {
  color: #999;
  font-size: 0.9rem;
  margin-top: 10px;
}

.login-form {
  margin: 30px 0;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 8px 15px;
}

.login-form :deep(.el-input__prefix) {
  margin-right: 10px;
}

.remember-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-password {
  font-size: 0.9rem;
  color: #999;
  margin-left: 185px;
}

.forgot-password:hover {
  color: #d4af37;
}

.login-btn {
  width: 100%;
  height: 45px;
  border-radius: 10px;
  font-size: 1rem;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}

.divider span {
  padding: 0 15px;
}

.register-link {
  text-align: center;
  color: #666;
  font-size: 0.95rem;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.register-btn {
  color: #d4af37;
  font-weight: 500;
  margin-left: 5px;
}

.register-btn:hover {
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
  .login-wrapper {
    padding: 10px;
  }

  .login-card {
    padding: 30px 20px;
  }

  .logo h1 {
    font-size: 2rem;
  }

  .decoration-left,
  .decoration-right {
    width: 150px;
  }
}
</style>