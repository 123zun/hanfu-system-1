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
export const getUserInfo = () => {
    return request({
        url: '/user/info',
        method: 'GET'
    })
}

// 更新用户信息
export const updateUserInfo = (data) => {
    return request({
        url: '/user/update',
        method: 'PUT',
        data
    })
}

// 修改密码
export const changePassword = (data) => {
    return request({
        url: '/user/change-password',
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
export const uploadAvatar = (userId, file) => {
    const formData = new FormData()
    formData.append('userId', userId)
    formData.append('file', file)

    return request({
        url: '/user/avatar',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false  // 上传时不显示全局loading
    })
}

// 获取默认头像
export const getDefaultAvatar = () => {
    return request({
        url: '/user/avatar/default',
        method: 'GET'
    })
}