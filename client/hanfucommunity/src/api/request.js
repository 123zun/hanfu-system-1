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
        const piniaToken = userStore?.token
        const storageToken = localStorage.getItem('hanfu_token')
        const token = piniaToken || storageToken

        console.log('[🔍 请求调试]', {
            url: config.url,
            method: config.method,
            piniaToken存在: !!piniaToken,
            storageToken存在: !!storageToken,
            最终token: token ? token.substring(0, 30) + '...' : null,
            Authorization头: token ? `Bearer ${token.substring(0, 30)}...` : '无token'
        })

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

// 响应拦截器 - 简化版本
// 响应拦截器 - 修复版本
request.interceptors.response.use(
    (response) => {
        // 隐藏loading
        if (response.config.showLoading !== false) {
            hideLoading()
        }

        console.log('📥 响应拦截器收到:', {
            url: response.config.url,
            method: response.config.method,
            状态码: response.status,
            状态文本: response.statusText,
            响应数据: response.data,
            数据类型: typeof response.data
        })

        // 处理文件下载
        if (response.config.responseType === 'blob') {
            return response
        }

        // 确保返回的数据不为 undefined
        if (response.data === undefined || response.data === null) {
            console.warn('⚠️ 响应数据为空，返回默认格式')
            return {
                code: 500,
                message: '服务器返回数据为空',
                data: null
            }
        }

        // 如果是字符串，尝试解析 JSON
        if (typeof response.data === 'string') {
            try {
                const parsed = JSON.parse(response.data)
                console.log('✅ JSON 解析成功:', parsed)
                return parsed
            } catch (e) {
                console.error('❌ JSON 解析失败:', e)
                return {
                    code: 500,
                    message: '数据格式错误',
                    data: null
                }
            }
        }

        // 正常情况返回数据
        return response.data
    },
    (error) => {
        // 隐藏loading
        hideLoading()

        console.error('❌ 响应错误:', error)

        // 构建标准错误响应
        let errorResponse = {
            code: 500,
            message: '请求失败',
            data: null
        }

        if (error.response) {
            console.error('服务器响应错误:', {
                status: error.response.status,
                data: error.response.data,
                headers: error.response.headers
            })

            errorResponse.code = error.response.status

            // 尝试从错误响应中提取消息
            if (error.response.data) {
                if (typeof error.response.data === 'string') {
                    try {
                        const parsed = JSON.parse(error.response.data)
                        errorResponse.message = parsed.message || parsed.msg || '请求失败'
                    } catch (e) {
                        errorResponse.message = error.response.data.substring(0, 100)
                    }
                } else {
                    errorResponse.message = error.response.data.message || error.response.data.msg || '请求失败'
                }
            }

            console.error('[🚨 认证失败详情]', {
                url: error.config?.url,
                status: error.response.status,
                errorMessage: errorResponse.message,
                responseData: error.response.data,
                requestHeaders: error.config?.headers,
            })

            // 显示错误消息
            switch (error.response.status) {
                case 400:
                    ElMessage.error(errorResponse.message || '请求参数错误')
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
                    ElMessage.error(errorResponse.message || '服务器内部错误')
                    break
                default:
                    ElMessage.error(errorResponse.message || '请求失败')
            }
        } else if (error.request) {
            console.error('请求无响应:', error.request)
            errorResponse.message = '网络异常，请检查网络连接'
            if (error.code === 'ECONNABORTED') {
                errorResponse.message = '请求超时，请检查网络连接'
            }
            ElMessage.error(errorResponse.message)
        } else {
            console.error('请求配置错误:', error.message)
            errorResponse.message = '请求配置错误: ' + error.message
            ElMessage.error(errorResponse.message)
        }

        // 【关键修改】返回 Promise.resolve 而不是 Promise.reject
        // 这样就不会触发 catch 块
        return Promise.resolve(errorResponse)
    }
)

// 导出配置好的axios实例
export default request