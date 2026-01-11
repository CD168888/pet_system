<template>
  <div class="shipping-manage-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="物流状态">
          <el-select v-model="searchForm.shippingStatus" placeholder="请选择状态" clearable>
            <el-option v-for="(status, index) in shippingStatusOptions" :key="index" :label="status.label" :value="status.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏和表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <div class="left">
            <span class="title">物流管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" :loading="refreshLoading" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
          </div>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="shippingList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80"></el-table-column>
        <el-table-column v-if="isColumnVisible('orderNo')" prop="orderNo" label="订单号" min-width="150"></el-table-column>
        <el-table-column v-if="isColumnVisible('shippingNo')" prop="shippingNo" label="物流单号" min-width="180"></el-table-column>
        <el-table-column v-if="isColumnVisible('deliveryCompany')" prop="deliveryCompany" label="快递公司" min-width="120"></el-table-column>
        <el-table-column v-if="isColumnVisible('trackingNo')" prop="trackingNo" label="快递单号" min-width="150"></el-table-column>
        <el-table-column v-if="isColumnVisible('shippingStatus')" prop="shippingStatus" label="物流状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.shippingStatus)">
              {{ scope.row.shippingStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('deliveryTime')" prop="deliveryTime" label="发货时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.deliveryTime) }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('receiptTime')" prop="receiptTime" label="签收时间" min-width="160">
          <template #default="scope">
            {{ scope.row.receiptTime ? formatDate(scope.row.receiptTime) : '未签收' }}
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('operator')" prop="operator" label="操作人" min-width="100"></el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="viewShippingDetail(scope.row)">查看详情</el-button>
            <el-button 
              v-if="scope.row.shippingStatus === '已发货'" 
              type="success" 
              size="small" 
              link
              @click="handleUpdateStatus(scope.row, '已签收')">
              标记签收
            </el-button>
            <el-button 
              v-if="scope.row.shippingStatus === '已发货'" 
              type="warning" 
              size="small" 
              link
              @click="handleUpdateStatus(scope.row, '已退回')">
              标记退回
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
        </el-pagination>
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

    <!-- 物流详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="物流详情"
      width="700px">
      <div v-if="currentShipping" class="shipping-detail">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ currentShipping.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ currentShipping.shippingNo }}</el-descriptions-item>
          <el-descriptions-item label="快递公司">{{ currentShipping.deliveryCompany }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ currentShipping.trackingNo }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">物流状态</el-divider>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物流状态">
            <el-tag :type="getStatusTagType(currentShipping.shippingStatus)">
              {{ currentShipping.shippingStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentShipping.operator || '系统' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDate(currentShipping.deliveryTime) }}</el-descriptions-item>
          <el-descriptions-item label="签收时间">
            {{ currentShipping.receiptTime ? formatDate(currentShipping.receiptTime) : '未签收' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">其他信息</el-divider>
        
        <el-descriptions :column="1" border>
    
          <el-descriptions-item label="备注">{{ currentShipping.notes || '无' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentShipping.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDate(currentShipping.updateTime) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">物流轨迹</el-divider>
        
        <el-timeline>
          <el-timeline-item
            v-if="currentShipping.shippingStatus === '已签收'"
            timestamp="签收"
            placement="top"
            type="success"
            :hollow="false">
            <h4>包裹已签收</h4>
            <p>{{ currentShipping.receiptTime ? formatDate(currentShipping.receiptTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            v-if="currentShipping.shippingStatus === '已发货' || currentShipping.shippingStatus === '已签收'"
            timestamp="运输中"
            placement="top"
            type="primary"
            :hollow="currentShipping.shippingStatus === '已签收'">
            <h4>包裹正在配送</h4>
            <p>{{ formatDate(currentShipping.deliveryTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="已发货"
            placement="top"
            type="info">
            <h4>商家已发货</h4>
            <p>{{ formatDate(currentShipping.deliveryTime) }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            v-if="currentShipping.shippingStatus === '已退回'"
            timestamp="已退回"
            placement="top"
            type="danger"
            :hollow="false">
            <h4>包裹已退回</h4>
            <p>{{ currentShipping.updateTime ? formatDate(currentShipping.updateTime) : '' }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Download, Setting } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { format } from '@/utils/dateUtils'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
// 数据定义
const loading = ref(false)
const refreshLoading = ref(false)
const shippingList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const detailDialogVisible = ref(false)
const currentShipping = ref(null)
const userStore = useUserStore()

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 修改列设置相关代码
const STORAGE_KEY = 'shippingListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'orderNo', label: '订单号' },
  { prop: 'shippingNo', label: '物流单号' },
  { prop: 'deliveryCompany', label: '快递公司' },
  { prop: 'trackingNo', label: '快递单号' },
  { prop: 'shippingStatus', label: '物流状态' },
  { prop: 'deliveryTime', label: '发货时间' },
  { prop: 'receiptTime', label: '签收时间' },
  { prop: 'operator', label: '操作人' }
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

// 物流状态选项
const shippingStatusOptions = [
  { value: '待发货', label: '待发货' },
  { value: '已发货', label: '已发货' },
  { value: '已签收', label: '已签收' },
  { value: '已退回', label: '已退回' }
]

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  shippingStatus: ''
})

// 获取物流列表
const fetchShippings = async () => {
  loading.value = true
  try {
    await request.get('/shipping/page', {
      orderNo: searchForm.orderNo,
      shippingStatus: searchForm.shippingStatus,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        shippingList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取物流列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchShippings()
}

// 重置搜索
const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.shippingStatus = ''
  currentPage.value = 1
  fetchShippings()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 将日期对象格式化为字符串（用于发送到后端）
const formatDateString = (date) => {
  if (!date) return ''
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待发货':
      return 'warning'
    case '已发货':
      return 'primary'
    case '已签收':
      return 'success'
    case '已退回':
      return 'danger'
    default:
      return ''
  }
}

// 查看物流详情
const viewShippingDetail = (shipping) => {
  currentShipping.value = shipping
  detailDialogVisible.value = true
}

// 更新物流状态
const handleUpdateStatus = (shipping, status) => {
  const statusText = status === '已签收' ? '签收' : '退回'
  
  ElMessageBox.confirm(
    `确定要将该物流标记为${statusText}吗？`,
    `标记${statusText}`,
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const updateData = {
        ...shipping,
        shippingStatus: status,
        operator: userStore.userInfo.username
      }
      
      // 如果是签收状态，设置签收时间并格式化为字符串
      if (status === '已签收') {
        updateData.receiptTime = formatDateString(new Date())
        console.log('格式化后的签收时间:', updateData.receiptTime)
      }
      
      await request.put(`/shipping/${shipping.id}`, updateData, {
        successMsg: `物流已标记为${statusText}`,
        onSuccess: () => {
          fetchShippings()
        }
      })
    } catch (error) {
      console.error('更新物流状态失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchShippings()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchShippings()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 优化导出功能
const handleExport = () => {
  try {
    loading.value = true
    
    // 获取当前可见列的配置
    const visibleColumnConfigs = allColumns.filter(col => isColumnVisible(col.prop))
    
    // 准备导出数据
    const exportData = shippingList.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'deliveryTime' || col.prop === 'receiptTime') {
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
    XLSX.utils.book_append_sheet(workbook, worksheet, '物流列表')

    // 导出文件
    XLSX.writeFile(workbook, `物流列表_${format(new Date())}.xlsx`)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

// 手动刷新数据
const handleRefresh = () => {
  refreshLoading.value = true
  fetchShippings().finally(() => {
    refreshLoading.value = false
  })
}

// 页面加载时获取物流列表
onMounted(() => {
  fetchShippings()
})
</script>

<style scoped>
.shipping-manage-container {
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

.shipping-detail {
  padding: 10px;
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