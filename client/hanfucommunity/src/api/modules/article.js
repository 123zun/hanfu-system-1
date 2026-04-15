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

// 获取用户收藏的资讯列表
export const getMyArticleCollections = (userId) => {
    return request({
        url: '/article/my-collections',
        method: 'GET',
        params: { userId }
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

// 增加资讯浏览量
export const increaseArticleView = (id) => {
    return request({
        url: `/article/view/${id}`,
        method: 'POST'
    })
}

// 在 article.js 中添加

// 点赞/取消点赞文章
export const likeArticle = (id, userId) => {
    return request({
        url: `/article/like/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// 收藏/取消收藏文章
export const collectArticle = (id, userId) => {
    return request({
        url: `/article/collect/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// ========== 评论相关 ==========

// 获取评论列表
export const getComments = (targetType, targetId, userId) => {
    return request({
        url: '/comment/list',
        method: 'GET',
        params: { targetType, targetId, userId }
    })
}

// 添加评论
export const addComment = (data) => {
    return request({
        url: '/comment/add',
        method: 'POST',
        data: data
    })
}

// 删除评论
export const deleteComment = (id, userId) => {
    return request({
        url: `/comment/delete/${id}`,
        method: 'DELETE',
        params: { userId }
    })
}

// 点赞/取消点赞评论
export const likeComment = (id, userId) => {
    return request({
        url: `/comment/like/${id}`,
        method: 'POST',
        params: { userId }
    })
}

// 检查评论点赞状态
export const checkCommentLiked = (commentId, userId) => {
    return request({
        url: '/comment/like/check',
        method: 'GET',
        params: { commentId, userId }
    })
}