<template>
  <div class="pet-list-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>宠物领养</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>寻找你的伴侣</h1>
        <p>每一只宠物都在等待一个温暖的家</p>
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
            <h3>寻找宠物</h3>
            <div class="filter-icon"><el-icon><Search /></el-icon></div>
          </div>
          
          <el-form :model="searchForm" label-width="70px" class="search-form">
            <el-form-item label="名称">
              <el-input 
                v-model="searchForm.name" 
                placeholder="请输入宠物名称" 
                clearable 
                @keyup.enter="handleSearch" 
                class="custom-input" />
            </el-form-item>
            
            <el-form-item label="分类">
              <el-cascader 
                v-model="searchForm.categoryId" 
                :options="categoriesTree"
                :props="{
                  checkStrictly: true,
                  value: 'id',
                  label: 'name',
                  emitPath: false
                }"
                placeholder="请选择宠物分类" 
                clearable 
                filterable
                class="custom-select" />
            </el-form-item>
            
            <el-form-item label="品种">
              <el-input 
                v-model="searchForm.breed" 
                placeholder="请输入宠物品种" 
                clearable 
                @keyup.enter="handleSearch"
                class="custom-input" />
            </el-form-item>
            
            <el-form-item label="状态">
              <el-select 
                v-model="searchForm.adoptionStatus" 
                placeholder="请选择领养状态" 
                clearable 
                class="custom-select">
                <el-option v-for="item in statusOptions" :key="item" :label="item" :value="item" />
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
          <div class="tip-text">我们的宠物全部接受过健康检查和疫苗接种</div>
        </div>
        
        <div class="category-tags" v-if="categoriesList.length > 0">
          <h4 class="tags-title">热门分类</h4>
          <div class="tags-container">
            <el-tag 
              v-for="category in categoriesList.slice(0, 8)" 
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
          宠物列表
          <span class="title-count">(共 {{ total }} 只)</span>
        </h2>
        
        <div class="pet-grid" v-loading="loading">
          <div v-for="pet in petList" :key="pet.id" class="pet-card-wrapper">
            <div class="pet-card" @click="goToDetail(pet.id)">
              <div class="pet-image-container">
                <img :src="getImageUrl(pet.images)" :alt="pet.name" class="pet-image">
                <div class="pet-status" :class="getStatusClass(pet.adoptionStatus)">
                  {{ pet.adoptionStatus }}
                </div>
                <div class="pet-overlay">
                  <div class="pet-quick-info">
                    <div class="quick-info-item">
                      <span class="quick-icon"><el-icon><Search /></el-icon></span>
                      <span>{{ pet.age }}岁</span>
                    </div>
                    <div class="quick-info-item">
                      <span class="quick-icon"><el-icon v-if="pet.gender === '公'"><Male /></el-icon><el-icon v-else><Female /></el-icon></span>
                      <span>{{ pet.gender }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="pet-info">
                <h3 class="pet-name">{{ pet.name }}</h3>
                <div class="pet-tags">
                  <span class="pet-tag tag-type">{{ pet.categoryName || '未分类' }}</span>
                  <span class="pet-tag tag-breed">{{ pet.breed }}</span>
                </div>
                <div class="pet-meta">
                  <div class="meta-item">
                    <span class="meta-icon"><el-icon><Search /></el-icon></span>
                    <span>{{ pet.age }}岁</span>
                  </div>
                  <div class="meta-item">
                      <span class="meta-icon"><el-icon v-if="pet.gender === '公'"><Male /></el-icon><el-icon v-else><Female /></el-icon></span>
                      <span>{{ pet.gender }}</span>
                    </div>
                </div>
                <div class="pet-button">
                  <span>了解更多</span>
                  <span class="button-icon">→</span>
                </div>
              </div>
            </div>
          </div>
          
          <el-empty v-if="petList.length === 0 && !loading" description="暂无符合条件的宠物" />
        </div>
        
        <div class="pagination-container">
          <el-pagination
            background
            layout="prev, pager, next, jumper"
            :total="total"
            :page-size="pageSize"
            :current-page="currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const baseAPI = process.env.VUE_APP_BASE_API || '/api'

const petList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const loading = ref(false)

const statusOptions = ['可领养', '已领养']
const categoriesList = ref([])
const categoriesTree = ref([])

const searchForm = reactive({
  name: '',
  breed: '',
  adoptionStatus: '',
  categoryId: ''
})

// 获取宠物列表
const fetchPets = async () => {
  loading.value = true
  try {
    await request.get('/pet/page', {
      name: searchForm.name,
      breed: searchForm.breed,
      adoptionStatus: searchForm.adoptionStatus,
      categoryId: searchForm.categoryId || null,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        petList.value = res.records
        total.value = res.total
      }
    })
  } catch (error) {
    console.error('获取宠物列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表和树形结构
const fetchCategories = async () => {
  try {
    await request.get('/pet-category/tree', {}, {
      onSuccess: (res) => {
          if (res) {
        categoriesTree.value = res.map(item => ({
          ...item,
            disabled: item.status === 0
          }))
            flattenCategories(res)
        }else{
          categoriesTree.value = []
          flattenCategories([])
        }
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 将树形结构扁平化
const flattenCategories = (tree) => {
  categoriesList.value = []
  const flatten = (items) => {
    items.forEach(item => {
      categoriesList.value.push({
        id: item.id,
        name: item.name
      })
      if (item.children && item.children.length > 0) {
        flatten(item.children)
      }
    })
  }
  flatten(tree)
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

// 处理搜索事件
const handleSearch = () => {
  currentPage.value = 1
  fetchPets()
}

// 重置搜索条件
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  currentPage.value = 1
  fetchPets()
}

// 处理分页事件
const handlePageChange = (page) => {
  currentPage.value = page
  fetchPets()
}

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/pet/${id}`)
}

// 获取宠物图片
const getImageUrl = (images) => {
  // 使用数据URI作为默认图片，而不是引用外部图片文件
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

// 获取状态样式类
const getStatusClass = (status) => {
  if (status === '可领养') return 'status-available'
  if (status === '已领养') return 'status-adopted'
  return ''
}

// 页面加载时获取数据
onMounted(() => {
  fetchPets()
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.pet-list-page {
  min-height: 100vh;
  padding-bottom: 50px;
}

.page-banner {
  background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 100%);
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
      color: #1a202c;
      font-weight: 500;
    }
    
    .custom-input {
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        transition: all 0.3s ease;
        background: rgba(255, 255, 255, 0.9);
        border: 1px solid rgba(102, 187, 106, 0.3);
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba(102, 187, 106, 0.5);
          border-color: rgba(102, 187, 106, 0.5);
        }
        
        :deep(.el-input__inner) {
          color: #1a202c;
          
          &::placeholder {
            color: rgba(26, 32, 44, 0.5);
          }
        }
        
        :deep(.el-input__suffix-inner) {
          color: #66bb6a;
        }
      }
    }
    
    .custom-select {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        transition: all 0.3s ease;
        background: rgba(255, 255, 255, 0.9);
        border: 1px solid rgba(102, 187, 106, 0.3);
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba(102, 187, 106, 0.5);
          border-color: rgba(102, 187, 106, 0.5);
        }
      }
      
      :deep(.el-select__input) {
        color: #1a202c;
      }
      
      :deep(.el-select__placeholder) {
        color: rgba(26, 32, 44, 0.5);
      }
      
      :deep(.el-select__suffix-inner) {
        color: #66bb6a;
      }
    }
    
    .option-icon {
      margin-right: 5px;
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
      background: #66bb6a;
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

.list-section {
  flex-grow: 1;
}

.section-title {
  display: flex;
  align-items: center;
  font-family: 'Nunito Sans', sans-serif;
  font-size: 32px;
  color: #1a202c;
  margin-bottom: 30px;
  
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

.pet-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30px;
  margin-bottom: 40px;
  
  .pet-card-wrapper {
    animation: fadeInUp 0.6s ease-out;
    
    &:nth-child(1) { animation-delay: 0.1s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.3s; }
    &:nth-child(4) { animation-delay: 0.4s; }
    &:nth-child(5) { animation-delay: 0.5s; }
    &:nth-child(6) { animation-delay: 0.6s; }
    &:nth-child(7) { animation-delay: 0.7s; }
    &:nth-child(8) { animation-delay: 0.8s; }
  }
}

.pet-card-wrapper {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-10px);
  }
}

.pet-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
    transform: translateY(-8px);
    
    .pet-overlay {
      opacity: 1;
    }
    
    .pet-button {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
      color: white;
      border-radius: 12px;
      padding: 8px 16px;
      font-weight: 600;
      transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
      box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
      position: relative;
      overflow: hidden;
      z-index: 1;
      display: inline-block;
      
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
      
      .button-icon {
        transform: translateX(5px);
      }
    }
  }
}

.pet-image-container {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  
  .pet-card:hover & {
    transform: scale(1.1);
  }
}

.pet-status {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  z-index: 2;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.status-available {
  background: #67c23a;
  box-shadow: 0 3px 6px rgba(103, 194, 58, 0.2);
}

.status-adopted {
  background: #909399;
  box-shadow: 0 3px 6px rgba(144, 147, 153, 0.2);
}

.pet-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.4) 100%);
  opacity: 0;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: flex-end;
  padding: 15px;
}

.pet-quick-info {
    display: flex;
    gap: 15px;
    animation: fadeInUp 0.4s ease-out;
}

.quick-info-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  padding: 5px 10px;
  font-size: 12px;
  display: flex;
  align-items: center;
  
  .quick-icon {
    margin-right: 5px;
  }
}

.pet-info {
      padding: 20px;
      flex-grow: 1;
      display: flex;
      flex-direction: column;
    }
    
    .pet-name {
      font-size: 22px;
      font-weight: 600;
      margin: 0 0 12px;
      color: #1a202c;
    }
    
    .pet-tags {
      display: flex;
      gap: 10px;
      margin-bottom: 18px;
      flex-wrap: wrap;
    }
    
    .pet-tag {
      background: rgba(102, 187, 106, 0.2);
      padding: 6px 12px;
      border-radius: 16px;
      font-size: 12px;
      font-weight: 500;
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      color: #1a202c;
      
      &.tag-type {
        background: rgba(102, 187, 106, 0.3);
      }
      
      &.tag-breed {
        background: rgba(100, 181, 246, 0.3);
      }
    }
    
    .pet-meta {
      display: flex;
      gap: 16px;
      margin-bottom: 20px;
      flex-grow: 1;
      
      .meta-item {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #718096;
        
        .meta-icon {
          margin-right: 6px;
          font-size: 18px;
          color: #66bb6a;
        }
      }
    }

.pet-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: rgba(102, 187, 106, 0.1);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #1a202c;
  transition: all 0.3s ease;
  margin-top: auto;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  
  .button-icon {
    margin-left: 8px;
    transition: transform 0.3s ease;
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
    
    .el-pagination__jump {
      color: #1a202c;
    }
    
    .el-pagination__total {
      color: #1a202c;
    }
    
    button:not(:disabled):hover {
      color: #66bb6a;
      background: rgba(102, 187, 106, 0.1);
    }
    
    .is-active {
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%) !important;
      color: white !important;
      border-color: transparent;
    }
    
    :deep(.el-input__wrapper) {
      background: rgba(255, 255, 255, 0.9);
      border-color: rgba(102, 187, 106, 0.3);
      
      .el-input__inner {
        color: #1a202c;
      }
    }
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

@keyframes pulse {
  0% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(102, 187, 106, 0.4);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
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

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .pet-grid {
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
  
  .pet-grid {
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
  
  .pet-grid {
    gap: 20px;
  }
  
  .section-title {
    font-size: 24px;
  }
  
  .pet-name {
    font-size: 20px;
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
  
  .pet-grid {
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
  
  .filter-actions {
    flex-direction: column;
  }
  
  .section-title {
    font-size: 22px;
    flex-direction: column;
    align-items: flex-start;
  }
  
  .title-icon {
    margin-bottom: 8px;
  }
  
  .category-tags {
    padding: 15px;
  }
  
  .pagination-container {
    margin-top: 30px;
  }
}

.category-tags {
  margin-top: 25px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(102, 187, 106, 0.3);
  
  .tags-title {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 18px;
    color: #1a202c;
    margin: 0 0 15px;
    font-weight: 600;
  }
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .category-tag {
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
}
</style> 