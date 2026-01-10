<template>
  <div class="training-course-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.name" placeholder="课程名称" clearable />
        </el-form-item>
        <el-form-item label="课程分类">
          <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable style="width: 200px">
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :disabled="category.status === 0"
              :label="category.name" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="课程状态" clearable style="width: 200px">
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
            <span class="title">训练课程管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd">新增课程</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80" />
        <el-table-column v-if="isColumnVisible('name')" prop="name" label="课程名称" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('category')" prop="category" label="分类" min-width="100" />
        <el-table-column v-if="isColumnVisible('price')" prop="price" label="价格(¥)" min-width="100">
          <template #default="scope">
            {{ formatPrice(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('duration')" prop="duration" label="时长(分钟)" min-width="100" />
        <el-table-column v-if="isColumnVisible('maxParticipants')" prop="maxParticipants" label="最大人数" min-width="100" />
        <el-table-column v-if="isColumnVisible('status')" prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" prop="createTime" label="创建时间" min-width="150">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button :type="scope.row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(scope.row)">
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
    
    <!-- 课程表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="650px"
      destroy-on-close
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        
        <el-form-item label="课程分类" prop="categoryId">
          <el-select v-model="courseForm.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.name" 
              :disabled="category.status === 0"
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程价格" prop="price">
              <el-input-number
                v-model="courseForm.price"
                :min="0"
                :precision="2"
                :step="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程时长" prop="duration">
              <el-input-number
                v-model="courseForm.duration"
                :min="1"
                :precision="0"
                :step="15"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="最大参与人数" prop="maxParticipants">
          <el-input-number
            v-model="courseForm.maxParticipants"
            :min="1"
            :max="100"
            :precision="0"
            :step="1"
            style="width: 40%"
          />
        </el-form-item>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 课程表单对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="650px"
      @close="resetForm"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="rules"
        label-width="100px"
        class="dialog-form"
      >
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="课程分类" prop="categoryId">
          <el-select v-model="courseForm.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.name" 
              :disabled="category.status === 0"
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程价格" prop="price">
              <el-input-number
                v-model="courseForm.price"
                :min="0"
                :precision="2"
                :step="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程时长" prop="duration">
              <el-input-number
                v-model="courseForm.duration"
                :min="1"
                :precision="0"
                :step="15"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="最大参与人数" prop="maxParticipants">
          <el-input-number
            v-model="courseForm.maxParticipants"
            :min="1"
            :max="100"
            :precision="0"
            :step="1"
            style="width: 40%"
          />
        </el-form-item>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="courseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分类管理对话框 -->
    <el-dialog
      title="训练课程分类管理"
      v-model="categoryDialogVisible"
      width="500px"
    >
      <div class="category-container">
        <div class="category-form">
          <el-form :inline="true" :model="categoryForm" class="demo-form-inline">
            <el-form-item label="分类名称" prop="name">
              <el-input v-model="categoryForm.name" placeholder="分类名称" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitCategory">
                {{ categoryForm.id ? '更新' : '添加' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <el-divider />
        
        <div class="category-list">
          <el-table :data="categories" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="分类名称" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="editCategory(scope.row)"
                  plain
                >
                  编辑
                </el-button>
                <el-popconfirm
                  confirm-button-text="确定"
                  cancel-button-text="取消"
                  title="确定删除该分类吗？"
                  @confirm="deleteCategory(scope.row.id)"
                >
                  <template #reference>
                    <el-button 
                      type="danger" 
                      size="small" 
                      plain
                    >
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">关闭</el-button>
        </span>
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
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
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const courseFormRef = ref(null)
const categories = ref([])
const selectedRows = ref([])

// 对话框标题
const dialogTitle = computed(() => {
  return courseForm.id ? '编辑课程' : '新增课程'
})

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: '',
  status: ''
})

// 课程表单
const courseForm = reactive({
  id: '',
  name: '',
  categoryId: '',
  price: 0,
  duration: 60,
  maxParticipants: 1,
  description: '',
  status: 1
})

// 分类表单
const categoryForm = reactive({
  id: '',
  name: '',
  status: 1 // 默认启用
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入课程价格', trigger: 'blur' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  maxParticipants: [
    { required: true, message: '请输入最大参与人数', trigger: 'blur' }
  ]
}

// 加载课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    
    await request.get('/training/course/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const fetchCategories = async () => {
  try {
    await request.get('/training/category/list', null, {
      onSuccess: (res) => {
        categories.value = res || []
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCourses()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  handleSearch()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchCourses()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchCourses()
}

// 新增课程
const handleAdd = () => {
  dialogTitle.value = '新增课程'
  dialogVisible.value = true
  resetForm()
}

// 编辑课程
const handleEdit = (row) => {
  dialogTitle.value = '编辑课程'
  Object.assign(courseForm, row)
  dialogVisible.value = true
}

// 删除课程
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该课程吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/training/course/${row.id}`, {
      successMsg: '课程已删除',
      onSuccess: () => {
        fetchCourses()
      }
    })
  } catch (error) {
    console.error('删除课程失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
    const isEdit = !!courseForm.id
    const api = isEdit ? `/training/course/${courseForm.id}` : '/training/course'
    const method = isEdit ? 'put' : 'post'
    courseForm.createTime = null
    courseForm.updateTime = null
    
    await request[method](api, courseForm, {
      successMsg: isEdit ? '课程更新成功' : '课程添加成功',
      onSuccess: () => {
        dialogVisible.value = false
        fetchCourses()
      }
    })
  } catch (error) {
    console.error('保存课程失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  if (courseFormRef.value) {
    courseFormRef.value.resetFields()
  }
  Object.assign(courseForm, {
    id: '',
    name: '',
    categoryId: '',
    price: 0,
    duration: 60,
    maxParticipants: 1,
    description: '',
    status: 1
  })
}

// 切换课程状态
const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '停用'
  
  try {
    await request.put(`/training/course/${row.id}/status?status=${newStatus}`, null, {
      successMsg: `课程已${statusText}`,
      onSuccess: () => {
        row.status = newStatus
      }
    })
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 刷新
const handleRefresh = () => {
  fetchCourses()
  ElMessage.success('刷新成功')
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个课程吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/training/course/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchCourses()
      }
    })
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

// 打开分类管理对话框
const openCategoryDialog = () => {
  resetCategoryForm()
  categoryDialogVisible.value = true
}

// 编辑分类
const editCategory = (row) => {
  Object.assign(categoryForm, row)
}

// 提交分类
const submitCategory = async () => {
  if (!categoryForm.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  try {
    const isEdit = !!categoryForm.id
    const api = isEdit ? `/training/category/${categoryForm.id}` : '/training/category'
    const method = isEdit ? 'put' : 'post'
    
    await request[method](api, categoryForm, {
      successMsg: isEdit ? '分类更新成功' : '分类添加成功',
      onSuccess: () => {
        resetCategoryForm()
        fetchCategories()
      }
    })
  } catch (error) {
    console.error('保存分类失败:', error)
  }
}

// 删除分类
const deleteCategory = async (id) => {
  try {
    await request.delete(`/training/category/${id}`, null, {
      successMsg: '分类已删除',
      onSuccess: () => {
        fetchCategories()
      }
    })
  } catch (error) {
    console.error('删除分类失败:', error)
  }
}

// 重置分类表单
const resetCategoryForm = () => {
  Object.assign(categoryForm, {
    id: '',
    name: '',
    status: 1 // 默认启用
  })
}

// 列设置相关代码
const STORAGE_KEY = 'trainingCourseVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'name', label: '课程名称' },
  { prop: 'category', label: '分类' },
  { prop: 'price', label: '价格(¥)' },
  { prop: 'duration', label: '时长(分钟)' },
  { prop: 'maxParticipants', label: '最大人数' },
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
        if (col.prop === 'status') {
          row[col.label] = item.status === 1 ? '启用' : '停用'
        } else if (col.prop === 'price') {
          row[col.label] = formatPrice(item.price)
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
    XLSX.utils.book_append_sheet(workbook, worksheet, '训练课程列表')

    // 导出文件
    XLSX.writeFile(workbook, `训练课程列表_${new Date().toISOString().slice(0, 10)}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化价格
const formatPrice = (price) => {
  return price.toFixed(2)
}

// 页面加载时获取数据
onMounted(() => {
  fetchCourses()
  fetchCategories()
})</script>

<style scoped>
.training-course-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
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

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
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

.category-container {
  padding: 10px;
}

.category-form {
  margin-bottom: 20px;
}

.category-list {
  margin-top: 20px;
}

:deep(.el-table .cell) {
  white-space: nowrap;
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}
</style>