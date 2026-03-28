<template>
  <div class="monitor-container">
    <div class="page-header">
      <h2>GIS监控管理</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入监控点名称" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button @click="searchMonitor"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addMonitor">添加监控点</el-button>
    </div>
    
    <div class="monitor-table">
      <el-table :data="monitorList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="监控点名称"></el-table-column>
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
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editMonitor(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteMonitor(scope.row.id)">删除</el-button>
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
    
    <!-- 添加/编辑监控点对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑监控点' : '添加监控点'">
      <el-form :model="monitorForm" :rules="rules" ref="monitorFormRef">
        <el-form-item label="监控点名称" prop="name">
          <el-input v-model="monitorForm.name"></el-input>
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
    const loadMonitorData = () => {
      // 模拟数据
      monitorList.value = [
        { id: 1, name: '监控点1', latitude: 39.90923, longitude: 116.417428, type: 'road', status: 'online' },
        { id: 2, name: '监控点2', latitude: 39.92923, longitude: 116.397428, type: 'community', status: 'online' },
        { id: 3, name: '监控点3', latitude: 39.91923, longitude: 116.427428, type: 'road', status: 'offline' },
        { id: 4, name: '监控点4', latitude: 39.93923, longitude: 116.407428, type: 'community', status: 'online' },
        { id: 5, name: '监控点5', latitude: 39.94923, longitude: 116.387428, type: 'road', status: 'online' }
      ]
      total.value = monitorList.value.length
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
    
    onMounted(() => {
      loadMonitorData()
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

.monitor-table {
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
