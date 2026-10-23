import { defineStore } from 'pinia'

/**
 * 管理端登录态：与后端共用登录接口，校验 ADMIN 角色
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem('admin_accessToken') || '',
    refreshToken: localStorage.getItem('admin_refreshToken') || '',
    user: JSON.parse(localStorage.getItem('admin_user') || 'null')
  }),
  getters: {
    isLoggedIn: (state) => !!state.accessToken,
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },
  actions: {
    setLogin(data) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem('admin_accessToken', data.accessToken)
      localStorage.setItem('admin_refreshToken', data.refreshToken)
      localStorage.setItem('admin_user', JSON.stringify(data.user))
    },
    clearLogin() {
      this.accessToken = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem('admin_accessToken')
      localStorage.removeItem('admin_refreshToken')
      localStorage.removeItem('admin_user')
    }
  }
})
