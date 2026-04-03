// src/api/modules/upload.js
import request from '../request'

// 上传图片（通用）
export const uploadImage = (formData) => {
    return request({
        url: '/upload/image',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false
    })
}

// 上传文章封面图
export const uploadCover = (formData) => {
    return request({
        url: '/article/upload-cover',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false
    })
}

// 上传文章内容图片
export const uploadContentImage = (formData) => {
    return request({
        url: '/article/upload-image',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        showLoading: false
    })
}