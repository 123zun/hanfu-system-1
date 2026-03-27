import request from '../request'

// 获取资讯列表
export const getArticleList = (params) => {
    return request({
        url: '/article/list',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 10,
            category: params?.category,
            keyword: params?.keyword
        }
    })
}

// 获取资讯详情
export const getArticleDetail = (id) => {
    return request({
        url: `/article/detail/${id}`,
        method: 'GET'
    })
}

// 创建资讯
export const createArticle = (data) => {
    return request({
        url: '/article/create',
        method: 'POST',
        data
    })
}

// 更新资讯
export const updateArticle = (id, data) => {
    return request({
        url: `/article/update/${id}`,
        method: 'PUT',
        data
    })
}

// 删除资讯
export const deleteArticle = (id) => {
    return request({
        url: `/article/delete/${id}`,
        method: 'DELETE'
    })
}

// 点赞/取消点赞
export const toggleLikeArticle = (id) => {
    return request({
        url: `/article/like/${id}`,
        method: 'POST'
    })
}

// 收藏/取消收藏
export const toggleCollectArticle = (id) => {
    return request({
        url: `/article/collect/${id}`,
        method: 'POST'
    })
}

// 获取资讯分类
export const getArticleCategories = () => {
    return request({
        url: '/article/categories',
        method: 'GET'
    })
}