<template>
  <div class="profile-container">
    <!-- Hero顶部区域 -->
    <!-- <div class="page-hero">
      <div class="hero-background">
        <div class="hero-gradient"></div>
      </div>
      <div class="hero-content">
        <div class="breadcrumb">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item @click="$router.push('/')">首页</el-breadcrumb-item>
            <el-breadcrumb-item>个人中心</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <h1 class="page-title">
          <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          个人中心
        </h1>
        <p class="page-subtitle">
          管理您的个人信息和账户设置
        </p>
      </div>
    </div> -->

    <!-- 现代化标签页区域 -->
    <div class="profile-section">
      <div class="section-container">
        <div class="profile-tabs glass-card">
          <el-tabs v-model="activeTab" class="modern-tabs">
            <!-- 基本信息标签页 -->
            <el-tab-pane label="基本信息" name="basic">
              <template #label>
                <div class="tab-label">
                  <svg class="label-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                    <circle cx="12" cy="7" r="4"></circle>
                  </svg>
                  <span>基本信息</span>
                </div>
              </template>

              <div class="profile-content">
                <div class="profile-layout">
                  <!-- 头像区域 -->
                  <div class="avatar-section">
                    <div class="avatar-card glass-card-inner">
                      <div class="avatar-wrapper">
                        <el-upload class="avatar-uploader" action="#" :auto-upload="true" :show-file-list="false"
                          :http-request="customUploadAvatar" :before-upload="beforeAvatarUpload">
                          <el-avatar :size="120" :src="avatarUrl" class="user-avatar">
                            <span class="avatar-fallback">{{ userForm.name?.charAt(0) || userForm.username?.charAt(0) || '用' }}</span>
                          </el-avatar>
                        </el-upload>
                      </div>
                      <div class="avatar-info">
                        <h3 class="user-name">{{ userForm.name || userForm.username }}</h3>
                        <p class="user-desc">点击头像更换</p>
                      </div>
                    </div>
                  </div>

                  <!-- 表单区域 -->
                  <div class="form-section">
                    <div class="form-card glass-card-inner">
                      <h3 class="card-title">
                        <svg class="title-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                          stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                        </svg>
                        编辑个人信息
                      </h3>

                      <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="100px"
                        class="modern-form">
                        <div class="form-row">
                          <el-form-item label="用户名" prop="username" class="form-item">
                            <el-input v-model="userForm.username" disabled class="form-input" :prefix-icon="User" />
                          </el-form-item>

                          <el-form-item label="姓名" prop="name" class="form-item">
                            <el-input v-model="userForm.name" class="form-input" :prefix-icon="UserFilled"
                              placeholder="请输入姓名" />
                          </el-form-item>
                        </div>

                        <div class="form-row">
                          <el-form-item label="性别" prop="sex" class="form-item">
                            <el-radio-group v-model="userForm.sex" class="gender-group">
                              <el-radio label="男" class="gender-radio">
                                <el-icon>
                                  <UserFilled />
                                </el-icon>
                                男
                              </el-radio>
                              <el-radio label="女" class="gender-radio">
                                <el-icon>
                                  <UserFilled />
                                </el-icon>
                                女
                              </el-radio>
                            </el-radio-group>
                          </el-form-item>

                          <el-form-item label="电子邮箱" prop="email" class="form-item">
                            <el-input v-model="userForm.email" type="email" class="form-input" :prefix-icon="Message"
                              placeholder="请输入电子邮箱" />
                          </el-form-item>
                        </div>

                        <div class="form-row">
                          <el-form-item label="手机号码" prop="phone" class="form-item">
                            <el-input v-model="userForm.phone" type="tel" class="form-input" :prefix-icon="Phone"
                              placeholder="请输入手机号码" />
                          </el-form-item>
                        </div>

                        <div class="form-actions">
                          <el-button type="primary" @click="handleUpdate" class="save-btn">
                            <el-icon><Check /></el-icon>
                            保存修改
                          </el-button>
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
                  <svg class="label-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                  </svg>
                  <span>修改密码</span>
                </div>
              </template>

              <div class="password-content">
                <div class="password-card glass-card-inner">
                  <div class="password-header">
                    <svg class="security-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                    </svg>
                    <h3 class="password-title">账户安全</h3>
                    <p class="password-desc">定期更新密码可以提高账户安全性</p>
                  </div>

                  <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="120px"
                    class="password-form">
                    <div class="form-row">
                      <el-form-item label="当前密码" prop="oldPassword" class="form-item">
                        <el-input v-model="passwordForm.oldPassword" type="password" class="form-input"
                          :prefix-icon="Lock" placeholder="请输入当前密码" show-password />
                      </el-form-item>
                    </div>

                    <div class="form-row">
                      <el-form-item label="新密码" prop="newPassword" class="form-item">
                        <el-input v-model="passwordForm.newPassword" type="password" class="form-input"
                          :prefix-icon="Lock" placeholder="请输入新密码" show-password />
                      </el-form-item>

                      <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
                        <el-input v-model="passwordForm.confirmPassword" type="password" class="form-input"
                          :prefix-icon="Lock" placeholder="请确认新密码" show-password />
                      </el-form-item>
                    </div>

                    <div class="form-actions">
                      <el-button type="primary" @click="handleChangePassword" class="save-btn">
                        <el-icon><RefreshRight /></el-icon>
                        更新密码
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
import { ref, reactive, computed, onMounted, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";
import { 
  User, 
  UserFilled, 
  Message, 
  Phone, 
  Lock, 
  Key, 
  Check,
  ArrowUp
} from '@element-plus/icons-vue';

const baseAPI = process.env.VUE_APP_BASE_API || "/api";
const userStore = useUserStore();
const activeTab = ref("basic");

// 表单引用
const userFormRef = ref(null);
const passwordFormRef = ref(null);

// 用户表单数据
const userForm = reactive({
  id: "",
  username: "",
  name: "",
  email: "",
  phone: "",
  sex: "",
  avatar: "",
});

// 头像地址
const avatarUrl = computed(() => {
  return baseAPI + userForm.avatar;
});

// 密码表单数据
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
  phone: [
    { required: false, trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: ["blur", "change"],
    },
  ],
};

// 密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: ["blur", "change"],
    },
  ],
};

// 获取用户信息
const getUserInfo = async () => {
  try {
    // 如果用户已登录，从 store 中获取用户信息
    if (userStore.isLoggedIn) {
      // 从后端重新获取最新的用户信息
      const response = await request.get("/user/current", null, {
        showDefaultMsg: false,
      });

      // 确保返回数据存在
      if (response) {
        // 更新store中的用户信息
        userStore.updateUserInfo(response);

        // 直接更新表单数据
        userForm.id = response.id || "";
        userForm.username = response.username || "";
        userForm.name = response.name || "";
        userForm.email = response.email || "";
        userForm.phone = response.phone || "";
        userForm.sex = response.sex || "男";
        userForm.avatar = response.avatar || "";

        console.log("用户信息加载成功:", userForm);
      }
    }
  } catch (error) {
    console.error("获取用户信息失败", error);
    ElMessage.error("获取用户信息失败");
  }
};

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === "image/jpeg";
  const isPNG = file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    ElMessage.error("头像只能是 JPG 或 PNG 格式!");
    return false;
  }
  if (!isLt2M) {
    ElMessage.error("头像大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 自定义头像上传方法
const customUploadAvatar = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem("token") || "",
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: "头像上传成功",
      // 自定义错误消息
      errorMsg: "头像上传失败",
      // 成功回调
      onSuccess: async (data) => {
        // 更新用户头像
        userForm.avatar = data;

        // 保存到后端
        await updateUserAvatar(data);

        // 通知上传成功
        options.onSuccess({ data });
      },
      // 错误回调
      onError: (error) => {
        console.error("头像上传错误:", error);
        options.onError(new Error(error.message || "上传失败"));
      },
    };

    // 发送上传请求
    await request.post("/file/upload/img", formData, uploadOptions);
  } catch (error) {
    options.onError(error);
    console.error("头像上传过程发生错误:", error);
  }
};

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    await request.put(
      `/user/${userForm.id}`,
      { avatar: avatarPath },
      {
        showDefaultMsg: false,
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = { ...userStore.userInfo, avatar: avatarPath };
          userStore.updateUserInfo(updatedUserInfo);
        },
        onError: (error) => {
          console.error("头像信息保存失败", error);
          ElMessage.error("头像信息保存失败");
        },
      }
    );
  } catch (error) {
    console.error("头像信息保存失败", error);
    ElMessage.error("头像信息保存失败");
    throw error;
  }
};

// 提交用户信息更新
const submitUserInfo = async () => {
  if (!userFormRef.value) return;

  try {
    // 表单验证
    await userFormRef.value.validate();

    await request.put(
      `/user/${userForm.id}`,
      {
        name: userForm.name,
        email: userForm.email,
        phone: userForm.phone,
        sex: userForm.sex,
      },
      {
        showDefaultMsg: false,
        successMsg: "个人信息更新成功!",
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = {
            ...userStore.userInfo,
            name: userForm.name,
            email: userForm.email,
            phone: userForm.phone,
            sex: userForm.sex,
          };
          userStore.updateUserInfo(updatedUserInfo);
        },
        onError: (error) => {
          console.error("用户信息更新失败", error);
          ElMessage.error("用户信息更新失败");
        },
      }
    );

  } catch (error) {
    console.error("保存个人信息失败", error);
    ElMessage.error("保存个人信息失败");
  }
};

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    // 表单验证
    await passwordFormRef.value.validate();

    await request.put(
      `/user/password/${userForm.id}`,
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      },
      {
        showDefaultMsg: false,

        onSuccess: (data) => {
          // 清空表单
          passwordForm.oldPassword = "";
          passwordForm.newPassword = "";
          passwordForm.confirmPassword = "";

          // 提示用户重新登录
          ElMessageBox.confirm("密码已修改，需要重新登录", "提示", {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
          }).then(() => {
            // 清除用户信息并跳转到登录页
            userStore.clearUserInfo();
            window.location.href = "/login";
          });
        },
        onError: (error) => {
          console.error("密码信息保存失败", error);
          ElMessage.error("密码信息保存失败");
        },
      }
    );
  } catch (error) {
    console.error("密码修改失败", error);
    ElMessage.error(error.message || "密码修改失败");
  }
};

// 监听用户表单数据变化
watch(
  userForm,
  (newVal) => {
    console.log("用户表单数据变化:", newVal);
  },
  { deep: true }
);

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo();
});
</script>

<style scoped>
/* 现代化玻璃态卡片和渐变设计 */
.profile-container {
  min-height: 100;
  position: relative;
}



/* 玻璃态卡片设计 */
.profile-section {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  position: relative;
  z-index: 3;
}

.section-container {
  width: 100%;
  max-width: 1200px;
}

.glass-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  padding: 30px;
  width: 100%;
  max-width: 1200px;
  min-height: auto;
  animation: cardSlideUp 0.8s ease-out 0.6s both;
  transition: all 0.3s ease;
}

.glass-card:hover {
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  transform: translateY(-5px);
}

.glass-card-inner {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  padding: 24px;
  transition: all 0.3s ease;
}

.glass-card-inner:hover {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

/* 现代化标签页 */
.modern-tabs {
  position: relative;
}

.modern-tabs .el-tabs__header {
  border-bottom: 2px solid rgba(255, 215, 0, 0.2);
  margin-bottom: 30px;
}

.modern-tabs .el-tabs__nav {
  display: flex;
  gap: 50px;
}

.modern-tabs .el-tabs__item {
  color: rgba(0, 0, 0, 0.6);
  font-size: 1.1rem;
  font-weight: 500;
  padding: 16px 0;
  position: relative;
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
}

.modern-tabs .el-tabs__item:hover {
  color: #FF9800;
  border-bottom-color: rgba(255, 152, 0, 0.3);
  transform: translateY(-2px);
}

.modern-tabs .el-tabs__item.is-active {
  color: #FF9800;
  border-bottom-color: #FF9800;
  font-weight: 600;
}

.modern-tabs .el-tabs__active-bar {
  background-color: #FF9800;
  height: 3px;
  border-radius: 3px;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 10px;
}

.tab-label .label-icon {
  width: 20px;
  height: 20px;
  transition: all 0.3s ease;
}

.modern-tabs .el-tabs__item:hover .label-icon,
.modern-tabs .el-tabs__item.is-active .label-icon {
  fill: #FF9800;
  transform: translateY(-2px) scale(1.1);
}

/* 标签页内容过渡 */
.el-tabs__content {
  transition: all 0.3s ease;
}

.el-tab-pane {
  animation: fadeIn 0.5s ease-out;
}

/* 个人资料内容区域 */
.profile-content {
  padding: 20px 0;
}

.profile-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

/* 头像区域 */
.avatar-section {
  flex: 0 0 auto;
  width: 280px;
}

.avatar-card {
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.user-avatar {
  border-radius: 50%;
  border: 4px solid rgba(255, 152, 0, 0.2);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.15);
  transition: all 0.3s ease;
  animation: avatarPulse 3s ease-in-out infinite;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 35px rgba(255, 152, 0, 0.25);
}

.avatar-fallback {
  font-size: 40px;
  color: #FF9800;
  font-weight: 600;
}

.avatar-uploader {
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-uploader:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
}

.avatar-info .user-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
  transition: all 0.3s ease;
}

.avatar-info .user-name:hover {
  color: #FF9800;
}

.avatar-info .user-desc {
  font-size: 0.9rem;
  color: #666;
  margin: 0;
  transition: all 0.3s ease;
}

/* 表单区域 */
.form-section {
  flex: 1;
  min-width: 0;
}

.form-card {
  padding: 30px;
}

.card-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 25px;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
}

.card-title:hover {
  color: #FF9800;
}

.card-title .title-icon {
  width: 24px;
  height: 24px;
  fill: #FF9800;
  transition: all 0.3s ease;
}

.card-title:hover .title-icon {
  transform: rotate(5deg) scale(1.1);
}

/* 现代化表单样式 */
.modern-form {
  width: 100%;
}

.form-row {
  display: flex;
  gap: 25px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.form-item {
  flex: 1;
  min-width: 280px;
  margin-bottom: 0 !important;
  animation: formItemSlideIn 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

.form-item:nth-child(1) { animation-delay: 0.1s; }
.form-item:nth-child(2) { animation-delay: 0.2s; }
.form-item:nth-child(3) { animation-delay: 0.3s; }
.form-item:nth-child(4) { animation-delay: 0.4s; }

.modern-form .el-form-item__label {
  font-weight: 600;
  color: #555;
  font-size: 0.95rem;
  padding-bottom: 8px;
}

.form-input {
  border-radius: 10px;
  border: 1px solid rgba(255, 152, 0, 0.15);
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.9);
  position: relative;
  overflow: hidden;
}

.form-input::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 152, 0, 0.1), transparent);
  transition: all 0.5s ease;
}

.form-input:hover {
  border-color: rgba(255, 152, 0, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 152, 0, 0.1);
}

.form-input:focus-within {
  border-color: #FF9800;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.1);
  transform: translateY(-1px);
}

.form-input .el-input__inner {
  border-radius: 10px;
  padding: 12px 15px;
  font-size: 0.95rem;
  border: none;
  background-color: transparent;
  transition: all 0.3s ease;
}

/* 性别选择 */
.gender-group {
  display: flex;
  gap: 30px;
}

.gender-radio {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  color: #555;
  transition: all 0.3s ease;
  padding: 8px 15px;
  border-radius: 8px;
  background-color: rgba(255, 255, 255, 0.5);
}

.gender-radio:hover {
  color: #FFD700;
  transform: translateY(-2px);
  background-color: rgba(255, 215, 0, 0.05);
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.1);
}

.gender-radio .el-icon {
  font-size: 18px;
  transition: all 0.3s ease;
}

.gender-radio:hover .el-icon {
  transform: scale(1.2);
}

.gender-radio .el-radio__input.is-checked .el-radio__inner {
    border-color: #FFD700;
    background-color: #FFD700;
    transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
  }

  .gender-radio .el-radio__input.is-checked .el-radio__inner::after {
    background-color: #333;
  }

  .gender-radio .el-radio__input.is-checked + span {
    color: #FFD700;
  }

  .gender-radio .el-radio__input.is-checked .el-icon {
    color: #FFD700;
  }

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  animation: formItemSlideIn 0.5s ease-out 0.5s forwards;
  opacity: 0;
  transform: translateY(20px);
}

.save-btn {
  background: linear-gradient(135deg, #FF9800 0%, #F57C00 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 30px;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.save-btn::before {
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

.save-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 152, 0, 0.4);
}

.save-btn:hover::before {
  left: 100%;
}

.save-btn:active {
  transform: translateY(-1px);
}

/* 密码修改区域 */
.password-content {
  padding: 20px 0;
}

.password-card {
  padding: 30px;
}

.password-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.security-icon {
  width: 50px;
  height: 50px;
  fill: #FF9800;
  margin-bottom: 15px;
  transition: all 0.3s ease;
  animation: securityPulse 3s ease-in-out infinite;
}

.security-icon:hover {
  transform: scale(1.1) rotate(5deg);
}

.password-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  transition: all 0.3s ease;
}

.password-title:hover {
  color: #FF9800;
}

.password-desc {
  font-size: 0.95rem;
  color: #666;
  margin: 0;
  transition: all 0.3s ease;
}

/* 密码表单 */
.password-form {
  max-width: 600px;
  margin: 0 auto;
}

.password-form .form-item {
  margin-bottom: 25px !important;
}

/* 动画效果定义 */
@keyframes cardSlideUp {
  from {
    opacity: 0;
    transform: translateY(60px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes formItemSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}



@keyframes avatarPulse {
  0%, 100% {
    box-shadow: 0 8px 25px rgba(255, 152, 0, 0.15);
  }
  50% {
    box-shadow: 0 12px 35px rgba(255, 152, 0, 0.25);
  }
}

@keyframes buttonFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

@keyframes securityPulse {
  0%, 100% {
    fill: #FF9800;
    transform: scale(1);
  }
  50% {
    fill: #F57C00;
    transform: scale(1.05);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .profile-layout {
    flex-direction: column;
    align-items: center;
  }

  .avatar-section {
    width: 100%;
    max-width: 300px;
  }

  .avatar-card {
    text-align: center;
  }

  .form-row {
    flex-direction: column;
    gap: 20px;
  }

  .form-item {
    min-width: 100%;
  }
}

@media (max-width: 768px) {
  .profile-section {
    padding: 30px 15px;
  }

  .glass-card {
    padding: 20px;
  }

  .glass-card-inner {
    padding: 20px;
  }

  .modern-tabs .el-tabs__nav {
    gap: 30px;
  }

  .tab-label {
    gap: 8px;
  }

  .card-title {
    font-size: 1.2rem;
    margin-bottom: 20px;
  }

  .form-card {
    padding: 20px;
  }

  .form-actions {
    justify-content: center;
  }

  .password-form {
    padding: 0 10px;
  }
}

@media (max-width: 480px) {
  .profile-section {
    padding: 20px 10px;
  }

  .glass-card {
    padding: 15px;
  }

  .glass-card-inner {
    padding: 15px;
  }

  .modern-tabs .el-tabs__nav {
    gap: 20px;
  }

  .modern-tabs .el-tabs__item {
    font-size: 0.95rem;
  }

  .form-row {
    gap: 15px;
  }

  .form-item {
    margin-bottom: 15px !important;
  }
}
</style>