<template>
  <div class="police-container">
    <div class="page-header">
      <h2>GIS警务点管理</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入警务点名称" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button @click="searchPolice"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addPolice">添加警务点</el-button>
    </div>
    
    <div class="police-table">
      <el-table :data="policeList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="警务点名称"></el-table-column>
        <el-table-column prop="latitude" label="纬度" width="120"></el-table-column>
        <el-table-column prop="longitude" label="经度" width="120"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editPolice(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deletePolice(scope.row.id)">删除</el-button>
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
    
    <!-- 添加/编辑警务点对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑警务点' : '添加警务点'">
      <el-form :model="policeForm" :rules="rules" ref="policeFormRef">
        <el-form-item label="警务点名称" prop="name">
          <el-input v-model="policeForm.name"></el-input>
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model.number="policeForm.latitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model.number="policeForm.longitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="policeForm.description" type="textarea"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePolice">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useGisStore } from '../stores/gis'

export default {
  name: 'GisPolice',
  setup() {
    const gisStore = useGisStore()
    
    const searchKeyword = ref('')
    const policeList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    const dialogVisible = ref(false)
    const isEditing = ref(false)
    const policeFormRef = ref(null)
    
    const policeForm = reactive({
      id: null,
      name: '',
      latitude: 0,
      longitude: 0,
      description: ''
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入警务点名称', trigger: 'blur' }
      ],
      latitude: [
        { required: true, message: '请输入纬度', trigger: 'blur' }
      ],
      longitude: [
        { required: true, message: '请输入经度', trigger: 'blur' }
      ]
    }
    
    // 加载警务点数据
    const loadPoliceData = () => {
      // 模拟数据
      policeList.value = [
        { id: 1, name: '派出所1', latitude: 39.90923, longitude: 116.397428, description: '北京市公安局东城分局' },
        { id: 2, name: '派出所2', latitude: 39.91923, longitude: 116.407428, description: '北京市公安局西城分局' },
        { id: 3, name: '派出所3', latitude: 39.92923, longitude: 116.417428, description: '北京市公安局朝阳分局' },
        { id: 4, name: '派出所4', latitude: 39.93923, longitude: 116.427428, description: '北京市公安局海淀分局' },
        { id: 5, name: '派出所5', latitude: 39.94923, longitude: 116.437428, description: '北京市公安局丰台分局' }
      ]
      total.value = policeList.value.length
    }
    
    // 搜索警务点
    const searchPolice = () => {
      // 模拟搜索
      if (searchKeyword.value) {
        const filtered = policeList.value.filter(item => 
          item.name.includes(searchKeyword.value) || 
          item.description.includes(searchKeyword.value)
        )
        policeList.value = filtered
        total.value = filtered.length
      } else {
        loadPoliceData()
      }
    }
    
    // 添加警务点
    const addPolice = () => {
      isEditing.value = false
      policeForm.id = null
      policeForm.name = ''
      policeForm.latitude = 0
      policeForm.longitude = 0
      policeForm.description = ''
      dialogVisible.value = true
    }
    
    // 编辑警务点
    const editPolice = (row) => {
      isEditing.value = true
      policeForm.id = row.id
      policeForm.name = row.name
      policeForm.latitude = row.latitude
      policeForm.longitude = row.longitude
      policeForm.description = row.description
      dialogVisible.value = true
    }
    
    // 保存警务点
    const savePolice = async () => {
      if (policeFormRef.value) {
        await policeFormRef.value.validate((valid) => {
          if (valid) {
            // 模拟保存
            if (isEditing.value) {
              const index = policeList.value.findIndex(item => item.id === policeForm.id)
              if (index !== -1) {
                policeList.value[index] = { ...policeForm }
              }
            } else {
              const newId = Math.max(...policeList.value.map(item => item.id)) + 1
              policeList.value.push({
                ...policeForm,
                id: newId
              })
              total.value++
            }
            dialogVisible.value = false
          }
        })
      }
    }
    
    // 删除警务点
    const deletePolice = (id) => {
      const index = policeList.value.findIndex(item => item.id === id)
      if (index !== -1) {
        policeList.value.splice(index, 1)
        total.value--
      }
    }
    
    // 分页处理
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    onMounted(() => {
      loadPoliceData()
    })
    
    return {
      searchKeyword,
      policeList,
      total,
      currentPage,
      pageSize,
      dialogVisible,
      isEditing,
      policeForm,
      rules,
      policeFormRef,
      searchPolice,
      addPolice,
      editPolice,
      savePolice,
      deletePolice,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.police-container {
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

.police-table {
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
