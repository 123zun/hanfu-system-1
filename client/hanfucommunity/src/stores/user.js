import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi, register as registerApi } from '@/api/modules/user'

export const useUserStore = defineStore('user', () => {
    // 状态 - 修复 JSON.parse 错误
    const getStoredUser = () => {
        const storedUser = localStorage.getItem('hanfu_user')
        if (!storedUser) {
            return {}
        }
        try {
            return JSON.parse(storedUser)
        } catch (e) {
            console.error('解析存储的用户信息失败:', e)
            return {}
        }
    }

    const token = ref(localStorage.getItem('hanfu_token') || '')
    const userInfo = ref(getStoredUser())  // 使用安全的解析函数

    // 计算属性
    const isLogin = computed(() => !!token.value)
    const nickname = computed(() => userInfo.value.nickname || userInfo.value.username || '')
    const avatar = computed(() => userInfo.value.avatar || '')
    const userId = computed(() => userInfo.value.id || '')

    // 登录
    const login = async (loginData) => {
        try {
            console.log('Pinia store 开始登录，数据:', loginData)
            const res = await loginApi(loginData)
            console.log('Pinia store 登录API返回:', res)

            // 新的响应格式: { code: 200, message: 'xxx', data: { token: 'xxx', userInfo: {...} } }
            if (res && (res.code === 200 || res.code === 0)) {
                const data = res.data || {}
                const userToken = data.token
                const info = data.userInfo || data

                if (userToken) {
                    // 保存token
                    token.value = userToken
                    userInfo.value = info

                    // 持久化存储
                    localStorage.setItem('hanfu_token', userToken)
                    localStorage.setItem('hanfu_user', JSON.stringify(info))

                    console.log('Pinia store 登录成功，token已保存:', userToken)
                } else {
                    console.warn('登录成功但未返回token')
                }

                return Promise.resolve(res)
            } else {
                const errorMsg = res?.message || '登录失败'
                console.error('登录失败:', errorMsg)
                return Promise.reject(new Error(errorMsg))
            }
        } catch (error) {
            console.error('Pinia store 登录异常:', error)
            return Promise.reject(error)
        }
    }

    // 注册
    const register = async (registerData) => {
        try {
            console.log('Pinia store 开始注册，数据:', registerData)
            const res = await registerApi(registerData)
            console.log('Pinia store 注册API返回:', res)

            // 新的响应格式: { code: 200, message: 'xxx', data: {...} }
            if (res && (res.code === 200 || res.code === 0)) {
                console.log('Pinia store 注册成功')
                return Promise.resolve(res)
            } else {
                const errorMsg = res?.message || '注册失败'
                console.error('注册失败:', errorMsg)
                return Promise.reject(new Error(errorMsg))
            }
        } catch (error) {
            console.error('Pinia store 注册异常:', error)
            return Promise.reject(error)
        }
    }

    // 获取用户信息
    const fetchUserInfo = async () => {
        if (!token.value) {
            console.warn('未登录，无法获取用户信息')
            return
        }

        try {
            console.log('Pinia store 获取用户信息')
            const res = await getUserInfoApi()
            console.log('Pinia store 获取用户信息返回:', res)

            // 新的响应格式: { code: 200, message: 'xxx', data: {...} }
            if (res && (res.code === 200 || res.code === 0)) {
                const info = res.data || res
                userInfo.value = info
                localStorage.setItem('hanfu_user', JSON.stringify(info))
                console.log('Pinia store 用户信息更新成功')
            } else {
                console.error('获取用户信息失败:', res?.message)
                logout()
            }
        } catch (error) {
            console.error('Pinia store 获取用户信息异常:', error)
            logout()
        }
    }

    // 设置token
    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('hanfu_token', newToken)
        console.log('Pinia store token已设置')
    }

    // 设置用户信息
    const setUserInfo = (info) => {
        userInfo.value = info
        localStorage.setItem('hanfu_user', JSON.stringify(info))
        console.log('Pinia store 用户信息已设置:', info)
    }

    // 清除用户信息
    const clearUserInfo = () => {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('hanfu_token')
        localStorage.removeItem('hanfu_user')
        console.log('Pinia store 用户信息已清除')
    }

    // 退出登录
    const logout = async () => {
        try {
            console.log('Pinia store 开始退出登录')
            await logoutApi()
        } catch (error) {
            console.error('退出登录API调用失败:', error)
        } finally {
            clearUserInfo()
            console.log('Pinia store 跳转到登录页')
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