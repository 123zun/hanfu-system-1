import request from '../request'

// 获取活动列表（分页）
export function getActivityList(params) {
    return request({
        url: '/activity/list',
        method: 'get',
        params
    })
}

// 获取活动详情
export function getActivityDetail(id, params) {
    return request({
        url: `/activity/detail/${id}`,
        method: 'get',
        params
    })
}

// 创建活动
export function createActivity(data) {
    return request({
        url: '/activity/create',
        method: 'post',
        data
    })
}

// 更新活动
export function updateActivity(data) {
    return request({
        url: '/activity/update',
        method: 'put',
        data
    })
}

// 删除活动
export function deleteActivity(id) {
    return request({
        url: `/activity/delete/${id}`,
        method: 'delete'
    })
}

// 上传封面图
export function uploadCover(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/activity/upload-cover',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 报名活动
export function registerActivity(id, userId) {
    return request({
        url: `/activity/register/${id}`,
        method: 'post',
        params: { userId }
    })
}

// 取消报名
export function cancelRegistration(id, userId) {
    return request({
        url: `/activity/cancel/${id}`,
        method: 'post',
        params: { userId }
    })
}

// 获取参与人员列表
export function getParticipants(activityId) {
    return request({
        url: `/activity/participants/${activityId}`,
        method: 'get'
    })
}

// 新增参与人员
export function addParticipants(activityId, userIds) {
    return request({
        url: '/activity/participants/add',
        method: 'post',
        params: { activityId, userIds }
    })
}

// 删除参与人员
export function removeParticipants(activityId, signupIds) {
    return request({
        url: '/activity/participants/remove',
        method: 'delete',
        params: { activityId, signupIds }
    })
}
