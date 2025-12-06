<template>
  <div class="home-container">
    <!-- Hero区域 - 轮播图 -->
    <div class="hero-section">
      <div class="hero-background">
        <banner-carousel :bannerList="bannerList" height="100%" indicator-position="outside" :interval="5000" arrow="always" />
        <div class="hero-particles"></div>
      </div>
    </div>

    <!-- 公告区域 -->
    <section class="announcement-section">
      <div class="section-container">
        <announcement-list title="最新公告" :limit="5" @view-more="navigateTo('/announcement')" />
      </div>
    </section>

    <!-- 核心功能区 -->
    <section class="quick-nav-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">
            <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
            <span class="gradient-text">核心服务</span>
          </h2>
        </div>

        <div class="category-grid">
          <div class="category-card glass-effect" v-for="(feature, index) in coreFeatures" :key="index" @click="navigateTo(feature.link)">
            <div class="category-icon-wrapper">
              <el-icon :size="32">
                <component :is="feature.icon" />
              </el-icon>
            </div>
            <h3 class="category-name">{{ feature.title }}</h3>
            <p class="category-desc">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 宠物推荐区 -->
    <section class="featured-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">
            <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span class="gradient-text">宠物推荐</span>
          </h2>
          <router-link to="/pet" class="view-more-btn glass-btn">
            查看全部 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <el-skeleton :loading="petsLoading" animated :count="8" :throttle="500">
          <template #template>
            <div class="scenic-grid">
              <div v-for="i in 8" :key="i" class="skeleton-item">
                <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
                <div style="padding: 16px;">
                  <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 8px" />
                  <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 8px" />
                  <el-skeleton-item variant="text" style="width: 40%" />
                </div>
              </div>
            </div>
          </template>
          <template #default>
            <div class="scenic-grid">
              <div v-for="(pet, index) in recommendPets" :key="index" class="scenic-card glass-card" @click="navigateTo(`/pet/${pet.id}`)">
                <div class="card-image-wrapper">
                  <img :src="pet.imageUrl" :alt="pet.name" />
                  <div class="image-gradient-overlay"></div>
                  <div class="card-badges">
                    <span :class="pet.status === '可领养' ? 'badge badge-free' : 'badge badge-adopted'">
                      {{ pet.status }}
                    </span>
                  </div>
                </div>
                <div class="card-content">
                  <h3 class="scenic-name">{{ pet.name }}</h3>
                  <div class="pet-info">
                    <div class="pet-tags">
                      <span class="pet-tag">{{ pet.category }}</span>
                      <span class="pet-tag">{{ pet.breed }}</span>
                    </div>
                    <div class="pet-props">
                      <span class="pet-prop">{{ pet.age }}岁</span>
                      <span class="pet-prop">{{ pet.gender }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <!-- 宠物用品区 -->
    <section class="inspiration-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">
            <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"></path>
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <path d="M16 10a4 4 0 0 1-8 0"></path>
            </svg>
            <span class="gradient-text">精选用品</span>
          </h2>
          <router-link to="/product" class="view-more-btn glass-btn">
            浏览更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <el-skeleton :loading="productsLoading" animated :count="8" :throttle="500">
          <template #template>
            <div class="scenic-grid">
              <div v-for="i in 8" :key="i" class="skeleton-item">
                <el-skeleton-item variant="image" style="width: 100%; height: 200px" />
                <div style="padding: 16px;">
                  <el-skeleton-item variant="h3" style="width: 80%; margin-bottom: 8px" />
                  <el-skeleton-item variant="text" style="width: 60%; margin-bottom: 8px" />
                  <el-skeleton-item variant="text" style="width: 40%" />
                </div>
              </div>
            </div>
          </template>
          <template #default>
            <div class="scenic-grid">
              <div v-for="(product, index) in recommendProducts" :key="index" class="scenic-card glass-card" @click="navigateTo(`/product/${product.id}`)">
                <div class="card-image-wrapper">
                  <img :src="product.imageUrl" :alt="product.name" />
                  <div class="image-gradient-overlay"></div>
                  <div class="card-badges">
                    <span v-if="product.tag" class="badge badge-free">{{ product.tag }}</span>
                  </div>
                </div>
                <div class="card-content">
                  <h3 class="scenic-name">{{ product.name }}</h3>
                  <p class="scenic-desc">{{ product.brief }}</p>
                  <div class="product-price-row">
                    <span class="product-price">¥{{ product.price }}</span>
                    <span class="product-orig-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
    </section>

    <!-- 服务特色区 -->
    <section class="featured-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">
            <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span class="gradient-text">专业服务</span>
          </h2>
          <router-link to="/service" class="view-more-btn glass-btn">
            了解更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div class="scenic-grid">
          <div v-for="(service, index) in specialServices" :key="index" class="scenic-card glass-card" @click="navigateTo(service.link)">
            <div class="card-image-wrapper">
              <img :src="service.imageUrl" :alt="service.title" />
              <div class="image-gradient-overlay"></div>
            </div>
            <div class="card-content">
              <h3 class="scenic-name">{{ service.title }}</h3>
              <p class="scenic-desc">{{ service.description }}</p>
              <el-button class="service-btn glass-btn" @click.stop="navigateTo(service.link)">
                了解更多
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
import { ElMessage } from 'element-plus';
import BannerCarousel from '@/components/frontend/BannerCarousel.vue';
import AnnouncementList from '@/components/frontend/AnnouncementList.vue';
import {
  HomeFilled,
  ShoppingBag,
  Service,
  FirstAid,
  Scissors,
  House,
  Medal,
  ArrowRight
} from '@element-plus/icons-vue';

const router = useRouter();

// 轮播图数据
const bannerList = ref([]);
const fetchBanners = async () => {
  try {
    await request.get('/banner/list', { status: 1 }, { // 只获取状态为启用的轮播图
      onSuccess: (data) => {
        // 如果有轮播图数据，对其进行处理以匹配默认数据结构；否则使用默认数据
        if (data && data.length > 0) {
          // 处理API返回的数据，确保包含所有必要的字段
          bannerList.value = data.map(item => ({
            title: item.title || '',
            subtitle: item.subtitle || '',
            description: item.description || '',
            imageUrl: item.imageUrl || '',
            linkUrl: item.linkUrl || ''
          }));
        } else {
          useDefaultBanners();
        }
      },
      onError: () => {
        useDefaultBanners();
        console.error('获取轮播图失败，使用默认数据');
      }
    });
  } catch (error) {
    console.error('获取轮播图失败:', error);
    useDefaultBanners();
  }
};

// 使用默认轮播图数据
const useDefaultBanners = () => {
  bannerList.value = [
    {
      title: '寻找温暖的家',
      subtitle: '让每一只毛孩子都能找到属于自己的爱巢',
      description: '我们提供专业的宠物领养服务，帮助流浪动物重新找到温暖的家庭。所有宠物都经过健康检查和疫苗接种，确保它们能够健康快乐地生活。',
      imageUrl: 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1470&q=80',
      linkUrl: '/pet'
    },
    {
      title: '专业宠物服务',
      subtitle: '全方位呵护您的爱宠健康与美丽',
      description: '由资深宠物美容师和兽医组成的专业团队，为您的爱宠提供美容、洗澡、寄养、医疗等一站式服务，让您的宠物始终保持最佳状态。',
      imageUrl: 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1460&q=80',
      linkUrl: '/service'
    },
    {
      title: '精选宠物用品',
      subtitle: '为您的爱宠提供高品质生活体验',
      description: '严选国内外知名品牌宠物用品，包括食品、玩具、窝具、服饰等，全方位满足您爱宠的生活需求，让它们享受舒适快乐的每一天。',
      imageUrl: 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1469&q=80',
      linkUrl: '/product'
    }
  ];
};

// 核心功能区数据
const coreFeatures = ref([
  {
    title: '宠物领养',
    description: '为流浪宠物寻找温暖的家，让爱延续',
    icon: HomeFilled,
    link: '/pet',
    bgColor: '#FFB6C1'
  },
  {
    title: '宠物商城',
    description: '精选高品质宠物用品，让爱宠生活更舒适',
    icon: ShoppingBag,
    link: '/product',
    bgColor: '#FFEE93'
  },
  {
    title: '宠物服务',
    description: '专业的医疗、美容、训练等全方位服务',
    icon: Service,
    link: '/service',
    bgColor: '#A0CFD3'
  },
  {
    title: '宠物训练',
    description: '专业的宠物训练，让爱宠健康成长',
    icon: Medal,
    link: '/training',
    bgColor: '#B5EAD7'
  }
]);

// 宠物推荐数据
const recommendPets = ref([]);
const petsLoading = ref(false);

// 宠物用品数据
const recommendProducts = ref([]);
const productsLoading = ref(false);
const baseAPI = process.env.VUE_APP_BASE_API || '/api'
// 服务特色数据
const specialServices = ref([
  {
    title: '宠物服务',
    description: '提供宠物美容、洗护等一站式专业服务',
    icon: Scissors,
    iconBg: '#FF9AA2',
    link: '/service',
    imageUrl: 'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1471&q=80'
  },
  {
    title: '宠物寄养',
    description: '安全舒适的寄养环境，让您的爱宠得到精心呵护',
    icon: House,
    iconBg: '#FFDAC1',
    link: '/boarding/apply',
    imageUrl: 'https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1460&q=80'
  },
  {
    title: '宠物训练',
    description: '由专业训练师提供个性化训练课程，培养良好行为习惯',
    icon: Medal,
    iconBg: '#E2F0CB',
    link: '/training',
    imageUrl: 'https://images.unsplash.com/photo-1559190394-df5a28aab5c5?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1469&q=80'
  }
]);

// 获取推荐宠物列表
const fetchRecommendPets = async () => {
  petsLoading.value = true;
  try {
    await request.get('/pet/recommend', { limit: 8 }, {
      onSuccess: (data) => {
        recommendPets.value = data.map(pet => ({
          id: pet.id,
          name: pet.name,
          category: pet.categoryName,
          breed: pet.breed,
          age: pet.age,
          gender: pet.gender,
          status: pet.adoptionStatus,
          imageUrl: getImageUrl(pet.images)
        }));
      },
      onError: () => {
        ElMessage.error('获取推荐宠物失败，请稍后再试');
        recommendPets.value = [];
      },
      showDefaultMsg: false
    });
  } catch (error) {
    console.error('获取推荐宠物失败:', error);
    recommendPets.value = [];
  } finally {
    petsLoading.value = false;
  }
};

// 获取宠物用品列表
const fetchRecommendProducts = async () => {
  productsLoading.value = true;
  try {
    await request.get('/product/recommend', { limit: 8 }, {
      onSuccess: (data) => {
        recommendProducts.value = data.map(product => ({
          id: product.id,
          name: product.name,
          brief: product.description,
          price: product.price,
          imageUrl: getImageUrl(product.images),
          tag: product.status === 1 ? '推荐' : '新品'
        }));
      },
      onError: () => {
        ElMessage.error('获取推荐宠物用品失败，请稍后再试');
        recommendProducts.value = [];
      },
      showDefaultMsg: false
    });
  } catch (error) {
    console.error('获取推荐宠物用品失败:', error);
    recommendProducts.value = [];
  } finally {
    productsLoading.value = false;
  }
};

// 获取图片URL
const getImageUrl = (images) => {
  if (!images) return '';
  const imageList = images.split(',');
  if (imageList.length === 0) return '';

  const firstImage = imageList[0].trim();
  if (firstImage.startsWith('http')) {
    return firstImage;
  }
  return `${baseAPI}${firstImage}`;
};

// 导航方法
const navigateTo = (path) => {
  router.push(path);
};

onMounted(() => {
  fetchBanners();
  fetchRecommendPets();
  fetchRecommendProducts();
});
</script>

<style lang="scss" scoped>
// 全局变量
:root {
  --gradient-primary: linear-gradient(135deg, #67b6f5 0%, #5aa9e6 100%);
  --gradient-secondary: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  --gradient-success: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  --gradient-warm: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.home-container {
  width: 100%;
  font-family: "PingFang SC", "Helvetica Neue", Helvetica, Arial, "Microsoft YaHei", sans-serif;
  color: #1a202c;
  min-height: 100vh;
}

// Hero区域样式
.hero-section {
  position: relative;
  height: 600px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;

  :deep(.banner-carousel) {
    height: 100%;
    z-index: 1;

    .el-carousel {
      height: 100%;
      z-index: 1;
    }

    .el-carousel__item {
      height: 100%;
      z-index: 1;
    }

    .el-carousel__item img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.hero-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 2;
  pointer-events: none;
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.h-content {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 800px;
  padding: 0 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// 为每个卡片添加延迟动画
.scenic-card, .guide-card {
  @for $i from 1 through 12 {
    &:nth-child(#{$i}) {
      animation-delay: #{$i * 0.1}s;
    }
  }
}

// 通用容器样式
.section-container {
  max-width: 1300px;
  margin: 0 auto;
  padding: 80px 20px;
}

// 区域样式
.announcement-section, .quick-nav-section, .featured-section, .inspiration-section {
  position: relative;
}

// 公告区域特殊样式
.announcement-section {
  padding: 40px 0;
}

// 标题样式
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 50px;
  flex-wrap: wrap;
  gap: 20px;
}

.section-title {
  font-size: 38px;
  font-weight: 800;
  margin: 0;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 12px;
}

.gradient-text {
  background: linear-gradient(135deg, #67b6f5 0%, #5aa9e6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  display: inline-block;

  &::after {
    content: '';
    position: absolute;
    bottom: -10px;
    left: 0;
    width: 60px;
    height: 4px;
    background: linear-gradient(135deg, #67b6f5 0%, #5aa9e6 100%);
    border-radius: 2px;
  }
}

.title-icon {
  width: 32px;
  height: 32px;
  color: #67b6f5;
  flex-shrink: 0;
}

.view-more-btn, .more-link {
  color: #67b6f5;
  font-size: 15px;
  display: flex;
  align-items: center;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s ease;
  padding: 0;

  .el-icon {
    margin-left: 6px;
    transition: transform 0.3s ease;
  }

  &:hover {
    color: #5aa9e6;
    .el-icon {
      transform: translateX(4px);
    }
  }
}

.glass-btn {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 10px 20px;
  border-radius: 20px;
  border: 1px solid rgba(103, 182, 245, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.95);
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(103, 182, 245, 0.2);
  }
}

// 快速导航样式
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
  margin-bottom: 20px;
}

.category-card {
  position: relative;
  border-radius: 24px;
  padding: 40px 30px;
  cursor: pointer;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  overflow: hidden;
  opacity: 1;
  transform: translateY(0);
  animation: fadeInUp 0.6s ease both;

  &.visible {
    opacity: 1;
    transform: translateY(0);
  }

  &:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);

    .category-icon-wrapper {
      transform: scale(1.15) rotate(5deg);
    }
  }
}

.glass-effect {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.category-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  transition: all 0.4s ease;
  color: #67b6f5;
  box-shadow: 0 4px 15px rgba(103, 182, 245, 0.2);
}

.category-name {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 10px;
  color: #1a202c;
}

.category-desc {
  font-size: 14px;
  margin: 0;
  color: #5a6c7d;
  line-height: 1.6;
}

// 景点网格布局（复用为宠物、用品、服务布局）
.scenic-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 30px;
}

.scenic-card, .glass-card {
  border-radius: 20px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  opacity: 1;
  transform: translateY(0);
  animation: fadeInUp 0.6s ease both;

  &.visible {
    opacity: 1;
    transform: translateY(0);
  }

  &:hover {
    transform: translateY(-12px);
    box-shadow: 0 20px 50px rgba(103, 182, 245, 0.2);

    .card-image-wrapper img {
      transform: scale(1.15);
    }

    .image-gradient-overlay {
      opacity: 0.6;
    }
  }
}

.card-image-wrapper {
  height: 240px;
  overflow: hidden;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

.image-gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0) 0%, rgba(0, 0, 0, 0.3) 70%, rgba(0, 0, 0, 0.6) 100%);
  opacity: 0.4;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.card-badges {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2;
}

.badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

  &.badge-free {
    background: linear-gradient(135deg, #52c787, #4ecdc4);
    color: white;
  }

  &.badge-adopted {
    background: linear-gradient(135deg, #ff9a56, #ff6b6b);
    color: white;
  }

  &.badge-price {
    background: linear-gradient(135deg, #ff9a56, #ff6b6b);
    color: white;
  }
}

.card-content {
  padding: 24px;
}

.scenic-name {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.scenic-desc {
  font-size: 14px;
  color: #718096;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
  margin-bottom: 12px;
}

// 宠物信息样式
.pet-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pet-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pet-tag {
  background: rgba(103, 182, 245, 0.1);
  color: #67b6f5;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.pet-props {
  display: flex;
  gap: 16px;
  color: #5a6c7d;
  font-size: 14px;
}

// 产品价格样式
.product-price-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.product-price {
  font-size: 24px;
  font-weight: 800;
  color: #ff6b6b;
}

.product-orig-price {
  font-size: 16px;
  color: #a0aec0;
  text-decoration: line-through;
}

// 服务按钮样式
.service-btn {
  margin-top: 16px;
}

// 骨架屏样式
.skeleton-item {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

// 响应式样式
@media (max-width: 992px) {
  .scenic-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }

  .section-container {
    padding: 60px 20px;
  }
}

@media (max-width: 768px) {
  .hero-section {
    height: 500px;
  }

  .section-title {
    font-size: 28px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 16px;
  }

  .category-card {
    padding: 30px 20px;
  }

  .category-icon-wrapper {
    width: 60px;
    height: 60px;
  }

  .scenic-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }

  .section-container {
    padding: 40px 15px;
  }
}

@media (max-width: 480px) {
  .hero-section {
    height: 400px;
  }

  .section-title {
    font-size: 24px;
  }

  .category-grid {
    grid-template-columns: 1fr;
  }

  .category-card {
    padding: 25px 20px;
  }

  .card-image-wrapper {
    height: 200px;
  }
}
</style>
