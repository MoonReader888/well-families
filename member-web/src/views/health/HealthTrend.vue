<template>
  <div class="page">
    <van-nav-bar title="健康趋势（近 6 个月）" left-arrow @click-left="router.back()" />

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!hasData" description="暂无足够数据" />

    <div v-for="t in processedTrend" v-else :key="t.indicator" class="card">
      <div class="trend-head">
        <div class="head-left">
          <div class="trend-name">{{ meta(t.indicator).name }}</div>
          <div v-if="meta(t.indicator).range" class="trend-sub">
            正常范围 {{ meta(t.indicator).range[0] }}–{{ meta(t.indicator).range[1] }} {{ meta(t.indicator).unit }}
          </div>
        </div>
        <span class="stat">{{ statText(t) }}</span>
      </div>

      <!-- SVG 折线图：单线 / 双线（血压） -->
      <svg
        v-if="t.points && t.points.length"
        :viewBox="`0 0 ${W} ${H}`"
        class="chart"
        preserveAspectRatio="xMidYMid meet"
      >
        <defs>
          <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" class="stop-primary" stop-opacity="0.3" />
            <stop offset="100%" class="stop-primary" stop-opacity="0.02" />
          </linearGradient>
        </defs>

        <!-- Y 轴网格线 + 刻度 -->
        <g>
          <line
            v-for="(g, i) in yGridLines(t)"
            :key="'g' + i"
            :x1="padL"
            :x2="W - padR"
            :y1="g.y"
            :y2="g.y"
            stroke="#ebedf0"
            stroke-width="1"
          />
          <text
            v-for="(g, i) in yGridLines(t)"
            :key="'gl' + i"
            :x="padL - 4"
            :y="g.y + 3"
            text-anchor="end"
            class="axis-text"
          >{{ g.label }}</text>
        </g>

        <!-- 正常范围带（血糖 / 心率 / BMI） -->
        <rect
          v-if="meta(t.indicator).range"
          :x="padL"
          :y="py(meta(t.indicator).range[1], t)"
          :width="chartW"
          :height="Math.max(0, py(meta(t.indicator).range[0], t) - py(meta(t.indicator).range[1], t))"
          class="fill-success"
          fill-opacity="0.1"
        />

        <!-- 双线：血压（收缩压红 + 舒张压蓝） -->
        <template v-if="meta(t.indicator).dual">
          <path
            :d="linePath(t.points, 'sys', t)"
            fill="none"
            stroke="#ee0a24"
            stroke-width="2"
            stroke-linejoin="round"
            stroke-linecap="round"
          />
          <path
            :d="linePath(t.points, 'dia', t)"
            fill="none"
            class="stroke-primary"
            stroke-width="2"
            stroke-linejoin="round"
            stroke-linecap="round"
          />
          <template v-for="(p, i) in t.points" :key="'bp' + i">
            <circle
              v-if="p.sys != null"
              :cx="px(i, t.points.length)"
              :cy="py(p.sys, t)"
              r="2.5"
              fill="#ee0a24"
            />
            <circle
              v-if="p.dia != null"
              :cx="px(i, t.points.length)"
              :cy="py(p.dia, t)"
              r="2.5"
              class="fill-primary"
            />
          </template>
          <!-- 图例 -->
          <g :transform="`translate(${padL}, 10)`">
            <line x1="0" y1="0" x2="14" y2="0" stroke="#ee0a24" stroke-width="2" />
            <text x="18" y="4" class="legend-text">收缩压</text>
            <line x1="68" y1="0" x2="82" y2="0" class="stroke-primary" stroke-width="2" />
            <text x="86" y="4" class="legend-text">舒张压</text>
          </g>
        </template>

        <!-- 单线 + 面积填充（体重）+ 数值标注 -->
        <template v-else>
          <path
            v-if="meta(t.indicator).area"
            :d="areaPath(t.points, t)"
            fill="url(#areaGrad)"
          />
          <path
            :d="linePath(t.points, 'avg', t)"
            fill="none"
            class="stroke-primary"
            stroke-width="2"
            stroke-linejoin="round"
            stroke-linecap="round"
          />
          <template v-for="(p, i) in t.points" :key="'pt' + i">
            <circle :cx="px(i, t.points.length)" :cy="py(p.avg, t)" r="2.5" class="fill-primary" />
            <text
              :x="px(i, t.points.length)"
              :y="py(p.avg, t) - 8"
              text-anchor="middle"
              class="value-text"
            >{{ p.avg }}</text>
          </template>
        </template>

        <!-- X 轴：月份 -->
        <text
          v-for="(p, i) in t.points"
          :key="'x' + i"
          :x="px(i, t.points.length)"
          :y="H - 8"
          text-anchor="middle"
          class="axis-text"
        >{{ p.month.slice(5) }}</text>
      </svg>
      <div v-else class="text-secondary">该指标暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHealthTrend } from '../../api/health'

const router = useRouter()
const trend = ref([])
const loading = ref(true)

// SVG 画布参数
const W = 320
const H = 200
const padL = 30
const padR = 14
const padT = 24
const padB = 28
const chartW = W - padL - padR
const chartH = H - padT - padB

// 指标元数据：名称 / 单位 / 正常范围 / 是否双线 / 是否面积图
const indicatorMeta = {
  bloodPressure: { name: '血压', unit: 'mmHg', dual: true },
  bloodSugar: { name: '血糖', unit: 'mmol/L', range: [3.9, 6.1] },
  heartRate: { name: '心率', unit: '次/分', range: [60, 100] },
  weight: { name: '体重', unit: 'kg', area: true },
  bmi: { name: 'BMI', unit: '', range: [18.5, 24] }
}

function meta(key) {
  return indicatorMeta[key] || { name: key, unit: '' }
}

const hasData = computed(() => trend.value.some((t) => t.points && t.points.length))

// 合并 systolic + diastolic 成一项 bloodPressure（双线图）
const processedTrend = computed(() => {
  const sys = trend.value.find((t) => t.indicator === 'systolic')
  const dia = trend.value.find((t) => t.indicator === 'diastolic')
  const result = []
  if (sys || dia) {
    result.push({ indicator: 'bloodPressure', points: mergePoints(sys?.points, dia?.points) })
  }
  trend.value
    .filter((t) => !['systolic', 'diastolic'].includes(t.indicator))
    .forEach((t) => result.push(t))
  return result
})

function mergePoints(sys, dia) {
  const map = new Map()
  sys?.forEach((p) => map.set(p.month, { month: p.month, sys: p.avg ?? null, dia: null }))
  dia?.forEach((p) => {
    const exist = map.get(p.month)
    if (exist) exist.dia = p.avg ?? null
    else map.set(p.month, { month: p.month, sys: null, dia: p.avg ?? null })
  })
  return [...map.values()].sort((a, b) => a.month.localeCompare(b.month))
}

onMounted(async () => {
  try {
    const res = await getHealthTrend()
    trend.value = (res.data || []).filter((t) => t.points && t.points.length)
  } finally {
    loading.value = false
  }
})

// Y 轴范围：既包含数据点，也包含正常区间，使正常带显示在中间
function yScale(t) {
  const m = meta(t.indicator)
  const vals = []
  if (m.dual) {
    t.points.forEach((p) => {
      if (p.sys != null) vals.push(Number(p.sys))
      if (p.dia != null) vals.push(Number(p.dia))
    })
  } else {
    t.points.forEach((p) => {
      if (p.avg != null) vals.push(Number(p.avg))
    })
  }
  if (m.range) vals.push(m.range[0], m.range[1])
  let min = Math.min(...vals)
  let max = Math.max(...vals)
  const span = max - min || 1
  min = min - span * 0.12
  max = max + span * 0.12
  return { min: Math.floor(min), max: Math.ceil(max) }
}

function py(v, t) {
  if (v == null) return 0
  const { min, max } = yScale(t)
  return padT + chartH - ((Number(v) - min) / (max - min)) * chartH
}

function px(i, n) {
  if (n <= 1) return padL + chartW / 2
  return padL + (chartW * i) / (n - 1)
}

// Y 轴网格线（4 等分，不画底部那条避免与 X 轴重叠）
function yGridLines(t) {
  const { min, max } = yScale(t)
  const steps = 4
  const lines = []
  for (let i = 1; i <= steps; i++) {
    const v = min + ((max - min) * i) / steps
    lines.push({ y: py(v, t), label: Math.round(v) })
  }
  return lines
}

// 折线 path：支持缺失值断开
function linePath(points, key, t) {
  const vals = points.map((p) => p[key])
  const n = vals.length
  let d = ''
  let started = false
  for (let i = 0; i < n; i++) {
    if (vals[i] == null) {
      started = false
      continue
    }
    const x = px(i, n)
    const y = py(vals[i], t)
    d += `${started ? 'L' : 'M'}${x.toFixed(1)},${y.toFixed(1)} `
    started = true
  }
  return d.trim()
}

// 面积 path：折线 + 下方填充
function areaPath(points, t) {
  const vals = points.map((p) => p.avg)
  const n = vals.length
  const baseline = padT + chartH
  let d = ''
  let started = false
  let firstX = 0
  let lastX = 0
  for (let i = 0; i < n; i++) {
    if (vals[i] == null) {
      if (started) {
        d += `L${lastX.toFixed(1)},${baseline.toFixed(1)} L${firstX.toFixed(1)},${baseline.toFixed(1)} Z `
        started = false
      }
      continue
    }
    const x = px(i, n)
    const y = py(vals[i], t)
    if (!started) {
      firstX = x
      d += `M${x.toFixed(1)},${baseline.toFixed(1)} L${x.toFixed(1)},${y.toFixed(1)} `
      started = true
    } else {
      d += `L${x.toFixed(1)},${y.toFixed(1)} `
    }
    lastX = x
  }
  if (started) {
    d += `L${lastX.toFixed(1)},${baseline.toFixed(1)} Z`
  }
  return d.trim()
}

function statText(t) {
  if (t.indicator === 'bloodPressure') {
    const sysVals = t.points.map((p) => p.sys).filter((v) => v != null)
    const diaVals = t.points.map((p) => p.dia).filter((v) => v != null)
    const sa = sysVals.length ? (sysVals.reduce((a, b) => a + Number(b), 0) / sysVals.length).toFixed(0) : null
    const da = diaVals.length ? (diaVals.reduce((a, b) => a + Number(b), 0) / diaVals.length).toFixed(0) : null
    return `收缩压均值 ${sa ?? '-'} / 舒张压均值 ${da ?? '-'}`
  }
  return `均值 ${t.avg ?? '-'} / 最高 ${t.max ?? '-'} / 最低 ${t.min ?? '-'}`
}
</script>

<style scoped>
.trend-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
  gap: 8px;
}
.head-left {
  flex: 1;
}
.trend-name {
  font-weight: 600;
  font-size: 1.05rem;
}
.trend-sub {
  font-size: 0.75rem;
  color: var(--success-color);
  margin-top: 2px;
}
.stat {
  font-size: 0.78rem;
  color: #969799;
  text-align: right;
  white-space: nowrap;
}
.chart {
  width: 100%;
  display: block;
}
.chart .stroke-primary {
  stroke: var(--primary-color);
}
.chart .fill-primary {
  fill: var(--primary-color);
}
.chart .fill-success {
  fill: var(--success-color);
}
.chart .stop-primary {
  stop-color: var(--primary-color);
}
.axis-text {
  font-size: 10px;
  fill: #969799;
}
.value-text {
  font-size: 9px;
  fill: var(--primary-color);
  font-weight: 600;
}
.legend-text {
  font-size: 10px;
  fill: #646566;
}
</style>
