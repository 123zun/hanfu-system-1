import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
    // 状态
    const token = ref(localStorage.getItem('hanfu_token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('hanfu_user') || '{}'))

    // 计算属性
    const isLogin = computed(() => !!token.value)
    const nickname = computed(() => userInfo.value.nickname || '')
    const avatar = computed(() => userInfo.value.avatar || '')
    const userId = computed(() => userInfo.value.id || '')

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

    // 获取用户信息（模拟）
    const fetchUserInfo = async () => {
        if (!token.value) return

        // 这里先返回模拟数据，后面会对接真实API
        return new Promise((resolve) => {
            setTimeout(() => {
                const mockUser = {
                    id: 1,
                    nickname: '汉服爱好者',
                    avatar: '',
                    email: 'user@example.com'
                }
                setUserInfo(mockUser)
                resolve(mockUser)
            }, 500)
        })
    }

    // 退出登录
    const logout = () => {
        clearUserInfo()
    }

    return {
        token,
        userInfo,
        isLogin,
        nickname,
        avatar,
        userId,
        setToken,
        setUserInfo,
        fetchUserInfo,
        clearUserInfo,
        logout
    }
})