<template>
  <div class="boarding-manage-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="宠物名称">
          <el-input v-model="searchForm.petName" placeholder="宠物名称" clearable />
        </el-form-item>
        <el-form-item label="宠物类型">
          <el-cascader 
            v-model="searchForm.petType" 
            :options="categoriesTree"
            :props="{
              checkStrictly: true,
              value: 'id',
              label: 'name',
              emitPath: false
            }"
            placeholder="宠物类型" 
            clearable 
            filterable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="寄养状态">
          <el-select v-model="searchForm.status" placeholder="寄养状态" clearable>
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏和表格 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">寄养管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" :loading="refreshLoading" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-button type="primary" @click="handleAdd">添加寄养服务</el-button>
          </div>
        </div>
      </template>

      <!-- 数据加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>

      <!-- 错误状态 -->
      <div v-else-if="loadError" class="error-container">
        <el-result
          icon="error"
          title="数据加载失败"
          sub-title="无法获取寄养信息，请检查网络连接或稍后重试"
        >
          <template #extra>
            <el-button type="primary" @click="handleRefresh">重新加载</el-button>
          </template>
        </el-result>
      </div>

      <!-- 空数据状态 -->
      <div v-else-if="boardingList.length === 0" class="empty-container">
        <el-empty 
          description="暂无寄养信息" 
          :image-size="200"
        >
          <template #description>
            <p>系统中尚未添加任何寄养信息</p>
            <p class="empty-hint">您可以点击"添加寄养服务"按钮创建新的寄养记录</p>
          </template>
          <el-button type="primary" @click="handleAdd">添加寄养服务</el-button>
        </el-empty>
      </div>

      <!-- 数据展示 -->
      <el-table
        v-else
        :data="boardingList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80" />
        <el-table-column v-if="isColumnVisible('petName')" prop="petName" label="宠物名称" min-width="120" />
        <el-table-column v-if="isColumnVisible('petType')" prop="petType" label="宠物类型" min-width="100">
          <template #default="scope">
            <span>{{ scope.row.petType }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('startTime')" label="开始时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('endTime')" label="结束时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" label="寄养状态" min-width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('price')" prop="price" label="价格" min-width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('userName')" prop="userName" label="用户" min-width="120">
          <template #default="scope">
            <el-tooltip 
              v-if="scope.row.userName" 
              :content="`用户ID: ${scope.row.userId}`" 
              placement="top"
            >
              <span>{{ scope.row.userName }}</span>
            </el-tooltip>
            <span v-else class="no-data">暂无用户</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('requirements')" prop="requirements" label="特殊要求" min-width="200" :show-overflow-tooltip="true">
          <template #default="scope">
            <span v-if="scope.row.requirements">{{ scope.row.requirements }}</span>
            <span v-else class="no-data">无特殊要求</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
            <el-button 
              v-if="scope.row.status === '已申请'"
              type="success" 
              size="small" 
              @click="handleUpdateStatus(scope.row, '已接受')"
            >
              接受
            </el-button>
            <el-button 
              v-if="scope.row.status === '已接受'"
              type="warning" 
              size="small" 
              @click="handleUpdateStatus(scope.row, '进行中')"
            >
              开始
            </el-button>
            <el-button 
              v-if="scope.row.status === '进行中'"
              type="success" 
              size="small" 
              @click="handleUpdateStatus(scope.row, '已完成')"
            >
              完成
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" v-if="boardingList.length > 0">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
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

    <!-- 添加/编辑寄养服务对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑寄养服务' : '添加寄养服务'" 
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form :model="boardingForm" label-width="100px" :rules="boardingRules" ref="boardingFormRef">
        <el-form-item label="宠物名称" prop="petName">
          <el-input v-model="boardingForm.petName" placeholder="请输入宠物名称" />
        </el-form-item>
        <el-form-item label="宠物类型" prop="petType">
          <template v-if="isEdit">
            <el-input v-model="boardingForm.petType" disabled />
          </template>
          <template v-else>
            <el-cascader 
              v-model="boardingForm.petType" 
              :options="categoriesTree"
              :props="{
                checkStrictly: true,
                value: 'id',
                label: 'name',
                emitPath: false
              }"
              placeholder="请选择宠物类型" 
              clearable 
              filterable
              style="width: 100%"
              @change="handleCategoryChange"
            />
          </template>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="boardingForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="boardingForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="boardingForm.price" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="寄养状态" prop="status">
          <el-select v-model="boardingForm.status" placeholder="请选择寄养状态" style="width: 100%">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户ID" prop="userId" v-if="isEdit">
          <el-input v-model="boardingForm.userId" placeholder="用户ID" :disabled="true" />
        </el-form-item>
        <el-form-item label="特殊要求" prop="requirements">
          <el-input v-model="boardingForm.requirements" type="textarea" rows="4" placeholder="请输入特殊要求" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitBoarding" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/dateUtils'
import * as XLSX from 'xlsx'
import { format } from '@/utils/dateUtils'
import request from '@/utils/request'

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 寄养列表数据
const boardingList = ref([])
const loading = ref(false)
const refreshLoading = ref(false)
const loadError = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusOptions = ['已申请', '已接受', '进行中', '已完成', '已取消']
const selectedRows = ref([])

// 添加分类树相关数据
const categoriesTree = ref([])
const categoriesList = ref([])

// 搜索表单
const searchForm = reactive({
  petName: '',
  petType: null,
  status: ''
})

// 列设置相关代码
const STORAGE_KEY = 'boardingListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'petName', label: '宠物名称' },
  { prop: 'petType', label: '宠物类型' },
  { prop: 'startTime', label: '开始时间' },
  { prop: 'endTime', label: '结束时间' },
  { prop: 'status', label: '寄养状态' },
  { prop: 'price', label: '价格' },
  { prop: 'userName', label: '用户' },
  { prop: 'requirements', label: '特殊要求' },
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

// 寄养表单
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const boardingFormRef = ref(null)

const boardingForm = reactive({
  id: null,
  petName: '',
  petType: null,
  startTime: '',
  endTime: '',
  status: '已申请',
  price: 100,
  userId: null,
  requirements: ''
})

// 表单验证规则
const boardingRules = {
  petName: [
    { required: true, message: '请输入宠物名称', trigger: 'blur' }
  ],
  petType: [
    { 
      required: true, 
      message: '请选择宠物类型', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        // 编辑模式下不验证petType (因为显示的是disabled输入框)
        if (isEdit.value) {
          callback()
        } else if (!value) {
          callback(new Error('请选择宠物类型'))
        } else {
          callback()
        }
      }
    }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于0', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择寄养状态', trigger: 'change' }
  ]
}

// 获取宠物分类列表和树形结构
const fetchCategories = async () => {
  try {
    await request.get('/pet-category/tree', {}, {
      onSuccess: (res) => {
        categoriesTree.value = res
        // 同时保留平铺的分类列表
        flattenCategories(res)
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 将树形结构扁平化
const flattenCategories = (tree) => {
  categoriesList.value = []
  const flatten = (items) => {
    items.forEach(item => {
      categoriesList.value.push({
        id: item.id,
        name: item.name
      })
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  flatten(tree)
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '未设置'
  
  const category = categoriesList.value.find(cat => cat.id === categoryId)
  return category ? category.name : '未知分类'
}

// 获取寄养列表
const fetchBoardings = async () => {
  loading.value = true
  loadError.value = false
  // 确保初始化为空数组而不是undefined
  boardingList.value = []
  
  try {
    await request.get('/boarding/page', {
      petName: searchForm.petName,
      petType: searchForm.petType,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          console.log('寄养记录数据:', res.records);
          // 确保日期格式正确 - DateUtils类已内置LocalDateTime处理
          boardingList.value = res.records
          total.value = res.total || 0
        } else {
          // 如果没有数据，设置为空数组
          boardingList.value = []
          total.value = 0
        }
      },
      showDefaultMsg: false
    })
  } catch (error) {
    console.error('获取寄养列表失败:', error)
    // 确保在出错时也将列表设置为空数组
    boardingList.value = []
    total.value = 0
    loadError.value = true
    ElMessage.error('获取寄养列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 监听宠物类型变化
const handleCategoryChange = (categoryId) => {
  if (categoryId) {
    // 在分类列表中查找对应的分类名称
    const category = categoriesList.value.find(cat => cat.id === categoryId)
    const categoryName = category ? category.name : ''
    
    // 直接将分类名称赋值给petType
    boardingForm.petType = categoryName
    
    // 根据分类名称调整价格建议
    adjustPriceByCategory(categoryName)
  } else {
    boardingForm.petType = ''
  }
}

// 根据宠物类型调整价格建议
const adjustPriceByCategory = (categoryName) => {
  switch (categoryName) {
    case '猫':
      boardingForm.price = 80
      break
    case '狗':
      boardingForm.price = 120
      break
    case '兔子':
      boardingForm.price = 50
      break
    case '鸟类':
      boardingForm.price = 40
      break
    default:
      boardingForm.price = 60
  }
}

// 添加 watch 监听 petType 变化
watch(() => boardingForm.petType, (newVal) => {
  handleCategoryChange(newVal)
})

// 手动刷新数据
const handleRefresh = () => {
  refreshLoading.value = true
  fetchBoardings().finally(() => {
    refreshLoading.value = false
  })
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchBoardings()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  searchForm.petType = null
  currentPage.value = 1
  fetchBoardings()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchBoardings()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchBoardings()
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个寄养记录吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/boarding/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchBoardings()
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
    
    // 准备导出数据
    const exportData = boardingList.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'startTime' || col.prop === 'endTime' || col.prop === 'createTime') {
          row[col.label] = formatDate(item[col.prop])
        } else {
          row[col.label] = item[col.prop] || ''
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '寄养记录列表')

    // 导出文件
    XLSX.writeFile(workbook, `寄养记录列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 添加寄养服务
const handleAdd = () => {
  isEdit.value = false
  resetBoardingForm()
  dialogVisible.value = true
}

// 编辑寄养服务
const handleEdit = (row) => {
  isEdit.value = true
  resetBoardingForm()
  
  // 复制数据到表单
  Object.keys(boardingForm).forEach(key => {
    if (key in row) {
      boardingForm[key] = row[key]
    }
  })
  
  dialogVisible.value = true
}

// 删除寄养服务
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该寄养服务吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/boarding/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchBoardings()
        }
      })
    } catch (error) {
      console.error('删除寄养服务失败:', error)
    }
  }).catch(() => {})
}

// 更新寄养状态
const handleUpdateStatus = (row, newStatus) => {
  let confirmMessage = '确定要将状态更新为"' + newStatus + '"吗?'
  
  ElMessageBox.confirm(confirmMessage, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/boarding/${row.id}/status`, null, {
        params: {
          status: newStatus
        },
        successMsg: '状态更新成功',
        onSuccess: () => {
          fetchBoardings()
        }
      })
    } catch (error) {
      console.error('更新寄养状态失败:', error)
    }
  }).catch(() => {})
}

// 重置寄养表单
const resetBoardingForm = () => {
  boardingForm.id = null
  boardingForm.petName = ''
  boardingForm.petType = null
  boardingForm.startTime = ''
  boardingForm.endTime = ''
  boardingForm.status = '已申请'
  boardingForm.price = 100
  boardingForm.userId = null
  boardingForm.requirements = ''
}

// 提交寄养表单
const submitBoarding = () => {
  boardingFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证结束时间是否大于开始时间
    if (new Date(boardingForm.endTime) <= new Date(boardingForm.startTime)) {
      ElMessage.error('结束时间必须大于开始时间')
      return
    }
    
    // 验证价格是否已填写且大于0
    if (!boardingForm.price || boardingForm.price <= 0) {
      ElMessage.error('请填写有效的寄养价格')
      return
    }
    
    submitLoading.value = true
    
    try {
      if (isEdit.value) {
        // 编辑
        await request.put('/boarding', boardingForm, {
          successMsg: '更新成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchBoardings()
          }
        })
      } else {
        // 添加
        await request.post('/boarding', boardingForm, {
          successMsg: '添加成功',
          onSuccess: () => {
            dialogVisible.value = false
            fetchBoardings()
          }
        })
      }
    } catch (error) {
      console.error('保存寄养服务失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 获取状态类型
const getStatusType = (status) => {
  switch(status) {
    case '已申请': return 'info'
    case '已接受': return 'success'
    case '进行中': return 'warning'
    case '已完成': return 'success'
    case '已取消': return 'danger'
    default: return ''
  }
}

// 处理关闭对话框
const handleDialogClose = (done) => {
  boardingFormRef.value.resetFields()
  done()
}

onMounted(() => {
  fetchCategories()
  fetchBoardings()
})
</script>

<style scoped>
.boarding-manage-container {
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

.column-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px;
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

.no-data {
  color: #909399;
  font-style: italic;
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