<template>
  <div class="boarding-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>我的寄养</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>我的寄养记录</h1>
        <p>查看和管理您的宠物寄养服务记录</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="boarding-content">

      <el-card class="boarding-card" shadow="never">
        <template #header>
          <div class="card-header">
            <h2 class="section-title">
              <span class="title-icon">🐾</span>
              寄养记录
              <span class="title-count">(共 {{ total }} 条)</span>
            </h2>
            <div class="card-actions">
              <el-form :inline="true" :model="searchForm" class="filter-form">
                <el-form-item label="寄养状态">
                  <el-select v-model="searchForm.status" placeholder="寄养状态" clearable>
                    <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">
                    <el-icon><Search /></el-icon>查询
                  </el-button>
                  <el-button @click="resetSearch">
                    <el-icon><RefreshRight /></el-icon>重置
                  </el-button>
                  <el-button type="success" @click="handleRefresh" :loading="refreshLoading">
                    <el-icon><Refresh /></el-icon>刷新
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </template>

        <!-- 数据加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <!-- 错误状态 -->
        <div v-else-if="loadError" class="error-container">
          <el-result
            icon="error"
            title="数据加载失败"
            sub-title="无法获取寄养记录，请检查网络连接或稍后重试"
          >
            <template #extra>
              <el-button type="primary" @click="handleRefresh">重新加载</el-button>
            </template>
          </el-result>
        </div>

        <!-- 空数据状态 -->
        <div v-else-if="boardingList.length === 0" class="empty-container">
          <el-empty 
            description="暂无寄养记录" 
            :image-size="200"
          >
            <template #description>
              <p>您还没有任何寄养记录</p>
              <p class="empty-hint">您可以立即预约寄养服务，为您的宠物提供专业照顾</p>
            </template>
            <el-button type="primary" class="action-btn" @click="goToBooking">
              <el-icon><HomeFilled /></el-icon>
              立即预约
            </el-button>
          </el-empty>
        </div>

        <!-- 数据展示 -->
        <el-table
          v-else
          :data="boardingList"
          border
          style="width: 100%"
          class="boarding-table"
          :header-cell-style="{background: '#f5f7fa', color: '#606266'}"
        >
          <el-table-column label="宠物名称" prop="petName" min-width="120">
            <template #default="scope">
              <span class="pet-name">{{ scope.row.petName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="宠物类型" prop="petType" min-width="100" />
          <el-table-column label="开始时间" min-width="160">
            <template #default="scope">
              <div class="date-info">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(scope.row.startTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="结束时间" min-width="160">
            <template #default="scope">
              <div class="date-info">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(scope.row.endTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" prop="price" min-width="100">
            <template #default="scope">
              <span class="price">¥{{ scope.row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="特殊要求" prop="requirements" min-width="200" :show-overflow-tooltip="true">
            <template #default="scope">
              <div class="requirements">
                <el-icon><InfoFilled /></el-icon>
                <span v-if="scope.row.requirements">{{ scope.row.requirements }}</span>
                <span v-else class="no-data">无特殊要求</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" min-width="160">
            <template #default="scope">
              <div class="date-info">
                <el-icon><Clock /></el-icon>
                {{ formatDate(scope.row.createTime) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="180">
            <template #default="scope">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleDetail(scope.row)"
              >
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button 
                v-if="scope.row.status === '已申请'"
                type="danger" 
                plain
                size="small" 
                @click="handleCancel(scope.row)"
              >
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="寄养详情" width="500px" class="boarding-dialog">
      <div v-if="currentBoarding" class="boarding-detail">
        <div class="detail-section">
          <div class="section-header">
            <el-icon><Pet /></el-icon>
            <h3>宠物信息</h3>
          </div>
          <div class="detail-item">
            <span class="detail-label">宠物名称：</span>
            <span class="detail-value">{{ currentBoarding.petName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">宠物类型：</span>
            <span class="detail-value">{{ currentBoarding.petType }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-header">
            <el-icon><Calendar /></el-icon>
            <h3>时间信息</h3>
          </div>
          <div class="detail-item">
            <span class="detail-label">开始时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.startTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">结束时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.endTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">申请时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.createTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">更新时间：</span>
            <span class="detail-value">{{ formatDate(currentBoarding.updateTime) }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-header">
            <el-icon><Wallet /></el-icon>
            <h3>费用与状态</h3>
          </div>
          <div class="detail-item">
            <span class="detail-label">价格：</span>
            <span class="detail-value price">¥{{ currentBoarding.price }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">状态：</span>
            <el-tag :type="getStatusType(currentBoarding.status)">{{ currentBoarding.status }}</el-tag>
          </div>
        </div>
        
        <div class="detail-section">
          <div class="section-header">
            <el-icon><InfoFilled /></el-icon>
            <h3>特殊要求</h3>
          </div>
          <div class="detail-content">
            {{ currentBoarding.requirements || '无特殊要求' }}
          </div>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            v-if="currentBoarding && currentBoarding.status === '已申请'"
            type="danger" 
            @click="handleCancel(currentBoarding)"
          >
            取消申请
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import DateUtils from '@/utils/dateUtils'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import { 
  Refresh, 
  RefreshRight, 
  Search, 
  Calendar, 
  Clock, 
  View, 
  Close, 
  Pet, 
  HomeFilled, 
  InfoFilled,
  Wallet
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

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

// 数据列表
const boardingList = ref([])
const loading = ref(false)
const refreshLoading = ref(false)
const loadError = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusOptions = ['已申请', '已接受', '进行中', '已完成', '已取消']

// 搜索表单
const searchForm = reactive({
  status: ''
})

// 重置搜索条件
const resetSearch = () => {
  searchForm.status = ''
  handleSearch()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchMyBoardingList()
}

// 处理分页
const handlePageChange = (page) => {
  currentPage.value = page
  fetchMyBoardingList()
}

// 手动刷新数据
const handleRefresh = () => {
  refreshLoading.value = true
  fetchMyBoardingList().finally(() => {
    refreshLoading.value = false
  })
}

// 前往预约寄养页面
const goToBooking = () => {
  router.push('/boarding/apply');
};

// 获取我的寄养列表
const fetchMyBoardingList = async () => {
  // 确保初始化为空数组而不是undefined
  boardingList.value = []
  
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('您需要登录后才能查看寄养记录，是否前往登录？', '提示', {
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

  loading.value = true;
  loadError.value = false;
  try {
    await request.get('/boarding/my', {
      userId: userStore.userInfo.id,
      status: searchForm.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        // 确保响应数据存在且包含记录
        if (res && res.records) {
          console.log('寄养记录数据:', res.records);
          // 确保日期格式正确 - DateUtils类已内置LocalDateTime处理
          boardingList.value = res.records;
          total.value = res.total || 0;
        } else {
          // 如果没有数据，设置为空数组
          boardingList.value = [];
          total.value = 0;
        }
      }
    });
  } catch (error) {
    console.error('获取寄养列表失败:', error);
    // 确保在出错时也将列表设置为空数组
    boardingList.value = [];
    total.value = 0;
    loadError.value = true;
    ElMessage.error('获取寄养记录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch(status) {
    case '已申请': return 'info'
    case '已接受': return 'success'
    case '进行中': return 'warning'
    case '已完成': return 'success'
    case '已取消': return 'danger'
    default: return 'info'
  }
}

// 详情相关变量和方法
const detailDialogVisible = ref(false)
const currentBoarding = ref(null)

// 查看详情
const handleDetail = (row) => {
  currentBoarding.value = row
  detailDialogVisible.value = true
}

// 取消申请
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该寄养申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/boarding/${row.id}/cancel`, null, {
        successMsg: '寄养申请已取消',
        onSuccess: () => {
          detailDialogVisible.value = false
          fetchMyBoardingList()
        }
      })
    } catch (error) {
      console.error('取消寄养申请失败:', error)
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchMyBoardingList()
})
</script>

<style lang="scss" scoped>
.boarding-page {
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

.page-header {
  text-align: center;
  margin-bottom: 30px;
  
  .page-title {
    margin: 0;
    font-family: 'Nunito Sans', sans-serif;
    font-size: 32px;
    color: #683e35;
  }
  
  .page-subtitle {
    margin: 10px 0 0;
    color: #666;
    font-size: 16px;
  }
}

.boarding-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 182, 193, 0.3);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
    border-color: rgba(255, 182, 193, 0.5);
  }
  
  :deep(.el-card__header) {
    padding: 25px;
    border-bottom: 1px solid rgba(255, 182, 193, 0.3);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 25px;
}

.section-title {
  display: flex;
  align-items: center;
  font-family: 'Nunito Sans', sans-serif;
  font-size: 32px;
  color: #6E4C1E;
  margin: 0;
  
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

.card-actions {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  
  :deep(.el-form-item) {
    margin-bottom: 0;
  }
  
  :deep(.el-form-item__label) {
    color: #6E4C1E;
    font-weight: 500;
  }
  
  :deep(.el-select) {
    width: 140px;
    
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
  
  :deep(.el-button) {
    border-radius: 12px;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
    z-index: 1;
    
    .el-icon {
      margin-right: 5px;
    }
    
    &:hover {
      transform: translateY(-3px);
    }
  }
  
  :deep(.el-button--primary) {
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
      z-index: -1;
    }
    
    &:hover {
      box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
    }
    
    &:hover::before {
      left: 100%;
    }
    
    &:active {
      transform: translateY(-1px);
    }
  }
  
  :deep(.el-button--success) {
    background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%);
    border: none;
    color: white;
    box-shadow: 0 4px 15px rgba(76, 175, 80, 0.3);
    
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
    
    &:hover {
      box-shadow: 0 8px 25px rgba(76, 175, 80, 0.4);
    }
    
    &:hover::before {
      left: 100%;
    }
    
    &:active {
      transform: translateY(-1px);
    }
  }
  
  :deep(.el-button:not(.el-button--primary):not(.el-button--success)) {
    background: rgba(255, 255, 255, 0.9);
    border: 1px solid rgba(255, 182, 193, 0.3);
    color: #6E4C1E;
    
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
    
    &:hover {
      background: rgba(255, 182, 193, 0.1);
      border-color: rgba(255, 182, 193, 0.5);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
    }
    
    &:hover::before {
      left: 100%;
    }
    
    &:active {
      transform: translateY(-1px);
    }
  }
}

.loading-container,
.empty-container,
.error-container {
  padding: 60px 0;
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  margin: 20px;
  border: 1px solid rgba(255, 182, 193, 0.3);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.error-container {
  background: rgba(255, 240, 240, 0.9);
}

.empty-hint {
  color: #909399;
  font-size: 14px;
  margin-top: 10px;
  margin-bottom: 20px;
}

.action-btn {
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
  border: none;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  border-radius: 12px;
  padding: 12px 25px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
  position: relative;
  overflow: hidden;
  z-index: 1;
  
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
  
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
  }
  
  &:hover::before {
    left: 100%;
  }
  
  &:active {
    transform: translateY(-1px);
  }
  
  .el-icon {
    margin-right: 5px;
  }
}

.no-data {
  color: #909399;
  font-style: italic;
}

.boarding-table {
  border-radius: 12px;
  overflow: hidden;
  margin: 20px;
  
  :deep(.el-table) {
    background: transparent;
    
    :deep(.el-table__header-wrapper) {
      :deep(.el-table__header) {
        :deep(.el-table__header-cell) {
          background: rgba(255, 255, 255, 0.9);
          color: #6E4C1E;
          font-weight: 600;
          border-bottom: 1px solid rgba(255, 182, 193, 0.3);
        }
      }
    }
    
    :deep(.el-table__body-wrapper) {
      :deep(.el-table__body) {
        :deep(.el-table__row) {
          transition: all 0.3s ease;
          background: rgba(255, 255, 255, 0.8);
          
          &:hover {
            background: rgba(255, 238, 147, 0.2);
            transform: translateY(-2px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
          }
          
          &:nth-child(odd) {
            background: rgba(255, 255, 255, 0.6);
          }
          
          &:nth-child(odd):hover {
            background: rgba(255, 238, 147, 0.2);
          }
        }
        
        :deep(.el-table__cell) {
          border-bottom: 1px solid rgba(255, 182, 193, 0.2);
          color: #6E4C1E;
        }
      }
    }
  }
  
  :deep(.el-button) {
    border-radius: 12px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
    z-index: 1;
    
    .el-icon {
      margin-right: 5px;
    }
    
    &:hover {
      transform: translateY(-3px);
    }
  }
  
  :deep(.el-button--primary) {
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
      z-index: -1;
    }
    
    &:hover {
      box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
    }
    
    &:hover::before {
      left: 100%;
    }
  }
  
  :deep(.el-button--danger) {
    background: linear-gradient(135deg, #F56C6C 0%, #E64A4A 100%);
    border: none;
    color: white;
    box-shadow: 0 4px 15px rgba(245, 108, 108, 0.3);
    
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
    
    &:hover {
      box-shadow: 0 8px 25px rgba(245, 108, 108, 0.4);
    }
    
    &:hover::before {
      left: 100%;
    }
  }
  
  .pet-name {
    font-weight: 600;
    color: #333;
  }
  
  .date-info {
    display: flex;
    align-items: center;
    gap: 5px;
    
    .el-icon {
      color: #FFB6C1;
    }
  }
  
  .price {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .requirements {
    display: flex;
    align-items: flex-start;
    gap: 5px;
    
    .el-icon {
      margin-top: 3px;
      color: #FFB6C1;
    }
  }
}

.pagination-container {
  margin: 30px 20px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: rgba(255, 255, 255, 0.9);
    --el-pagination-button-color: #6E4C1E;
    --el-pagination-button-disabled-bg-color: rgba(255, 255, 255, 0.6);
    --el-pagination-button-disabled-color: #a8abb2;
    --el-pagination-hover-color: #FFB6C1;
    
    .el-pagination__jump,
    .el-pagination__total {
      color: #8D6E63;
    }
    
    .el-pager li.is-active {
      background-color: #FFB6C1;
      color: white;
    }
    
    .el-pager li:hover {
      color: #FFB6C1;
    }
    
    .el-pagination__button {
      border-radius: 8px;
      transition: all 0.3s ease;
    }
    
    .el-pagination__button:hover {
      transform: translateY(-2px);
    }
  }
}

.boarding-dialog {
  :deep(.el-dialog) {
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-radius: 24px;
    border: 1px solid rgba(255, 182, 193, 0.3);
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  }
  
  :deep(.el-dialog__header) {
    text-align: center;
    font-weight: 600;
    padding: 25px;
    margin-right: 0;
    border-bottom: 1px solid rgba(255, 182, 193, 0.3);
  }
  
  :deep(.el-dialog__title) {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 24px;
    color: #6E4C1E;
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 20px 30px 30px;
    border-top: 1px solid rgba(255, 182, 193, 0.3);
    display: flex;
    justify-content: center;
    gap: 15px;
  }
  
  :deep(.el-dialog__footer .el-button) {
    border-radius: 12px;
    padding: 10px 20px;
    font-weight: 600;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
    }
  }
  
  :deep(.el-dialog__footer .el-button--primary) {
    background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
    border: none;
    
    &:hover {
      box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
    }
  }
}

.boarding-detail {
  .detail-section {
    margin-bottom: 25px;
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid rgba(255, 182, 193, 0.3);
      
      .el-icon {
        color: #FFB6C1;
        font-size: 20px;
      }
      
      h3 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #6E4C1E;
        font-family: 'Nunito Sans', sans-serif;
      }
    }
    
    .detail-item {
      margin: 10px 0;
      display: flex;
      
      .detail-label {
        width: 90px;
        color: #8D6E63;
        font-weight: 500;
      }
      
      .detail-value {
        flex: 1;
        color: #6E4C1E;
        
        &.price {
          color: #f56c6c;
          font-weight: 600;
        }
      }
    }
    
    .detail-content {
      padding: 15px;
      background: rgba(255, 255, 255, 0.8);
      border-radius: 12px;
      color: #6E4C1E;
      line-height: 1.6;
      border: 1px solid rgba(255, 182, 193, 0.2);
    }
  }
}

/* 动画定义 */
@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
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

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
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
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    
    .card-actions {
      width: 100%;
    }
    
    .filter-form {
      width: 100%;
      justify-content: space-between;
      
      .el-form-item {
        margin-bottom: 10px;
      }
    }
  }
  
  .boarding-table {
    :deep(.el-table__body) {
      width: 100% !important;
    }
  }
  
  .boarding-dialog {
    width: 90% !important;
    
    :deep(.el-dialog__body) {
      padding: 20px;
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