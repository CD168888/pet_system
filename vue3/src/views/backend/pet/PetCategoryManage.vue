<template>
  <div class="pet-category-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="分类名称" clearable />
        </el-form-item>
        <el-form-item label="父分类">
          <el-input v-model="searchForm.parentId" placeholder="请输入父分类ID" clearable type="number" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="状态" clearable>
            <el-option :label="'启用'" :value="1" />
            <el-option :label="'禁用'" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏和表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">宠物分类管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" :loading="refreshLoading" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd">添加分类</el-button>
          </div>
        </div>
      </template>

      <!-- 数据加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>

      <!-- 错误状态 -->
      <div v-else-if="loadError" class="error-container">
        <el-result
          icon="error"
          title="数据加载失败"
          sub-title="无法获取分类信息，请检查网络连接或稍后重试"
        >
          <template #extra>
            <el-button type="primary" @click="handleRefresh">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <!-- 空数据状态 -->
      <div v-else-if="categoryList.length === 0" class="empty-container">
        <el-empty 
          description="暂无分类信息" 
          :image-size="200"
        >
          <template #description>
            <p>系统中尚未添加任何分类信息</p>
            <p class="empty-hint">您可以点击"添加分类"按钮创建新的分类</p>
          </template>
          <el-button type="primary" @click="handleAdd">添加分类</el-button>
        </el-empty>
      </div>

      <!-- 数据展示 -->
      <el-table
        v-else
        :data="categoryList"
        border
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80" />
        <el-table-column v-if="isColumnVisible('name')" prop="name" label="分类名称" min-width="150" />
        <el-table-column v-if="isColumnVisible('description')" prop="description" label="描述" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column v-if="isColumnVisible('sort')" prop="sort" label="排序" min-width="80" />
        <el-table-column v-if="isColumnVisible('status')" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="380" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleStatusChange(scope.row)"
              :disabled="scope.row.id === 1"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="success" size="small" @click="handleAddChild(scope.row)">添加子分类</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="categoryList.length > 0 && !isTreeData">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑分类' : (isAddChild ? '添加子分类' : '添加分类')" 
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form :model="categoryForm" label-width="100px" :rules="categoryRules" ref="categoryFormRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId" v-if="!isAddChild">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父分类" clearable style="width: 100%">
            <el-option label="无父分类" :value="0" />
            <el-option 
              v-for="item in parentCategories" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
              :disabled="isEdit && item.id === categoryForm.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" style="width: 120px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCategory" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime as formatDateTimeUtil } from '@/utils/dateUtils'
import request from '@/utils/request'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { format } from '@/utils/dateUtils'

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 分类列表数据
const categoryList = ref([])
const parentCategories = ref([])
const loading = ref(false)
const loadError = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const isTreeData = ref(false)
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  parentId: '',
  status: ''
})

// 分类表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const isAddChild = ref(false)
const submitLoading = ref(false)
const categoryFormRef = ref(null)

const categoryForm = reactive({
  id: null,
  name: '',
  parentId: 0,
  description: '',
  sort: 0,
  status: 1
})

// 分类表单验证规则
const categoryRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 添加刷新功能
const refreshLoading = ref(false)

// 修改列设置相关代码
const STORAGE_KEY = 'petCategoryVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'name', label: '分类名称' },
  { prop: 'description', label: '描述' },
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

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  loadError.value = false
  
  try {
    // 如果有搜索条件，使用分页模式
    if (searchForm.name || searchForm.parentId !== '' || searchForm.status !== '') {
      isTreeData.value = false;
      
      await request.get('/pet-category/page', {
        name: searchForm.name,
        parentId: searchForm.parentId === '' ? null : searchForm.parentId,
        status: searchForm.status === '' ? null : searchForm.status,
        currentPage: currentPage.value,
        size: pageSize.value
      }, {
        onSuccess: (res) => {
          categoryList.value = res.records
          total.value = res.total
        }
      })
    } else {
      // 没有搜索条件，使用树形结构
      isTreeData.value = true;
      
      await request.get('/pet-category/all', {}, {
        onSuccess: (res) => {
          // 转换为树形结构
          categoryList.value = buildTree(res)
          total.value = res.length
        }
      })
    }
    
    // 获取所有顶级分类供选择
    await fetchParentCategories()
  } catch (error) {
    console.error('获取分类列表失败:', error)
    categoryList.value = []
    total.value = 0
    loadError.value = true
    ElMessage.error('获取分类列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 构建树形结构
const buildTree = (data) => {
  // 先找出所有的顶级节点
  const rootNodes = data.filter(item => !item.parentId || item.parentId === 0)
  // 递归构建子树
  const buildChildren = (nodes) => {
    return nodes.map(node => {
      const children = data.filter(item => item.parentId === node.id)
      if (children.length > 0) {
        return {
          ...node,
          children: buildChildren(children)
        }
      }
      return node
    })
  }
  
  return buildChildren(rootNodes)
}

// 获取父分类列表
const fetchParentCategories = async () => {
  try {
    await request.get('/pet-category/all-enabled', {}, {
      onSuccess: (res) => {
        parentCategories.value = res
      }
    })
  } catch (error) {
    console.error('获取父分类列表失败:', error)
    ElMessage.error('获取父分类列表失败')
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchCategories()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchCategories()
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchCategories()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchCategories()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 添加分类
const handleAdd = () => {
  isEdit.value = false
  isAddChild.value = false
  resetCategoryForm()
  dialogVisible.value = true
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个分类吗？删除后无法恢复，且会同时删除其下所有子分类。`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/pet-category/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchCategories()
      }
    })
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

// 优化导出功能
const handleExport = () => {
  try {
    loading.value = true
    
    // 获取当前可见列的配置
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    // 准备导出数据 - 需要将树形数据展平
    const flatData = []
    const flattenData = (items, level = 0) => {
      items.forEach(item => {
        const row = { ...item }
        delete row.children
        flatData.push(row)
        if (item.children && item.children.length > 0) {
          flattenData(item.children, level + 1)
        }
      })
    }
    flattenData(categoryList.value)
    
    // 准备导出数据
    const exportData = flatData.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'createTime') {
          row[col.label] = formatDateTime(item[col.prop])
        } else if (col.prop === 'status') {
          row[col.label] = item[col.prop] === 1 ? '启用' : '禁用'
        } else {
          row[col.label] = item[col.prop] || ''
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '宠物分类列表')

    // 导出文件
    XLSX.writeFile(workbook, `宠物分类列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 添加子分类
const handleAddChild = (row) => {
  isEdit.value = false
  isAddChild.value = true
  resetCategoryForm()
  categoryForm.parentId = row.id
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  isEdit.value = true
  isAddChild.value = false
  resetCategoryForm()
  
  // 复制数据到表单
  Object.keys(categoryForm).forEach(key => {
    if (key in row) {
      categoryForm[key] = row[key]
    }
  })
  
  // 如果parentId为null，设置为0
  if (categoryForm.parentId === null) {
    categoryForm.parentId = 0
  }
  
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该分类吗? 删除后无法恢复，且会同时删除其下所有子分类。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/pet-category/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchCategories()
        }
      })
    } catch (error) {
      console.error('删除分类失败:', error)
    }
  }).catch(() => {})
}

// 更改分类状态
const handleStatusChange = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const statusText = newStatus === 1 ? '启用' : '禁用'
  
  ElMessageBox.confirm(`确定要${statusText}该分类吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/pet-category/${row.id}/status/${newStatus}`, null, {
        successMsg: `${statusText}成功`,
        onSuccess: () => {
          fetchCategories()
        }
      })
    } catch (error) {
      console.error(`${statusText}分类失败:`, error)
    }
  }).catch(() => {})
}

// 重置分类表单
const resetCategoryForm = () => {
  categoryForm.id = null
  categoryForm.name = ''
  categoryForm.parentId = 0
  categoryForm.description = ''
  categoryForm.sort = 0
  categoryForm.status = 1
}

// 提交分类表单
const submitCategory = () => {
  categoryFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    
    try {
      if (isEdit.value) {
        // 编辑
        await request.put('/pet-category', categoryForm, {
          successMsg: '更新成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchCategories()
          }
        })
      } else {
        // 添加
        await request.post('/pet-category', categoryForm, {
          successMsg: '添加成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchCategories()
          }
        })
      }
    } catch (error) {
      console.error('保存分类失败:', error)
      ElMessage.error('保存分类失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 处理关闭对话框
const handleDialogClose = (done) => {
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
  done()
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  return formatDateTimeUtil(dateTimeStr)
}

// 手动刷新数据
const handleRefresh = () => {
  refreshLoading.value = true
  fetchCategories().finally(() => {
    refreshLoading.value = false
  })
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.pet-category-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
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

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.loading-container,
.error-container,
.empty-container {
  padding: 50px 0;
  background-color: #f9f9f9;
  border-radius: 4px;
  margin: 20px 0;
}

.error-container {
  background-color: #fff0f0;
}

.empty-hint {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
}

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
}

/* 搜索表单的表单项间距 */
.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .el-form-item {
    margin-right: 0;
    width: 100%;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .card-header .right {
    flex-wrap: wrap;
    width: 100%;
  }
  
  .card-header .right .el-button {
    flex: 1;
    min-width: 120px;
  }
}
</style> 