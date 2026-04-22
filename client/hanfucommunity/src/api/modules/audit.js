import request from '../request'

// ========== 作品审核 ==========

// 获取待审核作品列表
export const getPendingWorkAuditList = (params) => {
    return request({
        url: '/work/pending-audit',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 12
        }
    })
}

// 审核作品
export const auditWork = (workId, data) => {
    return request({
        url: `/work/audit/${workId}`,
        method: 'POST',
        data: {
            approved: data.approved,
            reason: data.reason || '',
            auditorId: data.auditorId
        }
    })
}

// 获取作品详情（silent=true时不计入浏览量，用于审核模块）
export const getWorkDetail = (id, silent = false) => {
    return request({
        url: `/work/detail/${id}`,
        method: 'GET',
        params: { silent }
    })
}

// ========== 活动审核 ==========

// 获取待审核活动列表
export const getPendingActivityAuditList = (params) => {
    return request({
        url: '/activity/pending-audit',
        method: 'GET',
        params: {
            page: params?.page || 1,
            size: params?.size || 9
        }
    })
}

// 审核活动
export const auditActivity = (activityId, data) => {
    return request({
        url: `/activity/audit/${activityId}`,
        method: 'POST',
        data: {
            approved: data.approved,
            reason: data.reason || '',
            auditorId: data.auditorId
        }
    })
}

// 获取活动详情（silent=true时不计入浏览量，用于审核模块）
export const getActivityDetail = (id, silent = false) => {
    return request({
        url: `/activity/detail/${id}`,
        method: 'GET',
        params: { silent }
    })
}
