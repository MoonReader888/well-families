import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  // 公开路由
  { path: '/login', component: () => import('../views/auth/Login.vue'), meta: { public: true, title: '登录' } },
  { path: '/register', component: () => import('../views/auth/Register.vue'), meta: { public: true, title: '注册' } },
  { path: '/reset-password', component: () => import('../views/auth/ResetPassword.vue'), meta: { public: true, title: '找回密码' } },

  // 底部 Tab 页
  { path: '/', component: () => import('../views/tabs/Home.vue'), meta: { tab: true, title: '首页' } },
  { path: '/health', component: () => import('../views/health/HealthList.vue'), meta: { tab: true, title: '健康记录' } },
  { path: '/appointment', component: () => import('../views/appointment/PackageList.vue'), meta: { tab: true, title: '体检预约' } },
  { path: '/activity', component: () => import('../views/activity/ActivityList.vue'), meta: { tab: true, title: '社区活动' } },
  { path: '/profile', component: () => import('../views/profile/Profile.vue'), meta: { tab: true, title: '我的' } },

  // 子页面
  { path: '/health/new', component: () => import('../views/health/HealthForm.vue'), meta: { title: '录入健康数据' } },
  { path: '/health/trend', component: () => import('../views/health/HealthTrend.vue'), meta: { title: '健康趋势' } },
  { path: '/chat', component: () => import('../views/chat/Chat.vue'), meta: { title: 'AI 健康咨询' } },
  { path: '/assessment', component: () => import('../views/assessment/AssessmentList.vue'), meta: { title: '健康评测' } },
  { path: '/assessment/history/:id', component: () => import('../views/assessment/AssessmentHistoryDetail.vue'), meta: { title: '评测记录' } },
  { path: '/assessment/:id', component: () => import('../views/assessment/AssessmentDetail.vue'), meta: { title: '健康评测' } },
  { path: '/appointment/book/:packageId', component: () => import('../views/appointment/Book.vue'), meta: { title: '预约体检' } },
  { path: '/appointment/my', component: () => import('../views/appointment/MyAppointments.vue'), meta: { title: '我的预约' } },
  { path: '/activity/:id', component: () => import('../views/activity/ActivityDetail.vue'), meta: { title: '活动详情' } },
  { path: '/activity/my', component: () => import('../views/activity/MyActivities.vue'), meta: { title: '我的活动' } },
  { path: '/message', component: () => import('../views/message/MessageList.vue'), meta: { title: '消息通知' } },
  { path: '/announcements', component: () => import('../views/message/MessageList.vue'), meta: { title: '社区公告', announcement: true } },
  { path: '/message/:id', component: () => import('../views/message/MessageDetail.vue'), meta: { title: '消息详情' } },
  { path: '/profile/password', component: () => import('../views/profile/ChangePassword.vue'), meta: { title: '修改密码' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '智慧养老社区'
  if (!to.meta.public && !store.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.public && store.isLoggedIn && to.path === '/login') {
    return '/'
  }
  return true
})

export default router
