import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const request = axios.create({
  baseURL: '',
  timeout: 30000,
  withCredentials: true // 支持 Cookie
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    // 如果有 sessionId，作为 header 传递（备用方案）
    if (userStore.sessionId) {
      config.headers['X-Session-Id'] = userStore.sessionId
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response
    // 统一返回格式: { code: 0, message: 'ok', data: {} }
    if (data && typeof data.code !== 'undefined') {
      if (data.code === 0) {
        return data.data
      } else if (data.code === 401) {
        // 未登录或会话过期
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
        return Promise.reject(new Error(data.message || '未登录'))
      } else {
        // 业务错误
        ElMessage.error(data.message || '操作失败')
        return Promise.reject(new Error(data.message || '操作失败'))
      }
    }
    // 兼容无 code 格式
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      } else {
        ElMessage.error(data?.message || `请求错误: ${status}`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时')
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default request