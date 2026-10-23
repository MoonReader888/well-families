<template>
  <div class="page">
    <div class="tab-hero">
      <div>
        <div class="hero-title">社区活动</div>
        <div class="hero-sub">丰富老年生活 · 结识邻里</div>
      </div>
      <div class="hero-actions">
        <van-button size="small" round plain icon="friends-o" @click="router.push('/activity/my')">我的活动</van-button>
      </div>
    </div>

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!activities.length" description="暂无活动" />

    <div v-for="a in activities" :key="a.id" class="card activity-card" @click="router.push(`/activity/${a.id}`)">
      <img v-if="a.coverUrl" :src="resolveImageUrl(a.coverUrl)" class="activity-cover" alt="活动封面" />
      <div class="activity-head">
        <span class="activity-title">{{ a.title }}</span>
        <van-tag type="primary" plain>{{ activityStatusMap[a.status] || a.status }}</van-tag>
      </div>
      <div class="text-secondary activity-info">
        <div><van-icon name="clock-o" /> {{ fmtTime(a.activityStart) }}</div>
        <div><van-icon name="friends-o" /> {{ a.currentParticipants }}/{{ a.maxParticipants }} 人</div>
      </div>
      <div v-if="a.registered" class="registered-tag">已报名</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listActivities } from '../../api/activity'
import { activityStatusMap, fmtTime, resolveImageUrl } from '../../utils/format'

const router = useRouter()
const activities = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await listActivities()
    activities.value = res.data || []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.activity-card {
  position: relative;
}
.activity-cover {
  display: block;
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 10px;
}
.activity-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.activity-title {
  flex: 1 1 auto;
  min-width: 0;
  font-weight: 600;
  font-size: 1.05rem;
  overflow-wrap: break-word;
}
.activity-head :deep(.van-tag) {
  flex-shrink: 0;
  white-space: nowrap;
}
.activity-info {
  display: flex;
  gap: 16px;
}
.registered-tag {
  position: absolute;
  right: 12px;
  bottom: 12px;
  color: #07c160;
  font-size: 0.85rem;
}
</style>
