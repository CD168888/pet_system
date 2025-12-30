<template>
  <div class="adoption-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item @click="$router.push('/pet')">宠物领养</el-breadcrumb-item>
            <el-breadcrumb-item>我的领养申请</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>我的领养申请</h1>
        <p>查看和管理您的宠物领养申请记录</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="adoption-content">
      
      <el-card shadow="never" class="adoption-card">
        <template #header>
          <div class="card-header">
            <h2>申请记录</h2>
            <div class="card-actions">
              <el-radio-group v-model="currentStatus" @change="handleStatusChange" size="small">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="已申请">已申请</el-radio-button>
                <el-radio-button label="审核中">审核中</el-radio-button>
                <el-radio-button label="已通过">已通过</el-radio-button>
                <el-radio-button label="已拒绝">已拒绝</el-radio-button>
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
            sub-title="无法获取领养申请记录，请检查网络连接或稍后重试"
          >
            <template #extra>
              <el-button type="primary" @click="handleRefresh">重新加载</el-button>
            </template>
          </el-result>
        </div>
        
        <!-- 空数据状态 -->
        <div v-else-if="adoptionList.length === 0" class="empty-container">
          <el-empty description="暂无申请记录" :image-size="200">
            <template #description>
              <p>您还没有提交过领养申请</p>
              <p class="empty-hint">您可以浏览可领养的宠物，找到心仪的宠物后提交申请</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToAdoption">
              <el-icon><Pet /></el-icon>
              去看看可爱的宠物
            </el-button>
          </el-empty>
        </div>
        
        <!-- 数据列表 -->
        <div v-else class="adoption-list">
          <el-timeline>
            <el-timeline-item
              v-for="item in adoptionList"
              :key="item.id"
              :timestamp="formatDate(item.createTime)"
              :type="getStatusType(item.status)"
            >
              <el-card class="adoption-item">
                <div class="adoption-header">
                  <div class="adoption-info">
                    <h3>申请ID: {{ item.id }}</h3>
                    <el-tag :type="getStatusType(item.status)" effect="light">{{ item.status }}</el-tag>
                  </div>
                  <div class="adoption-actions">
                    <el-button 
                      v-if="item.status === '已申请' || item.status === '审核中'"
                      type="danger" 
                      plain
                      size="small" 
                      @click="handleCancel(item)"
                    >
                      <el-icon><Close /></el-icon>
                      取消申请
                    </el-button>
                    <el-button type="primary" size="small" @click="viewPet(item.petId)">
                      <el-icon><View /></el-icon>
                      查看宠物
                    </el-button>
                  </div>
                </div>
                
                <div class="adoption-details">
                  <div class="detail-item">
                    <span class="detail-label"><el-icon><Document /></el-icon> 申请理由:</span>
                    <span class="detail-value">{{ item.applyReason || '无' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label"><el-icon><Phone /></el-icon> 联系电话:</span>
                    <span class="detail-value">{{ item.contactPhone || '无' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="detail-label"><el-icon><Location /></el-icon> 联系地址:</span>
                    <span class="detail-value">{{ item.address || '无' }}</span>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'
import { useUserStore } from '@/store/user'
import { Refresh, Pet, Close, View, Document, Phone, Location } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 格式化日期函数
const formatDate = (date) => {
  if (!date) return '';
  try {
    return DateUtils.formatDateTime(date);
  } catch (error) {
    console.error('日期格式化错误:', error, date);
    return date;
  }
}

const loading = ref(false)
const loadError = ref(false)
const adoptionList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const currentStatus = ref('')

// 申请状态保持不变，仍然有4种状态
const statusOptions = ['已申请', '审核中', '已通过', '已拒绝']

// 获取领养申请列表
const fetchAdoptions = async () => {
  // 确保初始化为空数组而不是undefined
  adoptionList.value = []
  
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('您需要登录后才能查看领养申请记录，是否前往登录？', '提示', {
      confirmButtonText: '前往登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath }
      });
    }).catch(() => {
      router.push('/');
    });
    return;
  }
  
  loading.value = true
  loadError.value = false
  try {
    await request.get('/adoption/user', {
      status: currentStatus.value,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          adoptionList.value = res.records
          total.value = res.total || 0
        } else {
          // 如果没有数据，设置为空数组
          adoptionList.value = []
          total.value = 0
        }
      }
    })
  } catch (error) {
    console.error('获取领养申请列表失败:', error)
    // 确保在出错时也将列表设置为空数组
    adoptionList.value = []
    total.value = 0
    loadError.value = true
    ElMessage.error('获取领养申请记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchAdoptions()
}

// 状态筛选变更
const handleStatusChange = () => {
  currentPage.value = 1
  fetchAdoptions()
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchAdoptions()
}

// 查看宠物
const viewPet = (petId) => {
  router.push(`/pet/${petId}`)
}

// 前往领养页面
const goToAdoption = () => {
  router.push('/pet');
};

// 取消申请
const handleCancel = (item) => {
  ElMessageBox.confirm('确定要取消该领养申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/adoption/${item.id}`, {
        successMsg: '申请已取消',
        onSuccess: () => {
          fetchAdoptions() // 刷新列表
        }
      })
    } catch (error) {
      console.error('取消申请失败:', error)
    }
  }).catch(() => {})
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '已申请':
      return 'info'
    case '审核中':
      return 'warning'
    case '已通过':
      return 'success'
    case '已拒绝':
      return 'danger'
    default:
      return 'info'
  }
}

onMounted(() => {
  fetchAdoptions()
})
</script>

<style lang="scss" scoped>
.adoption-page {
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

.adoption-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.adoption-card {
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

.adoption-list {
  margin: 20px 0;
  
  :deep(.el-timeline) {
      padding: 20px 0;
      position: relative;
      
      .el-timeline-item__node {
        box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
        transition: all 0.3s ease;
      }
      
      .el-timeline-item__node--primary {
        background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
        border-color: #66bb6a;
      }
      
      .el-timeline-item__node--success {
        background: linear-gradient(135deg, #90EE90 0%, #81C784 100%);
        border-color: #90EE90;
      }
      
      .el-timeline-item__node--warning {
        background: linear-gradient(135deg, #FFD700 0%, #FFA000 100%);
        border-color: #FFD700;
      }
      
      .el-timeline-item__node--danger {
        background: linear-gradient(135deg, #FF6B6B 0%, #EF5350 100%);
        border-color: #FF6B6B;
      }
      
      .el-timeline-item__tail {
        background: linear-gradient(to bottom, #66bb6a 0%, #b3e5fc 100%);
        box-shadow: 0 0 10px rgba(102, 187, 106, 0.3);
      }
    
    .el-timeline-item__wrapper {
      padding-bottom: 30px;
    }
    
    .el-timeline-item__timestamp {
      color: #8D6E63;
      font-size: 14px;
      margin-top: 8px;
      margin-bottom: 10px;
      font-weight: 600;
    }
  }
}

.adoption-item {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 187, 106, 0.2);
  margin-bottom: 20px;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
    border-color: rgba(102, 187, 106, 0.4);
  }
}

.adoption-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(102, 187, 106, 0.3);
  
  .adoption-info {
    display: flex;
    align-items: center;
    gap: 15px;
    
    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 700;
      color: #2e7d32;
    }
  }
  
  .adoption-actions {
    display: flex;
    gap: 10px;
    
    .el-button {
      border-radius: 25px;
      padding: 8px 20px;
      transition: all 0.3s ease;
      font-weight: 600;
      
      &.el-button--primary {
        background: linear-gradient(135deg, #66bb6a 0%, #b3e5fc 100%);
        border: 1px solid rgba(102, 187, 106, 0.5);
        color: #2e7d32;
        
        &:hover {
          background: linear-gradient(135deg, #b3e5fc 0%, #66bb6a 100%);
          border-color: #66bb6a;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(102, 187, 106, 0.4);
        }
      }
      
      &.el-button--danger {
        background: linear-gradient(135deg, #FFB6B9 0%, #FF8787 100%);
        border: 1px solid rgba(255, 135, 135, 0.5);
        color: #721C24;
        
        &:hover {
          background: linear-gradient(135deg, #FF8787 0%, #FFB6B9 100%);
          border-color: #FF8787;
          transform: translateY(-1px);
          box-shadow: 0 4px 12px rgba(255, 135, 135, 0.4);
        }
      }
    }
  }
}

.adoption-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  
  .detail-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .detail-label {
      font-weight: 600;
      color: #434343;
      font-size: 14px;
      
      .el-icon {
        margin-right: 8px;
        color: #66bb6a;
        font-size: 16px;
      }
    }
    
    .detail-value {
      color: #2e7d32;
      font-size: 15px;
      line-height: 1.6;
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
  .page-header {
    .page-title {
      font-size: 26px;
    }
    
    .page-subtitle {
      font-size: 14px;
    }
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .card-actions {
      width: 100%;
      justify-content: space-between;
    }
  }
  
  .adoption-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
    
    .adoption-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
  
  .detail-item {
    flex-direction: column;
    
    .detail-label {
      width: 100%;
      margin-bottom: 5px;
    }
  }
}
</style> 