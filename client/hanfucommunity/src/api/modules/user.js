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
        method: 'Post',
        data
    })
}

// 更新用户信息
export const updateUserInfo = (data) => {
    return request({
        url: '/user/update',
        method: 'Post',
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

// src/api/modules/user.js
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
