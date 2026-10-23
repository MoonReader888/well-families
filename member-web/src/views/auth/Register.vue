<template>
  <div class="page auth-page">
    <van-nav-bar title="注册账号" left-arrow @click-left="router.back()" />

    <van-form @submit="onRegister">
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
          v-model="form.password"
          label="密码"
          type="password"
          placeholder="8 位以上，含字母和数字"
          :rules="[{ required: true, message: '请输入密码' }, { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码需 8 位以上，含字母和数字' }]"
        />
      </van-cell-group>

      <div class="auth-actions">
        <van-button round block type="primary" native-type="submit" class="big-btn" :loading="loading">
          注 册
        </van-button>
        <p class="text-secondary auth-tip">注册即赠 100 积分</p>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { register } from '../../api/auth'

const router = useRouter()
const form = ref({ phone: '', code: '', captchaId: '', password: '' })
const loading = ref(false)
const captchaId = ref('')
const captchaUrl = ref('')

function refreshCaptcha() {
  captchaId.value = crypto.randomUUID()
  captchaUrl.value = '/api/captcha?id=' + captchaId.value
}

onMounted(refreshCaptcha)

async function onRegister() {
  loading.value = true
  try {
    form.value.captchaId = captchaId.value
    await register(form.value)
    showSuccessToast('注册成功，请登录')
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
.auth-tip {
  text-align: center;
  margin-top: 12px;
}
.captcha-img {
  height: 34px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
