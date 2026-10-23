<template>
  <div class="page">
    <van-nav-bar title="修改密码" left-arrow @click-left="router.back()" />

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.oldPassword"
          label="原密码"
          type="password"
          placeholder="请输入原密码"
          :rules="[{ required: true, message: '请输入原密码' }]"
        />
        <van-field
          v-model="form.newPassword"
          label="新密码"
          type="password"
          placeholder="8 位以上，含字母和数字"
          :rules="[{ required: true, message: '请输入新密码' }, { pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/, message: '密码需 8 位以上，含字母和数字' }]"
        />
      </van-cell-group>

      <div class="actions">
        <van-button round block type="primary" native-type="submit" class="big-btn" :loading="loading">
          确认修改
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast, showToast } from 'vant'
import { changePassword } from '../../api/profile'
import { useUserStore } from '../../store/user'

const router = useRouter()
const userStore = useUserStore()
const form = ref({ oldPassword: '', newPassword: '' })
const loading = ref(false)

async function onSubmit() {
  if (form.value.oldPassword === form.value.newPassword) {
    return showToast('新密码不能与原密码相同')
  }
  loading.value = true
  try {
    await changePassword(form.value)
    showSuccessToast('修改成功，请重新登录')
    userStore.clearLogin()
    router.replace('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.actions {
  padding: 24px 16px;
}
</style>
