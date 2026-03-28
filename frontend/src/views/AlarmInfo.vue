<template>
  <div class="alarm-container">
    <div class="page-header">
      <h2>警情信息</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入警情名称" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button type="primary" @click="searchAlarm"><el-icon><Search /></el-icon> 查询</el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addAlarm">添加警情</el-button>
    </div>
    
    <div class="alarm-table">
      <div class="table-container">
        <el-table :data="alarmList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="alarmNumber" label="警情编号" width="150"></el-table-column>
          <el-table-column prop="name" label="警情名称"></el-table-column>
          <el-table-column prop="alarmLocation" label="警情地点"></el-table-column>
          <el-table-column prop="description" label="案件描述"></el-table-column>
          <el-table-column prop="reporter" label="报案人"></el-table-column>
          <el-table-column prop="reporterPhone" label="报案人联系方式" width="150"></el-table-column>
          <el-table-column prop="latitude" label="纬度" width="120"></el-table-column>
          <el-table-column prop="longitude" label="经度" width="120"></el-table-column>
          <el-table-column prop="level" label="级别" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.level === 'high' ? 'danger' : scope.row.level === 'medium' ? 'warning' : 'success'">
                {{ scope.row.level === 'high' ? '高' : scope.row.level === 'medium' ? '中' : '低' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'pending' ? 'warning' : scope.row.status === 'processing' ? 'info' : 'success'">
                {{ scope.row.status === 'pending' ? '待处理' : scope.row.status === 'processing' ? '处理中' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="scope">
              <el-button size="small" @click="editAlarm(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteAlarm(scope.row.id)">删除</el-button>
              <el-button v-if="scope.row.status === 'pending'" size="small" type="primary" @click="processAlarm(scope.row)">处理</el-button>
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
    
    <!-- 添加/编辑警情对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑警情' : '添加警情'">
      <el-form :model="alarmForm" :rules="rules" ref="alarmFormRef">
        <el-form-item label="警情编号" prop="alarmNumber">
          <el-input v-model="alarmForm.alarmNumber"></el-input>
        </el-form-item>
        <el-form-item label="警情名称" prop="name">
          <el-input v-model="alarmForm.name"></el-input>
        </el-form-item>
        <el-form-item label="警情地点" prop="alarmLocation">
          <el-input v-model="alarmForm.alarmLocation"></el-input>
        </el-form-item>
        <el-form-item label="报案人" prop="reporter">
          <el-input v-model="alarmForm.reporter"></el-input>
        </el-form-item>
        <el-form-item label="报案人联系方式" prop="reporterPhone">
          <el-input v-model="alarmForm.reporterPhone"></el-input>
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model.number="alarmForm.latitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model.number="alarmForm.longitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="alarmForm.level" placeholder="请选择级别">
            <el-option label="高" value="high"></el-option>
            <el-option label="中" value="medium"></el-option>
            <el-option label="低" value="low"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="alarmForm.status" placeholder="请选择状态">
            <el-option label="待处理" value="pending"></el-option>
            <el-option label="处理中" value="processing"></el-option>
            <el-option label="已处理" value="processed"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="alarmForm.description" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAlarm">保存</el-button>
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
  name: 'AlarmInfo',
  setup() {
    const gisStore = useGisStore()
    
    const searchKeyword = ref('')
    const alarmList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    const dialogVisible = ref(false)
    const isEditing = ref(false)
    const alarmFormRef = ref(null)
    
    const alarmForm = reactive({
      id: null,
      alarmNumber: '',
      name: '',
      alarmLocation: '',
      reporter: '',
      reporterPhone: '',
      latitude: 0,
      longitude: 0,
      level: 'medium',
      status: 'pending',
      description: ''
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入警情名称', trigger: 'blur' }
      ],
      latitude: [
        { required: true, message: '请输入纬度', trigger: 'blur' }
      ],
      longitude: [
        { required: true, message: '请输入经度', trigger: 'blur' }
      ],
      level: [
        { required: true, message: '请选择级别', trigger: 'change' }
      ]
    }
    
    // 加载警情数据
    const loadAlarmData = async () => {
      try {
        console.log('开始获取警情数据...')
        const response = await axios.get('http://localhost:3001/api/gis/alarm')
        console.log('警情数据获取成功:', response.data)
        // 转换数据格式
        alarmList.value = response.data.map(item => ({
          id: item.id,
          alarmNumber: item.alarmNumber || '',
          name: item.name,
          alarmLocation: item.alarmLocation || '',
          reporter: item.reporter || '',
          reporterPhone: item.reporterPhone || '',
          latitude: item.latitude,
          longitude: item.longitude,
          level: item.level || 'medium',
          status: item.status || 'pending',
          description: item.description,
          createTime: item.createTime || new Date().toLocaleString('zh-CN')
        }))
        total.value = alarmList.value.length
      } catch (error) {
        console.error('获取警情数据失败:', error)
        // 失败时使用模拟数据
        alarmList.value = [
          { id: 1, alarmNumber: 'A20260327001', name: '交通事故', alarmLocation: '北京市东城区东直门外大街42号门口', reporter: '张三', reporterPhone: '13800138001', latitude: 39.91923, longitude: 116.417428, level: 'high', status: 'pending', description: '两车相撞，有人受伤', createTime: '2026-03-27 10:00:00' },
          { id: 2, alarmNumber: 'A20260327002', name: '纠纷', alarmLocation: '北京市西城区二龙路27号院内', reporter: '李四', reporterPhone: '13800138002', latitude: 39.92923, longitude: 116.407428, level: 'medium', status: 'processing', description: '邻里纠纷，需要调解', createTime: '2026-03-27 10:30:00' },
          { id: 3, alarmNumber: 'A20260327003', name: '噪音投诉', alarmLocation: '北京市朝阳区朝阳公园南路1号附近', reporter: '王五', reporterPhone: '13800138003', latitude: 39.93923, longitude: 116.397428, level: 'low', status: 'processed', description: '工地施工噪音扰民', createTime: '2026-03-27 11:00:00' },
          { id: 4, alarmNumber: 'A20260327004', name: '盗窃', alarmLocation: '北京市海淀区长春桥路17号商场', reporter: '赵六', reporterPhone: '13800138004', latitude: 39.94923, longitude: 116.417428, level: 'high', status: 'pending', description: '手机被偷', createTime: '2026-03-27 11:30:00' },
          { id: 5, alarmNumber: 'A20260327005', name: '斗殴', alarmLocation: '北京市丰台区丰台镇文体路2号酒吧', reporter: '钱七', reporterPhone: '13800138005', latitude: 39.95923, longitude: 116.407428, level: 'medium', status: 'pending', description: '酒后斗殴', createTime: '2026-03-27 12:00:00' }
        ]
        total.value = alarmList.value.length
      }
    }
    
    // 搜索警情
    const searchAlarm = () => {
      // 模拟搜索
      if (searchKeyword.value) {
        const filtered = alarmList.value.filter(item => 
          item.name.includes(searchKeyword.value) || 
          item.description.includes(searchKeyword.value)
        )
        alarmList.value = filtered
        total.value = filtered.length
      } else {
        loadAlarmData()
      }
    }
    
    // 添加警情
    const addAlarm = () => {
      isEditing.value = false
      alarmForm.id = null
      alarmForm.alarmNumber = ''
      alarmForm.name = ''
      alarmForm.alarmLocation = ''
      alarmForm.reporter = ''
      alarmForm.reporterPhone = ''
      alarmForm.latitude = 0
      alarmForm.longitude = 0
      alarmForm.level = 'medium'
      alarmForm.status = 'pending'
      alarmForm.description = ''
      dialogVisible.value = true
    }
    
    // 编辑警情
    const editAlarm = (row) => {
      isEditing.value = true
      alarmForm.id = row.id
      alarmForm.alarmNumber = row.alarmNumber || ''
      alarmForm.name = row.name
      alarmForm.alarmLocation = row.alarmLocation || ''
      alarmForm.reporter = row.reporter || ''
      alarmForm.reporterPhone = row.reporterPhone || ''
      alarmForm.latitude = row.latitude
      alarmForm.longitude = row.longitude
      alarmForm.level = row.level
      alarmForm.status = row.status || 'pending'
      alarmForm.description = row.description
      dialogVisible.value = true
    }
    
    // 保存警情
    const saveAlarm = async () => {
      if (alarmFormRef.value) {
        await alarmFormRef.value.validate((valid) => {
          if (valid) {
            // 模拟保存
            if (isEditing.value) {
              const index = alarmList.value.findIndex(item => item.id === alarmForm.id)
              if (index !== -1) {
                alarmList.value[index] = { ...alarmForm, createTime: alarmList.value[index].createTime }
              }
            } else {
              const newId = Math.max(...alarmList.value.map(item => item.id)) + 1
              const now = new Date().toLocaleString('zh-CN')
              alarmList.value.push({
                ...alarmForm,
                id: newId,
                createTime: now
              })
              total.value++
            }
            dialogVisible.value = false
          }
        })
      }
    }
    
    // 处理警情
    const processAlarm = (row) => {
      const index = alarmList.value.findIndex(item => item.id === row.id)
      if (index !== -1) {
        alarmList.value[index].status = 'processing'
        // 可以添加更多处理逻辑，比如打开处理对话框等
        setTimeout(() => {
          alarmList.value[index].status = 'processed'
        }, 2000)
      }
    }
    
    // 删除警情
    const deleteAlarm = (id) => {
      const index = alarmList.value.findIndex(item => item.id === id)
      if (index !== -1) {
        alarmList.value.splice(index, 1)
        total.value--
      }
    }
    
    // 分页处理
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    onMounted(async () => {
      await loadAlarmData()
    })
    
    return {
      searchKeyword,
      alarmList,
      total,
      currentPage,
      pageSize,
      dialogVisible,
      isEditing,
      alarmForm,
      rules,
      alarmFormRef,
      searchAlarm,
      addAlarm,
      editAlarm,
      saveAlarm,
      deleteAlarm,
      processAlarm,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.alarm-container {
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

.alarm-table {
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
  min-width: 1000px;
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
  .alarm-container {
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
  
  .alarm-table {
    padding: 16px;
  }
  
  :deep(.el-table) {
    min-width: 800px;
  }
}
</style>
