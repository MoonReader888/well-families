<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">智慧养老社区<br /><small>管理后台</small></div>
      <el-menu :default-active="route.path" router background-color="#001529" text-color="#a6adb4" active-text-color="#fff">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>仪表盘</span></el-menu-item>
        <el-sub-menu index="member">
          <template #title><el-icon><User /></el-icon><span>会员管理</span></template>
          <el-menu-item index="/members">会员列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="appointment">
          <template #title><el-icon><FirstAidKit /></el-icon><span>体检管理</span></template>
          <el-menu-item index="/appointment/packages">体检套餐</el-menu-item>
          <el-menu-item index="/appointment/list">预约管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/assessment"><el-icon><Document /></el-icon><span>评测管理</span></el-menu-item>
        <el-menu-item index="/activity"><el-icon><Calendar /></el-icon><span>活动管理</span></el-menu-item>
        <el-menu-item index="/message"><el-icon><Message /></el-icon><span>消息管理</span></el-menu-item>
        <el-menu-item index="/config"><el-icon><Setting /></el-icon><span>系统配置</span></el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-title">{{ route.meta.title }}</div>
        <el-dropdown @command="onCommand">
          <span class="admin-name">
            <el-icon><UserFilled /></el-icon>
            {{ userStore.user?.realName || userStore.user?.phone }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, User, UserFilled, FirstAidKit, Document, Calendar, Message, Setting, ArrowDown
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { logout } from '../api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

async function onCommand(cmd) {
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
    try {
      await logout(userStore.refreshToken)
    } catch {
      /* 忽略 */
    }
    userStore.clearLogin()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #001529;
}
.logo {
  color: #fff;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  padding: 18px 0;
  line-height: 1.4;
}
.logo small {
  font-weight: 400;
  color: #a6adb4;
  font-size: 12px;
}
.aside :deep(.el-menu) {
  border-right: none;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.admin-name {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
}
.main {
  background: #f0f2f5;
  padding: 16px;
}
</style>
