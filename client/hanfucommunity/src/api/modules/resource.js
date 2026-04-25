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

// 获取资源列表
export const getResourceList = (params) => {
    const userId = getCurrentUserId()

    return request({
        url: '/resource/list',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 12,
            keyword: params?.keyword,
            type: params?.type,
            status: params?.status,
            sortField: params?.sortField || 'create_time',
            sortOrder: params?.sortOrder || 'desc',
            userId: params?.userId !== undefined ? params.userId : userId
        }
    })
}

// 获取资源详情
export const getResourceDetail = (id) => {
    const userId = getCurrentUserId()

    return request({
        url: `/resource/detail/${id}`,
        method: 'GET',
        params: {
            userId: userId
        }
    })
}

// 获取资源类型列表
export const getResourceTypes = () => {
    return request({
        url: '/resource/types',
        method: 'GET'
    })
}

// 创建资源
export const createResource = (data) => {
    return request({
        url: '/resource/create',
        method: 'POST',
        data: data
    })
}

// 更新资源
export const updateResource = (id, data) => {
    return request({
        url: `/resource/update/${id}`,
        method: 'PUT',
        data: data
    })
}

// 删除资源
export const deleteResource = (id) => {
    return request({
        url: `/resource/delete/${id}`,
        method: 'DELETE'
    })
}

// 下载资源
export const downloadResource = (id) => {
    return request({
        url: `/resource/download/${id}`,
        method: 'GET'
    })
}

// 上传资源文件（上传成功后会直接创建数据库记录）
export const uploadResource = (file, params) => {
    const formData = new FormData()
    formData.append('file', file)

    if (params) {
        Object.keys(params).forEach(key => {
            if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                formData.append(key, params[key])
            }
        })
    }

    return request({
        url: '/resource/upload',
        method: 'POST',
        data: formData,
        isUpload: true
    })
}

// 点赞/取消点赞资源
export const likeResource = (id, userId) => {
    return request({
        url: `/resource/like/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// 检查资源点赞状态
export const checkResourceLiked = (resourceId, userId) => {
    return request({
        url: '/resource/like/check',
        method: 'GET',
        params: { resourceId, userId }
    })
}

// 收藏/取消收藏资源
export const collectResource = (id, userId) => {
    return request({
        url: `/resource/collect/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// 检查资源收藏状态
export const checkResourceCollected = (resourceId, userId) => {
    return request({
        url: '/resource/collect/check',
        method: 'GET',
        params: { resourceId, userId }
    })
}
