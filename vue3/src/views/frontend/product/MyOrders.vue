<template>
  <div class="orders-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
      <div class="decoration-paw paw-3">🐾</div>
    </div>
    
    <!-- 页面Banner -->
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item @click="$router.push('/product')">宠物物资</el-breadcrumb-item>
            <el-breadcrumb-item>我的订单</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1 class="page-title">我的订单</h1>
        <p class="page-subtitle">查看和管理您的所有订单</p>
        <div class="banner-decoration">
          <div class="decoration-paw paw-1">🐾</div>
          <div class="decoration-paw paw-2">🐾</div>
          <div class="decoration-paw paw-3">🐾</div>
        </div>
      </div>
    </div>
    
    <div class="orders-content">
      
      <el-card shadow="never" class="orders-card">
        <template #header>
          <div class="orders-header">
            <h2>订单列表</h2>
            <el-select 
              v-model="currentStatus" 
              placeholder="订单状态" 
              size="small" 
              @change="handleStatusChange"
              class="status-filter"
            >
              <el-option label="全部订单" value=""></el-option>
              <el-option 
                v-for="status in orderStatusOptions" 
                :key="status.value" 
                :label="status.label" 
                :value="status.value"
              >
                <div class="status-option">
                  <el-tag size="small" :type="getStatusTagType(status.value)" class="status-tag">{{ status.label }}</el-tag>
                </div>
              </el-option>
            </el-select>
          </div>
        </template>
        
        <div v-loading="loading">
          <!-- 订单为空 -->
          <el-empty 
            v-if="!orderList || orderList.length === 0" 
            description="暂无订单"
            :image-size="200"
          >
            <template #description>
              <p>您还没有任何订单</p>
              <p>快去选购您喜爱的商品吧</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToProductList">
              <el-icon><ShoppingCart /></el-icon>
              去选购商品
            </el-button>
          </el-empty>
          
          <!-- 订单列表 -->
          <div v-else class="orders-list">
            <div v-for="order in orderList" :key="order.orderNo" class="order-item">
              <div class="order-header">
                <div class="order-info">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatDate(order.createTime) }}</span>
                </div>
                <div class="order-status">
                  <el-tag :type="getStatusTagType(order.status)" effect="light">{{ order.status }}</el-tag>
                  <!-- 添加倒计时显示 -->
                  <div v-if="order.status === '待付款'" class="payment-countdown">
                    <el-tooltip
                      :content="'支付截止时间: ' + formatDate(order.paymentDeadline)"
                      placement="top"
                    >
                      <span class="countdown-label">
                        <el-icon><Timer /></el-icon>
                        支付倒计时:
                      </span>
                    </el-tooltip>
                    <countdown-timer
                      :order-id="order.productList[0].id"
                      @expired="handleOrderExpired(order)"
                    />
                  </div>
                </div>
              </div>
              
              <div class="order-content">
                <div v-for="product in order.productList" :key="product.id" class="product-info">
                  <el-image
                    :src="getImageUrl(product.productImage)"
                    fit="cover"
                    class="product-image"
                    @click="goToProductDetail(product.productId)">
                  </el-image>
                  <div class="product-details">
                    <div class="product-name" @click="goToProductDetail(product.productId)">{{ product.productName }}</div>
                    <div class="product-price">¥{{ product.price }} × {{ product.quantity }}</div>
                  </div>
                </div>
                
                <div class="order-amount">
                  <div class="amount-label">总计：</div>
                  <div class="amount-value">¥{{ order.totalAmount }}</div>
                </div>
              </div>
              
              <div class="order-footer">
                <el-button size="small" class="detail-btn" @click="viewOrderDetail(order.orderNo)">
                  <el-icon><InfoFilled /></el-icon>
                  查看详情
                </el-button>
                
                <template v-if="order.status === '待付款'">
                  <el-button type="danger" plain size="small" class="cancel-btn" @click="cancelOrder(order.productList[0].id)">
                    <el-icon><Close /></el-icon>
                    取消订单
                  </el-button>
                  <el-button type="primary" size="small" class="pay-btn" @click="payOrder(order)">
                    <el-icon><Wallet /></el-icon>
                    立即付款
                  </el-button>
                </template>
                
                <template v-if="order.status === '待收货'">
                  <el-button type="success" size="small" class="receive-btn" @click="confirmReceipt(order.productList[0].id)">
                    <el-icon><Check /></el-icon>
                    确认收货
                  </el-button>
                  <el-button v-if="!order.isReturned" type="warning" size="small" @click="applyReturn(order)">
                    <el-icon><SwitchButton /></el-icon>
                    申请退货
                  </el-button>
                  <el-button v-else type="info" size="small" @click="viewReturn(order)">
                    <el-icon><Document /></el-icon>
                    查看退货
                  </el-button>
                </template>
                
                <template v-if="order.status === '已完成'">
                  <!-- 已退货显示查看退货按钮 -->
                  <el-button v-if="order.isReturned" type="info" size="small" @click="viewReturn(order)">
                    <el-icon><Document /></el-icon>
                    查看退货
                  </el-button>
                  
                  <!-- 已评价显示查看评价按钮 -->
                  <el-button v-if="order.isReviewed" type="info" size="small" @click="viewReview(order)">
                    <el-icon><ChatLineSquare /></el-icon>
                    查看评价
                  </el-button>
                  
                  <!-- 未退货显示申请退货按钮 -->
                  <el-button v-if="!order.isReturned" type="warning" size="small" @click="applyReturn(order)">
                    <el-icon><SwitchButton /></el-icon>
                    申请退货
                  </el-button>
                  
                  <!-- 未评价显示评价商品按钮 -->
                  <el-button v-if="!order.isReviewed" type="primary" size="small" @click="writeReview(order)">
                    <el-icon><ChatDotRound /></el-icon>
                    评价商品
                  </el-button>
                </template>
                
                <template v-if="order.status === '已完成' || order.status === '已取消'">
                  <el-button type="danger" plain size="small" class="delete-btn" @click="deleteOrder(order.id)">
                    <el-icon><Delete /></el-icon>
                    删除订单
                  </el-button>
                </template>
              </div>
            </div>
          </div>
          
          <div class="pagination-container" v-if="total > 0">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="payDialogVisible"
      title="订单支付"
      width="550px"
      class="pay-dialog"
      destroy-on-close
    >
      <div class="pay-dialog-content">
        <div class="pay-order-info">
          <h3 class="order-title">
            <el-icon><Goods /></el-icon>
            订单信息
          </h3>
          <div class="order-details">
            <p><span class="label">订单号：</span>{{ currentOrder?.orderNo }}</p>
            <p><span class="label">商品：</span>{{ currentOrder?.productList?.[0]?.productName }}</p>
            <p><span class="label">数量：</span>{{ currentOrder?.productList?.[0]?.quantity }}</p>
            <p class="pay-amount"><span class="label">支付金额：</span><span>¥{{ currentOrder?.totalAmount }}</span></p>
          </div>
        </div>
        
        <div class="pay-methods">
          <h3 class="method-title">
            <el-icon><CreditCard /></el-icon>
            选择支付方式
          </h3>
          <el-radio-group v-model="paymentMethod" class="payment-options">
            <el-radio label="微信支付">
              <div class="payment-option">
                <i class="payment-icon wechat">微信</i>
                <span>微信支付</span>
              </div>
            </el-radio>
            <el-radio label="支付宝">
              <div class="payment-option">
                <i class="payment-icon alipay">支付宝</i>
                <span>支付宝</span>
              </div>
            </el-radio>
            <el-radio label="货到付款">
              <div class="payment-option">
                <i class="payment-icon cod">货到付款</i>
                <span>货到付款</span>
              </div>
            </el-radio>
          </el-radio-group>
        </div>
        
        <div class="pay-qrcode" v-if="paymentMethod === '微信支付' || paymentMethod === '支付宝'">
<!--          <p>请扫描二维码支付</p>-->
<!--          <el-image src="https://via.placeholder.com/200" fit="contain" class="qrcode-image"></el-image>-->
<!--          <p class="pay-note">此处为模拟支付，实际开发中接入真实支付接口</p>-->
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmPayment" :loading="paying" class="confirm-btn">
            <el-icon><Check /></el-icon>
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 退货申请对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      :title="isViewingReturn ? '退货详情' : '申请退货'"
      width="500px"
      class="return-dialog"
      destroy-on-close
    >
      <div v-if="isViewingReturn && currentReturn" class="return-info">
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
                :preview-src-list="currentReturn.images.split(',').map(img => getImageUrl(img))"
                fit="cover"
                class="return-image"
              />
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-form
        v-else
        ref="returnFormRef"
        :model="returnForm"
        :rules="returnRules"
        label-width="80px"
      >
        <el-form-item label="退货原因" prop="reason">
          <el-input
            v-model="returnForm.reason"
            type="textarea"
            :rows="2"
            placeholder="请输入退货原因"
          ></el-input>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="returnForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述退货原因"
          ></el-input>
        </el-form-item>
        <el-form-item label="图片凭证" prop="images">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="true"
            :on-remove="handleUploadRemove"
            :before-upload="beforeUpload"
            :http-request="customUploadImage"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传3张图片，每张不超过2MB</div>
        </el-form-item>
        <el-form-item label="退款金额">
          <div class="refund-amount">¥{{ returnForm.refundAmount }}</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button
            v-if="!isViewingReturn"
            type="primary"
            @click="submitReturn"
            :loading="submitting"
          >提交申请</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="isViewingReview ? '评价详情' : '商品评价'"
      width="500px"
      class="review-dialog"
      destroy-on-close
    >
      <div v-if="isViewingReview && currentReview" class="review-info">
        <el-descriptions :column="1" border>
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
                :preview-src-list="currentReview.images.split(',').map(img => getImageUrl(img))"
                fit="cover"
                class="review-image"
              />
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReview.reply" label="商家回复">
            {{ currentReview.reply }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-form
        v-else
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="80px"
      >
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating"></el-rate>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="3"
            placeholder="请输入您的评价内容"
          ></el-input>
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="true"
            :on-remove="handleReviewImageRemove"
            :before-upload="beforeReviewImageUpload"
            :http-request="customUploadReviewImage"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传3张图片，每张不超过2MB</div>
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="reviewForm.isAnonymous"></el-switch>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button
            v-if="!isViewingReview"
            type="primary"
            @click="submitReview"
            :loading="submitting"
          >提交评价</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import CountdownTimer from '@/components/CountdownTimer.vue'
import { 
  ShoppingCart, 
  InfoFilled, 
  Close, 
  Wallet, 
  Check, 
  Delete, 
  Goods, 
  CreditCard,
  SwitchButton,
  ChatDotRound,
  Document,
  ChatLineSquare,
  Plus,
  Timer
} from '@element-plus/icons-vue'

// 路由
const router = useRouter()
const userStore = useUserStore()

// 数据定义
const loading = ref(false)
const orderList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const currentStatus = ref('')

// 支付相关
const payDialogVisible = ref(false)
const currentOrder = ref(null)
const paymentMethod = ref('微信支付')
const paying = ref(false)

// 退货相关
const returnDialogVisible = ref(false)
const isViewingReturn = ref(false)
const currentReturn = ref(null)
const returnFormRef = ref(null)
const submitting = ref(false)
const returnForm = reactive({
  orderId: '',
  userId: '',
  reason: '',
  description: '',
  refundAmount: 0,
  images: '',
  status: '待处理'  // 新增退货状态默认值
})

const returnRules = {
  reason: [
    { required: true, message: '请输入退货原因', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入详细说明', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

// 评价相关
const reviewDialogVisible = ref(false)
const isViewingReview = ref(false)
const currentReview = ref(null)
const reviewFormRef = ref(null)
const reviewForm = reactive({
  orderId: '',
  userId: '',
  productId: '',
  rating: 5,
  content: '',
  isAnonymous: false,
  images: ''
})

const reviewRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, max: 500, message: '长度在 5 到 500 个字符', trigger: 'blur' }
  ],
  userId: [
    { required: true, message: '用户ID不能为空', trigger: 'blur' }
  ],
  productId: [
    { required: true, message: '商品ID不能为空', trigger: 'blur' }
  ],
  orderId: [
    { required: true, message: '订单ID不能为空', trigger: 'blur' }
  ]
}

// 订单状态选项
const orderStatusOptions = [
  { value: '待付款', label: '待付款' },
  { value: '待发货', label: '待发货' },
  { value: '待收货', label: '待收货' },
  { value: '已完成', label: '已完成' },
  { value: '已取消', label: '已取消' }
]

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    await request.get('/order/merged/page', {
      status: currentStatus.value,
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

// 处理订单过期
const handleOrderExpired = (order) => {
  ElMessage.warning('订单已超时未支付，系统将自动取消')
  
  // 调用后端API取消订单
  request.put(`/order/${order.productList[0].id}/cancel?userId=${userStore.userInfo.id}`, null, {
    successMsg: '订单已自动取消',
    onSuccess: () => {
      // 更新订单状态为已取消
      order.status = '已取消'
      // 重新获取订单列表
      fetchOrders()
    },
    errorMsg: '订单取消失败',
    showError: true
  })
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

// 将日期对象格式化为字符串（用于发送到后端）
const formatDateString = (date) => {
  if (!date) return ''
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 查看订单详情
const viewOrderDetail = (orderNo) => {
  router.push({ name: 'OrderDetail', params: { orderNo: orderNo } })
}

// 取消订单
const cancelOrder = (id) => {
  ElMessageBox.confirm(
    '确定要取消该订单吗？',
    '取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.put(`/order/${id}/cancel?userId=${userStore.userInfo.id}`, null, {
        successMsg: '订单已取消',
        onSuccess: () => {
          fetchOrders()
        }
      })
    } catch (error) {
      console.error('取消订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 支付订单
const payOrder = (order) => {
  currentOrder.value = order
  payDialogVisible.value = true
}

// 确认支付
const confirmPayment = async () => {
  paying.value = true
  try {
    // 使用订单号批量更新订单状态
    await request.put(`/order/${currentOrder.value.orderNo}/status`, null, {
      params: {
        status: '待发货'
      },
      successMsg: '支付成功',
      onSuccess: () => {
        payDialogVisible.value = false
        fetchOrders()
      }
    })
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败，请稍后重试')
  } finally {
    paying.value = false
  }
}

// 确认收货
const confirmReceipt = (id) => {
  ElMessageBox.confirm(
    '确认已收到商品？',
    '确认收货',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      const now = formatDateString(new Date());
      console.log('确认收货时间:', now);
      
      // 发送格式化后的时间
      await request.put(`/order/${id}/confirm?userId=${userStore.userInfo.id}`, {
        receiptTime: now
      }, {
        successMsg: '已确认收货',
        onSuccess: () => {
          fetchOrders()
        }
      })
    } catch (error) {
      console.error('确认收货失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 删除订单
const deleteOrder = (id) => {
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
      await request.delete(`/order/${id}`, {
        successMsg: '订单已删除',
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

// 跳转到商品列表页
const goToProductList = () => {
  router.push('/product')
}

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push({ name: 'ProductDetail', params: { id: productId } })
}

// 处理状态变化
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

// 申请退货
const applyReturn = (order) => {
  currentOrder.value = order
  isViewingReturn.value = false
  returnForm.orderId = order.id
  returnForm.userId = userStore.userInfo.id
  returnForm.refundAmount = order.totalAmount
  returnForm.reason = ''
  returnForm.description = ''
  returnForm.images = ''
  returnDialogVisible.value = true
}

// 提交退货申请
const submitReturn = async () => {
  if (!returnFormRef.value) return
  
  await returnFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 构造退货数据
        const returnData = {
          ...returnForm,
          createTime: formatDateString(new Date())
        }
        
        await request.post('/order/return', returnData, {
          successMsg: '退货申请已提交',
          onSuccess: () => {
            returnDialogVisible.value = false
            fetchOrders()
          }
        })
      } catch (error) {
        console.error('提交退货申请失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 查看退货详情
const viewReturn = async (order) => {
  currentOrder.value = order
  isViewingReturn.value = true
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

// 评价商品
const writeReview = (order) => {
  currentOrder.value = order
  isViewingReview.value = false
  reviewForm.orderId = order.id
  reviewForm.userId = userStore.userInfo.id
  reviewForm.productId = order.productId
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.isAnonymous = false
  reviewForm.images = ''
  reviewDialogVisible.value = true
}

// 提交评价
const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await request.post('/order/review', reviewForm, {
          successMsg: '评价已提交',
          onSuccess: () => {
            reviewDialogVisible.value = false
            fetchOrders()
          }
        })
      } catch (error) {
        console.error('提交评价失败:', error)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 查看评价详情
const viewReview = async (order) => {
  currentOrder.value = order
  isViewingReview.value = true
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

// 自定义图片上传方法
const customUploadImage = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem("token") || "",
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: "图片上传成功",
      // 自定义错误消息
      errorMsg: "图片上传失败",
      // 成功回调
      onSuccess: (data) => {
        // 更新退货表单的图片列表
        returnForm.images = returnForm.images 
          ? returnForm.images + ',' + data
          : data;

        // 通知上传成功
        options.onSuccess({ data });
      },
      // 错误回调
      onError: (error) => {
        console.error("图片上传错误:", error);
        options.onError(new Error(error.message || "上传失败"));
      },
    };

    // 发送上传请求
    await request.post("/file/upload/img", formData, uploadOptions);
  } catch (error) {
    options.onError(error);
    console.error("图片上传过程发生错误:", error);
  }
};

// 修改 handleUploadRemove 方法
const handleUploadRemove = (file) => {
  const imageUrl = file.response?.data;
  if (imageUrl && returnForm.images) {
    const images = returnForm.images.split(',');
    const index = images.indexOf(imageUrl);
    if (index > -1) {
      images.splice(index, 1);
      returnForm.images = images.join(',');
    }
  }
};

// 修改 beforeUpload 方法
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }

  const images = returnForm.images ? returnForm.images.split(',') : [];
  if (images.length >= 3) {
    ElMessage.error('最多只能上传3张图片!');
    return false;
  }
  return true;
};

// 自定义评价图片上传方法
const customUploadReviewImage = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem("token") || "",
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: "图片上传成功",
      // 自定义错误消息
      errorMsg: "图片上传失败",
      // 成功回调
      onSuccess: (data) => {
        // 更新评价表单的图片列表
        reviewForm.images = reviewForm.images 
          ? reviewForm.images + ',' + data
          : data;

        // 通知上传成功
        options.onSuccess({ data });
      },
      // 错误回调
      onError: (error) => {
        console.error("图片上传错误:", error);
        options.onError(new Error(error.message || "上传失败"));
      },
    };

    // 发送上传请求
    await request.post("/file/upload/img", formData, uploadOptions);
  } catch (error) {
    options.onError(error);
    console.error("图片上传过程发生错误:", error);
  }
};

// 评价图片删除处理
const handleReviewImageRemove = (file) => {
  const imageUrl = file.response?.data;
  if (imageUrl && reviewForm.images) {
    const images = reviewForm.images.split(',');
    const index = images.indexOf(imageUrl);
    if (index > -1) {
      images.splice(index, 1);
      reviewForm.images = images.join(',');
    }
  }
};

// 评价图片上传前验证
const beforeReviewImageUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!');
    return false;
  }

  const images = reviewForm.images ? reviewForm.images.split(',') : [];
  if (images.length >= 3) {
    ElMessage.error('最多只能上传3张图片!');
    return false;
  }
  return true;
};

// 页面加载时获取订单列表
onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.orders-page {
  position: relative;
  min-height: 100vh;
  padding: 0;
  overflow: hidden;
}

.page-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  
  .decoration-bubble {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
    opacity: 0.05;
    
    &.bubble-1 {
      width: 300px;
      height: 300px;
      top: -150px;
      left: -100px;
    }
    
    &.bubble-2 {
      width: 200px;
      height: 200px;
      bottom: 10%;
      right: -50px;
    }
    
    &.bubble-3 {
      width: 150px;
      height: 150px;
      top: 40%;
      right: 10%;
    }
  }
  
  .decoration-paw {
    position: absolute;
    font-size: 60px;
    opacity: 0.1;
    
    &.paw-1 {
      top: 10%;
      left: 5%;
      animation: float 15s infinite ease-in-out;
    }
    
    &.paw-2 {
      top: 60%;
      right: 15%;
      animation: float 18s infinite ease-in-out reverse;
    }
    
    &.paw-3 {
      bottom: 10%;
      left: 20%;
      animation: float 20s infinite ease-in-out;
    }
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-banner {
  position: relative;
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
  padding: 60px 40px;
  overflow: hidden;
  text-align: center;
  z-index: 1;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-bottom: 40px;
  
  .breadcrumb {
    margin-bottom: 20px;
    
    :deep(.el-breadcrumb__item) {
      font-size: 14px;
      color: #2e7d32;
      opacity: 0.8;
    }
    
    :deep(.el-breadcrumb__separator) {
      color: #2e7d32;
      opacity: 0.5;
    }
  }
  
  h1.page-title {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 36px;
    color: #2e7d32;
    margin-bottom: 10px;
  }
  
  p.page-subtitle {
    margin: 0;
    color: #2e7d32;
    opacity: 0.9;
    font-size: 16px;
  }
  
  .banner-decoration {
    position: absolute;
    bottom: 0;
    right: 0;
    left: 0;
    display: flex;
    justify-content: space-around;
    
    .decoration-paw {
      font-size: 40px;
      opacity: 0.2;
    }
  }
}

.orders-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 60px;
}

.orders-card {
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1) !important;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(102, 187, 106, 0.3);
  
  :deep(.el-card__header) {
    padding: 25px;
    border-bottom: 1px solid rgba(102, 187, 106, 0.3);
    background: transparent;
  }
  
  :deep(.el-card__body) {
    padding: 25px;
  }
}

.orders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #2e7d32;
    position: relative;
    padding-left: 15px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #66bb6a;
      border-radius: 2px;
    }
  }
  
  .status-filter {
    width: 150px;
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px #dcdfe6 inset;
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 1px #FFA726 inset;
      }
    }
  }
  
  .status-option {
    display: flex;
    align-items: center;
    
    .status-tag {
      margin-left: 0;
      width: 100%;
      text-align: center;
    }
  }
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.order-item {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 187, 106, 0.2);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 30px rgba(102, 187, 106, 0.15);
    border-color: rgba(102, 187, 106, 0.5);
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 20px;
  margin-bottom: 25px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .order-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .order-no {
      font-size: 18px;
      font-weight: 700;
      color: #683e35;
    }
    
    .order-time {
      font-size: 14px;
      color: #909399;
    }
  }
  
  .order-status {
    padding: 8px 20px;
    border-radius: 25px;
    font-size: 14px;
    font-weight: 600;
  }
}

.order-content {
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin-bottom: 25px;
  
  .product-info {
      display: flex;
      align-items: center;
      width: 100%;
      padding: 15px 0;
      border-bottom: 1px solid rgba(102, 187, 106, 0.3);
      
      .product-image {
        width: 80px;
        height: 80px;
        object-fit: cover;
        border-radius: 8px;
        margin-right: 15px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        transition: transform 0.3s ease;
        cursor: pointer;
        
        &:hover {
          transform: scale(1.05);
        }
      }
      
      .product-details {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: center;
        
        .product-name {
          font-size: 14px;
          color: #2e7d32;
          margin-bottom: 5px;
          line-height: 1.4;
          font-weight: 500;
          cursor: pointer;
          
          &:hover {
            color: #66bb6a;
          }
        }
        
        .product-price {
          font-size: 16px;
          font-weight: 600;
          color: #66bb6a;
        }
      }
  }
  
  .order-amount {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 5px;
    padding: 15px 20px 0 20px;
    margin-bottom: 25px;
    
    .amount-label {
      font-size: 16px;
      color: #6E4C1E;
      font-weight: 500;
    }
    
    .amount-value {
      color: #FFA726;
      font-size: 28px;
      font-weight: bold;
    }
  }
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 25px;
  border-top: 1px solid rgba(102, 187, 106, 0.3);
  background-color: rgba(255, 255, 255, 0.5);
  
  .detail-btn, .cancel-btn, .pay-btn, .receive-btn, .delete-btn, .apply-return-btn, .view-return-btn, .view-review-btn, .write-review-btn {
    padding: 8px 20px;
    border-radius: 25px;
    font-size: 14px;
    transition: all 0.3s ease;
    font-weight: 600;
    
    .el-icon {
      margin-right: 5px;
    }
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
  
  .pay-btn, .receive-btn, .write-review-btn {
    background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
    border: 1px solid rgba(102, 187, 106, 0.5);
    color: #2e7d32;
    
    &:hover {
      background: linear-gradient(135deg, #b3e5fc 0%, #66bb6a 100%);
      border-color: #66bb6a;
      box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
    }
  }
  
  .cancel-btn, .delete-btn {
    background: linear-gradient(135deg, #FFB6B9 0%, #FF8787 100%);
    border: 1px solid rgba(255, 135, 135, 0.5);
    color: #721C24;
    
    &:hover {
      background: linear-gradient(135deg, #FF8787 0%, #FFB6B9 100%);
      border-color: #FF8787;
      box-shadow: 0 4px 12px rgba(255, 135, 135, 0.4);
    }
  }
  
  .detail-btn, .view-return-btn, .view-review-btn, .apply-return-btn {
    background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 100%);
    border: 1px solid rgba(102, 187, 106, 0.3);
    color: #2e7d32;
    
    &:hover {
      background: linear-gradient(135deg, #b3e5fc 0%, #e8f5e9 100%);
      border-color: #66bb6a;
      box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
    }
  }
}

.pagination-container {
  margin-top: 40px;
  padding: 25px 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 25px;
  box-shadow: 0 4px 20px rgba(102, 187, 106, 0.2);
  margin-bottom: 60px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    .el-pagination__sizes {
      margin-right: 20px;
    }
    
    .el-pagination__total {
      margin-right: 20px;
      color: #434343;
      font-weight: 600;
    }
    
    .el-pagination__jump {
      margin-left: 20px;
      color: #434343;
    }
    
    .el-pagination__prev, .el-pagination__next, .el-pager li {
      border-radius: 50%;
      width: 40px;
      height: 40px;
      line-height: 40px;
      margin: 0 5px;
      background-color: rgba(255, 255, 255, 0.8);
      border: 1px solid rgba(102, 187, 106, 0.3);
      color: #2e7d32;
      transition: all 0.3s ease;
      font-weight: 600;
      
      &:hover {
        background: rgba(102, 187, 106, 0.3);
        border-color: #66bb6a;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
      }
    }
    
    .el-pager li.is-active {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
      border-color: #66bb6a;
      color: #fff;
      
      &:hover {
        background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 187, 106, 0.4);
      }
    }
  }
}

.pay-dialog {
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 600;
    padding: 20px;
    margin-right: 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
  
  :deep(.el-radio__input.is-checked .el-radio__inner) {
    background-color: #FFA726;
    border-color: #FFA726;
  }
  
  :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: #FFA726;
  }
}

.pay-dialog-content {
  padding: 0;
}

.pay-order-info {
  margin-bottom: 30px;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  
  .order-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #333;
    margin-top: 0;
    margin-bottom: 15px;
    position: relative;
    padding-left: 15px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #FFB6C1;
      border-radius: 2px;
    }
    
    .el-icon {
      color: #FFA726;
    }
  }
  
  .order-details {
    p {
      margin: 8px 0;
      display: flex;
      
      .label {
        width: 80px;
        color: #666;
      }
    }
    
    .pay-amount {
      font-weight: bold;
      
      span:last-child {
        color: #f56c6c;
        font-size: 20px;
      }
    }
  }
}

.pay-methods {
  margin-bottom: 30px;
  
  .method-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #333;
    margin-bottom: 15px;
    position: relative;
    padding-left: 15px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #FFB6C1;
      border-radius: 2px;
    }
    
    .el-icon {
      color: #FFA726;
    }
  }
  
  .payment-options {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .payment-option {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .payment-icon {
      display: inline-block;
      width: 20px;
      height: 20px;
      border-radius: 4px;
      color: white;
      font-size: 12px;
      line-height: 20px;
      text-align: center;
      
      &.wechat {
        background-color: #07C160;
      }
      
      &.alipay {
        background-color: #1677FF;
      }
      
      &.cod {
        background-color: #FF9800;
      }
    }
  }
}

.pay-qrcode {
  text-align: center;
  margin: 20px 0;
  
  .qrcode-image {
    width: 200px;
    height: 200px;
    margin: 15px auto;
    border: 1px solid #f0f0f0;
    padding: 10px;
    border-radius: 8px;
    transition: transform 0.3s ease;
    
    &:hover {
      transform: scale(1.03);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
  }
  
  .pay-note {
    color: #909399;
    font-size: 12px;
    margin-top: 10px;
  }
}

.confirm-btn {
  min-width: 120px;
  background-color: #FFA726;
  border-color: #FFA726;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.3);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.action-btn {
  min-width: 150px;
  background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
  border: 1px solid rgba(102, 187, 106, 0.5);
  color: #2e7d32;
  border-radius: 25px;
  transition: all 0.3s ease;
  font-weight: 600;
  
  &:hover {
    background: linear-gradient(135deg, #b3e5fc 0%, #66bb6a 100%);
    border-color: #66bb6a;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.return-dialog,
.review-dialog {
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 600;
    padding: 20px;
    margin-right: 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
  
  :deep(.el-rate) {
    margin-top: 8px;
    
    .el-rate__icon {
      font-size: 24px;
    }
  }
}

.return-info,
.review-info {
  :deep(.el-descriptions) {
    margin-bottom: 20px;
    
    .el-descriptions__label {
      width: 100px;
      color: #666;
      font-weight: 600;
    }
    
    .el-descriptions__content {
      color: #333;
    }
  }
}

.return-image {
  width: 100px;
  height: 100px;
  margin-right: 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover {
    transform: scale(1.05);
  }
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.refund-amount {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

.review-images {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s;
  
  &:hover {
    transform: scale(1.05);
  }
}

@keyframes float {
  0% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.03;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.05;
  }
  100% {
    transform: translateY(0px) rotate(360deg);
    opacity: 0.03;
  }
}

@media (max-width: 1200px) {
  .orders-content {
    padding: 0 20px 60px;
  }
  
  .orders-card {
    margin-bottom: 20px;
  }
}

@media (max-width: 992px) {
  .page-banner {
    padding: 40px 30px;
  }
  
  .page-title {
    font-size: 30px !important;
  }
  
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .order-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .order-amount {
    align-items: flex-start;
  }
}

@media (max-width: 768px) {
  .page-banner {
    padding: 30px 20px;
  }
  
  .page-title {
    font-size: 26px !important;
  }
  
  .orders-content {
    padding: 0 15px 40px;
  }
  
  .order-item {
    padding: 15px;
  }
  
  .order-footer {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .product-image {
    width: 80px !important;
    height: 80px !important;
  }
  
  .product-name {
    font-size: 14px !important;
  }
  
  .product-price {
    font-size: 16px !important;
  }
}

@media (max-width: 480px) {
  .page-banner {
    padding: 20px 15px;
  }
  
  .page-title {
    font-size: 22px !important;
  }
  
  .page-subtitle {
    font-size: 14px !important;
  }
  
  .orders-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .status-filter {
    width: 100% !important;
  }
  
  .order-info {
    gap: 5px !important;
  }
  
  .order-no {
    font-size: 16px !important;
  }
  
  .order-time {
    font-size: 12px !important;
  }
  
  .order-status {
    padding: 6px 16px !important;
    font-size: 12px !important;
  }
  
  .amount-value {
    font-size: 22px !important;
  }
  
  .detail-btn, .cancel-btn, .pay-btn, .receive-btn, .delete-btn, .apply-return-btn, .view-return-btn, .view-review-btn, .write-review-btn {
    padding: 6px 16px !important;
    font-size: 12px !important;
  }
  
  .action-btn {
    min-width: 120px !important;
  }
}
</style> 