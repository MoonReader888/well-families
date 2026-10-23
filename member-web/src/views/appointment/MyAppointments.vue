<template>
  <div class="page">
    <van-nav-bar title="我的预约" left-arrow @click-left="router.back()" />

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!list.length" description="暂无预约记录" />

    <div v-for="a in list" :key="a.id" class="card">
      <div class="appt-head">
        <span class="appt-package">{{ a.packageName }}</span>
        <van-tag :color="appointmentStatusMap[a.status]?.color">
          {{ appointmentStatusMap[a.status]?.text || a.status }}
        </van-tag>
      </div>
      <div class="appt-info text-secondary">
        <div>{{ a.appointDate }} {{ a.timeRange }}</div>
        <div>共 {{ a.price }} 积分</div>
      </div>
      <div class="appt-actions">
        <van-button
          v-if="['PENDING', 'CONFIRMED'].includes(a.status)"
          size="small"
          round
          plain
          type="danger"
          @click="onCancel(a)"
        >
          取消预约
        </van-button>
        <van-button
          v-if="a.reportDownloadUrl"
          size="small"
          round
          type="primary"
          @click="downloadReport(a)"
        >
          查看报告
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showSuccessToast } from 'vant'
import { getMyAppointments, cancelAppointment } from '../../api/appointment'
import { appointmentStatusMap } from '../../utils/format'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const list = ref([])
const loading = ref(true)

onMounted(load)

async function load() {
  try {
    const res = await getMyAppointments()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function onCancel(a) {
  try {
    await showConfirmDialog({
      title: '取消预约',
      message: `确定取消 ${a.packageName}（${a.appointDate} ${a.timeRange}）吗？积分将退还。`
    })
    await cancelAppointment(a.id)
    showSuccessToast('已取消')
    load()
  } catch {
    /* 取消 */
  }
}

function downloadReport(a) {
  // 带认证 token 的下载：走 fetch blob
  fetch(a.reportDownloadUrl, { headers: { Authorization: `Bearer ${userStore.accessToken}` } })
    .then((resp) => resp.blob())
    .then((blob) => {
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `体检报告-${a.packageName}.pdf`
      link.click()
      URL.revokeObjectURL(url)
    })
    .catch(() => {})
}
</script>

<style scoped>
.appt-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.appt-package {
  font-weight: 600;
  font-size: 1.05rem;
}
.appt-info {
  line-height: 1.6;
}
.appt-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}
</style>
