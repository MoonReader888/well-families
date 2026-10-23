<template>
  <div class="page dashboard" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="c in cards" :key="c.label" class="stat-card" :style="{ '--accent': c.color }">
        <div class="stat-icon" :style="{ background: c.tint, color: c.color }">
          <el-icon :size="22"><component :is="c.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-num">{{ c.value }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <!-- 趋势 + 今日分布 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <div class="chart-card">
          <BaseChart :option="trendOption" height="320px" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="8">
        <div class="chart-card">
          <BaseChart :option="todayOption" height="320px" />
        </div>
      </el-col>
    </el-row>

    <!-- 状态分布 + 等级分布 -->
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <BaseChart :option="statusOption" height="320px" />
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <BaseChart :option="levelOption" height="320px" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { graphic } from 'echarts/core'
import { User, UserFilled, FirstAidKit, Star, Clock } from '@element-plus/icons-vue'
import { getDashboard } from '../../api/admin'
import BaseChart from '../../components/BaseChart.vue'

const loading = ref(false)
const data = ref({})

/* 柔和的莫兰迪色系，避免高饱和色刺激 */
const C = {
  blue: '#5a8bb8',
  teal: '#5f9a8a',
  amber: '#c29a5f',
  plum: '#967aa8',
  rose: '#c48a8a'
}
const MUTED_PALETTE = ['#5a8bb8', '#5f9a8a', '#c29a5f', '#967aa8', '#c48a8a', '#7d8aa0', '#9aa88f', '#a08bc4']

const titleStyle = { fontSize: 15, color: '#3a4a63', fontWeight: 600 }
const tooltipStyle = {
  backgroundColor: '#fff',
  borderColor: '#e6ebf1',
  borderWidth: 1,
  textStyle: { color: '#3a4a63' },
  extraCssText: 'box-shadow: 0 4px 14px rgba(0,0,0,0.08); border-radius: 8px;'
}
const axisLabelStyle = { color: '#7d8aa0' }

const cards = computed(() => [
  { label: '会员总数', value: data.value.memberTotal ?? '-', color: C.blue, tint: '#eef3f8', icon: User },
  { label: '今日新增会员', value: data.value.todayNewMembers ?? '-', color: C.teal, tint: '#edf4f1', icon: UserFilled },
  { label: '今日预约数', value: data.value.todayAppointments ?? '-', color: C.amber, tint: '#f8f2e8', icon: FirstAidKit },
  { label: '今日活动报名', value: data.value.todayActivityRegistrations ?? '-', color: C.plum, tint: '#f2eff5', icon: Star },
  { label: '待完成预约', value: data.value.pendingAppointments ?? '-', color: C.rose, tint: '#f7efef', icon: Clock }
])

const hexToRgba = (hex, alpha) => {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const lineArea = (color) =>
  new graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: hexToRgba(color, 0.16) },
    { offset: 1, color: hexToRgba(color, 0) }
  ])

const lineSeries = (name, color, list) => ({
  name,
  type: 'line',
  smooth: true,
  symbol: 'circle',
  symbolSize: 6,
  data: list,
  lineStyle: { width: 2, color },
  itemStyle: { color, borderColor: '#fff', borderWidth: 1 },
  areaStyle: { color: lineArea(color) }
})

const trendOption = computed(() => ({
  title: { text: '近7日业务趋势', left: 'center', textStyle: titleStyle },
  tooltip: { trigger: 'axis', ...tooltipStyle },
  legend: {
    data: ['新增会员', '预约', '活动报名'], bottom: 0, icon: 'circle',
    itemWidth: 8, itemHeight: 8, itemGap: 16, textStyle: { color: '#606a7a' }
  },
  grid: { left: 44, right: 20, top: 44, bottom: 44 },
  xAxis: {
    type: 'category', boundaryGap: false,
    data: data.value.last7Days || [],
    axisLine: { lineStyle: { color: '#e6ebf1' } },
    axisTick: { show: false },
    axisLabel: axisLabelStyle
  },
  yAxis: {
    type: 'value', minInterval: 1,
    splitLine: { lineStyle: { type: 'dashed', color: '#e6ebf1' } },
    axisLabel: axisLabelStyle
  },
  series: [
    lineSeries('新增会员', C.blue, data.value.newMemberTrend || []),
    lineSeries('预约', C.teal, data.value.appointmentTrend || []),
    lineSeries('活动报名', C.amber, data.value.registrationTrend || [])
  ]
}))

const pieSeries = (data) => ({
  type: 'pie',
  radius: ['45%', '70%'],
  center: ['50%', '46%'],
  itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
  label: { show: false },
  emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold', formatter: '{b}: {c}', color: '#3a4a63' } },
  data
})

const todayOption = computed(() => ({
  title: { text: '今日业务分布', left: 'center', textStyle: titleStyle },
  tooltip: { trigger: 'item', ...tooltipStyle, formatter: '{b}: {c}' },
  legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#606a7a' } },
  series: [
    pieSeries([
      { name: '新增会员', value: data.value.todayNewMembers ?? 0, itemStyle: { color: C.blue } },
      { name: '预约', value: data.value.todayAppointments ?? 0, itemStyle: { color: C.teal } },
      { name: '活动报名', value: data.value.todayActivityRegistrations ?? 0, itemStyle: { color: C.amber } }
    ])
  ]
}))

const statusOption = computed(() => ({
  title: { text: '预约状态分布', left: 'center', textStyle: titleStyle },
  tooltip: { trigger: 'item', ...tooltipStyle, formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#606a7a' } },
  series: [
    pieSeries(
      (data.value.appointmentStatus || []).map((s, i) => ({
        ...s,
        itemStyle: { color: MUTED_PALETTE[i % MUTED_PALETTE.length] }
      }))
    )
  ]
}))

const levelOption = computed(() => ({
  title: { text: '会员等级分布', left: 'center', textStyle: titleStyle },
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, ...tooltipStyle },
  grid: { left: 44, right: 20, top: 44, bottom: 30 },
  xAxis: {
    type: 'category',
    data: (data.value.memberLevels || []).map((m) => m.name),
    axisLine: { lineStyle: { color: '#e6ebf1' } },
    axisTick: { show: false },
    axisLabel: axisLabelStyle
  },
  yAxis: {
    type: 'value', minInterval: 1,
    splitLine: { lineStyle: { type: 'dashed', color: '#e6ebf1' } },
    axisLabel: axisLabelStyle
  },
  series: [{
    type: 'bar',
    barWidth: '45%',
    data: (data.value.memberLevels || []).map((m) => m.value),
    itemStyle: {
      borderRadius: [4, 4, 0, 0],
      color: new graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: '#6f9cc4' },
        { offset: 1, color: '#5a8bb8' }
      ])
    }
  }]
}))

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    data.value = res.data || {}
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.stat-card {
  position: relative;
  overflow: hidden;
  background: #fff;
  border-radius: 12px;
  padding: 18px 16px 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(31, 45, 61, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--accent);
  opacity: 0.75;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(31, 45, 61, 0.1);
}

.stat-icon {
  flex: none;
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
  color: #3a4a63;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  color: #7d8aa0;
  font-size: 13px;
  margin-top: 2px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 10px 12px 4px;
  box-shadow: 0 2px 8px rgba(31, 45, 61, 0.05);
  margin-bottom: 16px;
}
</style>
