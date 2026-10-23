<template>
  <div class="page">
    <div class="tab-hero">
      <div>
        <div class="hero-title">健康记录</div>
        <div class="hero-sub">记录每一次身体变化</div>
      </div>
      <div class="hero-actions">
        <van-button size="small" round plain icon="plus" @click="router.push('/health/new')">记录</van-button>
        <van-button size="small" round plain icon="chart-trending-o" @click="router.push('/health/trend')">趋势</van-button>
      </div>
    </div>

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!records.length" description="暂无健康记录，点击右上角记录吧" />

    <div v-for="r in records" :key="r.id" class="card">
      <div class="record-head">
        <span class="record-time">{{ fmtTime(r.recordedAt) }}</span>
      </div>
      <div class="metric-grid">
        <div v-if="r.systolic != null" class="metric">
          <div class="metric-value">{{ r.systolic }}<span class="unit">mmHg</span></div>
          <div class="metric-label">收缩压</div>
        </div>
        <div v-if="r.diastolic != null" class="metric">
          <div class="metric-value">{{ r.diastolic }}<span class="unit">mmHg</span></div>
          <div class="metric-label">舒张压</div>
        </div>
        <div v-if="r.bloodSugar != null" class="metric">
          <div class="metric-value">{{ r.bloodSugar }}<span class="unit">mmol/L</span></div>
          <div class="metric-label">血糖</div>
        </div>
        <div v-if="r.heartRate != null" class="metric">
          <div class="metric-value">{{ r.heartRate }}<span class="unit">次/分</span></div>
          <div class="metric-label">心率</div>
        </div>
        <div v-if="r.weight != null" class="metric">
          <div class="metric-value">{{ r.weight }}<span class="unit">kg</span></div>
          <div class="metric-label">体重</div>
        </div>
        <div v-if="r.displayBmi != null" class="metric">
          <div class="metric-value" :class="bmiClass(r.displayBmi)">{{ r.displayBmi }}<span class="unit">BMI</span></div>
          <div class="metric-label">{{ bmiText(r.displayBmi) }}</div>
        </div>
      </div>
      <div v-if="r.memo" class="text-secondary record-memo">备注：{{ r.memo }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHealthHistory } from '../../api/health'
import { getProfile } from '../../api/profile'
import { fmtTime, bmiText } from '../../utils/format'

const router = useRouter()
const records = ref([])
const loading = ref(true)
const height = ref(null)

onMounted(async () => {
  try {
    const res = await getHealthHistory()
    records.value = res.data || []
    try {
      const profileRes = await getProfile()
      height.value = profileRes.data?.height || null
    } catch {
      /* 身高获取失败时只展示已存储的 BMI */
    }
    records.value = records.value.map((r) => ({ ...r, displayBmi: calcBmi(r) }))
  } finally {
    loading.value = false
  }
})

// BMI：优先用后端已存的，否则用基本资料身高 + 本记录体重自动计算
function calcBmi(r) {
  if (r.bmi != null) return Number(r.bmi)
  if (r.weight != null && height.value) {
    const hm = Number(height.value) / 100
    return Math.round((Number(r.weight) / (hm * hm)) * 10) / 10
  }
  return null
}

function bmiClass(bmi) {
  if (bmi < 18.5 || bmi >= 24) return 'bmi-warn'
  return 'bmi-ok'
}
</script>

<style scoped>
.record-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.record-time {
  color: #969799;
  font-size: 0.9rem;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.metric {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
}
.metric-value {
  font-size: 1.2rem;
  font-weight: 600;
}
.metric-value.bmi-ok {
  color: #07c160;
}
.metric-value.bmi-warn {
  color: #ee0a24;
}
.unit {
  font-size: 0.7rem;
  color: #969799;
  margin-left: 2px;
}
.metric-label {
  font-size: 0.8rem;
  color: #969799;
  margin-top: 4px;
}
.record-memo {
  margin-top: 8px;
}
</style>
