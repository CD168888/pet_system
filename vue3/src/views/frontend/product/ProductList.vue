<template>
  <div class="product-list-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>宠物商城</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>为您的宠物精选优质商品</h1>
        <p>让您的宠物生活更加幸福快乐</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="product-list-content">
      <div class="filter-section">
        <div class="filter-card">
          <div class="filter-header">
            <h3>筛选商品</h3>
            <div class="filter-icon"><el-icon><Search /></el-icon></div>
          </div>
          
          <el-form :model="searchForm" label-width="70px" class="search-form">
            <el-form-item label="商品名称">
              <el-input 
                v-model="searchForm.name" 
                placeholder="请输入商品名称" 
                clearable 
                @keyup.enter="handleSearch" 
                class="custom-input" />
            </el-form-item>
            
            <el-form-item label="商品分类">
              <el-select 
                v-model="searchForm.category" 
                placeholder="请选择分类" 
                clearable 
                class="custom-select">
                <el-option 
                  v-for="category in categories" 
                  :key="category.value" 
                  :label="category.label" 
                  :value="category.value">
                </el-option>
              </el-select>
            </el-form-item>
            
            <div class="filter-actions">
              <el-button class="search-btn" @click="handleSearch">
                <el-icon><search /></el-icon>搜索
              </el-button>
              <el-button class="reset-btn" @click="resetSearch">
                <el-icon><refresh /></el-icon>重置
              </el-button>
            </div>
          </el-form>
          
          <div class="filter-decoration">
            <div class="decoration-bubble bubble-1"></div>
            <div class="decoration-bubble bubble-2"></div>
          </div>
        </div>

        <div class="filter-tip">
          <div class="tip-icon"><el-icon><Bulb /></el-icon></div>
          <div class="tip-text">所有商品均通过严格质量检测，让您放心购买</div>
        </div>
      </div>
      
      <div class="list-section">
        <h2 class="section-title">
          <span class="title-icon">🐾</span>
          商品列表
          <span class="title-count">(共 {{ total }} 件)</span>
        </h2>
        
        <div class="product-list-container" v-loading="loading">
        <transition-group name="product-fade" tag="div" class="product-grid">
          <div v-for="product in productList" :key="product.id" class="product-item product-item-wrapper">
            <div class="product-card" :class="{'out-of-stock': product.stock <= 0}">
              <div class="product-image" @click="viewProductDetail(product.id)">
                <el-image 
                  :src="getImageUrl(product.images)" 
                  fit="cover"
                  loading="lazy"
                  lazy>
                  <template #error>
                    <div class="image-error">
                      <el-icon><picture-failed /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-if="product.stock <= 0" class="stock-badge">缺货</div>
                <div v-else-if="product.stock < 10" class="stock-badge limited">库存紧张</div>
              </div>
              
              <div class="product-info">
                <div class="product-category">{{ product.category }}</div>
                <h3 class="product-name" @click="viewProductDetail(product.id)">{{ product.name }}</h3>
                <div class="product-meta">
                  <div class="product-price">
                    <span class="price">¥{{ product.price }}</span>
                    <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">¥{{ product.originalPrice }}</span>
                  </div>
                  <div v-if="product.sales" class="product-sales">已售 {{ product.sales }}+</div>
                </div>
                <div class="product-action">
                  <el-button 
                    class="cart-button" 
                    :disabled="product.stock <= 0"
                    @click="addToCart(product)">
                    <el-icon><shopping-cart /></el-icon>
                    {{ product.stock <= 0 ? '暂时缺货' : '加入购物车' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </transition-group>
        
        <el-empty 
          v-if="!loading && productList.length === 0" 
          description="暂无相关商品"
          class="product-empty">
        </el-empty>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          background
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[8, 16, 24, 32]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
        </el-pagination>
      </div>
    </div>
  </div>
</div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { PictureFailed, ShoppingCart, Search, Refresh, Bulb } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 路由
const router = useRouter()

// 数据定义
const loading = ref(false)
const productList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)

// 搜索表单
const searchForm = reactive({
  name: '',
  category: ''
})

// 商品分类
const categories = [
  { label: '主粮', value: '主粮' },
  { label: '零食', value: '零食' },
  { label: '玩具', value: '玩具' },
  { label: '日用品', value: '日用品' },
  { label: '保健品', value: '保健品' },
  { label: '服饰', value: '服饰' },
  { label: '其他', value: '其他' }
]

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    await request.get('/product/page', {
      name: searchForm.name,
      category: searchForm.category,
      currentPage: currentPage.value,
      size: pageSize.value,
      status: 1
    }, {
      onSuccess: (res) => {
        productList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 重置搜索
const resetSearch = () => {
  searchForm.name = ''
  searchForm.category = ''
  currentPage.value = 1
  fetchProducts()
}

// 获取图片URL
const getImageUrl = (images) => {
  if (!images) return ''
  const firstImage = images.split(',')[0]
  const baseAPI = process.env.VUE_APP_BASE_API || '/api'
  return firstImage.startsWith('http') ? firstImage : `${baseAPI}${firstImage}`
}

// 查看商品详情
const viewProductDetail = (id) => {
  router.push({ name: 'ProductDetail', params: { id } })
}

// 添加到购物车
const addToCart = async (product) => {
  try {
    await request.post('/cart/add', null, {
      params: {
        productId: product.id,
        quantity: 1
      },
      successMsg: '已加入购物车'
    })
  } catch (error) {
    console.error('添加到购物车失败:', error)
  }
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

// 页面加载时获取商品列表
onMounted(() => {
  fetchProducts()
})
</script>

<style lang="scss" scoped>
.product-list-page {
  min-height: 100vh;
  padding-bottom: 50px;
}

.page-banner {
  background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
  padding: 60px 40px;
  text-align: center;
  position: relative;
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 24px;
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    position: relative;
    z-index: 1;
    
    .breadcrumb {
      margin-bottom: 20px;
      justify-content: center;
      
      .el-breadcrumb__item a {
        color: rgba(255, 255, 255, 0.8);
        text-decoration: none;
        
        &:hover {
          color: white;
        }
      }
    }
  }
  
  h1 {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 42px;
    color: white;
    margin-bottom: 15px;
    animation: fadeInDown 0.8s ease;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }
  
  p {
    font-family: 'Open Sans', sans-serif;
    font-size: 20px;
    color: rgba(255, 255, 255, 0.9);
    opacity: 0.9;
    animation: fadeInUp 0.8s ease;
  }
  
  .banner-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    overflow: hidden;
    pointer-events: none;
  }
  
  .decoration-paw {
      position: absolute;
      font-size: 28px;
      opacity: 0.2;
      color: white;
      
      &.paw-1 {
        top: 15%;
        right: 20%;
        animation: rotate 25s linear infinite, float 6s ease-in-out infinite;
      }
      
      &.paw-2 {
        bottom: 25%;
        left: 15%;
        animation: rotate 20s linear infinite reverse, float 8s ease-in-out infinite;
        font-size: 22px;
      }
      
      &.paw-3 {
        top: 60%;
        right: 30%;
        animation: rotate 30s linear infinite, float 10s ease-in-out infinite;
        font-size: 20px;
      }
    }
}

.product-list-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  gap: 25px;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.filter-section {
  width: 280px;
  flex-shrink: 0;
}

.filter-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 182, 193, 0.3);
  padding: 25px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
    border-color: rgba(255, 182, 193, 0.5);
  }
  
  .filter-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 25px;
    
    h3 {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 22px;
      color: #6E4C1E;
      margin: 0;
    }
    
    .filter-icon {
      font-size: 22px;
      width: 40px;
      height: 40px;
      background: #FFEE93;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: pulse 2s infinite, float 4s ease-in-out infinite;
      box-shadow: 0 4px 12px rgba(255, 238, 147, 0.3);
    }
  }
  
  .search-form {
    :deep(.el-form-item__label) {
      color: #6E4C1E;
      font-weight: 500;
    }
    
    .custom-input {
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        transition: all 0.3s ease;
        background: rgba(255, 255, 255, 0.9);
        border: 1px solid rgba(255, 182, 193, 0.3);
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.5);
          border-color: rgba(255, 182, 193, 0.5);
        }
        
        :deep(.el-input__inner) {
          color: #6E4C1E;
          
          &::placeholder {
            color: rgba(110, 76, 30, 0.5);
          }
        }
        
        :deep(.el-input__suffix-inner) {
          color: #FFB6C1;
        }
      }
    }
    
    .custom-select {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        transition: all 0.3s ease;
        background: rgba(255, 255, 255, 0.9);
        border: 1px solid rgba(255, 182, 193, 0.3);
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.5);
          border-color: rgba(255, 182, 193, 0.5);
        }
      }
      
      :deep(.el-select__input) {
        color: #6E4C1E;
      }
      
      :deep(.el-select__placeholder) {
        color: rgba(110, 76, 30, 0.5);
      }
      
      :deep(.el-select__suffix-inner) {
        color: #FFB6C1;
      }
    }
  }
  
  .filter-actions {
          display: flex;
          gap: 12px;
          margin-top: 20px;
          
          .search-btn, .reset-btn {
            flex: 1;
            height: 44px;
            border-radius: 12px;
            font-weight: 600;
            font-size: 14px;
            transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            position: relative;
            overflow: hidden;
            z-index: 1;
          }
          
          .search-btn {
            background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
            border: none;
            color: white;
            box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
            
            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: -100%;
              width: 100%;
              height: 100%;
              background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
              transition: all 0.5s ease;
            }
            
            &:hover::before {
              left: 100%;
            }
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
            }
          }
          
          .reset-btn {
            background: white;
            border: 2px solid rgba(255, 182, 193, 0.3);
            color: #6E4C1E;
            
            &:hover {
              background: rgba(255, 182, 193, 0.1);
              border-color: #FFB6C1;
              transform: translateY(-2px);
              box-shadow: 0 6px 15px rgba(255, 182, 193, 0.2);
            }
          }
        }
  
  .filter-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    overflow: hidden;
    pointer-events: none;
    z-index: 0;
    
    .decoration-bubble {
      position: absolute;
      border-radius: 50%;
      opacity: 0.05;
      
      &.bubble-1 {
        width: 200px;
        height: 200px;
        top: -50px;
        right: -50px;
        background: linear-gradient(135deg, #FFB6C1, #FFEE93);
      }
      
      &.bubble-2 {
        width: 150px;
        height: 150px;
        bottom: -30px;
        left: -30px;
        background: linear-gradient(135deg, #FFEE93, #FFB6C1);
      }
    }
  }
}

.filter-tip {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-left: 3px solid #FFB6C1;
  padding: 15px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  
  .tip-icon {
    font-size: 20px;
    margin-right: 12px;
    color: #FFB6C1;
  }
  
  .tip-text {
    font-size: 14px;
    color: #6E4C1E;
    line-height: 1.5;
  }
}

.list-section {
  flex: 1;
}

.section-title {
  display: flex;
  align-items: center;
  font-family: 'Nunito Sans', sans-serif;
  font-size: 32px;
  color: #6E4C1E;
  margin-bottom: 30px;
  
  .title-icon {
    margin-right: 15px;
    font-size: 36px;
    color: #FFB6C1;
  }
  
  .title-count {
    margin-left: 15px;
    font-size: 18px;
    opacity: 0.8;
    color: #8D6E63;
  }
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
  margin-bottom: 40px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
}

.product-fade-enter-active, .product-fade-leave-active {
  transition: all 0.5s ease;
}

.product-fade-enter-from, .product-fade-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

.product-item {
  display: flex;
}

.product-item-wrapper {
  animation: fadeInUp 0.6s ease-out;
  
  &:hover {
    transform: translateY(-10px);
  }
  
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  height: 100%;
  
  &:hover {
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
    transform: translateY(-8px);
  }
  
  &.out-of-stock {
    opacity: 0.7;
    
    &:hover {
      transform: translateY(-5px);
    }
    
    .product-image::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.1);
    }
  }
}

.product-image {
  height: 220px;
  position: relative;
  overflow: hidden;
  
  .el-image {
    width: 100%;
    height: 100%;
    transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  }
  
  &:hover .el-image {
    transform: scale(1.1);
  }
  
  .stock-badge {
    position: absolute;
    top: 15px;
    right: 15px;
    padding: 8px 16px;
    background-color: rgba(144, 147, 153, 0.8);
    color: white;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transition: all 0.3s ease;
    
    &.limited {
      background-color: rgba(230, 162, 60, 0.8);
      box-shadow: 0 3px 6px rgba(230, 162, 60, 0.2);
    }
  }
  
  .image-error {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: #f9f9f9;
    color: #909399;
    font-size: 30px;
  }
}

.product-info {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-category {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.product-name {
  margin: 0 0 15px;
  font-size: 16px;
  color: #683e35;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
  height: 3em;
  cursor: pointer;
  transition: color 0.3s ease;
  
  &:hover {
    color: #FFB6C1;
  }
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex: 1;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 5px;
  
  .price {
    font-size: 20px;
    font-weight: bold;
    color: #f56c6c;
  }
  
  .original-price {
    font-size: 14px;
    color: #909399;
    text-decoration: line-through;
  }
}

.product-sales {
  font-size: 12px;
  color: #909399;
}

.product-action {
  margin-top: 10px;
  
  .cart-button {
    width: 100%;
    height: 42px;
    font-size: 14px;
    background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
    color: white;
    border: none;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
    position: relative;
    overflow: hidden;
    z-index: 1;
    
    .el-icon {
      margin-right: 5px;
      transition: transform 0.3s ease;
    }
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: all 0.5s ease;
      z-index: -1;
    }
    
    &:hover:not(:disabled) {
      transform: translateY(-3px);
      box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
      
      &::before {
        left: 100%;
      }
      
      .el-icon {
        transform: translateX(5px);
      }
    }
    
    &:active:not(:disabled) {
      transform: translateY(-1px);
    }
    
    &:disabled {
      background: #f0f0f0;
      color: #999;
      box-shadow: none;
    }
  }
}

.product-empty {
  margin-top: 50px;
}

.pagination-container {
  margin-top: 50px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: rgba(255, 255, 255, 0.9);
    --el-pagination-hover-color: #FF9800;
    --el-pagination-border-color: rgba(255, 182, 193, 0.3);
    
    .el-pagination__jump {
      color: #6E4C1E;
    }
    
    .el-pagination__total {
      color: #6E4C1E;
    }
    
    button:not(:disabled):hover {
      color: #FF9800;
      background: rgba(255, 182, 193, 0.1);
    }
    
    .is-active {
      background: linear-gradient(135deg, #FFA726 0%, #FF9800 100%) !important;
      color: white !important;
      border-color: transparent;
    }
    
    :deep(.el-input__wrapper) {
      background: rgba(255, 255, 255, 0.9);
      border-color: rgba(255, 182, 193, 0.3);
      
      .el-input__inner {
        color: #6E4C1E;
      }
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(255, 182, 193, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(255, 182, 193, 0.4);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(255, 182, 193, 0.3);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@media screen and (max-width: 992px) {
  .product-list-content {
    flex-direction: column;
  }
  
  .filter-section {
    width: 100%;
  }
  
  .filter-card {
    margin-bottom: 25px;
  }
}

@media screen and (max-width: 768px) {
  .page-banner {
    margin: 20px;
    padding: 60px 20px;
    border-radius: 24px;
    
    h1 {
      font-size: 32px;
    }
    
    p {
      font-size: 18px;
    }
  }
  
  .product-list-content {
    padding: 0 15px;
  }
  
  .filter-card {
    padding: 20px;
  }
  
  .product-grid {
    gap: 20px;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .product-image {
    height: 200px;
  }
  
  .product-info {
    padding: 15px;
  }
  
  .product-name {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .product-price {
    .price {
      font-size: 18px;
    }
  }
  
  .cart-button {
    height: 38px !important;
    font-size: 13px !important;
  }
}

@media screen and (max-width: 576px) {
  .page-banner {
    padding: 40px 20px;
    border-radius: 24px;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .product-grid {
    grid-template-columns: repeat(1, 1fr);
    gap: 25px;
  }
  
  .filter-header h3 {
    font-size: 20px;
  }
  
  .filter-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }
  
  .search-form :deep(.el-form-item__label) {
    font-size: 13px;
  }
  
  .filter-actions {
    flex-direction: column;
  }
  
  .section-title {
    font-size: 22px;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .title-icon {
    margin-bottom: 8px;
  }
  
  .pagination-container {
    margin-top: 30px;
  }
}
</style> 