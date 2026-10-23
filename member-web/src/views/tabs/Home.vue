<template>
  <div class="page">
    <!-- 用户概览 -->
    <div class="user-card">
      <div class="user-info">
        <div>
          <div class="user-name">{{ userStore.user?.realName || '亲爱的用户' }}</div>
          <div class="user-sub text-secondary">
            {{ userStore.user?.phone }}
          </div>
        </div>
      </div>
      <div class="msg-btn" @click="router.push('/message')">
        <van-icon name="bell" size="26" color="#fff" />
        <span class="msg-btn-text">消息通知</span>
        <span class="msg-btn-unread">{{ unread > 0 ? unread : ' ' }}</span>
      </div>
    </div>

    <!-- 记录健康数据 -->
    <div class="card entry-row" @click="router.push('/health/new')">
      <van-icon name="edit" color="#07c160" />
      <div class="entry-body">
        <div class="entry-label">记录健康数据</div>
        <div class="entry-text">血压 · 血糖 · 心率 · 体重</div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="grid">
      <div v-for="item in entries" :key="item.path" class="grid-item card" @click="router.push(item.path)">
        <van-icon :name="item.icon" size="30" :color="item.color" />
        <div class="grid-text">{{ item.label }}</div>
      </div>
    </div>

    <!-- 社区公告（系统消息） -->
    <div class="card entry-row" @click="router.push('/announcements')">
      <van-icon name="bullhorn-o" color="#ff976a" />
      <div class="entry-body">
        <div class="entry-label">社区公告</div>
        <div class="entry-text">
          {{ latestAnnouncement ? latestAnnouncement.title : '暂无社区公告' }}
        </div>
      </div>
      <van-badge v-if="systemUnread > 0" :content="systemUnread" class="msg-badge" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { getAnnouncements, getUnreadCount } from '../../api/message'

const router = useRouter()
const userStore = useUserStore()
const latestAnnouncement = ref(null)
const systemUnread = ref(0)
const unread = ref(0)

const entries = [
  { path: '/chat', label: 'AI 健康咨询', icon: 'chat-o', color: 'var(--primary-color)' },
  { path: '/health/trend', label: '健康趋势', icon: 'chart-trending-o', color: '#7232dd' },
  { path: '/assessment', label: '健康评测', icon: 'notes-o', color: '#ff976a' },
  { path: '/appointment/my', label: '我的预约', icon: 'orders-o', color: '#ee0a24' }
]

onMounted(async () => {
  try {
    const res = await getAnnouncements()
    const system = res.data || []
    latestAnnouncement.value = system[0] || null
    systemUnread.value = system.filter((m) => m.isRead === 0).length
    const unreadRes = await getUnreadCount()
    unread.value = unreadRes.data
  } catch {
    /* 忽略 */
  }
})
</script>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  border-radius: 12px;
  padding: 20px 8px 20px 16px;
  color: #fff;
  margin-bottom: 12px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-name {
  font-size: 1.2rem;
  font-weight: 600;
}
.user-sub {
  color: rgba(255, 255, 255, 0.85);
}
.msg-btn {
  margin-left: auto;
  flex: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  padding: 10px 18px;
}
.msg-btn-text {
  font-size: 0.8rem;
  color: #fff;
}
.msg-btn-unread {
  font-size: 0.7rem;
  line-height: 1;
  color: #fff;
  opacity: 0.9;
}
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}
.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 8px;
  margin-bottom: 0;
}
.grid-text {
  font-size: 0.9rem;
}
.entry-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.entry-body {
  flex: 1;
  min-width: 0;
}
.entry-label {
  font-weight: 600;
}
.entry-text {
  margin-top: 2px;
  font-size: 0.85rem;
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.msg-badge {
  margin-left: auto;
}
</style>
