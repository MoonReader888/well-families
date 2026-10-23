<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.phone" placeholder="手机号" clearable style="width: 180px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <div class="spacer" />
      <el-button type="primary" @click="openPush(false)">推送消息</el-button>
      <el-button type="success" @click="openPush(true)">批量推送</el-button>
    </div>

    <div class="content-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column label="已读" width="70">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'info' : 'danger'" size="small">{{ row.isRead === 1 ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="170" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.pageSize"
          :current-page="query.pageNum"
          @current-change="onPage"
        />
      </div>
    </div>

    <el-dialog v-model="pushVisible" :title="isBatch ? '批量推送消息' : '推送消息'" width="480px">
      <el-form label-width="90px">
        <el-form-item v-if="!isBatch" label="用户 ID">
          <el-input v-model="pushForm.userId" placeholder="接收用户 ID" />
        </el-form-item>
        <template v-else>
          <el-form-item label="目标人群">
            <el-radio-group v-model="pushForm.targetMode">
              <el-radio value="all">全部会员</el-radio>
              <el-radio value="condition">按条件筛选</el-radio>
            </el-radio-group>
          </el-form-item>
          <template v-if="pushForm.targetMode === 'condition'">
            <el-form-item label="会员等级">
              <el-select v-model="pushForm.memberLevel" clearable placeholder="不限" style="width: 100%">
                <el-option v-for="lv in levels" :key="lv" :label="lv" :value="lv" />
              </el-select>
            </el-form-item>
            <el-form-item label="账号状态">
              <el-select v-model="pushForm.status" clearable placeholder="不限" style="width: 100%">
                <el-option label="正常" value="ENABLED" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
            <el-form-item label="注册时间">
              <el-date-picker
                v-model="pushForm.dateRange"
                type="daterange"
                value-format="YYYY-MM-DD HH:mm:ss"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </template>
          <el-form-item label="目标人数">
            <span>将发送给 {{ pushCount }} 位会员</span>
          </el-form-item>
        </template>
        <el-form-item label="标题"><el-input v-model="pushForm.title" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="pushForm.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="pushForm.type" style="width: 100%">
            <el-option label="系统" value="SYSTEM" />
            <el-option label="预约" value="APPOINTMENT" />
            <el-option label="活动" value="ACTIVITY" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pushVisible = false">取消</el-button>
        <el-button type="primary" @click="onPush">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMessages, deleteMessage, pushMessage, pushBatchMessage, getPushCount } from '../../api/admin'

const levels = ['NORMAL', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND']

const query = reactive({ pageNum: 1, pageSize: 10, phone: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const pushVisible = ref(false)
const isBatch = ref(false)
const pushForm = reactive({ userId: '', title: '', content: '', type: 'SYSTEM', targetMode: 'all', memberLevel: '', status: '', dateRange: [] })
const pushCount = ref(0)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getMessages(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function onPage(page) {
  query.pageNum = page
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除该消息吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteMessage(row.id)
  ElMessage.success('已删除')
  load()
}

function openPush(batch) {
  isBatch.value = batch
  Object.assign(pushForm, { userId: '', title: '', content: '', type: 'SYSTEM', targetMode: 'all', memberLevel: '', status: '', dateRange: [] })
  pushVisible.value = true
  refreshCount()
}

function targetParams() {
  const params = {}
  if (pushForm.memberLevel) params.memberLevel = pushForm.memberLevel
  if (pushForm.status) params.status = pushForm.status
  if (pushForm.dateRange?.length) {
    params.startTime = pushForm.dateRange[0]
    params.endTime = pushForm.dateRange[1]
  }
  return params
}

async function refreshCount() {
  if (!isBatch.value) return
  const params = pushForm.targetMode === 'condition' ? targetParams() : {}
  const res = await getPushCount(params)
  pushCount.value = res.data || 0
}

watch(
  () => [pushForm.targetMode, pushForm.memberLevel, pushForm.status, pushForm.dateRange],
  refreshCount,
  { deep: true }
)

async function onPush() {
  if (isBatch.value) {
    if (pushForm.targetMode === 'condition' && !Object.keys(targetParams()).length) {
      return ElMessage.warning('请选择至少一个筛选条件')
    }
    await pushBatchMessage({
      ...targetParams(),
      title: pushForm.title,
      content: pushForm.content,
      type: pushForm.type
    })
  } else {
    if (!pushForm.userId) return ElMessage.warning('请填写用户 ID')
    await pushMessage({ userId: Number(pushForm.userId), title: pushForm.title, content: pushForm.content, type: pushForm.type })
  }
  ElMessage.success('已发送')
  pushVisible.value = false
  load()
}
</script>
