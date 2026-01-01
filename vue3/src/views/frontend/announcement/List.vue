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
            <el-breadcrumb-item>公告中心</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>公告中心</h1>
        <p>最新公告信息，为您提供第一手资讯</p>
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
            <h3>寻找公告</h3>
            <div class="filter-description">根据您的需求筛选合适的公告</div>
          </div>
          
          <el-form :model="searchForm" class="filter-form" label-position="top">
            <!-- 搜索服务 -->
            <el-form-item label="公告标题">
              <el-input
                v-model="searchForm.title"
                placeholder="输入公告标题搜索"
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
                搜索公告
              </el-button>
              <el-button @click="resetSearch" class="reset-button">
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
            <el-tab-pane label="全部公告" name="all"></el-tab-pane>
            <el-tab-pane label="通知" name="NOTICE"></el-tab-pane>
            <el-tab-pane label="活动" name="ACTIVITY"></el-tab-pane>
            <el-tab-pane label="其他" name="OTHER"></el-tab-pane>
          </el-tabs>
        </div>
        <div class="service-list-container" v-loading="loading">
        <!-- 只有在有服务数据时才渲染过渡组 -->
        <div v-if="announcements.length > 0">
          <transition-group name="service-fade" tag="div" class="service-grid">
            <div v-for="item in announcements" :key="item.id" class="service-item">
              <div class="service-card" @click="viewAnnouncementDetail(item)">
                <div class="service-card-content">
                  <div class="service-info">
                    <div class="service-title-row">
                      <h3 class="service-name">{{ item.title }}</h3>
                      <el-tag :type="getTypeTagType(item.type)" size="small" class="service-status">
                        {{ getTypeLabel(item.type) }}
                      </el-tag>
                    </div>
                    
                    <div class="service-meta">
                      <div class="service-duration">
                        <el-icon><timer /></el-icon>
                        {{ formatDate(item.createdTime) }}
                      </div>
                    </div>
                    
                    <div class="service-description">{{ truncateText(item.content || '暂无内容', 150) }}</div>
                  </div>
                  
                  <div class="service-actions">
                    <el-button 
                      class="detail-button"
                      @click.stop="viewAnnouncementDetail(item)">
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
          description="暂无公告" 
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
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="公告详情"
      width="800px"
      destroy-on-close
      :close-on-click-modal="true"
      :show-close="true"
      custom-class="announcement-dialog"
      append-to-body
    >
      <div class="announcement-detail">
        <h3 class="dialog-title">{{ currentAnnouncement.title || '暂无标题' }}</h3>
        <div class="announcement-meta">
          <el-tag :type="getTypeTagType(currentAnnouncement.type)" size="small">
            {{ getTypeLabel(currentAnnouncement.type) }}
          </el-tag>
          <span class="announcement-time">发布时间：{{ formatDateTime(currentAnnouncement.createdTime) }}</span>
        </div>
        <div class="announcement-detail-content">
          <div v-if="processedContent" v-html="processedContent"></div>
          <div v-else class="no-content">暂无内容</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Timer } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 分页参数
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 加载状态
const loading = ref(false)

// 公告列表数据
const announcements = ref([])

// 搜索表单
const searchForm = reactive({
  title: ''
})

// 活动Tab
const activeTab = ref('all')

// 公告详情对话框
const dialogVisible = ref(false)
const currentAnnouncement = reactive({})

// 处理内容，确保安全显示
const processedContent = computed(() => {
  let content = currentAnnouncement.content || ''
  
  // 过滤危险的HTML标签
  content = content.replace(/<script[^>]*>.*?<\/script>/gi, '')
  content = content.replace(/<style[^>]*>.*?<\/style>/gi, '')
  
  // 如果内容不是HTML格式，添加适当的格式
  if (!content.includes('<')) {
    // 替换换行符为<br>
    content = content.replace(/\n/g, '<br>')
    // 添加段落标签
    content = `<p>${content}</p>`
  }
  
  // 确保内容有合适的段落结构
  if (!content.startsWith('<p>')) {
    content = `<p>${content}</p>`
  }
  
  return content
})

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const params = {
      title: searchForm.title,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/announcement/page', params, {
      onSuccess: (res) => {
        // 直接获取所有公告，不在请求时过滤
        const allRecords = res.records || []
        
        // 前端根据当前选择的标签过滤公告
        let filteredRecords = allRecords
        if (activeTab.value !== 'all') {
          filteredRecords = allRecords.filter(announcement => announcement.type === activeTab.value)
        }
        
        announcements.value = filteredRecords
        total.value = filteredRecords.length
      }
    })
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchAnnouncements()
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  activeTab.value = 'all'
  currentPage.value = 1
  fetchAnnouncements()
}

// 分页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchAnnouncements()
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchAnnouncements()
}

// 处理分类标签点击
const handleTabClick = () => {
  currentPage.value = 1
  fetchAnnouncements()
}

// 查看公告详情
const viewAnnouncementDetail = (announcement) => {
  Object.assign(currentAnnouncement, announcement)
  dialogVisible.value = true
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '暂无时间'
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return '暂无时间'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取公告类型标签类型
const getTypeTagType = (type) => {
  switch (type) {
    case 'NOTICE':
      return 'primary'
    case 'ACTIVITY':
      return 'success'
    case 'OTHER':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取公告类型标签文本
const getTypeLabel = (type) => {
  switch (type) {
    case 'NOTICE':
      return '通知'
    case 'ACTIVITY':
      return '活动'
    case 'OTHER':
      return '其他'
    default:
      return '其他'
  }
}

// 截断文本
const truncateText = (text, length) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

// 页面加载时获取公告列表
onMounted(() => {
  fetchAnnouncements()
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
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
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
    border: 1px solid rgba(102, 187, 106, 0.2);
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
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    color: white;
    box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
    
    &:hover:not(:disabled) {
      box-shadow: 0 8px 25px rgba(102, 187, 106, 0.4);
    }
  }
  
  .reset-button {
    background: rgba(102, 187, 106, 0.15);
    color: #2e7d32;
    border: 2px solid rgba(102, 187, 106, 0.3);
    
    &:hover {
      background: rgba(102, 187, 106, 0.25);
      border-color: #66bb6a;
      color: #2e7d32;
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
    background: transparent !important;
    
    &:hover {
      color: #66bb6a !important;
      background: rgba(102, 187, 106, 0.1) !important;
    }
  }
  
  :deep(.el-tabs__item.is-active) {
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
    color: white !important;
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3) !important;
    padding: 10px 22px !important;
    text-align: center !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
    width: auto !important;
    border: none !important;
    outline: none !important;
  }
  
  :deep(.el-tabs__active-bar) {
    display: none !important;
  }
  
  :deep(.el-tabs__nav-scroll) {
    overflow: visible;
  }
  
  /* 移除Element Plus默认的激活标签样式 */
  :deep(.el-tabs--card > .el-tabs__header .el-tabs__item.is-active) {
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
    color: white !important;
    border: none !important;
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3) !important;
  }
  
  /* 移除Element Plus默认的激活标签边框 */
  :deep(.el-tabs--card > .el-tabs__header .el-tabs__nav-wrap) {
    border-bottom: none !important;
  }
  
  /* 移除Element Plus默认的激活标签边框 */
  :deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
    border: none !important;
    margin-right: 10px !important;
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
  border: 1px solid rgba(102, 187, 106, 0.2);
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
    background: linear-gradient(90deg, transparent, rgba(102, 187, 106, 0.1), transparent);
    transition: left 0.5s ease;
  }
  
  &:hover {
    transform: translateY(-12px);
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
    border-color: rgba(102, 187, 106, 0.4);
    
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
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    color: white;
    padding: 3px 10px;
    border-radius: 15px;
    font-size: 12px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
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
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    transition: transform 0.3s ease;
  }
  
  .service-duration {
    display: flex;
    align-items: center;
    color: #2e7d32;
    font-size: 15px;
    font-weight: 500;
    background: rgba(102, 187, 106, 0.15);
    padding: 6px 12px;
    border-radius: 15px;
    transition: all 0.3s ease;
    
    .el-icon {
      margin-right: 6px;
      color: #66bb6a;
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
  justify-content: flex-end;
  gap: 12px;
  margin-top: auto;
  
  .detail-button {
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
  
  .detail-button {
    background: rgba(102, 187, 106, 0.15);
    color: #2e7d32;
    border: 2px solid rgba(102, 187, 106, 0.3);
    
    &:hover {
      background: rgba(102, 187, 106, 0.25);
      border-color: #66bb6a;
      color: #66bb6a;
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
    --el-pagination-hover-color: #66bb6a;
    
    .el-pagination__jump {
      color: #2e7d32;
    }
    
    button:not(:disabled):hover {
      color: #66bb6a;
      background: rgba(102, 187, 106, 0.3);
      border-color: #66bb6a;
    }
    
    .is-active {
      background-color: #66bb6a !important;
      color: white !important;
      border-color: #66bb6a !important;
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

/* 公告详情对话框 */
.announcement-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.announcement-dialog .el-dialog__header {
  background-color: #f8f9fa;
  color: #333;
  padding: 20px 24px;
}

.announcement-dialog .el-dialog__title {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.announcement-dialog .el-dialog__headerbtn .el-dialog__close {
  color: #999;
}

.announcement-dialog .el-dialog__body {
  padding: 24px;
}

.announcement-detail {
  line-height: 1.6;
}

.dialog-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #333;
  text-align: center;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.announcement-time {
  font-size: 14px;
  color: #999;
}

.announcement-detail-content {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 20px;
  min-height: 200px;
  line-height: 1.8;
}

.announcement-detail-content p {
  margin: 0 0 16px 0;
  text-indent: 2em;
}

.announcement-detail-content p:last-child {
  margin-bottom: 0;
}

.no-content {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>