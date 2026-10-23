<template>
  <div class="page auth-page">
    <div class="auth-header">
      <div class="auth-logo"><van-icon name="shield-o" size="34" color="#fff" /></div>
      <h2>智慧养老社区</h2>
      <p class="text-secondary">AI 智能健康管理 · 一站式养老服务</p>
    </div>

    <van-form @submit="onLogin">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          label="手机号"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确' }]"
        />
        <van-field
          v-model="form.password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
        <van-field
          v-model="form.code"
          center
          label="验证码"
          placeholder="请输入图形验证码"
          maxlength="4"
          :rules="[{ required: true, message: '请输入验证码' }]"
        >
          <template #button>
            <img :src="captchaUrl" class="captcha-img" alt="验证码" title="点击刷新" @click="refreshCaptcha" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="auth-actions">
        <van-button round block type="primary" native-type="submit" class="big-btn" :loading="loading">
          登 录
        </van-button>
        <div class="auth-links">
          <router-link to="/register">注册账号</router-link>
          <router-link to="/reset-password">忘记密码</router-link>
        </div>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast } from 'vant'
import { login } from '../../api/auth'
import { useUserStore } from '../../store/user'

const router = useRouter()
const route = useRoute()
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
  loading.value = true
  try {
    form.value.captchaId = captchaId.value
    const res = await login(form.value)
    userStore.setLogin(res.data)
    showSuccessToast('登录成功')
    router.replace(route.query.redirect || '/')
  } catch (e) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(180deg, var(--primary-tint) 0%, var(--bg-page) 40%);
}
.auth-header {
  text-align: center;
  margin-bottom: 30px;
}
.auth-logo {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  box-shadow: 0 6px 16px rgba(82, 168, 122, 0.35);
}
.auth-header h2 {
  margin: 0 0 8px;
  color: var(--primary-color);
  font-size: 1.6rem;
}
.auth-actions {
  padding: 24px 16px;
}
.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  font-size: 0.9rem;
}
.captcha-img {
  height: 34px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
