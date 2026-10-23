<template>
  <div class="page">
    <van-nav-bar title="我的活动" left-arrow @click-left="router.back()" />

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!list.length" description="还没有报名活动" />

    <div v-for="a in list" :key="a.id" class="card">
      <div class="act-head" @click="router.push(`/activity/${a.id}`)">
        <span class="act-title">{{ a.title }}</span>
        <van-tag :type="a.checkInStatus === 'CHECKED_IN' ? 'success' : 'warning'">
          {{ checkInStatusMap[a.checkInStatus] || a.checkInStatus }}
        </van-tag>
      </div>
      <div class="text-secondary">活动时间：{{ fmtTime(a.activityStart) }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyActivities } from '../../api/activity'
import { checkInStatusMap, fmtTime } from '../../utils/format'

const router = useRouter()
const list = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getMyActivities()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.act-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.act-title {
  flex: 1 1 auto;
  min-width: 0;
  font-weight: 600;
  font-size: 1.05rem;
  overflow-wrap: break-word;
}
.act-head :deep(.van-tag) {
  flex-shrink: 0;
  white-space: nowrap;
}
</style>
