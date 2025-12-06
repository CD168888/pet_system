<template>
  <div class="announcement-list">
    <div class="announcement-header">
      <div class="title-wrapper">
        <i class="announcement-icon"></i>
        <h3 class="announcement-title">{{ title }}</h3>
      </div>
      <div class="more-link" v-if="showMoreLink" @click="onViewMore">
        更多公告
        <el-icon><arrow-right /></el-icon>
      </div>
    </div>
    
    <div class="announcement-content" v-loading="loading">
      <template v-if="announcements.length > 0">
        <div class="carousel-container">
          <div 
            class="carousel-track"
            :style="{ transform: `translateY(-${currentIndex * itemHeight}px)` }"
          >
            <div 
              class="announcement-item" 
              v-for="item in announcements" 
              :key="item.id"
              @click="onAnnouncementClick(item)"
            >
              <div class="announcement-badge" :class="getBadgeClass(item.type)">{{ getTypeShortName(item.type) }}</div>
              <div class="announcement-info">
                <div class="announcement-item-title">{{ item.title }}</div>
                <div class="announcement-date">{{ formatDate(item.createdTime) }}</div>
              </div>
            </div>
            <!-- 复制第一条作为无缝滚动的过渡 -->
            <div 
              v-if="announcements.length > 1" 
              class="announcement-item"
              @click="onAnnouncementClick(announcements[0])"
            >
              <div class="announcement-badge" :class="getBadgeClass(announcements[0].type)">{{ getTypeShortName(announcements[0].type) }}</div>
              <div class="announcement-info">
                <div class="announcement-item-title">{{ announcements[0].title }}</div>
                <div class="announcement-date">{{ formatDate(announcements[0].createdTime) }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>
      <div class="empty-tip" v-else>
        <el-empty description="暂无公告" :image-size="60" />
      </div>
    </div>
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="公告详情"
      width="600px"
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
          <!-- 使用计算属性处理内容，确保安全显示 -->
          <div v-if="processedContent" v-html="processedContent"></div>
          <div v-else class="no-content">暂无内容</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watchEffect } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

const props = defineProps({
  title: {
    type: String,
    default: '公告'
  },
  limit: {
    type: Number,
    default: 5
  },
  showMoreLink: {
    type: Boolean,
    default: true
  },
  autoPlay: {
    type: Boolean,
    default: true
  },
  interval: {
    type: Number,
    default: 3000
  }
})

const emit = defineEmits(['view-more', 'item-click'])

const announcements = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentAnnouncement = reactive({
  id: null,
  title: '',
  type: '',
  content: '',
  createdTime: ''
})

// 轮播相关状态
const currentIndex = ref(0)
const itemHeight = ref(40) // 每条公告的高度
let timer = null
const isAnimating = ref(false)

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true
  try {
    await request.get('/announcement/latest', { limit: props.limit }, {
      onSuccess: (data) => {
        announcements.value = data || []
      },
      showDefaultMsg: false
    })
  } catch (error) {
    console.error('获取公告列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取公告类型短名称
const getTypeShortName = (type) => {
  switch (type) {
    case 'NOTICE':
      return '通'
    case 'ACTIVITY':
      return '活'
    case 'PROMOTION':
      return '促'
    default:
      return '其'
  }
}

// 获取公告类型标签样式
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

// 处理公告内容，确保安全显示
const processedContent = computed(() => {
  if (!currentAnnouncement.content) return '';
  
  // 确保内容是字符串
  let content = String(currentAnnouncement.content).trim();
  
  // 如果内容为空，直接返回
  if (!content) return '';
  
  // 简单的HTML过滤，移除危险标签
  content = content.replace(/<script[^>]*>.*?<\/script>/gi, '');
  content = content.replace(/<style[^>]*>.*?<\/style>/gi, '');
  
  // 如果内容不是HTML格式，添加适当的格式
  if (!content.includes('<')) {
    // 替换换行符为<br>
    content = content.replace(/\n/g, '<br>');
    // 添加段落标签
    content = `<p>${content}</p>`;
  }
  
  // 确保内容有合适的段落结构
  if (!content.startsWith('<p>')) {
    content = `<p>${content}</p>`;
  }
  
  return content;
})

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

// 获取公告类型徽章样式
const getBadgeClass = (type) => {
  switch (type) {
    case 'NOTICE':
      return 'badge-notice'
    case 'ACTIVITY':
      return 'badge-activity'
    case 'PROMOTION':
      return 'badge-promotion'
    default:
      return 'badge-other'
  }
}

// 查看更多公告
const onViewMore = () => {
  emit('view-more')
}

// 点击公告项
const onAnnouncementClick = (announcement) => {
  console.log('点击的公告:', announcement); // 调试信息
  
  // 确保announcement对象有效
  if (!announcement) {
    console.error('announcement对象无效');
    return;
  }
  
  // 清空之前的数据，避免残留
  Object.keys(currentAnnouncement).forEach(key => {
    currentAnnouncement[key] = '';
  });
  
  // 直接赋值当前公告信息，确保所有字段都被正确设置
  currentAnnouncement.id = announcement.id || '';
  currentAnnouncement.title = announcement.title || '暂无标题';
  currentAnnouncement.type = announcement.type || '';
  currentAnnouncement.content = announcement.content || '暂无内容';
  currentAnnouncement.createdTime = announcement.createdTime || '';
  
  console.log('当前公告信息:', currentAnnouncement); // 调试信息
  
  // 延迟打开弹窗，确保数据已更新
  setTimeout(() => {
    dialogVisible.value = true;
  }, 50);
  
  emit('item-click', announcement);
}

// 轮播控制方法
const nextSlide = () => {
  if (isAnimating.value || announcements.value.length <= 1) return
  
  isAnimating.value = true
  currentIndex.value++
  
  // 当滚动到最后一个复制项时，重置到第一个
  setTimeout(() => {
    if (currentIndex.value >= announcements.value.length) {
      currentIndex.value = 0
      // 关闭动画效果进行重置
      const track = document.querySelector('.carousel-track')
      if (track) {
        track.style.transition = 'none'
        track.style.transform = `translateY(0)`
        
        // 重新开启动画
        setTimeout(() => {
          track.style.transition = 'transform 0.5s ease'
        }, 50)
      }
    }
    isAnimating.value = false
  }, 500)
}



// 自动轮播
const startAutoPlay = () => {
  if (!props.autoPlay || announcements.value.length <= 1) return
  
  stopAutoPlay()
  timer = setInterval(() => {
    nextSlide()
  }, props.interval)
}

// 停止自动轮播
const stopAutoPlay = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(() => {
  fetchAnnouncements()
})

onUnmounted(() => {
  stopAutoPlay()
})

// 监听公告数据变化，重新开始自动轮播
watchEffect(() => {
  if (announcements.value.length > 0) {
    // 重置当前索引
    currentIndex.value = 0
    // 延迟启动自动轮播，确保DOM已更新
    setTimeout(() => {
      startAutoPlay()
    }, 1000)
  }
})
</script>

<style lang="scss" scoped>
.announcement-list {
  width: 100%;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
  .announcement-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    
    .title-wrapper {
      display: flex;
      align-items: center;
      
      .announcement-icon {
        width: 16px;
        height: 16px;
        background-color: #FFB6C1;
        border-radius: 50%;
        margin-right: 8px;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          width: 8px;
          height: 8px;
          background-color: white;
          border-radius: 50%;
        }
      }
      
      .announcement-title {
        font-size: 16px;
        font-weight: 600;
        color: rgba(0, 0, 0, 0.8);
        margin: 0;
      }
    }
    
    .more-link {
        display: flex;
        align-items: center;
        color: rgba(135, 206, 235, 0.9);
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s ease;
      
      .el-icon {
        margin-left: 4px;
        font-size: 12px;
        transition: transform 0.3s ease;
      }
      
      &:hover {
        color: #FFB6C1;
        
        .el-icon {
          transform: translateX(3px);
        }
      }
    }
  }
  
  .announcement-content {
    padding: 10px 0;
    min-height: 100px;
    
    /* 响应式调整 */
    @media (max-width: 768px) {
      .carousel-container {
        height: calc(var(--item-height, 40px) * 1.5); /* 移动端显示1.5条公告 */
      }
      
      .announcement-item {
        padding: 8px 15px;
        font-size: 13px;
      }
      
      .carousel-controls {
        right: 5px;
      }
      
      .control-btn {
        width: 24px;
        height: 24px;
      }
    }
    
    @media (max-width: 480px) {
        .carousel-container {
          height: var(--item-height, 40px); /* 小屏只显示1条公告 */
        }
      }
    
    .carousel-container {
      position: relative;
      overflow: hidden;
      height: calc(var(--item-height, 40px) * 2); /* 显示2条公告 */
    }
    
    .carousel-track {
      transition: transform 0.5s ease;
    }
    
    .announcement-item {
        display: flex;
        align-items: center;
        padding: 10px 20px;
        cursor: pointer;
        transition: background-color 0.3s ease;
        height: var(--item-height, 40px); /* 固定高度 */
        box-sizing: border-box;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.2);
        }
      
      .announcement-badge {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        color: white;
        margin-right: 12px;
        flex-shrink: 0;
      }
      
      .badge-notice {
        background-color: #87CEEB;
      }
      
      .badge-activity {
        background-color: #67C23A;
      }
      
      .badge-promotion {
        background-color: #FFA07A;
      }
      
      .badge-other {
        background-color: #909399;
      }
      
      .announcement-info {
        flex: 1;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .announcement-item-title {
          font-size: 14px;
          color: rgba(0, 0, 0, 0.8);
          flex: 1;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          margin-right: 15px;
        }
        
        .announcement-date {
          font-size: 12px;
          color: rgba(0, 0, 0, 0.6);
          flex-shrink: 0;
        }
      }
    }
    
    .empty-tip {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100px;
    }
    

  }
}

.announcement-detail {
  .dialog-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #333;
    text-align: center;
    padding: 0 20px;
    line-height: 1.4;
  }
  
  .announcement-meta {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 2px solid rgba(135, 206, 235, 0.3);
    
    .announcement-time {
      margin-left: 15px;
      font-size: 14px;
      color: #666;
    }
  }
  
  .announcement-detail-content {
    padding: 25px;
    background-color: #ffffff;
    border: 1px solid #eee;
    border-radius: 12px;
    line-height: 2.0;
    min-height: 250px;
    color: #333;
    font-size: 15px;
    overflow-y: auto;
    max-height: 450px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    
    p {
      margin-bottom: 15px;
      margin-top: 0;
      text-indent: 2em;
    }
    
    &::-webkit-scrollbar {
      width: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f5f5f5;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(135, 206, 235, 0.6);
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb:hover {
      background: rgba(135, 206, 235, 0.8);
    }
    
    .no-content {
      text-align: center;
      color: #999;
      padding: 60px 0;
      font-size: 16px;
    }
  }
}

/* 自定义弹窗样式 */
:deep(.announcement-dialog) {
  .el-dialog__header {
    background: linear-gradient(135deg, rgba(135, 206, 235, 0.8), rgba(255, 182, 193, 0.6));
    border-radius: 8px 8px 0 0;
    padding: 20px;
  }
  
  .el-dialog__title {
    color: #333;
    font-size: 18px;
    font-weight: 600;
  }
  
  .el-dialog__close {
    color: #333;
    font-size: 20px;
  }
  
  .el-dialog__body {
    padding: 25px;
    background: rgba(255, 255, 255, 0.95);
  }
}

/* 响应式调整 */
@media (max-width: 768px) {
  :deep(.announcement-dialog) {
    width: 90% !important;
  }
  
  .announcement-detail {
    .dialog-title {
      font-size: 18px;
    }
    
    .announcement-detail-content {
      padding: 20px;
      font-size: 14px;
    }
  }
}
</style> 