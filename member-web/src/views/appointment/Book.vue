<template>
  <div class="page">
    <van-nav-bar title="预约体检" left-arrow @click-left="router.back()" />

    <div class="card" v-if="pkg">
      <div class="package-head">
        <span class="package-name">{{ pkg.name }}</span>
        <span class="package-price">{{ pkg.price }} 积分</span>
      </div>
      <div class="text-secondary">当前积分：<b>{{ userStore.points }}</b></div>
    </div>

    <!-- 日期选择 -->
    <van-calendar
      v-model:show="showCalendar"
      :min-date="today"
      color="var(--primary-color)"
      :row-height="44"
      @confirm="onDateConfirm"
    />
    <div class="card" @click="showCalendar = true">
      <div class="card-title">选择日期</div>
      <div v-if="selectedDate" class="date-value">{{ selectedDate }}</div>
      <div v-else class="text-secondary">点击选择日期</div>
    </div>

    <!-- 时段选择 -->
    <div class="card">
      <div class="card-title">选择时段</div>
      <van-loading v-if="loadingSlots" size="24" style="padding: 10px" />
      <div v-else-if="slots.length" class="slot-grid">
        <div
          v-for="s in slots"
          :key="s.id"
          class="slot-item"
          :class="{ selected: selectedSlotId === s.id, full: s.currentCount >= s.maxCount }"
          @click="selectSlot(s)"
        >
          <div class="slot-time">{{ s.timeRange }}</div>
          <div class="slot-count">{{ s.currentCount }}/{{ s.maxCount }}</div>
        </div>
      </div>
      <div v-else class="text-secondary">该日暂无可预约时段</div>
    </div>

    <div class="submit-wrap">
      <van-button round block type="primary" class="big-btn" :loading="booking" :disabled="!selectedSlotId" @click="onBook">
        确认预约（扣除 {{ pkg?.price || 0 }} 积分）
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast, showConfirmDialog } from 'vant'
import { getPackageDetail, listSlots, bookAppointment } from '../../api/appointment'
import { useUserStore } from '../../store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const pkg = ref(null)
const today = computed(() => new Date())
const showCalendar = ref(false)
const selectedDate = ref(null)
const slots = ref([])
const selectedSlotId = ref(null)
const loadingSlots = ref(false)
const booking = ref(false)

function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

onMounted(async () => {
  const res = await getPackageDetail(route.params.packageId)
  pkg.value = res.data
})

async function onDateConfirm(date) {
  // vant calendar 返回 JS Date 对象，格式化为 yyyy-MM-dd 字符串
  // 避免传给后端时 Date.toString() 导致 LocalDate 解析失败（"参数类型错误: date"）
  selectedDate.value = formatDate(date)
  showCalendar.value = false
  selectedSlotId.value = null
  await loadSlots()
}

async function loadSlots() {
  if (!selectedDate.value) return
  loadingSlots.value = true
  try {
    const res = await listSlots(route.params.packageId, selectedDate.value)
    slots.value = res.data || []
  } finally {
    loadingSlots.value = false
  }
}

function selectSlot(s) {
  if (s.currentCount >= s.maxCount) return
  selectedSlotId.value = s.id
}

async function onBook() {
  if (userStore.points < (pkg.value?.price || 0)) {
    return showConfirmDialog({ title: '积分不足', message: '当前积分不足以预约该套餐，先去参加活动赚积分吧' }).catch(() => {})
  }
  booking.value = true
  try {
    await bookAppointment({ packageId: pkg.value.id, slotId: selectedSlotId.value })
    showSuccessToast('预约成功')
    router.replace('/appointment/my')
  } finally {
    booking.value = false
  }
}
</script>

<style scoped>
.package-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.package-name {
  font-weight: 600;
  font-size: 1.1rem;
}
.package-price {
  color: #ee0a24;
  font-weight: 700;
}
.date-value {
  color: var(--primary-color);
  font-weight: 600;
}
.slot-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}
.slot-item {
  border: 1px solid #ebedf0;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
}
.slot-item.selected {
  border-color: var(--primary-color);
  background: var(--primary-tint);
  color: var(--primary-color);
}
.slot-item.full {
  color: #c8c9cc;
  border-color: #ebedf0;
  background: #f7f8fa;
}
.slot-time {
  font-weight: 600;
}
.slot-count {
  font-size: 0.8rem;
  color: #969799;
  margin-top: 4px;
}
.submit-wrap {
  padding: 16px;
}
</style>
