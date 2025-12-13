<template>
  <div class="cart-page">
    <!-- 页面装饰 -->
    <div class="page-decoration">
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
            <el-breadcrumb-item>我的购物车</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>我的购物车</h1>
        <p>查看和管理您的购物车商品</p>
        <div class="banner-decoration">
          <div class="decoration-paw paw-1">🐾</div>
          <div class="decoration-paw paw-2">🐾</div>
          <div class="decoration-paw paw-3">🐾</div>
        </div>
      </div>
    </div>
    
    <div class="cart-content">
      <el-card shadow="never" class="cart-card">
        <template #header>
          <div class="cart-header">
            <h2>购物车</h2>
            <div class="card-actions">
              <el-button 
                v-if="cartData.items && cartData.items.length > 0" 
                type="danger" 
                plain 
                size="small" 
                @click="clearCart"
                class="clear-btn"
              >
                <el-icon><Delete /></el-icon>
                清空购物车
              </el-button>
              <el-button type="primary" size="small" @click="fetchCart" :icon="Refresh">刷新</el-button>
            </div>
          </div>
        </template>
        
        <div v-loading="loading">
          <!-- 购物车为空 -->
          <div v-if="!cartData.items || cartData.items.length === 0" class="empty-cart">
            <div class="empty-content">
              <div class="empty-icon">
                <el-icon size="120">
                  <ShoppingCart />
                </el-icon>
              </div>
              <h3>购物车是空的</h3>
              <p>快去挑选您喜欢的宠物物资吧！</p>
              <el-button type="primary" @click="goToProductList" class="go-shopping-btn">
                <el-icon><Shop /></el-icon>
                去购物
              </el-button>
            </div>
          </div>
          
          <!-- 购物车有商品 -->
          <div v-else>
            <div class="cart-controls">
              <el-checkbox v-model="selectAll" @change="handleSelectAllChange" class="select-all">
                全选
              </el-checkbox>
              <el-button 
                v-if="selectedItems.length > 0" 
                type="danger" 
                plain 
                size="small" 
                @click="removeSelected"
                class="remove-selected-btn"
              >
                <el-icon><Delete /></el-icon>
                删除选中商品
              </el-button>
            </div>
            
            <el-table :data="cartData.items" stripe style="width: 100%" class="cart-table" row-key="productId">
              <el-table-column type="selection" width="55" :reserve-selection="true">
                <template #default="{ row }">
                  <el-checkbox v-model="row.checked" @change="handleItemSelectChange(row)"></el-checkbox>
                </template>
              </el-table-column>
              
              <el-table-column label="商品图片">
                <template #default="{ row }">
                  <div class="image-container">
                    <el-image 
                      :src="getImageUrl(row.productImage)" 
                      fit="cover" 
                      class="product-image"
                      :preview-teleported="true"
                      :preview-src-list="[getImageUrl(row.productImage)]">
                    </el-image>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="productName" label="商品名称">
                <template #default="{ row }">
                  <div class="product-info">
                    <el-link @click="goToProductDetail(row.productId)" :underline="false" class="product-name">
                      {{ row.productName }}
                    </el-link>
                  </div>
                </template>
              </el-table-column>
              
              <el-table-column prop="price" label="单价" width="120">
                <template #default="{ row }">
                  <span class="price">¥{{ row.price }}</span>
                </template>
              </el-table-column>
              
              <el-table-column label="数量" width="200">
                <template #default="{ row }">
                  <el-input-number 
                    v-model="row.quantity" 
                    :min="1" 
                    :precision="0"
                    size="small"
                    class="quantity-input"
                    @change="(value) => updateQuantity(row.productId, value)">
                  </el-input-number>
                </template>
              </el-table-column>
              
              <el-table-column prop="totalAmount" label="小计" width="120">
                <template #default="{ row }">
                  <span class="total-price">¥{{ row.totalAmount }}</span>
                </template>
              </el-table-column>
              
              <el-table-column fixed="right" label="操作" width="120">
                <template #default="{ row }">
                  <el-button 
                    type="danger" 
                    plain 
                    size="small" 
                    class="delete-btn"
                    @click="removeFromCart(row.productId)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="cart-footer">
              <div class="cart-total">
                <span>已选：</span>
                <span class="selected-quantity">{{ selectedTotalQuantity }}件商品</span>
                <span class="total-price">总计：¥{{ selectedTotalAmount }}</span>
              </div>
              
              <div class="cart-actions">
                <el-button @click="goToProductList" class="continue-btn">
                  <el-icon><Back /></el-icon>
                  继续购物
                </el-button>
                <el-button type="primary" @click="checkout" class="checkout-btn">
                  <el-icon><Shop /></el-icon>
                  结算
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 结算对话框 -->
    <el-dialog
      v-model="checkoutDialogVisible"
      title="填写订单信息"
      width="600px"
      class="checkout-dialog"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="orderForm" label-width="120px" ref="orderFormRef" :rules="orderFormRules" class="order-form">
        <el-form-item label="收货地址" prop="address">
          <el-input v-model="orderForm.address" placeholder="请输入详细收货地址" clearable>
            <template #prefix>
              <el-icon><Location /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="orderForm.contactName" placeholder="请输入联系人姓名" clearable>
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="orderForm.contactPhone" placeholder="请输入联系电话" clearable>
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="orderForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%;" clearable>
            <el-option label="微信支付" value="微信支付">
              <div class="payment-option">
                <i class="payment-icon wechat">微信</i>
                <span>微信支付</span>
              </div>
            </el-option>
            <el-option label="支付宝" value="支付宝">
              <div class="payment-option">
                <i class="payment-icon alipay">支付宝</i>
                <span>支付宝</span>
              </div>
            </el-option>
            <el-option label="货到付款" value="货到付款">
              <div class="payment-option">
                <i class="payment-icon cod">货到付款</i>
                <span>货到付款</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="订单备注" prop="remark">
          <el-input 
            v-model="orderForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入订单备注（选填）"
            clearable
          >
          </el-input>
        </el-form-item>
      </el-form>
      
      <div class="checkout-summary">
        <div class="order-items-summary">
          <h4 class="summary-title">
            <el-icon><ShoppingCart /></el-icon>
            订单商品
          </h4>
          <div class="summary-content">
            <div v-for="(item, index) in selectedItems" :key="index" class="item-summary">
              <el-image 
                :src="getImageUrl(item.productImage)" 
                fit="cover" 
                class="item-image"
                :preview-teleported="true"
                :preview-src-list="[getImageUrl(item.productImage)]">
              </el-image>
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-quantity">(x{{ item.quantity }})</span>
              <span class="item-price">¥{{ item.totalAmount }}</span>
            </div>
          </div>
        </div>
        
        <div class="checkout-total">
          <span>订单总计：</span>
          <span class="total-price">¥{{ selectedTotalAmount }}</span>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkoutDialogVisible = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" @click="confirmOrder" :loading="submitting" class="confirm-btn">
            <el-icon><Check /></el-icon>
            提交订单
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { 
  Delete, 
  Back, 
  Shop, 
  Location, 
  User, 
  Phone, 
  Check, 
  ShoppingCart,
  Refresh
} from '@element-plus/icons-vue'

// 路由
const router = useRouter()

// 用户状态
const userStore = useUserStore()

// 组件卸载标记
const isUnmounted = ref(false)

// 数据定义
const loading = ref(false)
const cartData = ref({
  items: [],
  totalQuantity: 0,
  totalAmount: 0
})

// 选择相关
const selectAll = ref(false)
const selectedTotalAmount = ref(0)
const selectedTotalQuantity = ref(0)

// 结算相关
const checkoutDialogVisible = ref(false)
const submitting = ref(false)
const orderFormRef = ref(null)

// 订单表单
const orderForm = reactive({
  address: '',
  contactName: '',
  contactPhone: '',
  paymentMethod: '',
  remark: ''
})

// 表单验证规则
const orderFormRules = {
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 获取购物车数据
const fetchCart = async () => {
  loading.value = true
  try {
    await request.get('/cart', null, {
      onSuccess: (res) => {
        // 检查组件是否已卸载
        if (isUnmounted.value) return
        
        // 为每个商品添加checked属性
        res.items = res.items.map(item => ({
          ...item,
          checked: false
        }))
        cartData.value = res
        updateSelectedInfo()
      }
    })
  } catch (error) {
    console.error('获取购物车失败:', error)
  } finally {
    // 只在组件未卸载时更新加载状态
    if (!isUnmounted.value) {
      loading.value = false
    }
  }
}

// 获取图片URL
const getImageUrl = (image) => {
  if (!image) return ''
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return image.startsWith('http') ? image : `${baseAPI}${image}`
}

// 更新购物车商品数量
const updateQuantity = async (productId, quantity) => {
  try {
    await request.put('/cart/update', null, {
      params: {
        productId,
        quantity
      },
      showDefaultMsg: false,
      onSuccess: (res) => {
        // 检查组件是否已卸载
        if (isUnmounted.value) return
        
        cartData.value = res
      }
    })
  } catch (error) {
    console.error('更新购物车失败:', error)
    if (!isUnmounted.value) {
      fetchCart() // 更新失败，重新获取购物车数据
    }
  }
}

// 从购物车移除商品
const removeFromCart = async (productId) => {
  try {
    await request.delete('/cart/remove', {
      params: {
        productId
      },
      successMsg: '商品已从购物车移除',
      onSuccess: (res) => {
        // 检查组件是否已卸载
        if (isUnmounted.value) return
        
        // 为每个商品添加checked属性
        res.items = res.items.map(item => ({
          ...item,
          checked: false
        }))
        cartData.value = res
        updateSelectedInfo()
      }
    })
  } catch (error) {
    console.error('移除商品失败:', error)
  }
}

// 移除选中商品
const removeSelected = async () => {
  const selectedIds = selectedItems.value.map(item => item.productId)
  
  if (selectedIds.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }
  
  try {
    await request.delete('/cart/removeBatch', {
      params: {
        productIds: selectedIds.join(',')
      },
      successMsg: '选中商品已从购物车移除',
      onSuccess: (res) => {
        // 检查组件是否已卸载
        if (isUnmounted.value) return
        
        // 为每个商品添加checked属性
        res.items = res.items.map(item => ({
          ...item,
          checked: false
        }))
        cartData.value = res
        updateSelectedInfo()
      }
    })
  } catch (error) {
    console.error('移除选中商品失败:', error)
  }
}

// 获取选中的商品
const selectedItems = computed(() => {
  return cartData.value.items.filter(item => item.checked)
})

// 更新选中商品信息
const updateSelectedInfo = () => {
  const selected = selectedItems.value
  selectedTotalQuantity.value = selected.reduce((sum, item) => sum + item.quantity, 0)
  selectedTotalAmount.value = selected.reduce((sum, item) => sum + item.totalAmount, 0)
  
  // 更新全选状态
  selectAll.value = cartData.value.items.length > 0 && selected.length === cartData.value.items.length
}

// 单个商品选择变化
const handleItemSelectChange = (row) => {
  updateSelectedInfo()
}

// 全选变化
const handleSelectAllChange = (val) => {
  cartData.value.items.forEach(item => {
    item.checked = val
  })
  updateSelectedInfo()
}

// 清空购物车
const clearCart = async () => {
  ElMessageBox.confirm(
    '确定要清空购物车吗？',
    '清空购物车',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete('/cart/clear', {
        successMsg: '购物车已清空',
        onSuccess: (res) => {
          // 检查组件是否已卸载
          if (isUnmounted.value) return
          
          cartData.value = res
        }
      })
    } catch (error) {
      console.error('清空购物车失败:', error)
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

// 结算
const checkout = () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm(
      '请先登录后再进行结算',
      '提示',
      {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push({ path: '/login', query: { redirect: '/cart' } })
    }).catch(() => {
      // 用户取消操作
    })
    return
  }
  
  // 打开结算对话框
  checkoutDialogVisible.value = true
  
  // 初始化表单数据
  if (userStore.userInfo) {
    orderForm.contactName = userStore.userInfo.name || ''
    orderForm.contactPhone = userStore.userInfo.phone || ''
  }
}

// 提交订单
const confirmOrder = async () => {
  if (!orderFormRef.value) return
  
  // 检查是否有选中商品
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  
  orderFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 构造订单数据 - 只包含选中的商品
        const orderData = {
          items: selectedItems.value,
          address: orderForm.address,
          contactName: orderForm.contactName,
          contactPhone: orderForm.contactPhone,
          paymentMethod: orderForm.paymentMethod,
          remark: orderForm.remark
        }
        
        // 提交订单
        await request.post('/order?userId=' + userStore.userInfo.id, orderData, {
          successMsg: '订单提交成功',
          onSuccess: (res) => {
            // 检查组件是否已卸载
            if (isUnmounted.value) return
            
            checkoutDialogVisible.value = false
            
            // 刷新购物车
            fetchCart()
            
            // 跳转到订单详情页，传递订单号
            if (res && res.length > 0) {
              router.push({ name: 'OrderDetail', params: { orderNo: res[0].orderNo } })
            }
          }
        })
      } catch (error) {
        console.error('提交订单失败:', error)
      } finally {
        // 只在组件未卸载时更新提交状态
        if (!isUnmounted.value) {
          submitting.value = false
        }
      }
    }
  })
}

// 页面加载时获取购物车数据
onMounted(() => {
  fetchCart()
})

// 页面卸载时清理资源
onUnmounted(() => {
  isUnmounted.value = true
})
</script>

<style lang="scss" scoped>
.cart-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.cart-controls {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px 15px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .select-all {
    font-weight: 500;
    color: #6E4C1E;
  }
  
  .remove-selected-btn {
    border-radius: 20px;
    font-size: 13px;
    padding: 6px 16px;
  }
}

.selected-quantity {
  color: #6E4C1E;
  margin-right: 15px;
}

// 调整表格列宽度以适应选择框
:deep(.el-table__column--selection) {
  width: 55px !important;
}

// 确保选择框居中
:deep(.el-table__cell) {
  text-align: center;
  
  &:first-child {
    text-align: center;
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

.cart-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-banner {
  position: relative;
  background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
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

.page-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px 0;
  border-bottom: 2px solid #ffc0cb;
  display: none;
}

.cart-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1) !important;
  border: 1px solid rgba(255, 182, 193, 0.3);
  margin-bottom: 30px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15) !important;
    border-color: rgba(255, 182, 193, 0.5);
  }
  
  :deep(.el-card__header) {
    padding: 25px 30px;
    border-bottom: 1px solid rgba(255, 182, 193, 0.3);
  }
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h2 {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
    color: #6E4C1E;
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
      background-color: #FFA726;
      border-color: #FFA726;
      box-shadow: -1px 0 0 0 #FFA726;
    }
  }
}

.cart-table {
  margin-bottom: 20px;
  
  :deep(.el-table) {
    border-radius: 16px;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
  }
  
  :deep(.el-table__header) {
    th {
      background-color: rgba(255, 182, 193, 0.1);
      color: #6E4C1E;
      font-weight: 700;
      padding: 15px;
      border-bottom: 2px solid rgba(255, 182, 193, 0.3);
    }
  }
  
  :deep(.el-table__body) {
    td {
      padding: 15px;
      border-bottom: 1px solid rgba(255, 182, 193, 0.1);
    }
  }
  
  :deep(.el-table__row) {
    transition: all 0.3s ease;
    
    &:hover {
      background-color: rgba(255, 182, 193, 0.1);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(255, 182, 193, 0.2);
    }
    
    &:last-child td {
      border-bottom: none;
    }
  }
  
  .image-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    padding: 8px;
  }
  
  .product-image {
    width: 100%;
    max-width: 100px;
    height: 100px;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: transform 0.3s ease;
    object-fit: cover;
    border: 2px solid rgba(255, 255, 255, 0.5);
    
    &:hover {
      transform: scale(1.1);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
    }
  }
  
  .product-name {
    color: #6E4C1E;
    font-weight: 600;
    font-size: 16px;
    
    &:hover {
      color: #FFA726;
    }
  }
  
  .price {
    color: #666;
    font-weight: 500;
  }
  
  .total-price {
    color: #FFA726;
    font-weight: bold;
    font-size: 18px;
  }
  
  .quantity-input {
    :deep(.el-input-number) {
      width: 160px !important;
      display: inline-flex;
      align-items: center;
      overflow: visible;
    }
    
    :deep(.el-input) {
      width: 90px !important;
      flex: 1;
    }
    
    :deep(.el-input__wrapper) {
      width: 100% !important;
      box-sizing: border-box;
    }
    
    :deep(.el-input__inner) {
      width: 100% !important;
      text-align: center;
      padding: 8px 10px !important;
      box-sizing: border-box;
      min-height: 32px;
    }
    
    :deep(.el-input-number__decrease),
    :deep(.el-input-number__increase) {
      width: 32px !important;
      height: 32px !important;
      flex-shrink: 0;
    }
    
    :deep(.el-input-number__decrease), 
    :deep(.el-input-number__increase) {
      background-color: rgba(255, 182, 193, 0.1);
      color: #6E4C1E;
      border-color: rgba(255, 182, 193, 0.3);
      
      &:hover {
        background-color: rgba(255, 182, 193, 0.2);
      }
    }
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px rgba(255, 182, 193, 0.3) inset;
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 1px #FFB6C1 inset;
      }
    }
  }
  
  .delete-btn {
    border-radius: 25px;
    transition: all 0.3s ease;
    font-weight: 600;
    padding: 8px 16px;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.cart-footer {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 25px;
  box-shadow: 0 4px 20px rgba(255, 182, 193, 0.2);
}
  
  .cart-total {
    font-size: 16px;
    
    .total-price {
      color: #6E4C1E;
      font-weight: bold;
      font-size: 24px;
      margin: 0 5px;
    }
    
    .total-quantity {
      margin-left: 5px;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .cart-actions {
    display: flex;
    gap: 15px;
    
    .continue-btn {
      border-radius: 25px;
      transition: all 0.3s ease;
      font-weight: 600;
      
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      .el-icon {
        margin-right: 5px;
      }
    }
    
    .checkout-btn {
      min-width: 120px;
      background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
      border: 1px solid rgba(255, 182, 193, 0.5);
      color: #6E4C1E;
      border-radius: 25px;
      transition: all 0.3s ease;
      font-weight: 600;
      
      &:hover {
        background: linear-gradient(135deg, #FFEE93 0%, #FFB6C1 100%);
        border-color: #FFB6C1;
        transform: translateY(-1px);
        box-shadow: 0 4px 12px rgba(255, 182, 193, 0.4);
      }
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }

.checkout-dialog {
  :deep(.el-dialog) {
    border-radius: 24px;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2) !important;
  }
  
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 700;
    padding: 30px;
    margin: 0;
    border-bottom: 1px solid rgba(255, 182, 193, 0.3);
    background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
    color: white;
    font-size: 20px;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
  }
  
  :deep(.el-dialog__footer) {
    padding: 20px 30px;
    background: rgba(255, 255, 55, 0.95);
    border-top: 1px solid rgba(255, 182, 193, 0.2);
  }
  
  .order-form {
    :deep(.el-form-item__label) {
      color: #6E4C1E;
      font-weight: 600;
      font-size: 15px;
    }
    
    :deep(.el-form-item) {
      margin-bottom: 25px;
    }
    
    :deep(.el-input__wrapper) {
      box-shadow: 0 0 0 1px rgba(255, 182, 193, 0.3) inset;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.8);
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 2px #FFB6C1 inset;
      }
    }
    
    :deep(.el-textarea__inner) {
      box-shadow: 0 0 0 1px rgba(255, 182, 193, 0.3) inset;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.8);
      
      &:hover, &:focus {
        box-shadow: 0 0 0 2px #FFB6C1 inset;
      }
    }
    
    :deep(.el-select__wrapper) {
      box-shadow: 0 0 0 1px rgba(255, 182, 193, 0.3) inset;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.8);
      
      &:hover, &.is-focus {
        box-shadow: 0 0 0 2px #FFB6C1 inset;
      }
    }
    
    :deep(.el-input__inner),
    :deep(.el-select__inner),
    :deep(.el-textarea__inner) {
      color: #6E4C1E;
      font-size: 14px;
    }
  }
  
  .payment-option {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .payment-icon {
      display: inline-block;
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
      
      &.wechat {
        background: #E6F7FF;
        color: #1890FF;
        border: 1px solid #91D5FF;
      }
      
      &.alipay {
        background: #FFF7E6;
        color: #FA8C16;
        border: 1px solid #FFD591;
      }
      
      &.cod {
        background: #F6FFED;
        color: #52C41A;
        border: 1px solid #B7EB8F;
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 15px;
  }
  
  .cancel-btn {
    border-radius: 25px;
    transition: all 0.3s ease;
    font-weight: 500;
    padding: 8px 20px;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }
  
  .confirm-btn {
    border-radius: 25px;
    transition: all 0.3s ease;
    font-weight: 600;
    padding: 8px 24px;
    
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(255, 167, 38, 0.4);
    }
  }
}

.payment-option {
  display: flex;
  align-items: center;
  
  .payment-icon {
    display: inline-block;
    width: 20px;
    height: 20px;
    border-radius: 4px;
    color: white;
    font-size: 12px;
    line-height: 20px;
    text-align: center;
    margin-right: 8px;
    
    &.wechat {
      background-color: #07c160;
    }
    
    &.alipay {
      background-color: #1677ff;
    }
    
    &.cod {
      background-color: #ff9800;
    }
  }
}

.checkout-summary {
  margin-top: 30px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 182, 193, 0.3);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
}

.order-items-summary {
  margin-bottom: 25px;
  
  .summary-title {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    color: #6E4C1E;
    font-weight: 700;
    position: relative;
    padding-left: 20px;
    font-size: 16px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      font-size: 18px;
    }
    
    .el-icon {
      margin-right: 10px;
      color: #FFB6C1;
    }
  }
  
  .summary-content {
    max-height: 180px;
    overflow-y: auto;
    padding-right: 8px;
    background: rgba(255, 255, 255, 0.7);
    border-radius: 12px;
    padding: 15px;
    border: 1px solid rgba(255, 182, 193, 0.2);
    
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: rgba(255, 182, 193, 0.1);
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 182, 193, 0.4);
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb:hover {
      background: rgba(255, 182, 193, 0.6);
    }
  }
  
  .item-summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding-bottom: 12px;
    border-bottom: 1px dashed rgba(255, 182, 193, 0.2);
    
    &:last-child {
      border-bottom: none;
      margin-bottom: 0;
      padding-bottom: 0;
    }
    
    .item-image {
      width: 40px;
      height: 40px;
      border-radius: 6px;
      margin-right: 12px;
      object-fit: cover;
      border: 1px solid rgba(255, 182, 193, 0.3);
    }
    
    .item-name {
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-right: 15px;
      color: #333;
      font-weight: 500;
    }
    
    .item-quantity {
      color: #909399;
      margin-right: 20px;
      font-size: 14px;
    }
    
    .item-price {
      color: #6E4C1E;
      font-weight: 600;
    }
  }
}

.checkout-total {
  text-align: right;
  font-size: 18px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 182, 193, 0.3);
  font-weight: 600;
  
  .total-price {
    color: #FFA726;
    font-weight: bold;
    font-size: 26px;
    margin-left: 10px;
  }
}

.confirm-btn {
  min-width: 120px;
  background-color: #FFA726;
  border-color: #FFA726;
  border-radius: 25px;
  transition: all 0.3s ease;
  font-weight: 600;
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.4);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.action-btn {
  background-color: #FFA726;
  border-color: #FFA726;
  border-radius: 25px;
  transition: all 0.3s ease;
  font-weight: 600;
  
  &:hover {
    background-color: darken(#FFA726, 5%);
    border-color: darken(#FFA726, 5%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 167, 38, 0.4);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.empty-cart {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 100px 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 182, 193, 0.3);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.empty-content {
  text-align: center;
}

.empty-icon {
  color: rgba(255, 182, 193, 0.3);
  margin-bottom: 20px;
  animation: float 6s ease-in-out infinite;
}

.empty-content h3 {
  font-size: 28px;
  margin-bottom: 15px;
  color: #6E4C1E;
  font-weight: 700;
}

.empty-content p {
  margin-bottom: 30px;
  color: #909399;
  font-size: 16px;
  line-height: 1.6;
}

.go-shopping-btn {
  background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
  border: 1px solid rgba(255, 182, 193, 0.5);
  color: #6E4C1E;
  border-radius: 25px;
  transition: all 0.3s ease;
  font-weight: 600;
  padding: 12px 36px;
  font-size: 16px;
  
  &:hover {
    background: linear-gradient(135deg, #FFEE93 0%, #FFB6C1 100%);
    border-color: #FFB6C1;
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 182, 193, 0.4);
  }
  
  .el-icon {
    margin-right: 8px;
    font-size: 20px;
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

// 响应式设计 - 与 MyAdoption.vue 保持一致
@media (max-width: 1200px) {
  .cart-main {
    flex-direction: column;
    gap: 25px;
  }
  
  .cart-content {
    width: 100%;
  }
  
  .cart-sidebar {
    width: 100%;
    max-width: 100%;
  }
  
  .checkout-dialog {
    :deep(.el-dialog) {
      width: 90% !important;
      max-width: 500px;
    }
  }
}

@media (max-width: 992px) {
  .container {
    padding: 0 15px;
  }
  
  .banner {
    height: 200px;
  }
  
  .cart-main {
    padding: 0 15px;
  }
  
  .cart-table {
    overflow-x: auto;
    min-width: 768px;
    
    .table-header {
      font-size: 14px;
      padding: 12px 15px;
    }
    
    .table-body {
      font-size: 13px;
      
      .table-row {
        padding: 15px;
      }
      
      .product-info {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
        
        .product-image {
          width: 80px;
          height: 80px;
        }
        
        .product-name {
          max-width: 150px;
          font-size: 14px;
        }
      }
      
      .product-price {
        min-width: 80px;
      }
      
      .quantity-control {
        width: 100px;
      }
      
      .action-btn {
        padding: 6px 12px;
        font-size: 12px;
      }
    }
  }
  
  .checkout-summary {
    padding: 20px;
  }
  
  .order-form {
    :deep(.el-form-item__label) {
      width: 100px !important;
    }
  }
}

@media (max-width: 768px) {
  .container {
    padding: 0 10px;
  }
  
  .banner {
    height: 180px;
  }
  
  .breadcrumb {
    margin-bottom: 20px;
    font-size: 14px;
  }
  
  .page-title {
    font-size: 22px;
    margin-bottom: 20px;
  }
  
  .cart-table {
    min-width: 600px;
    
    .table-body {
      .table-row {
        padding: 10px;
      }
      
      .product-info {
        .product-image {
          width: 70px;
          height: 70px;
        }
        
        .product-name {
          max-width: 120px;
          font-size: 13px;
        }
      }
      
      .product-price {
        min-width: 70px;
      }
      
      .quantity-control {
        width: 90px;
        
        button {
          width: 24px;
          height: 24px;
        }
        
        input {
          width: 36px;
          font-size: 13px;
        }
      }
      
      .action-btn {
        padding: 4px 10px;
        font-size: 11px;
      }
    }
  }
  
  .cart-footer {
    flex-direction: column;
    align-items: flex-end;
    gap: 15px;
    padding: 20px 15px;
    
    .total-amount {
      font-size: 20px;
    }
    
    .checkout-btn {
      padding: 10px 20px;
      font-size: 14px;
      width: 100%;
    }
  }
  
  .checkout-dialog {
    :deep(.el-dialog__body) {
      padding: 20px;
    }
    
    .order-form {
      :deep(.el-form-item__label) {
        width: 90px !important;
        font-size: 14px;
      }
    }
  }
  
  .checkout-summary {
    padding: 15px;
  }
  
  .order-items-summary {
    .summary-title {
      font-size: 15px;
    }
  }
}

@media (max-width: 480px) {
  .container {
    padding: 0 8px;
  }
  
  .banner {
    height: 150px;
  }
  
  .page-title {
    font-size: 20px;
    margin-bottom: 15px;
  }
  
  .cart-table {
    min-width: 500px;
    
    .table-header {
      font-size: 13px;
      padding: 10px 8px;
    }
    
    .table-body {
      font-size: 12px;
      
      .table-row {
        padding: 8px;
      }
      
      .product-info {
        .product-image {
          width: 60px;
          height: 60px;
        }
        
        .product-name {
          max-width: 100px;
          font-size: 12px;
        }
      }
    }
  }
  
  .checkout-dialog {
    :deep(.el-dialog__header) {
      padding: 20px;
    }
    
    :deep(.el-dialog__body) {
      padding: 15px;
    }
    
    :deep(.el-dialog__footer) {
      padding: 15px;
    }
  }
}
</style> 