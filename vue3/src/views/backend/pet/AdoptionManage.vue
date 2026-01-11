<template>
  <div class="adoption-manage-container">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="宠物ID">
          <el-input v-model="searchForm.petId" placeholder="宠物ID" clearable />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="用户ID" clearable />
        </el-form-item>
        <el-form-item label="申请状态">
          <el-select v-model="searchForm.status" placeholder="申请状态" clearable>
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
            <span class="title">领养管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
          </div>
        </div>
      </template>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="adoptionList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80" />
        <el-table-column v-if="isColumnVisible('userId')" prop="userId" label="用户ID" min-width="100" />
        <el-table-column v-if="isColumnVisible('petId')" prop="petId" label="宠物ID" min-width="100" />
        <el-table-column v-if="isColumnVisible('applyReason')" prop="applyReason" label="申请理由" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column v-if="isColumnVisible('contactPhone')" prop="contactPhone" label="联系电话" min-width="120" />
        <el-table-column v-if="isColumnVisible('address')" prop="address" label="联系地址" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column v-if="isColumnVisible('status')" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" prop="createTime" label="申请时间" min-width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === '已申请' || scope.row.status === '审核中'"
              type="success" 
              size="small" 
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === '已申请' || scope.row.status === '审核中'"
              type="danger" 
              size="small" 
              @click="handleReject(scope.row)"
            >
              拒绝
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import { formatDate } from '@/utils/dateUtils'
import * as XLSX from 'xlsx'
import { format } from '@/utils/dateUtils'
import request from '@/utils/request'

const router = useRouter()

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

const loading = ref(false)
const adoptionList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusOptions = ['已申请', '审核中', '已通过', '已拒绝']
const selectedRows = ref([])

// 搜索表单
const searchForm = reactive({
  petId: '',
  userId: '',
  status: ''
})

// 列设置相关代码
const STORAGE_KEY = 'adoptionListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'userId', label: '用户ID' },
  { prop: 'petId', label: '宠物ID' },
  { prop: 'applyReason', label: '申请理由' },
  { prop: 'contactPhone', label: '联系电话' },
  { prop: 'address', label: '联系地址' },
  { prop: 'status', label: '状态' },
  { prop: 'createTime', label: '申请时间' }
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

// 获取领养申请列表
const fetchAdoptions = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    // 添加筛选条件
    if (searchForm.petId) params.petId = searchForm.petId
    if (searchForm.userId) params.userId = searchForm.userId
    if (searchForm.status) params.status = searchForm.status
    
    await request.get('/adoption/page', params, {
      onSuccess: (res) => {
        adoptionList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取领养申请列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAdoptions()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchAdoptions()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAdoptions()
}

// 处理分页大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchAdoptions()
}

// 刷新数据
const handleRefresh = () => {
  fetchAdoptions()
  ElMessage.success('刷新成功')
}

// 优化导出功能
const handleExport = () => {
  try {
    loading.value = true
    
    // 获取当前可见列的配置
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    // 准备导出数据
    const exportData = adoptionList.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'createTime') {
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
    XLSX.utils.book_append_sheet(workbook, worksheet, '领养申请列表')

    // 导出文件
    XLSX.writeFile(workbook, `领养申请列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 通过申请
const handleApprove = (row) => {
  ElMessageBox.confirm('确定要通过该申请吗? 通过后该宠物将被标记为已领养，其他申请将被自动拒绝。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/adoption/${row.id}/review`, null, {
        params: {
          status: '已通过'
        },
        successMsg: '申请已通过',
        onSuccess: () => {
          fetchAdoptions() // 刷新列表
        }
      })
    } catch (error) {
      console.error('审核申请失败:', error)
    }
  }).catch(() => {})
}

// 拒绝申请
const handleReject = (row) => {
  ElMessageBox.confirm('确定要拒绝该申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/adoption/${row.id}/review`, null, {
        params: {
          status: '已拒绝'
        },
        successMsg: '申请已拒绝',
        onSuccess: () => {
          fetchAdoptions() // 刷新列表
        }
      })
    } catch (error) {
      console.error('审核申请失败:', error)
    }
  }).catch(() => {})
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '已申请':
      return 'info'
    case '审核中':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化日期时间
const formatDateTime = (row, column, cellValue) => {
  return formatDate(cellValue)
}

onMounted(() => {
  fetchAdoptions()
})
</script>

<style scoped>
.adoption-manage-container {
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