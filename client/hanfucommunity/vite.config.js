import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    //vueDevTools(),
  ],
  server: {
    port: 3000, // 前端开发服务器端口
    proxy: {
      '/api': { // 所有以/api开头的请求
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true
        // 注意：不要 rewrite 路径，后端 Controller 路径是 /api/xxx
        // rewrite: (path) => path.replace(/^\\/api/, '') // 已移除，否则 /api/work/pending-audit 会被改成 /work/pending-audit
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
