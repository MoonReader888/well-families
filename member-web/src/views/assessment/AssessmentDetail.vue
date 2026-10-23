<template>
  <div class="page">
    <van-nav-bar title="健康评测" left-arrow @click-left="router.back()"/>

    <van-empty v-if="loading" description="加载中..."/>

    <!-- 评测结果 -->
    <div v-else-if="result">
      <div class="card result-card">
        <div class="score-circle" :class="{ pass: result.score >= 60 }">
          <div class="score-num">{{ result.score != null ? result.score : '--' }}</div>
          <div class="score-label">健康评分</div>
        </div>
        <div v-if="result.level" class="result-level">{{ result.level }}</div>
        <div class="result-title">{{ result.questionnaireTitle }}</div>
      </div>

      <div class="card">
        <div class="card-title">健康建议</div>

        <!-- 状态：生成中 -->
        <div v-if="result.status === 'PROCESSING'" class="suggestion generating">
          <van-loading size="18"/>
          健康建议生成中，请稍候…
        </div>

        <!-- 状态：失败 -->
        <div v-else-if="result.status === 'FAILED'" class="suggestion failed">
          {{ result.errorMsg || '生成失败，请稍后重试' }}
        </div>

        <!-- 状态：成功（或老数据无 status） -->
        <div v-else class="suggestion">
          {{ result.aiSuggestion || '暂无建议' }}
        </div>
      </div>

      <div class="actions">
        <van-button round block type="primary" class="big-btn" @click="router.back()">返回</van-button>
      </div>
    </div>

    <!-- 答题 -->
    <template v-else>
      <div class="card">
        <div class="card-title">{{ questionnaire.title }}</div>
        <div class="text-secondary">{{ questionnaire.description }}</div>
      </div>

      <div v-for="(q, idx) in questions" :key="q.id" class="card">
        <div class="q-title">{{ idx + 1 }}. {{ q.content }}</div>
        <!-- 单选 -->
        <van-radio-group v-if="q.type === 'SINGLE'" v-model="answers[q.id].value" direction="horizontal"
                         class="q-options">
          <van-radio v-for="(opt, i) in optionsOf(q)" :key="i" :name="i">{{ opt.label }}</van-radio>
        </van-radio-group>
        <!-- 多选 -->
        <van-checkbox-group v-else-if="q.type === 'MULTIPLE'" v-model="answers[q.id].value" class="q-options">
          <van-checkbox v-for="(opt, i) in optionsOf(q)" :key="i" :name="i">{{ opt.label }}</van-checkbox>
        </van-checkbox-group>
        <!-- 文本 -->
        <van-field
            v-else
            v-model="answers[q.id].value"
            type="textarea"
            rows="3"
            placeholder="请输入您的回答"
            maxlength="500"
            class="q-text"
        />
      </div>

      <div class="actions">
        <van-button round block type="primary" class="big-btn" :loading="submitting" @click="onSubmit">
          提交评测（完成送 20 积分）
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, onUnmounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {showSuccessToast, showToast} from 'vant'
import {getQuestionnaireDetail, submitAssessment, getAssessmentDetail} from '../../api/assessment'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const questionnaire = ref({})
const questions = ref([])
const answers = reactive({})
const result = ref(null)
const submitting = ref(false)

// 轮询定时器
let pollTimer = null
// 轮询次数上限为 60次*2秒 = 2分钟
let pollCount = 0
const MAX_POLL = 60

// localStorage 里存的 key，用于跨页面记住“待查看的评测结果”
const PENDING_KEY = 'pendingAssessmentId'

// ————生命周期
onMounted(async () => {
  // 1. 判断是否有“待查看的评测”
  const pendingId = localStorage.getItem(PENDING_KEY)

  // 如果有 pendingId，说明上次提交的结果还没看，直接进结果页轮询
  if (pendingId) {
    loading.value = false
    await loadResultAndPoll(pendingId)
    return
  }

  // 2. 否则正常加载问卷详情
  try {
    const res = await getQuestionnaireDetail(route.params.id)
    questionnaire.value = res.data
    questions.value = res.data.questions || []
    questions.value.forEach((q) => {
      answers[q.id] = {qid: q.id, type: q.type, value: q.type === 'MULTIPLE' ? [] : q.type === 'TEXT' ? '' : null}
    })
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  // 离开页面一定停掉轮询，否则后台一直发请求
  stopPoll()
})

// ————选项解析
function optionsOf(q) {
  if (!q.options) return []
  try {
    const arr = JSON.parse(q.options)
    if (!Array.isArray(arr)) return []
    // 兼容旧版纯字符串选项：归一化为 {label, score}
    return arr.map((o) =>
        typeof o === 'string' ? {label: o, score: 0} : {label: o.label || '', score: o.score ?? 0}
    )
  } catch {
    return []
  }
}

// ————提交评测
async function onSubmit() {
  const items = questions.value
      .map((q) => {
        const a = answers[q.id]
        if (a.value === null || a.value === '' || (Array.isArray(a.value) && a.value.length === 0)) return null
        return {qid: q.id, type: q.type, value: a.value}
      })
      .filter(Boolean)

  if (items.length < questions.value.length) {
    return showToast('请完成所有题目')
  }

  submitting.value = true
  try {
    const res = await submitAssessment({questionnaireId: questionnaire.value.id, answers: items})
    const data = res.data

    // 1. 把 resultId 存起来，用户离开页面再回来还能查
    localStorage.setItem(PENDING_KEY, data.id)

    showSuccessToast('评测完成！\n+20 积分')

    // 2. 直接切到结果页，先占位为“生成中”，避免闪“暂无建议”
    result.value = {...data, status: 'PROCESSING', aiSuggestion: null}

    // 3. 立刻拉一次 + 开始轮询
    loadResultAndPoll(data.id)
  } finally {
    submitting.value = false
  }
}

// ————加载结果 + 轮询
async function loadResultAndPoll(id) {
  let data
  try {
    const res = await getAssessmentDetail(id)
    data = res.data
  } catch (e) {
    // 拉取失败（比如任务不存在），清掉 pending，避免死循环
    localStorage.removeItem(PENDING_KEY)
    showToast('评测记录不存在')
    return
  }

  // SUCCESS 且建议非空，才算真完成
  const isDone = data.status === 'SUCCESS' && data.aiSuggestion
  const isFailed = data.status === 'FAILED'

  // 终态：直接展示
  if (isDone || isFailed || !data.status) {
    result.value = data
    // 成功 或 老数据无 status → 清掉标记
    if (isDone || !data.status) {
      localStorage.removeItem(PENDING_KEY)
    }
    return
  }

  // 非终态：保持“生成中”，再轮询
  result.value = {...data, status: 'PROCESSING'}
  startPoll(id)
}

/** 启动轮询：每 2 秒查一次 */
function startPoll(id) {
  stopPoll()               // 先清掉可能存在的旧定时器
  pollCount = 0
  pollTimer = setInterval(async () => {
    pollCount++
    if (pollCount > MAX_POLL) {
      stopPoll()
      showToast('生成时间较长，请稍后回到本页查看')
      return
    }

    try {
      const res = await getAssessmentDetail(id)
      const data = res.data

      const isDone = data.status === 'SUCCESS' && data.aiSuggestion
      const isFailed = data.status === 'FAILED'

      if (isDone) {
        result.value = data
        stopPoll()
        localStorage.removeItem(PENDING_KEY)
      } else if (isFailed) {
        result.value = data
        stopPoll()
      } else {
        // 还没完成：保持“生成中”，避免闪“暂无建议”
        result.value = {...data, status: 'PROCESSING'}
      }
    } catch (e) {
      // 单次请求失败不停止轮询，等下次重试
      console.warn('轮询请求失败', e)
    }
  }, 2000)
}

/** 停止轮询 */
function stopPoll() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}
</script>

<style scoped>
.q-title {
  font-weight: 600;
  margin-bottom: 10px;
  line-height: 1.5;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.q-text {
  background: #f7f8fa;
  border-radius: 8px;
  margin-top: 4px;
}

.actions {
  padding: 16px;
}

.result-card {
  text-align: center;
}

.score-circle {
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: #fde8e9;
  color: #ee0a24;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin: 0 auto 12px;
}

.score-circle.pass {
  background: #e8f8ee;
  color: #07c160;
}

.score-num {
  font-size: 2rem;
  font-weight: 700;
}

.score-label {
  font-size: 0.85rem;
}

.result-level {
  display: inline-block;
  margin-bottom: 8px;
  padding: 2px 12px;
  border-radius: 10px;
  background: var(--primary-tint);
  color: var(--primary-color);
  font-size: 0.85rem;
}

.result-title {
  font-weight: 600;
  font-size: 1.1rem;
}

.suggestion {
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 生成中样式 */
.suggestion.generating {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #969799;
}

/* 失败样式 */
.suggestion.failed {
  color: #ee0a24;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}
</style>