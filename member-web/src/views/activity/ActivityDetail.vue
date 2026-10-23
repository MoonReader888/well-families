<template>
  <div class="page">
    <van-nav-bar title="活动详情" left-arrow @click-left="router.back()" />

    <van-empty v-if="!activity" description="加载中..." />

    <template v-else>
      <div class="card">
        <img v-if="activity.coverUrl" :src="resolveImageUrl(activity.coverUrl)" class="detail-cover" alt="活动封面" />
        <div class="detail-head">
          <span class="detail-title">{{ activity.title }}</span>
          <van-tag type="primary" plain>{{ activityStatusMap[activity.status] || activity.status }}</van-tag>
        </div>
        <div class="info-row text-secondary">
          <div>活动时间：{{ fmtTime(activity.activityStart) }} ~ {{ fmtTime(activity.activityEnd) }}</div>
          <div>报名时间：{{ fmtTime(activity.registrationStart) }} ~ {{ fmtTime(activity.registrationEnd) }}</div>
          <div>报名人数：{{ activity.currentParticipants }}/{{ activity.maxParticipants }} 人</div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">活动内容</div>
        <div class="content">{{ activity.content || '暂无介绍' }}</div>
      </div>

      <div class="actions">
        <!-- 已报名 -->
        <template v-if="activity.registered">
          <van-tag v-if="activity.checkInStatus === 'CHECKED_IN'" type="success" size="large">已签到</van-tag>
          <van-button v-else round block type="primary" class="big-btn" @click="onCheckIn">
            签到（送 50 积分）
          </van-button>
        </template>
        <!-- 未报名 -->
        <template v-else>
          <van-button round block type="primary" class="big-btn" :disabled="activity.status !== 'REGISTRATING'" @click="onRegister">
            立即报名
          </van-button>
          <div v-if="activity.status !== 'REGISTRATING'" class="text-secondary tip">当前不在报名时间</div>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast, showConfirmDialog } from 'vant'
import { getActivityDetail, registerActivity, checkInActivity } from '../../api/activity'
import { activityStatusMap, fmtTime, resolveImageUrl } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const activity = ref(null)

onMounted(load)

async function load() {
  const res = await getActivityDetail(route.params.id)
  activity.value = res.data
}

async function onRegister() {
  try {
    await showConfirmDialog({ title: '报名', message: '确认报名该活动吗？' })
    await registerActivity(activity.value.id)
    showSuccessToast('报名成功')
    load()
  } catch {
    /* 取消 */
  }
}

async function onCheckIn() {
  try {
    await showConfirmDialog({ title: '签到', message: '确认现场签到？签到将获得 50 积分。' })
    await checkInActivity(activity.value.id)
    showSuccessToast('签到成功，+50 积分')
    load()
  } catch {
    /* 取消 */
  }
}
</script>

<style scoped>
.detail-cover {
  display: block;
  width: 100%;
  height: 190px;
  object-fit: cover;
  border-radius: 10px;
  margin-bottom: 12px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}
.detail-title {
  flex: 1 1 auto;
  min-width: 0;
  font-weight: 600;
  font-size: 1.15rem;
  overflow-wrap: break-word;
}
.detail-head :deep(.van-tag) {
  flex-shrink: 0;
  white-space: nowrap;
}
.info-row {
  line-height: 1.8;
}
.content {
  line-height: 1.7;
  white-space: pre-wrap;
}
.actions {
  padding: 16px;
}
.tip {
  text-align: center;
  margin-top: 10px;
}
</style>
