import request from '../request'

// 获取个性化推荐
export const getRecommendations = (userId, limit = 10) => {
    return request({
        url: '/recommend',
        method: 'GET',
        params: { userId, limit }
    })
}

// 手动触发重建 embeddings（管理员）
export const rebuildEmbeddings = () => {
    return request({
        url: '/recommend/rebuild',
        method: 'POST'
    })
}

// 更新单个作品 embedding
export const updateWorkEmbedding = (workId) => {
    return request({
        url: `/recommend/work/${workId}`,
        method: 'POST'
    })
}

// 更新单个用户 embedding
export const updateUserEmbedding = (userId) => {
    return request({
        url: `/recommend/user/${userId}`,
        method: 'POST'
    })
}
