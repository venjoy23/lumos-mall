import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { store } from '../store'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('lumos_token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

request.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 200) return body
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg))
    }
    return body
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      store.logout()
      ElMessage.warning('登录已过期，请重新登录')
      if (router.currentRoute.value.path.startsWith('/admin')) {
        router.push('/admin/login')
      } else {
        router.push('/login')
      }
    } else {
      ElMessage.error(error.response?.data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
