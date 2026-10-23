import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import router from '../router'

/**
 * Axios 封装（与管理端同构）：
 * 附加 Token、401 无感刷新、统一错误提示
 */
const request = axios.create({ baseURL: '/api', timeout: 30000 })

request.interceptors.request.use((config) => {
  const store = useUserStore()
  if (store.accessToken) {
    config.headers.Authorization = `Bearer ${store.accessToken}`
  }
  return config
})

let refreshing = false
let pendingQueue = []

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  async (error) => {
    const { response, config } = error
    if (!response) {
      ElMessage.error('网络异常，请稍后再试')
      return Promise.reject(error)
    }
    if (response.status === 401 && !config._retried) {
      const store = useUserStore()
      if (!store.refreshToken) {
        store.clearLogin()
        router.replace('/login')
        return Promise.reject(error)
      }
      if (refreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({ resolve, reject, config })
        })
      }
      refreshing = true
      try {
        const res = await axios.post('/api/auth/refresh', { refreshToken: store.refreshToken })
        const data = res.data.data
        store.setLogin(data)
        pendingQueue.forEach((p) => {
          p.config._retried = true
          p.config.headers.Authorization = `Bearer ${data.accessToken}`
          p.resolve(request(p.config))
        })
        pendingQueue = []
        config._retried = true
        config.headers.Authorization = `Bearer ${data.accessToken}`
        return request(config)
      } catch (e) {
        pendingQueue.forEach((p) => p.reject(e))
        pendingQueue = []
        store.clearLogin()
        ElMessage.warning('登录已过期，请重新登录')
        router.replace('/login')
        return Promise.reject(error)
      } finally {
        refreshing = false
      }
    }
    if (response.status === 429) {
      ElMessage.error('操作过于频繁，请稍后再试')
    } else if (response.data && response.data.message) {
      ElMessage.error(response.data.message)
    }
    return Promise.reject(error)
  }
)

export default request
