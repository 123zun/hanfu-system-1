import request from '../request'

// 用户登录
export const login = (data) => {
    return request({
        url: '/auth/login',
        method: 'POST',
        data
    })
}

// 用户注册
export const register = (data) => {
    return request({
        url: '/auth/register',
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

// 上传头像
export const uploadAvatar = (file) => {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/user/upload-avatar',
        method: 'POST',
        data: formData,
        isUpload: true, // 标记为上传文件
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 退出登录
export const logout = () => {
    return request({
        url: '/auth/logout',
        method: 'POST'
    })
}