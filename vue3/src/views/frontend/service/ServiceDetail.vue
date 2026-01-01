<template>
  <div class="service-detail-page">
    <div class="page-decoration">
      <div class="decoration-bubble bubble-1"></div>
      <div class="decoration-bubble bubble-2"></div>
      <div class="decoration-bubble bubble-3"></div>
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
    </div>
    
    <!-- 添加Page Banner -->
    <div class="page-banner" v-if="service">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item @click="$router.push('/services')">服务列表</el-breadcrumb-item>
            <el-breadcrumb-item>{{ service.name }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ service.name }}</h1>
        <p>{{ categoryName }}</p>
       
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="service-detail-content" v-loading="loading">
      <div v-if="service" class="service-content">
        <div class="service-main-info">
          <div class="service-card info-card">
            <h2 class="section-title">基本信息</h2>
            <el-descriptions :column="2" border class="info-descriptions">
              <el-descriptions-item label="服务分类">{{ categoryName }}</el-descriptions-item>
              <el-descriptions-item label="服务价格">
                <span class="price">¥{{ service.price }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="服务时长">{{ getDurationText(service.duration) }}</el-descriptions-item>
              <el-descriptions-item label="服务状态">
                <el-tag :type="service.status === 1 ? 'success' : 'info'">
                  {{ service.status === 1 ? '可预约' : '暂停服务' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="service-card description-card">
            <h2 class="section-title">服务介绍</h2>
            <div class="service-description">
              <p>{{ service.description || '暂无介绍' }}</p>
            </div>
          </div>
        </div>
        
        <div class="service-card process-card">
          <h2 class="section-title">服务流程</h2>
          <div class="service-process">
            <el-steps :active="5" finish-status="success" simple>
              <el-step title="预约服务" description="选择服务和预约时间"></el-step>
              <el-step title="确认预约" description="客服确认预约信息"></el-step>
              <el-step title="到店服务" description="按预约时间到店"></el-step>
              <el-step title="服务完成" description="完成服务内容"></el-step>
              <el-step title="评价服务" description="对服务进行评价"></el-step>
            </el-steps>
          </div>
        </div>
        
        <div class="service-card notice-card">
          <h2 class="section-title">注意事项</h2>
          <div class="service-notice">
            <el-alert
              title="预约须知"
              type="warning"
              :closable="false"
              show-icon
            >
              <div class="notice-content">
                <p>1. 请至少提前24小时预约服务，以便我们合理安排时间。</p>
                <p>2. 到店服务时请携带宠物的有效健康证明和疫苗接种记录。</p>
                <p>3. 如需取消或改期，请提前12小时通知我们。</p>
                <p>4. 为保证服务质量，请按预约时间准时到店。</p>
                <p>5. 如有特殊要求，请在预约时备注说明。</p>
              </div>
            </el-alert>
          </div>
        </div>
        
        <div class="book-button-container">
          <el-button 
            type="primary" 
            size="large" 
            class="book-button"
            :disabled="service.status !== 1"
            @click="bookService"
          >
            <el-icon><calendar /></el-icon>
            立即预约
          </el-button>
          
          <div class="booking-tip" v-if="service.status !== 1">
            <el-icon><warning /></el-icon>
            <span>抱歉，该服务当前暂停预约</span>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="服务不存在或已下架" class="service-empty"></el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const serviceId = route.params.id

// 数据定义
const loading = ref(false)
const service = ref(null)
const categories = ref([])

// 计算属性
const categoryName = computed(() => {
  if (!service.value || !service.value.categoryId || !categories.value.length) {
    return '未分类'
  }
  
  const category = categories.value.find(c => c.id === service.value.categoryId)
  return category ? category.name : '未分类'
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

// 获取服务详情
const fetchServiceDetail = async () => {
  loading.value = true
  try {
    await request.get(`/service/${serviceId}`, null, {
      onSuccess: (res) => {
        service.value = res
      }
    })
  } catch (error) {
    console.error('获取服务详情失败:', error)
  } finally {
    loading.value = false
  }
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

// 预约服务
const bookService = () => {
  if (!service.value) return
  
  if (service.value.status !== 1) {
    ElMessage.warning('该服务暂不可预约')
    return
  }
  
  router.push({ 
    name: 'ServiceBooking', 
    params: { id: service.value.id },
    query: { serviceName: service.value.name } 
  })
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchServiceDetail()
})
</script>

<style lang="scss" scoped>
.service-detail-page {
  min-height: 100vh;
  padding: 0 20px 60px;
  position: relative;
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

.service-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.service-content {
  margin-top: 20px;
}

.service-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 30px;
  
  .service-title-section {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .service-title {
      display: none;
    }
    
    .status-tag {
      font-size: 16px;
      font-weight: 500;
      border-radius: 20px;
      padding: 5px 15px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
  }
}

/* Page Banner */
.page-banner {
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
  padding: 60px 40px;
  text-align: center;
  position: relative;
  margin-bottom: 20px;
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
      
      :deep(.el-breadcrumb__item) {
        .el-breadcrumb__inner {
          color: rgba(255, 255, 255, 0.8);
          text-decoration: none;
          font-weight: normal;
          
          &.is-link:hover {
            color: white;
          }
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

.service-main-info {
  display: flex;
  gap: 25px;
  margin-bottom: 30px;
  
  @media (max-width: 992px) {
    flex-direction: column;
  }
}

.service-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 187, 106, 0.2);
  border-radius: 20px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
    border-color: rgba(102, 187, 106, 0.4);
  }
  
  .section-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 20px;
    color: #2e7d32;
    margin: 0 0 20px;
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
}

.info-card {
  flex: 1;
  
  :deep(.el-descriptions__label) {
    color: #666;
    font-weight: 500;
    background-color: #f9f9f9;
  }
  
  :deep(.el-descriptions__content) {
    padding: 12px 15px;
  }
}

.description-card {
  flex: 1;
  
  .service-description {
    line-height: 1.8;
    color: #666;
    font-size: 15px;
  }
}

.process-card, .notice-card {
  margin-bottom: 30px;
  
  .service-process {
    :deep(.el-step__title) {
      font-weight: 500;
      color: #666;
    }
    
    :deep(.el-step__title.is-success) {
      color: #67c23a;
    }
    
    :deep(.el-step__description) {
      font-size: 13px;
    }
  }
  
  .service-notice {
    :deep(.el-alert) {
      background-color: rgba(255, 238, 147, 0.2);
      border: 1px solid rgba(255, 167, 38, 0.2);
      
      .el-alert__icon {
        color: #E6A23C;
      }
      
      .el-alert__title {
        color: #683e35;
        font-weight: 600;
      }
    }
    
    .notice-content {
      padding: 10px;
      color: #666;
      
      p {
        margin: 8px 0;
        position: relative;
        padding-left: 5px;
      }
    }
  }
}

.price {
  color: #66bb6a;
  font-size: 22px;
  font-weight: bold;
}

.book-button-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 40px;
  
  .book-button {
    min-width: 250px;
    height: 50px;
    font-size: 16px;
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    color: white;
    border: none;
    border-radius: 8px;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
    
    &:hover:not(:disabled) {
      transform: translateY(-3px);
      box-shadow: 0 8px 25px rgba(102, 187, 106, 0.4);
    }
    
    &:disabled {
      background-color: #f0f0f0;
      color: #999;
      box-shadow: none;
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
  
  .booking-tip {
    margin-top: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #E6A23C;
    font-size: 14px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

.service-empty {
  margin-top: 100px;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
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

@media (max-width: 768px) {
  .service-header {
    .service-title-section {
      .service-title {
        font-size: 24px;
      }
    }
  }
  
  .service-card {
    padding: 20px;
  }
  
  .info-card {
    :deep(.el-descriptions) {
      .el-descriptions__body {
        display: block;
      }
      
      .el-descriptions__table {
        display: block;
      }
      
      .el-descriptions__cell {
        display: block;
        width: 100%;
      }
    }
  }
}
</style> 