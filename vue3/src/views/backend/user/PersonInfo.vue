<template>
  <div class="person-info">
    <div class="profile-section">
      <div class="section-container">
        <div class="profile-tabs glass-card">
          <el-tabs v-model="activeTab" class="modern-tabs">
            <!-- 个人信息标签页 -->
            <el-tab-pane label="个人信息" name="basic">
              <template #label>
                <div class="tab-label">
                  <svg class="label-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  <span>个人信息</span>
                </div>
              </template>

              <div class="profile-content">
                <div class="profile-layout">
                  <!-- 头像区域 -->
                  <div class="avatar-section">
                    <div class="avatar-card glass-card-inner">
                      <div class="avatar-wrapper">
                        <el-avatar :size="120" :src="avatarUrl" class="user-avatar" />
                        <el-upload
                          class="avatar-uploader"
                          action="#"
                          :auto-upload="true"
                          :show-file-list="false"
                          :http-request="customUploadAvatar"
                          :before-upload="beforeAvatarUpload"
                          :disabled="!isEditing"
                        >
                          <el-button size="small" type="primary" :disabled="!isEditing" class="change-avatar-btn">
                            更换头像
                          </el-button>
                        </el-upload>
                      </div>
                      <div class="avatar-info">
                        <h3 class="user-name">{{ form.name || form.username }}</h3>
                        <p class="user-desc">{{ form.role || '系统管理员' }}</p>
                      </div>
                    </div>
                  </div>

                  <!-- 表单区域 -->
                  <div class="form-section">
                    <div class="form-card glass-card-inner">
                      <div class="card-header">
                        <h3 class="card-title">
                          <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                          </svg>
                          <span>编辑个人信息</span>
                        </h3>
                        <div class="card-actions">
                          <el-button type="primary" @click="handleEdit" v-if="!isEditing" class="edit-btn">
                            编辑信息
                          </el-button>
                          <div v-else class="edit-actions">
                            <el-button type="primary" @click="handleSave" :loading="saving" class="save-btn">
                              保存
                            </el-button>
                            <el-button @click="handleCancel" class="cancel-btn">取消</el-button>
                          </div>
                        </div>
                      </div>

                      <el-form 
                        ref="formRef"
                        :model="form"
                        :rules="rules"
                        label-width="120px"
                        :disabled="!isEditing"
                        class="modern-form"
                      >
                        <div class="form-row">
                          <el-form-item label="用户名" prop="username" class="form-item">
                            <el-input v-model="form.username" disabled class="form-input" />
                          </el-form-item>

                          <el-form-item label="姓名" prop="name" class="form-item">
                            <el-input v-model="form.name" placeholder="请输入姓名" class="form-input" />
                          </el-form-item>
                        </div>

                        <div class="form-row">
                          <el-form-item label="性别" prop="sex" class="form-item">
                            <el-radio-group v-model="form.sex" class="gender-group">
                              <el-radio label="男" class="gender-radio">男</el-radio>
                              <el-radio label="女" class="gender-radio">女</el-radio>
                            </el-radio-group>
                          </el-form-item>

                          <el-form-item label="邮箱" prop="email" class="form-item">
                            <el-input v-model="form.email" placeholder="请输入邮箱" class="form-input" />
                          </el-form-item>
                        </div>

                        <div class="form-row">
                          <el-form-item label="手机号" prop="phone" class="form-item">
                            <el-input v-model="form.phone" placeholder="请输入手机号" class="form-input" />
                          </el-form-item>
                        </div>
                      </el-form>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- 修改密码标签页 -->
            <el-tab-pane label="修改密码" name="password">
              <template #label>
                <div class="tab-label">
                  <svg class="label-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                  <span>修改密码</span>
                </div>
              </template>

              <div class="password-content">
                <div class="password-card glass-card-inner">
                  <div class="password-header">
                    <svg class="security-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    </svg>
                    <h3 class="password-title">账户安全</h3>
                    <p class="password-desc">定期更新密码可以提高账户安全性</p>
                  </div>

                  <el-form
                    ref="passwordFormRef"
                    :model="passwordForm"
                    :rules="passwordRules"
                    label-width="140px"
                    class="password-form modern-form"
                  >
                    <div class="form-row">
                      <el-form-item label="原密码" prop="oldPassword" class="form-item full-width">
                        <el-input 
                          v-model="passwordForm.oldPassword" 
                          type="password"
                          placeholder="请输入原密码"
                          show-password
                          class="form-input"
                        />
                      </el-form-item>
                    </div>

                    <div class="form-row">
                      <el-form-item label="新密码" prop="newPassword" class="form-item full-width">
                        <el-input 
                          v-model="passwordForm.newPassword" 
                          type="password"
                          placeholder="请输入新密码"
                          show-password
                          class="form-input"
                        />
                      </el-form-item>
                    </div>

                    <div class="form-row">
                      <el-form-item label="确认密码" prop="confirmPassword" class="form-item full-width">
                        <el-input 
                          v-model="passwordForm.confirmPassword" 
                          type="password"
                          placeholder="请再次输入新密码"
                          show-password
                          class="form-input"
                        />
                      </el-form-item>
                    </div>

                    <div class="form-actions">
                      <el-button 
                        type="primary" 
                        @click="handleChangePassword"
                        :loading="changingPassword"
                        class="change-password-btn"
                      >
                        修改密码
                      </el-button>
                    </div>
                  </el-form>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const baseAPI = process.env.VUE_APP_BASE_API || '/api'
const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const isEditing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)
const activeTab = ref('basic')

// 表单数据
const form = reactive({
  id: '',
  username: '',
  name: '',
  email: '',
  phone: '',
  sex: '男',
  avatar: '',
  role: ''
})

// 头像地址
const avatarUrl = computed(() => {
  return form.avatar ? baseAPI + form.avatar : '';
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 获取当前用户的最新信息
    const userId = userStore.userInfo.id
    const res = await request.get(`/user/${userId}`, null, {
      showDefaultMsg: false
    })
    
    // 直接更新表单数据
    form.id = res.id || userStore.userInfo.id
    form.username = res.username || ''
    form.name = res.name || ''
    form.email = res.email || ''
    form.phone = res.phone || ''
    form.sex = res.sex || '男'
    form.avatar = res.avatar || ''
    
    console.log('用户信息加载成功:', form)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义头像上传方法
const customUploadAvatar = async (options) => {
  try {
    const { file } = options

    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('file', file)

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem('token') || '',
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: '头像上传成功',
      // 自定义错误消息
      errorMsg: '头像上传失败',
      // 成功回调
      onSuccess: async (data) => {
        // 更新用户头像
        form.avatar = data

        // 保存到后端
        await updateUserAvatar(data)

        // 通知上传成功
        options.onSuccess({ data })
      },
      // 错误回调
      onError: (error) => {
        console.error('头像上传错误:', error)
        options.onError(new Error(error.message || '上传失败'))
      },
    }

    // 发送上传请求
    await request.post('/file/upload/img', formData, uploadOptions)
  } catch (error) {
    options.onError(error)
    console.error('头像上传过程发生错误:', error)
  }
}

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    await request.put(
      `/user/${form.id}`,
      { avatar: avatarPath },
      {
        showDefaultMsg: false,
        successMsg: '头像更新成功',
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = { ...userStore.userInfo, avatar: avatarPath }
          userStore.updateUserInfo(updatedUserInfo)
        },
        onError: (error) => {
          console.error('头像信息保存失败', error)
          ElMessage.error('头像信息保存失败')
        },
      }
    )
  } catch (error) {
    console.error('头像信息保存失败', error)
    ElMessage.error('头像信息保存失败')
    throw error
  }
}

// 编辑信息
const handleEdit = () => {
  isEditing.value = true
}

// 取消编辑
const handleCancel = () => {
  isEditing.value = false
  fetchUserInfo() // 重新获取数据，恢复原值
}

// 保存信息
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    await request.put(
      `/user/${form.id}`,
      {
        name: form.name,
        email: form.email,
        phone: form.phone,
        sex: form.sex
      },
      {
        showDefaultMsg: false,
        successMsg: '个人信息更新成功',
        onSuccess: (data) => {
          isEditing.value = false
          // 更新store中的用户信息
          userStore.updateUserInfo({
            ...userStore.userInfo,
            name: form.name,
            email: form.email,
            phone: form.phone,
            sex: form.sex
          })
        },
        onError: (error) => {
          console.error('更新用户信息失败:', error)
          ElMessage.error('更新用户信息失败')
        }
      }
    )
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error('更新用户信息失败')
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await request.put(
      `/user/password/${form.id}`,
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      },
      {
        showDefaultMsg: false,
        successMsg: '密码修改成功',
        onSuccess: (data) => {
          // 清空密码表单
          passwordFormRef.value.resetFields()
          
          // 提示用户重新登录
          ElMessageBox.confirm('密码已修改，需要重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(() => {
            // 清除用户信息并跳转到登录页
            userStore.clearUserInfo()
            window.location.href = '/login'
          })
        },
        onError: (error) => {
          console.error('密码修改失败', error)
          ElMessage.error('密码修改失败')
        }
      }
    )
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
// 全局变量
:root {
  --gradient-primary: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  --gradient-secondary: linear-gradient(135deg, #42a5f5 0%, #2196f3 100%);
  --glass-bg: rgba(255, 255, 255, 0.15);
  --glass-border: rgba(255, 255, 255, 0.5);
  --glass-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  --text-primary: #1a202c;
  --text-secondary: #718096;
}

.person-info {
  padding: 20px;
  min-height: calc(100vh - 64px);
  background: linear-gradient(135deg, #f0fff4 0%, #e6f7e9 100%);
  font-family: "PingFang SC", "Helvetica Neue", Helvetica, Arial, "Microsoft YaHei", sans-serif;
}

// 通用容器样式
.section-container {
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

// 玻璃态卡片样式
.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(102, 187, 106, 0.2);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(102, 187, 106, 0.1);
  transition: all 0.3s ease;
  overflow: hidden;
}

.glass-card-inner {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(102, 187, 106, 0.15);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(102, 187, 106, 0.1);
  padding: 30px;
  transition: all 0.3s ease;
}

// 标签页样式
.profile-tabs {
  padding: 20px;
  min-height: calc(100vh - 100px);
}

.modern-tabs {
  :deep(.el-tabs__nav-wrap) {
    padding: 0 20px;
  }
  
  :deep(.el-tabs__header) {
    border-bottom: 2px solid rgba(102, 187, 106, 0.1);
    margin-bottom: 30px;
  }
  
  :deep(.el-tabs__nav) {
    height: 60px;
  }
  
  :deep(.el-tabs__item) {
    padding: 0 24px;
    height: 60px;
    line-height: 60px;
    font-size: 16px;
    font-weight: 500;
    color: var(--text-secondary);
    margin-right: 16px;
    transition: all 0.3s ease;
    border-radius: 12px 12px 0 0;
    background: transparent;
    position: relative;
    overflow: visible;
  }
  
  :deep(.el-tabs__item.is-active) {
    color: #43a047;
    background: rgba(102, 187, 106, 0.05);
  }
  
  :deep(.el-tabs__item:hover) {
    color: #66bb6a;
    background: rgba(102, 187, 106, 0.05);
  }
  
  :deep(.el-tabs__active-bar) {
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    height: 3px;
    border-radius: 2px;
  }
  
  :deep(.el-tabs__content) {
    padding: 0;
  }
}

// 标签样式
.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 100%;
  
  .label-icon {
    width: 20px;
    height: 20px;
    color: inherit;
    transition: all 0.3s ease;
  }
  
  .modern-tabs :deep(.el-tabs__item.is-active) & .label-icon {
    color: #43a047;
  }
}

// 个人信息内容样式
.profile-content {
  padding: 0;
  width: 100%;
}

.profile-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 30px;
  width: 100%;
  
  @media (max-width: 992px) {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

// 头像区域样式
.avatar-section {
  display: flex;
  width: 100%;
}

.avatar-card {
  text-align: center;
  width: 100%;
  min-height: 400px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(102, 187, 106, 0.15);
  }
}

.avatar-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.user-avatar {
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(102, 187, 106, 0.2);
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 12px 32px rgba(102, 187, 106, 0.3);
  }
}

.change-avatar-btn {
  border-radius: 20px;
  background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  border: none;
  padding: 8px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(102, 187, 106, 0.3);
    background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
  }
  
  &:disabled {
    opacity: 0.6;
    transform: none;
    box-shadow: none;
    cursor: not-allowed;
  }
}

.avatar-info {
  margin-top: 20px;
  
  .user-name {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    margin: 0 0 8px;
  }
  
  .user-desc {
    font-size: 14px;
    color: var(--text-secondary);
    margin: 0;
  }
}

// 表单区域样式
.form-section {
  width: 100%;
  
  .form-card {
    width: 100%;
    min-height: 400px;
    display: flex;
    flex-direction: column;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(102, 187, 106, 0.15);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
      padding-bottom: 20px;
      border-bottom: 1px solid rgba(102, 187, 106, 0.1);
      
      .card-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 20px;
        font-weight: 700;
        color: var(--text-primary);
        
        .title-icon {
          width: 24px;
          height: 24px;
          color: #43a047;
        }
      }
      
      .card-actions {
        display: flex;
        gap: 12px;
        
        .edit-actions {
          display: flex;
          gap: 12px;
        }
      }
    }
  }
}

// 按钮样式
.edit-btn,
.save-btn,
.cancel-btn {
  border-radius: 12px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  
  .edit-btn,
  .save-btn {
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    border: none;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(102, 187, 106, 0.3);
      background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
    }
  }
  
  .cancel-btn {
    background: rgba(102, 187, 106, 0.1);
    border: 1px solid rgba(102, 187, 106, 0.2);
    color: #43a047;
    
    &:hover {
      background: rgba(102, 187, 106, 0.2);
      transform: translateY(-2px);
    }
  }
}

// 现代化表单样式
.modern-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
  
  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
    
    @media (max-width: 768px) {
      grid-template-columns: 1fr;
      gap: 16px;
    }
    
    .full-width {
      grid-column: 1 / -1;
    }
  }
  
  .form-item {
    
    :deep(.el-input) {
      --el-input-hover-border-color: #66bb6a;
      --el-input-focus-border-color: #66bb6a;
      
      .el-input__wrapper {
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        background: rgba(255, 255, 255, 0.9);
        border: 2px solid #e2e8f0;
        box-shadow: 0 2px 8px rgba(102, 187, 106, 0.05);
        height: 50px;
        
        &.is-focus {
          border-color: #66bb6a;
          background: #fff;
          box-shadow: 
            0 0 0 3px rgba(102, 187, 106, 0.15),
            0 4px 12px rgba(102, 187, 106, 0.2);
          transform: translateY(-1px);
        }
        
        &:hover {
          background: #fff;
          box-shadow: 0 4px 12px rgba(102, 187, 106, 0.15);
          transform: translateY(-1px);
          border-color: rgba(102, 187, 106, 0.5);
        }
      }
    }
    
    :deep(.el-radio-group) {
      display: flex;
      gap: 32px;
      
      .gender-radio {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        padding: 8px 16px;
        border-radius: 12px;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(102, 187, 106, 0.1);
        }
        
        :deep(.el-radio__input.is-checked .el-radio__inner) {
          background-color: #66bb6a;
          border-color: #66bb6a;
        }
        
        :deep(.el-radio__input.is-checked + .el-radio__label) {
          color: #43a047;
        }
      }
    }
  }
  
  .form-input {
    font-size: 15px;
  }
  
  .form-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
}

// 密码修改样式
.password-content {
  
  .password-card {
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(102, 187, 106, 0.15);
    }
    
    .password-header {
      text-align: center;
      margin-bottom: 40px;
      
      .security-icon {
        width: 60px;
        height: 60px;
        color: #43a047;
        margin: 0 auto 16px;
        display: block;
      }
      
      .password-title {
        font-size: 24px;
        font-weight: 700;
        color: var(--text-primary);
        margin: 0 0 8px;
      }
      
      .password-desc {
        font-size: 14px;
        color: var(--text-secondary);
        margin: 0;
      }
    }
  }
  
  .password-form {
    max-width: 600px;
    margin: 0 auto;
    
    :deep(.el-input) {
      --el-input-hover-border-color: #66bb6a;
      --el-input-focus-border-color: #66bb6a;
      
      .el-input__wrapper {
        border-radius: 12px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        background: rgba(255, 255, 255, 0.9);
        border: 2px solid #e2e8f0;
        box-shadow: 0 2px 8px rgba(102, 187, 106, 0.05);
        height: 50px;
        
        &.is-focus {
          border-color: #66bb6a;
          background: #fff;
          box-shadow: 
            0 0 0 3px rgba(102, 187, 106, 0.15),
            0 4px 12px rgba(102, 187, 106, 0.2);
          transform: translateY(-1px);
        }
        
        &:hover {
          background: #fff;
          box-shadow: 0 4px 12px rgba(102, 187, 106, 0.15);
          transform: translateY(-1px);
          border-color: rgba(102, 187, 106, 0.5);
        }
      }
    }
  }
  
  .change-password-btn {
    border-radius: 12px;
    background: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
    border: none;
    padding: 12px 32px;
    font-size: 15px;
    font-weight: 500;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(102, 187, 106, 0.3);
      background: linear-gradient(135deg, #43a047 0%, #388e3c 100%);
    }
  }
}

// 响应式样式
@media (max-width: 768px) {
  .person-info {
    padding: 10px;
  }
  
  .profile-tabs {
    padding: 20px;
  }
  
  .glass-card-inner {
    padding: 20px;
  }
  
  .profile-layout {
    gap: 20px;
  }
  
  .form-row {
    grid-template-columns: 1fr !important;
    gap: 16px !important;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start !important;
    gap: 16px;
    
    .card-actions {
      width: 100%;
      justify-content: flex-start;
    }
  }
}
</style> 