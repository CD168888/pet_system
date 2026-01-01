<template>
  <div class="training-detail-page">
  <!-- 添加Page Banner -->
  <div class="page-banner">
    <div class="container">
      <div class="breadcrumb">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
          <el-breadcrumb-item @click="$router.push('/training')">宠物训练</el-breadcrumb-item>
          <el-breadcrumb-item>{{ course?.name || '训练课程详情' }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <h1>{{ course?.name || '训练课程详情' }}</h1>
      <p>{{ course?.category || '训练课程详情页' }}</p>
    </div>
    <div class="banner-decoration">
      <div class="decoration-paw paw-1">🐾</div>
      <div class="decoration-paw paw-2">🐾</div>
      <div class="decoration-paw paw-3">🐾</div>
    </div>
  </div>
  
  <div class="training-content" v-loading="loading">
      
      <div v-if="!loading && course" class="course-detail-container">
        <!-- 课程头部信息 -->
        <!-- <div class="course-header-section"></div> -->
          
        <!-- 主内容区域 -->
        <div class="course-content-wrapper">
          <!-- 左侧：课程属性和特点 -->
          <div class="course-left-panel">
            <!-- 课程属性 -->
            <el-descriptions :column="2" border class="course-attributes">
              <el-descriptions-item label="课程时长">
                <div class="attribute-value">
                  <el-icon><Clock /></el-icon>
                  <span>{{ course.duration }}分钟</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="课程价格" class="price">
                <div class="attribute-value price">
                  <span>¥{{ course.price }}</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="课程状态">
                <div class="attribute-value">
                  <el-tag :type="course.status === 1 ? 'success' : 'danger'" size="small">
                    {{ course.status === 1 ? '可预约' : '停用' }}
                  </el-tag>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="最大参与人数">
                <div class="attribute-value">
                  <el-icon><User /></el-icon>
                  <span>{{ course.maxParticipants }}人</span>
                </div>
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 课程特点 -->
            <div class="section-block features-section">
              <h3 class="section-title">
                <div class="title-icon"><el-icon><Star /></el-icon></div>
                <span>课程特点</span>
              </h3>
              <div class="features-grid">
                <div class="feature-card">
                  <el-icon size="32" color="#FFA726"><Trophy /></el-icon>
                  <h4>专业训练师</h4>
                  <p>多年训练经验，为您的爱宠提供专业指导</p>
                </div>
                <div class="feature-card">
                  <el-icon size="32" color="#67C23A"><DataAnalysis /></el-icon>
                  <h4>进度追踪</h4>
                  <p>实时记录宠物训练进度，查看训练效果</p>
                </div>
                <div class="feature-card">
                  <el-icon size="32" color="#F56C6C"><Guide /></el-icon>
                  <h4>一对一指导</h4>
                  <p>根据宠物特点定制训练计划，提供个性化辅导</p>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 右侧：课程介绍和流程 -->
          <div class="course-right-panel">
            <!-- 课程介绍 -->
            <div class="section-block description-section">
              <h3 class="section-title">
                <div class="title-icon"><el-icon><InfoFilled /></el-icon></div>
                <span>课程介绍</span>
              </h3>
              <div class="description-content">
                <p>{{ course.description || '暂无详细介绍' }}</p>
              </div>
            </div>
            
            <!-- 服务流程 -->
            <div class="section-block process-section">
              <h3 class="section-title">
                <div class="title-icon"><el-icon><Connection /></el-icon></div>
                <span>服务流程</span>
              </h3>
              <div class="process-steps">
                <div class="custom-steps">
                  <div class="custom-step">
                    <div class="custom-step-head success">
                      <el-icon><Calendar /></el-icon>
                    </div>
                    <div class="custom-step-title success">在线预约</div>
                  </div>
                  <div class="custom-step-line success"></div>
                  <div class="custom-step">
                    <div class="custom-step-head success">
                      <el-icon><Check /></el-icon>
                    </div>
                    <div class="custom-step-title success">确认预约</div>
                  </div>
                  <div class="custom-step-line success"></div>
                  <div class="custom-step">
                    <div class="custom-step-head success">
                      <el-icon><Connection /></el-icon>
                    </div>
                    <div class="custom-step-title success">宠物训练</div>
                  </div>
                  <div class="custom-step-line success"></div>
                  <div class="custom-step">
                    <div class="custom-step-head success">
                      <el-icon><Comment /></el-icon>
                    </div>
                    <div class="custom-step-title success">训练反馈</div>
                  </div>
                  <div class="custom-step-line success"></div>
                  <div class="custom-step">
                    <div class="custom-step-head success">
                      <el-icon><Medal /></el-icon>
                    </div>
                    <div class="custom-step-title success">训练完成</div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 预约按钮 -->
            <div class="booking-action" v-if="course.status === 1">
              <el-button type="primary" class="booking-btn" @click="bookCourse" :disabled="course.status !== 1">
                <el-icon><Calendar /></el-icon>
                立即预约
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else-if="!loading" description="课程不存在或已下架"
        :image-size="200">
        <template #description>
          <p>抱歉，该课程当前不可用</p>
          <p>您可以浏览其他训练课程</p>
        </template>
        <el-button type="primary" @click="goBack">返回课程列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowLeft, 
  Calendar, 
  Check, 
  Connection, 
  Comment, 
  Medal,
  Trophy,
  DataAnalysis,
  Guide,
  Clock,
  InfoFilled,
  Star,
  User
} from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const courseId = route.params.id

// 数据定义
const loading = ref(false)
const course = ref(null)

// 获取课程详情
const fetchCourseDetail = async () => {
  loading.value = true
  try {
    await request.get(`/training/course/${courseId}`, null, {
      onSuccess: (res) => {
        course.value = res
      }
    })
  } catch (error) {
    console.error('获取课程详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 预约课程
const bookCourse = () => {
  router.push({ name: 'TrainingBooking', params: { id: courseId } })
}

// 返回上一页
const goBack = () => {
  router.push({ name: 'TrainingList' })
}

// 页面加载时获取数据
onMounted(() => {
  fetchCourseDetail()
})
</script>

<style lang="scss" scoped>
.training-detail-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

// Page Banner样式
.page-banner {
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
  color: white;
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
    z-index: 2;
  }
  
  .breadcrumb {
    margin-bottom: 20px;
    justify-content: center;
    
    :deep(.el-breadcrumb__item) {
      
      :deep(.el-breadcrumb__inner) {
        color: rgba(255, 255, 255, 0.8);
        
        &.is-link {
          cursor: pointer;
          
          &:hover {
            color: white;
            text-decoration: underline;
          }
        }
      }
      
      :deep(.el-breadcrumb__separator) {
        color: rgba(255, 255, 255, 0.6);
        margin: 0 8px;
      }
    }
  }
  
  h1 {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 42px;
    font-weight: 700;
    margin: 0 0 15px;
    line-height: 1.2;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    animation: fadeInDown 0.8s ease;
  }
  
  p {
    font-size: 20px;
    margin: 0;
    opacity: 0.9;
    animation: fadeInUp 0.8s ease;
  }
  
  .banner-decoration {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    pointer-events: none;
    
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
      background: linear-gradient(135deg, #8BC34A 0%, #4CAF50 100%);
      opacity: 0.05;
    
    &.bubble-1 {
      width: 300px;
      height: 300px;
      top: -150px;
      left: -100px;
      animation: float 15s infinite ease-in-out;
    }
    
    &.bubble-2 {
      width: 200px;
      height: 200px;
      bottom: 10%;
      right: -50px;
      animation: float 18s infinite ease-in-out;
    }
    
    &.bubble-3 {
      width: 150px;
      height: 150px;
      top: 40%;
      right: 10%;
      animation: float 12s infinite ease-in-out;
    }
  }
  
  .decoration-paw {
    position: absolute;
    font-size: 40px;
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

.training-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto 60px;
  padding: 0 20px;
}

.course-detail-container {
  background-color: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.course-header-section {
  margin-bottom: 20px;
  border-bottom: 2px solid #f5f5f5;
  padding-bottom: 20px;
}



.course-category-badge {
  display: inline-block;
  margin-top: 15px;
  padding: 6px 16px;
  background-color: rgba(139, 195, 74, 0.25);
  color: #4CAF50;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.course-content-wrapper {
  display: grid;
  grid-template-columns: minmax(350px, 45%) 1fr;
  gap: 40px;
  align-items: start;
}

/* 左侧面板 */
.course-left-panel {
  background-color: #fafafa;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.course-attributes {
  margin-bottom: 35px;
  
  :deep(.el-descriptions__table) {
    width: 100%;
  }
  
  :deep(.el-descriptions__label) {
    width: 120px;
    color: #666;
    font-weight: 600;
    padding: 12px 15px;
    background-color: rgba(0, 0, 0, 0.02);
    border-right: 1px solid #f0f0f0;
  }
  
  :deep(.el-descriptions__content) {
    color: #333;
    padding: 12px 15px;
  }
  
  .attribute-value {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .el-icon {
      color: #8BC34A;
      font-size: 20px;
    }
    
    &.price {
      color: #4CAF50;
      font-weight: 700;
      font-size: 24px;
      text-shadow: 0 1px 2px rgba(76, 175, 80, 0.1);
    }
  }
}

.section-block {
  margin-bottom: 40px;
  
  .section-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 22px;
    color: #2E7D32;
    margin: 0 0 25px;
    display: flex;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 2px solid #f0f0f0;
    animation: fadeInDown 0.8s ease;
    
    .title-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      background-color: rgba(139, 195, 74, 0.15);
      border-radius: 50%;
      margin-right: 15px;
      
      .el-icon {
        color: #8BC34A;
        font-size: 22px;
      }
    }
  }
}

/* 右侧面板 */
.course-right-panel {
  background-color: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.description-content {
  background-color: #fefefe;
  padding: 25px;
  border-radius: 12px;
  line-height: 1.8;
  color: #555;
  font-size: 15px;
  border: 1px solid #f5f5f5;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 25px;
  margin-top: 10px;
}

.feature-card {
  background-color: white;
  padding: 25px;
  text-align: center;
  border-radius: 16px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f5f5f5;
  
  &:hover {
    transform: translateY(-10px);
    box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
    border-color: #C8E6C9;
  }
  
  .el-icon {
    margin-bottom: 15px;
  }
  
  h4 {
    margin: 0 0 12px;
    font-size: 20px;
    color: #2E7D32;
    font-weight: 600;
  }
  
  p {
    color: #666;
    font-size: 14px;
    line-height: 1.7;
    margin: 0;
  }
}

.process-steps {
    margin-top: 35px;
    padding: 20px 0;
    
    .custom-steps {
      display: flex;
      align-items: center;
      width: 100%;
      margin: 0 auto;
      position: relative;
    }
    
    .custom-step {
      display: flex;
      flex-direction: column;
      align-items: center;
      position: relative;
      z-index: 1;
    }
    
    .custom-step + .custom-step-line + .custom-step {
      margin-left: 0;
    }
    
    .custom-step-head {
      width: 50px;
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 3px solid #e0e0e0;
      border-radius: 50%;
      background-color: white;
      font-size: 24px;
      color: #999;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }
    
    .custom-step-head.success {
      border-color: #8BC34A;
      color: #8BC34A;
      box-shadow: 0 4px 15px rgba(139, 195, 74, 0.2);
    }
    
    .custom-step-title {
      margin-top: 10px;
      font-size: 15px;
      color: #666;
      font-weight: 500;
      text-align: center;
    }
    
    .custom-step-title.success {
      color: #8BC34A;
      font-weight: 600;
    }
    
    .custom-step-line {
      flex: 1;
      height: 4px;
      background-color: #e0e0e0;
      margin: 0;
      position: relative;
      align-self: center;
    }
    
    .custom-step-line.success {
      background-color: #8BC34A;
    }
    
    .custom-step-line::after {
      content: '';
      position: absolute;
      right: 0;
      top: 50%;
      transform: translate(50%, -50%);
      width: 0;
      height: 0;
      border-style: solid;
      border-width: 6px 0 6px 10px;
      border-color: transparent transparent transparent #e0e0e0;
    }
    
    .custom-step-line.success::after {
      border-color: transparent transparent transparent #8BC34A;
    }
    
    /* 最后一个步骤的连接线不需要箭头 */
    .custom-steps .custom-step-line:last-child {
      display: none;
    }
  }

.booking-action {
  display: flex;
  justify-content: center;
  margin-top: 45px;
  padding-top: 30px;
  border-top: 2px solid #f5f5f5;
  
  .booking-btn {
    min-width: 240px;
    height: 56px;
    background: linear-gradient(135deg, #8BC34A 0%, #4CAF50 100%);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 18px;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    box-shadow: 0 6px 20px rgba(139, 195, 74, 0.25);
    
    &:hover:not(:disabled) {
      transform: translateY(-6px);
      background: linear-gradient(135deg, #4CAF50 0%, #388E3C 100%);
      box-shadow: 0 12px 35px rgba(139, 195, 74, 0.4);
    }
    
    &:disabled {
      background: linear-gradient(135deg, #f0f0f0 0%, #e0e0e0 100%);
      color: #999;
      box-shadow: none;
      transform: none;
    }
    
    .el-icon {
      margin-right: 10px;
      font-size: 20px;
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

// 响应式调整
@media (max-width: 1200px) {
  .training-content {
    padding: 0 15px;
    margin: -30px auto 40px;
  }
}

@media (max-width: 992px) {
  .course-content-wrapper {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .page-banner {
    padding: 50px 0 70px;
  }
  
  .page-banner h1 {
    font-size: 36px;
  }
}

@media (max-width: 768px) {
  .page-banner {
    padding: 40px 0 60px;
  }
  
  .page-banner h1 {
    font-size: 28px;
  }
  
  .page-banner p {
    font-size: 16px;
  }
  
  .training-content {
    margin: -25px auto 30px;
    padding: 0 15px;
  }
  
  .course-detail-container {
    padding: 25px;
    border-radius: 12px;
  }
  
  .course-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    
    .course-name {
      font-size: 26px;
    }
    
    .status-tag {
      align-self: flex-start;
    }
  }
  
  .course-left-panel,
  .course-right-panel {
    padding: 20px;
  }
  
  .course-attributes :deep(.el-descriptions__label) {
    width: 100px;
    font-size: 14px;
  }
  
  .attribute-value.price {
    font-size: 20px !important;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .feature-card {
    padding: 20px;
  }
  
  .process-steps :deep(.el-step__title) {
    font-size: 12px;
  }
  
  .process-steps :deep(.el-step__head) {
    width: 40px;
    height: 40px;
    line-height: 40px;
    font-size: 20px;
  }
  
  .booking-action {
    margin-top: 35px;
    padding-top: 25px;
    
    .booking-btn {
      width: 100%;
      min-width: unset;
      height: 50px;
      font-size: 16px;
    }
  }
}

@media (max-width: 480px) {
  .page-banner {
    padding: 30px 0 50px;
  }
  
  .page-banner h1 {
    font-size: 24px;
  }
  
  .page-banner .breadcrumb {
    display: none;
  }
  
  .course-detail-container {
    padding: 20px;
  }
  
  .course-header .course-name {
    font-size: 22px;
  }
  
  .course-attributes :deep(.el-descriptions__item) {
    display: block;
    width: 100%;
  }
  
  .course-attributes :deep(.el-descriptions__label) {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #f0f0f0;
  }
}
</style> 