import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/modules/user'

export const useUserStore = defineStore('user', () => {
    // 状态
    const token = ref(localStorage.getItem('hanfu_token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('hanfu_user') || '{}'))

    // 计算属性
    const isLogin = computed(() => !!token.value)
    const nickname = computed(() => userInfo.value.nickname || '')
    const avatar = computed(() => userInfo.value.avatar || '')
    const userId = computed(() => userInfo.value.id || '')

    // 登录
    const login = async (loginData) => {
        try {
            const res = await loginApi(loginData)

            // 假设后端返回: { token: 'xxx', userInfo: {...} }
            const { token: userToken, userInfo: info } = res

            // 保存token
            token.value = userToken
            userInfo.value = info

            // 持久化存储
            localStorage.setItem('hanfu_token', userToken)
            localStorage.setItem('hanfu_user', JSON.stringify(info))

            return Promise.resolve(res)
        } catch (error) {
            return Promise.reject(error)
        }
    }

    // 注册
    const register = async (registerData) => {
        try {
            const res = await registerApi(registerData)
            return Promise.resolve(res)
        } catch (error) {
            return Promise.reject(error)
        }
    }

    // 获取用户信息
    const fetchUserInfo = async () => {
        if (!token.value) return

        try {
            const res = await getUserInfoApi()
            userInfo.value = res
            localStorage.setItem('hanfu_user', JSON.stringify(res))
        } catch (error) {
            logout()
        }
    }

    // 设置token
    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('hanfu_token', newToken)
    }

    // 设置用户信息
    const setUserInfo = (info) => {
        userInfo.value = info
        localStorage.setItem('hanfu_user', JSON.stringify(info))
    }

    // 清除用户信息
    const clearUserInfo = () => {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('hanfu_token')
        localStorage.removeItem('hanfu_user')
    }

    // 退出登录
    const logout = async () => {
        try {
            await logoutApi()
        } catch (error) {
            console.error('退出登录失败:', error)
        } finally {
            clearUserInfo()
            router.push('/login')
        }
    }

    return {
        token,
        userInfo,
        isLogin,
        nickname,
        avatar,
        userId,
        login,
        register,
        fetchUserInfo,
        setToken,
        setUserInfo,
        clearUserInfo,
        logout
    }
})