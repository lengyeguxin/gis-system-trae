<template>
  <div class="system-container">
    <div class="page-header">
      <h2>系统管理</h2>
    </div>
    
    <div class="tab-section">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="user">
          <div class="user-management">
            <div class="search-section">
              <el-input v-model="userSearchKeyword" placeholder="请输入用户名" style="width: 300px; margin-right: 10px">
                <template #append>
                  <el-button type="primary" @click="searchUser"><el-icon><Search /></el-icon> 查询</el-button>
                </template>
              </el-input>
              <el-button type="primary" @click="addUser">添加用户</el-button>
            </div>
            
            <div class="user-table">
              <div class="table-container">
                <el-table :data="userList" style="width: 100%">
                  <el-table-column prop="id" label="ID" width="80"></el-table-column>
                  <el-table-column prop="username" label="用户名"></el-table-column>
                  <el-table-column prop="realName" label="姓名"></el-table-column>
                  <el-table-column prop="role" label="角色" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'info'">
                        {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.createTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="200">
                    <template #default="scope">
                      <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
                      <el-button size="small" type="danger" @click="deleteUser(scope.row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div class="pagination">
                <el-pagination
                  layout="prev, pager, next"
                  :total="userTotal"
                  :page-size="pageSize"
                  :current-page="currentPage"
                  @current-change="handlePageChange"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="日志管理" name="log">
          <div class="log-management">
            <div class="search-section">
              <el-input v-model="logSearchKeyword" placeholder="请输入日志关键词" style="width: 300px; margin-right: 10px">
                <template #append>
                  <el-button type="primary" @click="searchLog"><el-icon><Search /></el-icon> 查询</el-button>
                </template>
              </el-input>
            </div>
            
            <div class="log-table">
              <div class="table-container">
                <el-table :data="logList" style="width: 100%">
                  <el-table-column prop="id" label="ID" width="80"></el-table-column>
                  <el-table-column prop="username" label="操作人"></el-table-column>
                  <el-table-column prop="operation" label="操作"></el-table-column>
                  <el-table-column prop="details" label="描述"></el-table-column>
                  <el-table-column prop="ipAddress" label="IP地址" width="140"></el-table-column>
                  <el-table-column prop="createTime" label="操作时间" width="180">
                    <template #default="scope">
                      {{ formatDate(scope.row.createTime) }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              <div class="pagination">
                <el-pagination
                  layout="prev, pager, next"
                  :total="logTotal"
                  :page-size="pageSize"
                  :current-page="currentPage"
                  @current-change="handlePageChange"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="userDialogVisible" :title="isEditingUser ? '编辑用户' : '添加用户'">
      <el-form :model="userForm" :rules="userRules" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEditingUser">
          <el-input v-model="userForm.password" type="password"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="普通用户" value="viewer"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'SystemManagement',
  setup() {
    const activeTab = ref('user')
    
    // 用户管理相关
    const userSearchKeyword = ref('')
    const userList = ref([])
    const userTotal = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    const userDialogVisible = ref(false)
    const isEditingUser = ref(false)
    const userFormRef = ref(null)
    
    const userForm = reactive({
      id: null,
      username: '',
      realName: '',
      password: '',
      role: 'user'
    })
    
    const userRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入姓名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ]
    }
    
    // 日志管理相关
    const logSearchKeyword = ref('')
    const logList = ref([])
    const logTotal = ref(0)
    
    // 加载用户数据
    const loadUserData = async () => {
      try {
        const response = await axios.get('http://localhost:3001/api/users')
        userList.value = response.data
        userTotal.value = response.data.length
      } catch (error) {
        console.error('加载用户数据失败:', error)
      }
    }
    
    // 加载日志数据
    const loadLogData = async () => {
      try {
        const response = await axios.get('http://localhost:3001/api/logs')
        logList.value = response.data
        logTotal.value = response.data.length
      } catch (error) {
        console.error('加载日志数据失败:', error)
      }
    }
    
    // 搜索用户
    const searchUser = () => {
      // 这里可以实现后端搜索，暂时使用前端过滤
      if (userSearchKeyword.value) {
        const filtered = userList.value.filter(item => 
          item.username.includes(userSearchKeyword.value)
        )
        userList.value = filtered
        userTotal.value = filtered.length
      } else {
        loadUserData()
      }
    }
    
    // 搜索日志
    const searchLog = () => {
      if (logSearchKeyword.value) {
        const filtered = logList.value.filter(item => 
          item.username?.includes(logSearchKeyword.value) || 
          item.operation?.includes(logSearchKeyword.value) || 
          item.details?.includes(logSearchKeyword.value)
        )
        logList.value = filtered
        logTotal.value = filtered.length
      } else {
        loadLogData()
      }
    }
    
    // 添加用户
    const addUser = () => {
      isEditingUser.value = false
      userForm.id = null
      userForm.username = ''
      userForm.realName = ''
      userForm.password = ''
      userForm.role = 'viewer'
      userDialogVisible.value = true
    }
    
    // 编辑用户
    const editUser = (row) => {
      isEditingUser.value = true
      userForm.id = row.id
      userForm.username = row.username
      userForm.realName = row.realName
      userForm.role = row.role
      userDialogVisible.value = true
    }
    
    // 保存用户
    const saveUser = async () => {
      if (userFormRef.value) {
        await userFormRef.value.validate(async (valid) => {
          if (valid) {
            try {
              if (isEditingUser.value) {
                // 更新用户
                await axios.put(`http://localhost:3001/api/users/${userForm.id}`, userForm)
              } else {
                // 创建用户
                await axios.post('http://localhost:3001/api/users', userForm)
              }
              // 重新加载数据
              await loadUserData()
              userDialogVisible.value = false
            } catch (error) {
              console.error('保存用户失败:', error)
            }
          }
        })
      }
    }
    
    // 删除用户
    const deleteUser = async (id) => {
      try {
        await axios.delete(`http://localhost:3001/api/users/${id}`)
        // 重新加载数据
        await loadUserData()
      } catch (error) {
        console.error('删除用户失败:', error)
      }
    }
    
    // 分页处理
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }
    
    onMounted(() => {
      loadUserData()
      loadLogData()
    })
    
    return {
      activeTab,
      userSearchKeyword,
      userList,
      userTotal,
      currentPage,
      pageSize,
      userDialogVisible,
      isEditingUser,
      userForm,
      userRules,
      userFormRef,
      logSearchKeyword,
      logList,
      logTotal,
      searchUser,
      searchLog,
      addUser,
      editUser,
      saveUser,
      deleteUser,
      handlePageChange,
      formatDate
    }
  }
}
</script>

<style scoped>
.system-container {
  padding: 24px;
  background: var(--background-light);
  min-height: 100vh;
  color: var(--text-primary);
  width: 100%;
  box-sizing: border-box;
  margin-top: 70px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.tab-section {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-light);
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

:deep(.el-tabs) {
  background: var(--background-white);
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
}

:deep(.el-tabs__item) {
  color: var(--text-secondary);
  font-size: 14px;
  padding: 10px 20px;
  font-weight: 500;
}

:deep(.el-tabs__item:hover) {
  color: var(--primary-color);
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  border-bottom: 2px solid var(--primary-color);
  font-weight: 600;
}

.search-section {
  display: flex;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
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

.user-table,
.log-table {
  margin-top: 20px;
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.table-container {
  width: 100%;
  overflow-x: auto;
  margin-bottom: 20px;
}

:deep(.el-table) {
  background: var(--background-white) !important;
  min-width: 800px;
  width: 100%;
  border-radius: 4px;
}

:deep(.el-table th) {
  background: #F2F3F5 !important;
  color: var(--text-primary) !important;
  border-bottom: 1px solid var(--border-color) !important;
  font-weight: 600;
  padding: 12px 0;
}

:deep(.el-table td) {
  color: var(--text-secondary) !important;
  border-bottom: 1px solid var(--border-color) !important;
  background: var(--background-white) !important;
  padding: 12px 0;
}

:deep(.el-table tr:hover) {
  background: #F5F7FA !important;
}

:deep(.el-table__row) {
  background: var(--background-white) !important;
}

:deep(.el-table__body) {
  background: var(--background-white) !important;
}

/* 斑马纹效果 */
:deep(.el-table__row:nth-child(even)) {
  background: #F9FAFC !important;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
}

:deep(.el-pagination__item) {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  border-radius: 4px;
}

:deep(.el-pagination__item:hover) {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

:deep(.el-pagination__item.active) {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: white;
}

:deep(.el-dialog) {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-light);
  border-radius: 8px;
}

:deep(.el-dialog__title) {
  color: var(--text-primary);
  font-weight: 600;
  font-size: 16px;
}

:deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-select .el-input) {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  border-radius: 4px;
}

:deep(.el-select .el-input__inner) {
  color: var(--text-primary);
  background: var(--background-white);
  height: 36px;
  line-height: 36px;
}

:deep(.el-select-dropdown) {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-light);
  border-radius: 4px;
}

:deep(.el-select-dropdown__item) {
  color: var(--text-secondary);
  font-size: 14px;
}

:deep(.el-select-dropdown__item:hover) {
  background: var(--background-light);
  color: var(--primary-color);
}

:deep(.el-button) {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  font-weight: 500;
  border-radius: 4px;
  transition: all 0.2s ease;
  height: 36px;
  padding: 0 16px;
}

:deep(.el-button:hover) {
  background: var(--background-light);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

:deep(.el-button--danger) {
  background: var(--error-color);
  border: 1px solid var(--error-color);
  color: white;
}

:deep(.el-button--danger:hover) {
  background: #E53838;
  box-shadow: 0 2px 8px rgba(245, 63, 63, 0.3);
}

:deep(.el-button--primary) {
  background: var(--primary-color);
  border: 1px solid var(--primary-color);
  color: white;
}

:deep(.el-button--primary:hover) {
  background: #0E47D9;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .system-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .search-section {
    flex-direction: column;
    align-items: flex-start;
  }
  
  :deep(.el-input) {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .tab-section {
    padding: 16px;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
}
</style>