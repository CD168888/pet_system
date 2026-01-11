<template>
  <div class="announcement-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable>
            <el-option label="通知" value="NOTICE" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="促销" value="PROMOTION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchAnnouncements">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">公告列表</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增公告</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" min-width="80" />
        <el-table-column v-if="isColumnVisible('title')" prop="title" label="标题" min-width="150" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('type')" prop="type" label="类型" min-width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('content')" prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('status')" label="状态" min-width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="(val) => handleStatusChange(scope.row.id, val)" />
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createdTime')" prop="createdTime" label="创建时间" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ formatDateTime(scope.row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('updatedTime')" prop="updatedTime" label="更新时间" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ formatDateTime(scope.row.updatedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              link 
              @click="handlePreview(scope.row)"
              :icon="View"
            >
              预览
            </el-button>
            <el-button 
              size="small" 
              type="primary" 
              link 
              @click="handleEdit(scope.row)" 
              :icon="Edit"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              link 
              @click="handleDelete(scope.row)" 
              :icon="Delete"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑公告对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增公告' : '编辑公告'" 
      v-model="dialogVisible" 
      width="650px"
      @closed="handleDialogClosed"
      append-to-body
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="announcementFormRef" 
        label-width="100px"
        label-position="right"
        class="dialog-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="通知" value="NOTICE" />
            <el-option label="活动" value="ACTIVITY" />
            <el-option label="促销" value="PROMOTION" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入公告内容" 
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 预览对话框 -->
    <el-dialog 
      title="公告预览" 
      v-model="previewVisible" 
      width="650px"
      @closed="handlePreviewClosed"
      append-to-body
    >
      <div class="preview-container">
        <div class="preview-header">
          <h2 class="preview-title">{{ previewData.title }}</h2>
          <div class="preview-meta">
            <el-tag :type="getTypeTagType(previewData.type)" size="small">
              {{ getTypeLabel(previewData.type) }}
            </el-tag>
            <span class="preview-time">发布时间：{{ formatDateTime(previewData.createdTime) }}</span>
          </div>
        </div>
        <div class="preview-content">
          <p class="content-text">{{ previewData.content }}</p>
        </div>
        <div class="preview-info">
          <div class="info-item">
            <span class="label">状态:</span>
            <el-tag :type="previewData.status === 1 ? 'success' : 'info'">
              {{ previewData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">更新时间:</span>
            <span class="value">{{ formatDateTime(previewData.updatedTime) }}</span>
          </div>
        </div>
      </div>
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
import { 
  Search, 
  Refresh, 
  Download, 
  Setting,
  Plus, 
  Edit, 
  Delete, 
  View
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { format } from '@/utils/dateUtils'
import * as XLSX from 'xlsx'

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 表格数据
const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  title: '',
  type: '',
  status: ''
})

// 列设置相关代码
const STORAGE_KEY = 'announcementListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'title', label: '标题' },
  { prop: 'type', label: '类型' },
  { prop: 'content', label: '内容' },
  { prop: 'status', label: '状态' },
  { prop: 'createdTime', label: '创建时间' },
  { prop: 'updatedTime', label: '更新时间' }
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

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchAnnouncements()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 刷新方法
const handleRefresh = () => {
  fetchAnnouncements()
  ElMessage.success('刷新成功')
}

// 批量删除方法
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个公告吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/announcement/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchAnnouncements()
      }
    })
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

// 导出方法
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
          row[col.label] = item.status === 1 ? '启用' : '禁用'
        } else if (col.prop === 'type') {
          row[col.label] = getTypeLabel(item[col.prop])
        } else if (col.prop === 'createdTime' || col.prop === 'updatedTime') {
          row[col.label] = formatDateTime(item[col.prop])
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '公告列表')

    // 导出文件
    XLSX.writeFile(workbook, `公告列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      title: searchForm.title || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status ? parseInt(searchForm.status) : undefined,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/announcement/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAnnouncements()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAnnouncements()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取类型标签样式
const getTypeTagType = (type) => {
  switch (type) {
    case 'NOTICE':
      return 'primary'
    case 'ACTIVITY':
      return 'success'
    case 'PROMOTION':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取类型标签文本
const getTypeLabel = (type) => {
  switch (type) {
    case 'NOTICE':
      return '通知'
    case 'ACTIVITY':
      return '活动'
    case 'PROMOTION':
      return '促销'
    default:
      return '其他'
  }
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add or edit
const announcementFormRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  id: null,
  title: '',
  type: 'NOTICE',
  content: '',
  status: 1
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' }
  ]
}

// 新增公告
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑公告
const handleEdit = (row) => {
  dialogType.value = 'edit'
  
  // 先重置表单，然后设置新数据
  resetForm()
  
  // 复制行数据到表单
  Object.keys(form).forEach(key => {
    if (row[key] !== undefined) {
      form[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  // 完全重置表单数据
  form.id = null
  form.title = ''
  form.type = 'NOTICE'
  form.content = ''
  form.status = 1
  
  // 重置表单验证状态
  if (announcementFormRef.value) {
    announcementFormRef.value.resetFields()
  }
}

// 处理对话框关闭
const handleDialogClosed = () => {
  // 确保对话框关闭时完全重置表单
  form.id = null
  form.title = ''
  form.type = 'NOTICE'
  form.content = ''
  form.status = 1
  
  // 重置对话框类型
  dialogType.value = 'add'
  
  // 延迟重置表单验证状态，确保在DOM更新后执行
  setTimeout(() => {
    if (announcementFormRef.value) {
      announcementFormRef.value.resetFields()
    }
  }, 0)
}

// 提交表单
const submitForm = () => {
  announcementFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          // 新增
          await request.post('/announcement', form, {
            successMsg: '公告添加成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchAnnouncements()
            }
          })
        } else {
          // 编辑
          await request.put(`/announcement/${form.id}`, form, {
            successMsg: '公告更新成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchAnnouncements()
            }
          })
        }
      } catch (error) {
        console.error('提交公告表单失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除公告
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/announcement/${row.id}`, {
        successMsg: '公告已删除',
        onSuccess: () => {
          fetchAnnouncements()
        }
      })
    } catch (error) {
      console.error('删除公告失败:', error)
    }
  }).catch(() => {})
}

// 切换公告状态
const handleStatusChange = async (id, status) => {
  try {
    await request.put(`/announcement/${id}/status/${status}`, null, {
      successMsg: `公告已${status === 1 ? '启用' : '禁用'}`
    })
  } catch (error) {
    console.error('切换公告状态失败:', error)
    // 如果失败，重新获取数据以恢复正确状态
    fetchAnnouncements()
  }
}

// 预览公告
const previewVisible = ref(false)
const previewData = reactive({
  title: '',
  type: '',
  content: '',
  status: 1,
  createdTime: '',
  updatedTime: ''
})

const handlePreview = (row) => {
  Object.keys(previewData).forEach(key => {
    if (row[key] !== undefined) {
      previewData[key] = row[key]
    } else {
      previewData[key] = ''
    }
  })
  
  previewVisible.value = true
}

// 处理预览对话框关闭
const handlePreviewClosed = () => {
  // 重置预览数据
  Object.keys(previewData).forEach(key => {
    previewData[key] = ''
  })
}

onMounted(() => {
  fetchAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcement-management {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .right {
    display: flex;
    gap: 10px;
  }

  .title {
    font-size: 16px;
    font-weight: bold;
  }
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

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

.preview-container {
  .preview-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
    
    .preview-title {
      font-size: 22px;
      color: #333;
      margin: 0 0 10px;
    }
    
    .preview-meta {
      display: flex;
      align-items: center;
      gap: 15px;
      
      .preview-time {
        color: #999;
        font-size: 14px;
      }
    }
  }
  
  .preview-content {
    padding: 15px;
    background-color: #f9f9f9;
    border-radius: 8px;
    margin-bottom: 20px;
    min-height: 150px;
    
    .content-text {
      margin: 0;
      white-space: pre-wrap;
      line-height: 1.6;
    }
  }
  
  .preview-info {
    .info-item {
      margin-bottom: 10px;
      display: flex;
      
      .label {
        width: 80px;
        font-weight: 600;
        color: #666;
      }
      
      .value {
        color: #333;
        flex: 1;
      }
    }
  }
}
</style> 