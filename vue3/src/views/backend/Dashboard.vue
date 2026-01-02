<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-section glass-card">
      <div class="welcome-content">
        <div class="welcome-avatar">
          <el-avatar :size="90" :src="avatarUrl" class="welcome-avatar-inner">
            <el-icon class="welcome-avatar-icon"><UserFilled /></el-icon>
          </el-avatar>
        </div>
        <div class="welcome-info">
          <h1 class="welcome-title">欢迎回来，{{ userInfo?.name || userInfo?.username }}</h1>
          <p class="welcome-time">{{ currentTime }}</p>
        </div>
        <div class="welcome-action">
          <el-button 
            type="primary" 
            icon="Refresh" 
            @click="refreshData"
            class="refresh-btn"
            :icon-size="20"
          >
            刷新数据
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 数据总览卡片 -->
    <div class="stats-grid">
      <div 
        v-for="stat in statsData" 
        :key="stat.label"
        class="stat-card glass-card"
      >
        <div class="stat-content">
          <div class="stat-icon" :style="{ background: stat.gradient }">
            <el-icon><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
          <el-tooltip 
              :content="`相对于上月${stat.desc.replace('较上月增长', '')}`" 
              placement="top"
              effect="light"
              :enterable="true"
              hide-after="2000"
            >
              <div class="stat-growth" v-if="stat.desc">
                <el-icon 
                  class="growth-icon"
                  :class="{
                    'positive': parseFloat(stat.desc.replace(/[^\d.-]/g, '')) > 0,
                    'negative': parseFloat(stat.desc.replace(/[^\d.-]/g, '')) < 0,
                    'neutral': parseFloat(stat.desc.replace(/[^\d.-]/g, '')) === 0
                  }"
                >
                  <ArrowUp v-if="parseFloat(stat.desc.replace(/[^\d.-]/g, '')) > 0" />
                  <ArrowDown v-else-if="parseFloat(stat.desc.replace(/[^\d.-]/g, '')) < 0" />
                  <Minus v-else />
                </el-icon>
                <span 
                  class="growth-value"
                  :class="{
                    'positive': parseFloat(stat.desc.replace(/[^\d.-]/g, '')) > 0,
                    'negative': parseFloat(stat.desc.replace(/[^\d.-]/g, '')) < 0,
                    'neutral': parseFloat(stat.desc.replace(/[^\d.-]/g, '')) === 0
                  }"
                >
                  {{ stat.desc.replace('较上月增长', '') }}
                </span>
              </div>
            </el-tooltip>
        </div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-section">
      <!-- 主要趋势图表 -->
      <div class="main-chart-container glass-card">
        <div class="chart-header">
          <h3 class="chart-title">数据趋势</h3>
          <el-select 
            v-model="trendType" 
            placeholder="选择数据类型" 
            size="small"
            class="chart-select"
          >
            <el-option label="用户注册" value="user" />
            <el-option label="宠物领养" value="pet" />
            <el-option label="服务预约" value="service" />
            <el-option label="训练预约" value="training" />
            <el-option label="寄养申请" value="boarding" />
          </el-select>
        </div>
        <div class="chart-content" v-loading="loading">
          <div ref="trendChartRef" class="trend-chart"></div>
        </div>
      </div>
      
      <!-- 右侧卡片区域 -->
      <div class="right-cards">
        <!-- 卡片1：数据分布 -->
        <div class="chart-card glass-card">
          <div class="chart-header">
            <h3 class="chart-title">数据分布</h3>
            <el-select 
              v-model="pieType" 
              placeholder="选择分布类型" 
              size="small"
              class="chart-select"
            >
              <el-option label="服务状态" value="serviceStatus" />
              <el-option label="训练状态" value="trainingStatus" />
            </el-select>
          </div>
          <div class="chart-content" v-loading="loading">
            <div ref="pieChartRef" class="pie-chart"></div>
          </div>
        </div>
        
        <!-- 卡片2：系统状态 -->
        <div class="status-card glass-card">
          <div class="chart-header">
            <h3 class="chart-title">系统状态</h3>
          </div>
          <div class="status-grid">
            <div class="status-item" v-for="status in systemStatus" :key="status.label">
              <div class="status-label">{{ status.label }}</div>
              <div class="status-value" :class="status.status">
                <el-icon><component :is="status.icon" /></el-icon>
                <span>{{ status.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import {
  User,
  House,
  Service,
  Trophy,
  CircleCheckFilled,
  CircleCloseFilled,
  CircleWarningFilled,
  ArrowUp,
  ArrowDown,
  Minus
} from '@element-plus/icons-vue'
import { UserFilled, Refresh } from '@element-plus/icons-vue'

// 修改ECharts的导入方式
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
const loading = ref(false)

// 图表实例
let trendChart = null
let pieChart = null

// 当前选择的图表类型
const trendType = ref('user')
const pieType = ref('serviceStatus')

// 统计数据
const statistics = ref({})
const serviceStatusData = ref([])
const trainingStatusData = ref([])

// 图表DOM引用
const trendChartRef = ref(null)
const pieChartRef = ref(null)

// 角色标签
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'USER': '普通用户'
  }
  return roleMap[userInfo.value?.roleCode] || '未知角色'
})

const avatarUrl = computed(() => {
  return userInfo.value?.avatar ? baseAPI + userInfo.value.avatar : '';
})

// 当前时间
const currentTime = ref('')
let timeInterval = null // 保存定时器引用

const updateTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

// 数据总览卡片数据
const statsData = ref([
  {
    label: '用户总数',
    value: '0',
    icon: User,
    gradient: 'linear-gradient(135deg, #66bb6a 0%, #43a047 100%)',
    desc: '较上月增长 0%'
  },
  {
    label: '宠物总数',
    value: '0',
    icon: House,
    gradient: 'linear-gradient(135deg, #52c787 0%, #4ecdc4 100%)',
    desc: '较上月增长 0%'
  },
  {
    label: '服务总数',
    value: '0',
    icon: Service,
    gradient: 'linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%)',
    desc: '较上月增长 0%'
  },
  {
    label: '训练总数',
    value: '0',
    icon: Trophy,
    gradient: 'linear-gradient(135deg, #48bfe3 0%, #5390d9 100%)',
    desc: '较上月增长 0%'
  }
])

// 系统状态数据
const systemStatus = ref([
  {
    label: '系统状态',
    value: '运行正常',
    status: 'success',
    icon: CircleCheckFilled
  },
  {
    label: '服务器负载',
    value: '32%',
    status: 'warning',
    icon: CircleWarningFilled
  },
  {
    label: '数据库连接',
    value: '正常',
    status: 'success',
    icon: CircleCheckFilled
  },
  {
    label: 'API响应',
    value: '23ms',
    status: 'success',
    icon: CircleCheckFilled
  }
])

// 刷新数据函数
const refreshData = async () => {
  await fetchStatistics()
  await fetchPieChartData()
}

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    await request.get('/statistics/overview', {}, {
      onSuccess: (data) => {
        statistics.value = data
        
        // 更新数据总览卡片数据
        statsData.value = [
          {
            label: '用户总数',
            value: data.totalUserCount.toLocaleString(),
            icon: User,
            gradient: 'linear-gradient(135deg, #66bb6a 0%, #43a047 100%)',
            desc: `较上月增长 ${data.userGrowthRate || 0}%` // 使用真实增长数据
          },
          {
            label: '宠物总数',
            value: data.totalPetCount.toLocaleString(),
            icon: House,
            gradient: 'linear-gradient(135deg, #52c787 0%, #4ecdc4 100%)',
            desc: `较上月增长 ${data.petGrowthRate || 0}%`
          },
          {
            label: '服务总数',
            value: data.totalServiceAppointmentCount.toLocaleString(),
            icon: Service,
            gradient: 'linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%)',
            desc: `较上月增长 ${data.serviceGrowthRate || 0}%`
          },
          {
            label: '训练总数',
            value: data.totalTrainingCount.toLocaleString(),
            icon: Trophy,
            gradient: 'linear-gradient(135deg, #48bfe3 0%, #5390d9 100%)',
            desc: `较上月增长 ${data.trainingGrowthRate || 0}%`
          }
        ]
        
        // 获取数据后更新趋势图
        nextTick(() => {
          updateTrendChart()
        })
      }
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取饼图数据
const fetchPieChartData = async () => {
  loading.value = true
  try {
    // 服务状态分布
    await request.get('/statistics/service/status', {}, {
      onSuccess: (data) => {
        serviceStatusData.value = data
      }
    })
    
    // 训练状态分布
    await request.get('/statistics/training/status', {}, {
      onSuccess: (data) => {
        trainingStatusData.value = data
        // 获取所有数据后更新饼图
        nextTick(() => {
          updatePieChart()
        })
      }
    })
  } catch (error) {
    console.error('获取饼图数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化趋势图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  // 立即更新图表数据
  updateTrendChart()
}

// 更新趋势图
const updateTrendChart = () => {
  if (!trendChart) {
    // 如果图表实例不存在，先初始化
    initTrendChart();
    return;
  }
  
  let trendData = []
  let title = ''
  
  // 渐变颜色配置 - 与背景主题贴合，底部与背景色融合
  const gradientColors = {
    user: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
      { offset: 0, color: 'rgba(240, 242, 245, 1)' }, // 底部：与背景色一致
      { offset: 0.5, color: 'rgba(240, 242, 245, 1)' }, // 中间：继续保持背景色，增加与背景的融合度
      { offset: 0.8, color: '#66bb6a' }, // 主题色：位置上移，让背景色占据更多比例
      { offset: 1, color: '#43a047' } // 顶部：深色主题色
    ]),
    pet: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
      { offset: 0, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.5, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.8, color: '#52c787' },
      { offset: 1, color: '#4ecdc4' }
    ]),
    service: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
      { offset: 0, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.5, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.8, color: '#ff9a56' },
      { offset: 1, color: '#ff6b6b' }
    ]),
    training: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
      { offset: 0, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.5, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.8, color: '#48bfe3' },
      { offset: 1, color: '#5390d9' }
    ]),
    boarding: new echarts.graphic.LinearGradient(0, 1, 0, 0, [
      { offset: 0, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.5, color: 'rgba(240, 242, 245, 1)' },
      { offset: 0.8, color: '#ffb74d' },
      { offset: 1, color: '#ffa726' }
    ])
  }
  
  switch (trendType.value) {
    case 'user':
      trendData = statistics.value.userTrend || []
      title = '用户注册趋势'
      break
    case 'pet':
      trendData = statistics.value.petTrend || []
      title = '宠物领养趋势'
      break
    case 'service':
      trendData = statistics.value.serviceTrend || []
      title = '服务预约趋势'
      break
    case 'training':
      trendData = statistics.value.trainingTrend || []
      title = '训练预约趋势'
      break
    case 'boarding':
      trendData = statistics.value.boardingTrend || []
      title = '寄养申请趋势'
      break
  }
  
  const months = trendData.map(item => item.month)
  const counts = trendData.map(item => item.count)
  
  const option = {
    title: {
      text: title,
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        shadowBlur: 10,
        shadowColor: 'rgba(102, 126, 234, 0.1)'
      },
      formatter: '{b}: {c}',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.2)',
      borderWidth: 1,
      borderRadius: 12,
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisTick: {
        alignWithLabel: true
      },
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#e0e0e0'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: '数量',
        type: 'line',
        data: counts,
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#43a047'
        },
        itemStyle: {
          color: '#43a047',
          borderColor: '#fff',
          borderWidth: 3,
          borderRadius: 50
        },
        symbol: 'circle',
        symbolSize: 8,
        emphasis: {
          itemStyle: {
            symbolSize: 12,
            shadowBlur: 10,
            shadowColor: 'rgba(67, 160, 71, 0.5)'
          }
        },
        areaStyle: {
          opacity: 0.3,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#66bb6a' },
            { offset: 0.5, color: 'rgba(102, 187, 106, 0.2)' },
            { offset: 1, color: 'rgba(102, 187, 106, 0)' }
          ])
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  
  // 立即更新图表数据
  updatePieChart()
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) {
    // 如果图表实例不存在，先初始化
    initPieChart();
    return;
  }
  
  let pieData = []
  let title = ''
  
  // 默认数据，防止API返回为空
  const defaultPieData = [
    { value: 10, name: '已完成' },
    { value: 8, name: '进行中' },
    { value: 5, name: '待处理' }
  ]
  
  switch (pieType.value) {
    case 'serviceStatus':
      pieData = serviceStatusData.value.length > 0 ? serviceStatusData.value : defaultPieData
      title = '服务状态分布'
      break
    case 'trainingStatus':
      pieData = trainingStatusData.value.length > 0 ? trainingStatusData.value : defaultPieData
      title = '训练状态分布'
      break
  }
  
  // 饼图渐变色配置 - 与Home.vue保持一致的色调
  const pieColors = [
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#66bb6a' },
      { offset: 1, color: '#43a047' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#52c787' },
      { offset: 1, color: '#4ecdc4' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#ff9a56' },
      { offset: 1, color: '#ff6b6b' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#48bfe3' },
      { offset: 1, color: '#5390d9' }
    ]),
    new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#ffb74d' },
      { offset: 1, color: '#ffa726' }
    ])
  ]
  
  const option = {
    title: {
      text: title,
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#333'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: 'rgba(255, 255, 255, 0.2)',
      borderWidth: 1,
      borderRadius: 12,
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)'
    },
    legend: {
      orient: 'vertical',
      left: '10%',
      top: 'middle',
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 16,
      textStyle: {
        color: '#666',
        fontSize: 14
      },
      formatter: '{name}'
    },
    series: [
      {
        name: '数量',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['65%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 12,
          borderColor: '#fff',
          borderWidth: 4,
          color: function(params) {
            return pieColors[params.dataIndex % pieColors.length]
          }
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
            color: '#333'
          },
          itemStyle: {
            shadowBlur: 20,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 监听图表类型变化
watch(trendType, () => {
  nextTick(() => {
    updateTrendChart()
  })
})

watch(pieType, () => {
  nextTick(() => {
    updatePieChart()
  })
})

// 处理窗口大小变化
const handleResize = () => {
  // 使用nextTick确保DOM已更新
  nextTick(() => {
    if (trendChart) {
      trendChart.resize()
    }
    if (pieChart) {
      pieChart.resize()
    }
  })
}

onMounted(() => {
  updateTime()
  // 每分钟更新一次时间
  timeInterval = setInterval(updateTime, 60000)
  
  // 先初始化图表容器
  nextTick(() => {
    // 初始化图表实例
    if (trendChartRef.value) {
      trendChart = echarts.init(trendChartRef.value)
    }
    
    if (pieChartRef.value) {
      pieChart = echarts.init(pieChartRef.value)
    }
    
    // 获取数据
    fetchStatistics()
    fetchPieChartData()
  })
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清除定时器
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }
  
  // 销毁图表实例
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
  
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
/* 全局变量 - 与Home.vue保持一致的颜色方案 */
$primary-color: #66bb6a;
$secondary-color: #43a047;
$tertiary-color: #52c787;
$accent-color: #4ecdc4;
$warning-color: #ff9a56;
$error-color: #ff6b6b;
$bg-color: #f0f2f5;
$card-radius: 24px;
$shadow-sm: 0 2px 10px rgba(102, 187, 106, 0.1);
$shadow-md: 0 8px 32px rgba(102, 187, 106, 0.15);
$shadow-lg: 0 12px 48px rgba(102, 187, 106, 0.2);

/* 玻璃态效果 - 与Home.vue保持一致 */
.glass-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: $card-radius;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
  
  &:nth-child(1) {
    animation-delay: 0.1s;
  }
  
  &:nth-child(2) {
    animation-delay: 0.2s;
  }
  
  &:nth-child(3) {
    animation-delay: 0.3s;
  }
  
  &:nth-child(4) {
    animation-delay: 0.4s;
  }
  
  &:nth-child(5) {
    animation-delay: 0.5s;
  }
  
  &:nth-child(6) {
    animation-delay: 0.6s;
  }
  
  &:hover {
    box-shadow: 0 20px 50px rgba(102, 187, 106, 0.2);
    transform: translateY(-10px);
    background: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.7);
  }
}

/* 动画效果 */
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

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.dashboard {
  background: $bg-color;
  min-height: 100vh;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  
  /* 欢迎卡片 */
  .welcome-section {
    margin-bottom: 24px;
    
    .welcome-content {
      display: flex;
      align-items: center;
      gap: 24px;
      padding: 24px;
      
      @media (max-width: 768px) {
        flex-direction: column;
        text-align: center;
      }
      
      .welcome-avatar {
        .welcome-avatar-inner {
          border: 4px solid rgba(255, 255, 255, 0.8);
          box-shadow: $shadow-md;
          transition: transform 0.3s ease;
          
          &:hover {
            transform: scale(1.1);
          }
        }
        
        .welcome-avatar-icon {
          font-size: 40px;
          color: $primary-color;
        }
      }
      
      .welcome-info {
        flex: 1;
        
        .welcome-title {
          font-size: 28px;
          font-weight: 600;
          margin: 0 0 8px 0;
          background: linear-gradient(135deg, $primary-color, $secondary-color);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
        
        .welcome-time {
          font-size: 16px;
          color: #666;
          margin: 0;
        }
      }
      
      .welcome-action {
        .refresh-btn {
          background: linear-gradient(135deg, $primary-color, $secondary-color);
          border: none;
          padding: 10px 24px;
          font-size: 16px;
          font-weight: 500;
          border-radius: 50px;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 4px 15px rgba(102, 187, 106, 0.2);
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
            transition: left 0.5s ease;
            z-index: -1;
          }
          
          &:hover {
            box-shadow: 0 8px 20px rgba(102, 187, 106, 0.3);
            transform: translateY(-3px) scale(1.05);
            
            &::before {
              left: 100%;
            }
          }
          
          &:active {
            transform: translateY(-1px) scale(0.98);
          }
          
          &:focus {
            outline: none;
            box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.3);
          }
        }
      }
    }
  }
  
  /* 数据总览卡片 */
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    @media (max-width: 768px) {
      grid-template-columns: 1fr;
      gap: 16px;
    }
    
    @media (max-width: 480px) {
      gap: 12px;
    }
    
    .stat-card {
        padding: 20px;
        
        @media (max-width: 480px) {
          padding: 16px;
        }
        
        .stat-content {
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 20px;
          
          @media (max-width: 480px) {
            gap: 16px;
          }
        }
        
        .stat-icon {
          width: 60px;
          height: 60px;
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          box-shadow: $shadow-sm;
          
          @media (max-width: 480px) {
            width: 50px;
            height: 50px;
          }
          
          .el-icon {
            font-size: 28px;
            color: white;
            
            @media (max-width: 480px) {
              font-size: 22px;
            }
          }
        }
        
        .stat-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          text-align: left;
          
          .stat-value {
            font-size: 32px;
            font-weight: 700;
            color: #1a1a1a;
            margin: 0 0 4px 0;
            
            @media (max-width: 480px) {
              font-size: 24px;
            }
          }
          
          .stat-label {
            font-size: 16px;
            color: #666;
            margin: 0;
            font-weight: 500;
            
            @media (max-width: 480px) {
              font-size: 14px;
            }
          }
        }
        
        .stat-growth {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          justify-content: center;
          gap: 4px;
          
          .growth-icon {
            font-size: 16px;
            
            &.positive {
              color: #67c23a;
            }
            
            &.negative {
              color: #f56c6c;
            }
            
            &.neutral {
              color: #909399;
            }
          }
          
          .growth-value {
            font-size: 14px;
            font-weight: 600;
            
            @media (max-width: 480px) {
              font-size: 12px;
            }
            
            &.positive {
              color: #67c23a;
            }
            
            &.negative {
              color: #f56c6c;
            }
            
            &.neutral {
              color: #909399;
            }
          }
        }
      }
  }
  
  /* 图表区域 */
  .charts-section {
    display: grid;
    grid-template-columns: 1fr 360px;
    gap: 20px;
    
    @media (max-width: 1200px) {
      grid-template-columns: 1fr;
    }
    
    @media (max-width: 768px) {
      gap: 16px;
    }
    
    @media (max-width: 480px) {
      gap: 12px;
    }
    
    /* 主要趋势图表 */
    .main-chart-container {
      padding: 24px;
      
      @media (max-width: 480px) {
        padding: 16px;
      }
      
      .chart-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        @media (max-width: 480px) {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;
          margin-bottom: 16px;
        }
      }
      
      .chart-title {
        font-size: 20px;
        font-weight: 600;
        color: #1a1a1a;
        margin: 0;
        
        @media (max-width: 480px) {
          font-size: 18px;
        }
      }
      
      .chart-select {
        border-radius: 12px;
        border-color: rgba(102, 126, 234, 0.3);
        
        &:hover {
          border-color: $primary-color;
        }
        
        @media (max-width: 480px) {
          width: 100%;
        }
      }
      
      .chart-content {
        .trend-chart {
          height: 400px;
          width: 100%;
          
          @media (max-width: 768px) {
            height: 300px;
          }
          
          @media (max-width: 480px) {
            height: 250px;
          }
        }
      }
    }
    
    /* 右侧卡片区域 */
    .right-cards {
      display: flex;
      flex-direction: column;
      gap: 20px;
      
      @media (max-width: 768px) {
        gap: 16px;
      }
      
      @media (max-width: 480px) {
        gap: 12px;
      }
      
      .chart-card {
        padding: 24px;
        
        @media (max-width: 480px) {
          padding: 16px;
        }
        
        .chart-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          
          @media (max-width: 480px) {
            flex-direction: column;
            align-items: flex-start;
            gap: 12px;
            margin-bottom: 16px;
          }
        }
        
        .chart-content {
          .pie-chart {
            height: 280px;
            width: 100%;
            
            @media (max-width: 768px) {
              height: 240px;
            }
            
            @media (max-width: 480px) {
              height: 200px;
            }
          }
        }
      }
      
      .status-card {
        padding: 24px;
        
        @media (max-width: 480px) {
          padding: 16px;
        }
        
        .chart-header {
          margin-bottom: 20px;
          text-align: center;
          
          @media (max-width: 480px) {
            margin-bottom: 16px;
          }
        }
        
        .status-grid {
          display: grid;
          grid-template-columns: 1fr;
          gap: 20px;
          
          .status-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 16px 24px;
            background: rgba(255, 255, 255, 0.8);
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            
            &:hover {
              box-shadow: 0 4px 12px rgba(102, 187, 106, 0.15);
              transform: translateY(-1px);
            }
            
            .status-label {
              font-size: 16px;
              color: #666;
              font-weight: 500;
            }
            
            .status-value {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 18px;
              font-weight: 600;
              
              &.success {
                color: #67c23a;
              }
              
              &.warning {
                color: #e6a23c;
              }
              
              &.error {
                color: #f56c6c;
              }
              
              .el-icon {
                font-size: 20px;
              }
            }
          }
        }
      }
    }
  }
}

/* 加载状态 */
:deep(.el-loading-mask) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  
  .el-loading-spinner {
    .path {
      stroke: $primary-color;
    }
  }
}

/* 按钮样式 */
:deep(.el-button) {
  border-radius: 12px;
  transition: all 0.3s ease;
}

/* 选择器样式 */
:deep(.el-select) {
  .el-input {
    .el-input__wrapper {
      border-radius: 12px;
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: $shadow-sm;
      }
    }
  }
}

/* 卡片通用样式 */
:deep(.el-card) {
  border-radius: $card-radius;
  border: none;
  box-shadow: $shadow-sm;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
  
  .el-card__header {
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    padding: 20px;
  }
  
  .el-card__body {
    padding: 20px;
  }
}
</style> 