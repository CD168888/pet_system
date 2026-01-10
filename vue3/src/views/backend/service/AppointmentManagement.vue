<template>
  <div class="appointment-management-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="服务名称">
          <el-input v-model="searchForm.serviceName" placeholder="服务名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="宠物名称">
          <el-input v-model="searchForm.petName" placeholder="宠物名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="searchForm.contactPhone" placeholder="联系电话" clearable></el-input>
        </el-form-item>
        <el-form-item label="预约状态">
          <el-select v-model="searchForm.status" placeholder="预约状态" clearable>
            <el-option value="已预约" label="待确认"></el-option>
            <el-option value="已确认" label="已确认"></el-option>
            <el-option value="已完成" label="已完成"></el-option>
            <el-option value="已取消" label="已取消"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card shadow="never" v-loading="loading" class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">服务预约管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="handleDateRangeChange"
              style="width: 300px; margin-left: 10px;"
            ></el-date-picker>
          </div>
        </div>
      </template>
      
      <el-table :data="tableData" border style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column v-if="isColumnVisible('serviceName')" prop="serviceName" label="服务名称" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column v-if="isColumnVisible('petName')" prop="petName" label="宠物名称" min-width="100"></el-table-column>
        <el-table-column v-if="isColumnVisible('userName')" prop="userName" label="预约用户" min-width="100"></el-table-column>
        <el-table-column v-if="isColumnVisible('contactPhone')" prop="contactPhone" label="联系电话" min-width="120"></el-table-column>
        <el-table-column v-if="isColumnVisible('appointmentTime')" label="预约时间" min-width="150" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" prop="status" label="状态" min-width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" label="创建时间" min-width="150" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" align="center">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === '已预约'" 
              size="small" 
              type="success" 
              @click="confirmAppointment(scope.row)"
            >
              确认
            </el-button>
            <el-button 
              v-if="scope.row.status === '已确认'" 
              size="small" 
              type="primary" 
              @click="completeAppointment(scope.row)"
            >
              完成
            </el-button>
            <el-button 
              v-if="scope.row.status !== '已取消' && scope.row.status !== '已完成'" 
              size="small" 
              type="danger" 
              @click="cancelAppointment(scope.row)"
            >
              取消
            </el-button>
            <el-button 
              size="small" 
              @click="viewDetails(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
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
    
    <!-- 预约详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="预约详情" 
      width="600px"
    >
      <div class="appointment-details" v-if="currentAppointment">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="预约编号">{{ currentAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="服务名称">{{ currentAppointment.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <el-tag :type="getStatusType(currentAppointment.status)">
              {{ currentAppointment.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">
            {{ formatDateTime(currentAppointment.appointmentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="宠物名称">{{ currentAppointment.petName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentAppointment.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="用户姓名">{{ currentAppointment.userName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentAppointment.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="特殊要求" :span="2">
            <div class="requirements-content">
              {{ currentAppointment.requirements || '无' }}
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentAppointment && currentAppointment.status === '已预约'" 
            type="success" 
            @click="confirmAppointment(currentAppointment)"
          >
            确认预约
          </el-button>
          <el-button 
            v-if="currentAppointment && currentAppointment.status === '已确认'" 
            type="primary" 
            @click="completeAppointment(currentAppointment)"
          >
            完成服务
          </el-button>
          <el-button 
            v-if="currentAppointment && currentAppointment.status !== '已取消' && currentAppointment.status !== '已完成'" 
            type="danger" 
            @click="cancelAppointment(currentAppointment)"
          >
            取消预约
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 备注输入对话框 -->
    <el-dialog 
      v-model="remarkDialogVisible" 
      :title="remarkTitle" 
      width="400px"
    >
      <el-form>
        <el-form-item label="备注信息">
          <el-input
            v-model="remarkContent"
            type="textarea"
            :rows="4"
            placeholder="请输入备注信息（可选）"
          ></el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="remarkDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusChange" :loading="statusChanging">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import * as XLSX from 'xlsx'
import { format } from '@/utils/dateUtils'

const userStore = useUserStore()

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 数据定义
const loading = ref(false)
const statusChanging = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const remarkDialogVisible = ref(false)
const currentAppointment = ref(null)
const remarkTitle = ref('')
const remarkContent = ref('')
const statusAction = ref('')
const dateRange = ref([])
const selectedRows = ref([])

// 搜索表单
const searchForm = ref({
  serviceName: '',
  petName: '',
  contactPhone: '',
  status: ''
})

// 列设置相关代码
const STORAGE_KEY = 'appointmentListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'serviceName', label: '服务名称' },
  { prop: 'petName', label: '宠物名称' },
  { prop: 'userName', label: '预约用户' },
  { prop: 'contactPhone', label: '联系电话' },
  { prop: 'appointmentTime', label: '预约时间' },
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

// 获取预约列表
const fetchAppointments = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      ...searchForm.value
    }
    
    // 添加日期范围过滤
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    await request.get('/service/appointment/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取预约详情
const fetchAppointmentDetails = async (id) => {
  try {
    await request.get(`/service/appointment/${id}`, null, {
      onSuccess: (res) => {
        currentAppointment.value = res
      }
    })
  } catch (error) {
    console.error('获取预约详情失败:', error)
  }
}

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态对应的类型
const getStatusType = (status) => {
  switch (status) {
    case '已预约': return 'warning'
    case '已确认': return 'primary'
    case '已完成': return 'success'
    case '已取消': return 'info'
    default: return ''
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAppointments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    serviceName: '',
    petName: '',
    contactPhone: '',
    status: ''
  }
  dateRange.value = []
  handleSearch()
}

// 处理日期范围变化
const handleDateRangeChange = () => {
  handleSearch()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchAppointments()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchAppointments()
}

// 刷新数据
const handleRefresh = () => {
  fetchAppointments()
  ElMessage.success('刷新成功')
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个预约记录吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/service/appointment/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchAppointments()
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
    const exportData = tableData.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'appointmentTime' || col.prop === 'createTime') {
          row[col.label] = formatDateTime(item[col.prop])
        } else if (col.prop === 'status') {
          row[col.label] = item[col.prop]
        } else {
          row[col.label] = item[col.prop]
        }
      })
      return row
    })

    // 创建工作簿
    const worksheet = XLSX.utils.json_to_sheet(exportData)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, '服务预约列表')

    // 导出文件
    XLSX.writeFile(workbook, `服务预约列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetails = (row) => {
  fetchAppointmentDetails(row.id)
  detailDialogVisible.value = true
}

// 确认预约
const confirmAppointment = (appointment) => {
  openRemarkDialog('确认预约', '确认预约', appointment)
}

// 完成服务
const completeAppointment = (appointment) => {
  openRemarkDialog('完成服务', '完成服务', appointment)
}

// 取消预约
const cancelAppointment = (appointment) => {
  openRemarkDialog('取消预约', '取消预约', appointment)
}

// 打开备注对话框
const openRemarkDialog = (title, action, appointment) => {
  remarkTitle.value = title
  statusAction.value = action
  currentAppointment.value = appointment
  remarkContent.value = ''
  remarkDialogVisible.value = true
}

// 提交状态变更
const submitStatusChange = async () => {
  if (!currentAppointment.value) return
  
  statusChanging.value = true
  try {
    let newStatus = ''
    switch (statusAction.value) {
      case '确认预约':
        newStatus = '已确认'
        break
      case '完成服务':
        newStatus = '已完成'
        break
      case '取消预约':
        newStatus = '已取消'
        break
      default:
        ElMessage.error('未知的操作类型')
        return
    }
    
    const params = {
      id: currentAppointment.value.id,
      status: newStatus,
      remark: remarkContent.value,
      operatorId: userStore.userInfo.id,
      operatorName: userStore.userInfo.name || userStore.userInfo.username
    }
    
    await request.put(`/service/appointment/${currentAppointment.value.id}/status`, params, {
      successMsg: `${statusAction.value}成功`,
      onSuccess: () => {
        remarkDialogVisible.value = false
        
        // 如果详情对话框已打开，刷新详情
        if (detailDialogVisible.value) {
          fetchAppointmentDetails(currentAppointment.value.id)
        }
        
        // 刷新列表
        fetchAppointments()
      }
    })
  } catch (error) {
    console.error(`${statusAction.value}失败:`, error)
  } finally {
    statusChanging.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.appointment-management-container {
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
  align-items: center;
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

.appointment-details {
  margin-bottom: 20px;
}

.requirements-content {
  white-space: pre-wrap;
  color: #606266;
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