<template>
  <div class="home-container">
    <header class="home-header">
      <h1 class="title">汉韵华章</h1>
      <p class="subtitle">汉服文化社区</p>
      <div class="nav-buttons">
        <router-link to="/login" class="nav-btn login-btn">登录</router-link>
        <router-link to="/register" class="nav-btn register-btn">注册</router-link>
      </div>
    </header>

    <main class="home-main">
      <div class="welcome-section">
        <h2>欢迎来到汉韵华章社区</h2>
        <p>这里是一个分享、交流、学习汉服文化的平台</p>
        <div class="features">
          <div class="feature-card">
            <div class="feature-icon">👘</div>
            <h3>汉服资讯</h3>
            <p>获取最新的汉服文化动态和资讯</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📸</div>
            <h3>作品展示</h3>
            <p>分享你的汉服摄影和穿搭作品</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🎪</div>
            <h3>活动交流</h3>
            <p>参与线下汉服活动和同袍交流</p>
          </div>
        </div>
      </div>

      <div class="quick-links">
        <router-link to="/login" class="quick-link">开始使用 →</router-link>
      </div>
    </main>

    <footer class="home-footer">
      <p>© 2026 汉韵华章汉服文化社区 | 弘扬传统文化，传承华夏衣冠</p>
    </footer>
  </div>
</template>

<script setup>
// 这里可以添加首页的逻辑
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f8f5f0 0%, #f0e6d6 100%);
}

.home-header {
  text-align: center;
  padding: 40px 20px;
  background: white;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.title {
  font-size: 3rem;
  background: linear-gradient(135deg, #d4af37, #8b4513);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 10px 0;
  font-family: 'STKaiti', 'KaiTi', serif;
}

.subtitle {
  font-size: 1.2rem;
  color: #666;
  letter-spacing: 8px;
  margin: 0 0 30px 0;
}

.nav-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.nav-btn {
  padding: 10px 30px;
  border-radius: 25px;
  text-decoration: none;
  font-weight: bold;
  transition: all 0.3s;
}

.login-btn {
  background: white;
  border: 2px solid #d4af37;
  color: #d4af37;
}

.login-btn:hover {
  background: #d4af37;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.register-btn {
  background: linear-gradient(135deg, #d4af37, #b8860b);
  border: 2px solid #d4af37;
  color: white;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
  filter: brightness(1.1);
}

.home-main {
  flex: 1;
  padding: 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.welcome-section {
  text-align: center;
  margin-bottom: 60px;
}

.welcome-section h2 {
  font-size: 2.2rem;
  color: #333;
  margin-bottom: 15px;
}

.welcome-section p {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 40px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  margin-top: 40px;
}

.feature-card {
  background: white;
  padding: 30px 20px;
  border-radius: 15px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
  border-color: #d4af37;
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-card h3 {
  color: #333;
  font-size: 1.3rem;
  margin-bottom: 10px;
}

.feature-card p {
  color: #666;
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0;
}

.quick-links {
  text-align: center;
  margin-top: 40px;
}

.quick-link {
  display: inline-block;
  padding: 12px 40px;
  background: linear-gradient(135deg, #d4af37, #b8860b);
  color: white;
  text-decoration: none;
  border-radius: 25px;
  font-size: 1.1rem;
  font-weight: bold;
  transition: all 0.3s;
}

.quick-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(212, 175, 55, 0.3);
  text-decoration: none;
}

.home-footer {
  text-align: center;
  padding: 20px;
  background: white;
  color: #999;
  font-size: 0.9rem;
  border-top: 1px solid rgba(212, 175, 55, 0.1);
  margin-top: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title {
    font-size: 2.2rem;
  }

  .subtitle {
    font-size: 1rem;
    letter-spacing: 4px;
  }

  .nav-buttons {
    flex-direction: column;
    align-items: center;
  }

  .nav-btn {
    width: 200px;
  }

  .features {
    grid-template-columns: 1fr;
  }
}
</style>