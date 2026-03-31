<template>
  <div class="address-container">
    <div class="page-header">
      <h2>GIS图层地址库</h2>
    </div>
    
    <div class="search-section">
      <el-input v-model="searchKeyword" placeholder="请输入地址关键词" style="width: 300px; margin-right: 10px">
        <template #append>
          <el-button type="primary" @click="searchAddress"><el-icon><Search /></el-icon> 查询</el-button>
        </template>
      </el-input>
      <el-button type="primary" @click="addAddress">添加地址</el-button>
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
    
    <div class="address-table">
      <div class="table-container">
        <el-table :data="paginatedList" style="width: 100%" stripe border>
          <el-table-column type="index" label="序号" width="90" align="center"></el-table-column>
          <el-table-column prop="name" label="地址名称" min-width="200" show-overflow-tooltip></el-table-column>
          <el-table-column prop="district" label="行政区划" min-width="180" show-overflow-tooltip></el-table-column>
          <el-table-column prop="street" label="街道" min-width="120" show-overflow-tooltip></el-table-column>
          <el-table-column label="坐标" min-width="160">
            <template #default="scope">
              <span class="coord-text">{{ formatCoord(scope.row.longitude) }}, {{ formatCoord(scope.row.latitude) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip></el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'valid' ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 'valid' ? '有效' : '无效' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="scope">
              <el-button size="small" type="primary" link @click="editAddress(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" link @click="deleteAddress(scope.row.id)">删除</el-button>
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
    
    <!-- 添加/编辑地址对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEditing ? '编辑地址' : '添加地址'">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef">
        <el-form-item label="地址名称" prop="name">
          <el-input v-model="addressForm.name"></el-input>
        </el-form-item>
        <el-form-item label="行政区划" prop="province">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="addressForm.province" placeholder="请选择省份" @change="handleProvinceChange" style="width: 100%; min-width: 150px">
                <el-option v-for="province in provinces" :key="province.id" :label="province.name" :value="province.id"></el-option>
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.city" placeholder="请选择城市" @change="handleCityChange" style="width: 100%; min-width: 150px" :disabled="!addressForm.province">
                <el-option v-for="city in cities" :key="city.id" :label="city.name" :value="city.id"></el-option>
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-select v-model="addressForm.district" placeholder="请选择区县" @change="handleDistrictChange" style="width: 100%; min-width: 150px" :disabled="!addressForm.city">
                <el-option v-for="district in districts" :key="district.id" :label="district.name" :value="district.id"></el-option>
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="街道名称" prop="street">
          <el-select v-model="addressForm.street" placeholder="请选择街道（可选）" clearable @change="handleStreetChange">
            <el-option v-for="street in streets" :key="street.division_code" :label="street.division_name" :value="street.division_code"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="纬度" prop="latitude">
          <el-input v-model.number="addressForm.latitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input v-model.number="addressForm.longitude" type="number" step="0.000001"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addressForm.remark" type="textarea" rows="3"></el-input>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useGisStore } from '../stores/gis'
import { Download, Upload, Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

export default {
  name: 'GisAddress',
  components: { Download, Upload, Search },
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
    const uploadRef = ref(null)
    
    const provinces = ref([])
    const cities = ref([])
    const districts = ref([])
    const streets = ref([])
    const allDivisions = ref([])
    
    const addressForm = reactive({
      id: null,
      name: '',
      province: '',
      provinceName: '',
      city: '',
      cityName: '',
      district: '',
      districtName: '',
      street: '',
      streetName: '',
      latitude: 0,
      longitude: 0,
      remark: ''
    })
    
    const rules = {
      name: [
        { required: true, message: '请输入地址名称', trigger: 'blur' }
      ],
      province: [
        { required: true, message: '请选择省份', trigger: 'change' }
      ],
      city: [
        { required: true, message: '请选择城市', trigger: 'change' }
      ],
      district: [
        { required: true, message: '请选择区县', trigger: 'change' }
      ],

      latitude: [
        { required: true, message: '请输入纬度', trigger: 'blur' }
      ],
      longitude: [
        { required: true, message: '请输入经度', trigger: 'blur' }
      ]
    }
    
    // 构建级联数据
    const buildCascaderData = (data, parentId = 0) => {
      return data
        .filter(item => item.parentId === parentId)
        .map(item => ({
          id: item.id,
          name: item.name,
          level: item.level,
          children: buildCascaderData(data, item.id)
        }))
    }

    // 加载区划数据
    const loadDivisionData = async () => {
      try {
        // 加载所有区划数据
        const response = await axios.get('/api/division/all')
        allDivisions.value = response.data.map(item => ({
          id: item.id.toString(),
          name: item.name,
          parentId: item.parentId?.toString() || '0',
          level: item.level
        }))
        
        // 分离出省份、城市、区县数据
        provinces.value = allDivisions.value.filter(item => item.level === 1)
        cities.value = allDivisions.value.filter(item => item.level === 2)
        districts.value = allDivisions.value.filter(item => item.level === 3)
        
        console.log('区划数据获取成功:')
        console.log('省份:', provinces.value)
        console.log('城市:', cities.value)
        console.log('区县:', districts.value)
      } catch (error) {
        console.error('获取区划数据失败:', error)
        // 失败时显示空数据
        allDivisions.value = []
        provinces.value = []
        cities.value = []
        districts.value = []
      }
    }
    
    // 加载街道数据
    const loadStreetData = async (districtCode) => {
      try {
        const response = await axios.get(`/api/division/parent/${parseInt(districtCode)}`)
        // 转换数据格式以适应前端
        streets.value = response.data.map(item => ({
          division_code: item.id.toString(),
          division_name: item.name
        }))
        console.log('街道数据获取成功:', streets.value)
      } catch (error) {
        console.error('获取街道数据失败:', error)
        // 失败时显示空数据
        streets.value = []
      }
    }
    
    // 处理省份变更
    const handleProvinceChange = (provinceId) => {
      // 重置城市、区县和街道
      addressForm.city = ''
      addressForm.cityName = ''
      addressForm.district = ''
      addressForm.districtName = ''
      addressForm.street = ''
      addressForm.streetName = ''
      
      // 根据省份筛选城市
      cities.value = allDivisions.value.filter(item => item.level === 2 && item.parentId === provinceId)
      
      // 获取省份名称
      const province = provinces.value.find(p => p.id === provinceId)
      if (province) {
        addressForm.provinceName = province.name
      }
    }
    
    // 处理城市变更
    const handleCityChange = (cityId) => {
      // 重置区县和街道
      addressForm.district = ''
      addressForm.districtName = ''
      addressForm.street = ''
      addressForm.streetName = ''
      
      // 根据城市筛选区县
      districts.value = allDivisions.value.filter(item => item.level === 3 && item.parentId === cityId)
      
      // 获取城市名称
      const city = cities.value.find(c => c.id === cityId)
      if (city) {
        addressForm.cityName = city.name
      }
    }
    
    // 处理区县变更
    const handleDistrictChange = (districtId) => {
      // 重置街道
      addressForm.street = ''
      addressForm.streetName = ''
      
      if (districtId) {
        loadStreetData(districtId)
        // 获取区县名称
        const district = districts.value.find(d => d.id === districtId)
        if (district) {
          addressForm.districtName = district.name
        }
      }
    }
    
    // 处理街道变更
    const handleStreetChange = (streetCode) => {
      if (streetCode) {
        // 获取街道名称
        const street = streets.value.find(s => s.division_code === streetCode)
        if (street) {
          addressForm.streetName = street.division_name
        }
      } else {
        addressForm.streetName = ''
      }
    }
    
    // 加载地址数据
    const loadAddressData = async () => {
      try {
        console.log('开始获取地址数据...')
        const response = await axios.get('/api/address')
        console.log('地址数据获取成功:', response.data)
        // 转换数据格式
        addressList.value = response.data.map(item => {
          const fullDistrictName = getFullDistrictName(item.admin_code)
          return {
            id: item.id,
            name: item.address_full || '',
            adminCode: item.admin_code || '',
            district: fullDistrictName || item.admin_name || item.admin_code || '',
            street: item.street || '',
            doorNumber: item.house_number || '',
            latitude: item.lat,
            longitude: item.lon,
            remark: item.remark || '',
            status: item.status === 1 ? 'valid' : 'invalid'
          }
        })
        total.value = addressList.value.length
        console.log('地址数据加载完成，共', total.value, '条记录')
      } catch (error) {
        console.error('获取地址数据失败:', error)
        // 失败时显示空数据，不再使用模拟数据
        addressList.value = []
        total.value = 0
        console.log('地址数据加载失败，显示空列表')
      }
    }
    
    // 根据行政区划码获取完整的行政区划名称（省+市+区）
    const getFullDistrictName = (adminCode) => {
      if (!adminCode || allDivisions.value.length === 0) return ''
      
      const districtId = adminCode.toString().padEnd(9, '0')
      const district = allDivisions.value.find(d => d.id === districtId)
      if (!district) return ''
      
      const cityId = district.parentId?.toString()
      const city = allDivisions.value.find(d => d.id === cityId)
      
      const provinceId = city?.parentId?.toString()
      const province = allDivisions.value.find(d => d.id === provinceId)
      
      let fullName = ''
      if (province) fullName += province.name
      if (city && city.name !== province?.name) fullName += city.name
      fullName += district.name
      
      return fullName
    }
    
    // 根据区县编码解析出省编码、市编码
    const parseAdminCode = (adminCode) => {
      if (!adminCode) return { provinceCode: '', cityCode: '', districtCode: '' }
      
      const code = adminCode.toString().padEnd(9, '0')
      const provinceCode = code.substring(0, 2) + '0000000'
      const cityCode = code.substring(0, 4) + '00000'
      const districtCode = code.substring(0, 6) + '000'
      
      return { provinceCode, cityCode, districtCode }
    }
    
    // 搜索地址
    const searchAddress = async () => {
      try {
        if (searchKeyword.value) {
          // 调用后端API进行搜索
          const response = await axios.get('/api/address/search', {
            params: {
              keyword: searchKeyword.value
            }
          })
          console.log('搜索地址数据获取成功:', response.data)
          // 转换数据格式
          addressList.value = response.data.map(item => {
            const fullDistrictName = getFullDistrictName(item.admin_code)
            return {
              id: item.id,
              name: item.address_full || '',
              adminCode: item.admin_code || '',
              district: fullDistrictName || item.admin_name || item.admin_code || '',
              street: item.street || '',
              doorNumber: item.house_number || '',
              latitude: item.lat,
              longitude: item.lon,
              remark: item.remark || '',
              status: item.status === 1 ? 'valid' : 'invalid'
            }
          })
          total.value = addressList.value.length
          console.log('搜索完成，共', total.value, '条记录')
        } else {
          // 无关键词时加载所有数据
          await loadAddressData()
        }
      } catch (error) {
        console.error('搜索地址失败:', error)
        // 搜索失败时显示空数据
        addressList.value = []
        total.value = 0
        console.log('搜索失败，显示空列表')
      }
    }
    
    // 添加地址
    const addAddress = () => {
      isEditing.value = false
      addressForm.id = null
      addressForm.name = ''
      addressForm.province = ''
      addressForm.provinceName = ''
      addressForm.city = ''
      addressForm.cityName = ''
      addressForm.district = ''
      addressForm.districtName = ''
      addressForm.street = ''
      addressForm.streetName = ''
      addressForm.latitude = 0
      addressForm.longitude = 0
      addressForm.remark = ''
      dialogVisible.value = true
    }
    
    // 编辑地址
    const editAddress = (row) => {
      isEditing.value = true
      addressForm.id = row.id
      addressForm.name = row.name
      
      // 根据行政区划码解析出省、市、区编码
      const { provinceCode, cityCode, districtCode } = parseAdminCode(row.adminCode)
      
      // 设置省份
      addressForm.province = provinceCode
      const province = provinces.value.find(p => p.id === provinceCode)
      addressForm.provinceName = province ? province.name : ''
      
      // 根据省份筛选城市
      if (provinceCode) {
        cities.value = allDivisions.value.filter(item => item.level === 2 && item.parentId === provinceCode)
      }
      
      // 设置城市
      addressForm.city = cityCode
      const city = cities.value.find(c => c.id === cityCode)
      addressForm.cityName = city ? city.name : ''
      
      // 根据城市筛选区县
      if (cityCode) {
        districts.value = allDivisions.value.filter(item => item.level === 3 && item.parentId === cityCode)
      }
      
      // 设置区县
      addressForm.district = districtCode
      const district = districts.value.find(d => d.id === districtCode)
      addressForm.districtName = district ? district.name : ''
      
      // 加载街道数据
      if (districtCode) {
        loadStreetData(districtCode)
      }
      
      addressForm.street = row.street || ''
      addressForm.streetName = row.street || ''
      addressForm.latitude = row.latitude
      addressForm.longitude = row.longitude
      addressForm.remark = row.remark || ''
      dialogVisible.value = true
    }
    
    // 保存地址
    const saveAddress = async () => {
      if (addressFormRef.value) {
        await addressFormRef.value.validate((valid) => {
          if (valid) {
            // 构建后端需要的数据结构
            const addressData = {
              id: addressForm.id,
              address_full: addressForm.name,
              admin_code: addressForm.district,
              admin_name: addressForm.districtName,
              street_code: addressForm.street,
              street: addressForm.streetName || addressForm.street,
              lat: addressForm.latitude,
              lon: addressForm.longitude,
              status: 1,
              source: 'manual',
              remark: addressForm.remark
            }
            
            // 发送请求到后端
            if (isEditing.value) {
              // 更新地址
              axios.put(`/api/address/${addressForm.id}`, addressData)
                .then(response => {
                  if (response.data.success) {
                    console.log('地址更新成功:', response.data)
                    ElMessage.success('地址更新成功')
                    loadAddressData()
                  } else {
                    ElMessage.error(response.data.message || '地址更新失败')
                  }
                })
                .catch(error => {
                  console.error('地址更新失败:', error)
                  ElMessage.error(error.response?.data?.message || '地址更新失败')
                })
            } else {
              // 创建新地址
              axios.post('/api/address', addressData)
                .then(response => {
                  if (response.data.success) {
                    console.log('地址创建成功:', response.data)
                    ElMessage.success('地址创建成功')
                    loadAddressData()
                  } else {
                    ElMessage.error(response.data.message || '地址创建失败')
                  }
                })
                .catch(error => {
                  console.error('地址创建失败:', error)
                  ElMessage.error(error.response?.data?.message || '地址创建失败')
                })
            }
            
            dialogVisible.value = false
          }
        })
      }
    }
    
    // 删除地址
    const deleteAddress = (id) => {
      // 调用后端API删除地址
      axios.delete(`/api/address/${id}`)
        .then(response => {
          console.log('地址删除成功:', response.data)
          // 重新加载数据
          loadAddressData()
        })
        .catch(error => {
          console.error('地址删除失败:', error)
        })
    }
    
    // 下载模板
    const downloadTemplate = () => {
      window.open('/api/address/template', '_blank')
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
        const response = await axios.post('/api/address/import', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        })
        if (response.data.success) {
          ElMessage.success(`导入成功，共导入 ${response.data.importedCount} 条数据`)
          await loadAddressData()
        } else {
          ElMessage.error(response.data.message || '导入失败')
        }
      } catch (error) {
        console.error('导入失败:', error)
        ElMessage.error('导入失败，请检查文件格式')
      }
    }
    
    // 分页处理
    const handlePageChange = (page) => {
      currentPage.value = page
    }
    
    // 格式化坐标，去掉尾部补0
    const formatCoord = (value) => {
      if (value === null || value === undefined) return ''
      return parseFloat(value.toFixed(6)).toString()
    }
    
    // 分页后的数据列表
    const paginatedList = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      return addressList.value.slice(start, end)
    })
    
    onMounted(() => {
      loadDivisionData()
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
      uploadRef,
      provinces,
      cities,
      districts,
      streets,
      searchAddress,
      addAddress,
      editAddress,
      saveAddress,
      deleteAddress,
      downloadTemplate,
      beforeUpload,
      handleImport,
      handlePageChange,
      handleProvinceChange,
      handleCityChange,
      handleDistrictChange,
      handleStreetChange,
      paginatedList,
      formatCoord
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
  
  .address-table {
    padding: 16px;
  }
  
  :deep(.el-table) {
    min-width: 600px;
  }
}
</style>
