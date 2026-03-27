<template>
  <div class="register-container">
    <div class="register-wrapper">
      <!-- 返回首页按钮 -->
      <router-link to="/" class="back-home">
        <span>← 返回首页</span>
      </router-link>

      <!-- 返回登录按钮 -->
      <router-link to="/login" class="back-login">
        <span>← 返回登录</span>
      </router-link>

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
          <div class="form-group">
            <label>昵称</label>
            <input
                v-model="registerForm.nickname"
                type="text"
                placeholder="请输入昵称（2-10个字符）"
                required
            />
          </div>

          <div class="form-group">
            <label>用户名</label>
            <input
                v-model="registerForm.username"
                type="text"
                placeholder="请输入用户名（英文/数字）"
                required
            />
          </div>

          <div class="form-group">
            <label>邮箱</label>
            <input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱地址"
                required
            />
          </div>

          <div class="form-group">
            <label>密码</label>
            <input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（6-20位）"
                required
            />
            <div class="password-strength" v-if="registerForm.password">
              密码强度：{{ passwordStrength }}
            </div>
          </div>

          <div class="form-group">
            <label>确认密码</label>
            <input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                required
            />
          </div>

          <div class="form-group">
            <label>性别</label>
            <div class="gender-options">
              <label>
                <input v-model="registerForm.gender" type="radio" value="male" />
                <span>男生</span>
              </label>
              <label>
                <input v-model="registerForm.gender" type="radio" value="female" />
                <span>女生</span>
              </label>
              <label>
                <input v-model="registerForm.gender" type="radio" value="secret" />
                <span>保密</span>
              </label>
            </div>
          </div>

          <div class="form-options">
            <label class="agree">
              <input v-model="registerForm.agree" type="checkbox" />
              <span>我已阅读并同意 <a href="#" @click.prevent>《用户协议》</a> 和 <a href="#" @click.prevent>《隐私政策》</a></span>
            </label>
          </div>

          <button type="submit" class="register-btn" :disabled="loading">
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const registerForm = reactive({
  nickname: '',
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  gender: 'secret',
  agree: false
})

// 计算密码强度
const passwordStrength = computed(() => {
  const password = registerForm.password
  if (!password) return '无'

  let strength = 0
  if (password.length >= 6) strength++
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[^a-zA-Z0-9]/.test(password)) strength++

  const strengths = ['', '弱', '中', '强']
  return strengths[strength] || '无'
})

const handleRegister = async () => {
  loading.value = true

  try {
    // 模拟注册成功
    await new Promise(resolve => setTimeout(resolve, 1000))

    // 跳转到登录页
    router.push({
      path: '/login',
      query: { username: registerForm.username }
    })

  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
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

.back-home {
  position: absolute;
  top: -50px;
  left: 0;
  color: #666;
  text-decoration: none;
  padding: 8px 16px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.back-login {
  position: absolute;
  top: -50px;
  right: 0;
  color: #666;
  text-decoration: none;
  padding: 8px 16px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.back-home:hover,
.back-login:hover {
  background: #f8f5f0;
  color: #d4af37;
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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #d4af37;
  box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
}

.password-strength {
  font-size: 0.85rem;
  color: #666;
  margin-top: 5px;
}

.gender-options {
  display: flex;
  gap: 20px;
  margin-top: 8px;
}

.gender-options label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-weight: normal;
}

.gender-options input[type="radio"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.form-options {
  margin-bottom: 20px;
}

.agree {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  cursor: pointer;
  font-size: 0.9rem;
}

.agree input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.agree a {
  color: #d4af37;
  text-decoration: none;
}

.agree a:hover {
  text-decoration: underline;
}

.register-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  color: #666;
  font-size: 0.95rem;
  padding-top: 20px;
  border-top: 1px solid #eee;
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

  .back-home,
  .back-login {
    position: relative;
    top: 0;
    display: block;
    width: fit-content;
    margin: 0 auto 10px;
  }
}
</style>