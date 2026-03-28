<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="logo">
        <h1>GIS警务系统</h1>
      </div>
      <div class="nav-menu">
        <el-menu :default-active="activeMenu" class="el-menu-demo" mode="horizontal" router>
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/home/address">GIS图层地址库</el-menu-item>
          <el-menu-item index="/home/police">GIS警务点管理</el-menu-item>
          <el-menu-item index="/home/monitor">GIS监控管理</el-menu-item>
          <el-menu-item index="/home/alarm">警情信息</el-menu-item>
          <el-menu-item index="/home/system">系统管理</el-menu-item>
        </el-menu>
      </div>
      <div class="user-info">
        <el-dropdown>
          <span class="el-dropdown-link">
            管理员 <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人中心</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 子路由内容区域 -->
      <router-view v-if="$route.path !== '/home'"></router-view>
      
      <!-- 地图区域 -->
      <div class="map-container" v-else>
        <div id="map" class="map">
          <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #00ffff; text-align: center; padding: 20px;">
            <h3>地图加载中...</h3>
            <p style="margin-top: 10px; color: rgba(255,255,255,0.7);">请稍候</p>
          </div>
        </div>
        
        <!-- 地图上方的控制面板 -->
        <div class="map-control-overlay">
          <div class="control-section">
            <h3>功能控制</h3>
            <el-checkbox-group v-model="checkedFeatures">
              <el-checkbox value="police">警务点</el-checkbox>
              <el-checkbox value="monitor">监控点</el-checkbox>
              <el-checkbox value="alarm">警情信息</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <div class="control-section">
            <h3>地址搜索</h3>
            <el-input v-model="searchAddress" placeholder="请输入地址">
              <template #append>
                <el-button><i class="el-icon-search"></i></el-button>
              </template>
            </el-input>
          </div>
          
          <!-- 测试用：显示API Key -->
          <div class="control-section">
            <h3>测试信息</h3>
            <p style="font-size: 12px; color: rgba(255,255,255,0.7);">
              API Key: {{ amapKey }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useGisStore } from '../stores/gis'
import AMapLoader from '@amap/amap-jsapi-loader'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const gisStore = useGisStore()
    
    let map = null
    let markers = []
    
    const activeMenu = ref('/home')
    const checkedFeatures = ref(['police', 'monitor', 'alarm'])
    const searchAddress = ref('')
    const amapKey = ref(import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY')
    
    // 退出登录
    const logout = () => {
      userStore.logout()
      router.push('/login')
    }
    
    // 初始化地图
    const initMap = async () => {
      try {
        console.log('开始初始化地图...')
        const amapKey = import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY'
        console.log('AMap API Key:', amapKey)
        
        console.log('开始加载AMap Loader...')
        await AMapLoader.load({
          key: amapKey,
          version: '2.0',
          plugins: ['AMap.Geolocation', 'AMap.AutoComplete', 'AMap.PlaceSearch'],
          securityJsCode: '165d0bc38e2ff6148f6345dbc8b2c376'
        })
        console.log('AMap Loader加载成功')
        
        console.log('开始创建地图实例...')
        map = new AMap.Map('map', {
          center: [116.397428, 39.90923],
          zoom: 13
        })
        console.log('地图实例创建成功')
        
        console.log('添加定位控件...')
        map.addControl(new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 10000,
          buttonPosition: 'RB'
        }))
        console.log('定位控件添加成功')
        
        // 从后端获取数据
        console.log('开始从后端获取数据...')
        await loadDataFromBackend()
        console.log('后端数据获取成功')
        
        console.log('更新地图要素...')
        updateMapFeatures()
        console.log('地图初始化完成')
      } catch (error) {
        console.error('地图初始化失败:', error)
        const mapContainer = document.getElementById('map')
        if (mapContainer) {
          mapContainer.innerHTML = `
            <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #00ffff; text-align: center; padding: 20px;">
              <h3>地图加载失败</h3>
              <p style="margin-top: 10px; color: rgba(255,255,255,0.7);">请配置有效的高德地图 API Key</p>
              <p style="margin-top: 10px; color: rgba(255,255,255,0.5); font-size: 12px;">
                1. 访问 <a href="https://lbs.amap.com/" target="_blank" style="color: #00ffff;">高德开放平台</a> 申请 Key<br>
                2. 在项目根目录创建 .env 文件<br>
                3. 添加 VITE_AMAP_KEY=您的API密钥
              </p>
              <p style="margin-top: 10px; color: rgba(255,255,255,0.5); font-size: 12px;">
                错误信息: ${error.message}
              </p>
            </div>
          `
        }
      }
    }
    
    // 从后端获取数据
    const loadDataFromBackend = async () => {
      try {
        // 获取警务点数据
        await gisStore.getPolicePoints()
        // 获取监控点数据
        await gisStore.getMonitorPoints()
        // 获取警情信息数据
        await gisStore.getAlarmPoints()
      } catch (error) {
        console.error('从后端获取数据失败:', error)
        // 如果后端数据获取失败，使用模拟数据
        loadMockData()
      }
    }
    
    // 加载模拟数据（备用）
    const loadMockData = () => {
      // 模拟警务点数据
      gisStore.policePoints = [
        { id: 1, name: '派出所1', latitude: 39.90923, longitude: 116.397428, description: '北京市公安局东城分局' },
        { id: 2, name: '派出所2', latitude: 39.91923, longitude: 116.407428, description: '北京市公安局西城分局' }
      ]
      
      // 模拟监控点数据
      gisStore.monitorPoints = [
        { id: 1, name: '监控点1', latitude: 39.90923, longitude: 116.417428, description: '路口监控' },
        { id: 2, name: '监控点2', latitude: 39.92923, longitude: 116.397428, description: '小区监控' }
      ]
      
      // 模拟警情信息数据
      gisStore.alarmPoints = [
        { id: 1, name: '警情1', latitude: 39.91923, longitude: 116.417428, description: '交通事故', level: 'high' },
        { id: 2, name: '警情2', latitude: 39.92923, longitude: 116.407428, description: '纠纷', level: 'medium' }
      ]
    }
    
    // 更新地图要素
    const updateMapFeatures = () => {
      if (!map) return
      
      // 清除所有标记
      markers.forEach(marker => map.remove(marker))
      markers = []
      
      // 添加警务点标记
      if (checkedFeatures.value.includes('police')) {
        gisStore.policePoints.forEach(point => {
          const marker = new AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new AMap.Icon({
              size: new AMap.Size(32, 32),
              image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8dmlld0JveD4KICAgIDxnIGZpbGw9IiMxMDRBREMiPgogICAgICA8cGF0aCBkPSJNNS41IDcuNSBDNS41IDcuNSA0IDEwLjUgNCAyMC41IEM0IDI5LjUgNS41IDMyLjUgNS41IDMyLjUgQzE2IDIzLjUgMjYuNSAzMi41IDI2LjUgMzIuNSBDMjguMCAyOS41IDI4IDIwLjUgMjggMjAuNSBDMjggMTEuNSAyNi41IDcuNSAyNi41IDcuNSBDMTYuIDcuNSA1LjUgNy41IDUuNSA3LjV6Ii8+CiAgICA8cGF0aCBkPSJNMTIgMTMuNSBDMTIgMTMuNSAxMCAxNi41IDEwIDIwLjUgQzEwIDI0LjUgMTIgMjcuNSAxMiAyNy41IEMxNiAyMC41IDIwIDI3LjUgMjAgMjcuNSBDMjAgMjQuNSAyMiAxNi41IDIyIDE2LjUgQzIyIDEzLjUgMTYgMTMuNSAxMiAxMy41eiIgZmlsbD0iI2ZmZmZmZiIvPgogICAgICA8cGF0aCBkPSJNMTYgMTMuNSB2NC41IiBmaWxsPSIjZmZmZmZmIi8+CiAgICA8L2c+CiAgPC92aWV3Qm94Pgo8L3N2Zz4='
            })
          })
          marker.on('click', () => {
            new AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;"><h3 style="color: #104BAD; margin-bottom: 10px;">${point.name}</h3><p style="color: #333; margin-bottom: 5px;">${point.description}</p><p style="color: #666; font-size: 12px;">类型: 警务点</p></div>`,
              offset: new AMap.Pixel(0, -30),
              autoMove: true
            }).open(map, marker.getPosition())
          })
          map.add(marker)
          markers.push(marker)
        })
      }
      
      // 添加监控点标记
      if (checkedFeatures.value.includes('monitor')) {
        gisStore.monitorPoints.forEach(point => {
          const marker = new AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new AMap.Icon({
              size: new AMap.Size(32, 32),
              image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB4PSI0IiB5PSI0IiB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHJ4PSIyIiBmaWxsPSIjNDBDQjk1IiBzdHJva2U9IndoaXRlIiBzdHJva2Utd2lkdGg9IjIiLz4KICA8cmVjdCB4PSI4IiB5PSI4IiB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHJ4PSIxIiBmaWxsPSIjMjIyMjIyIi8+CiAgPGNpcmNsZSBjeD0iMTYiIGN5PSIxNiIgcj0iNSIgZmlsbD0iIzQwQ0I5NSIgc3Ryb2tlPSJ3aGl0ZSIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTkgMThoMTQiIHN0cm9rZT0iI2ZmZmZmZiIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTIwIDE4SDEyIiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4='
            })
          })
          marker.on('click', () => {
            new AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;"><h3 style="color: #40CB95; margin-bottom: 10px;">${point.name}</h3><p style="color: #333; margin-bottom: 5px;">${point.description}</p><p style="color: #666; font-size: 12px;">类型: 监控点</p></div>`,
              offset: new AMap.Pixel(0, -30),
              autoMove: true
            }).open(map, marker.getPosition())
          })
          map.add(marker)
          markers.push(marker)
        })
      }
      
      // 添加警情信息标记
      if (checkedFeatures.value.includes('alarm')) {
        gisStore.alarmPoints.forEach(point => {
          // 处理警情级别，默认为 medium
          const level = point.level || point.type || 'medium'
          const isHighLevel = level === 'high' || level === '1' || level === 1
          
          const marker = new AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new AMap.Icon({
              size: new AMap.Size(32, 32),
              image: isHighLevel ? 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSIxMCIgZmlsbD0iI0ZGRjAwMCIgc3Ryb2tlPSJ3aGl0ZSIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTkgMTBoMTQiIHN0cm9rZT0iI2ZmZmZmZiIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTE2IDZoLTJ2LTRoNHY0aC0yek0xNiAyNmwtNS01djJoMTB2LTJ6IiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4=' : 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSIxMCIgZmlsbD0iI0ZGRmQ1MCIgc3Ryb2tlPSJ3aGl0ZSIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTkgMTBoMTQiIHN0cm9rZT0iI2ZmZmZmZiIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTE2IDZoLTJ2LTRoNHY0aC0yek0xNiAyNmwtNS01djJoMTB2LTJ6IiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4='
            })
          })
          marker.on('click', () => {
            const levelText = isHighLevel ? '高' : level === 'low' || level === '0' || level === 0 ? '低' : '中'
            const levelColor = isHighLevel ? '#FF0000' : level === 'low' || level === '0' || level === 0 ? '#00FF00' : '#FFD500'
            new AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;"><h3 style="color: ${levelColor}; margin-bottom: 10px;">${point.name}</h3><p style="color: #333; margin-bottom: 5px;">${point.description}</p><p style="color: ${levelColor}; font-size: 12px;">级别: ${levelText}</p><p style="color: #666; font-size: 12px;">类型: 警情信息</p></div>`,
              offset: new AMap.Pixel(0, -30),
              autoMove: true
            }).open(map, marker.getPosition())
          })
          map.add(marker)
          markers.push(marker)
        })
      }
    }
    
    // 搜索地址
    const searchAddressFn = () => {
      if (!map || !searchAddress.value) return
      
      const placeSearch = new AMap.PlaceSearch({
        map: map,
        pageSize: 1,
        pageIndex: 1,
        extensions: 'base'
      })
      
      placeSearch.search(searchAddress.value, function(status, result) {
        if (status === 'complete' && result.info === 'OK') {
          const pois = result.pois
          if (pois.length > 0) {
            const poi = pois[0]
            map.setCenter(poi.location)
            map.setZoom(15)
            
            new AMap.InfoWindow({
              content: `<div><h3>${poi.name}</h3><p>${poi.address}</p></div>`,
              offset: new AMap.Pixel(0, -30)
            }).open(map, poi.location)
          }
        }
      })
    }
    
    onMounted(() => {
      try {
        initMap()
      } catch (error) {
        console.error('初始化地图时发生错误:', error)
      }
    })
    
    onUnmounted(() => {
      if (map) {
        map.destroy()
      }
    })
    
    return {
      activeMenu,
      checkedFeatures,
      searchAddress,
      amapKey,
      logout,
      updateMapFeatures,
      searchAddressFn
    }
  }
}
</script>

<style scoped>
.home-container {
  width: 100vw;
  height: 100vh;
  background: #0f172a;
  color: #ffffff;
  display: flex;
  flex-direction: column;
}

.top-nav {
  height: 60px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.1);
}

.logo h1 {
  font-size: 20px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
  margin: 0;
}

.nav-menu {
  flex: 1;
  margin: 0 40px;
}

.el-menu {
  background: transparent;
  border: none;
}

.el-menu-item {
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

.el-menu-item:hover {
  color: #00ffff;
  background: rgba(0, 255, 255, 0.1);
}

.el-menu-item.is-active {
  color: #00ffff;
  border-bottom-color: #00ffff;
  background: rgba(0, 255, 255, 0.1);
}

.user-info {
  color: rgba(255, 255, 255, 0.8);
}

.el-dropdown-link {
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
}

.el-dropdown-menu {
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.1);
}

.el-dropdown-item {
  color: rgba(255, 255, 255, 0.8);
}

.el-dropdown-item:hover {
  background: rgba(0, 255, 255, 0.1);
  color: #00ffff;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.map-control-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  width: 300px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.2);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.1);
  z-index: 1000;
}

.control-section {
  margin-bottom: 30px;
}

.control-section h3 {
  font-size: 16px;
  font-weight: bold;
  color: #00ffff;
  margin-bottom: 15px;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
}

.el-checkbox {
  display: block;
  margin-bottom: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #00ffff;
  border-color: #00ffff;
}

.el-checkbox__input.is-checked + .el-checkbox__label {
  color: #00ffff;
}

.el-input {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 6px;
}

.el-input__inner {
  color: #ffffff;
  background: transparent;
}

.el-input__append .el-button {
  background: linear-gradient(90deg, #00ffff, #0099cc);
  border: none;
  color: #0f172a;
  font-weight: bold;
}

.map-container {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 500px;
}

.map {
  width: 100%;
  height: 100%;
}
</style>