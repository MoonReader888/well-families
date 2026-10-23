<template>
  <div class="chat-page">
    <van-nav-bar title="AI 健康咨询" left-arrow @click-left="router.back()" fixed placeholder>
      <template #right>
        <van-icon name="add-o" size="24" @click="newSession"/>
        <van-icon name="bars" size="24" style="margin-left: 12px" @click="showSessions = true"/>
      </template>
    </van-nav-bar>

    <!-- 消息列表 -->
    <div ref="msgBox" class="msg-area">
      <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
        <div v-if="m.role === 'assistant'" class="avatar">
          <van-icon name="service-o" color="#fff" size="18"/>
        </div>
        <div class="bubble">{{ m.message }}</div>
        <div v-if="m.role === 'user'" class="avatar">
          <van-icon name="contact" color="#fff" size="18"/>
        </div>
      </div>
      <van-empty v-if="!messages.length" description="开始一段健康咨询吧~"/>
    </div>

    <!-- 会话列表 -->
    <van-popup v-model:show="showSessions" position="bottom" round safe-area-inset-bottom>
      <div class="session-list">
        <div class="session-title">历史会话</div>
        <div
            v-for="s in sessions"
            :key="s.id"
            class="session-item"
            :class="{ active: s.id === currentSessionId }"
            @click="switchSession(s.id)"
        >
          <van-icon name="chat-o"/>
          <span class="session-name">{{ s.sessionName }}</span>
          <van-icon name="delete-o" color="#ee0a24" @click.stop="removeSession(s.id)"/>
        </div>
      </div>
    </van-popup>

    <!-- 输入区 -->
    <div class="input-bar">
      <van-field
          v-model="input"
          placeholder="请输入您想咨询的健康问题"
          type="textarea"
          autosize
          maxlength="2000"
          class="input-field"
          :disabled="streaming"
      />
      <!-- 生成中显示"停止"，否则显示"发送" -->
      <van-button
          v-if="streaming"
          type="danger"
          size="small"
          round
          class="send-btn"
          @click="stop">
        停止
      </van-button>
      <van-button
          v-if="!streaming"
          type="primary"
          size="small"
          round
          class="send-btn"
          @click="send">
        发送
      </van-button>

    </div>
  </div>
</template>

<script setup>
import {ref, nextTick, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {showConfirmDialog, showToast} from 'vant'
import {createSession, listSessions, getMessages, deleteSession} from '../../api/chat'
import {useUserStore} from '../../store/user'

const router = useRouter()
const userStore = useUserStore()

const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const input = ref('')
const streaming = ref(false)
const showSessions = ref(false)
const msgBox = ref(null)

const abortController = ref(null)

onMounted(init)

async function init() {
  try {
    const res = await listSessions()
    sessions.value = res.data || []
    if (sessions.value.length) {
      await switchSession(sessions.value[0].id)
    }
  } catch {
    /* 忽略 */
  }
}

async function newSession() {
  const res = await createSession('新的对话')
  sessions.value.unshift(res.data)
  await switchSession(res.data.id)
}

async function switchSession(id) {
  currentSessionId.value = id
  showSessions.value = false
  const res = await getMessages(id)
  messages.value = res.data || []
  scrollBottom()
}

async function removeSession(id) {
  try {
    await showConfirmDialog({title: '删除会话', message: '确定删除该会话吗？'})
    await deleteSession(id)
    sessions.value = sessions.value.filter((s) => s.id !== id)
    if (currentSessionId.value === id) {
      if (sessions.value.length) {
        await switchSession(sessions.value[0].id)
      } else {
        currentSessionId.value = null
        messages.value = []
      }
    }
  } catch {
    /* 取消 */
  }
}

async function send() {
  const text = input.value.trim()
  if (!text) return
  if (!currentSessionId.value) {
    await newSession()
  }
  input.value = ''
  messages.value.push({ role: 'user', message: text })
  const replyIndex = messages.value.length
  messages.value.push({ role: 'assistant', message: '' })
  scrollBottom()
  streaming.value = true

  // 每次发送新建一个 controller
  abortController.value = new AbortController()

  try {
    await streamChat(
        currentSessionId.value,
        text,
        (chunk) => {
          messages.value[replyIndex].message += chunk
          scrollBottom()
        },
        abortController.value.signal   // 传 signal
    )
  } catch (e) {
    // 主动停止不算错误，不提示
    if (e.name === 'AbortError') {
      console.log('用户主动停止生成')
    } else {
      messages.value[replyIndex].message = '连接中断，请重试'
      showToast('连接中断，请重试')
    }
  } finally {
    streaming.value = false
    abortController.value = null
    scrollBottom()
  }
}

// 停止按钮
function stop() {
  if (abortController.value) {
    abortController.value.abort()   // 触发 fetch 中断，后端收到断开
    abortController.value = null
  }
  streaming.value = false
}

/** SSE 流式对话：fetch 解析 text/event-stream（axios 不支持流式） */
async function streamChat(sessionId, message, onChunk, signal) {
  const resp = await fetch(`/api/member/chat/${sessionId}/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${userStore.accessToken}`
    },
    body: JSON.stringify({ message }),
    signal   // ← 关键：传进去，abort 时 fetch 会中断
  })
  if (!resp.ok || !resp.body) {
    throw new Error('stream failed')
  }
  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop()
    for (const line of lines) {
      const trimmed = line.trim()
      if (trimmed.startsWith('data:')) {
        const data = trimmed.slice(5).trim()
        if (data) onChunk(data)
      }
    }
  }
}

function scrollBottom() {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.msg-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 10px;
}

.msg-row.user {
  justify-content: flex-end;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  flex: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

.msg-row.assistant .avatar {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  margin-right: 8px;
}

.msg-row.user .avatar {
  background: var(--text-secondary);
  margin-left: 8px;
}

.bubble {
  max-width: 78%;
  padding: 10px 12px;
  border-radius: 10px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-row.assistant .bubble {
  background: #fff;
  border: 1px solid var(--border-color);
}

.msg-row.user .bubble {
  background: var(--primary-color);
  color: #fff;
}

.input-bar {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border-top: 1px solid #ebedf0;
}

.input-field {
  flex: 1;
  background: #f7f8fa;
  border-radius: 8px;
}

.send-btn {
  flex-shrink: 0;
  height: 42px;
  min-width: 72px;
}

.session-list {
  padding: 16px;
}

.session-title {
  font-weight: 600;
  margin-bottom: 10px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  border-radius: 8px;
}

.session-item.active {
  background: var(--primary-tint);
  color: var(--primary-color);
}

.session-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
