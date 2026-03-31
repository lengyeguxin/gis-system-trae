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
        <el-table :data="monitorList" style="width: 100%" stripe border>
          <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>
          <el-table-column prop="name" label="监控点名称" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="cameraNo" label="编码" width="120" show-overflow-tooltip></el-table-column>
          <el-table-column prop="cameraType" label="类型" width="80" align="center"></el-table-column>
          <el-table-column prop="location" label="地址" min-width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="ip" label="IP地址" width="130"></el-table-column>
          <el-table-column prop="rtspUrl" label="RTSP地址" min-width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="department" label="责任单位" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column label="坐标" min-width="160">
            <template #default="scope">
              <span class="coord-text">{{ parseFloat(scope.row.longitude?.toFixed(6) || 0) }}, {{ parseFloat(scope.row.latitude?.toFixed(6) || 0) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 'online' ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" link @click="editMonitor(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" link @click="deleteMonitor(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination">
        <el-pagination
          layout="total, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
          :pager-count="5"
        />
      </div>
    </div>
    
    <!-- 添加/编辑监控点对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑监控点' : '添加监控点'">
      <el-form :model="monitorForm" :rules="rules" ref="monitorFormRef">
        <el-form-item label="监控点名称" prop="name">
          <el-input v-model="monitorForm.name"></el-input>
        </el-form-item>
        <el-form-item label="摄像头编码" prop="cameraNo">
          <el-input v-model="monitorForm.cameraNo"></el-input>
        </el-form-item>
        <el-form-item label="摄像头类型" prop="cameraType">
          <el-select v-model="monitorForm.cameraType" placeholder="请选择摄像头类型">
            <el-option label="球机" value="球机"></el-option>
            <el-option label="枪机" value="枪机"></el-option>
          </el-select>
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
        <el-form-item label="RTSP" prop="rtspUrl">
          <el-input v-model="monitorForm.rtspUrl"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model.number="monitorForm.longitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model.number="monitorForm.latitude" type="number" step="0.000001"></el-input>
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
      cameraNo: '',
      cameraType: '球机',
      location: '',
      ip: '',
      department: '',
      rtspUrl: '',
      latitude: 0,
      longitude: 0,
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
        const response = await axios.get('/api/camera')
        console.log('监控点数据获取成功:', response.data)
        // 转换数据格式
        monitorList.value = response.data.map(item => ({
          id: item.id,
          name: item.name,
          cameraNo: item.camera_no || '',
          cameraType: item.camera_type || '',
          location: item.address || '',
          ip: item.ip_address || '',
          department: item.responsibility_unit || '',
          rtspUrl: item.rtsp_url || '',
          latitude: item.lat,
          longitude: item.lon,
          status: item.online_status ? 'online' : 'offline'
        }))
        total.value = monitorList.value.length
        console.log('监控点数据加载完成，共', total.value, '条记录')
      } catch (error) {
        console.error('获取监控点数据失败:', error)
        // 失败时显示空数据
        monitorList.value = []
        total.value = 0
        console.log('监控点数据加载失败，显示空列表')
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
      monitorForm.cameraNo = ''
      monitorForm.cameraType = '球机'
      monitorForm.location = ''
      monitorForm.ip = ''
      monitorForm.department = ''
      monitorForm.rtspUrl = ''
      monitorForm.latitude = 0
      monitorForm.longitude = 0
      monitorForm.status = 'online'
      dialogVisible.value = true
    }
    
    // 编辑监控点
    const editMonitor = (row) => {
      isEditing.value = true
      monitorForm.id = row.id
      monitorForm.name = row.name
      monitorForm.cameraNo = row.cameraNo || ''
      monitorForm.cameraType = row.cameraType || '球机'
      monitorForm.location = row.location || ''
      monitorForm.ip = row.ip || ''
      monitorForm.department = row.department || ''
      monitorForm.rtspUrl = row.rtspUrl || ''
      monitorForm.latitude = row.latitude
      monitorForm.longitude = row.longitude
      monitorForm.status = row.status
      dialogVisible.value = true
    }
    
    // 保存监控点
    const saveMonitor = async () => {
      if (monitorFormRef.value) {
        try {
          // 验证表单
          const valid = await monitorFormRef.value.validate()
          if (valid) {
            // 准备请求数据
            const cameraData = {
              name: monitorForm.name,
              camera_no: monitorForm.cameraNo,
              camera_type: monitorForm.cameraType,
              address: monitorForm.location,
              ip_address: monitorForm.ip,
              rtsp_url: monitorForm.rtspUrl,
              lat: monitorForm.latitude,
              lon: monitorForm.longitude,
              online_status: monitorForm.status === 'online',
              responsibility_unit: monitorForm.department
            }
            
            if (isEditing.value) {
              // 更新监控点
              await axios.put(`/api/camera/${monitorForm.id}`, cameraData)
            } else {
              // 添加监控点
              await axios.post('/api/camera', cameraData)
            }
            
            // 重新加载数据
            await loadMonitorData()
            dialogVisible.value = false
          }
        } catch (error) {
          console.error('保存监控点失败:', error)
          if (error !== false) { // 验证失败时error为false，不需要弹窗
            alert('保存失败，请重试')
          }
        }
      }
    }
    
    // 删除监控点
    const deleteMonitor = async (id) => {
      try {
        await axios.delete(`/api/camera/${id}`)
        // 重新加载数据
        await loadMonitorData()
      } catch (error) {
        console.error('删除监控点失败:', error)
        alert('删除失败，请重试')
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

.coord-text {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  color: #666;
}

:deep(.el-table--border) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th.el-table__cell .cell) {
  white-space: nowrap;
  font-size: 13px;
}

:deep(.el-table th.el-table__cell) {
  background: #f5f7fa !important;
  font-weight: 600;
  color: #303133;
  padding: 8px 0;
}

:deep(.el-table td.el-table__cell) {
  padding: 12px 0;
}

:deep(.el-button--primary.is-link) {
  color: #165DFF;
}

:deep(.el-button--danger.is-link) {
  color: #F53F3F;
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
