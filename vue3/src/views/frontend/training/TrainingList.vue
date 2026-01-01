<template>
  <div class="training-list-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>宠物训练</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>宠物训练课程</h1>
        <p>专业宠物训练课程，助您的爱宠养成良好习惯</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="pet-list-content">
      <div class="filter-section">
        <div class="filter-card">
          <div class="filter-header">
            <h3>寻找课程</h3>
            <div class="filter-icon"><el-icon><Search /></el-icon></div>
          </div>
          
          <el-form :model="searchForm" label-width="70px" class="search-form">
            <el-form-item label="名称">
              <el-input 
                v-model="searchForm.name" 
                placeholder="请输入课程名称" 
                clearable 
                @keyup.enter="handleSearch" 
                class="custom-input" />
            </el-form-item>
            
            <el-form-item label="分类">
              <el-select 
                v-model="searchForm.categoryId" 
                placeholder="请选择课程分类" 
                clearable 
                class="custom-select">
                <el-option 
                  v-for="category in categories" 
                  :key="category.id" 
                  :label="category.name" 
                  :value="category.id" 
                />
              </el-select>
            </el-form-item>
            
            <div class="filter-actions">
              <el-button class="search-btn" @click="handleSearch">查询</el-button>
              <el-button class="reset-btn" @click="resetSearch">重置</el-button>
            </div>
          </el-form>
          
          <div class="filter-decoration">
            <div class="decoration-bubble bubble-1"></div>
            <div class="decoration-bubble bubble-2"></div>
          </div>
        </div>

        <div class="filter-tip">
          <div class="tip-icon"><el-icon><Bulb /></el-icon></div>
          <div class="tip-text">我们的课程由专业宠物训练师精心设计</div>
        </div>
        
        <div class="category-tags" v-if="categories.length > 0">
          <h4 class="tags-title">热门分类</h4>
          <div class="tags-container">
            <el-tag 
              v-for="category in categories.slice(0, 8)" 
              :key="category.id"
              :type="searchForm.categoryId === category.id ? 'warning' : 'info'"
              effect="light"
              class="category-tag"
              @click="selectCategory(category.id)"
            >
              {{ category.name }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <div class="list-section">
        <h2 class="section-title">
          <span class="title-icon">🐾</span>
          训练课程
          <span class="title-count">(共 {{ total }} 个)</span>
        </h2>
        
        <div class="course-list" v-loading="loading">
          <div v-if="!loading && courses.length > 0" class="course-grid">
            <div
              v-for="course in courses"
              :key="course.id"
              class="course-card"
              @click="viewCourseDetail(course.id)"
            >
              <div class="course-category-tag">{{ course.category }}</div>
              <div class="course-content">
                <h3 class="course-name">{{ course.name }}</h3>
                <div class="course-info">
                  <div class="course-meta">
                    <div class="meta-item">
                      <el-icon class="icon"><Clock /></el-icon>
                      <span>{{ course.duration }}分钟</span>
                    </div>
                    <div class="meta-item">
                      <el-icon class="icon"><User /></el-icon>
                      <span>最多{{ course.maxParticipants || 5 }}人</span>
                    </div>
                  </div>
                  <div class="course-description">{{ formatDescription(course.description) }}</div>
                  <div class="course-price">¥{{ course.price }}</div>
                </div>
                <div class="course-action">
                  <el-button type="primary" class="detail-btn" @click.stop="viewCourseDetail(course.id)">
                    <el-icon><InfoFilled /></el-icon>
                    查看详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <el-empty
            v-else-if="!loading && courses.length === 0"
            description="暂无可用的训练课程"
            :image-size="200"
          >
            <template #description>
              <p>目前没有匹配的训练课程</p>
              <p>请调整筛选条件或稍后再试</p>
            </template>
          </el-empty>
        </div>

        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[8, 16, 24, 32]"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Clock, User, InfoFilled, Bulb } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

// 数据定义
const loading = ref(false)
const courses = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(8)
const categories = ref([])

// 搜索表单
const searchForm = reactive({
  name: '',
  categoryId: ''
})

// 获取课程列表
const fetchCourses = async () => {
  loading.value = true
  try {
    const params = {
      name: searchForm.name,
      categoryId: searchForm.categoryId,
      currentPage: currentPage.value,
      size: pageSize.value
    }

    await request.get('/training/course/page', params, {
      onSuccess: (res) => {
        courses.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取所有课程分类
const fetchCategories = async () => {
  try {
    await request.get('/training/category/enabled', null, {
      onSuccess: (res) => {
        categories.value = res || []
      }
    })
  } catch (error) {
    console.error('获取课程分类失败:', error)
  }
}

// 格式化课程描述
const formatDescription = (description) => {
  if (!description) return '暂无描述'
  return description.length > 50 ? description.substring(0, 50) + '...' : description
}

// 处理搜索事件
const handleSearch = () => {
  currentPage.value = 1
  fetchCourses()
}

// 重置搜索条件
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchCourses()
}

// 选择分类
const selectCategory = (categoryId) => {
  if (searchForm.categoryId === categoryId) {
    searchForm.categoryId = ''; // 如果点击的是已选中的分类，取消选择
  } else {
    searchForm.categoryId = categoryId; // 否则选择该分类
  }
  handleSearch();
}

// 处理分页大小改变
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchCourses()
}

// 处理当前页改变
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchCourses()
}

// 查看课程详情
const viewCourseDetail = (id) => {
  router.push({ name: 'TrainingDetail', params: { id } })
}

// 页面加载时获取数据
onMounted(() => {
  fetchCategories()
  fetchCourses()
})
</script>

<style lang="scss" scoped>
.training-list-page {
  min-height: 100vh;
  position: relative;
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

.pet-list-content {
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
    border: 1px solid rgba(102, 187, 106, 0.3);
    padding: 25px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    position: relative;
    overflow: hidden;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
      border-color: rgba(102, 187, 106, 0.5);
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
      background: #e8f5e9;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      animation: pulse 2s infinite, float 4s ease-in-out infinite;
      box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
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
          border: 1px solid rgba(139, 195, 74, 0.3);
          
          &.is-focus {
            box-shadow: 0 0 0 2px rgba(139, 195, 74, 0.5);
            border-color: rgba(139, 195, 74, 0.5);
          }
          
          :deep(.el-input__inner) {
            color: #2E7D32;
            
            &::placeholder {
              color: rgba(46, 125, 50, 0.5);
            }
          }
          
          :deep(.el-input__suffix-inner) {
            color: #8BC34A;
          }
        }
    }
    
    .custom-select {
      width: 100%;
      
      :deep(.el-input__wrapper) {
          border-radius: 12px;
          transition: all 0.3s ease;
          background: rgba(255, 255, 255, 0.9);
          border: 1px solid rgba(139, 195, 74, 0.3);
          
          &.is-focus {
            box-shadow: 0 0 0 2px rgba(139, 195, 74, 0.5);
            border-color: rgba(139, 195, 74, 0.5);
          }
        }
        
        :deep(.el-select__input) {
          color: #2E7D32;
        }
        
        :deep(.el-select__placeholder) {
          color: rgba(46, 125, 50, 0.5);
        }
        
        :deep(.el-select__suffix-inner) {
          color: #8BC34A;
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
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
      border: none;
      color: white;
      box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
      
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
        box-shadow: 0 8px 25px rgba(102, 187, 106, 0.4);
      }
      
      &:hover::before {
        left: 100%;
      }
      
      &:active {
        transform: translateY(-1px);
      }
    }
    
    .reset-btn {
      background: rgba(255, 255, 255, 0.9);
      border: 1px solid rgba(102, 187, 106, 0.3);
      color: #1a202c;
      
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
        background: rgba(102, 187, 106, 0.1);
        border-color: rgba(102, 187, 106, 0.5);
        transform: translateY(-3px);
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
  
  .filter-decoration {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    pointer-events: none;
    overflow: hidden;
    
    .decoration-bubble {
      position: absolute;
      border-radius: 50%;
      background: #FFB6C1;
      opacity: 0.1;
      
      &.bubble-1 {
        width: 100px;
        height: 100px;
        bottom: -30px;
        right: -30px;
      }
      
      &.bubble-2 {
        width: 60px;
        height: 60px;
        top: 20px;
        left: -20px;
      }
    }
  }
}

.filter-tip {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-left: 3px solid #66bb6a;
  padding: 15px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  
  .tip-icon {
    font-size: 20px;
    margin-right: 12px;
    color: #66bb6a;
  }
  
  .tip-text {
    font-size: 14px;
    color: #1a202c;
    line-height: 1.5;
  }
}

.category-tags {
  margin-top: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(102, 187, 106, 0.3);
  
  .tags-title {
    font-size: 18px;
    color: #1a202c;
    margin-bottom: 15px;
    font-weight: 600;
  }
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .category-tag {
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
  }
}

.list-section {
  flex-grow: 1;
  
  .section-title {
      display: flex;
      align-items: center;
      font-family: 'Nunito Sans', sans-serif;
      font-size: 32px;
      color: #1a202c;
      margin-bottom: 30px;
      animation: fadeInDown 0.8s ease;
      
      .title-icon {
        margin-right: 15px;
        font-size: 36px;
        color: #66bb6a;
      }
      
      .title-count {
        margin-left: 15px;
        font-size: 18px;
        opacity: 0.8;
        color: #718096;
      }
    }
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.course-list {
  margin-bottom: 30px;
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.course-card {
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    overflow: hidden;
    position: relative;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
    }
    
    .course-category-tag {
        position: absolute;
        top: 15px;
        right: 15px;
        background-color: #66bb6a;
        color: white;
        padding: 3px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
        z-index: 2;
      }
  
  .course-content {
    padding: 25px;
  }
  
  .course-name {
      margin-top: 5px;
      margin-bottom: 15px;
      font-size: 18px;
      font-weight: 600;
      color: #1a202c;
      line-height: 1.4;
    }
  
  .course-info {
    margin-bottom: 15px;
  }
  
  .course-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 15px;
    
    .meta-item {
          display: flex;
          align-items: center;
          gap: 6px;
          color: #718096;
          font-size: 14px;
          
          .icon {
            color: #66bb6a;
          }
        }
  }
  
  .course-description {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 15px;
    min-height: 65px;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .course-price {
      color: #66bb6a;
      font-weight: bold;
      font-size: 20px;
      margin-top: 10px;
      
      &::before {
        content: '¥';
        font-size: 16px;
      }
    }
  
  .course-action {
      display: flex;
      justify-content: flex-end;
      
      .detail-btn {
        background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
        color: white;
        border: none;
        border-radius: 12px;
        padding: 8px 16px;
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
        
        &:hover {
          background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
          transform: translateY(-3px);
          box-shadow: 0 8px 25px rgba(102, 187, 106, 0.4);
        }
        
        .el-icon {
          margin-right: 5px;
        }
      }
    }
}

.pagination-container {
  margin-top: 50px;
  display: flex;
  justify-content: center;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: rgba(255, 255, 255, 0.9);
    --el-pagination-hover-color: #66bb6a;
    --el-pagination-border-color: rgba(102, 187, 106, 0.3);
    
    .el-pagination__jump,
    .el-pagination__total {
      color: #1a202c;
    }
    
    button:not(:disabled):hover {
      color: #66bb6a;
      background: rgba(102, 187, 106, 0.1);
    }
    
    .el-pager li.is-active {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
      color: white !important;
      border-color: transparent;
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

/* 响应式布局 */
@media (max-width: 1200px) {
  .course-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .pet-list-content {
    flex-direction: column;
  }
  
  .filter-section {
    width: 100%;
  }
  
  .filter-card {
    margin-bottom: 25px;
  }
  
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .section-title {
    font-size: 28px;
  }
}

@media (max-width: 768px) {
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
  
  .pet-list-content {
    padding: 0 15px;
  }
  
  .filter-card {
    padding: 20px;
  }
  
  .course-grid {
    gap: 20px;
  }
  
  .section-title {
    font-size: 24px;
  }
}

@media (max-width: 576px) {
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
  
  .course-grid {
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
}


</style> 