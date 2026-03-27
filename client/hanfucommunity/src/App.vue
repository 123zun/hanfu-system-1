<template>
  <div id="app" class="hanfu-app">
    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app, .hanfu-app {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  background-color: #f8f5f0;
  color: #333;
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #d4af37;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #b8860b;
}

/* 链接样式 */
a {
  color: #d4af37;
  text-decoration: none;
  transition: color 0.2s;
}

a:hover {
  color: #b8860b;
  text-decoration: underline;
}
</style>