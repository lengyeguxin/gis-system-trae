<template>
  <div class="police-container">
    <div class="page-header">
      <h2>GIS警务点管理</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入警务点名称" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button type="primary" @click="searchPolice"><el-icon><Search /></el-icon> 查询</el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addPolice">添加警务点</el-button>
    </div>
    
    <div class="police-table">
      <div class="table-container">
        <el-table :data="policeList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="name" label="警务点名称"></el-table-column>
          <el-table-column prop="location" label="警务点地点"></el-table-column>
          <el-table-column prop="description" label="描述"></el-table-column>
          <el-table-column prop="contactPerson" label="联系人"></el-table-column>
          <el-table-column prop="contactPhone" label="联系方式"></el-table-column>
          <el-table-column prop="latitude" label="纬度" width="120"></el-table-column>
          <el-table-column prop="longitude" label="经度" width="120"></el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="editPolice(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deletePolice(scope.row.id)">删除</el-button>
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
    
    <!-- 添加/编辑警务点对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑警务点' : '添加警务点'">
      <el-form :model="policeForm" :rules="rules" ref="policeFormRef">
        <el-form-item label="警务点名称" prop="name">
          <el-input v-model="policeForm.name"></el-input>
        </el-form-item>
        <el-form-item label="警务点地点" prop="location">
          <el-input v-model="policeForm.location"></el-input>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="policeForm.contactPerson"></el-input>
        </el-form-item>
        <el-form-item label="联系方式" prop="contactPhone">
          <el-input v-model="policeForm.contactPhone"></el-input>
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
import axios from 'axios'

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
      location: '',
      contactPerson: '',
      contactPhone: '',
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
    const loadPoliceData = async () => {
      try {
        console.log('开始获取警务点数据...')
        const response = await axios.get('http://localhost:3001/api/gis/police')
        console.log('警务点数据获取成功:', response.data)
        // 转换数据格式
        policeList.value = response.data.map(item => ({
          id: item.id,
          name: item.name,
          location: item.location || '',
          contactPerson: item.contactPerson || '',
          contactPhone: item.contactPhone || '',
          latitude: item.latitude,
          longitude: item.longitude,
          description: item.description
        }))
        total.value = policeList.value.length
      } catch (error) {
        console.error('获取警务点数据失败:', error)
        // 失败时使用模拟数据
        policeList.value = [
          { id: 1, name: '东直门外派出所', location: '北京市东城区东直门外大街1号', contactPerson: '张三', contactPhone: '13800138001', latitude: 39.90923, longitude: 116.397428, description: '北京市公安局东城分局东直门外派出所' },
          { id: 2, name: '西长安街派出所', location: '北京市西城区西长安街12号', contactPerson: '李四', contactPhone: '13800138002', latitude: 39.91923, longitude: 116.407428, description: '北京市公安局西城分局西长安街派出所' },
          { id: 3, name: '朝阳公园派出所', location: '北京市朝阳区朝阳公园路15号', contactPerson: '王五', contactPhone: '13800138003', latitude: 39.92923, longitude: 116.417428, description: '北京市公安局朝阳分局朝阳公园派出所' },
          { id: 4, name: '中关村派出所', location: '北京市海淀区中关村大街28号', contactPerson: '赵六', contactPhone: '13800138004', latitude: 39.93923, longitude: 116.427428, description: '北京市公安局海淀分局中关村派出所' },
          { id: 5, name: '丰台镇派出所', location: '北京市丰台区丰台镇正阳大街15号', contactPerson: '钱七', contactPhone: '13800138005', latitude: 39.94923, longitude: 116.437428, description: '北京市公安局丰台分局丰台镇派出所' }
        ]
        total.value = policeList.value.length
      }
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
      policeForm.location = ''
      policeForm.contactPerson = ''
      policeForm.contactPhone = ''
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
      policeForm.location = row.location || ''
      policeForm.contactPerson = row.contactPerson || ''
      policeForm.contactPhone = row.contactPhone || ''
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
    
    onMounted(async () => {
      await loadPoliceData()
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

.police-table {
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
  .police-container {
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
  
  .police-table {
    padding: 16px;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
}
</style>
