<template>
  <div class="page">
    <van-nav-bar :title="title" left-arrow @click-left="router.back()" />

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!list.length" :description="emptyText" />

    <div
      v-for="m in list"
      :key="m.id"
      class="card msg-card"
      @click="router.push(`/message/${m.id}`)"
    >
      <div class="msg-head">
        <span v-if="!isAnnouncement" class="msg-type">{{ messageTypeMap[m.type] || m.type }}</span>
        <span class="msg-title">{{ m.title }}</span>
        <span v-if="m.isRead === 0" class="dot"></span>
      </div>
      <div class="text-secondary msg-time">{{ fmtTime(m.createTime) }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getMessages, getAnnouncements } from '../../api/message'
import { messageTypeMap, fmtTime } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const list = ref([])
const loading = ref(true)

const isAnnouncement = computed(() => !!route.meta.announcement)
const title = computed(() => route.meta.title || '消息通知')
const emptyText = computed(() => (isAnnouncement.value ? '暂无社区公告' : '暂无消息'))

onMounted(load)

async function load() {
  try {
    const res = isAnnouncement.value ? await getAnnouncements() : await getMessages()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.msg-card {
  position: relative;
}
.msg-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg-type {
  flex: none;
  font-size: 0.8rem;
  color: var(--primary-color);
  background: var(--primary-tint);
  padding: 2px 8px;
  border-radius: 4px;
}
.msg-title {
  flex: 1;
  min-width: 0;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dot {
  flex: none;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #ee0a24;
}
.msg-time {
  margin-top: 6px;
  font-size: 0.8rem;
}
</style>
