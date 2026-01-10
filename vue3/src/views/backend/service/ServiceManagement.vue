<template>
  <div class="service-management-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="服务名称">
          <el-input v-model="searchForm.name" placeholder="请输入服务名称" clearable />
        </el-form-item>
        <el-form-item label="服务分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable style="width: 200px">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :disabled="item.status === 0"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 200px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">服务列表</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd">添加服务</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80" align="center" />
        <el-table-column v-if="isColumnVisible('name')" prop="name" label="服务名称" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('category')" label="服务分类" min-width="120">
          <template #default="scope">
            {{ getCategoryName(scope.row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('price')" prop="price" label="价格" width="100" align="center">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('duration')" label="时长" width="100" align="center">
          <template #default="scope">
            {{ getDurationText(scope.row.duration) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              :type="scope.row.status === 1 ? 'danger' : 'success'" 
              link 
              @click="handleChangeStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination 
          :current-page="currentPage" 
          :page-size="pageSize" 
          :total="total" 
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper" 
          @size-change="handleSizeChange" 
          @current-change="handleCurrentChange" 
        />
      </div>
    </el-card>
    
    <!-- 服务表单对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="form.id ? '编辑服务' : '添加服务'" 
      width="600px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="100px" 
        label-position="right"
        status-icon
        class="dialog-form"
      >
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称"></el-input>
        </el-form-item>
        
        <el-form-item label="服务分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :disabled="item.status === 0"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="服务价格" prop="price">
          <el-input-number 
            v-model="form.price" 
            :min="0" 
            :precision="2" 
            :step="10" 
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        
        <el-form-item label="服务时长" prop="duration">
          <el-input-number 
            v-model="form.duration" 
            :min="0" 
            :step="30" 
            style="width: 100%"
          ></el-input-number>
          <span class="duration-hint">分钟</span>
        </el-form-item>
        
        <el-form-item label="服务状态" prop="status">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          ></el-switch>
        </el-form-item>
        
        <el-form-item label="服务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入服务描述"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 列设置抽屉 -->
    <el-drawer
      v-model="columnSettingVisible"
      title="列设置"
      direction="rtl"
      size="300px"
    >
      <el-checkbox-group v-model="visibleColumns" class="column-list">
        <el-checkbox v-for="col in allColumns" :key="col.prop" :label="col.prop">
          {{ col.label }}
        </el-checkbox>
      </el-checkbox-group>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as XLSX from 'xlsx'

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const categorySubmitting = ref(false)
const tableData = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const formRef = ref(null)
const categoryFormRef = ref(null)
const selectedRows = ref([])
const columnSettingVisible = ref(false)

// 搜索表单
const searchForm = ref({
  name: '',
  categoryId: '',
  status: ''
})

// 服务表单
const form = ref({
  id: null,
  name: '',
  categoryId: '',
  price: 0,
  duration: 60,
  description: '',
  status: 1
})

// 分类表单
const categoryForm = ref({
  id: null,
  name: '',
  description: '',
  icon: '',
  sort: 0,
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入服务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择服务分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入服务价格', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入服务时长', trigger: 'blur' }
  ]
}

// 分类表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未分类'
  const category = categories.value.find(item => item.id === categoryId)
  return category ? category.name : '未知分类'
}

// 处理持续时间显示
const getDurationText = (minutes) => {
  if (!minutes) return '-'
  
  if (minutes < 60) {
    return `${minutes}分钟`
  } else if (minutes === 60) {
    return '1小时'
  } else if (minutes < 1440) {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  } else {
    const days = Math.floor(minutes / 1440)
    return `${days}天`
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取服务列表
const fetchServices = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      ...searchForm.value
    }
    
    await request.get('/service/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取服务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取服务分类
const fetchCategories = async () => {
  try {
    await request.get('/service/category/list', null, {
      onSuccess: (res) => {
        categories.value = res
      }
    })
  } catch (error) {
    console.error('获取服务分类失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    name: '',
    categoryId: '',
    status: ''
  }
  handleSearch()
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchServices()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchServices()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 新增服务
const handleAdd = () => {
  form.value = {
    id: null,
    name: '',
    categoryId: '',
    price: 0,
    duration: 60,
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

// 编辑服务
const handleEdit = (row) => {
  form.value = JSON.parse(JSON.stringify(row))
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.value = {
    id: null,
    name: '',
    categoryId: '',
    price: 0,
    duration: 60,
    description: '',
    status: 1
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.value.id) {
      // 编辑
      await request.put(`/service/${form.value.id}`, form.value, {
        successMsg: '更新成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchServices()
        }
      })
    } else {
      // 新增
      await request.post('/service', form.value, {
        successMsg: '添加成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchServices()
        }
      })
    }
  } catch (error) {
    console.error('提交服务信息失败:', error)
  } finally {
    submitting.value = false
  }
}

// 更改服务状态
const handleChangeStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${actionText}该服务吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await request.put(`/service/${row.id}/status?status=${newStatus}`, null, {
      successMsg: `${actionText}成功`,
      onSuccess: () => {
        fetchServices()
      }
    })
  } catch (error) {
    console.error(`${actionText}失败:`, error)
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的服务')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个服务吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/service/batch-delete', { ids }, {
      successMsg: '删除成功',
      onSuccess: () => {
        fetchServices()
      }
    })
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 刷新
const handleRefresh = () => {
  fetchServices()
  ElMessage.success('刷新成功')
}

// 导出功能
const handleExport = () => {
  try {
    loading.value = true
    
    // 获取当前可见列的配置
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    // 准备导出数据
    const exportData = tableData.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'category') {
          row[col.label] = getCategoryName(item.categoryId)
        } else if (col.prop === 'duration') {
          row[col.label] = getDurationText(item.duration)
        } else if (col.prop === 'status') {
          row[col.label] = item.status === 1 ? '启用' : '停用'
        } else if (col.prop === 'createTime') {
          row[col.label] = formatDateTime(item.createTime)
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '服务列表')

    // 导出文件
    XLSX.writeFile(workbook, `服务列表_${new Date().toISOString().slice(0, 10)}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 列设置相关代码
const STORAGE_KEY = 'serviceManagementVisibleColumns'
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'name', label: '服务名称' },
  { prop: 'category', label: '服务分类' },
  { prop: 'price', label: '价格' },
  { prop: 'duration', label: '时长' },
  { prop: 'status', label: '状态' },
  { prop: 'createTime', label: '创建时间' }
]

// 从localStorage获取保存的列设置，如果没有则使用默认值
const visibleColumns = ref(
  JSON.parse(localStorage.getItem(STORAGE_KEY)) || allColumns.map(col => col.prop)
)

// 监听列设置变化并保存到localStorage
watch(visibleColumns, (newVal) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal))
}, { deep: true })

const isColumnVisible = (prop) => {
  return visibleColumns.value.includes(prop)
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServices()
})






// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServices()
})
</script>

<style scoped>
.service-management-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header .right {
  display: flex;
  gap: 10px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 搜索表单的表单项间距 */
.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 弹窗表单的表单项间距 */
.dialog-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-select) {
  width: 200px;
}

/* 弹窗表单内的选择器宽度 */
.dialog-form :deep(.el-select) {
  width: 100%;
}

.duration-hint {
  margin-left: 10px;
  color: #909399;
}

.category-list-section {
  margin-bottom: 20px;
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.category-header span {
  font-size: 16px;
  font-weight: bold;
}

/* 列设置样式 */
.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

@media (max-width: 768px) {
  .el-form-item {
    margin-right: 0;
    width: 100%;
  }
}
</style> 