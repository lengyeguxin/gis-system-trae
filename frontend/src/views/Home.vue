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
        <div class="current-time">
          {{ currentTime }}
        </div>
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
            <el-checkbox-group v-model="checkedFeatures" @change="updateMapFeatures">
              <el-checkbox value="police">警务点</el-checkbox>
              <el-checkbox value="monitor">监控点</el-checkbox>
              <el-checkbox value="alarm">警情信息</el-checkbox>
              <el-checkbox value="address">地址库</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <div class="control-section">
            <h3>地址搜索</h3>
            <el-input v-model="searchAddress" placeholder="请输入地址">
              <template #append>
                <el-button @click="searchAddressFn" type="primary"><i class="el-icon-search"></i> 查询</el-button>
              </template>
            </el-input>
          </div>
          

          

        </div>
      </div>
      
      <!-- 警情处理对话框 -->
      <el-dialog
        v-model="processDialogVisible"
        title="处理警情"
        width="500px"
        :close-on-click-modal="false"
      >
        <div v-if="currentAlarm">
          <p><strong>警情名称：</strong>{{ currentAlarm.name }}</p>
          <p><strong>警情描述：</strong>{{ currentAlarm.description }}</p>
          <el-form style="margin-top: 20px;">
            <el-form-item label="处理结果">
              <el-input
                v-model="processResult"
                type="textarea"
                :rows="4"
                placeholder="请输入处理结果"
              />
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="processDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="processAlarm">提交</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useGisStore } from '../stores/gis'
import axios from 'axios'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const userStore = useUserStore()
    const gisStore = useGisStore()
    
    let map = null
    let markers = []
    let measureTool = null
    let mouseTool = null
    
    // 初始化时根据当前路由设置激活的菜单
    const activeMenu = ref(route.path === '/home' ? '/home' : route.path)
    const checkedFeatures = ref(['police', 'monitor', 'alarm'])
    const searchAddress = ref('')
    const amapKey = ref(import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY')
    const currentTime = ref('')
    let timeInterval = null
    
    // 警情处理相关
    const currentAlarm = ref(null)
    const processDialogVisible = ref(false)
    const processResult = ref('')
    
    // 退出登录
    const logout = () => {
      userStore.logout()
      router.push('/login')
    }
    
    // 更新当前时间
    const updateCurrentTime = () => {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      currentTime.value = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    
    // 初始化地图
    const initMap = async () => {
      try {
        console.log('开始初始化地图...')
        
        // 等待DOM更新完成
        await nextTick()
        
        // 检查地图容器是否存在
        const mapContainer = document.getElementById('map')
        if (!mapContainer) {
          throw new Error('地图容器不存在')
        }
        console.log('地图容器存在')
        
        // 检查AMap是否已加载
        if (!window.AMap) {
          // 动态加载高德地图API
          console.log('AMap未加载，开始动态加载...')
          await new Promise((resolve, reject) => {
            const script = document.createElement('script')
            script.type = 'text/javascript'
            script.src = 'https://webapi.amap.com/maps?v=2.0&key=060cfe198e52a78e1d8f43f51ef6d7d7&plugin=AMap.Geolocation,AMap.AutoComplete,AMap.PlaceSearch,AMap.Scale,AMap.ToolBar,AMap.RangingTool,AMap.MouseTool&securityJsCode=165d0bc38e2ff6148f6345dbc8b2c376'
            script.onload = () => {
              console.log('AMap加载成功')
              resolve()
            }
            script.onerror = (error) => {
              console.error('AMap加载失败:', error)
              reject(new Error('AMap加载失败'))
            }
            document.head.appendChild(script)
          })
        } else {
          console.log('AMap已加载')
        }
        
        // 等待AMap完全初始化
        await new Promise((resolve) => {
          if (window.AMap) {
            resolve()
          } else {
            const interval = setInterval(() => {
              if (window.AMap) {
                clearInterval(interval)
                resolve()
              }
            }, 100)
          }
        })
        
        console.log('开始创建地图实例...')
        map = new window.AMap.Map('map', {
          center: [116.397428, 39.90923],
          zoom: 13
        })
        console.log('地图实例创建成功')
        
        console.log('添加定位控件...')
        map.addControl(new window.AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 10000,
          buttonPosition: 'RB'
        }))
        console.log('定位控件添加成功')
        
        console.log('添加缩放控件...')
        map.addControl(new window.AMap.Scale({
          position: 'RB'
        }))
        console.log('缩放控件添加成功')
        
        console.log('添加比例尺控件...')
        map.addControl(new window.AMap.ToolBar({
          position: 'RB'
        }))
        console.log('比例尺控件添加成功')
        
        console.log('添加测距工具...')
        // 高德地图 2.0 使用 RangingTool 替代 MeasureTool
        if (window.AMap.RangingTool) {
          measureTool = new window.AMap.RangingTool(map)
          console.log('测距工具添加成功')
        } else {
          console.warn('测距工具插件未加载，将禁用测距功能')
        }
        
        console.log('添加鼠标工具（用于标记）...')
        if (window.AMap.MouseTool) {
          mouseTool = new window.AMap.MouseTool(map)
          // 监听标记添加事件，将用户添加的标记保存到markers数组
          mouseTool.on('draw', function(e) {
            if (e.obj && e.obj instanceof window.AMap.Marker) {
              markers.push(e.obj)
              console.log('用户添加了一个标记，已保存到markers数组')
            }
          })
          console.log('鼠标工具添加成功')
        } else {
          console.warn('鼠标工具插件未加载，将禁用标记功能')
        }
        
        // 添加地图右键事件监听，用于取消测距和标记模式
        map.on('rightclick', function() {
          // 取消测距模式
          if (measureTool) {
            measureTool.turnOff()
            console.log('鼠标右键点击，已取消测距模式')
          }
          // 取消标记模式
          if (mouseTool) {
            mouseTool.close()
            console.log('鼠标右键点击，已取消标记模式')
          }
        })
        
        // 创建自定义地图工具控件
        console.log('添加自定义地图工具控件...')
        try {
          // 创建一个DOM元素
          const div = document.createElement('div');
          div.className = 'amap-custom-control';
          div.style.cssText = `
            position: absolute;
            bottom: 80px;
            right: 20px;
            background: white;
            border-radius: 4px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.15);
            padding: 8px;
            z-index: 10;
            display: flex;
            flex-direction: column;
            gap: 8px;
          `;
          
          // 添加开始测距按钮
          const measureStartBtn = document.createElement('button');
          measureStartBtn.className = 'amap-custom-btn';
          measureStartBtn.style.cssText = `
            padding: 8px 12px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            background: white;
            cursor: pointer;
            font-size: 12px;
            transition: all 0.3s;
          `;
          measureStartBtn.textContent = '开始测距';
          measureStartBtn.onclick = startMeasure;
          div.appendChild(measureStartBtn);
          
          // 添加开始标记按钮
          const markerStartBtn = document.createElement('button');
          markerStartBtn.className = 'amap-custom-btn';
          markerStartBtn.style.cssText = `
            padding: 8px 12px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            background: white;
            cursor: pointer;
            font-size: 12px;
            transition: all 0.3s;
          `;
          markerStartBtn.textContent = '开始标记';
          markerStartBtn.onclick = startDrawMarker;
          div.appendChild(markerStartBtn);
          
          // 添加清空标记按钮
          const clearMarkersBtn = document.createElement('button');
          clearMarkersBtn.className = 'amap-custom-btn';
          clearMarkersBtn.style.cssText = `
            padding: 8px 12px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            background: white;
            cursor: pointer;
            font-size: 12px;
            transition: all 0.3s;
          `;
          clearMarkersBtn.textContent = '清空标记';
          clearMarkersBtn.onclick = clearMarkers;
          div.appendChild(clearMarkersBtn);
          
          // 鼠标悬停效果
          const btns = div.querySelectorAll('.amap-custom-btn');
          btns.forEach(btn => {
            btn.onmouseenter = function() {
              this.style.background = '#f0f0f0';
            };
            btn.onmouseleave = function() {
              this.style.background = 'white';
            };
          });
          
          // 将DOM元素添加到地图上
          map.getContainer().appendChild(div);
          console.log('自定义地图工具控件添加成功')
        } catch (error) {
          console.error('添加自定义地图工具控件失败:', error)
        }
        
        // 从后端获取数据
        console.log('开始从后端获取数据...')
        await loadDataFromBackend()
        console.log('后端数据获取成功')
        
        console.log('更新地图要素...')
        updateMapFeatures()
        console.log('地图初始化完成')
      } catch (error) {
        console.error('地图初始化失败:', error)
        console.error('错误堆栈:', error.stack)
        const mapContainer = document.getElementById('map')
        if (mapContainer) {
          mapContainer.innerHTML = `
            <div style="display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; color: #00ffff; text-align: center; padding: 20px;">
              <h3>地图加载失败</h3>
              <p style="margin-top: 10px; color: rgba(255,255,255,0.7);">请检查网络连接和API Key配置</p>
              <p style="margin-top: 10px; color: rgba(255,255,255,0.5); font-size: 12px;">
                1. 确认网络连接正常<br>
                2. 检查API Key是否正确<br>
                3. 确认API Key未过期
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
        // 获取地址数据
        await gisStore.getAddressPoints()
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
      
      // 模拟地址数据
      gisStore.addressPoints = [
        { id: 1, name: '地址1', latitude: 39.90923, longitude: 116.387428, address: '北京市东城区某某街道1号' },
        { id: 2, name: '地址2', latitude: 39.92923, longitude: 116.427428, address: '北京市西城区某某街道2号' }
      ]
    }
    
    // 更新地图要素
    const updateMapFeatures = () => {
      if (!map || !window.AMap) return
      
      // 清除所有标记
      markers.forEach(marker => map.remove(marker))
      markers = []
      
      // 添加警务点标记
      if (checkedFeatures.value.includes('police')) {
        gisStore.policePoints.forEach(point => {
          const marker = new window.AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new window.AMap.Icon({
              size: new window.AMap.Size(32, 32),
              image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8dmlld0JveD4KICAgIDxnIGZpbGw9IiMxMDRBREMiPgogICAgICA8cGF0aCBkPSJNNS41IDcuNSBDNS41IDcuNSA0IDEwLjUgNCAyMC41IEM0IDI5LjUgNS41IDMyLjUgNS41IDMyLjUgQzE2IDIzLjUgMjYuNSAzMi41IDI2LjUgMzIuNSBDMjguMCAyOS41IDI4IDIwLjUgMjggMjAuNSBDMjggMTEuNSAyNi41IDcuNSAyNi41IDcuNSBDMTYuIDcuNSA1LjUgNy41IDUuNSA3LjV6Ii8+CiAgICA8cGF0aCBkPSJNMTIgMTMuNSBDMTIgMTMuNSAxMCAxNi41IDEwIDIwLjUgQzEwIDI0LjUgMTIgMjcuNSAxMiAyNy41IEMxNiAyMC41IDIwIDI3LjUgMjAgMjcuNSBDMjAgMjQuNSAyMiAxNi41IDIyIDE2LjUgQzIyIDEzLjUgMTYgMTMuNSAxMiAxMy41eiIgZmlsbD0iI2ZmZmZmZiIvPgogICAgICA8cGF0aCBkPSJNMTYgMTMuNSB2NC41IiBmaWxsPSIjZmZmZmZmIi8+CiAgICA8L2c+CiAgPC92aWV3Qm94Pgo8L3N2Zz4='
            })
          })
          marker.on('click', () => {
            new window.AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;"><h3 style="color: #104BAD; margin-bottom: 10px;">${point.name}</h3><p style="color: #333; margin-bottom: 5px;">${point.description}</p><p style="color: #666; font-size: 12px;">类型: 警务点</p></div>`,
              offset: new window.AMap.Pixel(0, -30),
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
          const marker = new window.AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new window.AMap.Icon({
              size: new window.AMap.Size(32, 32),
              image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB4PSI0IiB5PSI0IiB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHJ4PSIyIiBmaWxsPSIjNDBDQjk1IiBzdHJva2U9IndoaXRlIiBzdHJva2Utd2lkdGg9IjIiLz4KICA8cmVjdCB4PSI4IiB5PSI4IiB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHJ4PSIxIiBmaWxsPSIjMjIyMjIyIi8+CiAgPGNpcmNsZSBjeD0iMTYiIGN5PSIxNiIgcj0iNSIgZmlsbD0iIzQwQ0I5NSIgc3Ryb2tlPSJ3aGl0ZSIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTkgMThoMTQiIHN0cm9rZT0iI2ZmZmZmZiIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTIwIDE4SDEyIiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4='
            })
          })
          marker.on('click', () => {
            new window.AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;"><h3 style="color: #40CB95; margin-bottom: 10px;">${point.name}</h3><p style="color: #333; margin-bottom: 5px;">${point.description}</p><p style="color: #666; font-size: 12px;">类型: 监控点</p></div>`,
              offset: new window.AMap.Pixel(0, -30),
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
          // 警情级别：1-高 2-中 3-低
          const level = point.level || 2
          const levelText = level === 1 ? '高' : level === 3 ? '低' : '中'
          const levelColor = level === 1 ? '#FF0000' : level === 3 ? '#00FF00' : '#FFD500'
          // 图标颜色：高-红色，中-黄色，低-绿色
          const iconColor = level === 1 ? 'FF0000' : level === 3 ? '00FF00' : 'FFD500'
          
          const marker = new window.AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new window.AMap.Icon({
              size: new window.AMap.Size(32, 32),
              image: `data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSIxMCIgZmlsbD0iIyR7aWNvbkNvbG9yfSIgc3Ryb2tlPSJ3aGl0ZSIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTkgMTBoMTQiIHN0cm9rZT0iI2ZmZmZmZiIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTE2IDZoLTJ2LTRoNHY0aC0yek0xNiAyNmwtNS01djJoMTB2LTJ6IiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4=`
            })
          })
          marker.on('click', () => {
            const infoWindow = new window.AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;">
                <h3 style="color: ${levelColor}; margin-bottom: 10px;">${point.name}</h3>
                <p style="color: #333; margin-bottom: 5px;">${point.description}</p>
                <p style="color: #666; font-size: 12px; margin-bottom: 5px;">案件描述: ${point.caseDescription || '无'}</p>
                <p style="color: ${levelColor}; font-size: 12px;">级别: ${levelText}</p>
                <p style="color: #666; font-size: 12px;">类型: ${point.alarmType || '未知'}</p>
                <button id="process-alarm-btn-${point.id}" style="margin-top: 10px; padding: 5px 10px; background: #165DFF; color: white; border: none; border-radius: 4px; cursor: pointer;">处理</button>
              </div>`,
              offset: new window.AMap.Pixel(0, -30),
              autoMove: true
            })
            infoWindow.open(map, marker.getPosition())
            
            // 延迟绑定处理按钮点击事件
            setTimeout(() => {
              const processBtn = document.getElementById(`process-alarm-btn-${point.id}`)
              if (processBtn) {
                processBtn.onclick = () => {
                  currentAlarm.value = point
                  processDialogVisible.value = true
                  infoWindow.close()
                }
              }
            }, 100)
          })
          map.add(marker)
          markers.push(marker)
        })
      }
      
      // 添加地址库标记
      if (checkedFeatures.value.includes('address')) {
        gisStore.addressPoints?.forEach(point => {
          const marker = new window.AMap.Marker({
            position: [point.longitude, point.latitude],
            title: point.name,
            icon: new window.AMap.Icon({
              size: new window.AMap.Size(32, 32),
              image: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSIxMCIgZmlsbD0iIzk5OTk5OSIgc3Ryb2tlPSJ3aGl0ZSIgc3Ryb2tlLXdpZHRoPSIyIi8+CiAgPHBhdGggZD0iTTE2IDZ2MjAiIHN0cm9rZT0id2hpdGUiIHN0cm9rZS13aWR0aD0iMiIvPgo8L3N2Zz4='
            })
          })
          marker.on('click', () => {
            new window.AMap.InfoWindow({
              content: `<div style="padding: 10px; min-width: 200px;"><h3 style="color: #999; margin-bottom: 10px;">${point.name}</h3><p style="color: #333; margin-bottom: 5px;">${point.description || point.address || ''}</p><p style="color: #666; font-size: 12px;">类型: 地址库</p></div>`,
              offset: new window.AMap.Pixel(0, -30),
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
      if (!map || !searchAddress.value || !window.AMap) return
      
      const placeSearch = new window.AMap.PlaceSearch({
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
            
            new window.AMap.InfoWindow({
              content: `<div><h3>${poi.name}</h3><p>${poi.address}</p></div>`,
              offset: new window.AMap.Pixel(0, -30)
            }).open(map, poi.location)
          }
        }
      })
    }
    
    // 开始测距
    const startMeasure = () => {
      if (measureTool) {
        measureTool.turnOn()
      } else {
        console.warn('测距工具未初始化，无法使用测距功能')
      }
    }
    
    // 停止测距
    const stopMeasure = () => {
      if (measureTool) {
        measureTool.turnOff()
      }
    }
    
    // 开始标记
    const startDrawMarker = () => {
      if (mouseTool) {
        mouseTool.marker()
      } else {
        console.warn('标记工具未初始化，无法使用标记功能')
      }
    }
    
    // 停止标记
    const stopDrawMarker = () => {
      if (mouseTool) {
        mouseTool.close()
      }
    }
    
    // 清空标记
    const clearMarkers = () => {
      if (map) {
        // 停止所有工具
        if (measureTool) {
          measureTool.turnOff()
        }
        if (mouseTool) {
          mouseTool.close()
        }
        
        // 清除用户添加的标记（保留系统默认标记）
        // 先保存系统默认标记
        const systemMarkers = []
        markers.forEach(marker => {
          // 系统默认标记有 title 属性，用户添加的标记没有
          if (marker.getTitle()) {
            systemMarkers.push(marker)
          } else {
            map.remove(marker)
          }
        })
        // 更新标记数组，只保留系统默认标记
        markers = systemMarkers
        
        console.log('用户添加的标记已清空，系统默认标记已保留')
      }
    }
    
    // 处理警情
    const processAlarm = async () => {
      if (!currentAlarm.value || !processResult.value.trim()) {
        return
      }
      
      try {
        await axios.put(`/api/alarm/${currentAlarm.value.id}`, {
          handling_result: processResult.value,
          status: 1
        })
        
        // 从本地数据中移除已处理的警情
        const index = gisStore.alarmPoints.findIndex(a => a.id === currentAlarm.value.id)
        if (index !== -1) {
          gisStore.alarmPoints.splice(index, 1)
        }
        
        processDialogVisible.value = false
        processResult.value = ''
        currentAlarm.value = null
        
        // 刷新地图标记
        updateMapFeatures()
        
        console.log('警情处理成功')
      } catch (error) {
        console.error('处理警情失败:', error)
      }
    }
    
    onMounted(() => {
      try {
        // 初始化时间
        updateCurrentTime()
        // 启动定时器，每秒更新一次时间
        timeInterval = setInterval(updateCurrentTime, 1000)
        // 初始化地图
        initMap()
      } catch (error) {
        console.error('初始化地图时发生错误:', error)
      }
    })
    
    onUnmounted(() => {
      if (map) {
        map.destroy()
      }
      // 清除定时器
      if (timeInterval) {
        clearInterval(timeInterval)
      }
    })
    
    // 监听路由变化，当切换到首页时重新初始化地图，并更新菜单选中状态
    watch(() => route.path, (newPath) => {
      // 更新菜单选中状态
      activeMenu.value = newPath === '/home' ? '/home' : newPath
      
      if (newPath === '/home') {
        console.log('路由切换到首页，重新初始化地图...')
        // 清除旧的地图实例
        if (map) {
          map.destroy()
          map = null
        }
        // 重新初始化地图
        initMap()
      }
    })
    
    return {
      activeMenu,
      checkedFeatures,
      searchAddress,
      amapKey,
      currentTime,
      logout,
      updateMapFeatures,
      searchAddressFn,
      startMeasure,
      startDrawMarker,
      clearMarkers,
      currentAlarm,
      processDialogVisible,
      processResult,
      processAlarm
    }
  }
}
</script>

<style scoped>
.home-container {
  width: 100vw;
  height: 100vh;
  background: linear-gradient(135deg, #165DFF 0%, #0FC6C2 100%);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 警务风格背景图案 */
.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxwYXR0ZXJuIGlkPSJwYXR0ZXJuIiB4PSIwIiB5PSIwIiB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHBhdHRlcm5Vbml0cz0idXNlclNwYWNlT25Vc2UiIHBhdHRlcm5UcmFuc2Zvcm09InJvdGF0ZSgzMCkiPjxwYXRoIGQ9Ik0gNDAgMCBMIDAgMCAwIDQwIiBmaWxsPSJub25lIiBzdHJva2U9IiNmZmZmZmYiIHN0cm9rZS13aWR0aD0iMC41Ii8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI3BhdHRlcm4pIiAvPjwvc3ZnPg==') repeat;
  opacity: 0.1;
  pointer-events: none;
  z-index: 0;
}

.top-nav {
  height: 70px;
  background: var(--background-white);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: var(--shadow-light);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.logo h1 {
  font-size: 20px;
  font-weight: 600;
  color: var(--primary-color);
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

:deep(.el-menu-item) {
  color: var(--text-primary) !important;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
  border-radius: 4px;
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
  height: 36px;
  line-height: 36px;
}

:deep(.el-menu-item:hover) {
  color: var(--primary-color) !important;
  background: rgba(22, 93, 255, 0.05) !important;
}

:deep(.el-menu-item.is-active) {
  color: var(--primary-color) !important;
  border-bottom-color: var(--primary-color);
  background: rgba(22, 93, 255, 0.05) !important;
  font-weight: 600;
}

.user-info {
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 24px;
}

.current-time {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
}

.el-dropdown-link {
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-size: 14px;
}

.el-dropdown-link:hover {
  background: var(--background-light);
  color: var(--primary-color);
}

.el-dropdown-menu {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-light);
  border-radius: 8px;
}

.el-dropdown-item {
  color: var(--text-secondary);
  border-radius: 4px;
  margin: 2px 8px;
  font-size: 14px;
}

.el-dropdown-item:hover {
  background: var(--background-light);
  color: var(--primary-color);
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  margin-top: 70px;
}

.map-control-overlay {
  position: absolute;
  top: 90px;
  left: 32px;
  width: 320px;
  background: var(--background-white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-light);
  z-index: 1000;
}

.control-section {
  margin-bottom: 24px;
}

.control-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.el-checkbox {
  display: block;
  margin-bottom: 12px;
  color: var(--text-secondary);
  font-size: 14px;
}

.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.el-checkbox__input.is-checked + .el-checkbox__label {
  color: var(--primary-color);
}

:deep(.el-input) {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

:deep(.el-input__inner) {
  color: var(--text-primary);
  background: var(--background-white);
  height: 36px;
  line-height: 36px;
}

:deep(.el-input__append .el-button) {
  background: var(--primary-color);
  border: 1px solid var(--primary-color);
  color: white;
  font-weight: 500;
  border-radius: 0 4px 4px 0;
  transition: all 0.2s ease;
  height: 36px;
  padding: 0 16px;
}

:deep(.el-input__append .el-button:hover) {
  background: #0E47D9;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
}

.map-container {
  flex: 1;
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 500px;
  overflow: hidden;
}

.map {
  width: 100%;
  height: 100%;
  min-height: 500px;
  position: relative;
  z-index: 1;
}
</style>