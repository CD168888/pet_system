<template>
  <div class="banner-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入标题" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchBanners">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">轮播图列表</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增轮播图</el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" style="width: 100%" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" min-width="80" />
        <el-table-column v-if="isColumnVisible('title')" prop="title" label="标题" min-width="150" />
        <el-table-column v-if="isColumnVisible('imageUrl')" label="图片" min-width="180">
          <template #default="scope">
            <el-image 
              :src="getImageUrl(scope.row.imageUrl)" 
              fit="cover" 
              style="width: 150px; height: 80px; border-radius: 4px;"
              :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
            />
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('linkUrl')" prop="linkUrl" label="链接" min-width="180" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('sort')" prop="sort" label="排序" min-width="80" />
        <el-table-column v-if="isColumnVisible('status')" label="状态" min-width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="(val) => handleStatusChange(scope.row.id, val)" />
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
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
    
    <!-- 新增/编辑轮播图对话框 -->
    <el-dialog 
      :title="dialogType === 'add' ? '新增轮播图' : '编辑轮播图'" 
      v-model="dialogVisible" 
      width="650px"
      append-to-body
    >
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="bannerFormRef" 
        label-width="100px"
        label-position="right"
        class="dialog-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            class="banner-upload"
            :action="`${baseAPI}/file/upload/img`"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <el-image 
              v-if="form.imageUrl" 
              :src="getImageUrl(form.imageUrl)" 
              class="banner-image" 
            />
            <div v-else class="upload-placeholder">
              <el-icon><Plus /></el-icon>
              <div class="text">点击上传图片</div>
              <div class="tips">建议尺寸: 1920×500像素，格式: JPG/PNG</div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="请输入跳转链接" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
    <el-dialog title="轮播图预览" v-model="previewVisible" width="950px" append-to-body>
      <div class="preview-container">
        <div 
          class="preview-banner" 
          :style="{ 
            backgroundImage: `url(${getImageUrl(previewData.imageUrl)})`,
            backgroundPosition: 'center center'
          }"
        >
          <div class="preview-content" v-if="previewData.title">
            <h2 class="preview-title">{{ previewData.title }}</h2>
          </div>
        </div>
        <div class="preview-info">
          <h3>轮播图信息</h3>
          <div class="info-item">
            <span class="label">标题:</span>
            <span class="value">{{ previewData.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">链接:</span>
            <span class="value">{{ previewData.linkUrl }}</span>
          </div>
          <div class="info-item">
            <span class="label">排序:</span>
            <span class="value">{{ previewData.sort }}</span>
          </div>
          <div class="info-item">
            <span class="label">状态:</span>
            <el-tag :type="previewData.status === 1 ? 'success' : 'info'">
              {{ previewData.status === 1 ? '启用' : '禁用' }}
            </el-tag>
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
  View, 
  Picture, 
  QuestionFilled 
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { format } from '@/utils/dateUtils'
import * as XLSX from 'xlsx'

const userStore = useUserStore()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
const uploadHeaders = computed(() => {
  return {
    token: userStore.token
  }
})

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
  status: ''
})

// 列设置相关代码
const STORAGE_KEY = 'bannerListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'title', label: '标题' },
  { prop: 'imageUrl', label: '图片' },
  { prop: 'linkUrl', label: '链接' },
  { prop: 'sort', label: '排序' },
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

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchBanners()
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 刷新方法
const handleRefresh = () => {
  fetchBanners()
  ElMessage.success('刷新成功')
}

// 批量删除方法
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个轮播图吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/banner/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchBanners()
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
        } else if (col.prop === 'createTime') {
          row[col.label] = formatDateTime(item.createTime)
        } else if (col.prop === 'imageUrl') {
          row[col.label] = getImageUrl(item.imageUrl)
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '轮播图列表')

    // 导出文件
    XLSX.writeFile(workbook, `轮播图列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 获取轮播图列表
const fetchBanners = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      title: searchForm.title || undefined,
      status: searchForm.status ? parseInt(searchForm.status) : undefined,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/banner/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取轮播图列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBanners()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBanners()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // add or edit
const bannerFormRef = ref(null)
const submitLoading = ref(false)

// 表单数据
const form = reactive({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '长度不能超过100个字符', trigger: 'blur' }
  ],
  imageUrl: [
    { required: true, message: '请上传图片', trigger: 'change' }
  ],
  sort: [
    { required: true, message: '请设置排序值', trigger: 'blur' }
  ]
}

// 新增轮播图
const handleAdd = () => {
  dialogType.value = 'add'
  resetForm()
  dialogVisible.value = true
}

// 编辑轮播图
const handleEdit = (row) => {
  dialogType.value = 'edit'
  resetForm()
  
  Object.keys(form).forEach(key => {
    if (row[key] !== undefined) {
      form[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.title = ''
  form.imageUrl = ''
  form.linkUrl = ''
  form.sort = 0
  form.status = 1
  
  if (bannerFormRef.value) {
    bannerFormRef.value.resetFields()
  }
}

// 图片上传前的处理
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  
  return true
}

// 图片上传成功的处理
const handleUploadSuccess = (res, file) => {
  if (res.code === '200') {
    form.imageUrl = res.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败: ' + res.msg)
  }
}

// 处理图片URL，添加API前缀
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }
  return baseAPI + url
}

// 提交表单
const submitForm = () => {
  bannerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          // 新增
          await request.post('/banner', form, {
            successMsg: '轮播图添加成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchBanners()
            }
          })
        } else {
          // 编辑
          await request.put(`/banner/${form.id}`, form, {
            successMsg: '轮播图更新成功',
            onSuccess: () => {
              dialogVisible.value = false
              fetchBanners()
            }
          })
        }
      } catch (error) {
        console.error('提交轮播图表单失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除轮播图
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/banner/${row.id}`, {
        successMsg: '轮播图已删除',
        onSuccess: () => {
          fetchBanners()
        }
      })
    } catch (error) {
      console.error('删除轮播图失败:', error)
    }
  }).catch(() => {})
}

// 切换轮播图状态
const handleStatusChange = async (id, status) => {
  try {
    await request.put(`/banner/${id}/status/${status}`, null, {
      successMsg: `轮播图已${status === 1 ? '启用' : '禁用'}`
    })
  } catch (error) {
    console.error('切换轮播图状态失败:', error)
    // 如果失败，重新获取数据以恢复正确状态
    fetchBanners()
  }
}

// 预览轮播图
const previewVisible = ref(false)
const previewData = reactive({
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
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

onMounted(() => {
  fetchBanners()
})
</script>

<style lang="scss" scoped>
.banner-management {
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

.banner-upload {
  .banner-image {
    width: 100%;
    height: 200px;
    border-radius: 6px;
    object-fit: cover;
  }
  
  .upload-placeholder {
    width: 100%;
    height: 200px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    background-color: #fafafa;
    
    &:hover {
      border-color: #FFB6C1;
      
      .text {
        color: #FFB6C1;
      }
    }
    
    .el-icon {
      font-size: 32px;
      color: #8c939d;
      margin-bottom: 10px;
    }
    
    .text {
      color: #606266;
      font-size: 14px;
      margin-bottom: 5px;
    }
    
    .tips {
      color: #999;
      font-size: 12px;
    }
  }
}

.preview-container {
  .preview-banner {
    height: 300px;
    width: 100%;
    background-size: cover;
    border-radius: 8px;
    position: relative;
    margin-bottom: 20px;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(to right, rgba(0, 0, 0, 0.5) 0%, rgba(0, 0, 0, 0.2) 100%);
      border-radius: 8px;
    }
  }
  
  .preview-content {
    position: absolute;
    left: 10%;
    top: 50%;
    transform: translateY(-50%);
    color: white;
    max-width: 500px;
    z-index: 1;
    text-align: left;
  }
  
  .preview-title {
    font-size: 32px;
    margin: 0 0 10px;
    font-weight: 700;
  }
  
  .preview-info {
    padding: 20px;
    background-color: #f9f9f9;
    border-radius: 8px;
    
    h3 {
      margin: 0 0 15px;
      font-size: 18px;
      color: #333;
      font-weight: 600;
    }
    
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