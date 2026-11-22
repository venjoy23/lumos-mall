import { reactive, computed } from 'vue'

function load(key) {
  try { return JSON.parse(localStorage.getItem(key)) } catch { return null }
}

export const store = reactive({
  token: localStorage.getItem('lumos_token') || '',
  user: load('lumos_user'),
  role: localStorage.getItem('lumos_role') || '',

  get isLogin() { return !!this.token },

  get isAdmin() { return this.role === 'ADMIN' },

  login(token, user, role) {
    this.token = token
    this.user = user
    this.role = role
    localStorage.setItem('lumos_token', token)
    localStorage.setItem('lumos_user', JSON.stringify(user))
    localStorage.setItem('lumos_role', role)
  },

  logout() {
    this.token = ''
    this.user = null
    this.role = ''
    localStorage.removeItem('lumos_token')
    localStorage.removeItem('lumos_user')
    localStorage.removeItem('lumos_role')
  },

  refreshUser(user) {
    this.user = user
    localStorage.setItem('lumos_user', JSON.stringify(user))
  }
})

export function fullImage(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url
}
