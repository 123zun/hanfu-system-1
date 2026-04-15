import request from '../request'

// 获取社区统计数据
export const getDashboardStats = () => {
    return request({
        url: '/dashboard/stats',
        method: 'GET'
    })
}
