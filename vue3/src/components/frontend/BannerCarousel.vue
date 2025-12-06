<template>
  <div class="banner-carousel glass-container">
    <!-- 粒子效果容器 -->
    <div class="hero-particles"></div>
    
    <el-carousel 
      :height="height" 
      :indicator-position="indicatorPosition" 
      :interval="interval" 
      :arrow="arrow"
      :autoplay="autoplay"
      :trigger="trigger"
      :initial-index="0"
      class="modern-carousel"
    >
      <el-carousel-item v-for="(item, index) in bannerList" :key="index" class="carousel-item-wrapper">
        <div 
          class="banner-item" 
          :style="{ 
            backgroundImage: `url(${getImageUrl(item.imageUrl)})`,
            backgroundPosition: 'center center',
            backgroundSize: 'cover'
          }"
          @click="handleClick(item)"
        >
          <!-- 渐变遮罩层 -->
          <div class="banner-gradient-overlay"></div>
          
          <!-- 粒子效果容器 -->
          <div id="particles-container" class="hero-particles"></div>
          
          <!-- 内容容器 -->
          <div class="banner-content-wrapper" v-if="showContent && (item.title || item.subtitle || item.description)">
            <div class="banner-content glass-card">
              <div class="content-header">
                <h2 class="banner-title" v-if="item.title">{{ item.title }}</h2>
                <h3 class="banner-subtitle" v-if="item.subtitle">{{ item.subtitle }}</h3>
              </div>
              <p class="banner-description" v-if="item.description">{{ item.description }}</p>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 没有轮播图时的占位 -->
    <div class="empty-banner glass-card" v-if="!bannerList || bannerList.length === 0">
      <el-empty description="暂无轮播图" :image-size="200">
        <template #description>
          <p>暂未配置轮播图</p>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 组件接收的属性
const props = defineProps({
  // 轮播图数据
  bannerList: {
    type: Array,
    default: () => []
  },
  // 轮播图高度
  height: {
    type: String,
    default: '600px' // 增加默认高度，更符合现代设计
  },
  // 指示器位置
  indicatorPosition: {
    type: String,
    default: 'bottom' // 默认底部位置，更符合现代设计
  },
  // 轮播间隔时间，单位毫秒
  interval: {
    type: Number,
    default: 6000 // 增加间隔时间，提升用户体验
  },
  // 箭头显示时机：always/hover/never
  arrow: {
    type: String,
    default: 'hover'
  },
  // 是否自动播放
  autoplay: {
    type: Boolean,
    default: true
  },
  // 指示器触发方式：hover/click
  trigger: {
    type: String,
    default: 'click'
  },
  // 是否显示内容
  showContent: {
    type: Boolean,
    default: true
  },
  // API前缀
  baseAPI: {
    type: String,
    default: process.env.VUE_APP_BASE_API || '/api'
  }
});

// 方法: 处理点击事件
const handleClick = (banner) => {
  if (banner.linkUrl) {
    // 判断是否是外部链接
    if (banner.linkUrl.startsWith('http') || banner.linkUrl.startsWith('https')) {
      window.open(banner.linkUrl, '_blank');
    } else {
      // 内部链接，路由跳转
      router.push(banner.linkUrl);
    }
  }
};

// 处理图片URL，添加API前缀
const getImageUrl = (url) => {
  if (!url) return '';
  if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('blob:')) {
    return url;
  }
  return props.baseAPI + url;
};

// 添加粒子效果 - 参考旅游系统首页设计
const createParticles = () => {
  // 选择所有粒子容器
  const particlesContainers = document.querySelectorAll('.hero-particles');
  if (!particlesContainers || particlesContainers.length === 0) return;

  particlesContainers.forEach(container => {
    // 清除现有粒子
    container.innerHTML = '';

    // 为每个容器创建20个粒子
    for (let i = 0; i < 20; i++) {
      const particle = document.createElement('div');
      particle.className = 'particle';
      particle.style.cssText = `
        position: absolute;
        width: ${Math.random() * 4 + 2}px;
        height: ${Math.random() * 4 + 2}px;
        background: rgba(255, 255, 255, ${Math.random() * 0.5 + 0.3});
        border-radius: 50%;
        left: ${Math.random() * 100}%;
        top: ${Math.random() * 100}%;
        animation: particleFloat ${Math.random() * 3 + 4}s ease-in-out infinite;
        animation-delay: ${Math.random() * 2}s;
        z-index: 2;
      `;
      container.appendChild(particle);
    }
  });
}

// 组件挂载后创建粒子效果
onMounted(() => {
  nextTick(() => {
    createParticles();
  });
});
</script>

<style lang="scss" scoped>
// 全局变量定义 - 采用小米风格的中性色调
:root {
  --gradient-primary: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  --gradient-secondary: linear-gradient(135deg, #ff9f43 0%, #ffa502 100%);
  --gradient-accent: linear-gradient(135deg, #2ed573 0%, #1e90ff 100%);
  --glass-bg: rgba(255, 255, 255, 0.15);
  --glass-border: rgba(255, 255, 255, 0.25);
  --glass-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  --text-primary: #333333;
  --text-secondary: #666666;
  --text-light: #ffffff;
}

.banner-carousel {
  position: relative;
  margin-bottom: 40px;
  width: 100%;
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  
  :deep(.el-carousel) {
    width: 100%;
  }
  
  :deep(.el-carousel__container) {
    border-radius: 16px;
    overflow: hidden;
  }
  
  // 自定义箭头样式 - 参考旅游系统简洁设计
  :deep(.el-carousel__arrow) {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: none;
    box-shadow: none;
    transition: all 0.3s ease;
    color: white;
    font-size: 24px;
    margin: 0 20px;
    opacity: 0.7;
    
    &:hover {
      opacity: 1;
      background: rgba(0, 0, 0, 0.5);
      transform: none;
    }
  }
  
  // 左右箭头位置
  :deep(.el-carousel__arrow--left) {
    left: 0;
  }
  
  :deep(.el-carousel__arrow--right) {
    right: 0;
  }
  
  // 自定义指示器样式 - 参考旅游系统设计
  :deep(.el-carousel__indicators) {
    bottom: 20px;
    z-index: 2;
    
    .el-carousel__button {
      width: 30px;
      height: 3px;
      border-radius: 2px;
      background: rgba(255, 255, 255, 0.6);
      transition: all 0.3s ease;
      border: none;
      
      &.is-active {
        background: white;
        width: 40px;
        box-shadow: 0 4px 12px rgba(255, 255, 255, 0.4);
      }
    }
  }
  
  // 外部指示器样式
  :deep(.el-carousel__indicators--outside) {
    margin: 0 30px;
    
    .el-carousel__button {
      margin: 0 8px;
    }
  }
}

.banner-item {
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.6s ease;
  
  &:hover {
    transform: scale(1.05);
  }
  
  &:hover .banner-title {
    transform: translateY(-5px);
  }
}

// 渐变遮罩层 - 参考旅游系统设计（底部渐变增强）
.banner-gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.7) 0%,
    rgba(0, 0, 0, 0.4) 50%,
    rgba(0, 0, 0, 0.1) 100%
  );
  z-index: 1;
}

// 粒子效果容器 - 参考旅游系统设计
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

// 粒子动画 - 参考旅游系统设计
@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0px);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-10px);
    opacity: 0.8;
  }
}

// 内容容器 - 参考旅游系统居中底部显示
.banner-content-wrapper {
  position: absolute;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
  max-width: 800px;
  width: 90%;
  padding: 0;
  text-align: center;
  animation: fadeInUp 1s ease;
}

// 内容卡片 - 移除背景和毛玻璃效果
.glass-card {
  background: none;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  border: none;
  box-shadow: none;
  border-radius: 0;
  padding: 0;
  transition: none;
  color: var(--text-light);
  
  &:hover {
    background: none;
    box-shadow: none;
    transform: none;
  }
}

// 内容头部样式
.content-header {
  margin-bottom: 24px;
}

// 标题样式 - 精致排版，添加渐变色效果
.banner-title {
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  font-size: 56px;
  font-weight: 700;
  margin-bottom: 12px;
  line-height: 1.2;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.5px;
  
  // 渐变色文字效果
  background: linear-gradient(135deg, #ffffff 0%, #ffd700 50%, #ffffff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
  background-size: 200% 200%;
  animation: gradientShift 4s ease-in-out infinite;
  
  // 响应式字体大小
  @media (max-width: 992px) {
    font-size: 48px;
  }
  
  @media (max-width: 768px) {
    font-size: 40px;
  }
  
  @media (max-width: 576px) {
    font-size: 32px;
  }
}

// 副标题样式 - 精致排版
.banner-subtitle {
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  font-size: 24px;
  font-weight: 500;
  margin-bottom: 0;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.4;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.25);
  letter-spacing: 0.2px;
  
  // 响应式字体大小
  @media (max-width: 992px) {
    font-size: 22px;
  }
  
  @media (max-width: 768px) {
    font-size: 20px;
  }
  
  @media (max-width: 576px) {
    font-size: 18px;
  }
}

// 描述文本样式 - 精致排版
.banner-description {
  font-family: 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
  font-size: 18px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 40px;
  line-height: 1.7;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
  letter-spacing: 0.1px;
  
  // 响应式字体大小
  @media (max-width: 992px) {
    font-size: 17px;
  }
  
  @media (max-width: 768px) {
    font-size: 16px;
  }
  
  @media (max-width: 576px) {
    font-size: 15px;
    margin-bottom: 30px;
  }
}

// 内容底部样式
.content-footer {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

// 按钮样式 - 参考旅游系统设计
.banner-button {
  background: var(--gradient-primary);
  border: none;
  border-radius: 25px;
  padding: 14px 40px;
  font-size: 18px;
  font-weight: 600;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.4);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(255, 107, 107, 0.5);
  }
  
  &:active {
    transform: translateY(0);
    box-shadow: 0 2px 4px rgba(255, 107, 107, 0.4);
  }
  
  // 响应式调整
  @media (max-width: 576px) {
    padding: 10px 24px;
    font-size: 14px;
  }
}

// 空状态样式
.empty-banner {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 16px;
  padding: 40px;
}

// 动画效果
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

// 渐变色移动动画
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .banner-content-wrapper {
    bottom: 40px;
    max-width: 600px;
  }
  
  // 在大屏幕上保持完整的粒子效果
  .hero-particles .particle {
    display: block;
  }
}

@media (max-width: 992px) {
  .banner-content-wrapper {
    left: 3%;
    bottom: 25px;
    max-width: 450px;
  }
  
  .glass-card {
    padding: 0;
  }
}

@media (max-width: 768px) {
  .banner-content-wrapper {
    left: 15px;
    right: 15px;
    bottom: 20px;
    max-width: none;
    text-align: left;
  }
  
  .glass-card {
    padding: 0;
  }
  
  .banner-carousel {
    :deep(.el-carousel__arrow) {
      width: 50px;
      height: 50px;
      font-size: 20px;
      margin: 0 10px;
    }
    
    :deep(.el-carousel__indicators) {
      bottom: 20px;
    }
  }
}

@media (max-width: 576px) {
  .banner-carousel {
    border-radius: 8px;
    
    :deep(.el-carousel__container) {
      border-radius: 8px;
    }
    
    :deep(.el-carousel__arrow) {
      width: 40px;
      height: 40px;
      font-size: 18px;
      margin: 0 5px;
    }
    
    :deep(.el-carousel__indicators) {
      bottom: 15px;
      
      .el-carousel__button {
        width: 12px;
        height: 4px;
        margin: 0 4px;
        
        &.is-active {
          width: 24px;
        }
      }
    }
  }
  
  .glass-card {
    padding: 15px;
  }
  
  .banner-content-wrapper {
    bottom: 20px;
    width: 95%;
  }
}

// 深色模式适配
@media (prefers-color-scheme: dark) {
  .banner-carousel {
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  }
  
  .glass-card {
    background: rgba(0, 0, 0, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.1);
  }
}
</style> 