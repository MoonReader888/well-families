<template>
  <div class="page auth-page">
    <van-nav-bar title="找回密码" left-arrow @click-left="router.back()" />

    <van-form @submit="onReset">
      <van-cell-group inset>
        <van-field
          v-model="form.phone"
          label="手机号"
          type="tel"
          maxlength="11"
          placeholder="请输入注册手机号"
          :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确' }]"
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
        <van-field
          v-model="form.newPassword"
          label="新密码"
          type="password"
          placeholder="8 位以上，含字母和数字"
          :rules="[{ required: true, message: '请输入新密码' }, { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码需 8 位以上，含字母和数字' }]"
        />
      </van-cell-group>

      <div class="auth-actions">
        <van-button round block type="primary" native-type="submit" class="big-btn" :loading="loading">
          重置密码
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { resetPassword } from '../../api/auth'

const router = useRouter()
const form = ref({ phone: '', code: '', captchaId: '', newPassword: '' })
const loading = ref(false)
const captchaId = ref('')
const captchaUrl = ref('')

function refreshCaptcha() {
  captchaId.value = crypto.randomUUID()
  captchaUrl.value = '/api/captcha?id=' + captchaId.value
}

onMounted(refreshCaptcha)

async function onReset() {
  loading.value = true
  try {
    form.value.captchaId = captchaId.value
    await resetPassword(form.value)
    showSuccessToast('密码已重置，请重新登录')
    router.replace('/login')
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
  background: linear-gradient(180deg, var(--primary-tint) 0%, var(--bg-page) 40%);
}
.auth-actions {
  padding: 24px 16px;
}
.captcha-img {
  height: 34px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
