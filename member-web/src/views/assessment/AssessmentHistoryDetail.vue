<template>
  <div class="page">
    <van-nav-bar title="评测记录" left-arrow @click-left="router.back()" />

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!result" description="暂无评测记录" />

    <template v-else>
      <div class="card result-card">
        <div class="score-circle" :class="{ pass: result.score >= 60 }">
          <div class="score-num">{{ result.score != null ? result.score : '--' }}</div>
          <div class="score-label">健康评分</div>
        </div>
        <div v-if="result.level" class="result-level">{{ result.level }}</div>
        <div class="result-title">{{ result.questionnaireTitle }}</div>
        <div class="text-secondary result-time">{{ result.createTime }}</div>
      </div>
      <div class="card">
        <div class="card-title">健康建议</div>
        <div class="suggestion">{{ result.aiSuggestion || '暂无建议' }}</div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getAssessmentDetail } from '../../api/assessment'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const result = ref(null)

onMounted(async () => {
  try {
    const res = await getAssessmentDetail(route.params.id)
    result.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
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
.result-time {
  margin-top: 6px;
  font-size: 0.85rem;
}
.suggestion {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
