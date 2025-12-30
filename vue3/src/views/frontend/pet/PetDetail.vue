<template>
  <div class="pet-detail-page" v-loading="loading">
    <!-- 添加Page Banner -->
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item @click="$router.push('/pet')">宠物领养</el-breadcrumb-item>
            <el-breadcrumb-item>{{ pet?.name || '宠物详情' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>{{ pet?.name || '宠物详情' }}</h1>
        <p>{{ pet?.categoryName || '宠物详情页' }}</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <!-- 面包屑已移除 -->
    
    <div class="pet-detail-content" v-if="pet">
      <div class="pet-main-info">
        <div class="pet-gallery">
          <div class="gallery-main">
            <el-carousel 
              ref="carouselRef"
              :interval="4000" 
              height="450px"
              indicator-position="outside"
              arrow="always"
              class="carousel-container"
              :initial-index="activeIndex"
              @change="handleCarouselChange">
              <el-carousel-item v-for="(image, index) in getImageList(pet.images)" :key="index" class="carousel-item">
                <el-image 
                  :src="image" 
                  fit="cover"
                  class="carousel-image"
                  :preview-src-list="getImageList(pet.images)"
                  :preview-teleported="true"
                  :initial-index="index">
                </el-image>
              </el-carousel-item>
            </el-carousel>
          </div>
          
          <div class="gallery-thumbnails" v-if="getImageList(pet.images).length > 1">
            <div 
              v-for="(image, index) in getImageList(pet.images)" 
              :key="index" 
              class="thumbnail-item" 
              :class="{ active: index === activeIndex }"
              @click="setActiveImage(index)">
              <img :src="image" :alt="`${pet.name} - 图片 ${index + 1}`">
            </div>
          </div>
        </div>
        
        <div class="pet-info-card">
          <div class="pet-header">
            <h1 class="pet-name">{{ pet.name }}</h1>
            <div class="pet-status-badge" :class="getStatusClass(pet.adoptionStatus)">
              {{ pet.adoptionStatus }}
            </div>
          </div>
          
          <div class="pet-tags">
            <span class="pet-tag tag-category" v-if="pet.categoryName">{{ pet.categoryName }}</span>
  
            <span class="pet-tag tag-breed">{{ pet.breed }}</span>
          </div>
          
          <div class="pet-attributes">
            <div class="attribute-item">
              <div class="attribute-icon"><el-icon><UserFilled /></el-icon></div>
              <div class="attribute-content">
                <div class="attribute-label">年龄</div>
                <div class="attribute-value">{{ pet.age }} 岁</div>
              </div>
            </div>
            
            <div class="attribute-item">
              <div class="attribute-icon">
                <el-icon v-if="pet.gender === '公'"><Male /></el-icon>
                <el-icon v-else><Female /></el-icon>
              </div>
              <div class="attribute-content">
                <div class="attribute-label">性别</div>
                <div class="attribute-value">{{ pet.gender }}</div>
              </div>
            </div>
            
            <div class="attribute-item">
              <div class="attribute-icon"><el-icon><StarFilled /></el-icon></div>
              <div class="attribute-content">
                <div class="attribute-label">健康状况</div>
                <div class="attribute-value">{{ pet.healthStatus }}</div>
              </div>
            </div>
          </div>
          
          <div class="pet-description-section">
            <h2 class="section-title">宠物介绍</h2>
            <p class="description-text">{{ pet.description }}</p>
          </div>
          
          <div class="pet-actions">
            <el-button v-if="pet.adoptionStatus === '可领养'" 
                      class="adopt-button" 
                      @click="handleAdopt">
              <span class="button-icon"><el-icon><House /></el-icon></span>
              申请领养
            </el-button>
            
            <el-button v-else-if="pet.userApplicationStatus === '已申请' || pet.userApplicationStatus === '审核中'" 
                      class="cancel-button" 
                      @click="handleCancel">
              取消申请
            </el-button>
            
            <el-button v-else-if="pet.userApplicationStatus === '已通过'" 
                      class="success-button" 
                      disabled>
              已通过申请
            </el-button>
            
            <el-button v-else-if="pet.userApplicationStatus === '已拒绝'" 
                      class="rejected-button" 
                      disabled>
              申请被拒绝
            </el-button>
            
            <el-button v-else-if="pet.adoptionStatus === '已领养'" 
                      class="adopted-button" 
                      disabled>
              已被领养
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="pet-details-tabs">
        <el-tabs v-model="activeTab" class="custom-tabs">
          <!-- 健康记录标签页 -->
          <el-tab-pane label="健康记录" name="healthRecord">
            <div v-loading="healthRecordLoading" class="tab-content">
              <div v-if="healthRecordList.length === 0" class="empty-records">
                <el-empty description="暂无健康记录" />
              </div>
              <div v-else class="health-timeline">
                <el-timeline>
                  <el-timeline-item
                    v-for="record in healthRecordList"
                    :key="record.id"
                    :timestamp="formatDate(record.recordDate)"
                    :type="getHealthRecordType(record.recordType)"
                    class="timeline-item"
                  >
                    <el-card class="record-card">
                      <template #header>
                        <div class="record-header">
                          <span class="record-title">{{ record.recordType }}</span>
                          <span class="record-hospital">{{ record.hospital }}</span>
                        </div>
                      </template>
                      <div class="record-content">
                        <div class="record-item" v-if="record.doctor">
                          <strong>医生：</strong> {{ record.doctor }}
                        </div>
                        <div class="record-item-group">
                          <div class="record-item" v-if="record.weight">
                            <strong>体重：</strong> {{ record.weight }}kg
                          </div>
                          <div class="record-item" v-if="record.temperature">
                            <strong>体温：</strong> {{ record.temperature }}℃
                          </div>
                          <div class="record-item" v-if="record.heartRate">
                            <strong>心率：</strong> {{ record.heartRate }}次/分
                          </div>
                        </div>
                        <div class="record-item" v-if="record.diagnosisResults">
                          <strong>诊断结果：</strong> {{ record.diagnosisResults }}
                        </div>
                        <div class="record-item" v-if="record.healthSuggestions">
                          <strong>健康建议：</strong> {{ record.healthSuggestions }}
                        </div>
                        <div class="record-item" v-if="record.notes">
                          <strong>备注：</strong> {{ record.notes }}
                        </div>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
                
                <div class="pagination-container" v-if="healthRecordTotal > healthRecordPageSize">
                  <el-pagination
                    background
                    layout="prev, pager, next"
                    :total="healthRecordTotal"
                    :page-size="healthRecordPageSize"
                    :current-page="healthRecordCurrentPage"
                    @current-change="handleHealthRecordPageChange"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- 疫苗接种标签页 -->
          <el-tab-pane label="疫苗记录" name="vaccination">
            <div v-loading="vaccinationLoading" class="tab-content">
              <div v-if="vaccinationList.length === 0" class="empty-records">
                <el-empty description="暂无疫苗接种记录" />
              </div>
              <div v-else class="vaccination-table">
                <el-table :data="vaccinationList" border style="width: 100%" class="custom-table">
                  <el-table-column prop="vaccinationDate" label="接种日期" width="180" :formatter="formatDateColumn" />
                  <el-table-column prop="vaccineName" label="疫苗名称" width="180" />
                  <el-table-column prop="hospital" label="接种医院" width="180" />
                  <el-table-column prop="nextDate" label="下次接种日期" width="180" :formatter="formatDateColumn" />
                  <el-table-column prop="notes" label="备注" min-width="180" />
                </el-table>
                
                <div class="pagination-container" v-if="vaccinationTotal > vaccinationPageSize">
                  <el-pagination
                    background
                    layout="prev, pager, next"
                    :total="vaccinationTotal"
                    :page-size="vaccinationPageSize"
                    :current-page="vaccinationCurrentPage"
                    @current-change="handleVaccinationPageChange"
                  />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <el-empty v-else-if="!loading" description="宠物不存在或已下架" class="pet-empty"></el-empty>
    
    <!-- 申请领养对话框 -->
    <el-dialog v-model="dialogVisible" title="申请领养" width="500px" class="custom-dialog">
      <div class="dialog-header">
        <div class="dialog-icon"><el-icon><House /></el-icon></div>
        <h3 class="dialog-title">领养申请</h3>
      </div>
      
      <el-form :model="adoptForm" label-width="80px" :rules="adoptRules" ref="adoptFormRef">
        <el-form-item label="申请理由" prop="applyReason">
          <el-input v-model="adoptForm.applyReason" type="textarea" rows="4" placeholder="请详细说明您申请领养的原因、饲养环境等信息"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="adoptForm.contactPhone" placeholder="请输入您的联系电话"></el-input>
        </el-form-item>
        <el-form-item label="联系地址" prop="address">
          <el-input v-model="adoptForm.address" type="textarea" rows="3" placeholder="请输入详细地址"></el-input>
        </el-form-item>
      </el-form>
      
      <div class="dialog-tip">
        <div class="tip-icon"><el-icon><InfoFilled /></el-icon></div>
        <div class="tip-text">提交申请后，我们将在1-3个工作日内进行审核，请保持电话畅通。</div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button class="cancel-btn" @click="dialogVisible = false">取消</el-button>
          <el-button class="submit-btn" @click="submitAdoption" :loading="submitLoading">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { formatDate, formatDateTime } from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'

const petId = computed(() => route.params.id)

// 数据定义
const pet = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const adoptFormRef = ref(null)
const activeTab = ref('healthRecord')
const activeIndex = ref(0)
const carouselRef = ref(null)

// 健康记录相关
const healthRecordLoading = ref(false)
const healthRecordList = ref([])
const healthRecordTotal = ref(0)
const healthRecordCurrentPage = ref(1)
const healthRecordPageSize = ref(5)

// 疫苗记录相关
const vaccinationLoading = ref(false)
const vaccinationList = ref([])
const vaccinationTotal = ref(0)
const vaccinationCurrentPage = ref(1)
const vaccinationPageSize = ref(5)

// 申请表单
const adoptForm = ref({
  petId: null,
  applyReason: '',
  contactPhone: '',
  address: ''
})

// 表单验证规则
const adoptRules = {
  applyReason: [
    { required: true, message: '请填写申请理由', trigger: 'blur' },
    { min: 10, message: '申请理由不能少于10个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请填写联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请填写联系地址', trigger: 'blur' },
    { min: 5, message: '地址不能少于5个字符', trigger: 'blur' }
  ]
}

// 获取宠物详情
const fetchPetDetail = async () => {
  loading.value = true
  try {
    // 如果用户已登录，获取带用户申请状态的详情
    if (userStore.isLoggedIn) {
      await request.get(`/pet/${petId.value}/with-user-status`, {}, {
        onSuccess: (data) => {
          pet.value = data
          adoptForm.value.petId = data.id
          
          // 获取健康记录和疫苗记录
          fetchHealthRecords()
          fetchVaccinations()
        }
      })
    } else {
      // 未登录用户获取普通详情
      await request.get(`/pet/${petId.value}`, {}, {
        onSuccess: (data) => {
          pet.value = data
          adoptForm.value.petId = data.id
          
          // 获取健康记录和疫苗记录
          fetchHealthRecords()
          fetchVaccinations()
        }
      })
    }
  } catch (error) {
    console.error('获取宠物详情失败:', error)
    ElMessage.error('获取宠物详情失败')
  } finally {
    loading.value = false
  }
}

// 获取健康记录列表
const fetchHealthRecords = async () => {
  if (!pet.value) return
  
  healthRecordLoading.value = true
  try {
    await request.get('/pet-health-record/page', {
      petId: pet.value.id,
      currentPage: healthRecordCurrentPage.value,
      size: healthRecordPageSize.value
    }, {
      onSuccess: (res) => {
        healthRecordList.value = res.records
        healthRecordTotal.value = res.total
      }
    })
  } catch (error) {
    console.error('获取健康记录失败:', error)
  } finally {
    healthRecordLoading.value = false
  }
}

// 获取疫苗记录列表
const fetchVaccinations = async () => {
  if (!pet.value) return
  
  vaccinationLoading.value = true
  try {
    await request.get('/pet-vaccination/page', {
      petId: pet.value.id,
      currentPage: vaccinationCurrentPage.value,
      size: vaccinationPageSize.value
    }, {
      onSuccess: (res) => {
        vaccinationList.value = res.records
        vaccinationTotal.value = res.total
      }
    })
  } catch (error) {
    console.error('获取疫苗记录失败:', error)
  } finally {
    vaccinationLoading.value = false
  }
}

// 处理健康记录分页
const handleHealthRecordPageChange = (page) => {
  healthRecordCurrentPage.value = page
  fetchHealthRecords()
}

// 处理疫苗记录分页
const handleVaccinationPageChange = (page) => {
  vaccinationCurrentPage.value = page
  fetchVaccinations()
}

// 获取健康记录类型对应的样式
const getHealthRecordType = (recordType) => {
  switch (recordType) {
    case '体检':
      return 'success'
    case '就诊':
      return 'warning'
    case '手术':
      return 'danger'
    default:
      return 'info'
  }
}

// 格式化日期列
const formatDateColumn = (row, column, cellValue) => {
  return cellValue ? formatDateTime(cellValue) : '-'
}

// 监听标签页变化
watch(activeTab, (newVal) => {
  if (newVal === 'vaccination' && vaccinationList.value.length === 0) {
    fetchVaccinations()
  } else if (newVal === 'healthRecord' && healthRecordList.value.length === 0) {
    fetchHealthRecords()
  }
})

// 处理申请领养
const handleAdopt = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再申请领养')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  
  dialogVisible.value = true
}

// 提交领养申请
const submitAdoption = () => {
  adoptFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      await request.post('/adoption', adoptForm.value, {
        successMsg: '申请提交成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchPetDetail() // 刷新详情
        }
      })
    } catch (error) {
      console.error('提交申请失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 取消申请
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消该领养申请吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/adoption/${pet.value.adoptionId}`, {
        successMsg: '申请已取消',
        onSuccess: () => {
          fetchPetDetail() // 刷新详情
        }
      })
    } catch (error) {
      console.error('取消申请失败:', error)
    }
  }).catch(() => {})
}

// 获取图片列表
const getImageList = (images) => {
  // 使用数据URI作为默认图片，而不是引用外部图片文件
  const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB4PSIyIiB5PSIyIiB3aWR0aD0iMjk2IiBoZWlnaHQ9IjI5NiIgc3R5bGU9ImZpbGw6I2VlZTtzdHJva2U6I2FhYTtzdHJva2Utd2lkdGg6MnB4OyIvPjx0ZXh0IHg9IjE1MCIgeT0iMTUwIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBzdHlsZT0iZmlsbDojOTk5O2ZvbnQtZmFtaWx5OkFyaWFsLHNhbnMtc2VyaWY7Zm9udC1zaXplOjI0cHg7Ij7mmoLml6Dlm77niYc8L3RleHQ+PC9zdmc+'
  
  if (!images) return [defaultImage]
  
  return images.split(',').map(img => {
    const trimmedImg = img.trim()
    if (trimmedImg.startsWith('http')) {
      return trimmedImg
    }
    return `${baseAPI}${trimmedImg}`
  })
}

// 设置当前激活的图片
const setActiveImage = (index) => {
  activeIndex.value = index
  // 使用Element Plus的正确API切换轮播图
  if (carouselRef.value) {
    // 使用setActiveItem方法切换到指定索引
    carouselRef.value.setActiveItem(index)
  }
}

// 处理轮播图变化事件
const handleCarouselChange = (index) => {
  activeIndex.value = index
}

// 获取状态样式类
const getStatusClass = (status) => {
  if (status === '可领养') return 'status-available'
  if (status === '已领养') return 'status-adopted'
  return ''
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchPetDetail()
})
</script>

<style lang="scss" scoped>
.pet-detail-page {
  min-height: 100vh;
  padding: 0 20px 60px;
  position: relative;
  overflow: hidden;
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
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
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

.breadcrumb-container {
  max-width: 1200px;
  margin: 0 auto 20px;
  position: relative;
  z-index: 2;
  
  .back-button {
    display: inline-flex;
    align-items: center;
    padding: 8px 16px;
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    font-weight: 500;
    color: #43a047;
    transition: all 0.3s ease;
    
    i {
      margin-right: 8px;
    }
    
    &:hover {
      transform: translateX(-3px);
      background-color: #f0fff4;
    }
  }
}

.pet-detail-content {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.pet-main-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 40px;
  animation: fadeInUp 0.8s ease both 0.2s;
  
  @media (max-width: 992px) {
    grid-template-columns: 1fr;
  }
}

.pet-gallery {
  flex: 1;
  max-width: 600px;
  
  @media (max-width: 992px) {
    max-width: 100%;
  }
  
  .gallery-main {
    margin-bottom: 20px;
    
    .carousel-container {
      :deep(.el-carousel__arrow) {
        background-color: rgba(255, 182, 193, 0.8);
        border-radius: 50%;
        
        &:hover {
          background-color: #FFB6C1;
        }
      }
      
      :deep(.el-carousel__indicators) {
        .el-carousel__button {
          background-color: #FFEE93;
        }
      }
    }
    
    .carousel-item {
      border-radius: 12px;
      overflow: hidden;
    }
    
    .carousel-image {
      width: 100%;
      height: 100%;
      border-radius: 12px;
    }
  }
  
  .gallery-thumbnails {
    display: flex;
    gap: 10px;
    overflow-x: auto;
    padding-bottom: 10px;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background-color: #FFEE93;
      border-radius: 2px;
    }
    
    &::-webkit-scrollbar-track {
      background-color: #f5f5f5;
      border-radius: 2px;
    }
    
    .thumbnail-item {
      width: 80px;
      height: 80px;
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      border: 2px solid transparent;
      transition: all 0.3s ease;
      flex-shrink: 0;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      }
      
      &.active {
        border-color: #FFB6C1;
      }
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
  }
}

.pet-info-card {
    flex: 1;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    padding: 35px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    transition: transform 0.3s ease, box-shadow 0.3s ease;
  
  &:hover {
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  }
  
  .pet-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    gap: 15px;
    
    .pet-name {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 32px;
      color: #1a202c;
      margin: 0;
      font-weight: 700;
      line-height: 1.3;
    }
    
    .pet-status-badge {
      padding: 8px 16px;
      border-radius: 25px;
      font-size: 13px;
      font-weight: 600;
      color: white;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      white-space: nowrap;
      transition: all 0.3s ease;
    }
  }
  
  .status-available {
    background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
    box-shadow: 0 3px 12px rgba(103, 194, 58, 0.3);
  }

  .status-adopted {
    background: linear-gradient(135deg, #909399 0%, #73767a 100%);
    box-shadow: 0 3px 12px rgba(144, 147, 153, 0.3);
  }
  
  .pet-tags {
    display: flex;
    gap: 10px;
    margin-bottom: 25px;
    flex-wrap: wrap;
    
    .pet-tag {
      padding: 8px 16px;
      border-radius: 25px;
      font-size: 13px;
      font-weight: 600;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
      
      &.tag-category {
        background: rgba(102, 187, 106, 0.1);
        color: #43a047;
        border: 1px solid rgba(102, 187, 106, 0.3);
      }
      
      &.tag-type {
        background: rgba(102, 187, 106, 0.1);
        color: #43a047;
        border: 1px solid rgba(102, 187, 106, 0.3);
      }
      
      &.tag-breed {
        background: rgba(102, 187, 106, 0.1);
        color: #43a047;
        border: 1px solid rgba(102, 187, 106, 0.3);
      }
    }
  }
  
  .pet-attributes {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 15px;
    margin-bottom: 30px;
    
    .attribute-item {
      min-width: 120px;
      background: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      border-radius: 15px;
      padding: 16px 20px;
      display: flex;
      align-items: center;
      border: 1px solid rgba(102, 187, 106, 0.2);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 15px rgba(102, 187, 106, 0.15);
      }
      
      .attribute-icon {
        width: 45px;
        height: 45px;
        border-radius: 50%;
        background: rgba(102, 187, 106, 0.2);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        
        .el-icon {
          width: 24px;
          height: 24px;
          color: #43a047;
        }
      }
      
      .attribute-content {
        flex: 1;
        
        .attribute-label {
          font-size: 12px;
          color: #999;
          margin-bottom: 5px;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
        
        .attribute-value {
          font-size: 16px;
          font-weight: 600;
          color: #1a202c;
        }
      }
    }
  }
  
  .pet-description-section {
    margin-bottom: 30px;
    flex-grow: 1;
    
    .section-title {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 22px;
      color: #1a202c;
      margin: 0 0 15px;
      position: relative;
      padding-left: 25px;
      font-weight: 600;
      animation: fadeInDown 0.8s ease both;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 4px;
        height: 22px;
        background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
        border-radius: 2px;
      }
    }
    
    .description-text {
      font-size: 15px;
      line-height: 1.8;
      color: #666;
      white-space: pre-line;
      background: rgba(255, 255, 255, 0.6);
      padding: 20px;
      border-radius: 12px;
      border: 1px solid rgba(102, 187, 106, 0.1);
    }
  }
  
  .pet-actions {
    margin-top: auto;
    display: flex;
    justify-content: center;
    
    .adopt-button, .cancel-button, .success-button, .rejected-button, .adopted-button {
      min-width: 200px;
      height: 50px;
      font-size: 16px;
      border-radius: 25px;
      transition: all 0.3s ease;
      font-weight: 600;
      border: none;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
      }
      
      .button-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }
    
    .adopt-button {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
      color: white;
      border: none;
      box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
      
      &:hover {
        background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
        box-shadow: 0 8px 25px rgba(102, 187, 106, 0.4);
      }
    }
    
    .cancel-button {
      background: linear-gradient(135deg, #f56c6c 0%, #f34e4e 100%);
      color: white;
      border: none;
      box-shadow: 0 4px 15px rgba(245, 108, 108, 0.3);
      
      &:hover {
        background: linear-gradient(135deg, #f34e4e 0%, #e83a3a 100%);
        box-shadow: 0 8px 25px rgba(245, 108, 108, 0.4);
      }
    }
    
    .success-button {
      background: linear-gradient(135deg, #67c23a 0%, #529b2e 100%);
      color: white;
      border: none;
      box-shadow: 0 4px 15px rgba(103, 194, 58, 0.3);
      
      &:hover {
        background: linear-gradient(135deg, #529b2e 0%, #437a25 100%);
        box-shadow: 0 8px 25px rgba(103, 194, 58, 0.4);
      }
    }
    
    .rejected-button, .adopted-button {
      background: linear-gradient(135deg, #909399 0%, #73767a 100%);
      color: white;
      border: none;
      box-shadow: 0 4px 15px rgba(144, 147, 153, 0.3);
      
      &:hover {
        background: linear-gradient(135deg, #73767a 0%, #606266 100%);
        box-shadow: 0 8px 25px rgba(144, 147, 153, 0.4);
      }
    }
  }
}

.pet-details-tabs {
  margin-bottom: 40px;
  padding: 30px;
  width: 100%;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
  /* 玻璃态卡片设计 */
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
    transform: translateY(-5px);
  }
}

.custom-tabs {
  position: relative;
  width: 100%;
  /* 现代化标签页设计 */
  &.el-tabs {
    :deep(.el-tabs__header) {
      border-bottom: 2px solid rgba(255, 215, 0, 0.2);
      margin-bottom: 30px;
    }

    :deep(.el-tabs__nav) {
      display: flex;
      gap: 50px;
    }

    :deep(.el-tabs__item) {
      color: rgba(0, 0, 0, 0.6);
      font-size: 1.1rem;
      font-weight: 500;
      padding: 16px 0;
      position: relative;
      transition: all 0.3s ease;
      border-bottom: 3px solid transparent;
    }

    :deep(.el-tabs__item:hover) {
      color: #FF9800;
      border-bottom-color: rgba(255, 152, 0, 0.3);
      transform: translateY(-2px);
    }

    :deep(.el-tabs__item.is-active) {
      color: #FF9800;
      border-bottom-color: #FF9800;
      font-weight: 600;
    }

    :deep(.el-tabs__active-bar) {
      background-color: #FF9800;
      height: 3px;
      border-radius: 3px;
      transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
    }

    :deep(.el-tab-pane) {
      padding: 35px;
      animation: fadeIn 0.5s ease-out;
    }
  }
}

.tab-content {
  padding: 40px 30px;
  min-height: 200px;
  animation: fadeInUp 0.8s ease both;
}

.health-timeline {
    padding: 20px 0;
    
    .timeline-item {
      margin-bottom: 30px;
      position: relative;
      
      :deep(.el-timeline-item__tail) {
        background-color: #FFB6C1;
        width: 2px;
      }
      
      :deep(.el-timeline-item__node--success) {
        background-color: #67c23a;
        box-shadow: 0 0 0 4px rgba(103, 194, 58, 0.1);
      }
      
      :deep(.el-timeline-item__node--warning) {
        background-color: #e6a23c;
        box-shadow: 0 0 0 4px rgba(230, 162, 60, 0.1);
      }
      
      :deep(.el-timeline-item__node--danger) {
        background-color: #f56c6c;
        box-shadow: 0 0 0 4px rgba(245, 108, 108, 0.1);
      }
      
      :deep(.el-timeline-item__timestamp) {
        color: #999;
        font-size: 13px;
        margin-bottom: 8px;
        font-weight: 500;
      }
    }
    
    .record-card {
      border-radius: 12px;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      background: rgba(255, 255, 255, 0.7);
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.2);
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.12);
        border-color: rgba(255, 182, 193, 0.3);
      }
      
      :deep(.el-card__header) {
        padding: 20px 25px;
        border-bottom: 1px solid rgba(255, 182, 193, 0.1);
      }
      
      .record-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .record-title {
          font-weight: 600;
          color: #683e35;
          font-size: 16px;
        }
        
        .record-hospital {
          font-size: 13px;
          color: #999;
        }
      }
      
      .record-content {
        padding: 20px 25px;
        
        .record-item {
          margin-bottom: 12px;
          font-size: 14px;
          color: #666;
          display: flex;
          gap: 8px;
          
          strong {
            color: #333;
            min-width: 60px;
          }
          
          &:last-child {
            margin-bottom: 0;
          }
        }
        
        .record-item-group {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;
          margin-bottom: 12px;
        }
      }
    }
  }
  
  .custom-table {
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    
    :deep(.el-table__header-wrapper) {
      th {
        background: linear-gradient(135deg, rgba(255, 182, 193, 0.1) 0%, rgba(255, 182, 193, 0.05) 100%);
        font-weight: 600;
        color: #683e35;
        border-bottom: 2px solid rgba(255, 182, 193, 0.2);
      }
    }
    
    :deep(.el-table__row) {
      &:hover {
        background-color: rgba(255, 182, 193, 0.05);
      }
    }
    
    :deep(.el-table__cell) {
      border-bottom: 1px solid rgba(255, 182, 193, 0.1);
    }
  }

.empty-records {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  padding: 40px;
  margin: 20px 0;
  
  :deep(.el-empty__description) {
    color: #999;
  }
}

.pagination-container {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  
  :deep(.el-pagination) {
    --el-pagination-button-bg-color: rgba(255, 255, 255, 0.8);
    --el-pagination-hover-color: #FFB6C1;
    --el-pagination-button-size: 40px;
    
    .el-pagination__jump {
      color: #683e35;
    }
    
    button:not(:disabled):hover {
      color: #FFB6C1;
      transform: translateY(-2px);
    }
    
    .is-active {
      background: linear-gradient(135deg, #FFB6C1 0%, #FF92B2 100%) !important;
      color: white !important;
      box-shadow: 0 4px 15px rgba(255, 182, 193, 0.3);
    }
    
    .el-pagination__button {
      border-radius: 8px;
      border: 1px solid rgba(255, 182, 193, 0.1);
      transition: all 0.3s ease;
    }
  }
}

.custom-dialog {
  :deep(.el-dialog__header) {
    padding: 30px 30px 0;
    border-bottom: 1px solid rgba(255, 182, 193, 0.1);
  }
  
  :deep(.el-dialog__body) {
    padding: 30px;
  }
  
  :deep(.el-dialog__footer) {
    padding: 20px 30px 30px;
    border-top: 1px solid rgba(255, 182, 193, 0.1);
  }
  
  .dialog-header {
    display: flex;
    align-items: center;
    margin-bottom: 25px;
    
    .dialog-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(255, 238, 147, 0.4) 0%, rgba(255, 238, 147, 0.2) 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      margin-right: 15px;
      box-shadow: 0 4px 15px rgba(255, 238, 147, 0.3);
    }
    
    .dialog-title {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 24px;
      color: #683e35;
      margin: 0;
      font-weight: 600;
    }
  }
  
  .dialog-tip {
    margin: 20px 0;
    background: linear-gradient(135deg, rgba(255, 238, 147, 0.4) 0%, rgba(255, 238, 147, 0.2) 100%);
    border-left: 4px solid #FFEE93;
    padding: 15px 20px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    gap: 12px;
    
    .tip-icon {
      font-size: 20px;
      color: #FFB6C1;
      flex-shrink: 0;
    }
    
    .tip-text {
      font-size: 14px;
      color: #683e35;
      line-height: 1.5;
      margin: 0;
    }
  }
  
  :deep(.el-form-item__label) {
    color: #683e35;
    font-weight: 500;
  }
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
  
  :deep(.el-input__wrapper),
  :deep(.el-textarea__wrapper) {
    border-radius: 10px;
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 182, 193, 0.2);
    
    &:hover {
      box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.2);
      border-color: rgba(255, 182, 193, 0.3);
    }
    
    &.is-focus {
      box-shadow: 0 0 0 2px rgba(255, 182, 193, 0.3);
      border-color: #FFB6C1;
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: center;
    gap: 20px;
    width: 100%;
    
    .cancel-btn, .submit-btn {
      min-width: 140px;
      height: 44px;
      border-radius: 22px;
      font-weight: 500;
      font-size: 15px;
      transition: all 0.3s ease;
    }
    
    .cancel-btn {
      background: rgba(255, 255, 255, 0.8);
      border: 1px solid rgba(255, 182, 193, 0.3);
      color: #606266;
      
      &:hover {
        border-color: #FFB6C1;
        color: #FFB6C1;
        box-shadow: 0 4px 15px rgba(255, 182, 193, 0.2);
      }
    }
    
    .submit-btn {
      background: linear-gradient(135deg, #FFA726 0%, #FF9800 100%);
      color: white;
      border: none;
      box-shadow: 0 4px 15px rgba(255, 167, 38, 0.3);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(255, 167, 38, 0.4);
      }
    }
  }
}

.pet-empty {
  margin-top: 100px;
  padding: 60px 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
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

@media (max-width: 768px) {
  .pet-detail-page {
    padding: 10px;
  }
  
  .pet-info-card {
    padding: 25px;
  }
  
  .pet-name {
    font-size: 28px !important;
  }
  
  .pet-attributes {
    grid-template-columns: 1fr;
    gap: 10px;
    
    .attribute-item {
      width: 100%;
    }
  }
  
  .pet-description-section {
    .description-text {
      font-size: 14px;
      padding: 15px;
    }
  }
  
  .tab-content {
    padding: 25px 15px;
  }
  
  .pet-actions {
    .adopt-button, .cancel-button, .success-button, .rejected-button, .adopted-button {
      min-width: 100%;
    }
  }
  
  .record-item-group {
    flex-direction: column;
    gap: 10px !important;
  }
  
  .dialog-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }
  
  .dialog-footer {
    flex-direction: column;
    
    .cancel-btn, .submit-btn {
      width: 100%;
    }
  }
}
</style> 