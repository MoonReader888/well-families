<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <van-tabbar v-if="route.meta.tab" v-model="active" @change="onTabChange" safe-area-inset-bottom>
    <van-tabbar-item name="/" icon="wap-home-o">首页</van-tabbar-item>
    <van-tabbar-item name="/health" icon="records-o">健康</van-tabbar-item>
    <van-tabbar-item name="/appointment" icon="medal-o">预约</van-tabbar-item>
    <van-tabbar-item name="/activity" icon="friends-o">活动</van-tabbar-item>
    <van-tabbar-item name="/profile" icon="user-o">我的</van-tabbar-item>
  </van-tabbar>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const active = ref(route.path)

watch(
  () => route.path,
  (path) => {
    active.value = path
  }
)

function onTabChange(path) {
  router.replace(path)
}
</script>
