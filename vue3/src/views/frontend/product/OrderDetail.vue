<template>
  <div class="order-detail-page">
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
            <el-breadcrumb-item @click="$router.push('/order')">我的订单</el-breadcrumb-item>
            <el-breadcrumb-item>订单详情</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>订单详情</h1>
        <p>查看订单的详细信息和物流状态</p>
        <div class="banner-decoration">
          <div class="decoration-paw paw-1">🐾</div>
          <div class="decoration-paw paw-2">🐾</div>
          <div class="decoration-paw paw-3">🐾</div>
        </div>
      </div>
    </div>
    
    <div class="order-detail-content">
      
      <!-- 订单详情卡片 -->
      <el-card class="order-detail-card" shadow="never" v-loading="loading">
        <div v-if="order">
          <!-- 订单进度 -->
          <div class="order-status-section">
            <el-steps :active="getStatusStep(order.status)" finish-status="success" simple align-center>
              <el-step title="待付款" :icon="Wallet"></el-step>
              <el-step title="待发货" :icon="ShoppingCart"></el-step>
              <el-step title="待收货" :icon="Van"></el-step>
              <el-step title="已完成" :icon="CircleCheck"></el-step>
            </el-steps>
            
            <div class="current-status">
              <el-tag :type="getStatusTagType(order.status)" size="large">{{ order.status }}</el-tag>
              
              <!-- 添加支付倒计时 -->
              <div v-if="order && order.status === '待付款'" class="payment-countdown">
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
                  v-if="order.id"
                  :order-id="order.id"
                  @expired="handleOrderExpired"
                />
              </div>
              
              <div class="action-buttons">
                <template v-if="order.status === '待付款'">
                  <el-button type="danger" plain size="small" @click="cancelOrder">
                    <el-icon><Close /></el-icon>取消订单
                  </el-button>
                  <el-button type="primary" size="small" @click="payOrder">
                    <el-icon><CreditCard /></el-icon>立即付款
                  </el-button>
                </template>
                
                <template v-if="order.status === '待收货'">
                  <el-button type="success" size="small" @click="confirmReceipt">
                    <el-icon><Check /></el-icon>确认收货
                  </el-button>
                  <el-button type="info" size="small" @click="viewTracking">
                    <el-icon><Van /></el-icon>查看物流
                  </el-button>
                </template>
                
                <template v-if="order.status === '已完成' || order.status === '已取消'">
                  <el-button type="danger" plain size="small" @click="deleteOrder">
                    <el-icon><Delete /></el-icon>删除订单
                  </el-button>
                </template>
              </div>
            </div>
          </div>
          
          <!-- 订单信息 -->
          <div class="info-section">
            <div class="section-header">
              <el-icon><InfoFilled /></el-icon>
              <h3>订单信息</h3>
            </div>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
              <el-descriptions-item label="下单时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="订单状态">{{ order.status }}</el-descriptions-item>
              <el-descriptions-item label="订单备注">{{ order.remark || '无' }}</el-descriptions-item>
              <el-descriptions-item label="支付方式">{{ order.paymentMethod || '暂未支付' }}</el-descriptions-item>
              <el-descriptions-item label="支付时间" :span="order.paymentTime ? 1 : 2">
                {{ order.paymentTime ? formatDate(order.paymentTime) : '暂未支付' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 收货信息 -->
          <div class="info-section">
            <div class="section-header">
              <el-icon><Location /></el-icon>
              <h3>收货信息</h3>
            </div>
            
            <el-descriptions :column="1" border>
              <el-descriptions-item label="收货地址">{{ order.address }}</el-descriptions-item>
              <el-descriptions-item label="收货人">{{ order.contactName }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ order.contactPhone }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 物流信息 -->
          <div class="info-section" v-if="shipping">
            <div class="section-header">
              <el-icon><Van /></el-icon>
              <h3>物流信息</h3>
            </div>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="快递公司">{{ shipping.deliveryCompany }}</el-descriptions-item>
              <el-descriptions-item label="快递单号">{{ shipping.trackingNo }}</el-descriptions-item>
              <el-descriptions-item label="发货时间">{{ formatDate(shipping.deliveryTime) }}</el-descriptions-item>
              <el-descriptions-item label="签收时间">{{ shipping.receiptTime ? formatDate(shipping.receiptTime) : '未签收' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 商品信息 -->
          <div class="info-section">
            <div class="section-header">
              <el-icon><Goods /></el-icon>
              <h3>商品信息</h3>
            </div>
            
            <div v-if="orders && orders.length > 0" class="product-list">
              <div v-for="item in orders" :key="item.id" class="product-card">
                <div class="product-info">
                  <el-image
                    :src="getImageUrl(item.productImage)"
                    fit="cover"
                    class="product-image"
                    @click="viewProduct(item.productId)"
                    lazy>
                  </el-image>
                  <div class="product-details">
                    <div class="product-name" @click="viewProduct(item.productId)">{{ item.productName }}</div>
                    <div class="product-spec">{{ item.spec || '默认规格' }}</div>
                    <div class="product-price">¥{{ item.price }}</div>
                  </div>
                  <div class="product-actions">
                    <div class="product-quantity">x{{ item.quantity }}</div>
                    <div class="product-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
                  </div>
                </div>
              </div>
              
              <!-- 订单总金额 -->
              <div class="order-total-section">
                <div class="order-total">
                  <div class="total-item">
                    <span class="total-label">商品总金额</span>
                    <span class="total-value">¥{{ getTotalAmount() }}</span>
                  </div>
                  
                  <div class="total-item shipping">
                    <span class="total-label">运费</span>
                    <span class="total-value">¥{{ getShippingFee() }}</span>
                  </div>
                  
                  <div class="total-item discount">
                    <span class="total-label">优惠</span>
                    <span class="total-value">-¥{{ getDiscount() }}</span>
                  </div>
                  
                  <div class="total-item final-amount">
                    <span class="total-label">实付款</span>
                    <span class="total-value highlight">¥{{ getFinalAmount() }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 退货信息 -->
          <div class="info-section" v-if="order.isReturned">
            <div class="section-header">
              <el-icon><SwitchButton /></el-icon>
              <h3>退货信息</h3>
            </div>
            
            <el-descriptions :column="2" border v-if="returnInfo">
              <el-descriptions-item label="退货原因">{{ returnInfo.reason }}</el-descriptions-item>
              <el-descriptions-item label="退货状态">
                <el-tag :type="getReturnStatusType(returnInfo.status)">{{ returnInfo.status }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ formatDate(returnInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="处理时间" v-if="returnInfo.processTime">
                {{ formatDate(returnInfo.processTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="处理备注" v-if="returnInfo.operatorNote">
                {{ returnInfo.operatorNote }}
              </el-descriptions-item>
              <el-descriptions-item label="申请前状态" v-if="returnInfo.previousOrderStatus">
                {{ returnInfo.previousOrderStatus }}
              </el-descriptions-item>
              <el-descriptions-item label="退款金额">¥{{ returnInfo.refundAmount }}</el-descriptions-item>
              <el-descriptions-item label="详细说明">{{ returnInfo.description }}</el-descriptions-item>
              <el-descriptions-item label="图片凭证" v-if="returnInfo.images" :span="2">
                <div class="return-images">
                  <el-image 
                    v-for="(img, index) in returnInfo.images.split(',')"
                    :key="index"
                    :src="getImageUrl(img)"
                    :preview-teleported="true"
                    :preview-src-list="returnInfo.images.split(',').map(img => getImageUrl(img))"
                    fit="cover"
                    class="return-image"
                  />
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 评价信息 -->
          <div class="info-section" v-if="order.isReviewed">
            <div class="section-header">
              <el-icon><ChatDotRound /></el-icon>
              <h3>评价信息</h3>
            </div>
            
            <el-descriptions :column="2" border v-if="reviewInfo">
              <el-descriptions-item label="评分">
                <el-rate v-model="reviewInfo.rating" disabled></el-rate>
              </el-descriptions-item>
              <el-descriptions-item label="评价时间">{{ formatDate(reviewInfo.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="评价内容">{{ reviewInfo.content }}</el-descriptions-item>
              <el-descriptions-item label="商家回复" v-if="reviewInfo.reply">
                {{ reviewInfo.reply }}
              </el-descriptions-item>
              <el-descriptions-item label="评价图片" v-if="reviewInfo.images" :span="2">
                <div class="review-images">
                  <el-image 
                    v-for="(img, index) in reviewInfo.images.split(',')"
                    :key="index"
                    :src="getImageUrl(img)"
                    :preview-teleported="true"
                    :preview-src-list="reviewInfo.images.split(',').map(img => getImageUrl(img))"
                    fit="cover"
                    class="review-image"
                  />
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        
        <el-empty v-else description="订单不存在或已被删除"></el-empty>
      </el-card>
    </div>
    
    <!-- 支付对话框 -->
    <el-dialog
      v-model="payDialogVisible"
      title="订单支付"
      width="500px"
      class="pay-dialog">
      <div class="pay-dialog-content">
        <div class="pay-order-info">
          <h3 class="order-title">
            <el-icon><InfoFilled /></el-icon>
            订单信息
          </h3>
          <div class="order-details">
            <p><span class="label">订单号：</span>{{ order?.orderNo }}</p>
            <div><span class="label">商品列表：</span></div>
            <div class="product-list">
              <div v-for="(item, index) in orders" :key="index" class="product-item">
                <div class="product-info">
                  <img v-if="item.productImage" :src="getImageUrl(item.productImage)" :alt="item.productName" class="product-image" />
                  <div class="product-details">
                    <div class="product-name">{{ item.productName }}</div>
                    <div class="product-quantity">数量：{{ item.quantity }}</div>
                    <div class="product-price">单价：¥{{ (item.price || 0).toFixed(2) }}</div>
                  </div>
                </div>
                <div class="product-subtotal">¥{{ ((item.price || 0) * (item.quantity || 1)).toFixed(2) }}</div>
              </div>
            </div>
            <p class="pay-amount"><span class="label">支付金额：</span><span>¥{{ getTotalAmount().toFixed(2) }}</span></p>
          </div>
        </div>
        
        <div class="pay-methods">
          <h3 class="method-title">
            <el-icon><Wallet /></el-icon>
            选择支付方式
          </h3>
          <div class="payment-options">
            <el-radio-group v-model="paymentMethod">
              <el-radio label="微信支付">
                <div class="payment-option">
                  <span class="payment-icon wechat">W</span>
                  微信支付
                </div>
              </el-radio>
              <el-radio label="支付宝">
                <div class="payment-option">
                  <span class="payment-icon alipay">A</span>
                  支付宝
                </div>
              </el-radio>
              <el-radio label="货到付款">
                <div class="payment-option">
                  <span class="payment-icon cod">C</span>
                  货到付款
                </div>
              </el-radio>
            </el-radio-group>
          </div>
        </div>
        
        <div class="pay-qrcode" v-if="paymentMethod === '微信支付' || paymentMethod === '支付宝'">
<!--          <p>请扫描二维码支付</p>-->
<!--          <div class="qrcode-image">-->
<!--            <el-image src="https://via.placeholder.com/200" fit="contain"></el-image>-->
<!--          </div>-->
<!--          <p class="pay-note">此处为模拟支付，实际开发中接入真实支付接口</p>-->
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" class="confirm-btn" @click="confirmPayment" :loading="paying">
            <el-icon><CreditCard /></el-icon>
            确认支付
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 物流追踪对话框 -->
    <el-dialog
      v-model="trackingDialogVisible"
      title="物流追踪"
      width="500px"
      class="tracking-dialog">
      <div v-if="shipping" class="tracking-dialog-content">
        <el-timeline>
          <el-timeline-item
            v-if="order.status === '已完成' || (shipping && shipping.shippingStatus === '已签收')"
            timestamp="签收"
            placement="top"
            type="success"
            :hollow="false">
            <h4>包裹已签收</h4>
            <p>{{ shipping && shipping.receiptTime ? formatDate(shipping.receiptTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            v-if="shipping && shipping.shippingStatus === '已退回'"
            timestamp="退回"
            placement="top"
            type="danger"
            :hollow="false">
            <h4>包裹已退回</h4>
            <p>{{ shipping && shipping.updateTime ? formatDate(shipping.updateTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="运输中"
            placement="top"
            type="primary"
            :hollow="order?.status !== '待收货' || (shipping && shipping.shippingStatus === '已退回')">
            <h4>包裹正在配送</h4>
            <p>{{ shipping && shipping.deliveryTime ? formatDate(shipping.deliveryTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="已发货"
            placement="top"
            type="info">
            <h4>商家已发货</h4>
            <p>{{ shipping && shipping.deliveryTime ? formatDate(shipping.deliveryTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="待发货"
            placement="top"
            :type="order?.status === '待发货' ? 'warning' : 'info'">
            <h4>订单已支付，等待商家发货</h4>
            <p>{{ order && order.paymentTime ? formatDate(order.paymentTime) : '' }}</p>
          </el-timeline-item>
          
          <el-timeline-item
            timestamp="已下单"
            placement="top"
            type="info">
            <h4>订单已创建</h4>
            <p>{{ order && order.createTime ? formatDate(order.createTime) : '' }}</p>
          </el-timeline-item>
        </el-timeline>
        
        <div class="tracking-info">
          <h3 class="tracking-title">
            <el-icon><InfoFilled /></el-icon>
            快递信息
          </h3>
          <p><strong>物流公司：</strong>{{ shipping?.deliveryCompany || '未知' }}</p>
          <p><strong>物流单号：</strong>{{ shipping?.trackingNo || '未知' }}</p>
          <p class="tracking-note">您也可以到物流公司官网查询物流进度</p>
        </div>
      </div>
      
      <div v-else class="no-tracking-data">
        <el-empty description="暂无物流信息"></el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Wallet, 
  ShoppingCart, 
  Van, 
  CircleCheck, 
  Close, 
  CreditCard, 
  Check, 
  Delete, 
  InfoFilled, 
  Location, 
  Goods, 
  ArrowLeft,
  SwitchButton,
  ChatDotRound,
  Timer
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import CountdownTimer from '@/components/CountdownTimer.vue'

// 路由
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const orderNo = route.params.orderNo

// 数据定义
const loading = ref(false)
const order = ref(null)
const orders = ref([]) // 存储同一个订单号下的所有商品订单
const shipping = ref(null)
const returnInfo = ref(null)
const reviewInfo = ref(null)

// 支付相关
const payDialogVisible = ref(false)
const paymentMethod = ref('微信支付')
const paying = ref(false)

// 物流相关
const trackingDialogVisible = ref(false)

// 获取订单详情
const fetchOrderDetail = async () => {
  loading.value = true
  try {
    // 根据订单号获取所有订单
    await request.get(`/order/user?orderNo=${orderNo}`, null, {
      onSuccess: (res) => {
        if (res && res.length > 0) {
          orders.value = res
          order.value = res[0] // 使用第一个订单作为主要订单信息
          
          // 如果订单状态是待收货或已完成，查询物流信息
          if (res[0].status === '待收货' || res[0].status === '已完成') {
            fetchShippingInfo()
          } else {
            shipping.value = null
          }
          // 如果订单已退货，获取退货信息
          if (res[0].isReturned) {
            fetchReturnInfo()
          } else {
            returnInfo.value = null
          }
          // 如果订单已评价，获取评价信息
          if (res[0].isReviewed) {
            fetchReviewInfo()
          } else {
            reviewInfo.value = null
          }
        } else {
          // 如果没有订单数据，重置所有状态
          orders.value = []
          order.value = null
          shipping.value = null
          returnInfo.value = null
          reviewInfo.value = null
        }
      }
    })
  } catch (error) {
    console.error('获取订单详情失败:', error)
    // 错误时重置所有数据
    orders.value = []
    order.value = null
    shipping.value = null
    returnInfo.value = null
    reviewInfo.value = null
  } finally {
    loading.value = false
  }
}



// 获取物流信息
const fetchShippingInfo = async () => {
  try {
    await request.get(`/shipping/order/${order.value.id}`, null, {
      onSuccess: (res) => {
        shipping.value = res
      }
    })
  } catch (error) {
    console.error('获取物流信息失败:', error)
  }
}

// 获取退货信息
const fetchReturnInfo = async () => {
  try {
    await request.get(`/order/return/order/${order.value.id}`, null, {
      onSuccess: (res) => {
        returnInfo.value = res
      }
    })
  } catch (error) {
    console.error('获取退货信息失败:', error)
  }
}

// 获取评价信息
const fetchReviewInfo = async () => {
  try {
    await request.get(`/order/review/order/${order.value.id}`, null, {
      onSuccess: (res) => {
        reviewInfo.value = res
      }
    })
  } catch (error) {
    console.error('获取评价信息失败:', error)
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

// 获取订单状态对应的步骤
const getStatusStep = (status) => {
  switch (status) {
    case '待付款': return 0
    case '待发货': return 1
    case '待收货': return 2
    case '已完成': return 3
    case '已取消': return 0 // 取消订单回到第一步，但会显示为取消状态
    default: return 0
  }
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case '待付款': return 'warning'
    case '待发货': return 'info'
    case '待收货': return 'primary'
    case '已完成': return 'success'
    case '已取消': return 'danger'
    default: return ''
  }
}

// 计算订单总金额
const getTotalAmount = () => {
  if (!orders.value || orders.value.length === 0) return 0
  return orders.value.reduce((total, item) => total + (item.totalAmount || 0), 0)
}

// 计算运费
const getShippingFee = () => {
  // 这里可以根据实际业务逻辑计算运费
  // 目前暂时返回0
  return 0
}

// 计算优惠金额
const getDiscount = () => {
  // 这里可以根据实际业务逻辑计算优惠金额
  // 目前暂时返回0
  return 0
}

// 计算最终金额
const getFinalAmount = () => {
  // 最终金额 = 商品总金额 + 运费 - 优惠
  const totalAmount = getTotalAmount()
  const shippingFee = getShippingFee()
  const discount = getDiscount()
  return Math.max(0, totalAmount + shippingFee - discount)
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 查看商品详情
const viewProduct = (productId) => {
  router.push({ name: 'ProductDetail', params: { id: productId } })
}

// 取消订单
const cancelOrder = () => {
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
      await request.put(`/order/${order.value.id}/cancel?userId=${userStore.userInfo.id}`, null, {
        successMsg: '订单已取消',
        onSuccess: () => {
          fetchOrderDetail()
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
const payOrder = () => {
  payDialogVisible.value = true
}

// 页面加载时检查订单状态，如果是待付款则自动打开支付对话框
onMounted(() => {
  fetchOrderDetail()
  // 延迟一下确保数据加载完成
  setTimeout(() => {
    if (orders.value && orders.value.length > 0 && orders.value[0].status === '待付款') {
      payDialogVisible.value = true
    }
  }, 500)
})

// 确认支付
const confirmPayment = async () => {
  paying.value = true
  try {
    await request.put(`/order/${order.value.orderNo}/status`, null, {
      params: {
        status: '待发货'
      },
      successMsg: '支付成功',
      onSuccess: () => {
        payDialogVisible.value = false
        fetchOrderDetail()
      }
    })
  } catch (error) {
    console.error('支付失败:', error)
  } finally {
    paying.value = false
  }
}

// 确认收货
const confirmReceipt = () => {
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
      await request.put(`/order/${order.value.id}/confirm?userId=${userStore.userInfo.id}`, null, {
        successMsg: '已确认收货',
        onSuccess: () => {
          fetchOrderDetail()
        }
      })
    } catch (error) {
      console.error('确认收货失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 查看物流
const viewTracking = () => {
  if (!shipping.value) {
    fetchShippingInfo()
  }
  trackingDialogVisible.value = true
}

// 删除订单
const deleteOrder = () => {
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
      await request.delete(`/order/${order.value.id}`, {
        successMsg: '订单已删除',
        onSuccess: () => {
          router.push('/order')
        }
      })
    } catch (error) {
      console.error('删除订单失败:', error)
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 获取退货状态类型
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

// 处理订单过期
const handleOrderExpired = () => {
  ElMessage.warning('订单已超时未支付，系统将自动取消')
  
  // 调用后端API取消订单
  if (order.value && order.value.id) {
    request.put(`/order/${order.value.id}/cancel?userId=${userStore.userInfo.id}`, null, {
      successMsg: '订单已自动取消',
      onSuccess: () => {
        // 重新获取订单信息
        fetchOrderDetail()
      },
      errorMsg: '订单取消失败',
      showError: true
    })
  }
}

// 页面加载时获取订单详情
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style lang="scss" scoped>
.order-detail-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-banner {
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
  padding: 60px 40px;
  overflow: hidden;
  text-align: center;
  z-index: 1;
  border-radius: 24px;
  text-align: center;
  position: relative;
  overflow: hidden;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  h1 {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 36px;
    color: white;
    margin-bottom: 10px;
    font-weight: 700;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.9);
    font-size: 18px;
    opacity: 0.9;
  }
  
  .breadcrumb {
    margin-bottom: 20px;
    display: flex;
    justify-content: flex-start;
    
    :deep(.el-breadcrumb__item) {
      color: rgba(255, 255, 255, 0.8);
      font-size: 14px;
      
      &:last-child {
        color: white;
        font-weight: 600;
      }
      
      a {
        color: rgba(255, 255, 255, 0.8);
        text-decoration: none;
        
        &:hover {
          color: white;
        }
      }
    }
  }
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
    opacity: 0.1;
    animation: float 15s infinite ease-in-out;
    
    &.paw-1 {
      font-size: 60px;
      top: 10%;
      left: 5%;
      animation-delay: 0s;
    }
    
    &.paw-2 {
      font-size: 40px;
      top: 60%;
      right: 15%;
      animation-delay: 3s;
    }
    
    &.paw-3 {
      font-size: 80px;
      bottom: 15%;
      left: 10%;
      animation-delay: 6s;
    }
  }
}

.banner-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100px;
  pointer-events: none;
  
  .decoration-paw {
    position: absolute;
    opacity: 0.2;
    
    &.paw-1 {
      font-size: 40px;
      bottom: -10px;
      left: 10%;
      transform: rotate(-20deg);
    }
    
    &.paw-2 {
      font-size: 30px;
      bottom: 10px;
      left: 50%;
      transform: translateX(-50%);
    }
    
    &.paw-3 {
      font-size: 40px;
      bottom: -10px;
      right: 10%;
      transform: rotate(20deg);
    }
  }
}

.order-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px;
  position: relative;
  z-index: 1;
}

.breadcrumb-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  :deep(.el-breadcrumb__item) {
    .el-breadcrumb__inner {
      color: #666;
      font-weight: normal;
      
      &.is-link:hover {
        color: #FFA726;
      }
    }
    
    &:last-child .el-breadcrumb__inner {
      color: #683e35;
      font-weight: 600;
    }
  }
  
  .back-button {
    color: #683e35;
    border-color: #FFA726;
    
    &:hover {
      color: white;
      background-color: #FFA726;
      border-color: #FFA726;
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.order-detail-card {
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1) !important;
  margin-bottom: 30px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(102, 187, 106, 0.3);
  
  :deep(.el-card__body) {
    padding: 25px;
  }
}

.order-status-section {
  margin-bottom: 30px;
  
  :deep(.el-steps) {
    margin-bottom: 25px;
    
    .el-step__title {
      font-size: 14px;
      font-weight: 500;
      color: #666;
      transition: all 0.3s ease;
    }
    
    .el-step__icon {
      color: #66bb6a;
      border-color: #66bb6a;
    }
    
    .el-step.is-success {
      .el-step__title, .el-step__icon {
        color: #66bb6a;
      }
    }
    
    .el-step.is-process {
      .el-step__title {
        color: #2e7d32;
        font-weight: 600;
      }
      
      .el-step__icon {
        background-color: #66bb6a;
        color: white;
      }
    }
  }
}

.current-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  
  .payment-countdown {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: #ff6b6b;
    
    .countdown-label {
      display: flex;
      align-items: center;
      gap: 2px;
      cursor: help;
    }
  }
  
  :deep(.el-tag) {
    padding: 8px 15px;
    font-size: 14px;
    font-weight: 600;
    border-radius: 20px;
    
    &.el-tag--success {
      background-color: #f0f9eb;
      border-color: #e1f3d8;
    }
    
    &.el-tag--warning {
      background-color: #fdf6ec;
      border-color: #faecd8;
    }
    
    &.el-tag--danger {
      background-color: #fef0f0;
      border-color: #fde2e2;
    }
    
    &.el-tag--primary {
      background-color: #ecf5ff;
      border-color: #d9ecff;
    }
  }
  
  .action-buttons {
      display: flex;
      gap: 10px;
      
      .el-button {
        transition: all 0.3s ease;
        border-radius: 25px;
        font-weight: 600;
        
        .el-icon {
          margin-right: 5px;
        }
        
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
      
      .el-button--primary {
        background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
        border: 1px solid rgba(102, 187, 106, 0.5);
        color: #2e7d32;
        
        &:hover {
          background: linear-gradient(135deg, #b3e5fc 0%, #66bb6a 100%);
          border-color: #66bb6a;
          box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
        }
      }
      
      .el-button--success {
        background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
        border: 1px solid rgba(102, 187, 106, 0.5);
        color: #2e7d32;
        
        &:hover {
          background: linear-gradient(135deg, #b3e5fc 0%, #66bb6a 100%);
          border-color: #66bb6a;
          box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
        }
      }
      
      .el-button--danger {
        background: linear-gradient(135deg, #FFB6B9 0%, #FF8787 100%);
        border: 1px solid rgba(255, 135, 135, 0.5);
        color: #721C24;
        
        &:hover {
          background: linear-gradient(135deg, #FF8787 0%, #FFB6B9 100%);
          border-color: #FF8787;
          box-shadow: 0 4px 12px rgba(255, 135, 135, 0.4);
        }
      }
    }
}

.info-section {
  margin-bottom: 30px;
  animation: fadeIn 0.5s ease-in-out;
  
  .section-header {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .el-icon {
      color: #66bb6a;
      font-size: 20px;
    }
    
    h3 {
      margin: 0 0 0 10px;
      font-size: 18px;
      font-weight: 600;
      color: #2e7d32;
    }
  }
  
  :deep(.el-descriptions) {
    --el-descriptions-item-bordered-label-background: #fafafa;
    
    .el-descriptions__body {
      background-color: #fafafa;
    }
    
    .el-descriptions__label {
      color: #666;
      font-weight: 600;
    }
    
    .el-descriptions__content {
      color: #333;
    }
  }
  
  :deep(.el-rate) {
    margin-top: 8px;
    
    .el-rate__icon {
      font-size: 24px;
      margin-right: 6px;
      
      &.hover {
        transform: scale(1.15);
      }
    }
  }
}

.product-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(240, 253, 240, 0.98) 100%);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  transition: all 0.3s ease;
  border: 1px solid rgba(102, 187, 106, 0.2);
  margin-bottom: 15px;
  width: 100%;
  box-sizing: border-box;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(102, 187, 106, 0.15);
    border-color: rgba(102, 187, 106, 0.4);
  }
  
  .product-info {
    display: flex;
    align-items: center;
    gap: 20px;
    width: 100%;
    
    .product-image {
      width: 90px;
      height: 90px;
      border-radius: 12px;
      object-fit: cover;
      box-shadow: 0 4px 12px rgba(102, 187, 106, 0.15);
      transition: all 0.3s ease;
      cursor: pointer;
      flex-shrink: 0;
      border: 2px solid rgba(102, 187, 106, 0.2);
      
      &:hover {
        transform: scale(1.08);
        box-shadow: 0 6px 16px rgba(102, 187, 106, 0.2);
        border-color: rgba(102, 187, 106, 0.5);
      }
    }
    
    .product-details {
      flex: 1;
      min-width: 0;
      padding: 5px 0;
      
      .product-name {
        font-size: 16px;
        font-weight: 700;
        margin-bottom: 8px;
        color: #2e7d32;
        cursor: pointer;
        transition: color 0.3s ease;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        
        &:hover {
          color: #66bb6a;
          text-decoration: underline;
        }
      }
      
      .product-spec {
        font-size: 13px;
        color: #8D6E63;
        margin-bottom: 5px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .product-price {
        font-size: 15px;
        font-weight: 700;
        color: #66bb6a;
      }
    }
    
    .product-actions {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: 12px;
      flex-shrink: 0;
      
      .product-quantity {
        font-size: 14px;
        color: #8D6E63;
        font-weight: 600;
        padding: 6px 15px;
        background-color: rgba(102, 187, 106, 0.1);
        border-radius: 20px;
        border: 1px solid rgba(102, 187, 106, 0.3);
      }
      
      .product-subtotal {
        font-size: 18px;
        font-weight: 800;
        color: #66bb6a;
        background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
  }
}

/* 订单总金额区域 */
.order-total-section {
  margin-top: 25px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(102, 187, 106, 0.05) 0%, rgba(184, 233, 134, 0.05) 100%);
  border-radius: 16px;
  border: 1px solid rgba(102, 187, 106, 0.2);
  box-shadow: 0 4px 12px rgba(102, 187, 106, 0.08);
  width: 100%;
  box-sizing: border-box;
}

.order-total {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
  
  .total-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: rgba(102, 187, 106, 0.05);
      padding: 8px 15px;
      border-radius: 8px;
    }
    
    .total-label {
      font-size: 14px;
      color: #666;
      font-weight: 600;
    }
    
    .total-value {
      font-size: 14px;
      color: #333;
      font-weight: 700;
    }
    
    &.shipping {
      border-top: 1px dashed rgba(102, 187, 106, 0.2);
      padding-top: 15px;
    }
    
    &.final-amount {
      border-top: 1px solid rgba(102, 187, 106, 0.3);
      padding-top: 18px;
      margin-top: 5px;
      
      .total-label {
        font-size: 16px;
        color: #2e7d32;
        font-weight: 800;
      }
      
      .total-value {
        font-size: 18px;
        color: #66bb6a;
        font-weight: 900;
      }
    }
    
    .total-value.highlight {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
}

/* 商品列表容器 */
.product-list {
  width: 100%;
  box-sizing: border-box;
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
    color: #2e7d32;
    margin-top: 0;
    margin-bottom: 15px;
    
    .el-icon {
      color: #66bb6a;
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
      
      .product-list {
        margin: 10px 0 20px 0px;
        background: #fafafa;
        border-radius: 8px;
        padding: 10px;
      }
      
      .product-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px;
        margin-bottom: 8px;
        background: white;
        border-radius: 6px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      }
      
      .product-info {
        display: flex;
        align-items: center;
      }
      
      .product-image {
        width: 50px;
        height: 50px;
        object-fit: cover;
        border-radius: 4px;
        margin-right: 12px;
        border: 1px solid #e8e8e8;
      }
      
      .product-details {
        display: flex;
        flex-direction: column;
      }
      
      .product-name {
        font-weight: 500;
        margin-bottom: 4px;
        color: #333;
      }
      
      .product-quantity, .product-price {
        font-size: 12px;
        color: #999;
        margin: 2px 0;
      }
      
      .product-subtotal {
        font-weight: bold;
        color: #ff4d4f;
        font-size: 14px;
      }
      
      .pay-amount {
        margin-top: 15px;
        font-size: 16px;
        color: #ff4d4f;
        font-weight: bold;
        text-align: right;
        padding-right: 10px;
      }
    }
}

.pay-methods {
  margin-bottom: 30px;
  
  .method-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #2e7d32;
    margin-bottom: 15px;
    
    .el-icon {
      color: #66bb6a;
    }
  }
  
  .payment-options {
    display: flex;
    flex-direction: column;
    gap: 10px;
    
    :deep(.el-radio__input.is-checked .el-radio__inner) {
      background-color: #FFA726;
      border-color: #FFA726;
    }
    
    :deep(.el-radio__input.is-checked + .el-radio__label) {
      color: #FFA726;
    }
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
  }
  
  .pay-note {
    color: #909399;
    font-size: 12px;
    margin-top: 10px;
  }
}

.confirm-btn {
  min-width: 120px;
  background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
  border: 1px solid rgba(102, 187, 106, 0.5);
  color: #2e7d32;
  border-radius: 25px;
  font-weight: 600;
  transition: all 0.3s ease;
  
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

.tracking-dialog {
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
}

.tracking-dialog-content {
  padding: 0;
  
  :deep(.el-timeline) {
    padding-left: 0;
    
    .el-timeline-item__node {
      background-color: #FFA726;
    }
    
    .el-timeline-item__node--primary {
      background-color: #409EFF;
    }
    
    .el-timeline-item__node--success {
      background-color: #67C23A;
    }
    
    .el-timeline-item__tail {
      border-left: 2px solid #e8e8e8;
    }
    
    .el-timeline-item__wrapper {
      padding-left: 20px;
    }
    
    .el-timeline-item__timestamp {
      color: #909399;
    }
    
    .el-timeline-item__content {
      h4 {
        color: #333;
        margin: 0 0 5px 0;
      }
      
      p {
        color: #666;
        margin: 0;
        font-size: 14px;
      }
    }
  }
}

.tracking-info {
  margin-top: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  
  .tracking-title {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #2e7d32;
    margin-top: 0;
    margin-bottom: 15px;
    
    .el-icon {
      color: #66bb6a;
    }
  }
  
  p {
    margin: 8px 0;
    color: #666;
    
    strong {
      color: #333;
      margin-right: 5px;
    }
  }
  
  .tracking-note {
    color: #909399;
    font-size: 12px;
    margin-top: 15px;
  }
}

.no-tracking-data {
  padding: 30px;
  text-align: center;
}

.return-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .return-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .review-image {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: scale(1.05);
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media screen and (max-width: 768px) {
  .page-header {
    .page-title {
      font-size: 26px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .breadcrumb-nav {
    flex-direction: column;
    align-items: flex-start;
    
    .back-button {
      margin-top: 10px;
      align-self: flex-end;
    }
  }
  
  .current-status {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .action-buttons {
      width: 100%;
      justify-content: flex-end;
    }
  }
  
  .product-info {
    flex-direction: column;
    
    .product-image {
      width: 120px;
      height: 120px;
      margin-bottom: 15px;
    }
    
    .product-details {
      margin-left: 0;
    }
  }
  
  .pay-dialog, .tracking-dialog {
    width: 90% !important;
    
    :deep(.el-dialog__body) {
      padding: 20px;
    }
  }
  
  :deep(.el-descriptions) {
    .el-descriptions__body {
      .el-descriptions__table {
        display: block;
        
        .el-descriptions__cell {
          display: block;
          width: 100%;
          
          &.is-bordered-label {
            width: 100%;
          }
        }
      }
    }
  }
}
</style> 