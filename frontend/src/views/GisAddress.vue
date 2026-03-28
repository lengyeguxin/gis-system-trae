<template>
  <div class="address-container">
    <div class="page-header">
      <h2>GIS图层地址库</h2>
      <div class="header-actions">
        <el-button type="primary" @click="downloadTemplate"><el-icon><Download /></el-icon> 下载模板</el-button>
        <el-upload
          class="upload-btn"
          action="#"
          :auto-upload="false"
          :on-change="handleFileChange"
          accept=".xlsx, .xls"
        >
          <el-button type="success"><el-icon><Upload /></el-icon> 批量导入</el-button>
        </el-upload>
      </div>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入地址关键词" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button type="primary" @click="searchAddress"><el-icon><Search /></el-icon> 查询</el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addAddress">添加地址</el-button>
    </div>
    
    <div class="address-table">
      <div class="table-container">
        <el-table :data="addressList" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="name" label="地址名称"></el-table-column>
          <el-table-column prop="description" label="地址描述"></el-table-column>
          <el-table-column prop="latitude" label="纬度" width="120"></el-table-column>
          <el-table-column prop="longitude" label="经度" width="120"></el-table-column>
          <el-table-column prop="code" label="地址编码" width="150"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'valid' ? 'success' : 'danger'">
                {{ scope.row.status === 'valid' ? '有效' : '无效' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" @click="editAddress(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteAddress(scope.row.id)">删除</el-button>
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
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑地址' : '添加地址'">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef">
        <el-form-item label="地址名称" prop="name">
          <el-input v-model="addressForm.name"></el-input>
        </el-form-item>
        <el-form-item label="地址描述" prop="description">
          <el-input v-model="addressForm.description" type="textarea"></el-input>
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input v-model.number="addressForm.latitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model.number="addressForm.longitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="地址编码" prop="code">
          <el-input v-model="addressForm.code"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
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
  name: 'GisAddress',
  setup() {
    const gisStore = useGisStore()
    
    const searchKeyword = ref('')
    const addressList = ref([])
    const total = ref(0)
    const currentPage = ref(1)
    const pageSize = ref(10)
    
    const dialogVisible = ref(false)
    const isEditing = ref(false)
    const addressFormRef = ref(null)
    
    const addressForm = reactive({
      id: null,
      name: '',
      description: '',
      latitude: 0,
      longitude: 0,
      code: ''
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入地址名称', trigger: 'blur' }
      ],
      latitude: [
        { required: true, message: '请输入纬度', trigger: 'blur' }
      ],
      longitude: [
        { required: true, message: '请输入经度', trigger: 'blur' }
      ],
      code: [
        { required: true, message: '请输入地址编码', trigger: 'blur' }
      ]
    }
    
    // 加载地址数据
    const loadAddressData = async () => {
      try {
        console.log('开始获取地址数据...')
        const response = await axios.get('http://localhost:3001/api/gis/address')
        console.log('地址数据获取成功:', response.data)
        // 转换数据格式
        addressList.value = response.data.map(item => ({
          id: item.id,
          name: item.name,
          description: item.description,
          latitude: item.latitude,
          longitude: item.longitude,
          code: item.code || '',
          status: 'valid'
        }))
        total.value = addressList.value.length
      } catch (error) {
        console.error('获取地址数据失败:', error)
        // 失败时使用模拟数据
        addressList.value = [
          { id: 1, name: '北京市东城区东直门外大街42号', description: '东城区政府所在地', latitude: 39.9288, longitude: 116.4166, code: '110101', status: 'valid' },
          { id: 2, name: '北京市西城区二龙路27号', description: '西城区政府所在地', latitude: 39.9046, longitude: 116.3691, code: '110102', status: 'valid' },
          { id: 3, name: '北京市朝阳区朝阳公园南路1号', description: '朝阳区政府所在地', latitude: 39.9219, longitude: 116.4551, code: '110105', status: 'valid' },
          { id: 4, name: '北京市海淀区长春桥路17号', description: '海淀区政府所在地', latitude: 39.9609, longitude: 116.3067, code: '110108', status: 'valid' },
          { id: 5, name: '北京市丰台区丰台镇文体路2号', description: '丰台区政府所在地', latitude: 39.8584, longitude: 116.2868, code: '110106', status: 'valid' }
        ]
        total.value = addressList.value.length
      }
    }
    
    // 搜索地址
    const searchAddress = () => {
      // 模拟搜索
      if (searchKeyword.value) {
        const filtered = addressList.value.filter(item => 
          item.name.includes(searchKeyword.value) || 
          item.code.includes(searchKeyword.value)
        )
        addressList.value = filtered
        total.value = filtered.length
      } else {
        loadAddressData()
      }
    }
    
    // 添加地址
    const addAddress = () => {
      isEditing.value = false
      addressForm.id = null
      addressForm.name = ''
      addressForm.description = ''
      addressForm.latitude = 0
      addressForm.longitude = 0
      addressForm.code = ''
      dialogVisible.value = true
    }
    
    // 编辑地址
    const editAddress = (row) => {
      isEditing.value = true
      addressForm.id = row.id
      addressForm.name = row.name
      addressForm.description = row.description || ''
      addressForm.latitude = row.latitude
      addressForm.longitude = row.longitude
      addressForm.code = row.code
      dialogVisible.value = true
    }
    
    // 保存地址
    const saveAddress = async () => {
      if (addressFormRef.value) {
        await addressFormRef.value.validate((valid) => {
          if (valid) {
            // 模拟保存
            if (isEditing.value) {
              const index = addressList.value.findIndex(item => item.id === addressForm.id)
              if (index !== -1) {
                addressList.value[index] = { ...addressForm, status: 'valid' }
              }
            } else {
              const newId = Math.max(...addressList.value.map(item => item.id)) + 1
              addressList.value.push({
                ...addressForm,
                id: newId,
                status: 'valid'
              })
              total.value++
            }
            dialogVisible.value = false
          }
        })
      }
    }
    
    // 删除地址
    const deleteAddress = (id) => {
      const index = addressList.value.findIndex(item => item.id === id)
      if (index !== -1) {
        addressList.value.splice(index, 1)
        total.value--
      }
    }
    
    // 下载模板
    const downloadTemplate = () => {
      // 生成CSV模板内容
      const csvContent = `地址名称,地址描述,纬度,经度,地址编码
北京市东城区东直门外大街42号,东城区政府所在地,39.9288,116.4166,110101
北京市西城区二龙路27号,西城区政府所在地,39.9046,116.3691,110102
`
      
      // 创建Blob对象
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      
      // 创建下载链接
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', '地址库模板.csv')
      link.style.visibility = 'hidden'
      
      // 添加到DOM并触发下载
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
    
    // 处理文件上传
    const handleFileChange = (file) => {
      // 模拟文件处理
      console.log('文件上传:', file)
    }
    
    // 分页处理
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    onMounted(async () => {
      await loadAddressData()
    })
    
    return {
      searchKeyword,
      addressList,
      total,
      currentPage,
      pageSize,
      dialogVisible,
      isEditing,
      addressForm,
      rules,
      addressFormRef,
      searchAddress,
      addAddress,
      editAddress,
      saveAddress,
      deleteAddress,
      downloadTemplate,
      handleFileChange,
      handlePageChange
    }
  }
}
</script>

<style scoped>
.address-container {
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

.header-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
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

.address-table {
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
  .address-container {
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
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .address-table {
    padding: 16px;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
}
</style>
