<template>
  <div class="test-container">
    <h1>测试页面</h1>
    <p>这是一个测试页面，用于验证前端应用是否能正常显示。</p>
  </div>
</template>

<script>
export default {
  name: 'Test'
}
</script>

<style scoped>
.test-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #0f172a;
  color: #ffffff;
}

h1 {
  font-size: 32px;
  color: #00ffff;
  margin-bottom: 20px;
}

p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
}
</style>