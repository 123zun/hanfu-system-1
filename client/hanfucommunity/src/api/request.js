import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 15000, // 请求超时时间
    headers: {
        'Content-Type': 'application/json;charset=UTF-8'
    }
})

// 全局 loading 实例
let loadingInstance = null
let requestCount = 0

// 显示loading
const showLoading = () => {
    if (requestCount === 0) {
        loadingInstance = ElLoading.service({
            lock: true,
            text: '加载中...',
            background: 'rgba(0, 0, 0, 0.3)',
            fullscreen: true
        })
    }
    requestCount++
}

// 隐藏loading
const hideLoading = () => {
    requestCount--
    if (requestCount <= 0) {
        loadingInstance?.close()
        loadingInstance = null
    }
}

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 如果需要显示loading
        if (config.showLoading !== false) {
            showLoading()
        }

        // 从Pinia store或localStorage获取token
        const userStore = useUserStore()
        const token = userStore.token || localStorage.getItem('hanfu_token')

        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        // 如果是上传文件，修改Content-Type
        if (config.isUpload) {
            config.headers['Content-Type'] = 'multipart/form-data'
        }

        return config
    },
    (error) => {
        // 请求错误时也要隐藏loading
        hideLoading()
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        // 隐藏loading
        if (response.config.showLoading !== false) {
            hideLoading()
        }

        const res = response.data

        // 处理文件下载
        if (response.config.responseType === 'blob') {
            return response
        }

        // 根据后端返回的数据结构处理
        // 假设标准返回格式: { code: 200, message: 'success', data: {} }
        if (res.code === 200 || res.code === 0) {
            return res.data || res
        } else {
            // 业务逻辑错误
            const errorMsg = res.message || '请求失败'
            ElMessage.error(errorMsg)

            // 特定错误码处理
            if (res.code === 401) {
                // token过期，清除登录状态
                localStorage.removeItem('hanfu_token')
                localStorage.removeItem('hanfu_user')
                router.push('/login')
            } else if (res.code === 403) {
                ElMessage.error('权限不足')
            }

            return Promise.reject(new Error(errorMsg))
        }
    },
    (error) => {
        // 隐藏loading
        hideLoading()

        // 统一错误处理
        if (error.response) {
            switch (error.response.status) {
                case 400:
                    ElMessage.error('请求参数错误')
                    break
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    localStorage.removeItem('hanfu_token')
                    localStorage.removeItem('hanfu_user')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('拒绝访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误')
                    break
                case 502:
                case 503:
                case 504:
                    ElMessage.error('服务暂时不可用，请稍后重试')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else if (error.request) {
            // 请求发出但没有收到响应
            if (error.code === 'ECONNABORTED') {
                ElMessage.error('请求超时，请检查网络连接')
            } else {
                ElMessage.error('网络异常，请检查网络连接')
            }
        } else {
            ElMessage.error('请求配置错误')
        }

        return Promise.reject(error)
    }
)

// 导出配置好的axios实例
export default request