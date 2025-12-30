<template>
  <div class="boarding-apply-page">
    <div class="page-banner">
      <div class="container">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>申请寄养</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1>宠物寄养申请</h1>
        <p>为您的爱宠提供专业舒适的寄养服务</p>
      </div>
      <div class="banner-decoration">
        <div class="decoration-paw paw-1">🐾</div>
        <div class="decoration-paw paw-2">🐾</div>
        <div class="decoration-paw paw-3">🐾</div>
      </div>
    </div>
    
    <div class="boarding-content">
      <div class="boarding-main">
        <div class="booking-grid">
          <div class="service-info">
            <h2 class="section-title">寄养服务说明</h2>
            <div class="service-header">
              <span class="service-category">专业宠物寄养</span>
            </div>
            <h2>宠物之家寄养服务</h2>
            <div class="service-meta">
              <div class="meta-item">
                <el-icon class="icon"><Calendar /></el-icon>
                <span>全天候服务</span>
              </div>
              <div class="meta-item">
                <el-icon class="icon"><Location /></el-icon>
                <span>安全舒适环境</span>
              </div>
            </div>
            <div class="service-description">
              我们提供专业的宠物寄养服务，满足您外出时宠物的照料需求。经验丰富的宠物看护员会确保您的爱宠在舒适、安全的环境中得到全面的照顾。
            </div>
            
            <div class="price-info-content">
              <h3>
                <el-icon class="icon"><InfoFilled /></el-icon>
                价格说明
              </h3>
              <ul>
                <li>猫类：80元/天</li>
                <li>狗类：100元/天（小型犬）、150元/天（中大型犬）</li>
                <li>兔子：50元/天</li>
                <li>鸟类：40元/天</li>
                <li>其他宠物：根据实际情况定价</li>
              </ul>
              <p class="price-note">最终价格由管理员确认后生效</p>
            </div>
          </div>
          
          <div class="booking-form">
            <h2>寄养申请</h2>
            <el-form 
              ref="formRef" 
              :model="boardingForm" 
              :rules="rules" 
              label-width="100px"
            >
              <el-form-item label="宠物名称" prop="petName">
                <el-input v-model="boardingForm.petName" placeholder="请输入您的宠物名称">
                  <template #prefix>
                    <el-icon><Pet /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="宠物类型" prop="petType">
                <el-cascader 
                  v-model="selectedCategoryId" 
                  :options="categoriesTree"
                  :props="{
                    checkStrictly: true,
                    value: 'id',
                    label: 'name',
                    emitPath: false
                  }"
                  placeholder="请选择宠物类型" 
                  clearable 
                  filterable
                  style="width: 100%"
                  @change="handleCascaderChange"
                >
                  <template #prefix>
                    <el-icon><Star /></el-icon>
                  </template>
                </el-cascader>
              </el-form-item>
              
              <el-form-item label="寄养时间" prop="dateRange">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
            
                  style="width: 100%"
                  @change="handleDateChange"
                  :disabled-date="disabledDate"
                >
                  <template #prefix>
                    <el-icon><Calendar /></el-icon>
                  </template>
                </el-date-picker>
              </el-form-item>
              
              <el-form-item label="特殊要求" prop="requirements">
                <el-input
                  v-model="boardingForm.requirements"
                  type="textarea"
                  :rows="4"
                  placeholder="请描述您对寄养服务的特殊要求（如饮食习惯、药物、行为特点等）"
                />
              </el-form-item>
              
              <el-form-item class="form-footer">
                <el-button type="primary" class="submit-btn" @click="submitForm" :loading="submitting">
                  <el-icon><Check /></el-icon>
                  提交申请
                </el-button>
                <el-button class="reset-btn" @click="resetForm">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
        
        <div class="notice-card">
          <h3>
            <el-icon class="icon"><Warning /></el-icon>
            寄养须知
          </h3>
          <ul>
            <li>请提前3天预约寄养服务，以确保有充足的安排时间</li>
            <li>寄养时请带上宠物的健康证明和疫苗接种记录</li>
            <li>如有特殊要求（如饮食、药物等），请在申请时详细说明</li>
            <li>寄养期间可随时联系我们了解宠物状况</li>
            <li>如需延长或缩短寄养时间，请提前24小时通知我们</li>
          </ul>
        </div>
      </div>
    </div>
    
    <!-- 登录提示对话框 -->
    <el-dialog
      v-model="loginDialogVisible"
      title="需要登录"
      width="400px"
      center
    >
      <div class="login-dialog-content">
        <p>您需要登录后才能申请寄养服务</p>
        <p>请先登录或注册一个账号</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="loginDialogVisible = false">取消</el-button>
          <el-button type="primary" class="login-btn" @click="goToLogin">去登录</el-button>
          <el-button type="success" class="register-btn" @click="goToRegister">去注册</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Pet, Calendar, Star, Location, InfoFilled, Check, Refresh, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const loginDialogVisible = ref(false)
const dateRange = ref(null)

// 添加分类相关数据
const categoriesList = ref([])
const categoriesTree = ref([])
// 用于级联选择器绑定的临时变量
const selectedCategoryId = ref('')

// 寄养表单数据
const boardingForm = reactive({
  petName: '',
  petType: '',
  startTime: '',
  endTime: '',
  requirements: '',
  price: 0,
  status: '已申请',
  userId: ''
})

// 获取分类列表和树形结构
const fetchCategories = async () => {
  try {
    await request.get('/pet-category/tree', {}, {
      onSuccess: (res) => {
        categoriesTree.value = res
        // 同时保留平铺的分类列表
        flattenCategories(res)
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

// 表单验证规则
const rules = {
  petName: [
    { required: true, message: '请输入宠物名称', trigger: 'blur' },
    { min: 1, max: 20, message: '宠物名称长度应在1-20个字符之间', trigger: 'blur' }
  ],
  petType: [
    { required: true, message: '请选择宠物类型', trigger: 'change' }
  ],
  dateRange: [
    { 
      required: true, 
      message: '请选择寄养时间段', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!dateRange.value) {
          callback(new Error('请选择寄养时间段'))
        } else {
          callback()
        }
      }
    }
  ]
}

// 禁用日期（今天之前的日期不可选）
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 86400000 = 24 * 60 * 60 * 1000
}

// 处理日期范围选择变化
const handleDateChange = (val) => {
  if (val) {
    boardingForm.startTime = val[0]
    boardingForm.endTime = val[1]
  } else {
    boardingForm.startTime = ''
    boardingForm.endTime = ''
  }
}

// 根据分类名称计算价格
const adjustPriceByCategory = (petType) => {
  switch (petType) {
    case '猫':
      boardingForm.price = 80
      break
    case '狗':
      boardingForm.price = 120 // 默认中型犬价格
      break
    case '兔子':
      boardingForm.price = 50
      break
    case '鸟类':
      boardingForm.price = 40
      break
    default:
      boardingForm.price = 60
  }
}

// 计算天数和预估价格
const calculateDaysAndPrice = () => {
  if (!boardingForm.startTime || !boardingForm.endTime) {
    return { days: 0, price: 0 }
  }
  
  const start = new Date(boardingForm.startTime)
  const end = new Date(boardingForm.endTime)
  const diffTime = Math.abs(end - start)
  const days = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  let pricePerDay = 0
  // 根据宠物类型判断价格
  switch (boardingForm.petType) {
    case '猫':
      pricePerDay = 80
      break
    case '狗':
      pricePerDay = 120 // 默认中型犬价格
      break
    case '兔子':
      pricePerDay = 50
      break
    case '鸟类':
      pricePerDay = 40
      break
    default:
      pricePerDay = 60
  }
  
  return {
    days,
    price: days * pricePerDay
  }
}

// 检查用户登录状态
const checkLogin = () => {
  if (!userStore.isLoggedIn) {
    loginDialogVisible.value = true
    return false
  }
  return true
}

// 提交表单
const submitForm = async () => {
  if (!checkLogin()) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请完善表单信息')
      return
    }
    
    if (!boardingForm.startTime || !boardingForm.endTime) {
      ElMessage.error('请选择寄养时间段')
      return
    }
    
    // 验证日期
    const startDate = new Date(boardingForm.startTime)
    const endDate = new Date(boardingForm.endTime)
    const now = new Date()
    
    if (startDate < now) {
      ElMessage.error('寄养开始时间不能早于当前时间')
      return
    }
    
    if (endDate <= startDate) {
      ElMessage.error('寄养结束时间必须晚于开始时间')
      return
    }
    
    // 计算预估价格
    const { price } = calculateDaysAndPrice()
    boardingForm.price = price
    boardingForm.userId = userStore.userInfo.id
    
    // 提交表单前确认
    const { days, price: estimatedPrice } = calculateDaysAndPrice()
    
    try {
      await ElMessageBox.confirm(
        `您选择了${days}天的寄养服务，预估费用为¥${estimatedPrice}元（最终价格以管理员确认为准）。确定提交申请吗？`,
        '确认提交',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      
      submitting.value = true
      
      await request.post('/boarding', boardingForm, {
        successMsg: '寄养申请提交成功，请等待审核',
        onSuccess: () => {
          ElMessageBox.confirm(
            '寄养申请已提交，您可以前往"我的寄养"查看申请状态',
            '申请成功',
            {
              confirmButtonText: '查看我的寄养',
              cancelButtonText: '返回首页',
              type: 'success'
            }
          ).then(() => {
            router.push('/my/boarding')
          }).catch(() => {
            router.push('/')
          })
          
          resetForm()
        }
      })
    } catch (error) {
      if (error === 'cancel') {
        // 用户取消，不做处理
      } else {
        console.error('提交寄养申请失败:', error)
        ElMessage.error('提交失败，请稍后重试')
      }
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  dateRange.value = null
  selectedCategoryId.value = ''
  boardingForm.startTime = ''
  boardingForm.endTime = ''
  boardingForm.requirements = ''
  boardingForm.petType = ''
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 导航到登录页
const goToLogin = () => {
  loginDialogVisible.value = false
  router.push({
    path: '/login',
    query: { redirect: router.currentRoute.value.fullPath }
  })
}

// 导航到注册页
const goToRegister = () => {
  loginDialogVisible.value = false
  router.push('/register')
}

onMounted(() => {
  // 初始化时检查登录状态和获取分类数据
  checkLogin()
  fetchCategories()
})

// 处理级联选择器变化
const handleCascaderChange = (categoryId) => {
  if (categoryId) {
    // 在分类列表中查找对应的分类名称
    const findCategoryName = (categories) => {
      for (const category of categories) {
        if (category.id === categoryId) {
          return category.name
        }
        if (category.children) {
          const name = findCategoryName(category.children)
          if (name) return name
        }
      }
      return ''
    }
    // 获取分类名称并赋值给petType
    const categoryName = findCategoryName(categoriesTree.value)
    // 更新categoryName后，调整价格
    boardingForm.petType = categoryName
    adjustPriceByCategory(categoryName)
  } else {
    boardingForm.petType = ''
  }
}
</script>

<style lang="scss" scoped>
.boarding-apply-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
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
    background: linear-gradient(135deg, #e8f5e9 0%, #b3e5fc 30%, #fff9c4 60%, #ffccbc 100%);
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

.boarding-content {
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
  padding: 0 20px;
}



.page-banner {
  background: linear-gradient(135deg, #FFB6C1 0%, #FFEE93 100%);
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
      
      .el-breadcrumb__item {
        a {
          color: rgba(255, 255, 255, 0.8);
          text-decoration: none;
          
          &:hover {
            color: white;
          }
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

.boarding-main {
  margin-top: 20px;
}

.booking-grid {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  margin: 0 auto 40px;
  max-width: 1400px;
  padding: 0 20px;
  
  @media (max-width: 992px) {
    grid-template-columns: 1fr;
  }
  
  .service-info, .booking-form {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 24px;
    padding: 25px;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 15px 20px rgba(0, 0, 0, 0.15);
    }
    
    .section-title {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 22px;
      color: #2e7d32;
      margin: 0 0 20px;
      position: relative;
      padding-left: 25px;
      font-weight: 600;
      
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
    
    .service-header {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .service-category {
        display: inline-block;
        padding: 8px 16px;
        background-color: rgba(102, 187, 106, 0.2);
        color: #43a047;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        transition: all 0.3s ease;
      }
    }
    
    h2 {
      font-size: 24px;
      color: #2e7d32;
      margin: 0 0 15px;
    }
    
    .service-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      margin-bottom: 15px;
      
      .meta-item {
        display: flex;
        align-items: center;
        color: #666;
        font-size: 14px;
        
        .icon {
          margin-right: 6px;
          color: #43a047;
        }
      }
    }
    
    .service-description {
      line-height: 1.8;
      color: #666;
      font-size: 15px;
      margin-bottom: 20px;
    }
    
    .price-info-content {
      background-color: rgba(232, 245, 233, 0.5);
      padding: 20px;
      border-radius: 10px;
      border-left: 4px solid #66bb6a;
      margin-top: 20px;
      
      h3 {
        color: #43a047;
        margin: 0 0 15px;
        font-size: 16px;
        display: flex;
        align-items: center;
        
        .icon {
          margin-right: 8px;
          color: #43a047;
        }
      }
      
      ul {
        padding-left: 20px;
        margin: 0 0 15px 0;
        
        li {
          margin-bottom: 8px;
          color: #555;
          line-height: 1.5;
          
          &:last-child {
            margin-bottom: 0;
          }
        }
      }
      
      .price-note {
        font-style: italic;
        color: #999;
        font-size: 14px;
        margin: 0;
      }
    }
  }
  
  .booking-form {
    flex: 1;
    
    h2 {
      font-family: 'Nunito Sans', sans-serif;
      font-size: 22px;
      color: #2e7d32;
      margin: 0 0 25px;
      position: relative;
      padding-left: 25px;
      font-weight: 600;
      
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
    
    :deep(.el-form-item__label) {
      color: #666;
      font-weight: 500;
    }
    
    :deep(.el-form .el-form-item) {
      margin-bottom: 60px;
    }
    
    :deep(.el-form-item.form-footer) {
      margin-top: 30px;
      display: flex;
      justify-content: flex-end;
      gap: 20px;
      
      & > div {
        display: flex;
        justify-content: flex-end;
        gap: 20px;
        width: 100%;
      }
    }
  }
}

.notice-card {
  background: rgba(232, 245, 233, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 187, 106, 0.2);
  border-radius: 24px;
  padding: 25px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  margin: 0 auto 30px;
  max-width: 1200px;
  border-left: 4px solid #66bb6a;
  
  h3 {
    color: #43a047;
    margin: 0 0 15px;
    font-size: 18px;
    display: flex;
    align-items: center;
    
    .icon {
      margin-right: 8px;
      color: #43a047;
    }
  }
  
  ul {
    padding-left: 20px;
    margin: 0;
    
    li {
      margin-bottom: 8px;
      color: #555;
      line-height: 1.5;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.submit-btn {
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
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 187, 106, 0.3);
  
  &:hover:not(:disabled) {
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(102, 187, 106, 0.4);
  }
  
  &:disabled {
    background-color: #f0f0f0;
    color: #999;
    box-shadow: none;
  }
  
  .el-icon {
    margin-right: 8px;
    font-size: 18px;
  }
}

.reset-btn {
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
  background: white;
  color: #43a047;
  border: 2px solid #43a047;
  
  &:hover, &:focus {
    color: white;
    background-color: #43a047;
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
  }
}

.login-dialog-content {
  text-align: center;
  padding: 20px 0;
  
  p {
    margin: 10px 0;
    color: #666;
  }
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
  
  .login-btn {
      background-color: #66bb6a;
      border-color: #66bb6a;
      
      &:hover {
        background-color: #43a047;
        border-color: #43a047;
      }
    }
  
  .register-btn {
    background-color: #67C23A;
    border-color: #67C23A;
    
    &:hover {
      background-color: darken(#67C23A, 5%);
      border-color: darken(#67C23A, 5%);
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
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
    box-shadow: 0 4px 12px rgba(255, 182, 193, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(255, 182, 193, 0.4);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px rgba(255, 182, 193, 0.3);
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

// 表单样式覆盖
:deep(.el-input__wrapper), :deep(.el-textarea__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  
  &:hover {
    box-shadow: 0 0 0 1px #66bb6a inset;
  }
  
  &.is-focus {
    box-shadow: 0 0 0 1px #66bb6a inset;
  }
}

// 响应式调整
@media (max-width: 1200px) {
  // 移除1200px断点处的max-width限制，保持与PetList.vue一致的响应式策略
}

@media (max-width: 992px) {
  .boarding-apply-page {
    padding: 20px 0 60px;
  }
  
  .page-banner {
    padding: 60px 20px;
    margin: 0 15px 30px;
    
    h1 {
      font-size: 32px;
    }
    
    p {
      font-size: 16px;
    }
    border-radius: 24px;
  }
  
  .booking-grid {
    grid-template-columns: 1fr;
    gap: 25px;
    padding: 0 15px;
    
    .service-info, .booking-form {
      padding: 25px;
    }
  }
  
  .notice-card {
    margin: 0 15px 30px;
    padding: 20px;
  }
}

@media (max-width: 768px) {
  .page-banner {
    padding: 50px 20px;
    margin: 0 10px 25px;
    border-radius: 24px;
    
    h1 {
      font-size: 28px;
    }
    
    p {
      font-size: 14px;
    }
  }
  
  .booking-grid {
    padding: 0 10px;
    
    .service-info, .booking-form {
      padding: 20px;
    }
  }
  
  .notice-card {
    margin: 0 10px 25px;
    padding: 18px;
  }
}

@media (max-width: 576px) {
  .page-banner {
    padding: 40px 16px;
    margin: 0 10px 20px;
    border-radius: 24px;
    
    h1 {
      font-size: 24px;
    }
    
    p {
      font-size: 13px;
    }
  }
  
  .booking-grid {
    gap: 20px;
    
    .service-info, .booking-form {
      padding: 18px;
    }
  }
  
  .notice-card {
    margin: 0 10px 20px;
    padding: 16px;
  }
}
</style> 