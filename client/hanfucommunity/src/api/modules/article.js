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

// 获取资讯列表
export const getArticleList = (params) => {
    const userId = getCurrentUserId()

    return request({
        url: '/article/list',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 10,
            category: params?.category,
            keyword: params?.keyword,
            status: params?.status,
            isTop: params?.isTop,
            isHot: params?.isHot,
            sortField: params?.sortField || 'publish_time',
            sortOrder: params?.sortOrder || 'desc',
            userId: params?.userId !== undefined ? params.userId : userId
        }
    })
}

// 获取热门资讯
export const getHotArticles = (limit = 5) => {
    const userId = getCurrentUserId()

    return request({
        url: '/article/hot',
        method: 'GET',
        params: {
            limit: limit,
            userId: userId
        }
    })
}

// 获取资讯详情
export const getArticleDetail = (id) => {
    const userId = getCurrentUserId()

    return request({
        url: `/article/detail/${id}`,
        method: 'GET',
        params: {
            userId: userId
        }
    })
}

// 获取资讯分类
export const getArticleCategories = () => {
    return request({
        url: '/article/categories',
        method: 'GET'
    })
}

// 创建资讯
export const createArticle = (data) => {
    return request({
        url: '/article/create',
        method: 'POST',
        data: data
    })
}

// 更新资讯
export const updateArticle = (id, data) => {
    return request({
        url: `/article/update/${id}`,
        method: 'PUT',
        data: data
    })
}

// 删除资讯
export const deleteArticle = (id) => {
    return request({
        url: `/article/delete/${id}`,
        method: 'DELETE'
    })
}