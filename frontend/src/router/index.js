import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import GisAddress from '../views/GisAddress.vue'
import GisPolice from '../views/GisPolice.vue'
import GisMonitor from '../views/GisMonitor.vue'
import AlarmInfo from '../views/AlarmInfo.vue'
import SystemManagement from '../views/SystemManagement.vue'
import Test from '../views/Test.vue'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/test',
    name: 'Test',
    component: Test
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    children: [
      {
        path: 'address',
        name: 'GisAddress',
        component: GisAddress
      },
      {
        path: 'police',
        name: 'GisPolice',
        component: GisPolice
      },
      {
        path: 'monitor',
        name: 'GisMonitor',
        component: GisMonitor
      },
      {
        path: 'alarm',
        name: 'AlarmInfo',
        component: AlarmInfo
      },
      {
        path: 'system',
        name: 'SystemManagement',
        component: SystemManagement
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫（暂时禁用）
/*
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  console.log('路由守卫检查:', { to: to.path, token: !!token })
  if (to.path !== '/login' && !token) {
    console.log('重定向到登录页')
    next('/login')
  } else {
    console.log('允许访问')
    next()
  }
})
*/

export default router
