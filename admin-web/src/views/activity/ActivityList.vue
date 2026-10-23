<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.title" placeholder="活动标题" clearable style="width: 200px" @keyup.enter="search" />
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="primary" @click="openEdit(null)">新增活动</el-button>
    </div>

    <div class="content-card">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="封面" width="110">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="'/uploads/' + row.coverUrl"
              :preview-src-list="['/uploads/' + row.coverUrl]"
              preview-teleported
              fit="cover"
              style="width: 80px; height: 45px; border-radius: 4px"
            />
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="活动标题" min-width="180" />
        <el-table-column prop="activityStart" label="活动开始" width="160" />
        <el-table-column prop="activityEnd" label="活动结束" width="160" />
        <el-table-column label="报名人数" width="100">
          <template #default="{ row }">{{ row.currentParticipants }}/{{ row.maxParticipants }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRegistrations(row)">报名</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <!-- 活动编辑 -->
    <el-dialog v-model="visible" :title="form.id ? '编辑活动' : '新增活动'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="活动标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="活动封面">
          <div class="cover-uploader">
            <el-upload
              action="#"
              :show-file-list="false"
              :before-upload="beforeCoverUpload"
              :http-request="doCoverUpload"
              accept="image/jpeg,image/png,image/gif,image/webp"
            >
              <img v-if="coverPreview" :src="coverPreview" class="cover-preview" />
              <el-button v-else>选择图片</el-button>
            </el-upload>
            <el-button v-if="form.coverUrl" link type="danger" @click="removeCover">移除</el-button>
          </div>
        </el-form-item>
        <el-form-item label="活动内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="报名时间">
          <el-date-picker v-model="form.registrationRange" type="datetimerange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" placement="top-start" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="form.activityRange" type="datetimerange" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD HH:mm:ss" placement="top-start" style="width: 100%" />
        </el-form-item>
        <el-form-item label="人数上限"><el-input-number v-model="form.maxParticipants" :min="1" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="报名中" value="REGISTRATING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已结束" value="ENDED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 报名列表 -->
    <el-drawer v-model="regVisible" :title="`报名列表：${currentAct?.title || ''}`" size="480px">
      <el-table :data="registrations" size="small" border>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column label="签到" width="80">
          <template #default="{ row }">
            <el-tag :type="row.checkInStatus === 'CHECKED_IN' ? 'success' : 'info'" size="small">
              {{ row.checkInStatus === 'CHECKED_IN' ? '已签到' : '未签到' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报名时间" width="160" />
      </el-table>

      <div class="pagination">
        <el-pagination
          layout="total, prev, pager, next"
          :total="regTotal"
          :page-size="regQuery.pageSize"
          :current-page="regQuery.pageNum"
          @current-change="onRegPage"
        />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivities, createActivity, updateActivity, deleteActivity, getActivityRegistrations, uploadActivityCover } from '../../api/admin'

const statusLabel = (s) => ({ DRAFT: '草稿', REGISTRATING: '报名中', IN_PROGRESS: '进行中', ENDED: '已结束' })[s] || s

const query = reactive({ pageNum: 1, pageSize: 10, title: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const visible = ref(false)
const form = reactive({
  id: null, title: '', content: '', coverUrl: '', registrationRange: [], activityRange: [], maxParticipants: 50, status: 'DRAFT'
})

const coverPreview = computed(() => (form.coverUrl ? '/uploads/' + form.coverUrl : ''))

function beforeCoverUpload(file) {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.warning('封面仅支持 JPG/PNG/GIF/WEBP 格式')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('封面不能超过 5MB')
    return false
  }
  return true
}

async function doCoverUpload({ file }) {
  const res = await uploadActivityCover(file)
  form.coverUrl = res.data.path
  ElMessage.success('封面上传成功')
}

function removeCover() {
  form.coverUrl = ''
}

const regVisible = ref(false)
const currentAct = ref(null)
const registrations = ref([])
const regQuery = reactive({ pageNum: 1, pageSize: 10 })
const regTotal = ref(0)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await getActivities(query)
    list.value = res.data.list || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onPage(page) {
  query.pageNum = page
  load()
}

function search() {
  query.pageNum = 1
  load()
}

function openEdit(row) {
  if (row) {
    Object.assign(form, row, {
      registrationRange: row.registrationStart && row.registrationEnd ? [row.registrationStart, row.registrationEnd] : [],
      activityRange: row.activityStart && row.activityEnd ? [row.activityStart, row.activityEnd] : []
    })
  } else {
    Object.assign(form, { id: null, title: '', content: '', coverUrl: '', registrationRange: [], activityRange: [], maxParticipants: 50, status: 'DRAFT' })
  }
  visible.value = true
}

async function onSave() {
  const regStart = form.registrationRange?.[0] || null
  const regEnd = form.registrationRange?.[1] || null
  const actStart = form.activityRange?.[0] || null
  const actEnd = form.activityRange?.[1] || null
  if (regStart && regEnd && regStart >= regEnd) return ElMessage.warning('报名开始时间必须早于报名结束时间')
  if (actStart && actEnd && actStart >= actEnd) return ElMessage.warning('活动开始时间必须早于活动结束时间')
  if (regEnd && actStart && regEnd > actStart) return ElMessage.warning('报名结束时间必须不晚于活动开始时间')

  const payload = { ...form, registrationRange: undefined, activityRange: undefined }
  payload.registrationStart = regStart
  payload.registrationEnd = regEnd
  payload.activityStart = actStart
  payload.activityEnd = actEnd
  if (form.id) {
    await updateActivity(form.id, payload)
  } else {
    await createActivity(payload)
  }
  ElMessage.success('已保存')
  visible.value = false
  load()
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await deleteActivity(row.id)
  ElMessage.success('已删除')
  load()
}

async function openRegistrations(row) {
  currentAct.value = row
  regVisible.value = true
  regQuery.pageNum = 1
  await loadRegistrations()
}

async function loadRegistrations() {
  const res = await getActivityRegistrations(currentAct.value.id, regQuery)
  registrations.value = res.data.list || []
  regTotal.value = res.data.total || 0
}

function onRegPage(page) {
  regQuery.pageNum = page
  loadRegistrations()
}
</script>

<style scoped>
.cover-uploader {
  display: flex;
  align-items: center;
  gap: 8px;
}
.cover-preview {
  display: block;
  width: 200px;
  height: 112px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
}
</style>
