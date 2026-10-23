<template>
  <div class="page">
    <van-nav-bar title="消息详情" left-arrow @click-left="router.back()" />

    <van-empty v-if="loading" description="加载中..." />

    <div v-else-if="msg" class="card">
      <div class="detail-head">
        <span class="msg-type">{{ messageTypeMap[msg.type] || msg.type }}</span>
        <span v-if="msg.isRead === 0" class="dot"></span>
      </div>
      <div class="detail-title">{{ msg.title }}</div>
      <div class="text-secondary detail-time">{{ fmtTime(msg.createTime) }}</div>
      <div class="detail-content">{{ msg.content || '暂无内容' }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getMessageDetail, markMessageRead } from '../../api/message'
import { messageTypeMap, fmtTime } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const msg = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getMessageDetail(route.params.id)
    msg.value = res.data
    if (msg.value && msg.value.isRead === 0) {
      await markMessageRead(msg.value.id)
      msg.value.isRead = 1
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}
.msg-type {
  font-size: 0.8rem;
  color: var(--primary-color);
  background: var(--primary-tint);
  padding: 2px 8px;
  border-radius: 4px;
}
.dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #ee0a24;
}
.detail-title {
  font-size: 1.2rem;
  font-weight: 600;
  line-height: 1.5;
}
.detail-time {
  margin: 8px 0 14px;
  font-size: 0.8rem;
}
.detail-content {
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  color: #323233;
}
</style>
