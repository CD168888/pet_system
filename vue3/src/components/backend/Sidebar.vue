<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo">
      <span class="logo-icon">🐾</span>
      <span class="logo-text" v-show="!isCollapsed">宠物之家管理系统</span>
    </div>
    <div class="menu-wrapper">
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false" mode="vertical" class="sidebar-menu"
        text-color="#6E4C1E" active-text-color="#683e35" router>
        
        <!-- 没有子菜单的顶级菜单项 -->
        <el-menu-item 
          v-for="menu in topLevelMenusWithoutChildren" 
          :key="'menu-' + menu.path" 
          :index="getRouterPath(menu.path)"
        >
          <el-icon v-if="menu.icon">
            <component :is="menu.icon" />
          </el-icon>
          <template #title>{{ menu.name }}</template>
        </el-menu-item>
        
        <!-- 有子菜单的顶级菜单项 -->
        <el-sub-menu 
          v-for="menu in topLevelMenusWithChildren" 
          :key="'submenu-' + menu.path" 
          :index="menu.component ? getRouterPath(menu.path) : menu.path"
        >
          <template #title>
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </template>
          
          <!-- 没有子菜单的二级菜单项 -->
          <el-menu-item 
            v-for="child in getChildrenWithoutSubMenus(menu)" 
            :key="'child-' + child.path" 
            :index="getRouterPath(child.path)"
          >
            <el-icon v-if="child.icon">
              <component :is="child.icon" />
            </el-icon>
            <template #title>{{ child.name }}</template>
          </el-menu-item>
          
          <!-- 有子菜单的二级菜单项 -->
          <el-sub-menu 
            v-for="child in getChildrenWithSubMenus(menu)" 
            :key="'child-submenu-' + child.path" 
            :index="child.component ? getRouterPath(child.path) : child.path"
          >
            <template #title>
              <el-icon v-if="child.icon">
                <component :is="child.icon" />
              </el-icon>
              <span>{{ child.name }}</span>
            </template>
            
            <!-- 三级菜单项 -->
            <el-menu-item 
              v-for="grandChild in child.children" 
              :key="'grandchild-' + grandChild.path" 
              :index="getRouterPath(grandChild.path)"
            >
              <el-icon v-if="grandChild.icon">
                <component :is="grandChild.icon" />
              </el-icon>
              <template #title>{{ grandChild.name }}</template>
            </el-menu-item>
          </el-sub-menu>
        </el-sub-menu>
      </el-menu>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 获取菜单数据
const menus = computed(() => {
  return JSON.parse(localStorage.getItem('menus') || '[]')
})

// 筛选没有子菜单的顶级菜单
const topLevelMenusWithoutChildren = computed(() => {
  return menus.value.filter(menu => !menu.children?.length)
})

// 筛选有子菜单的顶级菜单
const topLevelMenusWithChildren = computed(() => {
  return menus.value.filter(menu => menu.children?.length > 0)
})

// 获取没有子子菜单的二级菜单
const getChildrenWithoutSubMenus = (menu) => {
  return menu.children.filter(child => !child.children?.length)
}

// 获取有子子菜单的二级菜单
const getChildrenWithSubMenus = (menu) => {
  return menu.children.filter(child => child.children?.length > 0)
}

// 处理路由路径，确保与router/index.js中的generateAsyncRoutes函数处理方式一致
const getRouterPath = (path) => {
  if (!path) return ''
  const formattedPath = path.startsWith('/') ? path : `/${path}`
  return `/back${formattedPath}`
}

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return '/back' + meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%; 
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f5e9 0%, #c8e6c9 100%);
  display: flex;
  flex-direction: column;
  width: 220px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.08);
  
  &.is-collapsed {
    width: 64px;
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin: 0;
      }
    }

    :deep(.el-menu) {
      .el-sub-menu__title span,
      .el-menu-item span {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }
  
  .logo {
    height: 60px;
    flex-shrink: 0;
    line-height: 60px;
    text-align: center;
    background: rgba(129, 199, 132, 0.15);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(129, 199, 132, 0.2);
    display: flex;
    align-items: center;
    padding: 0 16px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    .logo-icon {
      font-size: 24px;
      margin-right: 8px;
      transition: margin 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      animation: bounce 2s infinite;
    }
    
    .logo-text {
      color: #2E7D32;
      font-size: 18px;
      font-weight: 600;
      font-family: 'Nunito Sans', sans-serif;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.2s;
      background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(129, 199, 132, 0.5);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    background: transparent;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .el-menu-item, .el-sub-menu__title {
      height: 50px;
      line-height: 50px;
      color: rgba(46, 125, 50, 0.8);
      background: transparent;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      span {
        opacity: 1;
        transition: opacity 0.3s;
      }
      
      &:hover {
        background: rgba(129, 199, 132, 0.15) !important;
        color: #2E7D32;
      }
    }

    .el-menu-item.is-active {
      background: rgba(129, 199, 132, 0.25) !important;
      color: #2E7D32 !important;
      box-shadow: 0 2px 8px rgba(129, 199, 132, 0.3);
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 3px;
        height: 100%;
        background: #66bb6a;
      }
    }

    .el-sub-menu {
      &.is-opened {
        > .el-sub-menu__title {
          color: #2E7D32;
          background: rgba(129, 199, 132, 0.1) !important;
        }
      }

      .el-menu {
        background: rgba(129, 199, 132, 0.05);
        
        .el-menu-item {
          background: transparent;
          
          &:hover {
            background: rgba(129, 199, 132, 0.15) !important;
          }
          
          &.is-active {
            background: rgba(129, 199, 132, 0.25) !important;
          }
        }
      }
    }

    // 折叠状态下的弹出菜单样式
    &.el-menu--collapse {
      .el-sub-menu {
        &.is-opened {
          > .el-sub-menu__title {
            background: transparent !important;
          }
        }
      }
    }
  }

  .el-icon {
    vertical-align: middle;
    margin-right: 5px;
    width: 24px;
    text-align: center;
    color: inherit;
  }

  span {
    vertical-align: middle;
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}
</style> 