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
            <el-breadcrumb-item>搜索结果</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>搜索结果：{{ keyword }}</h1>
        <p>为您找到相关内容</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="pet-list-content">
      <!-- 右侧搜索结果列表 -->
      <div class="list-section">
        <!-- 分类书签 -->
        <div class="category-tabs">
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="全部" name="all"></el-tab-pane>
            <el-tab-pane label="宠物" name="pets"></el-tab-pane>
            <el-tab-pane label="服务" name="services"></el-tab-pane>
            <el-tab-pane label="训练课程" name="training"></el-tab-pane>
          </el-tabs>
        </div>
        <!-- 全部结果 -->
        <template v-if="activeTab === 'all'">
          <!-- 宠物搜索结果 -->
          <template v-if="searchPets && petResults.records && petResults.records.length > 0">
            <div class="result-section">
              <div class="service-grid">
                <div v-for="pet in petResults.records" :key="pet.id" class="service-item">
                  <router-link :to="`/pet/${pet.id}`" class="service-link">
                    <div class="service-card">
                      <div class="service-card-content">
                        <!-- 类型标签 -->
                        <div class="content-type-tag">
                          <el-tag type="success" size="small" class="type-tag">宠物</el-tag>
                        </div>
                        <div class="pet-image-container">
                          <el-image 
                            :src="getImageUrl(pet.images)" 
                            fit="cover"
                            :preview-src-list="getImageUrlList(pet.images)"
                            class="pet-image"
                          >
                            <template #error>
                              <div class="image-placeholder">
                                <el-icon><Picture /></el-icon>
                              </div>
                            </template>
                          </el-image>
                        </div>
                        <div class="service-info">
                          <h3 class="service-name">{{ pet.name }}</h3>
                          <div class="pet-meta">
                            <div class="pet-breed">{{ pet.breed }}</div>
                            <div class="pet-details">
                              <span class="pet-age">{{ pet.age }}岁</span>
                              <span class="pet-gender">{{ pet.gender }}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </router-link>
                </div>
              </div>
              <div class="pagination-container" v-if="petResults.total > 10">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="petResults.total"
                  :page-size="10"
                  :current-page="currentPetPage"
                  @current-change="handlePetPageChange"
                />
              </div>
            </div>
          </template>
          
          <!-- 服务搜索结果 -->
          <template v-if="searchServices && serviceResults.records && serviceResults.records.length > 0">
            <div class="result-section">
              <div class="service-grid">
                <div v-for="service in serviceResults.records" :key="service.id" class="service-item">
                  <router-link :to="`/service/${service.id}`" class="service-link">
                    <div class="service-card">
                      <div class="service-card-content">
                        <!-- 类型标签 -->
                        <div class="content-type-tag">
                          <el-tag type="primary" size="small" class="type-tag">服务</el-tag>
                        </div>
                        <div class="service-info">
                          <h3 class="service-name">{{ service.name }}</h3>
                          <div class="service-meta">
                            <div class="service-price">¥{{ service.price }}</div>
                            <div class="service-duration">
                              <el-icon><Timer /></el-icon>
                              {{ service.duration }}分钟
                            </div>
                          </div>
                          <div class="service-description">{{ service.description }}</div>
                        </div>
                      </div>
                    </div>
                  </router-link>
                </div>
              </div>
              <div class="pagination-container" v-if="serviceResults.total > 10">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="serviceResults.total"
                  :page-size="10"
                  :current-page="currentServicePage"
                  @current-change="handleServicePageChange"
                />
              </div>
            </div>
          </template>
          
          <!-- 训练课程搜索结果 -->
          <template v-if="searchTrainingCourses && trainingResults.records && trainingResults.records.length > 0">
            <div class="result-section">
              <div class="service-grid">
                <div v-for="course in trainingResults.records" :key="course.id" class="service-item">
                  <router-link :to="`/training/${course.id}`" class="service-link">
                    <div class="service-card">
                      <div class="service-card-content">
                        <!-- 类型标签 -->
                        <div class="content-type-tag">
                          <el-tag type="warning" size="small" class="type-tag">训练课程</el-tag>
                        </div>
                        <div class="service-info">
                          <h3 class="service-name">{{ course.name }}</h3>
                          <div class="training-category">
                            <el-tag size="small" type="warning">{{ course.category }}</el-tag>
                          </div>
                          <div class="service-meta">
                            <div class="service-price">¥{{ course.price }}</div>
                            <div class="service-duration">
                              <el-icon><Timer /></el-icon>
                              {{ course.duration }}分钟
                            </div>
                          </div>
                          <div class="service-description">{{ course.description }}</div>
                        </div>
                      </div>
                    </div>
                  </router-link>
                </div>
              </div>
              <div class="pagination-container" v-if="trainingResults.total > 10">
                <el-pagination
                  background
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="trainingResults.total"
                  :page-size="10"
                  :current-page="currentTrainingPage"
                  @current-change="handleTrainingPageChange"
                />
              </div>
            </div>
          </template>
        </template>
        
        <!-- 宠物结果 -->
        <template v-else-if="activeTab === 'pets'">
          <template v-if="petResults.records && petResults.records.length > 0">
            <div class="service-grid">
              <div v-for="pet in petResults.records" :key="pet.id" class="service-item">
                <router-link :to="`/pet/${pet.id}`" class="service-link">
                  <div class="service-card">
                    <div class="service-card-content">
                      <!-- 类型标签 -->
                      <div class="content-type-tag">
                        <el-tag type="success" size="small" class="type-tag">宠物</el-tag>
                      </div>
                      <div class="pet-image-container">
                        <el-image 
                          :src="getImageUrl(pet.images)" 
                          fit="cover"
                          :preview-src-list="getImageUrlList(pet.images)"
                          class="pet-image"
                        >
                          <template #error>
                            <div class="image-placeholder">
                              <el-icon><Picture /></el-icon>
                            </div>
                          </template>
                        </el-image>
                      </div>
                      <div class="service-info">
                        <h3 class="service-name">{{ pet.name }}</h3>
                        <div class="pet-meta">
                          <div class="pet-breed">{{ pet.breed }}</div>
                          <div class="pet-details">
                            <span class="pet-age">{{ pet.age }}岁</span>
                            <span class="pet-gender">{{ pet.gender }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </div>
            <div class="pagination-container" v-if="petResults.total > 10">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="petResults.total"
                :page-size="10"
                :current-page="currentPetPage"
                @current-change="handlePetPageChange"
              />
            </div>
          </template>
        </template>
        
        <!-- 服务结果 -->
        <template v-else-if="activeTab === 'services'">
          <template v-if="serviceResults.records && serviceResults.records.length > 0">
            <div class="service-grid">
              <div v-for="service in serviceResults.records" :key="service.id" class="service-item">
                <router-link :to="`/service/${service.id}`" class="service-link">
                  <div class="service-card">
                    <div class="service-card-content">
                      <!-- 类型标签 -->
                      <div class="content-type-tag">
                        <el-tag type="primary" size="small" class="type-tag">服务</el-tag>
                      </div>
                      <div class="service-info">
                        <h3 class="service-name">{{ service.name }}</h3>
                        <div class="service-meta">
                          <div class="service-price">¥{{ service.price }}</div>
                          <div class="service-duration">
                            <el-icon><Timer /></el-icon>
                            {{ service.duration }}分钟
                          </div>
                        </div>
                        <div class="service-description">{{ service.description }}</div>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </div>
            <div class="pagination-container" v-if="serviceResults.total > 10">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="serviceResults.total"
                :page-size="10"
                :current-page="currentServicePage"
                @current-change="handleServicePageChange"
              />
            </div>
          </template>
        </template>
        
        <!-- 训练课程结果 -->
        <template v-else-if="activeTab === 'training'">
          <template v-if="trainingResults.records && trainingResults.records.length > 0">
            <div class="service-grid">
              <div v-for="course in trainingResults.records" :key="course.id" class="service-item">
                <router-link :to="`/training/${course.id}`" class="service-link">
                  <div class="service-card">
                    <div class="service-card-content">
                      <!-- 类型标签 -->
                      <div class="content-type-tag">
                        <el-tag type="warning" size="small" class="type-tag">训练课程</el-tag>
                      </div>
                      <div class="service-info">
                        <h3 class="service-name">{{ course.name }}</h3>
                        <div class="training-category">
                          <el-tag size="small" type="warning">{{ course.category }}</el-tag>
                        </div>
                        <div class="service-meta">
                          <div class="service-price">¥{{ course.price }}</div>
                          <div class="service-duration">
                            <el-icon><Timer /></el-icon>
                            {{ course.duration }}分钟
                          </div>
                        </div>
                        <div class="service-description">{{ course.description }}</div>
                      </div>
                    </div>
                  </div>
                </router-link>
              </div>
            </div>
            <div class="pagination-container" v-if="trainingResults.total > 10">
              <el-pagination
                background
                layout="total, sizes, prev, pager, next, jumper"
                :total="trainingResults.total"
                :page-size="10"
                :current-page="currentTrainingPage"
                @current-change="handleTrainingPageChange"
              />
            </div>
          </template>
        </template>
        
        <!-- 当前标签没有结果时显示 -->
        <template v-if="
          ((activeTab === 'all' && noResults) ||
           (activeTab === 'pets' && (!petResults.records || petResults.records.length === 0)) ||
           (activeTab === 'services' && (!serviceResults.records || serviceResults.records.length === 0)) ||
           (activeTab === 'training' && (!trainingResults.records || trainingResults.records.length === 0)))">
          <div class="no-results-container">
            <el-empty description="没有找到相关内容" :image-size="200" class="no-results-empty">
              <template #description>
                <p>没有找到与"{{ keyword }}"相关的内容，请尝试其他关键词</p>
              </template>
              <el-button type="primary" @click="goHome" class="home-button">返回首页</el-button>
            </el-empty>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Picture, Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'

// 搜索关键词
const keyword = ref('')

// 搜索选项
const activeTab = ref('all')
const searchPets = ref(true)
const searchServices = ref(true)
const searchTrainingCourses = ref(true)

// 搜索结果
const petResults = ref({ records: [], total: 0 })
const serviceResults = ref({ records: [], total: 0 })
const trainingResults = ref({ records: [], total: 0 })

// 分页
const currentPetPage = ref(1)
const currentServicePage = ref(1)
const currentTrainingPage = ref(1)

// 加载状态
const loading = ref(false)

// 计算是否没有搜索结果
const noResults = computed(() => {
  return (
    (!petResults.value.records || petResults.value.records.length === 0 || !searchPets.value) &&
    (!serviceResults.value.records || serviceResults.value.records.length === 0 || !searchServices.value) &&
    (!trainingResults.value.records || trainingResults.value.records.length === 0 || !searchTrainingCourses.value)
  )
})

// 处理标签点击
const handleTabClick = (tab) => {
  const tabName = tab.props.name
  
  switch (tabName) {
    case 'all':
      searchPets.value = true
      searchServices.value = true
      searchTrainingCourses.value = true
      break
    case 'pets':
      searchPets.value = true
      searchServices.value = false
      searchTrainingCourses.value = false
      break
    case 'services':
      searchPets.value = false
      searchServices.value = true
      searchTrainingCourses.value = false
      break
    case 'training':
      searchPets.value = false
      searchServices.value = false
      searchTrainingCourses.value = true
      break
    default:
      break
  }
  
  // 重置分页并重新搜索
  resetPagination()
  fetchSearchResults()
}

// 监听路由参数变化
watch(() => route.query.q, (newKeyword) => {
  if (newKeyword) {
    keyword.value = newKeyword
    resetPagination()
    fetchSearchResults()
  }
})

// 重置分页
const resetPagination = () => {
  currentPetPage.value = 1
  currentServicePage.value = 1
  currentTrainingPage.value = 1
}

// 获取搜索结果
const fetchSearchResults = async () => {
  if (!keyword.value) return
  
  loading.value = true
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: searchPets.value,
      searchServices: searchServices.value,
      searchTrainingCourses: searchTrainingCourses.value,
      currentPage: 1,
      size: 10
    }
    
    // 使用request工具直接发送请求
    const response = await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.pets) {
          petResults.value = data.pets
        }
        
        if (data.services) {
          serviceResults.value = data.services
        }
        
        if (data.trainingCourses) {
          trainingResults.value = data.trainingCourses
        }
      },
      onError: (error) => {
        console.error('搜索失败', error)
        ElMessage.error('搜索失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('搜索失败', error)
    ElMessage.error('搜索失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 宠物分页变化
const handlePetPageChange = async (page) => {
  currentPetPage.value = page
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: true,
      searchServices: false,
      searchTrainingCourses: false,
      currentPage: page,
      size: 10
    }
    
    // 使用request工具直接发送请求
    await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.pets) {
          petResults.value = data.pets
        }
      },
      onError: (error) => {
        console.error('加载宠物分页失败', error)
        ElMessage.error('加载失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('加载宠物分页失败', error)
    ElMessage.error('加载失败，请稍后再试')
  }
}

// 服务分页变化
const handleServicePageChange = async (page) => {
  currentServicePage.value = page
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: false,
      searchServices: true,
      searchTrainingCourses: false,
      currentPage: page,
      size: 10
    }
    
    // 使用request工具直接发送请求
    await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.services) {
          serviceResults.value = data.services
        }
      },
      onError: (error) => {
        console.error('加载服务分页失败', error)
        ElMessage.error('加载失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('加载服务分页失败', error)
    ElMessage.error('加载失败，请稍后再试')
  }
}

// 训练课程分页变化
const handleTrainingPageChange = async (page) => {
  currentTrainingPage.value = page
  
  try {
    const params = {
      keyword: keyword.value,
      searchPets: false,
      searchServices: false,
      searchTrainingCourses: true,
      currentPage: page,
      size: 10
    }
    
    // 使用request工具直接发送请求
    await request.get('/search', params, {
      onSuccess: (data) => {
        if (data.trainingCourses) {
          trainingResults.value = data.trainingCourses
        }
      },
      onError: (error) => {
        console.error('加载训练课程分页失败', error)
        ElMessage.error('加载失败，请稍后再试')
      }
    })
  } catch (error) {
    console.error('加载训练课程分页失败', error)
    ElMessage.error('加载失败，请稍后再试')
  }
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 页面加载时获取搜索关键词并执行搜索
onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q
    fetchSearchResults()
  }
})

// 获取宠物图片URL
const getImageUrl = (images) => {
  // 使用数据URI作为默认图片
  const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTgwIiBoZWlnaHQ9IjE4MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB4PSIyIiB5PSIyIiB3aWR0aD0iMTc2IiBoZWlnaHQ9IjE3NiIgc3R5bGU9ImZpbGw6I2VlZTtzdHJva2U6I2FhYTtzdHJva2Utd2lkdGg6MnB4OyIvPjx0ZXh0IHg9IjkwIiB5PSI5MCIgZG9taW5hbnQtYmFzZWxpbmU9Im1pZGRsZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgc3R5bGU9ImZpbGw6Izk5OTtmb250LWZhbWlseTpBcmlhbCxzYW5zLXNlcmlmO2ZvbnQtc2l6ZToxOHB4OyI+无图片</dGV4dD48L3N2Zz4='
  
  if (!images) return defaultImage
  const imageList = images.split(',')
  if (imageList.length === 0) return defaultImage
  
  const firstImage = imageList[0].trim()
  if (firstImage.startsWith('http')) {
    return firstImage
  }
  return `${baseAPI}${firstImage}`
}

// 获取宠物图片列表（用于预览）
const getImageUrlList = (images) => {
  if (!images) return []
  
  return images.split(',').map(img => {
    const trimmedImg = img.trim()
    if (trimmedImg.startsWith('http')) {
      return trimmedImg
    }
    return `${baseAPI}${trimmedImg}`
  })
}
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
  padding: 0 20px;
}

/* 右侧列表区域 */
.list-section {
  width: 100%;
}

/* 分类标签样式 */
.category-tabs {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 15px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 187, 106, 0.2);
  margin-bottom: 25px;
  animation: fadeInUp 0.8s ease;
  overflow: visible;
  
  :deep(.el-tabs) {
    width: 100%;
  }
  
  :deep(.el-tabs__header) {
    margin: 0 !important;
    padding: 0 !important;
    width: 100%;
  }
  
  :deep(.el-tabs__nav-wrap) {
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    display: flex !important;
    justify-content: center !important;
    overflow: visible !important;
    
    &::before, &::after {
      display: none;
    }
  }
  
  :deep(.el-tabs__nav-scroll) {
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    display: flex !important;
    justify-content: center !important;
    overflow: visible !important;
  }
  
  :deep(.el-tabs__nav) {
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    width: auto !important;
    margin: 0 auto !important;
    padding: 0 !important;
    float: none !important;
    
    // 重置所有子元素的默认样式
    > * {
      border: none !important;
      outline: none !important;
      box-shadow: none !important;
    }
    
    // 修复第一个和最后一个标签的特殊样式
    > :first-child {
      border-top-left-radius: 15px !important;
      border-bottom-left-radius: 15px !important;
    }
    
    > :last-child {
      border-top-right-radius: 15px !important;
      border-bottom-right-radius: 15px !important;
    }
  }
  
  :deep(.el-tabs__item) {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 18px;
    font-weight: 600;
    color: #6E4C1E;
    padding: 15px 35px !important;
    margin: 0 8px !important;
    border-radius: 25px !important;
    transition: all 0.3s ease;
    background: rgba(102, 187, 106, 0.1);
    float: none !important;
    position: relative;
    z-index: 1;
    overflow: visible !important;
    line-height: 1.5;
    border: none !important;
    outline: none !important;
    min-width: 100px;
    text-align: center;
    box-sizing: border-box;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    
    &:hover {
      color: #66bb6a;
      background: rgba(102, 187, 106, 0.2);
    }
    
    &.is-active {
      color: white !important;
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
      box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
      // 确保选中状态完全覆盖标签
      overflow: visible !important;
      z-index: 2;
      border: none !important;
      outline: none !important;
      min-width: 100px;
    }
    
    // 重置所有可能导致边框的样式
    &::before, &::after {
      display: none !important;
    }
    
    // 确保所有标签样式一致
    &:first-child,
    &:last-child,
    &:nth-child(2),
    &:nth-child(3) {
      border: none !important;
      outline: none !important;
      padding: 15px 35px !important;
      margin: 0 8px !important;
      border-radius: 25px !important;
    }
  }
  
  :deep(.el-tabs__active-bar) {
    background: transparent !important;
    display: none !important;
  }
  
  :deep(.el-tabs__content) {
    margin: 0 !important;
    padding: 0 !important;
  }
}

/* 结果区域样式 */
.result-section {
  margin-bottom: 50px;
  animation: fadeInUp 0.8s ease;
}

.section-title {
  font-family: 'Nunito Sans', sans-serif;
  font-size: 26px;
  color: #683e35;
  margin-bottom: 25px;
  font-weight: 700;
  padding-left: 15px;
  border-left: 5px solid #66bb6a;
  background: linear-gradient(135deg, rgba(102, 187, 106, 0.1), rgba(102, 187, 106, 0.05));
  padding: 15px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(102, 187, 106, 0.1);
  display: inline-block;
}

/* 服务网格布局 */
.service-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
  animation: fadeInUp 0.8s ease 0.2s both;
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 18px;
  }
}

/* 服务项样式 */
.service-item {
  display: flex;
  cursor: pointer;
  animation: fadeInUp 0.8s ease;
}

/* 服务链接样式 */
.service-link {
  text-decoration: none;
  color: inherit;
  flex: 1;
  transition: all 0.3s ease;
}

/* 服务卡片样式 */
.service-card {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.08);
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
    background: linear-gradient(90deg, transparent, rgba(102, 187, 106, 0.15), transparent);
    transition: left 0.5s ease;
  }
  
  &:hover {
    transform: translateY(-15px);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.18);
    border-color: rgba(102, 187, 106, 0.4);
    
    &::before {
      left: 100%;
    }
    
    .service-name {
      color: #66bb6a;
    }
    
    .service-price {
      transform: scale(1.05);
    }
  }
}

.service-card-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  z-index: 1;
}

.service-info {
  flex: 1;
}

/* 类型标签样式 */
.content-type-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  z-index: 2;
}

.type-tag {
  font-weight: 700;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  animation: fadeIn 0.5s ease;
}

/* 服务名称样式 */
.service-name {
  margin: 0 0 18px 0;
  font-family: 'Nunito Sans', sans-serif;
  font-size: 24px;
  color: #6E4C1E;
  font-weight: 700;
  line-height: 1.3;
  transition: color 0.3s ease;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  position: relative;
  z-index: 1;
}

/* 宠物图片样式 */
.pet-image-container {
  width: 100%;
  height: 200px;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
  position: relative;
  background: linear-gradient(135deg, rgba(102, 187, 106, 0.1), rgba(102, 187, 106, 0.05));
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(102, 187, 106, 0.1), transparent 50%);
    z-index: 1;
    pointer-events: none;
  }
  
  .service-card:hover & {
    box-shadow: 0 10px 35px rgba(0, 0, 0, 0.18);
    transform: scale(1.02);
  }
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 0;
  
  .service-card:hover & {
    transform: scale(1.1);
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 187, 106, 0.15), rgba(102, 187, 106, 0.05));
  color: #66bb6a;
  font-size: 2rem;
  position: relative;
  z-index: 1;
  
  .el-icon {
    font-size: 4rem;
    opacity: 0.6;
    animation: pulse 2s infinite;
  }
}

/* 宠物元数据样式 */
.pet-meta {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed rgba(102, 187, 106, 0.2);
}

.pet-breed {
  color: #6E4C1E;
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 12px;
  display: inline-block;
  background: rgba(102, 187, 106, 0.1);
  padding: 6px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  
  .service-card:hover & {
    background: rgba(102, 187, 106, 0.2);
    color: #66bb6a;
  }
}

.pet-details {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  margin-top: 10px;
}

.pet-age, .pet-gender {
  background: rgba(102, 187, 106, 0.15);
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  color: #2e7d32;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(102, 187, 106, 0.15);
  
  .service-card:hover & {
    background: rgba(102, 187, 106, 0.25);
    color: #66bb6a;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.25);
  }
}

/* 服务元数据样式 */
.service-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px dashed rgba(102, 187, 106, 0.3);
  flex-wrap: wrap;
  gap: 15px;
}

.service-price {
  font-size: 28px;
  font-weight: 800;
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
  line-height: 1;
  
  .service-card:hover & {
    transform: scale(1.08);
  }
}

.service-duration {
  display: flex;
  align-items: center;
  color: #2e7d32;
  font-size: 16px;
  font-weight: 600;
  background: rgba(102, 187, 106, 0.15);
  padding: 10px 18px;
  border-radius: 25px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 187, 106, 0.15);
  
  .el-icon {
    margin-right: 8px;
    color: #66bb6a;
    font-size: 18px;
    animation: pulse 2s infinite;
  }
  
  .service-card:hover & {
    background: rgba(102, 187, 106, 0.25);
    color: #66bb6a;
    transform: translateY(-3px);
    box-shadow: 0 6px 20px rgba(102, 187, 106, 0.25);
  }
}

.service-description {
  color: #6E4C1E;
  font-size: 15px;
  line-height: 1.8;
  opacity: 0.85;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  margin-bottom: 25px;
  transition: all 0.3s ease;
  padding: 0 2px;
  
  .service-card:hover & {
    opacity: 1;
    color: #5a3d1a;
  }
}

/* 训练课程分类 */
.training-category {
  margin: 15px 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 训练课程标签样式 */
.training-category .el-tag {
  background: linear-gradient(135deg, #ffb74d 0%, #ffa726 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(255, 183, 77, 0.3);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(255, 183, 77, 0.4);
  }
}

/* 分页容器 */
.pagination-container {
  margin-top: 40px;
  padding: 30px;
  display: flex;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 187, 106, 0.2);
  animation: fadeInUp 0.8s ease 0.4s both;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: transparent;
    --el-pagination-hover-color: #66bb6a;
    --el-pagination-text-color: #6E4C1E;
    --el-pagination-disabled-color: #ccc;
    --el-pagination-border-radius: 12px;
    
    .el-pagination__jump {
      color: #6E4C1E;
      font-weight: 600;
    }
    
    button:not(:disabled) {
      transition: all 0.3s ease;
      border-radius: 12px;
      padding: 8px 15px;
      margin: 0 4px;
      font-weight: 600;
    }
    
    button:not(:disabled):hover {
      color: white !important;
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
      border-color: #66bb6a !important;
      transform: translateY(-2px);
      box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
    }
    
    .is-active {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
      color: white !important;
      border-color: #66bb6a !important;
      box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
      transform: translateY(-2px);
    }
    
    .el-pagination__total {
      color: #6E4C1E;
      font-weight: 600;
      margin-right: 20px;
    }
    
    .el-select .el-input__wrapper {
      border-radius: 12px;
      border: 1px solid rgba(102, 187, 106, 0.2);
      
      &:hover {
        border-color: #66bb6a;
        box-shadow: 0 0 0 2px rgba(102, 187, 106, 0.1);
      }
    }
  }
}

/* 空结果样式 */
.empty-result-section {
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 187, 106, 0.2);
  text-align: center;
  animation: fadeInUp 0.8s ease;
}

.service-empty {
  margin: 0;
  
  :deep(.el-empty__description) {
    color: #999;
    font-size: 18px;
    font-weight: 500;
  }
  
  :deep(.el-empty__image) {
    margin-bottom: 20px;
  }
}

/* 没有结果时的样式 */
.no-results-container {
  padding: 80px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(102, 187, 106, 0.2);
  text-align: center;
  margin-top: 50px;
  animation: fadeInUp 0.8s ease;
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(102, 187, 106, 0.05) 0%, transparent 70%);
    animation: rotate 20s linear infinite;
  }
}

.no-results-empty {
  margin: 0;
  position: relative;
  z-index: 1;
  
  :deep(.el-empty__description) {
    color: #999;
    font-size: 18px;
    margin-bottom: 30px;
    font-weight: 500;
  }
  
  :deep(.el-empty__description p) {
    margin: 12px 0;
    line-height: 1.6;
  }
  
  :deep(.el-empty__image) {
    margin-bottom: 30px;
    animation: pulse 2s infinite ease-in-out;
  }
}

.home-button {
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  color: white;
  border: none;
  padding: 15px 40px;
  border-radius: 15px;
  font-weight: 700;
  font-size: 17px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(102, 187, 106, 0.3);
  font-family: 'Nunito Sans', sans-serif;
  position: relative;
  z-index: 1;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 35px rgba(102, 187, 106, 0.45);
  }
  
  &:active {
    transform: translateY(-2px);
  }
}

/* 响应式调整 */
@media (max-width: 992px) {
  .pet-list-content {
    padding: 0 15px;
  }
  
  .category-tabs {
    padding: 10px;
    
    :deep(.el-tabs__item) {
      font-size: 16px;
      padding: 10px 20px;
      margin: 0 5px;
    }
  }
  
  .list-section {
    margin-top: 15px;
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
  
  .service-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .filter-section {
    padding: 20px;
    margin-bottom: 15px;
  }
  
  .service-name {
    font-size: 19px;
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
</style> 