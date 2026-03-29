import { defineStore } from 'pinia'
import axios from 'axios'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || '',
    isLogin: !!localStorage.getItem('token')
  }),
  actions: {
    async login(username, password) {
      try {
        // 调用后端的登录接口
        const response = await axios.post('http://localhost:3001/api/login', {
          username,
          password
        })
        
        const { token, user } = response.data
        localStorage.setItem('token', token)
        this.token = token
        this.userInfo = user
        this.isLogin = true
        return true
      } catch (error) {
        console.error('Login failed:', error)
        // 处理后端返回的错误信息
        if (error.response && error.response.data && error.response.data.message) {
          console.error('Login error message:', error.response.data.message)
        }
        return false
      }
    },
    logout() {
      localStorage.removeItem('token')
      this.token = ''
      this.userInfo = null
      this.isLogin = false
    }
  }
})
