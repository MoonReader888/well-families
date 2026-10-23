import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

const routes = [
  { path: '/login', component: () => import('../views/login/Login.vue'), meta: { public: true, title: '登录' } },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/dashboard/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'members', component: () => import('../views/member/MemberList.vue'), meta: { title: '会员管理' } },
      { path: 'appointment/packages', component: () => import('../views/appointment/PackageList.vue'), meta: { title: '体检套餐' } },
      { path: 'appointment/list', component: () => import('../views/appointment/AppointmentList.vue'), meta: { title: '预约管理' } },
      { path: 'assessment', component: () => import('../views/assessment/QuestionnaireList.vue'), meta: { title: '评测管理' } },
      { path: 'activity', component: () => import('../views/activity/ActivityList.vue'), meta: { title: '活动管理' } },
      { path: 'message', component: () => import('../views/message/MessageList.vue'), meta: { title: '消息管理' } },
      { path: 'config', component: () => import('../views/config/ConfigList.vue'), meta: { title: '系统配置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const store = useUserStore()
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '智慧养老社区管理端'
  if (to.meta.public) {
    return true
  }
  if (!store.isLoggedIn) {
    return { path: '/login' }
  }
  if (!store.isAdmin) {
    store.clearLogin()
    return { path: '/login' }
  }
  return true
})

export default router
