<template>
  <div class="service-list-page">
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>宠物服务</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>宠物服务</h1>
        <p>为您的爱宠提供专业的护理服务</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="pet-list-content">
      <!-- 左侧过滤组件 -->
      <div class="filter-section">
        <div class="filter-card">
          <div class="filter-header">
            <h3>寻找服务</h3>
            <div class="filter-description">根据您的需求筛选合适的服务</div>
          </div>
          
          <el-form :model="searchForm" class="filter-form" label-position="top">
            <!-- 搜索服务 -->
            <el-form-item label="服务名称">
              <el-input
                v-model="searchForm.name"
                placeholder="输入服务名称搜索"
                clearable
                @input="handleSearch"
              >
                <template #prefix>
                  <el-icon><search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            

            
            <!-- 筛选按钮组 -->
            <div class="filter-actions">
              <el-button type="primary" @click="handleSearch" class="search-button">
                搜索服务
              </el-button>
              <el-button @click="handleReset" class="reset-button">
                重置
              </el-button>
            </div>
          </el-form>
        </div>
      </div>
      
      <!-- 右侧服务列表 -->
      <div class="list-section">
        <!-- 分类书签 -->
        <div class="category-tabs">
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="全部服务" name="all"></el-tab-pane>
            <el-tab-pane v-for="category in categories" :key="category.id" :label="category.name" :name="String(category.id)"></el-tab-pane>
          </el-tabs>
        </div>
        <div class="service-list-container" v-loading="loading">
        <!-- 只有在有服务数据时才渲染过渡组 -->
        <div v-if="services.length > 0">
          <transition-group name="service-fade" tag="div" class="service-grid">
            <div v-for="service in services" :key="service.id" class="service-item">
              <div class="service-card">
                <div class="service-card-content">
                  <div class="service-info">
                    <div class="service-title-row">
                      <h3 class="service-name">{{ service.name }}</h3>
                      <div class="service-status" v-if="service.status === 0">暂停服务</div>
                    </div>
                    
                    <div class="service-meta">
                      <div class="service-price">¥{{ service.price }}</div>
                      <div class="service-duration">
                        <el-icon><timer /></el-icon>
                        {{ getDurationText(service.duration) }}
                      </div>
                    </div>
                    
                    <div class="service-description">{{ service.description }}</div>
                  </div>
                  
                  <div class="service-actions">
                    <el-button 
                      type="primary" 
                      class="booking-button"
                      :disabled="service.status === 0"
                      @click.stop="bookService(service)">
                      立即预约
                    </el-button>
                    <el-button 
                      class="detail-button"
                      @click.stop="viewServiceDetail(service.id)">
                      查看详情
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </transition-group>
        </div>
        
        <el-empty 
          v-else-if="!loading" 
          description="暂无服务"
          class="service-empty" />
        </div>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[8, 12, 24, 36]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 数据定义
const loading = ref(false)
const services = ref([])
const total = ref(0)
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const activeTab = ref('all')

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: ''
})

// 获取服务分类
const fetchCategories = async () => {
  try {
    await request.get('/service/category/list', null, {
      onSuccess: (res) => {
        categories.value = res
      }
    })
  } catch (error) {
    console.error('获取服务分类失败:', error)
  }
}

// 获取服务列表
const fetchServices = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      name: searchForm.name
    }
    
    // 根据分类过滤
    if (activeTab.value !== 'all') {
      params.categoryId = activeTab.value
    }
    
    await request.get('/service/page', params, {
      onSuccess: (res) => {
        services.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取服务列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理分类点击
const handleCategoryClick = (categoryId) => {
  activeTab.value = String(categoryId)
  currentPage.value = 1
  fetchServices()
}

// 查看服务详情
const viewServiceDetail = (id) => {
  router.push({ name: 'ServiceDetail', params: { id } })
}

// 预约服务
const bookService = (service) => {
  if (service.status === 0) {
    ElMessage.warning('该服务暂停预约')
    return
  }
  
  router.push({ 
    name: 'ServiceBooking', 
    params: { id: service.id },
    query: { serviceName: service.name } 
  })
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchServices()
}

// 处理重置
const handleReset = () => {
  searchForm.name = ''
  activeTab.value = 'all'
  currentPage.value = 1
  fetchServices()
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchServices()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchServices()
}

// 处理分类标签点击
const handleTabClick = () => {
  currentPage.value = 1
  fetchServices()
}

// 处理持续时间显示
const getDurationText = (minutes) => {
  if (!minutes) return '时长未知'
  
  if (minutes < 60) {
    return `${minutes}分钟`
  } else if (minutes === 60) {
    return '1小时'
  } else if (minutes < 1440) {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins > 0 ? `${hours}小时${mins}分钟` : `${hours}小时`
  } else {
    const days = Math.floor(minutes / 1440)
    return `${days}天`
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServices()
})
</script>

<style lang="scss" scoped>
.service-list-page {
  min-height: 100vh;
  padding-bottom: 60px;
  position: relative;
  overflow: hidden;
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
      justify-content: left;
      
      .el-breadcrumb__item a {
        color: rgba(0, 0, 0, 0.5);
        text-decoration: none;
        
        &:hover {
          color: rgba(0, 0, 0, 0.8);
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
    margin: 0;
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
    font-size: 30px;
    opacity: 0.1;
    
    &.paw-1 {
      top: 20%;
      left: 5%;
      animation: float 15s infinite ease-in-out;
    }
    
    &.paw-2 {
      bottom: 10%;
      right: 10%;
      animation: float 18s infinite ease-in-out reverse;
    }
  }
}

.pet-list-content {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  display: flex;
  gap: 25px;
  padding: 0 20px;
}

/* 左侧过滤区域 */
.filter-section {
  width: 280px;
  flex-shrink: 0;
  
  .filter-card {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 25px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 182, 193, 0.2);
  }
  
  .filter-header {
    margin-bottom: 20px;
    
    h3 {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 24px;
      color: #683e35;
      margin-bottom: 8px;
      font-weight: 700;
    }
    
    .filter-description {
      font-size: 14px;
      color: #999;
    }
  }
  
  .filter-form {
    .el-form-item {
      margin-bottom: 20px;
      
      .el-form-item__label {
        font-size: 15px;
        color: #683e35;
        font-weight: 600;
        margin-bottom: 8px;
      }
    }
    
    .el-input {
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        transition: all 0.3s ease;
        background: white;
        border: 1px solid #e0e0e0;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        
        &:hover, &.is-focus {
          box-shadow: 0 4px 15px rgba(0, 0, 0, 0.12);
          border-color: #FFB6C1;
        }
      }
      
      :deep(.el-input__prefix-inner) {
        color: #FFB6C1;
      }
    }
  }
  
  /* 分类筛选标签 */
  .category-filter {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .category-tag {
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: 20px;
    padding: 6px 14px;
    font-size: 13px;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }
  
  /* 筛选按钮 */
  .filter-actions {
    display: flex;
    gap: 12px;
    margin-top: 25px;
  }
  
  .search-button, .reset-button {
    flex: 1;
    height: 44px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    font-family: 'Nunito Sans', sans-serif;
    
    &:hover:not(:disabled) {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    }
  }
  
  .search-button {
    background: linear-gradient(135deg, #FF6B6B 0%, #FFA500 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
    
    &:hover:not(:disabled) {
      box-shadow: 0 8px 25px rgba(255, 107, 107, 0.4);
    }
  }
  
  .reset-button {
    background: rgba(255, 182, 193, 0.15);
    color: #683e35;
    border: 2px solid rgba(255, 182, 193, 0.3);
    
    &:hover {
      background: rgba(255, 182, 193, 0.25);
      border-color: #FFB6C1;
      color: #683e35;
    }
  }
}

/* 右侧列表区域 */
.list-section {
  flex: 1;
  min-width: 0;
}

/* 分类书签样式 */
.category-tabs {
  margin-bottom: 20px;
  
  :deep(.el-tabs__nav-wrap) {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 12px;
    padding: 8px 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }
  
  :deep(.el-tabs__nav) {
    margin: 0;
    display: flex;
    align-items: center;
    padding: 0 10px;
  }
  
  :deep(.el-tabs__item) {
    border-radius: 8px;
    padding: 10px 20px;
    margin-right: 10px;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
    color: #6E4C1E;
    min-width: auto;
    box-sizing: border-box;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &:hover {
      color: #FFB6C1;
      background: rgba(255, 182, 193, 0.1);
    }
  }
  
  :deep(.el-tabs__item.is-active) {
    background: linear-gradient(135deg, #FF6B6B 0%, #FFA500 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
    padding: 10px 22px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;
    width: auto;
  }
  
  :deep(.el-tabs__active-bar) {
    display: none;
  }
  
  :deep(.el-tabs__nav-scroll) {
    overflow: visible;
  }
}

.service-fade-enter-active {
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.service-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.service-fade-enter-from {
  opacity: 0;
  transform: translateY(40px) scale(0.95);
}

.service-fade-leave-to {
  opacity: 0;
  transform: translateY(30px) scale(0.98);
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

/* 响应式调整 */
@media (max-width: 992px) {
  .pet-list-content {
    flex-direction: column;
  }
  
  .filter-section {
    width: 100%;
    
    .filter-card {
      animation: none;
    }
  }
  
  .list-section {
    margin-top: 20px;
  }
}

@media (max-width: 768px) {
  .page-banner {
    padding: 40px 20px;
    
    h1 {
      font-size: 32px;
    }
    
    p {
      font-size: 16px;
    }
  }
}

.service-item {
  display: flex;
  cursor: pointer;
}

.service-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 182, 193, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 182, 193, 0.1), transparent);
    transition: left 0.5s ease;
  }
  
  &:hover {
    transform: translateY(-12px);
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
    border-color: rgba(255, 182, 193, 0.4);
    
    &::before {
      left: 100%;
    }
  }
}

.service-card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.service-info {
  flex: 1;
}

.service-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  
  .service-name {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 21px;
    color: #6E4C1E;
    font-weight: 700;
    line-height: 1.3;
    transition: color 0.3s ease;
  }
  
  .service-status {
    background: linear-gradient(135deg, #FF6B6B 0%, #C70039 100%);
    color: white;
    padding: 3px 10px;
    border-radius: 15px;
    font-size: 12px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
    transition: all 0.3s ease;
  }
}

.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed rgba(255, 182, 193, 0.3);
  
  .service-price {
    font-size: 24px;
    font-weight: 800;
    background: linear-gradient(135deg, #FF6B6B 0%, #FFA500 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transition: transform 0.3s ease;
  }
  
  .service-duration {
    display: flex;
    align-items: center;
    color: #6E4C1E;
    font-size: 15px;
    font-weight: 500;
    background: rgba(255, 182, 193, 0.15);
    padding: 6px 12px;
    border-radius: 15px;
    transition: all 0.3s ease;
    
    .el-icon {
      margin-right: 6px;
      color: #FFB6C1;
      font-size: 16px;
    }
  }
}

.service-description {
  color: #6E4C1E;
  font-size: 14px;
  line-height: 1.7;
  opacity: 0.85;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  margin-bottom: 25px;
  transition: opacity 0.3s ease;
  
  &:hover {
    opacity: 1;
  }
}

.service-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  
  .booking-button, .detail-button {
    flex: 1;
    height: 44px;
    border-radius: 12px;
    font-size: 15px;
    font-weight: 600;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: none;
    font-family: 'Nunito Sans', sans-serif;
    
    &:hover:not(:disabled) {
      transform: translateY(-3px);
      box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
    }
    
    &:active:not(:disabled) {
      transform: translateY(-1px);
    }
    
    &:disabled {
      background-color: #f0f0f0;
      color: #999;
      cursor: not-allowed;
      transform: none;
      box-shadow: none;
    }
  }
  
  .booking-button {
    background: linear-gradient(135deg, #FF6B6B 0%, #FFA500 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
    
    &:hover:not(:disabled) {
      box-shadow: 0 8px 25px rgba(255, 107, 107, 0.4);
    }
  }
  
  .detail-button {
    background: rgba(255, 182, 193, 0.15);
    color: #6E4C1E;
    border: 2px solid rgba(255, 182, 193, 0.3);
    
    &:hover {
      background: rgba(255, 182, 193, 0.25);
      border-color: #FFB6C1;
      color: #FFB6C1;
    }
  }
}

.service-empty {
  margin-top: 50px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: white;
    --el-pagination-hover-color: #FFB6C1;
    
    .el-pagination__jump {
      color: #683e35;
    }
    
    button:not(:disabled):hover {
      color: #FFB6C1;
    }
    
    .is-active {
      background-color: #FFB6C1 !important;
      color: white !important;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
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

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

@media (max-width: 1024px) {
  .service-list-content {
    padding: 0 20px;
  }
  
  .service-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 18px;
  }
}

@media (max-width: 768px) {
  .page-banner {
    padding: 40px 20px 30px;
    
    h1 {
      font-size: 32px;
    }
    
    p {
      font-size: 16px;
    }
  }
  
  .service-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .filter-section {
    padding: 20px;
    margin-bottom: 15px;
    
    .search-section {
      max-width: 100%;
    }
  }
  
  .service-title-row {
    .service-name {
      font-size: 19px;
    }
    
    .service-status {
      font-size: 11px;
      padding: 2px 8px;
    }
  }
  
  .service-meta {
    .service-price {
      font-size: 22px;
    }
    
    .service-duration {
      font-size: 13px;
      padding: 4px 10px;
    }
  }
  
  .service-description {
    font-size: 13px;
    margin-bottom: 20px;
  }
  
  .service-card {
    padding: 20px;
  }
  
  .service-actions {
    gap: 8px;
    
    .booking-button, .detail-button {
      height: 40px;
      font-size: 14px;
    }
  }
}
</style> 