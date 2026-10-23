<template>
  <div class="page">
    <van-nav-bar title="健康评测" left-arrow @click-left="router.back()" />

    <div class="card">
      <div class="card-title">评测问卷</div>
      <van-empty v-if="loading" description="加载中..." />
      <div v-for="q in questionnaires" :key="q.id" class="qa-item" @click="router.push(`/assessment/${q.id}`)">
        <div class="qa-title">{{ q.title }}</div>
        <div class="text-secondary">{{ q.description }}</div>
        <van-button size="mini" type="primary" round style="margin-top: 8px">开始评测</van-button>
      </div>
    </div>

    <div class="card">
      <div class="card-title">历史评测</div>
      <van-empty v-if="!history.length" description="暂无评测记录" />
      <div v-for="h in history" :key="h.id" class="history-item" @click="router.push(`/assessment/history/${h.id}`)">
        <div>
          <div class="hist-title">{{ h.questionnaireTitle }}</div>
          <div class="text-secondary">{{ fmtTime(h.createTime) }}</div>
        </div>
        <div class="hist-right">
          <div class="hist-score" :class="{ pass: h.score >= 60 }">
            {{ h.score != null ? h.score + ' 分' : '--' }}
          </div>
          <van-icon name="arrow" color="#c8c9cc" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listQuestionnaires, getAssessmentHistory } from '../../api/assessment'
import { fmtTime } from '../../utils/format'

const router = useRouter()
const questionnaires = ref([])
const history = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [q, h] = await Promise.all([listQuestionnaires(), getAssessmentHistory()])
    questionnaires.value = q.data || []
    history.value = h.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.qa-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 0;
}
.qa-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}
.hist-title {
  font-weight: 500;
}
.hist-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.hist-score {
  color: #ee0a24;
  font-weight: 700;
}
.hist-score.pass {
  color: #07c160;
}
</style>
