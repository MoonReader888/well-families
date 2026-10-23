<template>
  <div class="page">
    <van-nav-bar title="录入健康数据" left-arrow @click-left="router.back()" />

    <van-form @submit="onSubmit">
      <van-cell-group inset title="血压">
        <van-field v-model="form.systolic" label="收缩压" type="digit" placeholder="90-139 mmHg" />
        <van-field v-model="form.diastolic" label="舒张压" type="digit" placeholder="60-89 mmHg" />
      </van-cell-group>
      <van-cell-group inset title="其他指标">
        <van-field v-model="form.bloodSugar" label="空腹血糖" type="number" placeholder="3.9-6.1 mmol/L" />
        <van-field v-model="form.heartRate" label="心率" type="digit" placeholder="60-100 次/分" />
        <van-field v-model="form.weight" label="体重" type="number" placeholder="kg（自动计算 BMI）" />
        <van-field v-model="form.memo" label="备注" type="textarea" rows="2" placeholder="可选" maxlength="500" />
      </van-cell-group>

      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" class="big-btn" :loading="loading">
          保存记录
        </van-button>
        <p class="text-secondary tip">至少填写一项指标；指标异常时系统会自动推送健康提醒。</p>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { createHealthRecord } from '../../api/health'

const router = useRouter()
const form = ref({ systolic: '', diastolic: '', bloodSugar: '', heartRate: '', weight: '', memo: '' })
const loading = ref(false)

async function onSubmit() {
  const payload = {}
  Object.entries(form.value).forEach(([k, v]) => {
    if (v !== '' && v != null) payload[k] = v
  })
  if (Object.keys(payload).length === 0) {
    return showToast('请至少填写一项指标')
  }
  loading.value = true
  try {
    await createHealthRecord(payload)
    showSuccessToast('保存成功')
    router.back()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.submit-wrap {
  padding: 24px 16px;
}
.tip {
  text-align: center;
  margin-top: 12px;
  font-size: 0.8rem;
}
</style>
