<template>
  <div class="training-appointment-management">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" placeholder="课程名称" clearable />
        </el-form-item>
        <el-form-item label="宠物名称">
          <el-input v-model="searchForm.petName" placeholder="宠物名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="预约状态" clearable style="width: 200px">
            <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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
            <span class="title">训练预约管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport" :loading="exporting">导出数据</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
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
        <el-table-column v-if="isColumnVisible('courseName')" prop="courseName" label="课程名称" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="isColumnVisible('petName')" prop="petName" label="宠物名称" width="100" />
        <el-table-column v-if="isColumnVisible('userName')" prop="userName" label="预约用户" width="100" />
        <el-table-column v-if="isColumnVisible('contactPhone')" prop="contactPhone" label="联系电话" width="120" />
        <el-table-column v-if="isColumnVisible('appointmentTime')" prop="appointmentTime" label="预约时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.appointmentTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('progress')" prop="progress" label="训练进度" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.progress || 0" :format="progressFormat" />
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('hasFeedback')" prop="hasFeedback" label="客户评价" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.hasFeedback" type="success">已评价</el-tag>
            <el-tag v-else-if="scope.row.status === '已完成'" type="info">未评价</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button type="primary" link @click="viewDetail(scope.row)">详情</el-button>
            <el-button type="warning" link @click="updateStatus(scope.row)">状态</el-button>
            <el-button type="success" link @click="updateProgress(scope.row)">进度</el-button>
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
    
    <!-- 详情对话框 -->
    <el-dialog
      title="预约详情"
      v-model="detailDialogVisible"
      width="650px"
    >
      <div v-if="currentAppointment" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预约ID">{{ currentAppointment.id }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ currentAppointment.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程分类">{{ currentAppointment.category }}</el-descriptions-item>
          <el-descriptions-item label="课程价格">¥{{ formatPrice(currentAppointment.coursePrice) }}</el-descriptions-item>
          <el-descriptions-item label="宠物名称">{{ currentAppointment.petName }}</el-descriptions-item>
          <el-descriptions-item label="预约状态">
            <el-tag :type="getStatusType(currentAppointment.status)">
              {{ currentAppointment.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="训练进度">
            <el-progress :percentage="currentAppointment.progress || 0" :format="progressFormat" />
          </el-descriptions-item>
          <el-descriptions-item label="预约人">{{ currentAppointment.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentAppointment.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ formatDateTime(currentAppointment.appointmentTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentAppointment.createTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div v-if="currentAppointment.requirements" class="special-requirements">
          <h4>特殊要求</h4>
          <p>{{ currentAppointment.requirements }}</p>
        </div>
        
        <div v-if="currentAppointment.remark" class="appointment-remark">
          <h4>备注</h4>
          <p>{{ currentAppointment.remark }}</p>
        </div>
        
        <!-- 添加反馈信息部分 -->
        <div v-if="currentAppointment.hasFeedback && currentAppointment.feedback" class="feedback-section">
          <h4>客户反馈</h4>
          <div class="feedback-content">
            <div class="rating-row">
              <span class="label">满意度评分：</span>
              <el-rate v-model="currentAppointment.rating" disabled show-score />
            </div>
            <div class="feedback-text">
              <span class="label">反馈内容：</span>
              <p>{{ currentAppointment.feedback }}</p>
            </div>
            <div class="feedback-time">
              <span class="label">提交时间：</span>
              <span>{{ formatDateTime(currentAppointment.feedbackTime) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 更新状态对话框 -->
    <el-dialog
      title="更新预约状态"
      v-model="statusDialogVisible"
      width="500px"
    >
      <el-form
        ref="statusFormRef"
        :model="statusForm"
        label-width="100px"
        status-icon
      >
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentStatus)">{{ currentStatus }}</el-tag>
        </el-form-item>
        
        <el-form-item label="新状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="选择新状态">
            <el-option 
              v-for="status in availableStatuses" 
              :key="status" 
              :label="status" 
              :value="status" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="statusForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入状态变更备注（选填）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusUpdate" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 更新进度对话框 -->
    <el-dialog
      title="更新训练进度"
      v-model="progressDialogVisible"
      width="500px"
    >
      <el-form
        ref="progressFormRef"
        :model="progressForm"
        label-width="100px"
        status-icon
      >
        <el-form-item label="当前进度">
          <el-progress :percentage="currentProgress" />
        </el-form-item>
        
        <el-form-item label="新进度" prop="progress">
          <el-slider
            v-model="progressForm.progress"
            :step="5"
            show-stops
            :marks="{
              0: '0%',
              25: '25%',
              50: '50%',
              75: '75%',
              100: '100%'
            }"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="progressForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入进度更新备注（选填）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="progressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProgressUpdate" :loading="submitting">确定</el-button>
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 数据定义
const loading = ref(false)
const submitting = ref(false)
const exporting = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const progressDialogVisible = ref(false)
const currentAppointment = ref(null)
const statusFormRef = ref(null)
const progressFormRef = ref(null)
const selectedRows = ref([])
const columnSettingVisible = ref(false)

// 状态选项
const statusOptions = ['已预约', '已确认', '进行中', '已完成', '已取消']

// 当前状态和进度
const currentStatus = ref('')
const currentProgress = ref(0)

// 可选状态计算属性
const availableStatuses = computed(() => {
  // 根据当前状态计算可选的下一状态
  const status = currentStatus.value
  if (status === '已预约') {
    return ['已确认', '已取消']
  } else if (status === '已确认') {
    return ['进行中', '已取消']
  } else if (status === '进行中') {
    return ['已完成', '已取消']
  }
  return []
})

// 搜索表单
const searchForm = reactive({
  courseName: '',
  petName: '',
  status: '',
  dateRange: []
})

// 状态表单
const statusForm = reactive({
  id: '',
  status: '',
  remark: ''
})

// 进度表单
const progressForm = reactive({
  id: '',
  progress: 0,
  remark: ''
})

// 用户商店
const userStore = useUserStore()

// 加载预约列表
const fetchAppointments = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      courseName: searchForm.courseName || undefined,
      petName: searchForm.petName || undefined,
      status: searchForm.status || undefined
    }
    
    // 添加日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    await request.get('/training/appointment/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 查看详情
const viewDetail = async (row) => {
  currentAppointment.value = row
  detailDialogVisible.value = true
}

// 更新状态对话框
const updateStatus = (row) => {
  currentAppointment.value = row
  currentStatus.value = row.status
  
  // 如果当前状态已完成或已取消，则不允许修改
  if (row.status === '已完成' || row.status === '已取消') {
    ElMessage.warning('该预约已完成或已取消，无法修改状态')
    return
  }
  
  statusForm.id = row.id
  statusForm.status = ''
  statusForm.remark = ''
  statusDialogVisible.value = true
}

// 更新进度对话框
const updateProgress = (row) => {
  currentAppointment.value = row
  currentProgress.value = row.progress || 0
  
  // 如果当前状态不是进行中，则不允许修改
  if (row.status !== '进行中') {
    ElMessage.warning('只有进行中的预约才能更新进度')
    return
  }
  
  progressForm.id = row.id
  progressForm.progress = row.progress || 0
  progressForm.remark = ''
  progressDialogVisible.value = true
}

// 提交状态更新
const submitStatusUpdate = async () => {
  if (!statusForm.status) {
    ElMessage.warning('请选择新状态')
    return
  }
  
  submitting.value = true
  try {
    await request.put(`/training/appointment/${statusForm.id}/status`, {
      status: statusForm.status,
      remark: statusForm.remark,
      operatorId: userStore.userInfo.id
    }, {
      successMsg: '状态更新成功',
      onSuccess: () => {
        statusDialogVisible.value = false
        fetchAppointments()
      }
    })
  } catch (error) {
    console.error('更新状态失败:', error)
  } finally {
    submitting.value = false
  }
}

// 提交进度更新
const submitProgressUpdate = async () => {
  submitting.value = true
  try {
    await request.put(`/training/appointment/${progressForm.id}/progress?progress=${progressForm.progress}&operatorId=${userStore.userInfo.id}`, null, {
      successMsg: '进度更新成功',
      onSuccess: () => {
        progressDialogVisible.value = false
        fetchAppointments()
      }
    })
  } catch (error) {
    console.error('更新进度失败:', error)
  } finally {
    submitting.value = false
  }
}

// 导出数据
const handleExport = async () => {
  exporting.value = true
  try {
    const params = {
      courseName: searchForm.courseName || undefined,
      petName: searchForm.petName || undefined,
      status: searchForm.status || undefined
    }
    
    // 添加日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    await request.get('/training/appointment/export', params, {
      responseType: 'blob',
      onSuccess: (res) => {
        // 创建下载链接
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = `训练预约数据_${new Date().getTime()}.xlsx`
        link.click()
        URL.revokeObjectURL(link.href)
        ElMessage.success('数据导出成功')
      }
    })
  } catch (error) {
    console.error('导出数据失败:', error)
  } finally {
    exporting.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAppointments()
}

// 重置搜索
const resetSearch = () => {
  searchForm.courseName = ''
  searchForm.petName = ''
  searchForm.status = ''
  searchForm.dateRange = []
  currentPage.value = 1
  fetchAppointments()
}

// 刷新
const handleRefresh = () => {
  fetchAppointments()
  ElMessage.success('刷新成功')
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAppointments()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAppointments()
}

// 获取状态标签类型
const getStatusType = (status) => {
  const statusMap = {
    '已预约': 'info',
    '已确认': 'primary',
    '进行中': 'warning',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
}

// 列设置相关代码
const STORAGE_KEY = 'trainingAppointmentVisibleColumns'
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'courseName', label: '课程名称' },
  { prop: 'petName', label: '宠物名称' },
  { prop: 'userName', label: '预约用户' },
  { prop: 'contactPhone', label: '联系电话' },
  { prop: 'appointmentTime', label: '预约时间' },
  { prop: 'status', label: '状态' },
  { prop: 'progress', label: '训练进度' },
  { prop: 'hasFeedback', label: '客户评价' },
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

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化价格
const formatPrice = (price) => {
  return parseFloat(price).toFixed(2)
}

// 进度条格式化
const progressFormat = (percentage) => {
  return percentage === 100 ? '完成' : `${percentage}%`
}

// 页面加载时获取数据
onMounted(() => {
  fetchAppointments()
})</script>

<style scoped>
.training-appointment-management {
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

:deep(.el-select) {
  width: 200px;
}

.detail-content {
  padding: 10px;
}

.special-requirements,
.appointment-remark {
  margin-top: 20px;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
}

.special-requirements h4,
.appointment-remark h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #409EFF;
}

.feedback-section {
  margin-top: 20px;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
}

.feedback-content {
  padding: 8px;
}

.rating-row, .feedback-text, .feedback-time {
  margin-bottom: 10px;
}

.label {
  font-weight: bold;
  margin-right: 8px;
}

.feedback-text p {
  margin-top: 5px;
  white-space: pre-wrap;
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
