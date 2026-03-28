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
        // 这里应该调用后端的登录接口
        // 现在使用模拟数据
        const response = await axios.post('http://localhost:3000/api/login', {
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
