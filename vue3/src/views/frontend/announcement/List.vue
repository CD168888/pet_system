<template>
  <div class="pet-list-page">
    <!-- 页面标题区域 -->
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>公告列表</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1 class="page-title">公告中心</h1>
        <p class="page-subtitle">最新公告信息，为您提供第一手资讯</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-section">
      <div class="container">
        <!-- 搜索和筛选区域 -->
        <div class="filter-container">
          <el-card shadow="never" class="search-card">
            <el-form :inline="true" :model="searchForm" class="search-form">
              <el-form-item label="公告标题">
                <el-input v-model="searchForm.title" placeholder="输入公告标题" clearable @keyup.enter="handleSearch" />
              </el-form-item>
              <el-form-item label="公告类型">
                <el-select v-model="searchForm.type" placeholder="选择公告类型" clearable>
                  <el-option label="所有类型" value="" />
                  <el-option label="通知" value="NOTICE" />
                  <el-option label="活动" value="ACTIVITY" />
                  <el-option label="促销" value="PROMOTION" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearch" class="search-btn">
                  <el-icon><Search /></el-icon> 搜索
                </el-button>
                <el-button @click="resetSearch" class="reset-btn">
                  <el-icon><Refresh /></el-icon> 重置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 公告列表区域 -->
        <div class="announcement-list-section">
          <div class="loading-container" v-loading="loading">
            <div v-if="announcements.length > 0" class="announcements-grid">
              <div 
                v-for="(item, index) in announcements" 
                :key="item.id" 
                class="announcement-card glass-card" 
                :class="`delay-${(index % 6 + 1) * 100}`"
                @click="viewAnnouncementDetail(item)"
              >
                <div class="card-header">
                  <el-tag :type="getTypeTagType(item.type)" size="small" class="type-tag">
                    {{ getTypeLabel(item.type) }}
                  </el-tag>
                  <span class="date">{{ formatDate(item.createdTime) }}</span>
                </div>
                <h3 class="card-title">{{ item.title }}</h3>
                <p class="card-content">{{ truncateText(item.content || '暂无内容', 150) }}</p>
                <div class="card-footer">
                  <span class="view-more">查看详情 <el-icon><ArrowRight /></el-icon></span>
                </div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-else class="empty-state">
              <svg class="empty-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <path d="M12 16v-4"></path>
                <path d="M12 8h.01"></path>
              </svg>
              <h3 class="empty-title">暂无公告信息</h3>
              <p class="empty-desc">请稍后再来查看，我们会及时更新公告内容</p>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="total > 0">
            <el-pagination 
              background 
              layout="total, prev, pager, next, jumper" 
              :current-page="currentPage" 
              :page-size="pageSize" 
              :total="total" 
              @current-change="handleCurrentChange" 
              class="modern-pagination" 
            />
          </div>
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
import { Search, Refresh, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 分页参数
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 加载状态
const loading = ref(false)

// 公告列表数据
const announcements = ref([])

// 搜索表单
const searchForm = reactive({
  title: '',
  type: ''
})

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
    await request.get('/announcement/page', {
      title: searchForm.title,
      type: searchForm.type,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        announcements.value = res.records || []
        total.value = res.total || 0
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
  searchForm.type = ''
  currentPage.value = 1
  fetchAnnouncements()
}

// 分页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
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
    case 'PROMOTION':
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
    case 'PROMOTION':
      return '促销'
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

<style scoped>
.pet-list-page {
  min-height: 100vh;
  background-color: transparent;
  position: relative;
  padding-bottom: 50px;
  
  &::before, &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    opacity: 0.1;
    z-index: 0;
  }
  
  &::before {
    background: radial-gradient(circle, #FFB6C1 0%, transparent 70%);
    top: 50%;
    left: -100px;
    transform: translateY(-50%);
  }
  
  &::after {
    background: radial-gradient(circle, #FFD700 0%, transparent 70%);
    top: 50%;
    right: -100px;
    transform: translateY(-50%);
  }
}

/* 页面标题区域 */
.page-banner {
  background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
  padding: 60px 40px;
  text-align: center;
  position: relative;
  margin-bottom: 40px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.page-banner .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  position: relative;
  z-index: 1;
}

.breadcrumb {
  margin-bottom: 20px;
  justify-content: center;
}

.breadcrumb .el-breadcrumb__item a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
}

.breadcrumb .el-breadcrumb__item a:hover {
  color: white;
}

.page-title {
  font-family: 'Nunito Sans', sans-serif;
  font-size: 42px;
  color: white;
  margin-bottom: 15px;
  animation: fadeInDown 0.8s ease;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-family: 'Open Sans', sans-serif;
  font-size: 20px;
  color: rgba(255, 255, 255, 0.9);
  opacity: 0.9;
  animation: fadeInUp 0.8s ease;
  margin: 0;
}

.page-banner .banner-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
  pointer-events: none;
}

.page-banner .decoration-paw {
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
    bottom: 20%;
    left: 15%;
    animation: rotate 30s linear infinite reverse, float 8s ease-in-out infinite;
  }
  
  &.paw-3 {
    top: 30%;
    left: 10%;
    animation: rotate 20s linear infinite, float 7s ease-in-out infinite;
  }
}

/* 内容区域 */
.content-section {
  padding: 20px 0;
  position: relative;
  z-index: 1;
}

.content-section .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 动画效果定义 */
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

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 筛选区域 */
.filter-container {
  margin-bottom: 30px;
}

.search-card {
  background-color: transparent;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.search-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-btn {
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 30px;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.search-btn::before {
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

.search-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
}

.search-btn:hover::before {
  left: 100%;
}

.search-btn:active {
  transform: translateY(-1px);
}

.reset-btn {
  border-radius: 6px;
}

/* 公告列表区域 */
.announcement-list-section {
  margin-bottom: 40px;
}

.loading-container {
  min-height: 400px;
}

.announcements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.announcement-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* 确保ElCard组件透明 */
.el-card {
  background-color: transparent !important;
  border: none !important;
}

/* 确保筛选区域的表单透明 */
.search-form {
  background-color: transparent !important;
}

.announcement-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 45px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.type-tag {
  font-size: 12px;
  padding: 2px 8px;
}

.date {
  font-size: 13px;
  color: #999;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.card-footer {
  text-align: right;
}

.view-more {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.view-more .el-icon {
  margin-left: 4px;
  font-size: 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #666;
}

.empty-desc {
  font-size: 14px;
  margin: 0;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.modern-pagination {
  --el-pagination-button-size: 36px;
  --el-pagination-font-size: 14px;
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

/* 玻璃态卡片效果 */
.glass-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* 动画延迟 */
.delay-100 {
  animation-delay: 0.1s;
}

.delay-200 {
  animation-delay: 0.2s;
}

.delay-300 {
  animation-delay: 0.3s;
}

.delay-400 {
  animation-delay: 0.4s;
}

.delay-500 {
  animation-delay: 0.5s;
}

.delay-600 {
  animation-delay: 0.6s;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
  
  .announcements-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .search-form {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .search-form .el-form-item {
    margin-bottom: 0;
  }
}
</style>