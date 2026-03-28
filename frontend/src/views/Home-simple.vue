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
      <!-- 地图区域 -->
      <div class="map-container">
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
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    
    const activeMenu = ref('/home')
    const checkedFeatures = ref(['police', 'monitor', 'alarm'])
    const searchAddress = ref('')
    const amapKey = ref(import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY')
    
    // 退出登录
    const logout = () => {
      userStore.logout()
      router.push('/login')
    }
    
    return {
      activeMenu,
      checkedFeatures,
      searchAddress,
      amapKey,
      logout
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