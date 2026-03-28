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
        const response = await axios.get('http://localhost:3001/api/gis/police')
        this.policePoints = response.data
      } catch (error) {
        console.error('Failed to get police points:', error)
      }
    },
    // 获取监控点数据
    async getMonitorPoints() {
      try {
        const response = await axios.get('http://localhost:3001/api/gis/monitor')
        this.monitorPoints = response.data
      } catch (error) {
        console.error('Failed to get monitor points:', error)
      }
    },
    // 获取警情信息数据
    async getAlarmPoints() {
      try {
        const response = await axios.get('http://localhost:3001/api/gis/alarm')
        this.alarmPoints = response.data
      } catch (error) {
        console.error('Failed to get alarm points:', error)
      }
    },
    // 获取地址数据
    async getAddressPoints() {
      try {
        const response = await axios.get('http://localhost:3001/api/gis/address')
        this.addressPoints = response.data
      } catch (error) {
        console.error('Failed to get address points:', error)
      }
    }
  }
})
