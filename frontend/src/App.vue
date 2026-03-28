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
  font-family: Arial, sans-serif;
  background: #0f172a;
  color: #ffffff;
}

.app {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>