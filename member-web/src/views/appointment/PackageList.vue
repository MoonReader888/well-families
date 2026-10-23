<template>
  <div class="page">
    <div class="tab-hero">
      <div>
        <div class="hero-title">体检预约</div>
        <div class="hero-sub">健康体检 · 一站式服务</div>
      </div>
      <div class="hero-actions">
        <van-button size="small" round plain icon="orders-o" @click="router.push('/appointment/my')">我的预约</van-button>
      </div>
    </div>

    <van-empty v-if="loading" description="加载中..." />
    <van-empty v-else-if="!packages.length" description="暂无可预约套餐" />

    <div v-for="pkg in packages" :key="pkg.id" class="card package-card">
      <div class="package-head">
        <span class="package-name">{{ pkg.name }}</span>
        <span class="package-price">{{ pkg.price }}<small> 积分</small></span>
      </div>
      <div v-if="pkg.suitablePeople" class="text-secondary">适合：{{ pkg.suitablePeople }}</div>
      <div v-if="pkg.description" class="text-secondary package-desc">{{ pkg.description }}</div>
      <div v-if="itemsOf(pkg).length" class="items">
        <span v-for="item in itemsOf(pkg)" :key="item" class="item-tag">{{ item }}</span>
      </div>
      <van-button type="primary" size="small" round class="book-btn" @click="router.push(`/appointment/book/${pkg.id}`)">
        立即预约
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listPackages } from '../../api/appointment'
import { parseItems } from '../../utils/format'

const router = useRouter()
const packages = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await listPackages()
    packages.value = res.data || []
  } finally {
    loading.value = false
  }
})

function itemsOf(pkg) {
  return parseItems(pkg.items)
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
  font-size: 1.2rem;
  font-weight: 700;
}
.package-desc {
  margin-top: 4px;
}
.items {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.item-tag {
  background: var(--primary-tint);
  color: var(--primary-color);
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 0.8rem;
}
.book-btn {
  margin-top: 12px;
}
</style>
