<template>
  <div class="orders-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item @click="$router.push('/product')">宠物物资</el-breadcrumb-item>
            <el-breadcrumb-item>我的订单</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>我的订单</h1>
        <p>查看和管理您的所有订单记录</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="orders-content">
      
      <el-card shadow="never" class="orders-card">
        <template #header>
          <div class="card-header">
            <h2>订单记录</h2>
            <div class="card-actions">
              <el-radio-group v-model="currentStatus" @change="handleStatusChange" size="small">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="待付款">待付款</el-radio-button>
                <el-radio-button label="待发货">待发货</el-radio-button>
                <el-radio-button label="待收货">待收货</el-radio-button>
                <el-radio-button label="已完成">已完成</el-radio-button>
                <el-radio-button label="已取消">已取消</el-radio-button>
              </el-radio-group>
              <el-button type="primary" size="small" @click="handleRefresh" :icon="Refresh">刷新</el-button>
            </div>
          </div>
        </template>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <!-- 错误状态 -->
        <div v-else-if="loadError" class="error-container">
          <el-result
            icon="error"
            title="数据加载失败"
            sub-title="无法获取订单记录，请检查网络连接或稍后重试"
          >
            <template #extra>
              <el-button type="primary" @click="handleRefresh">重新加载</el-button>
            </template>
          </el-result>
        </div>
        
        <!-- 空数据状态 -->
        <div v-else-if="orderList.length === 0" class="empty-container">
          <el-empty description="暂无订单记录" :image-size="200">
            <template #description>
              <p>您还没有任何订单记录</p>
              <p class="empty-hint">快去浏览商品，找到适合您的商品后下单吧</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToProductList">
              <el-icon><ShoppingCart /></el-icon>
              去选购商品
            </el-button>
          </el-empty>
        </div>
        
        <!-- 数据列表 -->
        <div v-else class="orders-list">
          <div class="order-list">
            <el-card 
              v-for="item in orderList" 
              :key="item.id" 
              class="order-item"
              shadow="never"
            >
              <div class="order-header">
                <div class="order-info">
                  <h3>{{ item.productList?.[0]?.productName || '订单' }}</h3>
                  <el-tag :type="getStatusType(item.status)" effect="light">{{ item.status }}</el-tag>
                </div>
                <div class="order-time">{{ formatDateTime(item.createTime) }}</div>
              </div>
              
              <div class="order-content">
                <div class="product-info">
                  <div class="product-item" v-for="product in (item.productList || [])" :key="product.productId">
                    <div class="product-image">
                      <el-image 
                        :src="getImageUrl(product.productImage)" 
                        fit="cover"
                        :preview-src-list="[getImageUrl(product.productImage)]"
                      />
                    </div>
                    <div class="product-details">
                      <div class="product-info">
                        <div class="product-name">{{ product.productName }}</div>
                        <div class="product-spec">{{ product.spec || '默认规格' }}</div>
                      </div>
                      <div class="product-quantity">x{{ product.quantity }}</div>
                    </div>
                  </div>
                  <div v-if="!(item.productList && item.productList.length > 0)" class="no-products">
                    暂无商品信息
                  </div>
                </div>
                
                <div class="order-amount">
                  <div class="amount-label">订单金额</div>
                  <div class="amount-value">¥{{ item.totalAmount || 0 }}</div>
                </div>
              </div>
              
              <div class="order-footer">
                <div class="order-actions">
                  <el-button 
                    v-if="item.status === '待付款'"
                    type="danger" 
                    plain
                    size="small" 
                    @click="cancelOrder(item.id)"
                  >
                    <el-icon><Close /></el-icon>
                    取消订单
                  </el-button>
                  <el-button 
                    v-if="item.status === '待付款'"
                    type="primary" 
                    size="small" 
                    @click="payOrder(item)"
                  >
                    <el-icon><Wallet /></el-icon>
                    立即付款
                  </el-button>
                  <el-button 
                    v-if="item.status === '待收货'"
                    type="success" 
                    size="small" 
                    @click="confirmReceipt(item.id)"
                  >
                    <el-icon><Check /></el-icon>
                    确认收货
                  </el-button>
                  <el-button type="primary" size="small" @click="viewOrderDetail(item.orderNo)">
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>
        
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            layout="total, prev, pager, next, jumper"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
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
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { 
  Refresh, 
  ShoppingCart, 
  Close, 
  Wallet, 
  Check, 
  View, 
  Goods, 
  Money, 
  Calendar, 
  Phone,
  CreditCard
} from '@element-plus/icons-vue'

// 路由
const router = useRouter()
const userStore = useUserStore()

// 数据定义
const loading = ref(false)
const loadError = ref(false)
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

// 获取订单列表
const fetchOrders = async () => {
  // 确保初始化为空数组而不是undefined
  orderList.value = []
  
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('查看订单记录需要先登录，是否立即前往登录？', '提示', {
      confirmButtonText: '前往登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }).catch(() => {
      router.push('/product')
    })
    return
  }
  
  loading.value = true
  loadError.value = false
  try {
    // 构建查询参数
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    // 只有当status有值时才添加到参数中，避免发送空字符串或null给后端
    if (currentStatus.value) {
      params.status = currentStatus.value
    }
    
    await request.get('/order/merged/page', params, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          orderList.value = res.records
          total.value = res.total || 0
        } else {
          // 如果没有数据，设置为空数组
          orderList.value = []
          total.value = 0
        }
      }
    })
  } catch (error) {
    console.error('获取订单列表失败:', error)
    // 确保在出错时也将列表设置为空数组
    orderList.value = []
    total.value = 0
    loadError.value = true
    ElMessage.error('获取订单记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchOrders()
}

// 状态筛选变更
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

// 取消订单
const cancelOrder = (id) => {
  ElMessageBox.confirm(
    '确定要取消该订单吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.put(`/order/${id}/cancel?userId=${userStore.userInfo.id}`, {}, {
        successMsg: '订单已取消',
        onSuccess: () => {
          fetchOrders()
        }
      })
    } catch (error) {
      console.error('取消订单失败:', error)
    }
  }).catch(() => {})
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
      await request.put(`/order/${id}/confirm?userId=${userStore.userInfo.id}`, null, {
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

// 查看订单详情
const viewOrderDetail = (orderNo) => {
  router.push({ name: 'OrderDetail', params: { orderNo: orderNo } })
}

// 导航到商品列表
const goToProductList = () => {
  router.push('/product')
}

// 根据状态获取标签类型
const getStatusType = (status) => {
  const statusMap = {
    '待付款': 'warning',
    '待发货': 'info',
    '待收货': 'primary',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return statusMap[status] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '暂无数据'
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return '/default-product.jpg'
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return image.startsWith('http') ? image : `${baseAPI}${image}`
}

// 监听登录状态变化
watch(() => userStore.isLoggedIn, (newVal) => {
  if (newVal) {
    fetchOrders()
  }
})

// 页面加载时获取数据
onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchOrders()
  } else {
    ElMessageBox.confirm(
      '查看订单记录需要先登录，是否立即前往登录？',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      })
    }).catch(() => {
      router.push('/product')
    })
  }
})
</script>

<style lang="scss" scoped>
.orders-page {
  position: relative;
  min-height: 100vh;
  
  overflow: hidden;
}

.page-banner {
  position: relative;
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
  padding: 60px 40px;
  overflow: hidden;
  text-align: center;
  z-index: 1;
  border-radius: 24px;
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    position: relative;
    z-index: 2;
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
  
  .banner-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    z-index: 1;
    
    .decoration-paw {
      position: absolute;
      opacity: 0.1;
      animation: float 15s infinite ease-in-out;
      
      &.paw-1 {
        top: 20%;
        left: 10%;
        font-size: 50px;
        animation-delay: 0s;
      }
      
      &.paw-2 {
        top: 60%;
        right: 15%;
        font-size: 60px;
        animation-delay: -5s;
        animation-direction: reverse;
      }
      
      &.paw-3 {
        top: 30%;
        right: 30%;
        font-size: 40px;
        animation-delay: -2s;
      }
    }
  }
}

.orders-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.orders-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid rgba(102, 187, 106, 0.3);
  margin-bottom: 30px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15) !important;
    border-color: rgba(102, 187, 106, 0.5);
  }
  
  :deep(.el-card__header) {
    padding: 25px 30px;
    border-bottom: 1px solid rgba(102, 187, 106, 0.3);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
    color: #2e7d32;
    display: flex;
    align-items: center;
    gap: 10px;
    
    &::before {
      content: '🐾';
      font-size: 20px;
    }
  }
  
  .card-actions {
    display: flex;
    gap: 10px;
    align-items: center;
    
    :deep(.el-radio-button__inner) {
      padding: 8px 15px;
    }
    
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      background-color: #66bb6a;
      border-color: #66bb6a;
      box-shadow: -1px 0 0 0 #66bb6a;
    }
  }
}

.loading-container,
.empty-container,
.error-container {
  padding: 40px 0;
  text-align: center;
  border-radius: 8px;
  background-color: #f9f9f9;
  margin: 20px 0;
}

.error-container {
  background-color: #fff0f0;
}

.empty-hint {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
  margin-bottom: 20px;
}

.action-btn {
  background-color: #66bb6a;
  border-color: #66bb6a;
  transition: all 0.3s ease;
  
  &:hover {
    background-color: #43a047;
    border-color: #43a047;
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.orders-list {
  margin: 20px 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(240, 253, 240, 0.98) 100%);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 20px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(102, 187, 106, 0.2);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.12), 0 4px 12px rgba(0, 0, 0, 0.08);
    border-color: rgba(102, 187, 106, 0.4);
  }
  
  :deep(.el-card__body) {
    padding: 25px;
    position: relative;
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(102, 187, 106, 0.3);
  position: relative;
  
  &:before {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    width: 60px;
    height: 3px;
    background: linear-gradient(90deg, #66bb6a 0%, #b3e5fc 100%);
    border-radius: 2px;
  }
  
  .order-info {
    display: flex;
    align-items: center;
    gap: 15px;
    
    h3 {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      color: #2e7d32;
      background: linear-gradient(135deg, #66bb6a 0%, #2e7d32 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .order-time {
    font-size: 14px;
    color: #8D6E63;
    font-weight: 600;
    background-color: rgba(102, 187, 106, 0.05);
    padding: 8px 16px;
    border-radius: 20px;
    border: 1px solid rgba(102, 187, 106, 0.2);
  }
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
  
  .product-info {
    flex: 1;
    
    .product-item {
      display: flex;
      align-items: center;
      margin-bottom: 10px;
      gap: 12px;
      
      .product-image {
        width: 60px;
        height: 60px;
        flex-shrink: 0;
        border-radius: 8px;
        overflow: hidden;
        border: 1px solid rgba(102, 187, 106, 0.3);
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
      
      .product-details {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        .product-info {
          flex: 1;
          
          .product-name {
            font-size: 15px;
            font-weight: 600;
            color: #2e7d32;
            margin-bottom: 3px;
          }
          
          .product-spec {
            font-size: 13px;
            color: #8D6E63;
          }
        }
        
        .product-quantity {
          font-size: 14px;
          color: #8D6E63;
          font-weight: 600;
          flex-shrink: 0;
          margin-left: 15px;
        }
      }
    }
    
    .no-products {
      text-align: center;
      padding: 20px;
      color: #8D6E63;
      font-size: 14px;
      background-color: rgba(102, 187, 106, 0.1);
      border-radius: 8px;
    }
  }
  
  .order-amount {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 5px;
    padding: 15px 25px;
    background: linear-gradient(135deg, rgba(102, 187, 106, 0.02) 0%, rgba(102, 187, 106, 0.05) 100%);
    border-radius: 12px;
    border: 1px solid rgba(102, 187, 106, 0.2);
    flex-shrink: 0;
    min-width: 120px;
    
    .amount-label {
      font-size: 13px;
      color: #434343;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-weight: 600;
      text-align: center;
    }
    
    .amount-value {
      font-size: 24px;
      font-weight: 800;
      background: linear-gradient(135deg, #66bb6a 0%, #2e7d32 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      text-shadow: 0 2px 4px rgba(102, 187, 106, 0.3);
      text-align: center;
    }
  }
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 1px solid rgba(102, 187, 106, 0.3);
  margin-top: 20px;
  
  .order-actions {
    display: flex;
    gap: 15px;
    
    .el-button {
      border-radius: 28px;
      padding: 10px 25px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-weight: 600;
      font-size: 14px;
      position: relative;
      overflow: hidden;
      
      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: -100%;
        width: 100%;
        height: 100%;
        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
        transition: left 0.5s;
      }
      
      &:hover::before {
        left: 100%;
      }
      
      &.el-button--primary {
        background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
        border: 1px solid rgba(102, 187, 106, 0.5);
        color: #2e7d32;
        box-shadow: 0 4px 12px rgba(102, 187, 106, 0.2);
        
        &:hover {
          background: linear-gradient(135deg, #b3e5fc 0%, #66bb6a 100%);
          border-color: #66bb6a;
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(102, 187, 106, 0.4);
        }
      }
      
      &.el-button--danger {
        background: linear-gradient(135deg, #FFB6B9 0%, #FF8787 100%);
        border: 1px solid rgba(255, 135, 135, 0.5);
        color: #721C24;
        box-shadow: 0 4px 12px rgba(255, 135, 135, 0.2);
        
        &:hover {
          background: linear-gradient(135deg, #FF8787 0%, #FFB6B9 100%);
          border-color: #FF8787;
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(255, 135, 135, 0.4);
        }
      }
      
      &.el-button--success {
        background: linear-gradient(135deg, #90EE90 0%, #81C784 100%);
        border: 1px solid rgba(144, 238, 144, 0.5);
        color: #2e7d32;
        box-shadow: 0 4px 12px rgba(144, 238, 144, 0.2);
        
        &:hover {
          background: linear-gradient(135deg, #81C784 0%, #90EE90 100%);
          border-color: #90EE90;
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(144, 238, 144, 0.4);
        }
      }
      
      &.el-button--warning {
        background: linear-gradient(135deg, #FFD700 0%, #FFA000 100%);
        border: 1px solid rgba(255, 215, 0, 0.5);
        color: #721C24;
        box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
        
        &:hover {
          background: linear-gradient(135deg, #FFA000 0%, #FFD700 100%);
          border-color: #FFD700;
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(255, 215, 0, 0.4);
        }
      }
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 25px 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 25px;
  box-shadow: 0 4px 20px rgba(102, 187, 106, 0.2);
  margin-bottom: 60px;
  
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

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

@media screen and (max-width: 768px) {
  .page-banner {
    padding: 40px 20px;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .card-actions {
      width: 100%;
      flex-direction: column;
      align-items: flex-start;
      gap: 10px;
    }
  }
  
  .order-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
    
    .order-time {
      align-self: flex-end;
    }
  }
  
  .order-content {
    flex-direction: column;
    gap: 15px;
    
    .order-amount {
      width: 100%;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      padding-top: 15px;
      border-top: 1px solid rgba(102, 187, 106, 0.3);
    }
    
    .product-item {
      .product-image {
        width: 60px;
        height: 60px;
      }
    }
  }
  
  .order-footer {
    flex-direction: column;
    gap: 10px;
    
    .order-actions {
      width: 100%;
      flex-direction: column;
      align-items: flex-start;
      gap: 10px;
    }
  }
  
  .orders-card {
    margin: 0 10px 20px;
    
    :deep(.el-card__header) {
      padding: 20px;
    }
    
    :deep(.el-card__body) {
      padding: 20px;
    }
  }
}
</style>
