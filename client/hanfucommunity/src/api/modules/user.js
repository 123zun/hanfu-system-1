import request from '../request'

// 用户登录
export const login = (data) => {
    return request({
        url: '/user/login',
        method: 'POST',
        data
    })
}

// 用户注册
export const register = (data) => {
    return request({
        url: '/user/register',
        method: 'POST',
        data
    })
}

// 获取用户信息
export const getUserInfo = (data) => {
    return request({
        url: '/user/info',
        method: 'POST',
        data
    })
}

// 更新用户信息
export const updateUserInfo = (data) => {
    return request({
        url: '/user/update',
        method: 'POST',
        data
    })
}

// 修改密码
export const changePassword = (data) => {
    return request({
        url: '/user/password',
        method: 'POST',
        data
    })
}

// 退出登录
export const logout = () => {
    return request({
        url: '/user/logout',
        method: 'POST'
    })
}

// 上传头像
export const uploadAvatar = (formData) => {
    return request({
        url: '/user/avatar',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false
    })
}

// ==================== 用户管理（后台） ====================

// 获取用户列表（分页+搜索）
export const getUserList = (params) => {
    return request({
        url: '/user/users',
        method: 'GET',
        params: {
            pageNum: params?.pageNum || 1,
            pageSize: params?.pageSize || 10,
            username: params?.username,
            email: params?.email,
            gender: params?.gender
        }
    })
}

// 删除用户
export const deleteUser = (id) => {
    return request({
        url: `/user/user/${id}`,
        method: 'DELETE'
    })
}

// 添加用户
export const addUser = (data) => {
    return request({
        url: '/user/register',
        method: 'POST',
        data
    })
}

// 更新用户信息（管理员）
export const updateUser = (data) => {
    return request({
        url: '/user/update',
        method: 'POST',
        data
    })
}

// ==================== 关注功能 ====================

// 获取用户主页信息
export const getUserProfile = (userId, currentUserId) => {
    return request({
        url: '/user/profile',
        method: 'GET',
        params: { userId, currentUserId }
    })
}

// 关注用户
export const followUser = (followerId, followingId) => {
    return request({
        url: '/follow/follow',
        method: 'POST',
        data: null,
        params: { followerId, followingId }
    })
}

// 取消关注
export const unfollowUser = (followerId, followingId) => {
    return request({
        url: '/follow/unfollow',
        method: 'POST',
        data: null,
        params: { followerId, followingId }
    })
}

// 检查关注状态
export const checkFollow = (followerId, followingId) => {
    return request({
        url: '/follow/check',
        method: 'GET',
        params: { followerId, followingId }
    })
}

// 获取用户关注/粉丝数
export const getFollowCounts = (userId) => {
    return request({
        url: '/follow/counts',
        method: 'GET',
        params: { userId }
    })
}
