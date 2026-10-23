<template>
  <div class="login-page">
    <div class="bg-decor">
      <div class="blob blob-1"></div>
      <div class="blob blob-2"></div>
      <div class="blob blob-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="login-card">
      <div class="brand-side">
        <h1>智慧养老社区</h1>
        <p class="subtitle">养老服务数字化管理平台</p>
        <ul class="features">
          <li><el-icon><Check /></el-icon> 会员健康档案统一管理</li>
          <li><el-icon><Check /></el-icon> 体检预约与套餐灵活配置</li>
          <li><el-icon><Check /></el-icon> 活动与消息精准触达</li>
        </ul>
        <div class="foot">© 2026 智慧养老社区运营团队</div>
      </div>

      <div class="form-side">
        <span class="eyebrow">智慧养老社区 · 管理后台</span>
        <h2>欢迎回来</h2>
        <p class="form-desc">请使用管理员账号登录管理后台</p>
        <el-form :model="form" @submit.prevent>
          <el-form-item>
            <el-input v-model="form.phone" placeholder="管理员手机号" size="large">
              <template #prefix><el-icon><Iphone /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="onLogin">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <div class="captcha-row">
              <el-input v-model="form.code" placeholder="验证码" size="large" maxlength="4" @keyup.enter="onLogin">
                <template #prefix><el-icon><Key /></el-icon></template>
              </el-input>
              <img :src="captchaUrl" class="captcha-img" alt="验证码" title="点击刷新" @click="refreshCaptcha" />
            </div>
          </el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="onLogin">
            登 录
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Iphone, Lock, Key, Check } from '@element-plus/icons-vue'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ phone: '', password: '', code: '', captchaId: '' })
const loading = ref(false)
const captchaId = ref('')
const captchaUrl = ref('')

function refreshCaptcha() {
  captchaId.value = crypto.randomUUID()
  captchaUrl.value = '/api/captcha?id=' + captchaId.value
}

onMounted(refreshCaptcha)

async function onLogin() {
  if (!form.value.phone || !form.value.password || !form.value.code) {
    return ElMessage.warning('请输入手机号、密码和验证码')
  }
  loading.value = true
  try {
    form.value.captchaId = captchaId.value
    const res = await login(form.value)
    if (res.data.user.role !== 'ADMIN') {
      ElMessage.error('该账号不是管理员，无权登录管理端')
      return
    }
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.replace('/')
  } catch (e) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: 24px;
  background: linear-gradient(150deg, #eef2f9 0%, #e3eaf4 45%, #d5e2f0 100%);
}

/* 背景装饰：柔和的低饱和光晕 + 点阵 */
.bg-decor {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  animation: float 14s ease-in-out infinite;
}
.blob-1 {
  width: 520px;
  height: 520px;
  top: -140px;
  left: -120px;
  background: radial-gradient(circle, #b7c8ef, transparent 70%);
}
.blob-2 {
  width: 460px;
  height: 460px;
  bottom: -120px;
  right: -100px;
  background: radial-gradient(circle, #b2e0e4, transparent 70%);
  animation-delay: -5s;
}
.blob-3 {
  width: 340px;
  height: 340px;
  top: 46%;
  left: 62%;
  background: radial-gradient(circle, #cfc3ef, transparent 70%);
  animation-delay: -9s;
}
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-24px) scale(1.04); }
}
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(90, 118, 173, 0.16) 1px, transparent 1px);
  background-size: 26px 26px;
  mask-image: radial-gradient(ellipse at center, rgba(0, 0, 0, 0.7), transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse at center, rgba(0, 0, 0, 0.7), transparent 75%);
}

/* 登录卡片：轻玻璃拟态 */
.login-card {
  position: relative;
  z-index: 1;
  display: flex;
  width: 720px;
  max-width: 100%;
  min-height: 420px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(22px) saturate(140%);
  -webkit-backdrop-filter: blur(22px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 30px 80px rgba(31, 45, 70, 0.18), 0 8px 24px rgba(31, 45, 70, 0.08);
  animation: rise 0.6s ease;
}
@keyframes rise {
  from { opacity: 0; transform: translateY(24px); }
  to { opacity: 1; transform: none; }
}

/* 左侧品牌区 */
.brand-side {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 300px;
  flex-shrink: 0;
  padding: 40px 30px;
  color: #fff;
  background: linear-gradient(165deg, #5b76ad 0%, #41588a 55%, #364c78 100%);
  overflow: hidden;
}
.brand-side::before {
  content: '';
  position: absolute;
  top: -60px;
  left: -60px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.12), transparent 70%);
}
.brand-side::after {
  content: '';
  position: absolute;
  bottom: -90px;
  right: -70px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(190, 205, 238, 0.3), transparent 70%);
}
.brand-side h1 {
  position: relative;
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 2px;
}
.brand-side .subtitle {
  position: relative;
  margin: 0 0 34px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  letter-spacing: 0.5px;
}
.features {
  position: relative;
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: rgba(255, 255, 255, 0.88);
  font-size: 13px;
}
.features li {
  display: flex;
  align-items: center;
  gap: 10px;
}
.features .el-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  color: #e3ecff;
  font-size: 12px;
}
.brand-side .foot {
  position: relative;
  margin-top: auto;
  color: rgba(255, 255, 255, 0.42);
  font-size: 12px;
}

/* 右侧表单区 */
.form-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 44px 44px;
}
.eyebrow {
  display: block;
  margin-bottom: 10px;
  color: #8aa1c9;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 3px;
  text-transform: uppercase;
}
.form-side h2 {
  margin: 0 0 8px;
  color: #24344d;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
}
.form-side .form-desc {
  margin: 0 0 30px;
  color: #8a94a6;
  font-size: 13px;
}
.form-side :deep(.el-form-item) {
  margin-bottom: 20px;
}
.form-side :deep(.el-input__wrapper) {
  padding: 7px 14px;
  border-radius: 10px;
  background: #f4f7fb;
  box-shadow: 0 0 0 1px #e3e9f2 inset;
  transition: box-shadow 0.2s, background 0.2s;
}
.form-side :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #5b76ad inset, 0 6px 18px rgba(91, 118, 173, 0.12);
}
.form-side :deep(.el-input__inner) {
  height: 42px;
  font-size: 14px;
}
.form-side :deep(.el-input__prefix .el-icon) {
  color: #9aa8bd;
}
.form-side :deep(.el-input__wrapper.is-focus .el-input__prefix .el-icon) {
  color: #5b76ad;
}
.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.captcha-row :deep(.el-input) {
  flex: 1;
}
.captcha-img {
  height: 44px;
  width: 110px;
  border-radius: 8px;
  cursor: pointer;
  object-fit: cover;
  border: 1px solid #e3e9f2;
}
.login-btn {
  width: 100%;
  margin-top: 6px;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #5b76ad, #3f5888);
  box-shadow: 0 10px 22px rgba(79, 104, 160, 0.28);
  transition: transform 0.2s, box-shadow 0.2s;
}
.login-btn:hover {
  background: linear-gradient(135deg, #5b76ad, #3f5888);
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(79, 104, 160, 0.36);
}
.login-btn:active {
  transform: translateY(0);
}
.tip {
  margin-top: 20px;
  text-align: center;
  color: #aab4c4;
  font-size: 12px;
}

@media (max-width: 720px) {
  .brand-side {
    display: none;
  }
  .login-card {
    width: 380px;
    min-height: 0;
  }
  .form-side {
    padding: 36px 28px;
  }
}
</style>
