import request from '../request'

// 获取当前用户ID
const getCurrentUserId = () => {
    try {
        const userStr = localStorage.getItem('hanfu_user')
        if (userStr) {
            const user = JSON.parse(userStr)
            return user.id
        }
    } catch (error) {
        console.error('获取用户ID失败:', error)
    }
    return null
}

// 获取作品列表
export const getWorkList = (params) => {
    return request({
        url: '/work/list',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 12,
            type: params?.type || undefined,
            keyword: params?.keyword || undefined,
            sort: params?.sort || 'latest',
            userId: params?.userId || undefined
        }
    })
}

// 获取作品详情
export const getWorkDetail = (id) => {
    const userId = getCurrentUserId()
    return request({
        url: `/work/detail/${id}`,
        method: 'GET',
        params: { userId }
    })
}

// 获取类型列表
export const getWorkTypes = () => {
    return request({
        url: '/work/types',
        method: 'GET'
    })
}

// 上传作品
export const uploadWork = (data) => {
    const formData = new FormData()
    formData.append('title', data.title)
    if (data.description) formData.append('description', data.description)
    if (data.type) formData.append('type', data.type)
    if (data.tags) formData.append('tags', JSON.stringify(data.tags))
    formData.append('userId', data.userId)
    if (data.coverImage) formData.append('coverImage', data.coverImage)
    if (data.images && data.images.length > 0) {
        data.images.forEach((image, index) => {
            formData.append('images', image)
        })
    }

    return request({
        url: '/work/upload',
        method: 'POST',
        data: formData,
        isUpload: true
    })
}

// 删除作品
export const deleteWork = (id, userId) => {
    return request({
        url: `/work/delete/${id}`,
        method: 'DELETE',
        params: { userId }
    })
}

// 点赞/取消点赞作品
export const likeWork = (id, userId) => {
    return request({
        url: `/work/like/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// 收藏/取消收藏作品
export const collectWork = (id, userId) => {
    return request({
        url: `/work/collect/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// ========== 评论相关 ==========

// 获取评论列表
export const getWorkComments = (targetType, targetId, userId) => {
    return request({
        url: '/comment/list',
        method: 'GET',
        params: { targetType, targetId, userId }
    })
}

// 添加评论
export const addWorkComment = (data) => {
    return request({
        url: '/comment/add',
        method: 'POST',
        data: data
    })
}

// 删除评论
export const deleteWorkComment = (id, userId) => {
    return request({
        url: `/comment/delete/${id}`,
        method: 'DELETE',
        params: { userId }
    })
}

// 点赞/取消点赞评论
export const likeWorkComment = (id, userId) => {
    return request({
        url: `/comment/like/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// ========== Work 专用上传接口 ==========

// 上传帖子封面图
export const uploadWorkCover = (formData) => {
    return request({
        url: '/work/upload-cover',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false
    })
}

// 上传帖子内容图片
export const uploadWorkImage = (formData) => {
    return request({
        url: '/work/upload-image',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false
    })
}

// 创建帖子
export const createWorkPost = (data) => {
    return request({
        url: '/work/create',
        method: 'POST',
        data: data
    })
}

// 更新帖子
export const updateWorkPost = (data) => {
    return request({
        url: '/work/update',
        method: 'PUT',
        data: data
    })
}

// 获取热门帖子
export const getHotWorks = (limit = 5) => {
    return request({
        url: '/work/hot',
        method: 'GET',
        params: { limit }
    })
}
