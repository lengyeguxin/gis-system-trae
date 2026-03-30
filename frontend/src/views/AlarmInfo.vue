<template>
  <div class="alarm-container">
    <div class="page-header">
      <h2>警情信息</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入关键词" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button type="primary" @click="searchAlarm"><el-icon><Search /></el-icon> 查询</el-button>
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态过滤" style="width: 150px; margin-right: 10px">
        <el-option label="全部" value=""></el-option>
        <el-option label="未处理" :value="0"></el-option>
        <el-option label="已处理" :value="1"></el-option>
      </el-select>
      <el-button type="primary" @click="addAlarm">添加警情</el-button>
      <el-button type="success" @click="downloadTemplate"><el-icon><Download /></el-icon> 下载模板</el-button>
      <el-upload
        ref="uploadRef"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :http-request="handleImport"
        accept=".xls,.xlsx"
        style="display: inline-block; margin-left: 10px"
      >
        <el-button type="warning"><el-icon><Upload /></el-icon> 批量导入</el-button>
      </el-upload>
    </div>
    
    <div class="alarm-table">
      <div class="table-container">
        <el-table :data="alarmList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="alarm_id" label="警情编号" width="150"></el-table-column>
          <el-table-column prop="alarm_location" label="警情地点"></el-table-column>
          <el-table-column prop="case_description" label="案件描述"></el-table-column>
          <el-table-column prop="alarm_phone" label="报案人联系方式" width="150"></el-table-column>
          <el-table-column prop="alarm_type" label="警情类型" width="120"></el-table-column>
          <el-table-column prop="alarm_level" label="级别" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.alarm_level === 1 ? 'danger' : scope.row.alarm_level === 2 ? 'warning' : 'success'">
                {{ scope.row.alarm_level === 1 ? '高' : scope.row.alarm_level === 3 ? '低' : '中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'">
                {{ scope.row.status === 0 ? '未处理' : '已处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="handling_result" label="处理结果" width="200"></el-table-column>
          <el-table-column label="报警时间" width="200">
            <template #default="scope">
              {{ formatTime(scope.row.alarm_time) }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="200">
            <template #default="scope">
              {{ formatTime(scope.row.create_time) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="scope">
              <el-button size="small" @click="editAlarm(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteAlarm(scope.row.id)">删除</el-button>
              <el-button v-if="scope.row.status === 0" size="small" type="primary" @click="processAlarm(scope.row)">处理</el-button>
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
        <el-form-item label="警情编号" prop="alarm_id">
          <el-input v-model="alarmForm.alarm_id"></el-input>
        </el-form-item>
        <el-form-item label="警情地点" prop="alarm_location">
          <el-input v-model="alarmForm.alarm_location"></el-input>
        </el-form-item>
        <el-form-item label="报警时间" prop="alarm_time">
          <el-date-picker
            v-model="alarmForm.alarm_time"
            type="datetime"
            placeholder="选择报警时间"
            style="width: 250px"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="报案人联系方式" prop="alarm_phone">
          <el-input v-model="alarmForm.alarm_phone"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="lon">
          <el-input v-model.number="alarmForm.lon" type="number" step="0.000001" placeholder="如: 116.407428"></el-input>
        </el-form-item>
        <el-form-item label="纬度" prop="lat">
          <el-input v-model.number="alarmForm.lat" type="number" step="0.000001" placeholder="如: 39.90923"></el-input>
        </el-form-item>
        <el-form-item label="警情类型" prop="alarm_type">
          <el-input v-model="alarmForm.alarm_type"></el-input>
        </el-form-item>
        <el-form-item label="级别" prop="alarm_level">
          <el-select v-model="alarmForm.alarm_level" placeholder="请选择级别">
            <el-option label="高" :value="1"></el-option>
            <el-option label="中" :value="2"></el-option>
            <el-option label="低" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="alarmForm.status" placeholder="请选择状态">
            <el-option label="未处理" :value="0"></el-option>
            <el-option label="已处理" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="案件描述" prop="case_description">
          <el-input v-model="alarmForm.case_description" type="textarea"></el-input>
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAlarm">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 处理警情对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理警情">
      <el-form :model="processForm" :rules="processRules" ref="processFormRef">
        <el-form-item label="处理结果" prop="handling_result">
          <el-input v-model="processForm.handling_result" type="textarea" rows="4"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess">提交处理</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useGisStore } from '../stores/gis'
import { Download, Upload, Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'AlarmInfo',
  components: { Download, Upload, Search },
  setup() {
    const gisStore = useGisStore()
    
    const searchKeyword = ref('')
    const statusFilter = ref('')
    const alarmList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    const dialogVisible = ref(false)
    const processDialogVisible = ref(false)
    const isEditing = ref(false)
    const alarmFormRef = ref(null)
    const processFormRef = ref(null)
    const currentAlarm = ref(null)
    const uploadRef = ref(null)
    
    const alarmForm = reactive({
      id: null,
      alarm_id: '',
      alarm_phone: '',
      alarm_time: new Date(),
      alarm_location: '',
      case_description: '',
      handling_result: '',
      lon: 116.407428,
      lat: 39.90923,
      alarm_type: '',
      alarm_level: 2,
      status: 0,
      police_point_id: null,
      camera_id: null
    })
    
    const processForm = reactive({
      handling_result: ''
    })
    
    const rules = {
      alarm_id: [
        { required: true, message: '请输入警情编号', trigger: 'blur' }
      ],
      alarm_location: [
        { required: true, message: '请输入警情地点', trigger: 'blur' }
      ],
      alarm_time: [
        { required: true, message: '请选择报警时间', trigger: 'change' }
      ],
      case_description: [
        { required: true, message: '请输入案件描述', trigger: 'blur' }
      ],
      lon: [
        { required: true, message: '请输入经度', trigger: 'blur' }
      ],
      lat: [
        { required: true, message: '请输入纬度', trigger: 'blur' }
      ],
      alarm_type: [
        { required: true, message: '请输入警情类型', trigger: 'blur' }
      ],
      alarm_level: [
        { required: true, message: '请选择级别', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }
    
    const processRules = {
      handling_result: [
        { required: true, message: '请输入处理结果', trigger: 'blur' }
      ]
    }
    
    const loadAlarmData = async () => {
      try {
        console.log('开始获取警情数据...')
        const response = await axios.get('/api/alarm')
        console.log('警情数据获取成功:', response.data)
        alarmList.value = response.data.map(item => ({
          ...item,
          alarm_time: formatTime(item.alarm_time),
          create_time: formatTime(item.create_time)
        }))
        total.value = alarmList.value.length
        applyStatusFilter()
        console.log('警情数据加载完成，共', total.value, '条记录')
      } catch (error) {
        console.error('获取警情数据失败:', error)
        alarmList.value = []
        total.value = 0
        console.log('警情数据加载失败，显示空列表')
      }
    }
    
    const applyStatusFilter = () => {
      if (statusFilter.value !== '' && statusFilter.value !== null) {
        const filtered = alarmList.value.filter(item => item.status === statusFilter.value)
        alarmList.value = filtered
        total.value = filtered.length
      }
    }
    
    const searchAlarm = () => {
      if (searchKeyword.value) {
        const filtered = alarmList.value.filter(item => 
          item.alarm_id.includes(searchKeyword.value) || 
          item.alarm_location.includes(searchKeyword.value) ||
          item.case_description.includes(searchKeyword.value)
        )
        alarmList.value = filtered
        total.value = filtered.length
      } else {
        loadAlarmData()
      }
    }
    
    const addAlarm = () => {
      isEditing.value = false
      alarmForm.id = null
      alarmForm.alarm_id = ''
      alarmForm.alarm_phone = ''
      alarmForm.alarm_time = new Date()
      alarmForm.alarm_location = ''
      alarmForm.case_description = ''
      alarmForm.handling_result = ''
      alarmForm.lon = 116.407428
      alarmForm.lat = 39.90923
      alarmForm.alarm_type = ''
      alarmForm.alarm_level = 2
      alarmForm.status = 0
      alarmForm.police_point_id = null
      alarmForm.camera_id = null
      dialogVisible.value = true
    }
    
    const editAlarm = (row) => {
      isEditing.value = true
      alarmForm.id = row.id
      alarmForm.alarm_id = row.alarm_id
      alarmForm.alarm_phone = row.alarm_phone
      alarmForm.alarm_time = new Date(row.alarm_time)
      alarmForm.alarm_location = row.alarm_location
      alarmForm.case_description = row.case_description
      alarmForm.handling_result = row.handling_result
      alarmForm.lon = row.lon
      alarmForm.lat = row.lat
      alarmForm.alarm_type = row.alarm_type
      alarmForm.alarm_level = row.alarm_level
      alarmForm.status = row.status
      alarmForm.police_point_id = row.police_point_id
      alarmForm.camera_id = row.camera_id
      dialogVisible.value = true
    }
    
    const saveAlarm = async () => {
      if (alarmFormRef.value) {
        await alarmFormRef.value.validate(async (valid) => {
          if (valid) {
            try {
              const submitData = { ...alarmForm }
              if (submitData.alarm_time instanceof Date) {
                const date = new Date(submitData.alarm_time)
                const year = date.getFullYear()
                const month = String(date.getMonth() + 1).padStart(2, '0')
                const day = String(date.getDate()).padStart(2, '0')
                const hours = String(date.getHours()).padStart(2, '0')
                const minutes = String(date.getMinutes()).padStart(2, '0')
                const seconds = String(date.getSeconds()).padStart(2, '0')
                submitData.alarm_time = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
              } else if (typeof submitData.alarm_time === 'string' && submitData.alarm_time.includes(' ')) {
                const date = new Date(submitData.alarm_time)
                const year = date.getFullYear()
                const month = String(date.getMonth() + 1).padStart(2, '0')
                const day = String(date.getDate()).padStart(2, '0')
                const hours = String(date.getHours()).padStart(2, '0')
                const minutes = String(date.getMinutes()).padStart(2, '0')
                const seconds = String(date.getSeconds()).padStart(2, '0')
                submitData.alarm_time = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
              }
              console.log('提交警情数据:', submitData)
              if (isEditing.value) {
                await axios.put(`/api/alarm/${alarmForm.id}`, submitData)
              } else {
                await axios.post('/api/alarm', submitData)
              }
              await loadAlarmData()
              dialogVisible.value = false
            } catch (error) {
              console.error('保存警情失败:', error)
              console.error('错误详情:', error.response)
              alert('保存失败，请重试')
            }
          }
        })
      }
    }
    
    const processAlarm = (row) => {
      currentAlarm.value = row
      processForm.handling_result = row.handling_result || ''
      processDialogVisible.value = true
    }
    
    const submitProcess = async () => {
      if (processFormRef.value) {
        await processFormRef.value.validate(async (valid) => {
          if (valid && currentAlarm.value) {
            try {
              await axios.put(`/api/alarm/${currentAlarm.value.id}`, {
                handling_result: processForm.handling_result,
                status: 1
              })
              await loadAlarmData()
              processDialogVisible.value = false
            } catch (error) {
              console.error('处理警情失败:', error)
              alert('处理失败，请重试')
            }
          }
        })
      }
    }
    
    const deleteAlarm = async (id) => {
      try {
        await axios.delete(`/api/alarm/${id}`)
        await loadAlarmData()
      } catch (error) {
        console.error('删除警情失败:', error)
        alert('删除失败，请重试')
      }
    }
    
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    const downloadTemplate = () => {
      window.open('/api/alarm/template', '_blank')
    }
    
    const beforeUpload = (file) => {
      const isExcel = file.name.endsWith('.xls') || file.name.endsWith('.xlsx')
      if (!isExcel) {
        ElMessage.error('只能上传Excel文件')
        return false
      }
      return true
    }
    
    const handleImport = async (options) => {
      const formData = new FormData()
      formData.append('file', options.file)
      try {
        const response = await axios.post('/api/alarm/import', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (response.data.success) {
          ElMessage.success(`导入成功，共导入 ${response.data.importedCount} 条数据`)
          await loadAlarmData()
        } else {
          ElMessage.error(response.data.message || '导入失败')
        }
      } catch (error) {
        console.error('导入失败:', error)
        ElMessage.error('导入失败，请检查文件格式')
      }
    }
    
    statusFilter.value = ''
    
    const formatTime = (time) => {
      if (!time) return ''
      if (typeof time === 'string') {
        if (time.includes('T')) {
          const date = new Date(time)
          const year = date.getFullYear()
          const month = String(date.getMonth() + 1).padStart(2, '0')
          const day = String(date.getDate()).padStart(2, '0')
          const hours = String(date.getHours()).padStart(2, '0')
          const minutes = String(date.getMinutes()).padStart(2, '0')
          const seconds = String(date.getSeconds()).padStart(2, '0')
          return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
        }
        if (time.includes('-') && time.includes(':')) {
          return time
        }
      }
      const date = new Date(time)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    }
    
    onMounted(async () => {
      await loadAlarmData()
    })
    
    return {
      searchKeyword,
      statusFilter,
      alarmList,
      total,
      currentPage,
      pageSize,
      dialogVisible,
      processDialogVisible,
      isEditing,
      alarmForm,
      processForm,
      rules,
      processRules,
      alarmFormRef,
      processFormRef,
      uploadRef,
      searchAlarm,
      addAlarm,
      editAlarm,
      saveAlarm,
      deleteAlarm,
      processAlarm,
      submitProcess,
      handlePageChange,
      downloadTemplate,
      beforeUpload,
      handleImport,
      formatTime
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

:deep(.el-date-editor .el-input__inner) {
  height: 28px;
  line-height: 28px;
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
