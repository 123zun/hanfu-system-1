import request from '../request'

// 获取作品列表
export const getWorkList = (params) => {
    return request({
        url: '/work/list',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 12,
            type: params?.type, // 摄影/设计/穿搭
            sort: params?.sort || 'latest' // latest, hottest
        }
    })
}

// 上传作品
export const uploadWork = (data) => {
    const formData = new FormData()
    formData.append('title', data.title)
    formData.append('description', data.description)
    formData.append('type', data.type)
    formData.append('tags', JSON.stringify(data.tags))

    // 上传图片
    data.images.forEach((image, index) => {
        formData.append(`images`, image)
    })

    return request({
        url: '/work/upload',
        method: 'POST',
        data: formData,
        isUpload: true
    })
}

// 获取作品详情
export const getWorkDetail = (id) => {
    return request({
        url: `/work/detail/${id}`,
        method: 'GET'
    })
}

// 删除作品
export const deleteWork = (id) => {
    return request({
        url: `/work/delete/${id}`,
        method: 'DELETE'
    })
}

// 点赞作品
export const likeWork = (id) => {
    return request({
        url: `/work/like/${id}`,
        method: 'POST'
    })
}

// 收藏作品
export const collectWork = (id) => {
    return request({
        url: `/work/collect/${id}`,
        method: 'POST'
    })
}

// 添加评论
export const addComment = (workId, content) => {
    return request({
        url: `/work/comment/${workId}`,
        method: 'POST',
        data: { content }
    })
}

// 获取作品评论
export const getComments = (workId, params) => {
    return request({
        url: `/work/comments/${workId}`,
        method: 'GET',
        params
    })
}