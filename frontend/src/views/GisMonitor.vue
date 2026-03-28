<template>
  <div class="monitor-container">
    <div class="page-header">
      <h2>GIS监控管理</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入监控点名称" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button type="primary" @click="searchMonitor"><el-icon><Search /></el-icon> 查询</el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addMonitor">添加监控点</el-button>
    </div>
    
    <div class="monitor-table">
      <div class="table-container">
        <el-table :data="monitorList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="name" label="监控点名称"></el-table-column>
          <el-table-column prop="location" label="监控点地点"></el-table-column>
          <el-table-column prop="department" label="责任单位"></el-table-column>
          <el-table-column prop="ip" label="监控点IP" width="150"></el-table-column>
          <el-table-column prop="latitude" label="纬度" width="120"></el-table-column>
          <el-table-column prop="longitude" label="经度" width="120"></el-table-column>
          <el-table-column prop="type" label="监控类型" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.type === 'road' ? 'info' : 'success'">
                {{ scope.row.type === 'road' ? '道路' : '小区' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'">
                {{ scope.row.status === 'online' ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="editMonitor(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteMonitor(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination">
        <el-pagination
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
    
    <!-- 添加/编辑监控点对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑监控点' : '添加监控点'">
      <el-form :model="monitorForm" :rules="rules" ref="monitorFormRef">
        <el-form-item label="监控点名称" prop="name">
          <el-input v-model="monitorForm.name"></el-input>
        </el-form-item>
        <el-form-item label="监控点地点" prop="location">
          <el-input v-model="monitorForm.location"></el-input>
        </el-form-item>
        <el-form-item label="责任单位" prop="department">
          <el-input v-model="monitorForm.department"></el-input>
        </el-form-item>
        <el-form-item label="监控点IP" prop="ip">
          <el-input v-model="monitorForm.ip"></el-input>
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model.number="monitorForm.latitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model.number="monitorForm.longitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="监控类型" prop="type">
          <el-select v-model="monitorForm.type" placeholder="请选择监控类型">
            <el-option label="道路" value="road"></el-option>
            <el-option label="小区" value="community"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="monitorForm.status" placeholder="请选择状态">
            <el-option label="在线" value="online"></el-option>
            <el-option label="离线" value="offline"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMonitor">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useGisStore } from '../stores/gis'
import axios from 'axios'

export default {
  name: 'GisMonitor',
  setup() {
    const gisStore = useGisStore()
    
    const searchKeyword = ref('')
    const monitorList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    const dialogVisible = ref(false)
    const isEditing = ref(false)
    const monitorFormRef = ref(null)
    
    const monitorForm = reactive({
      id: null,
      name: '',
      location: '',
      ip: '',
      department: '',
      latitude: 0,
      longitude: 0,
      type: 'road',
      status: 'online'
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入监控点名称', trigger: 'blur' }
      ],
      latitude: [
        { required: true, message: '请输入纬度', trigger: 'blur' }
      ],
      longitude: [
        { required: true, message: '请输入经度', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择监控类型', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }
    
    // 加载监控点数据
    const loadMonitorData = async () => {
      try {
        console.log('开始获取监控点数据...')
        const response = await axios.get('http://localhost:3001/api/gis/monitor')
        console.log('监控点数据获取成功:', response.data)
        // 转换数据格式
        monitorList.value = response.data.map(item => ({
          id: item.id,
          name: item.name,
          location: item.location || '',
          ip: item.ip || '',
          department: item.department || '',
          latitude: item.latitude,
          longitude: item.longitude,
          type: item.type || 'road',
          status: item.status || 'online'
        }))
        total.value = monitorList.value.length
      } catch (error) {
        console.error('获取监控点数据失败:', error)
        // 失败时使用模拟数据
        monitorList.value = [
          { id: 1, name: '东直门外大街监控点', location: '北京市东城区东直门外大街与香河园路交叉口', ip: '192.168.1.101', department: '东城分局', latitude: 39.90923, longitude: 116.417428, type: 'road', status: 'online' },
          { id: 2, name: '西长安街监控点', location: '北京市西城区西长安街与西单北大街交叉口', ip: '192.168.1.102', department: '西城分局', latitude: 39.92923, longitude: 116.397428, type: 'community', status: 'online' },
          { id: 3, name: '朝阳公园监控点', location: '北京市朝阳区朝阳公园路与亮马桥路交叉口', ip: '192.168.1.103', department: '朝阳分局', latitude: 39.91923, longitude: 116.427428, type: 'road', status: 'offline' },
          { id: 4, name: '中关村大街监控点', location: '北京市海淀区中关村大街与海淀黄庄路交叉口', ip: '192.168.1.104', department: '海淀分局', latitude: 39.93923, longitude: 116.407428, type: 'community', status: 'online' },
          { id: 5, name: '丰台镇监控点', location: '北京市丰台区丰台镇正阳大街与丰台路交叉口', ip: '192.168.1.105', department: '丰台分局', latitude: 39.94923, longitude: 116.387428, type: 'road', status: 'online' }
        ]
        total.value = monitorList.value.length
      }
    }
    
    // 搜索监控点
    const searchMonitor = () => {
      // 模拟搜索
      if (searchKeyword.value) {
        const filtered = monitorList.value.filter(item => 
          item.name.includes(searchKeyword.value)
        )
        monitorList.value = filtered
        total.value = filtered.length
      } else {
        loadMonitorData()
      }
    }
    
    // 添加监控点
    const addMonitor = () => {
      isEditing.value = false
      monitorForm.id = null
      monitorForm.name = ''
      monitorForm.location = ''
      monitorForm.ip = ''
      monitorForm.department = ''
      monitorForm.latitude = 0
      monitorForm.longitude = 0
      monitorForm.type = 'road'
      monitorForm.status = 'online'
      dialogVisible.value = true
    }
    
    // 编辑监控点
    const editMonitor = (row) => {
      isEditing.value = true
      monitorForm.id = row.id
      monitorForm.name = row.name
      monitorForm.location = row.location || ''
      monitorForm.ip = row.ip || ''
      monitorForm.department = row.department || ''
      monitorForm.latitude = row.latitude
      monitorForm.longitude = row.longitude
      monitorForm.type = row.type
      monitorForm.status = row.status
      dialogVisible.value = true
    }
    
    // 保存监控点
    const saveMonitor = async () => {
      if (monitorFormRef.value) {
        await monitorFormRef.value.validate((valid) => {
          if (valid) {
            // 模拟保存
            if (isEditing.value) {
              const index = monitorList.value.findIndex(item => item.id === monitorForm.id)
              if (index !== -1) {
                monitorList.value[index] = { ...monitorForm }
              }
            } else {
              const newId = Math.max(...monitorList.value.map(item => item.id)) + 1
              monitorList.value.push({
                ...monitorForm,
                id: newId
              })
              total.value++
            }
            dialogVisible.value = false
          }
        })
      }
    }
    
    // 删除监控点
    const deleteMonitor = (id) => {
      const index = monitorList.value.findIndex(item => item.id === id)
      if (index !== -1) {
        monitorList.value.splice(index, 1)
        total.value--
      }
    }
    
    // 分页处理
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    onMounted(async () => {
      await loadMonitorData()
    })
    
    return {
      searchKeyword,
      monitorList,
      total,
      currentPage,
      pageSize,
      dialogVisible,
      isEditing,
      monitorForm,
      rules,
      monitorFormRef,
      searchMonitor,
      addMonitor,
      editMonitor,
      saveMonitor,
      deleteMonitor,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.monitor-container {
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

.monitor-table {
  background: var(--background-white);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: var(--shadow-light);
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
  padding: 12px 16px;
}

:deep(.el-table td) {
  color: var(--text-secondary) !important;
  border-bottom: 1px solid var(--border-color) !important;
  background: var(--background-white) !important;
  padding: 12px 16px;
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
  .monitor-container {
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
  
  .monitor-table {
    padding: 16px;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
}
</style>
