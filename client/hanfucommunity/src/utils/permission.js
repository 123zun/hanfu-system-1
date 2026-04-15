// 全局权限工具

const ROLE_KEY = 'hanfu_role'
const USER_KEY = 'hanfu_user'

// 判断是否为管理员
export const isAdmin = () => {
  try {
    const role = localStorage.getItem(ROLE_KEY)
    if (role) return role === 'ADMIN'
    const userStr = localStorage.getItem(USER_KEY)
    if (userStr) {
      const user = JSON.parse(userStr)
      return user.role === 'ADMIN'
    }
  } catch (e) {
    console.error('检查权限失败', e)
  }
  return false
}

// 获取当前用户ID
export const getCurrentUserId = () => {
  try {
    const userStr = localStorage.getItem(USER_KEY)
    if (userStr) {
      const user = JSON.parse(userStr)
      return user.id
    }
  } catch (e) {
    console.error('获取用户ID失败', e)
  }
  return null
}

// 获取当前用户信息
export const getCurrentUser = () => {
  try {
    const userStr = localStorage.getItem(USER_KEY)
    if (userStr) {
      return JSON.parse(userStr)
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
  return null
}

// 保存用户信息和角色
export const setUserInfo = (user) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  if (user.role) {
    localStorage.setItem(ROLE_KEY, user.role)
  }
}

// 清除用户信息
export const clearUserInfo = () => {
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(ROLE_KEY)
}
