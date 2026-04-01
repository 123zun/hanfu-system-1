<template>
  <div class="posts-view">
    <div class="section-header">
      <h2 class="section-title">
        <el-icon><ChatLineSquare /></el-icon>
        互动帖子
      </h2>
      <p class="section-subtitle">与社区成员交流汉服心得，分享你的故事</p>
    </div>

    <div class="coming-soon">
      <el-icon><Clock /></el-icon>
      <p>互动帖子功能正在开发中，敬请期待...</p>
    </div>
  </div>
</template>

<script setup>
import { ChatLineSquare, Clock } from '@element-plus/icons-vue'
</script>

<style scoped>
.posts-view {
  padding: 30px;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;
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

.coming-soon {
  text-align: center;
  padding: 60px;
  background: #f8f5f0;
  border-radius: 12px;
  border: 2px dashed rgba(212, 175, 55, 0.3);
  width: 100%;
  max-width: 500px;
}

.coming-soon .el-icon {
  font-size: 3rem;
  color: #d4af37;
  margin-bottom: 20px;
}

.coming-soon p {
  color: #666;
  font-size: 1.2rem;
  margin: 0;
}
</style>