import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
        meta: { title: '汉韵华章 - 汉服文化社区' }
    },
    {
        path: '/main',
        name: 'main',
        component: () => import('@/views/MainView.vue'),
        meta: {
            title: '主页面',
            requiresAuth: true
        }
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/user/LoginView.vue'),
        meta: { title: '登录 - 汉韵华章' }
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/user/RegisterView.vue'),
        meta: { title: '注册 - 汉韵华章' }
    },
    {
        path: '/article',
        name: 'article',
        component: () => import('@/components/main/ArticleCreate.vue'),
        meta: {
            title: '创建资讯',
            requiresAuth: true
        }
    },
    {
        path: '/article/edit/:id',  // 编辑路由，带参数
        name: 'article-edit',       // 这个名字要与组件中的判断匹配
        component: () => import('@/components/main/ArticleCreate.vue'),
        meta: {
            title: '编辑资讯',
            requiresAuth: true
        }
    },
]

// 创建路由实例
const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title
    }
    next()
})

export default router