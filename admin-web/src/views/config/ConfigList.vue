<template>
  <div class="page">
    <div class="content-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="configKey" label="配置键" min-width="220" />
        <el-table-column prop="configValue" label="配置值" min-width="260" show-overflow-tooltip />
        <el-table-column prop="description" label="说明" min-width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" title="编辑配置" width="480px">
      <el-form label-width="80px">
        <el-form-item label="配置键">
          <el-input :model-value="form.configKey" disabled />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input v-model="form.configValue" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input :model-value="form.description" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfig } from '../../api/admin'

const list = ref([])
const loading = ref(false)
const visible = ref(false)
const form = reactive({ configKey: '', configValue: '', description: '' })

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getConfigs()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.assign(form, row)
  visible.value = true
}

async function onSave() {
  await updateConfig({ configKey: form.configKey, configValue: form.configValue })
  ElMessage.success('已保存')
  visible.value = false
  load()
}
</script>
