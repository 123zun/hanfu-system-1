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
