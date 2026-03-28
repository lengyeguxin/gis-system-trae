<template>
  <div class="alarm-container">
    <div class="page-header">
      <h2>警情信息</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入警情名称" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button @click="searchAlarm"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addAlarm">添加警情</el-button>
    </div>
    
    <div class="alarm-table">
      <el-table :data="alarmList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="警情名称"></el-table-column>
        <el-table-column prop="latitude" label="纬度" width="120"></el-table-column>
        <el-table-column prop="longitude" label="经度" width="120"></el-table-column>
        <el-table-column prop="level" label="级别" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.level === 'high' ? 'danger' : scope.row.level === 'medium' ? 'warning' : 'success'">
              {{ scope.row.level === 'high' ? '高' : scope.row.level === 'medium' ? '中' : '低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editAlarm(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteAlarm(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
        <el-form-item label="警情名称" prop="name">
          <el-input v-model="alarmForm.name"></el-input>
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
      name: '',
      latitude: 0,
      longitude: 0,
      level: 'medium',
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
    const loadAlarmData = () => {
      // 模拟数据
      alarmList.value = [
        { id: 1, name: '警情1', latitude: 39.91923, longitude: 116.417428, level: 'high', description: '交通事故', createTime: '2026-03-27 10:00:00' },
        { id: 2, name: '警情2', latitude: 39.92923, longitude: 116.407428, level: 'medium', description: '纠纷', createTime: '2026-03-27 10:30:00' },
        { id: 3, name: '警情3', latitude: 39.93923, longitude: 116.397428, level: 'low', description: '噪音投诉', createTime: '2026-03-27 11:00:00' },
        { id: 4, name: '警情4', latitude: 39.94923, longitude: 116.417428, level: 'high', description: '盗窃', createTime: '2026-03-27 11:30:00' },
        { id: 5, name: '警情5', latitude: 39.95923, longitude: 116.407428, level: 'medium', description: '斗殴', createTime: '2026-03-27 12:00:00' }
      ]
      total.value = alarmList.value.length
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
      alarmForm.name = ''
      alarmForm.latitude = 0
      alarmForm.longitude = 0
      alarmForm.level = 'medium'
      alarmForm.description = ''
      dialogVisible.value = true
    }
    
    // 编辑警情
    const editAlarm = (row) => {
      isEditing.value = true
      alarmForm.id = row.id
      alarmForm.name = row.name
      alarmForm.latitude = row.latitude
      alarmForm.longitude = row.longitude
      alarmForm.level = row.level
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
    
    onMounted(() => {
      loadAlarmData()
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
      handlePageChange
    }
  }
}
</script>

<style scoped>
.alarm-container {
  padding: 20px;
  background: #0f172a;
  min-height: 100vh;
  color: #ffffff;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
}

.page-header h2 {
  font-size: 24px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.3);
  margin: 0;
}

.search-section {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
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

.el-input__append .el-button {
  background: linear-gradient(90deg, #00ffff, #0099cc);
  border: none;
  color: #0f172a;
  font-weight: bold;
}

.alarm-table {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  padding: 20px;
  border: 1px solid rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.1);
}

.el-table {
  background: transparent;
}

.el-table th {
  background: rgba(0, 255, 255, 0.1);
  color: #00ffff;
  border-bottom: 1px solid rgba(0, 255, 255, 0.2);
}

.el-table td {
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(0, 255, 255, 0.1);
}

.el-table tr:hover {
  background: rgba(0, 255, 255, 0.05);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.el-pagination__item {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
}

.el-pagination__item:hover {
  border-color: #00ffff;
  color: #00ffff;
}

.el-pagination__item.active {
  background: #00ffff;
  border-color: #00ffff;
  color: #0f172a;
}

.el-dialog {
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.2);
  border-radius: 8px;
}

.el-dialog__title {
  color: #00ffff;
  font-weight: bold;
}

.el-form-item__label {
  color: rgba(255, 255, 255, 0.8);
}

.el-select {
  width: 100%;
}

.el-select .el-input {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 255, 255, 0.3);
  border-radius: 6px;
}

.el-select .el-input__inner {
  color: #ffffff;
  background: transparent;
}

.el-select-dropdown {
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(0, 255, 255, 0.2);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.1);
}

.el-select-dropdown__item {
  color: rgba(255, 255, 255, 0.8);
}

.el-select-dropdown__item:hover {
  background: rgba(0, 255, 255, 0.1);
  color: #00ffff;
}

.el-button {
  background: linear-gradient(90deg, #00ffff, #0099cc);
  border: none;
  color: #0f172a;
  font-weight: bold;
}

.el-button--danger {
  background: linear-gradient(90deg, #ff6666, #cc0000);
}

.el-button--primary {
  background: linear-gradient(90deg, #00ffff, #0099cc);
}
</style>
