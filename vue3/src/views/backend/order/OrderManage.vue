<template>
  <div class="order-manage-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable></el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option v-for="(status, index) in orderStatusOptions" :key="index" :label="status.label" :value="status.value"></el-option>
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
            <span class="title">订单管理</span>
            <el-button :icon="refreshIcon" circle @click="handleRefresh" :loading="refreshLoading" />
          </div>
          <div class="right">
            <el-button :icon="downloadIcon" @click="handleExport">导出</el-button>
            <el-button :icon="settingIcon" @click="columnSettingVisible = true">列设置</el-button>
            <el-button type="danger" :disabled="selectedRows.length === 0" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <!-- 表格区域 -->
      <el-table
        v-loading="loading"
        :data="orderList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column v-if="isColumnVisible('id')" prop="id" label="ID" width="80"></el-table-column>
        <el-table-column v-if="isColumnVisible('orderNo')" prop="orderNo" label="订单号" min-width="180"></el-table-column>
        <el-table-column v-if="isColumnVisible('productInfo')" label="商品信息" min-width="250">
          <template #default="scope">
            <div class="product-info">
              <el-image
                :src="getImageUrl(scope.row.productImage)"
                style="width: 50px; height: 50px"
                fit="cover">
              </el-image>
              <div class="product-detail">
                <div class="product-name">{{ scope.row.productName }}</div>
                <div class="product-price">
                  <span>¥{{ scope.row.price }} × {{ scope.row.quantity }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('totalAmount')" prop="totalAmount" label="订单金额" min-width="100">
          <template #default="scope">
            <span class="price">¥{{ scope.row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('status')" prop="status" label="订单状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('returnReviewStatus')" label="退货/评价状态" min-width="120">
          <template #default="scope">
            <div v-if="scope.row.isReturned" class="status-tag">
              <el-tag type="danger" size="small">已退货</el-tag>
            </div>
            <div v-if="scope.row.isReviewed" class="status-tag">
              <el-tag type="success" size="small">已评价</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column v-if="isColumnVisible('contactName')" prop="contactName" label="收货人" min-width="100"></el-table-column>
        <el-table-column v-if="isColumnVisible('contactPhone')" prop="contactPhone" label="联系电话" min-width="130"></el-table-column>
        <el-table-column v-if="isColumnVisible('createTime')" prop="createTime" label="下单时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="viewOrderDetail(scope.row)">查看详情</el-button>
            <el-button v-if="scope.row.status === '待发货'" type="success" size="small" link @click="handleShip(scope.row)">发货</el-button>
            <el-button v-if="scope.row.isReturned" type="warning" size="small" link @click="viewReturn(scope.row)">查看退货</el-button>
            <el-button v-if="scope.row.status === '已完成' && scope.row.isReviewed" type="info" size="small" link @click="viewReview(scope.row)">查看评价</el-button>
            <el-button v-if="scope.row.status === '已完成' || scope.row.status === '已取消'" type="danger" size="small" link @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="700px">
      <div v-if="currentOrder" class="order-detail">
        <el-descriptions title="订单信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatDate(currentOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusTagType(currentOrder.status)">{{ currentOrder.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '未支付' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" v-if="currentOrder.paymentTime">
            {{ formatDate(currentOrder.paymentTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="订单备注">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">收货信息</el-divider>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="收货地址">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ currentOrder.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.contactPhone }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentShipping">
          <el-divider content-position="left">物流信息</el-divider>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="发货单号">{{ currentShipping.shippingNo }}</el-descriptions-item>
            <el-descriptions-item label="发货状态">
              <el-tag :type="getShippingStatusTagType(currentShipping.shippingStatus)">
                {{ currentShipping.shippingStatus }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="快递公司">{{ currentShipping.deliveryCompany }}</el-descriptions-item>
            <el-descriptions-item label="快递单号">{{ currentShipping.trackingNo }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ formatDate(currentShipping.deliveryTime) }}</el-descriptions-item>
            <el-descriptions-item label="签收时间">
              {{ currentShipping.receiptTime ? formatDate(currentShipping.receiptTime) : '未签收' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider content-position="left">商品信息</el-divider>
        
        <div class="order-product-info">
          <div class="product-card">
            <el-image
              :src="getImageUrl(currentOrder.productImage)"
              style="width: 80px; height: 80px"
              fit="cover">
            </el-image>
            <div class="product-detail">
              <div class="product-name">{{ currentOrder.productName }}</div>
              <div class="product-price">单价：¥{{ currentOrder.price }}</div>
              <div class="product-quantity">数量：{{ currentOrder.quantity }}</div>
            </div>
            <div class="product-total">
              <div>商品总价：<span class="price">¥{{ currentOrder.totalAmount }}</span></div>
            </div>
          </div>
        </div>

        <!-- 添加退货信息部分 -->
        <div v-if="currentOrder.isReturned">
          <el-divider content-position="left">退货信息</el-divider>
          <el-descriptions :column="1" border v-if="currentReturn">
            <el-descriptions-item label="订单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="退货原因">{{ currentReturn.reason }}</el-descriptions-item>
            <el-descriptions-item label="退货说明">{{ currentReturn.description }}</el-descriptions-item>
            <el-descriptions-item label="退货状态">
              <el-tag :type="getReturnStatusType(currentReturn.status)">
                {{ currentReturn.status }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请前状态" v-if="currentReturn.previousOrderStatus">
              {{ currentReturn.previousOrderStatus }}
            </el-descriptions-item>
            <el-descriptions-item label="退款金额">¥{{ currentReturn.refundAmount }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatDate(currentReturn.createTime) }}</el-descriptions-item>
            <el-descriptions-item v-if="currentReturn.processTime" label="处理时间">
              {{ formatDate(currentReturn.processTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentReturn.operatorNote" label="处理备注">
              {{ currentReturn.operatorNote }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentReturn.images" label="图片凭证">
              <div class="return-images">
                <el-image 
                  v-for="(img, index) in currentReturn.images.split(',')"
                  :key="index"
                  :src="getImageUrl(img)"
                  :preview-teleported="true"
                  :preview-src-list="currentReturn.images.split(',').map(img => getImageUrl(img))"
                  fit="cover"
                  class="return-image"
                />
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 添加评价信息部分 -->
        <div v-if="currentOrder.isReviewed">
          <el-divider content-position="left">评价信息</el-divider>
          <el-descriptions :column="2" border v-if="currentReview">
            <el-descriptions-item label="评分">
              <el-rate v-model="currentReview.rating" disabled></el-rate>
            </el-descriptions-item>
            <el-descriptions-item label="评价时间">{{ formatDate(currentReview.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="评价内容">{{ currentReview.content }}</el-descriptions-item>
            <el-descriptions-item label="商家回复" v-if="currentReview.reply">
              {{ currentReview.reply }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单发货"
      width="550px">
      <el-form 
        ref="shipFormRef"
        :model="shipForm"
        :rules="shipRules"
        label-width="100px">
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="shipForm.orderNo" disabled></el-input>
        </el-form-item>
        <el-form-item label="快递公司" prop="deliveryCompany">
          <el-select v-model="shipForm.deliveryCompany" placeholder="请选择快递公司" style="width: 100%;">
            <el-option label="顺丰速运" value="顺丰速运"></el-option>
            <el-option label="中通快递" value="中通快递"></el-option>
            <el-option label="圆通速递" value="圆通速递"></el-option>
            <el-option label="韵达快递" value="韵达快递"></el-option>
            <el-option label="申通快递" value="申通快递"></el-option>
            <el-option label="京东物流" value="京东物流"></el-option>
            <el-option label="其他" value="其他"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号"></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input 
            v-model="shipForm.notes" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息（选填）">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitShipForm" :loading="submitting">
            确认发货
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加退货详情对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="退货详情"
      width="600px">
      <div v-if="currentReturn">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="退货原因">{{ currentReturn.reason }}</el-descriptions-item>
          <el-descriptions-item label="退货说明">{{ currentReturn.description }}</el-descriptions-item>
          <el-descriptions-item label="退货状态">
            <el-tag :type="getReturnStatusType(currentReturn.status)">
              {{ currentReturn.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请前状态" v-if="currentReturn.previousOrderStatus">
            {{ currentReturn.previousOrderStatus }}
          </el-descriptions-item>
          <el-descriptions-item label="退款金额">¥{{ currentReturn.refundAmount }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(currentReturn.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentReturn.processTime" label="处理时间">
            {{ formatDate(currentReturn.processTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReturn.operatorNote" label="处理备注">
            {{ currentReturn.operatorNote }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReturn.images" label="图片凭证">
            <div class="return-images">
              <el-image 
                v-for="(img, index) in currentReturn.images.split(',')"
                :key="index"
                :src="getImageUrl(img)"
                :preview-teleported="true"
                :preview-src-list="currentReturn.images.split(',').map(img => getImageUrl(img))"
                fit="cover"
                class="return-image"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentReturn.status === '待处理'" class="process-form">
          <el-divider>处理退货申请</el-divider>
          <el-form :model="processForm" ref="processFormRef" label-width="100px">
            <el-form-item label="处理结果" prop="status">
              <el-radio-group v-model="processForm.status">
                <el-radio label="已同意">同意退货</el-radio>
                <el-radio label="已拒绝">拒绝退货</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="处理备注" prop="operatorNote">
              <el-input
                v-model="processForm.operatorNote"
                type="textarea"
                :rows="3"
                placeholder="请输入处理备注"
              ></el-input>
            </el-form-item>
          </el-form>
          <div class="dialog-footer">
            <el-button @click="returnDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleProcessReturn" :loading="submitting">
              确认处理
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 添加评价详情对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="评价详情"
      width="600px">
      <div v-if="currentReview">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ currentOrder?.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate v-model="currentReview.rating" disabled></el-rate>
          </el-descriptions-item>
          <el-descriptions-item label="评价内容">{{ currentReview.content }}</el-descriptions-item>
          <el-descriptions-item label="评价时间">{{ formatDate(currentReview.createTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="currentReview.images" label="评价图片">
            <div class="review-images">
              <el-image 
                v-for="(img, index) in currentReview.images.split(',')"
                :key="index"
                :src="getImageUrl(img)"
                :preview-teleported="true"
                :preview-src-list="currentReview.images.split(',').map(img => getImageUrl(img))"
                fit="cover"
                class="review-image"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="!currentReview.reply" class="reply-form" style="margin-top: 20px;">
          <el-form :model="replyForm" ref="replyFormRef">
            <el-form-item label="商家回复" prop="reply">
              <el-input
                v-model="replyForm.reply"
                type="textarea"
                :rows="3"
                placeholder="请输入回复内容">
              </el-input>
            </el-form-item>
          </el-form>
          <div class="dialog-footer">
            <el-button type="primary" @click="handleReplyReview">提交回复</el-button>
          </div>
        </div>
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

// 将图标暴露给模板使用
const refreshIcon = Refresh
const downloadIcon = Download
const settingIcon = Setting

// 数据定义
const loading = ref(false)
const refreshLoading = ref(false)
const orderList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const detailDialogVisible = ref(false)
const shipDialogVisible = ref(false)
const currentOrder = ref(null)
const currentShipping = ref(null)
const submitting = ref(false)
const shipFormRef = ref(null)
const returnDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentReturn = ref(null)
const currentReview = ref(null)
const replyForm = reactive({
  reply: ''
})

// 修改列设置相关代码
const STORAGE_KEY = 'orderListVisibleColumns'
const columnSettingVisible = ref(false)
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'orderNo', label: '订单号' },
  { prop: 'productInfo', label: '商品信息' },
  { prop: 'totalAmount', label: '订单金额' },
  { prop: 'status', label: '订单状态' },
  { prop: 'returnReviewStatus', label: '退货/评价状态' },
  { prop: 'contactName', label: '收货人' },
  { prop: 'contactPhone', label: '联系电话' },
  { prop: 'createTime', label: '下单时间' }
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

// 订单状态选项
const orderStatusOptions = [
  { value: '待付款', label: '待付款' },
  { value: '待发货', label: '待发货' },
  { value: '待收货', label: '待收货' },
  { value: '已完成', label: '已完成' },
  { value: '已取消', label: '已取消' }
]

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: ''
})

// 发货表单
const shipForm = reactive({
  orderId: '',
  orderNo: '',
  deliveryCompany: '',
  trackingNo: '',
  notes: ''
})

// 发货表单校验规则
const shipRules = {
  deliveryCompany: [
    { required: true, message: '请选择快递公司', trigger: 'change' }
  ],
  trackingNo: [
    { required: true, message: '请输入快递单号', trigger: 'blur' }
  ]
}

// 退货相关
const processFormRef = ref(null)
const processForm = reactive({
  status: '',
  operatorNote: '',
  operator: 'admin' // 这里可以根据实际登录用户设置
})

// 获取退货状态标签类型
const getReturnStatusType = (status) => {
  switch (status) {
    case '待处理':
      return 'warning'
    case '已同意':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    await request.get('/order/page', {
      orderNo: searchForm.orderNo,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        orderList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 重置搜索
const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  currentPage.value = 1
  fetchOrders()
}

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return ''
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return image.startsWith('http') ? image : `${baseAPI}${image}`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待付款':
      return 'warning'
    case '待发货':
      return 'info'
    case '待收货':
      return 'primary'
    case '已完成':
      return 'success'
    case '已取消':
      return 'danger'
    default:
      return ''
  }
}

// 查看订单详情
const viewOrderDetail = async (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
  
  // 如果订单状态是待收货或已完成，查询物流信息
  if (order.status === '待收货' || order.status === '已完成') {
    try {
      await request.get(`/shipping/order/${order.id}`, null, {
        onSuccess: (res) => {
          currentShipping.value = res
        }
      })
    } catch (error) {
      console.error('获取物流信息失败:', error)
      currentShipping.value = null
    }
  } else {
    currentShipping.value = null
  }

  // 如果订单已退货，获取退货信息
  if (order.isReturned) {
    try {
      await request.get(`/order/return/order/${order.id}`, null, {
        onSuccess: (res) => {
          currentReturn.value = res
        }
      })
    } catch (error) {
      console.error('获取退货信息失败:', error)
      currentReturn.value = null
    }
  } else {
    currentReturn.value = null
  }

  // 如果订单已评价，获取评价信息
  if (order.isReviewed) {
    try {
      await request.get(`/order/review/order/${order.id}`, null, {
        onSuccess: (res) => {
          currentReview.value = res
        }
      })
    } catch (error) {
      console.error('获取评价信息失败:', error)
      currentReview.value = null
    }
  } else {
    currentReview.value = null
  }
}

// 发货处理
const handleShip = (order) => {
  shipForm.orderId = order.id
  shipForm.orderNo = order.orderNo
  shipForm.deliveryCompany = ''
  shipForm.trackingNo = ''
  shipForm.notes = ''
  
  shipDialogVisible.value = true
}

// 提交发货表单
const submitShipForm = async () => {
  if (!shipFormRef.value) return
  
  await shipFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 构造发货信息
        const shippingData = {
          orderId: shipForm.orderId,
          deliveryCompany: shipForm.deliveryCompany,
          trackingNo: shipForm.trackingNo,
          notes: shipForm.notes,
          shippingStatus: '已发货'
        }
        
        await request.post('/shipping', shippingData, {
          successMsg: '发货成功',
          onSuccess: () => {
            shipDialogVisible.value = false
            fetchOrders()
          }
        })
      } catch (error) {
        console.error('发货失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除订单
const handleDelete = (order) => {
  ElMessageBox.confirm(
    '确定要删除该订单吗？删除后不可恢复',
    '删除订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete(`/order/${order.id}`, {
        successMsg: '订单删除成功',
        onSuccess: () => {
          fetchOrders()
        }
      })
    } catch (error) {
      console.error('删除订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchOrders()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

// 处理表格选择
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) return
  
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedRows.value.length} 个订单吗？`, '提示', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.id)
    await request.post('/order/batch-delete', { ids }, {
      successMsg: '批量删除成功',
      onSuccess: () => {
        fetchOrders()
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
    const exportData = orderList.value.map(item => {
      const row = {}
      visibleColumnConfigs.forEach(col => {
        if (col.prop === 'productInfo') {
          row[col.label] = `${item.productName} (¥${item.price} × ${item.quantity})`
        } else if (col.prop === 'returnReviewStatus') {
          const status = []
          if (item.isReturned) status.push('已退货')
          if (item.isReviewed) status.push('已评价')
          row[col.label] = status.join('、') || '无'
        } else if (col.prop === 'createTime') {
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
    XLSX.utils.book_append_sheet(workbook, worksheet, '订单列表')

    // 导出文件
    XLSX.writeFile(workbook, `订单列表_${format(new Date())}.xlsx`)
    
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
  fetchOrders().finally(() => {
    refreshLoading.value = false
  })
}

// 查看退货详情
const viewReturn = async (order) => {
  currentOrder.value = order
  try {
    await request.get(`/order/return/order/${order.id}`, null, {
      onSuccess: (res) => {
        currentReturn.value = res
        returnDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取退货信息失败:', error)
  }
}

// 处理退货申请
const handleProcessReturn = async () => {
  if (!currentReturn.value) return
  
  if (!processForm.status) {
    ElMessage.warning('请选择处理结果')
    return
  }
  
  submitting.value = true
  try {
    await request.put(`/order/return/${currentReturn.value.id}/process`, null, {
      params: {
        status: processForm.status,
        operatorNote: processForm.operatorNote,
        operator: processForm.operator
      },
      successMsg: '处理成功',
      onSuccess: () => {
        returnDialogVisible.value = false
        fetchOrders()
      }
    })
  } catch (error) {
    console.error('处理退货申请失败:', error)
  } finally {
    submitting.value = false
  }
}

// 查看评价详情
const viewReview = async (order) => {
  currentOrder.value = order
  try {
    await request.get(`/order/review/order/${order.id}`, null, {
      onSuccess: (res) => {
        currentReview.value = res
        reviewDialogVisible.value = true
      }
    })
  } catch (error) {
    console.error('获取评价信息失败:', error)
  }
}

// 回复评价
const handleReplyReview = async () => {
  if (!replyForm.reply) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  try {
    await request.put(`/order/review/${currentReview.value.id}/reply`, null, {
      params: {
        reply: replyForm.reply
      },
      successMsg: '回复成功',
      onSuccess: () => {
        reviewDialogVisible.value = false
        fetchOrders()
      }
    })
  } catch (error) {
    console.error('回复评价失败:', error)
  }
}

// 获取物流状态标签类型
const getShippingStatusTagType = (status) => {
  switch (status) {
    case '已发货':
      return 'success'
    case '待发货':
      return 'info'
    case '已签收':
      return 'success'
    case '已退回':
      return 'danger'
    default:
      return 'info'
  }
}

// 页面加载时获取订单列表
onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.order-manage-container {
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

.price {
  color: #f56c6c;
  font-weight: bold;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-detail {
  margin-left: 10px;
  flex: 1;
}

.product-name {
  margin-bottom: 5px;
  font-weight: 500;
}

.product-price {
  font-size: 13px;
  color: #606266;
}

.product-quantity {
  font-size: 13px;
  color: #606266;
}

.order-detail {
  padding: 10px;
}

.order-product-info {
  margin-top: 20px;
}

.product-card {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  display: flex;
  align-items: center;
}

.product-total {
  margin-left: auto;
  text-align: right;
}

.status-tag {
  margin-bottom: 5px;
}

.status-tag:last-child {
  margin-bottom: 0;
}

.reply-form {
  margin-top: 20px;
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.dialog-footer {
  text-align: right;
  margin-top: 20px;
}

.return-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.return-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
}

.process-form {
  margin-top: 20px;
  padding-top: 20px;
  
  .el-divider {
    margin: 20px 0;
  }
  
  .dialog-footer {
    margin-top: 20px;
    text-align: right;
  }
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
  }
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