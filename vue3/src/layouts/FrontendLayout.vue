<template>
    <div class="frontend-layout">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-container">
          <div class="logo">
            <router-link to="/">
              <img src="@/assets/宠物手掌.svg" alt="宠物之家" class="logo-image" />
              <span class="logo-text">宠物之家</span>
            </router-link>
          </div>
          
          <el-menu 
            :default-active="activeIndex"
            class="main-menu"
            mode="horizontal"
            :ellipsis="false"
            :router="true"
            background-color="#FFFAF0"
            text-color="#6E4C1E"
            active-text-color="#FF9800"
          >
            <el-menu-item index="/">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-menu-item index="/pet">
              <el-icon><Promotion /></el-icon>
              <span>宠物领养</span>
            </el-menu-item>
            
            <el-menu-item index="/product">
              <el-icon><ShoppingBag /></el-icon>
              <span>宠物用品</span>
            </el-menu-item>
            
            <el-menu-item index="/service">
              <el-icon><Service /></el-icon>
              <span>宠物服务</span>
            </el-menu-item>

            <el-menu-item index="/boarding/apply">
              <el-icon><House /></el-icon>
              <span>申请寄养</span>
            </el-menu-item>
            
            <el-menu-item index="/training">
              <el-icon><Trophy /></el-icon>
              <span>宠物训练</span>
            </el-menu-item>
            
            <div class="flex-grow"></div>
            
            <div class="search-box">
              <el-input 
                v-model="searchQuery"
                placeholder="搜索宠物、商品或服务..." 
                class="rounded-search"
                size="small"
                @keyup.enter="handleSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
            
            <!-- 未登录状态 -->
            <template v-if="!isLoggedIn">
              <el-menu-item index="/login" class="login-item">
                <el-button type="text" size="small">
                  <el-icon><Key /></el-icon>
                  <span>登录</span>
                </el-button>
              </el-menu-item>
              <el-menu-item index="/register" class="register-item">
                <el-button type="primary" size="small" plain>
                  <span>注册</span>
                </el-button>
              </el-menu-item>
            </template>
            
            <!-- 已登录状态 -->
            <template v-else>
              <el-sub-menu index="5" popper-class="user-dropdown">
                <template #title>
                  <div class="user-avatar">
                    <el-avatar :size="32" :src="'/api'+userStore.userInfo.avatar" />
                  </div>
                </template>
                <el-menu-item index="/profile">
                  <el-icon><User /></el-icon>
                  <span>个人中心</span>
                </el-menu-item>
                <el-menu-item index="/cart">
                  <el-icon><ShoppingCart /></el-icon>
                  <span>我的购物车</span>
                  <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge" />
                </el-menu-item>
                <el-menu-item index="/order">
                  <el-icon><Document /></el-icon>
                  <span>我的订单</span>
                </el-menu-item>
                <el-menu-item index="/adoption">
                  <el-icon><House /></el-icon>
                  <span>我的领养</span>
                </el-menu-item>
                <el-menu-item index="/my/boarding">
                  <el-icon><House /></el-icon>
                  <span>我的寄养</span>
                </el-menu-item>
                <el-menu-item index="/service/my-appointments">
                  <el-icon><Calendar /></el-icon>
                  <span>我的预约</span>
                </el-menu-item>
                <el-menu-item index="/training/my-appointments">
                  <el-icon><Trophy /></el-icon>
                  <span>我的训练</span>
                </el-menu-item>
                <el-menu-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
          
          <!-- 移动端菜单按钮 -->
          <div class="mobile-toggle-btn" @click="toggleMobileMenu">
            <el-icon><Menu /></el-icon>
          </div>
        </div>
      </header>
  
      <!-- 移动端菜单 -->
      <el-drawer
        v-model="showMobileMenu"
        title="宠物之家"
        direction="ltr"
        size="80%"
        :with-header="true"
      >
        <div class="mobile-search-box">
          <el-input 
            v-model="searchQuery"
            placeholder="搜索宠物、商品或服务..." 
            clearable
            @keyup.enter="handleMobileSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      
        <el-menu 
          :default-active="activeIndex"
          class="mobile-menu"
          :router="true"
          @select="closeMobileMenu"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <el-menu-item index="/pet">
            <el-icon><Promotion /></el-icon>
            <span>宠物领养</span>
          </el-menu-item>
          
          <el-menu-item index="/product">
            <el-icon><ShoppingBag /></el-icon>
            <span>宠物用品</span>
          </el-menu-item>
          
          <el-menu-item index="/service">
            <el-icon><Service /></el-icon>
            <span>宠物服务</span>
          </el-menu-item>
          
          <el-menu-item index="/boarding/apply">
            <el-icon><House /></el-icon>
            <span>申请寄养</span>
          </el-menu-item>
          
          <el-menu-item index="/training">
            <el-icon><Trophy /></el-icon>
            <span>宠物训练</span>
          </el-menu-item>
          
          <el-menu-item index="/wiki">
            <el-icon><FirstAid /></el-icon>
            <span>宠物百科</span>
          </el-menu-item>
          
          <el-menu-item index="/guide">
            <el-icon><FirstAid /></el-icon>
            <span>饲养指南</span>
          </el-menu-item>
          
          <el-menu-item index="/adoption" v-if="isLoggedIn">
            <el-icon><House /></el-icon>
            <span>我的领养</span>
          </el-menu-item>
          
          <el-menu-item index="/order" v-if="isLoggedIn">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          
          <el-menu-item index="/cart" v-if="isLoggedIn">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的购物车</span>
            <el-badge :value="cartCount" :hidden="cartCount === 0" class="cart-badge" />
          </el-menu-item>
          
          <el-menu-item index="/my/boarding" v-if="isLoggedIn">
            <el-icon><House /></el-icon>
            <span>我的寄养</span>
          </el-menu-item>
          
          <el-menu-item index="/service/my-appointments" v-if="isLoggedIn">
            <el-icon><Calendar /></el-icon>
            <span>我的预约</span>
          </el-menu-item>
          
          <el-menu-item index="/training/my-appointments" v-if="isLoggedIn">
            <el-icon><Trophy /></el-icon>
            <span>我的训练</span>
          </el-menu-item>
          
          <el-menu-item index="/health-records" v-if="isLoggedIn">
            <el-icon><FirstAid /></el-icon>
            <span>健康档案</span>
          </el-menu-item>
        </el-menu>
        
        <div class="mobile-user-actions">
          <template v-if="isLoggedIn">
            <el-button @click="goToProfile" type="info" plain icon="User" block>个人中心</el-button>
            <el-button @click="handleLogoutMobile" type="warning" plain icon="SwitchButton" block class="logout-btn">
              退出登录
            </el-button>
          </template>
          <template v-else>
            <el-button @click="goToLogin" type="primary" plain block>登录</el-button>
            <el-button @click="goToRegister" type="warning" block>注册</el-button>
          </template>
        </div>
      </el-drawer>
  
      <!-- 主要内容区域 -->
      <main class="main-content">
        <router-view />
      </main>
  
      <!-- 页脚 -->
      <footer class="footer">
        
        <div class="footer-bottom">
          <p>&copy; 2025 宠物之家 - 为宠物提供温暖的家</p>
 
        </div>
      </footer>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, reactive, onMounted } from 'vue'
  import { useUserStore } from '@/store/user'
  import { useRouter, useRoute } from 'vue-router'
  import { 
    ShoppingCart, 
    User, 
    Key, 
    Edit, 
    SwitchButton, 
    Menu, 
    Search, 
    ArrowDown, 
    ArrowRight,
    Location,
    Phone,
    Message,
    Star,
    Link,
    HomeFilled,
    ShoppingBag,
    Service,
    Document,
    Pet,
    House,
    Calendar,
    Trophy,
    Promotion,
    FirstAid
  } from '@element-plus/icons-vue'
  
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()
  const searchQuery = ref('')
  const showMobileMenu = ref(false)
  const cartCount = ref(0) // 这里应该从购物车store中获取
  const activeIndex = ref('/')
  
  const isLoggedIn = computed(() => !!userStore.token)
  
  onMounted(() => {
    // 设置当前激活的菜单项
    activeIndex.value = route.path
  })
  
  // 处理搜索
  const handleSearch = () => {
    if (searchQuery.value.trim()) {
      router.push({ 
        path: '/search', 
        query: { q: searchQuery.value.trim() } 
      })
      searchQuery.value = ''
      closeMobileMenu() // 如果是移动菜单展开状态，关闭菜单
    }
  }
  
  const handleMobileSearch = () => {
    handleSearch()
  }
  
  // 处理退出登录
  const handleLogout = () => {
    userStore.clearUserInfo()
    router.push('/login')
  }
  
  const handleLogoutMobile = () => {
    handleLogout()
    closeMobileMenu()
  }
  
  // 移动端菜单控制
  const toggleMobileMenu = () => {
    showMobileMenu.value = !showMobileMenu.value
  }
  
  const closeMobileMenu = () => {
    showMobileMenu.value = false
  }
  
  // 移动端导航
  const goToProfile = () => {
    router.push('/profile')
    closeMobileMenu()
  }
  
  const goToLogin = () => {
    router.push('/login')
    closeMobileMenu()
  }
  
  const goToRegister = () => {
    router.push('/register')
    closeMobileMenu()
  }
  </script>
  
  <style scoped>
  .frontend-layout {
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    font-family: 'Open Sans', 'Nunito Sans', Arial, sans-serif;
    background:
      linear-gradient(to bottom, transparent 0%, transparent 1%, #fff 5%, #fff 100%),
      linear-gradient(135deg, #fff3e0 0%, #ffe0b2 8%, #ffecb3 15%, #ffebee 20%, #fff 30%, #fff 100%);
    background-attachment: fixed;
    position: relative;

    &::before {
      content: '';
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background-image:
        radial-gradient(circle at 20% 30%, rgba(255, 193, 7, 0.15) 0%, transparent 50%),
        radial-gradient(circle at 80% 70%, rgba(244, 67, 54, 0.15) 0%, transparent 50%),
        radial-gradient(circle at 50% 50%, rgba(255, 235, 59, 0.1) 0%, transparent 50%);
      pointer-events: none;
      z-index: 0;
    }
  }  
  /* 顶部导航栏 - 灵动岛效果 */
  .header {
    position: sticky;
    top: 0;
    z-index: 1000;
    height: 80px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 20px;
  }
  
  .header-container {
    display: flex;
    align-items: center;
    max-width: 1400px;
    width: 100%;
    margin: 0 auto;
    padding: 0 30px;
    height: 56px;
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(20px) saturate(180%);
    -webkit-backdrop-filter: blur(20px) saturate(180%);
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 30px;
  }
  
  .logo {
    margin-right: 30px;
  }
  
  .logo a {
    display: flex;
    align-items: center;
    text-decoration: none;
  }
  
  .logo-image {
    height: 32px;
    margin-right: 10px;
    transition: transform 0.3s;
  }
  
  .logo:hover .logo-image {
    transform: scale(1.1);
  }
  
  .logo-text {
    font-family: 'Nunito Sans', sans-serif;
    font-size: 20px;
    font-weight: 600;
    background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin: 0;
  }
  
  /* Element Plus Menu 样式覆盖 */
  .main-menu {
    flex: 1;
    border-bottom: none !important;
    background-color: transparent !important;
  }
  
  .main-menu :deep(.el-menu-item) {
    color: #5d6d7e;
    font-size: 16px;
    font-weight: 500;
    border-bottom: none !important;
    padding: 0 16px !important;
    height: 64px !important;
    transition: all 0.3s ease;
  }
  
  .main-menu :deep(.el-menu-item:hover,
                   .el-menu-item.is-active) {
    color: #ff9800;
    border-radius: 8px;
  }
  
  .main-menu :deep(.el-sub-menu__title) {
    color: #5d6d7e;
    font-size: 16px;
    font-weight: 500;
    border-bottom: none !important;
    padding: 0 16px !important;
    height: 64px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: flex-start !important;
    transition: all 0.3s ease;
  }
  
  .main-menu :deep(.el-sub-menu__title:hover,
                   .el-sub-menu.is-active > .el-sub-menu__title) {
    color: #ff9800;
    border-radius: 8px;
  }
  
  .main-menu :deep(.el-sub-menu__icon-arrow) {
    position: static !important;
    margin-left: 8px !important;
    margin-top: 0 !important;
    margin-right: 0 !important;
    order: 1 !important;
    color: #909399;
  }
  
  .main-menu :deep(.el-sub-menu__title .el-icon) {
    order: 0 !important;
    margin-right: 8px !important;
    color: #7e8e9f;
    transition: all 0.3s ease;
  }
  
  .main-menu :deep(.el-sub-menu__title span) {
    order: 0 !important;
  }
  
  .main-menu :deep(.el-menu-item:hover .el-icon,
                   .el-menu-item.is-active .el-icon,
                   .el-sub-menu__title:hover .el-icon,
                   .el-sub-menu.is-active > .el-sub-menu__title .el-icon) {
    color: #ff9800;
  }
  
  /* 为菜单添加图标和文字之间的间距 */
  .main-menu :deep(.el-icon), 
  .mobile-menu :deep(.el-icon) {
    margin-right: 4px; /* 减小图标与文字的间距 */
    width: 16px; /* 减小图标大小 */
    height: 16px; /* 减小图标大小 */
  }
  
  /* 用户头像 */
  .user-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  
  .user-avatar :deep(.el-avatar) {
    margin-right: 0;
    border: 2px solid #FFEED6;
    transition: all 0.3s;
    width: 28px; /* 减小头像大小 */
    height: 28px; /* 减小头像大小 */
  }
  
  .user-avatar:hover :deep(.el-avatar) {
    transform: scale(1.1);
    border-color: #FFCD70;
  }
  
  /* 购物车徽章 */
  .cart-badge :deep(.el-badge__content) {
    background-color: #FF9800;
    border: none;
  }
  
  /* 弹性增长空间，用于推动搜索框和用户操作到右侧 */
  .flex-grow {
    flex-grow: 1;
  }
  
  /* 搜索框样式 */
  .search-box {
    margin: 0 16px;
    width: 250px;
    display: flex;
    align-items: center;
    height: 100%;
  }
  
  .rounded-search {
    width: 100%;
  }
  
  .rounded-search :deep(.el-input__wrapper) {
    border-radius: 20px;
    background-color: rgba(255, 255, 255, 0.6);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(10px);
    transition: all 0.3s;
  }
  
  .rounded-search :deep(.el-input__wrapper:hover) {
    background-color: rgba(255, 255, 255, 0.8);
    box-shadow: 0 0 0 1px rgba(255, 152, 0, 0.3);
  }
  
  .rounded-search :deep(.el-input__wrapper.is-focus) {
    background-color: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 0 1px rgba(255, 152, 0, 0.4);
  }
  
  /* 用户头像 */
  .user-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    padding: 8px 14px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.4);
    transition: all 0.3s ease;
  }
  
  .user-avatar:hover {
    background: rgba(255, 152, 0, 0.15);
    border-color: rgba(255, 152, 0, 0.3);
    box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  }
  
  .user-avatar :deep(.el-avatar) {
    margin-right: 0;
    width: 28px;
    height: 28px;
  }
  
  /* 登录注册按钮样式 */
  .login-item :deep(.el-button) {
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.4);
    color: #5d6d7e;
    transition: all 0.3s ease;
  }
  
  .login-item :deep(.el-button:hover) {
    background: rgba(255, 152, 0, 0.15);
    border-color: rgba(255, 152, 0, 0.3);
    color: #ff9800;
    box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
  }
  
  .register-item :deep(.el-button) {
    border-radius: 20px;
    background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
    border: none;
    color: #fff;
    padding: 8px 20px;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(255, 152, 0, 0.3);
    transition: all 0.3s ease;
  }
  
  .register-item :deep(.el-button:hover) {
    background: linear-gradient(135deg, #f57c00 0%, #e65100 100%);
    box-shadow: 0 6px 16px rgba(255, 152, 0, 0.4);
    transform: translateY(-2px);
  }
  
  /* 移动端菜单按钮 */
  .mobile-toggle-btn {
    display: none;
    font-size: 24px;
    cursor: pointer;
    color: #6E4C1E;
  }
  
  /* 移动端搜索框 */
  .mobile-search-box {
    padding: 16px;
    border-bottom: 1px solid #EAEAEA;
  }
  
  /* 移动端菜单样式 */
  .mobile-menu {
    border-right: none;
  }
  
  .mobile-user-actions {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    border-top: 1px solid #EAEAEA;
    margin-top: auto;
  }
  
  .logout-btn {
    margin-top: 8px;
  }
  
  /* 主要内容区域 */
  .main-content {
    flex: 1;
    padding: 2rem;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
  }
  
  /* 主内容区域 */
  .main-content {
    flex: 1;
    padding: 2rem 20px;
    max-width: 1400px;
    margin: 0 auto;
    width: 100%;
    position: relative;
    z-index: 1;
  }
  
  /* 页脚样式 */
  .footer {
    background: rgba(255, 255, 255, 0.75);
    backdrop-filter: blur(20px) saturate(180%);
    -webkit-backdrop-filter: blur(20px) saturate(180%);
    border-top: 1px solid rgba(255, 255, 255, 0.3);
    color: #5d6d7e;
    margin-top: auto;
    position: relative;
    z-index: 1;
  }
  
  .footer-bottom {
    padding: 20px;
    text-align: center;
    color: #909399;
  }
  
  .footer-bottom p {
    margin: 0;
    font-size: 14px;
  }
  
  /* 响应式设计 */
  @media (max-width: 992px) {
    .main-menu {
      display: none !important;
    }
    
    .mobile-toggle-btn {
      display: block;
      margin-left: auto;
    }
    
    .header-container {
      height: 64px;
      padding: 0 16px;
      max-width: 700px;
    }
    
    .logo {
      margin-right: 20px;
    }
    
    .logo-text {
      font-size: 18px;
    }
    
    .logo-image {
      height: 28px;
    }
    
    .search-box {
      margin: 0 12px;
      width: 200px;
    }
  }
  
  @media (max-width: 768px) {
    .header-container {
      height: 56px;
      max-width: 500px;
      border-radius: 28px;
    }
    
    .logo-text {
      font-size: 16px;
    }
    
    .logo-image {
      height: 24px;
    }
    
    .search-box {
      display: none;
    }
    
    .main-content {
      padding: 1.5rem 16px;
    }
    
    .footer-bottom {
      padding: 16px;
    }
  }
  
  @media (max-width: 480px) {
    .header-container {
      max-width: calc(100% - 32px);
      padding: 0 12px;
    }
    
    .main-content {
      padding: 1rem 12px;
    }
    
    .logo-text {
      display: none;
    }
    
    .logo-image {
      height: 30px;
      margin-right: 0;
    }
  }
  </style>