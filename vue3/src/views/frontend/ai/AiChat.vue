<template>
  <div class="chat-container">
    <!-- 左侧聊天会话列表 -->
    <div class="chat-sessions">
      <div class="sessions-header">
        <div class="header-content">
          <svg class="header-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
          </svg>
          <h2 class="gradient-text">AI宠物助手</h2>
        </div>
        <el-button class="new-session-btn" @click="openNewSession">
          <el-icon>
            <Plus />
          </el-icon>
          <span>新会话</span>
        </el-button>
      </div>
      <div class="sessions-list">
        <div v-for="session in sessions" :key="session.id" class="session-item glass-effect" 
          :class="{ active: currentSession?.id === session.id }" @click="selectSession(session)">
          <div class="session-icon">
            <el-icon>
              <ChatDotRound />
            </el-icon>
          </div>
          <div class="session-info">
            <span class="session-name">{{ session.sessionName }}</span>
          </div>
          <div class="session-actions">
            <el-button class="edit-btn" size="small" text @click="editSessionTitle(session, $event)">
              <el-icon>
                <Edit />
              </el-icon>
            </el-button>
            <el-button class="delete-btn" size="small" text @click="deleteSession(session, $event)">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-window">
      <!-- Hero区域 -->
      <div class="chat-hero" v-if="messages.length === 0">
        <div class="hero-content">
          <div class="hero-icon">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              <path d="M13 8H7"></path>
              <path d="M17 12H7"></path>
            </svg>
          </div>
          <h1 class="hero-title">AI 宠物助手</h1>
          <p class="hero-subtitle">专业宠物顾问，为您的爱宠提供贴心建议</p>
          <div class="quick-questions">
            <div class="quick-question-item glass-card" @click="sendQuickQuestion('我的狗狗最近不爱吃饭，怎么办？')">
              <el-icon>
                <NotebookPen />
              </el-icon>
              <span>宠物饮食问题</span>
            </div>
            <div class="quick-question-item glass-card" @click="sendQuickQuestion('如何训练猫咪使用猫砂盆？')">
              <el-icon>
                <Trophy />
              </el-icon>
              <span>宠物训练</span>
            </div>
            <div class="quick-question-item glass-card" @click="sendQuickQuestion('狗狗最近总是掉毛，正常吗？')">
              <el-icon>
                <FirstAid />
              </el-icon>
              <span>宠物健康</span>
            </div>
            <div class="quick-question-item glass-card" @click="sendQuickQuestion('如何照顾新生的小猫？')">
              <el-icon>
                <Pet />
              </el-icon>
              <span>宠物护理</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="chatMessages" v-else>
        <div v-for="(message, index) in messages" :key="index" class="message-wrapper" 
          :class="{ 'user-message': message.type === 1, 'ai-message': message.type === 0 }">
          <div class="message-avatar">
            <div class="avatar-icon" :class="message.type === 1 ? 'user-avatar' : 'ai-avatar'">
              <el-icon v-if="message.type === 1">
                <User />
              </el-icon>
              <svg v-else xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
              </svg>
            </div>
          </div>
          <div class="message-content glass-card" v-html="renderMarkdown(message.msg)"></div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input-wrapper">
        <div class="chat-input glass-effect">
          <el-input v-model="newMessage" type="textarea" :rows="3" placeholder="询问任何关于宠物的问题..." :disabled="isStreaming"
            @keydown.enter.prevent="handleEnterKey" />
          <el-button v-if="!isStreaming" class="send-btn" @click="sendMessage" :disabled="!newMessage.trim()">
            <el-icon>
              <Promotion />
            </el-icon>
          </el-button>
          <el-button v-else class="stop-btn" @click="cancelStream">
            <el-icon>
              <VideoPause />
            </el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import {
  Plus,
  ChatDotRound,
  User,
  Promotion,
  VideoPause,
  Delete,
  Edit,
  NotebookPen,
  Trophy,
  FirstAid,
  Pet
} from '@element-plus/icons-vue'

// 初始化 Markdown 解析器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
      } catch (__) {
        // ignore
      }
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

// 渲染 Markdown 内容
const renderMarkdown = (content) => {
  return md.render(content)
}

const router = useRouter()
const chatMessages = ref(null)
const sessions = ref([])
const currentSession = ref(null)
const newMessage = ref('')
const messages = ref([])
const isStreaming = ref(false)
const eventSource = ref(null)
const isLoading = ref(false)

// 获取Token和用户信息
const token = computed(() => {
  return localStorage.getItem('token')
})

const userInfo = computed(() => {
  const userStr = localStorage.getItem('userInfo')
  return userStr ? JSON.parse(userStr) : null
})

// 检查登录状态
const checkLogin = () => {
  if (!token.value || !userInfo.value) {
    ElMessage.warning('请先登录后使用AI助手')
    router.push('/login')
    return false
  }
  return true
}

// 加载用户的会话列表
const loadSessions = async () => {
  try {
    const data = await request.get('/ai/sessions')
    sessions.value = data || []

    // 如果有会话，选择最近的一个
    if (sessions.value.length > 0) {
      await selectSession(sessions.value[0])
    }
  } catch (error) {
    console.error('加载会话列表失败', error)
    ElMessage.error('加载会话列表失败')
  }
}

// 选择会话并加载历史消息
const selectSession = async (session) => {
  if (isStreaming.value) {
    ElMessage.warning('请等待当前对话完成')
    return
  }

  currentSession.value = session
  isLoading.value = true

  try {
    // 从后端加载历史消息
    const data = await request.get(`/ai/messages/${session.id}`)
    messages.value = (data || []).map(msg => ({
      type: msg.messageType,
      msg: msg.content
    }))
    nextTick(() => scrollToBottom())
  } catch (error) {
    console.error('加载历史消息失败', error)
    messages.value = []
  } finally {
    isLoading.value = false
  }
}

// 创建新会话
const openNewSession = async () => {
  if (!checkLogin()) return

  if (isStreaming.value) {
    ElMessage.warning('请等待当前对话完成')
    return
  }

  try {
    const sessionName = `会话 ${new Date().toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })}`
    const res = await request.post('/ai/createSession', null, {
      params: { sessionName },
      successMsg: '已开启新会话'
    })
    sessions.value.unshift(res)
    currentSession.value = res
    messages.value = []
  } catch (error) {
    console.error('创建会话失败', error)
    if (error.code === 401) {
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
    }
  }
}

// 发送消息
const sendMessage = () => {
  if (!checkLogin()) return

  const message = newMessage.value.trim()
  if (!message) {
    return
  }

  if (!currentSession.value) {
    ElMessage.warning('请先创建会话')
    return
  }

  // 添加用户消息到聊天记录
  messages.value.push({
    type: 1, // 1=用户消息
    msg: message
  })

  // 清空输入框
  newMessage.value = ''

  // 调用 SSE 流式接口
  streamChat(message)

  // 滚动到底部
  scrollToBottom()
}

// 编辑会话标题
const editSessionTitle = async (session, event) => {
  event.stopPropagation()

  try {
    const { value: newTitle } = await ElMessageBox.prompt('请输入新的会话标题', '编辑标题', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: session.sessionName,
      inputValidator: (value) => {
        if (!value || !value.trim()) {
          return '标题不能为空'
        }
        if (value.length > 50) {
          return '标题长度不能超过50个字符'
        }
        return true
      },
      inputErrorMessage: '请输入有效的标题'
    })

    if (newTitle && newTitle.trim()) {
      await request.put(`/ai/session/${session.id}/title`, null, {
        params: { title: newTitle.trim() },
        successMsg: '标题更新成功'
      })

      // 更新本地会话标题
      session.sessionName = newTitle.trim()

      // 如果是当前会话，同时更新当前会话对象
      if (currentSession.value?.id === session.id) {
        currentSession.value.sessionName = newTitle.trim()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('编辑会话标题失败', error)
    }
  }
}

// 删除会话
const deleteSession = async (session, event) => {
  event.stopPropagation()

  try {
    await ElMessageBox.confirm('确定要删除这个会话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await request.delete(`/ai/session/${session.id}`, {
      successMsg: '删除成功'
    })

    // 从列表中移除
    const index = sessions.value.findIndex(s => s.id === session.id)
    if (index > -1) {
      sessions.value.splice(index, 1)
    }

    // 如果删除的是当前会话，清空消息
    if (currentSession.value?.id === session.id) {
      currentSession.value = null
      messages.value = []

      // 如果还有其他会话，选择第一个
      if (sessions.value.length > 0) {
        await selectSession(sessions.value[0])
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败', error)
    }
  }
}

// 快速提问
const sendQuickQuestion = (question) => {
  newMessage.value = question
  sendMessage()
}

// 处理回车键
const handleEnterKey = (e) => {
  if (!e.shiftKey) {
    sendMessage()
  }
}

// SSE 流式对话
const streamChat = (message) => {
  isStreaming.value = true

  // 添加 AI 消息占位符
  messages.value.push({
    type: 0, // 0=AI消息
    msg: ''
  })

  // 用于累积接收到的文本
  let messageOrigin = ''

  // 构建请求 URL
  const url = `/api/ai/chatStream?message=${encodeURIComponent(message)}&sessionId=${currentSession.value.id}&token=${encodeURIComponent(token.value)}`

  // 创建 EventSource 连接
  const es = new EventSource(url)
  eventSource.value = es

  // 监听消息事件
  es.onmessage = (event) => {
    const data = event.data

    // 检测 ETX 结束标记
    if (data === '\u0003') {
      es.close()
      isStreaming.value = false
      scrollToBottom()

      // 对话完成后，延迟2秒刷新会话列表（等待后端生成标题）
      setTimeout(async () => {
        try {
          const data = await request.get('/ai/sessions', { showDefaultMsg: false })
          const oldSessionId = currentSession.value?.id
          sessions.value = data || []

          // 保持当前会话选中，但更新数据
          if (oldSessionId) {
            const updatedSession = sessions.value.find(s => s.id === oldSessionId)
            if (updatedSession) {
              currentSession.value = updatedSession
            }
          }
        } catch (error) {
          console.error('刷新会话列表失败', error)
        }
      }, 2000)

      return
    }

    // 累积接收到的数据
    messageOrigin += data

    // 更新消息内容
    messages.value[messages.value.length - 1].msg = messageOrigin

    // 滚动到底部
    scrollToBottom()
  }

  // 监听错误事件
  es.addEventListener('error', (event) => {
    // 检查是否是后端发送的错误消息
    if (event.data) {
      messages.value[messages.value.length - 1].msg = event.data
      ElMessage.error(event.data)
    } else {
      ElMessage.error('连接出错，请重试')
      // 移除空的AI消息
      if (messages.value[messages.value.length - 1].msg === '') {
        messages.value.pop()
      }
    }
    es.close()
    isStreaming.value = false
  })
}

// 取消流式输出
const cancelStream = () => {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }

  // 调用后端取消接口
  fetch(`/api/ai/cancelStream?sessionId=${currentSession.value.id}`, {
    method: 'POST'
  }).catch(err => {
    console.error('取消请求失败', err)
  })

  isStreaming.value = false
  ElMessage.info('已停止生成')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessages.value) {
      chatMessages.value.scrollTop = chatMessages.value.scrollHeight
    }
  })
}

// 组件挂载时检查登录并加载会话列表
onMounted(async () => {
  if (!checkLogin()) return

  await loadSessions()

  // 如果没有会话，自动创建一个
  if (sessions.value.length === 0) {
    await openNewSession()
  }
})
</script>

<style scoped lang="scss">
// 全局变量
:root {
  --gradient-primary: linear-gradient(135deg, #66bb6a 0%, #43a047 100%);
  --gradient-secondary: linear-gradient(135deg, #42a5f5 0%, #1976d2 100%);
}

.chat-container {
  display: flex;
  height: calc(100vh - 80px);
  padding: 0;
  font-family: "PingFang SC", "Helvetica Neue", Helvetica, Arial, "Microsoft YaHei", sans-serif;
  border-radius: 24px;
  overflow: hidden;
}

.chat-sessions {
  width: 320px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  flex-direction: column;
  margin: 16px 0 16px 16px;

  .sessions-header {
    padding: 30px 24px;
    border-bottom: 1px solid rgba(102, 187, 106, 0.1);

    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;

      .header-icon {
        width: 32px;
        height: 32px;
        color: #66bb6a;
        flex-shrink: 0;
      }

      .gradient-text {
        font-size: 24px;
        font-weight: 800;
        background: var(--gradient-primary);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        margin: 0;
      }
    }

    .new-session-btn {
      width: 100%;
      background: var(--gradient-primary);
      border: none;
      border-radius: 12px;
      color: white;
      font-weight: 600;
      padding: 12px;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(102, 187, 106, 0.3);
      }
    }
  }

  .sessions-list {
    padding: 16px;
    overflow-y: auto;
    flex: 1;

    .session-item {
      display: flex;
      align-items: center;
      padding: 16px;
      margin-bottom: 12px;
      border-radius: 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      gap: 12px;
      position: relative;

      .session-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        background: var(--gradient-primary);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
        flex-shrink: 0;
      }

      .session-info {
        flex: 1;
        overflow: hidden;

        .session-name {
          font-size: 14px;
          font-weight: 600;
          color: #1a202c;
          display: block;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }

      .session-actions {
        display: flex;
        gap: 4px;
        opacity: 0;
        transition: opacity 0.2s ease;
      }

      .edit-btn {
        color: #66bb6a;
        padding: 4px;

        &:hover {
          background: rgba(102, 187, 106, 0.1);
        }
      }

      .delete-btn {
        color: #f56c6c;
        padding: 4px;

        &:hover {
          background: rgba(245, 108, 108, 0.1);
        }
      }

      &:hover {
        transform: translateX(4px);
        box-shadow: 0 4px 16px rgba(102, 187, 106, 0.15);

        .session-actions {
          opacity: 1;
        }
      }

      &.active {
        background: linear-gradient(135deg, rgba(102, 187, 106, 0.1) 0%, rgba(67, 160, 71, 0.1) 100%);
        border: 1px solid rgba(102, 187, 106, 0.3);
        transform: translateX(4px);

        .session-icon {
          box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);
        }

        .session-name {
          color: #43a047;
        }

        .session-actions {
          opacity: 1;
        }
      }
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

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
  margin: 16px 16px 16px 8px;
}

// Hero区域
.chat-hero {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  animation: fadeIn 0.6s ease;

  .hero-content {
    text-align: center;
    max-width: 800px;

    .hero-icon {
      width: 80px;
      height: 80px;
      margin: 0 auto 24px;
      padding: 20px;
      background: var(--gradient-primary);
      border-radius: 24px;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 8px 24px rgba(102, 187, 106, 0.3);
      animation: float 3s ease-in-out infinite;

      svg {
        width: 100%;
        height: 100%;
      }
    }

    .hero-title {
      font-size: 48px;
      font-weight: 800;
      margin: 0 0 16px;
      background: var(--gradient-primary);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: 1px;
    }

    .hero-subtitle {
      font-size: 18px;
      color: #718096;
      margin: 0 0 48px;
      font-weight: 500;
    }

    .quick-questions {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-top: 32px;

      .quick-question-item {
        padding: 20px;
        border-radius: 16px;
        cursor: pointer;
        transition: all 0.3s ease;
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 15px;
        font-weight: 600;
        color: #1a202c;

        .el-icon {
          font-size: 24px;
          color: #66bb6a;
        }

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 12px 28px rgba(102, 187, 106, 0.2);
        }
      }
    }
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 32px;
  animation: fadeIn 0.6s ease;

  /* 隐藏滚动条 - Chrome, Safari, Edge */
  &::-webkit-scrollbar {
    display: none;
  }

  /* 隐藏滚动条 - Firefox */
  scrollbar-width: none;

  /* 隐藏滚动条 - IE */
  -ms-overflow-style: none;

  .message-wrapper {
    display: flex;
    margin-bottom: 32px;
    animation: slideIn 0.4s ease;
    gap: 16px;
    width: 100%;

    &.user-message {
      flex-direction: row-reverse;
      justify-content: flex-start;

      .message-content {
        background: var(--gradient-primary);
        color: white;
        border: none;

        :deep(p) {
          color: white;
          text-align: left;
        }

        :deep(code) {
          background: rgba(255, 255, 255, 0.2);
          color: white;
        }

        :deep(ul),
        :deep(ol) {
          text-align: left;
        }

        :deep(h1),
        :deep(h2),
        :deep(h3),
        :deep(h4),
        :deep(h5),
        :deep(h6) {
          text-align: left;
        }

        :deep(blockquote) {
          text-align: left;
        }
      }
    }

    &.ai-message {
      .message-content {
        background: rgba(255, 255, 255, 0.9);
        border: 1px solid rgba(102, 187, 106, 0.2);
      }
    }

    .message-avatar {
      flex-shrink: 0;

      .avatar-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;

        &.user-avatar {
          background: var(--gradient-secondary);
        }

        &.ai-avatar {
          background: var(--gradient-primary);

          svg {
            width: 20px;
            height: 20px;
          }
        }
      }
    }

    .message-content {
      max-width: 70%;
      padding: 16px 20px;
      border-radius: 16px;
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      word-wrap: break-word;
      line-height: 1.8;
      font-size: 15px;
      text-align: left;

      :deep(pre) {
        background: rgba(0, 0, 0, 0.05);
        padding: 16px;
        border-radius: 12px;
        overflow-x: auto;
        margin: 12px 0;
        border: 1px solid rgba(0, 0, 0, 0.1);

        code {
          font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
          font-size: 14px;
          line-height: 1.6;
        }
      }

      :deep(.hljs) {
        background: rgba(0, 0, 0, 0.05);
        padding: 16px;
        border-radius: 12px;
        overflow-x: auto;
      }

      :deep(p) {
        margin: 12px 0;
        color: #1a202c;
        text-align: left;

        &:first-child {
          margin-top: 0;
        }

        &:last-child {
          margin-bottom: 0;
        }
      }

      :deep(ul),
      :deep(ol) {
        margin: 12px 0;
        padding-left: 28px;
        text-align: left;

        li {
          margin: 6px 0;
          line-height: 1.8;
        }
      }

      :deep(code) {
        background: rgba(102, 187, 106, 0.1);
        padding: 3px 8px;
        border-radius: 6px;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 14px;
        color: #66bb6a;
        font-weight: 600;
      }

      :deep(h1),
      :deep(h2),
      :deep(h3),
      :deep(h4),
      :deep(h5),
      :deep(h6) {
        margin: 16px 0 12px;
        font-weight: 700;
        color: #1a202c;
        text-align: left;
      }

      :deep(blockquote) {
        margin: 12px 0;
        padding: 12px 16px;
        border-left: 4px solid #66bb6a;
        background: rgba(102, 187, 106, 0.05);
        border-radius: 0 8px 8px 0;
        text-align: left;
      }

      :deep(a) {
        color: #66bb6a;
        text-decoration: none;
        font-weight: 600;

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.glass-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.chat-input-wrapper {
  padding: 0 32px 32px;
  background: transparent;

  .chat-input {
    background: rgba(255, 255, 255, 0.25);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
    border-radius: 24px;
    padding: 20px 24px;
    position: relative;
    display: flex;
    align-items: flex-end;
    gap: 16px;

    :deep(.el-textarea) {
      flex: 1;

      .el-textarea__inner {
        border: none;
        background: transparent;
        font-size: 15px;
        resize: none;
        padding: 0;
        color: #1a202c;
        line-height: 1.6;
        box-shadow: none;

        &::placeholder {
          color: #a0aec0;
        }

        &:focus {
          outline: none;
          box-shadow: none;
        }
      }
    }

    .el-button {
      border-radius: 16px;
      width: 48px;
      height: 48px;
      padding: 0;
      font-weight: 600;
      font-size: 20px;
      transition: all 0.3s ease;
      border: none;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      .el-icon {
        margin: 0;
      }

      &.send-btn {
        background: var(--gradient-primary);
        color: white;

        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 8px 20px rgba(102, 187, 106, 0.4);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }

      &.stop-btn {
        background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
        color: white;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 20px rgba(255, 107, 107, 0.4);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 992px) {
  .chat-container {
    flex-direction: column;
  }

  .chat-sessions {
    width: 100%;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  }

  .message-wrapper {
    .message-content {
      max-width: 85%;
    }
  }
}

@media (max-width: 768px) {
  .chat-hero {
    padding: 20px;

    .hero-content {
      .hero-icon {
        width: 60px;
        height: 60px;
      }

      .hero-title {
        font-size: 32px;
      }

      .hero-subtitle {
        font-size: 16px;
      }

      .quick-questions {
        grid-template-columns: 1fr;
      }
    }
  }

  .chat-messages {
    padding: 20px;

    .message-wrapper {
      .message-content {
        max-width: 90%;
        font-size: 14px;
      }
    }
  }

  .chat-input-wrapper {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .chat-container {
    height: calc(100vh - 64px);
  }

  .chat-sessions {
    margin: 8px;
    border-radius: 16px;

    .sessions-header {
      padding: 20px 16px;

      .header-content {
        .gradient-text {
          font-size: 20px;
        }
      }
    }

    .sessions-list {
      padding: 12px;

      .session-item {
        padding: 12px;

        .session-icon {
          width: 32px;
          height: 32px;
        }

        .session-name {
          font-size: 13px;
        }
      }
    }
  }

  .chat-window {
    margin: 8px;
  }

  .chat-messages {
    padding: 16px;
  }

  .chat-input-wrapper {
    padding: 0 16px 16px;
  }
}
</style>
