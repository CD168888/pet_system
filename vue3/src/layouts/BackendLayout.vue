<template>
    <div class="backend-layout">
      <!-- 侧边栏 -->
      <Sidebar />
  
      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 顶部导航栏 -->
        <Navbar @logout="handleLogout" />
  
        <!-- 页面内容 -->
        <div class="content-container">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </div>
      
      <!-- 页面装饰 -->
      <div class="page-decoration">
        <div class="decoration-bubble bubble-1"></div>
        <div class="decoration-bubble bubble-2"></div>
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter } from 'vue-router'
  import Sidebar from '@/components/backend/Sidebar.vue'
  import Navbar from '@/components/backend/Navbar.vue'
  const userStore = useUserStore()
  const router = useRouter()
  
  const isAdmin = computed(() => userStore.role === 'admin')
  
  const handleLogout = () => {
    userStore.clearUserInfo()
    router.push('/login')
  }
  </script>
  
  <style lang="scss" scoped>
  .backend-layout {
    display: flex;
    height: 100vh;
    min-height: 100vh;
    background: 
      linear-gradient(to bottom, transparent 0%, transparent 5%, #fff 20%),
      linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
    background-attachment: fixed;
    overflow: hidden;
    position: relative;

    &::before {
      content: '';
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-image: 
        radial-gradient(circle at 20% 30%, rgba(129, 199, 132, 0.15) 0%, transparent 50%),
        radial-gradient(circle at 80% 70%, rgba(100, 181, 246, 0.15) 0%, transparent 50%),
        radial-gradient(circle at 50% 50%, rgba(255, 238, 88, 0.1) 0%, transparent 50%);
      pointer-events: none;
      z-index: 0;
    }
  }
  
  /* 侧边栏样式将在Sidebar.vue中处理 */
  
  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100%;
    background: transparent;
    position: relative;
    z-index: 1;
  }
  
  .content-container {
    flex: 1;
    overflow-y: auto;
    position: relative;
    background-color: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;
    
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: rgba(129, 199, 132, 0.5);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background-color: transparent;
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
      background: linear-gradient(135deg, #e8f5e9 0%, #66bb6a 50%, #43a047 100%);
      opacity: 0.05;
      
      &.bubble-1 {
        width: 300px;
        height: 300px;
        bottom: -150px;
        right: -100px;
        animation: float 15s infinite ease-in-out;
      }
      
      &.bubble-2 {
        width: 200px;
        height: 200px;
        top: 10%;
        left: -50px;
        animation: float 18s infinite ease-in-out;
      }
    }
    
    .decoration-paw {
      position: absolute;
      font-size: 40px;
      opacity: 0.03;
      
      &.paw-1 {
        bottom: 5%;
        right: 5%;
        animation: float 15s infinite ease-in-out;
      }
      
      &.paw-2 {
        top: 20%;
        right: 30%;
        animation: rotate 18s infinite linear;
      }
    }
  }
  
  /* 过渡动画 */
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }

  @keyframes float {
    0%, 100% {
      transform: translateY(0);
    }
    50% {
      transform: translateY(-15px);
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
  </style>