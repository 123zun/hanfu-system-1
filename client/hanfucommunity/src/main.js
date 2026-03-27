import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

// 创建应用实例
const app = createApp(App)

// 使用插件
app.use(createPinia())  // Pinia 状态管理
app.use(ElementPlus)    // Element Plus UI
app.use(router)         // 路由

// 挂载应用
app.mount('#app')