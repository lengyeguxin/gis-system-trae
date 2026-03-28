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
          <el-button @click="searchAddress"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addAddress">添加地址</el-button>
    </div>
    
    <div class="address-table">
      <el-table :data="addressList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="地址名称"></el-table-column>
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
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="editAddress(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteAddress(scope.row.id)">删除</el-button>
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
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑地址' : '添加地址'">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef">
        <el-form-item label="地址名称" prop="name">
          <el-input v-model="addressForm.name"></el-input>
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
    const loadAddressData = () => {
      // 模拟数据
      addressList.value = [
        { id: 1, name: '北京市东城区', latitude: 39.9288, longitude: 116.4166, code: '110101', status: 'valid' },
        { id: 2, name: '北京市西城区', latitude: 39.9046, longitude: 116.3691, code: '110102', status: 'valid' },
        { id: 3, name: '北京市朝阳区', latitude: 39.9219, longitude: 116.4551, code: '110105', status: 'valid' },
        { id: 4, name: '北京市海淀区', latitude: 39.9609, longitude: 116.3067, code: '110108', status: 'valid' },
        { id: 5, name: '北京市丰台区', latitude: 39.8584, longitude: 116.2868, code: '110106', status: 'valid' }
      ]
      total.value = addressList.value.length
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
      // 模拟下载
      console.log('下载模板')
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
    
    onMounted(() => {
      loadAddressData()
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

.header-actions {
  display: flex;
  gap: 10px;
}

.upload-btn .el-button {
  background: linear-gradient(90deg, #00cc99, #009966);
  border: none;
  color: #0f172a;
  font-weight: bold;
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

.address-table {
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
