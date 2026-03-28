<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>GIS警务系统</h1>
        <p>科技守护平安</p>
      </div>
      <div class="login-form">
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="login">登录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <div class="login-bg"></div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { User, Lock } from '@element-plus/icons-vue'

export default {
  name: 'Login',
  setup() {
    console.log('Login组件初始化')
    const router = useRouter()
    const userStore = useUserStore()
    const loginFormRef = ref(null)
    
    const loginForm = reactive({
      username: '',
      password: ''
    })
    
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    }
    
    const login = async () => {
      console.log('登录按钮点击')
      if (loginFormRef.value) {
        await loginFormRef.value.validate(async (valid) => {
          if (valid) {
            console.log('表单验证通过，模拟登录成功')
            // 模拟登录成功
            localStorage.setItem('token', 'mock-token')
            console.log('设置token成功，准备跳转到首页')
            router.push('/home')
          }
        })
      }
    }
    
    console.log('Login组件初始化完成')
    return {
      loginForm,
      rules,
      loginFormRef,
      login
    }
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.login-box {
  position: relative;
  z-index: 10;
  width: 400px;
  padding: 40px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.3);
  border: 1px solid rgba(0, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: bold;
  color: #00ffff;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.5);
}

.login-header p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.login-form {
  margin-top: 20px;
}

.el-form-item__label {
  color: rgba(255, 255, 255, 0.8);
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

.el-input__prefix {
  color: rgba(0, 255, 255, 0.7);
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  background: linear-gradient(90deg, #00ffff, #0099cc);
  border: none;
  color: #0f172a;
  font-weight: bold;
  transition: all 0.3s ease;
}

.login-btn:hover {
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.5);
  transform: translateY(-2px);
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPjxkZWZzPjxwYXR0ZXJuIGlkPSJwYXR0ZXJuIiB4PSIwIiB5PSIwIiB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHBhdHRlcm5Vbml0cz0idXNlclNwYWNlT25Vc2UiIHBhdHRlcm5UcmFuc2Zvcm09InJvdGF0ZSgzMCkiPjxwYXRoIGQ9Ik0gNDAgMCBMIDAgMCAwIDQwIiBmaWxsPSJub25lIiBzdHJva2U9IiMwMGZmZmYiIHN0cm9rZS13aWR0aD0iMC41Ii8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI3BhdHRlcm4pIiAvPjwvc3ZnPg==') repeat;
  opacity: 0.1;
  animation: moveBackground 20s linear infinite;
}

@keyframes moveBackground {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(40px, 40px);
  }
}
</style>
