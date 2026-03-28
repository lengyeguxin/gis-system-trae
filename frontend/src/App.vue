<template>
  <div class="app">
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #F5F7FA;
  color: #1D2129;
  min-height: 100vh;
  overflow-x: hidden;
  line-height: 1.5;
}

.app {
  width: 100vw;
  min-height: 100vh;
  overflow: hidden;
  position: relative;
}

/* 企业级商务科技风基础样式 */
/* 色彩变量 */
:root {
  --primary-color: #165DFF;
  --secondary-color-cyan: #0FC6C2;
  --secondary-color-purple: #722ED1;
  --success-color: #00B42A;
  --warning-color: #FF7D00;
  --error-color: #F53F3F;
  --background-light: #F5F7FA;
  --background-white: #FFFFFF;
  --text-primary: #1D2129;
  --text-secondary: #4E5969;
  --border-color: #E5E6EB;
  --shadow-light: 0 2px 8px rgba(0,0,0,0.08);
}

/* 卡片样式 */
.card {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-light);
}

/* 按钮样式 */
.btn {
  border-radius: 4px;
  height: 36px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  cursor: pointer;
  border: none;
  outline: none;
}

.btn-primary {
  background: var(--primary-color);
  color: white;
}

.btn-primary:hover {
  background: #0E47D9;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
}

.btn-secondary {
  background: var(--background-white);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: var(--background-light);
  border-color: var(--primary-color);
}

/* 布局容器 */
.container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 间距工具类 */
.mt-8 { margin-top: 8px; }
.mt-16 { margin-top: 16px; }
.mt-24 { margin-top: 24px; }
.mt-32 { margin-top: 32px; }
.mb-8 { margin-bottom: 8px; }
.mb-16 { margin-bottom: 16px; }
.mb-24 { margin-bottom: 24px; }
.mb-32 { margin-bottom: 32px; }
.ml-8 { margin-left: 8px; }
.ml-16 { margin-left: 16px; }
.mr-8 { margin-right: 8px; }
.mr-16 { margin-right: 16px; }

.p-8 { padding: 8px; }
.p-16 { padding: 16px; }
.p-24 { padding: 24px; }
.p-32 { padding: 32px; }
</style>