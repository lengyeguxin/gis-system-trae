import { defineStore } from 'pinia'
import axios from 'axios'

export const useGisStore = defineStore('gis', {
  state: () => ({
    policePoints: [],
    monitorPoints: [],
    alarmPoints: [],
    addressPoints: []
  }),
  actions: {
    // 获取警务点数据
    async getPolicePoints() {
      try {
        console.log('开始获取警务点数据...')
        const response = await axios.get('http://localhost:3001/api/gis/police')
        console.log('警务点数据获取成功:', response.data)
        this.policePoints = response.data
      } catch (error) {
        console.error('Failed to get police points:', error)
        throw error
      }
    },
    // 获取监控点数据
    async getMonitorPoints() {
      try {
        console.log('开始获取监控点数据...')
        const response = await axios.get('http://localhost:3001/api/gis/monitor')
        console.log('监控点数据获取成功:', response.data)
        this.monitorPoints = response.data
      } catch (error) {
        console.error('Failed to get monitor points:', error)
        throw error
      }
    },
    // 获取警情信息数据
    async getAlarmPoints() {
      try {
        console.log('开始获取警情信息数据...')
        const response = await axios.get('http://localhost:3001/api/gis/alarm')
        console.log('警情信息数据获取成功:', response.data)
        this.alarmPoints = response.data
      } catch (error) {
        console.error('Failed to get alarm points:', error)
        throw error
      }
    },
    // 获取地址数据
    async getAddressPoints() {
      try {
        console.log('开始获取地址数据...')
        const response = await axios.get('http://localhost:3001/api/gis/address')
        console.log('地址数据获取成功:', response.data)
        this.addressPoints = response.data
      } catch (error) {
        console.error('Failed to get address points:', error)
        throw error
      }
    }
  }
})
